package com.bervan.toolsapp.security;

import com.bervan.common.user.User;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Deprecated
public class QRLoginService {

    private final Map<Integer, QRLoginData> qrLoginSessions = new ConcurrentHashMap<>();
    private final Map<String, QRSessionWaiting> waitingSessions = new ConcurrentHashMap<>();

    public QRLoginData generateQRLogin() {
        String sessionId = VaadinSession.getCurrent().getSession().getId();
        int uuid = ThreadLocalRandom.current().nextInt(1000, 9999);
        int number = ThreadLocalRandom.current().nextInt(1, 100); // 01-99

        int maxAttempts = 100;
        while (qrLoginSessions.containsKey(uuid) && --maxAttempts > 0) {
            uuid = ThreadLocalRandom.current().nextInt(1000, 9999);
        }

        if (maxAttempts <= 0) {
            throw new RuntimeException("Unable to generate unique QR code!");
        }

        QRLoginData data = new QRLoginData(uuid, number, sessionId);
        qrLoginSessions.put(uuid, data);

        // Create waiting session
        QRSessionWaiting waitingSession = new QRSessionWaiting(sessionId);
        waitingSessions.put(sessionId, waitingSession);

        // Clean expired sessions
        cleanExpiredSessions();

        return data;
    }

    public byte[] generateQRCode(String url) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 200, 200);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

        return outputStream.toByteArray();
    }

    public QRLoginData getQRLoginData(String uuid) {
        return qrLoginSessions.get(Integer.valueOf(uuid));
    }

    public boolean validateAndAuthenticateQRLogin(String uuid, int enteredNumber, User authenticatingUser) {
        QRLoginData data = qrLoginSessions.get(Integer.valueOf(uuid));
        if (data == null || data.isExpired() || data.isUsed()) {
            return false;
        }

        if (data.getNumber() == enteredNumber) {
            data.setUsed(true);

            // Set the authenticated user for the waiting session
            QRSessionWaiting waitingSession = waitingSessions.get(data.getOriginSessionId());
            if (waitingSession != null && !waitingSession.isExpired()) {
                waitingSession.setAuthenticatedUser(authenticatingUser);
                waitingSession.setCompleted(true);
                return true;
            }
        }

        return false;
    }

    public QRSessionWaiting getWaitingSession(String sessionId) {
        return waitingSessions.get(sessionId);
    }

    public void cleanupSession(String sessionId) {
        waitingSessions.remove(sessionId);
    }

    private void cleanExpiredSessions() {
        qrLoginSessions.entrySet().removeIf(entry -> entry.getValue().isExpired());
        waitingSessions.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    public String buildFullUrl(int uuid) {
        VaadinRequest request = VaadinRequest.getCurrent();
        if (request != null) {
            String scheme = request.getHeader("X-Forwarded-Proto");
            if (scheme == null) {
                scheme = request.isSecure() ? "https" : "http";
            }

            String host = request.getHeader("X-Forwarded-Host");
            if (host == null) {
                host = request.getHeader("Host");
            }
            if (host == null) {
                host = request.getRemoteHost();
                int port = request.getRemotePort();
                if ((scheme.equals("http") && port != 80) || (scheme.equals("https") && port != 443)) {
                    host = host + ":" + port;
                }
            }

            return scheme + "://" + host + "/accept-login/" + uuid;
        }

        throw new RuntimeException("Unable to build full URL!");
    }

    public static class QRLoginData {
        private final int uuid;
        private final int number;
        private final long createdTime;
        private final String originSessionId;
        private boolean used;

        public QRLoginData(int uuid, int number, String originSessionId) {
            this.uuid = uuid;
            this.number = number;
            this.originSessionId = originSessionId;
            this.createdTime = System.currentTimeMillis();
            this.used = false;
        }

        public Integer getUuid() {
            return uuid;
        }

        public int getNumber() {
            return number;
        }

        public String getOriginSessionId() {
            return originSessionId;
        }

        public long getCreatedTime() {
            return createdTime;
        }

        public boolean isUsed() {
            return used;
        }

        public void setUsed(boolean used) {
            this.used = used;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - createdTime > 300000; // 5 minutes
        }
    }

    public static class QRSessionWaiting {
        private final String sessionId;
        private final long createdTime;
        private User authenticatedUser;
        private boolean completed;

        public QRSessionWaiting(String sessionId) {
            this.sessionId = sessionId;
            this.createdTime = System.currentTimeMillis();
            this.completed = false;
        }

        public String getSessionId() {
            return sessionId;
        }

        public User getAuthenticatedUser() {
            return authenticatedUser;
        }

        public void setAuthenticatedUser(User user) {
            this.authenticatedUser = user;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - createdTime > 300000; // 5 minutes
        }
    }
}