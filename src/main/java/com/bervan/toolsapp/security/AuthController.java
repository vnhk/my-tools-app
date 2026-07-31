package com.bervan.toolsapp.security;

import com.bervan.common.user.User;
import com.bervan.common.user.UserRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final OTPService otpService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          OTPService otpService,
                          JwtService jwtService,
                          UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.otpService = otpService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user;
            if (request.otp() != null && !request.otp().isBlank()) {
                user = otpService.login(request.otp());
            } else {
                Authentication auth = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(request.username(), request.password())
                );
                user = (User) auth.getPrincipal();
            }

            String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());
            return ResponseEntity.ok(new LoginResponse(token, user.getUsername(), user.getRole()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new MeResponse(user.getId().toString(), user.getUsername(), user.getRole()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // JWT is stateless — client drops the token from localStorage
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    // ---- QR Login (REST, session-independent) ----

    private static class QrEntry {
        final int uuid;
        final int number;
        final String pollToken;
        // Set when the QR was shown by a TV — lets the confirming phone
        // auto-pair its remote control to the same room.
        final String roomId;
        final long created;
        volatile boolean done;
        volatile User user;

        QrEntry(int uuid, int number, String pollToken, String roomId) {
            this.uuid = uuid;
            this.number = number;
            this.pollToken = pollToken;
            this.roomId = roomId;
            this.created = System.currentTimeMillis();
        }

        boolean expired() { return System.currentTimeMillis() - created > 300_000; }
    }

    record QrGenerateRequest(String roomId) {}

    private final ConcurrentHashMap<Integer, QrEntry> qrByUuid = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, QrEntry> qrByPoll = new ConcurrentHashMap<>();

    @PostMapping("/qr/generate")
    public ResponseEntity<?> qrGenerate(HttpServletRequest request,
                                        @RequestBody(required = false) QrGenerateRequest body) {
        int uuid = ThreadLocalRandom.current().nextInt(1000, 9999);
        int attempts = 0;
        while (qrByUuid.containsKey(uuid) && ++attempts < 100)
            uuid = ThreadLocalRandom.current().nextInt(1000, 9999);
        int number = ThreadLocalRandom.current().nextInt(1, 100);
        String pollToken = UUID.randomUUID().toString();
        String roomId = body != null && body.roomId() != null && !body.roomId().isBlank() ? body.roomId() : null;
        QrEntry entry = new QrEntry(uuid, number, pollToken, roomId);
        qrByUuid.put(uuid, entry);
        qrByPoll.put(pollToken, entry);
        qrByUuid.entrySet().removeIf(e -> e.getValue().expired());

        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null) scheme = request.isSecure() ? "https" : "http";
        String host = request.getHeader("X-Forwarded-Host");
        if (host == null) host = request.getHeader("Host");
        String qrUrl = scheme + "://" + host + "/accept-login/" + uuid;

        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(qrUrl, BarcodeFormat.QR_CODE, 200, 200);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            String b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return ResponseEntity.ok(Map.of("uuid", uuid, "number", number, "pollToken", pollToken, "qrImage", b64));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "QR generation failed"));
        }
    }

    @GetMapping("/qr/poll")
    public ResponseEntity<?> qrPoll(@RequestParam String pollToken) {
        QrEntry entry = qrByPoll.get(pollToken);
        if (entry == null || entry.expired()) return ResponseEntity.status(HttpStatus.GONE).build();
        if (entry.done && entry.user != null) {
            User u = entry.user;
            qrByPoll.remove(pollToken);
            qrByUuid.remove(entry.uuid);
            String token = jwtService.generateToken(u.getId(), u.getUsername(), u.getRole());
            return ResponseEntity.ok(Map.of("done", true, "token", token, "username", u.getUsername(), "role", u.getRole()));
        }
        return ResponseEntity.ok(Map.of("done", false));
    }

    @PostMapping("/qr/confirm")
    public ResponseEntity<?> qrConfirm(@RequestParam int uuid, @RequestParam int number,
                                       @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        QrEntry entry = qrByUuid.get(uuid);
        if (entry == null || entry.expired() || entry.done)
            return ResponseEntity.status(HttpStatus.GONE).body(Map.of("error", "QR code expired or already used"));
        if (entry.number != number)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Wrong number"));
        entry.done = true;
        entry.user = user;
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Confirmed");
        if (entry.roomId != null) response.put("roomId", entry.roomId);
        return ResponseEntity.ok(response);
    }

    // ---- OTP generation ----

    @PostMapping("/otp/generate")
    public ResponseEntity<?> generateOtp(@RequestParam(defaultValue = "ROLE_STREAMING") String role,
                                         @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            String otpCode = otpService.generateOTP(user.getId(), role);
            return ResponseEntity.ok(Map.of("otp", otpCode, "role", role));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // ---- User settings ----

    @GetMapping("/settings")
    public ResponseEntity<?> getSettings(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User fresh = userRepository.findById(user.getId()).orElse(user);
        boolean hasCipher = fresh.getDataCipherPassword() != null && !fresh.getDataCipherPassword().isEmpty();
        return ResponseEntity.ok(Map.of("hasCipher", hasCipher, "role", fresh.getRole()));
    }

    @PostMapping("/settings/cipher/verify")
    public ResponseEntity<?> verifyCipher(@RequestBody CipherRequest req, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User fresh = userRepository.findById(user.getId()).orElse(user);
        boolean valid = req.cipher() != null && req.cipher().equals(fresh.getDataCipherPassword());
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    @PostMapping("/settings/cipher")
    public ResponseEntity<?> saveCipher(@RequestBody CipherRequest req, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (req.cipher() == null || req.cipher().length() < 5)
            return ResponseEntity.badRequest().body(Map.of("error", "Cipher must be at least 5 characters"));
        User fresh = userRepository.findById(user.getId()).orElse(user);
        fresh.setDataCipherPassword(req.cipher());
        userRepository.save(fresh);
        return ResponseEntity.ok(Map.of("message", "Cipher saved"));
    }

    record LoginRequest(String username, String password, String otp) {}
    record LoginResponse(String token, String username, String role) {}
    record MeResponse(String id, String username, String role) {}
    record CipherRequest(String cipher) {}
}
