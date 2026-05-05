package com.bervan.toolsapp.views.investtrackapp;

import com.bervan.common.service.AuthService;
import com.bervan.investtrack.model.Wallet;
import com.bervan.investtrack.model.WalletSnapshot;
import com.bervan.investtrack.service.InvestmentCalculationService;
import com.bervan.investtrack.service.WalletService;
import com.bervan.investtrack.service.WalletSnapshotService;
import com.bervan.common.config.EntityConfigValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/invest-track/wallets")
public class WalletRestController {

    private final WalletService walletService;
    private final WalletSnapshotService snapshotService;
    private final InvestmentCalculationService calculationService;
    private final EntityConfigValidator validator;

    public WalletRestController(WalletService walletService, WalletSnapshotService snapshotService,
                                 InvestmentCalculationService calculationService, EntityConfigValidator validator) {
        this.walletService = walletService;
        this.snapshotService = snapshotService;
        this.calculationService = calculationService;
        this.validator = validator;
    }

    record WalletDto(
            UUID id, String name, String description, String currency, String riskLevel,
            String walletType, Boolean compareWithSP500, LocalDateTime createdDate, LocalDateTime modificationDate,
            BigDecimal currentValue, BigDecimal totalDeposits, BigDecimal totalWithdrawals, BigDecimal totalEarnings, BigDecimal returnRate
    ) {}

    record WalletSnapshotDto(
            UUID id, UUID walletId, LocalDate snapshotDate, BigDecimal portfolioValue,
            BigDecimal monthlyDeposit, BigDecimal monthlyWithdrawal, BigDecimal monthlyEarnings, String notes
    ) {}

    record WalletCreateRequest(
            String name, String description, String currency, String riskLevel,
            String walletType, Boolean compareWithSP500
    ) {}

    record ValidationErrorResponse(List<EntityConfigValidator.FieldError> errors) {}

    private WalletDto toDto(Wallet w) {
        return new WalletDto(
                w.getId(), w.getName(), w.getDescription(), w.getCurrency(), w.getRiskLevel(),
                w.getWalletType(), w.getCompareWithSP500(), w.getCreatedDate(), w.getModificationDate(),
                w.getCurrentValue(), w.getTotalDeposits(), w.getTotalWithdrawals(), w.getTotalEarnings(), w.getReturnRate()
        );
    }

    @GetMapping
    public ResponseEntity<Page<WalletDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {

        Set<Wallet> wallets = walletService.load(PageRequest.of(0, Integer.MAX_VALUE));
        List<WalletDto> dtos = wallets.stream()
                .map(this::toDto)
                .sorted(Comparator.comparing(WalletDto::name, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        int total = dtos.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        return ResponseEntity.ok(new PageImpl<>(dtos.subList(from, to), PageRequest.of(page, size), total));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WalletDto> getById(@PathVariable UUID id) {
        return walletService.load(PageRequest.of(0, Integer.MAX_VALUE)).stream()
                .filter(w -> w.getId().equals(id))
                .findFirst()
                .map(w -> ResponseEntity.ok(toDto(w)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/snapshots")
    public ResponseEntity<List<WalletSnapshotDto>> getSnapshots(@PathVariable UUID id) {
        List<WalletSnapshot> snapshots = snapshotService.findByWalletId(id);
        List<WalletSnapshotDto> dtos = snapshots.stream()
                .map(s -> new WalletSnapshotDto(s.getId(), id, s.getSnapshotDate(),
                        s.getPortfolioValue(), s.getMonthlyDeposit(), s.getMonthlyWithdrawal(),
                        s.getMonthlyEarnings(), s.getNotes()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics(@PathVariable UUID id) {
        Optional<Wallet> walletOpt = walletService.load(PageRequest.of(0, Integer.MAX_VALUE)).stream()
                .filter(w -> w.getId().equals(id)).findFirst();
        if (walletOpt.isEmpty()) return ResponseEntity.notFound().build();

        Wallet wallet = walletOpt.get();
        List<WalletSnapshot> snapshots = snapshotService.findByWalletId(id);

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("currentValue", wallet.getCurrentValue());
        metrics.put("totalDeposits", wallet.getTotalDeposits());
        metrics.put("totalWithdrawals", wallet.getTotalWithdrawals());
        metrics.put("totalEarnings", wallet.getTotalEarnings());
        metrics.put("returnRate", wallet.getReturnRate());

        if (snapshots.size() >= 2) {
            BigDecimal twr = calculationService.calculateTWR(snapshots);
            metrics.put("twr", twr.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP));

            WalletSnapshot first = snapshots.stream().min(Comparator.comparing(WalletSnapshot::getSnapshotDate)).get();
            WalletSnapshot last = snapshots.stream().max(Comparator.comparing(WalletSnapshot::getSnapshotDate)).get();
            double years = ChronoUnit.DAYS.between(first.getSnapshotDate(), last.getSnapshotDate()) / 365.0;
            if (years > 0 && first.getPortfolioValue().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal cagr = calculationService.calculateCAGR(first.getPortfolioValue(), last.getPortfolioValue(), years);
                metrics.put("cagr", cagr.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP));
            }

            metrics.put("monthlyReturns", calculationService.calculateMonthlyReturns(snapshots));
            metrics.put("yearlyReturns", calculationService.calculateYearlyReturns(snapshots));
        }
        return ResponseEntity.ok(metrics);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody WalletCreateRequest req) {

        Map<String, Object> fields = new LinkedHashMap<>();
        if (req.name() != null) fields.put("name", req.name());
        if (req.description() != null) fields.put("description", req.description());
        if (req.currency() != null) fields.put("currency", req.currency());
        if (req.riskLevel() != null) fields.put("riskLevel", req.riskLevel());
        if (req.walletType() != null) fields.put("walletType", req.walletType());

        List<EntityConfigValidator.FieldError> errors = validator.validate("Wallet", fields);
        if (!errors.isEmpty()) return ResponseEntity.badRequest().body(new ValidationErrorResponse(errors));

        Wallet wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        wallet.setName(req.name());
        wallet.setDescription(req.description());
        wallet.setCurrency(req.currency());
        wallet.setRiskLevel(req.riskLevel());
        wallet.setWalletType(req.walletType() != null ? req.walletType() : "INVESTMENT");
        wallet.setCompareWithSP500(req.compareWithSP500());
        wallet.setModificationDate(LocalDateTime.now());
        wallet.setDeleted(false);
        wallet.addOwner(AuthService.getLoggedUser().get());
        Wallet saved = walletService.save(wallet);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {

        List<EntityConfigValidator.FieldError> errors = validator.validate("Wallet", req);
        if (!errors.isEmpty()) return ResponseEntity.badRequest().body(new ValidationErrorResponse(errors));

        Optional<Wallet> match = walletService.load(PageRequest.of(0, Integer.MAX_VALUE)).stream()
                .filter(w -> w.getId().equals(id)).findFirst();
        if (match.isEmpty()) return ResponseEntity.notFound().build();

        Wallet wallet = match.get();
        if (req.containsKey("name")) wallet.setName((String) req.get("name"));
        if (req.containsKey("description")) wallet.setDescription((String) req.get("description"));
        if (req.containsKey("currency")) wallet.setCurrency((String) req.get("currency"));
        if (req.containsKey("riskLevel")) wallet.setRiskLevel((String) req.get("riskLevel"));
        if (req.containsKey("walletType")) wallet.setWalletType((String) req.get("walletType"));
        if (req.containsKey("compareWithSP500")) wallet.setCompareWithSP500((Boolean) req.get("compareWithSP500"));
        wallet.setModificationDate(LocalDateTime.now());
        Wallet saved = walletService.save(wallet);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        Optional<Wallet> match = walletService.load(PageRequest.of(0, Integer.MAX_VALUE)).stream()
                .filter(w -> w.getId().equals(id)).findFirst();
        if (match.isEmpty()) return ResponseEntity.notFound().build();
        walletService.deleteWallet(id);
        return ResponseEntity.noContent().build();
    }

    // ── Snapshot endpoints ──────────────────────────────────────────────────────

    record SnapshotCreateRequest(
            LocalDate snapshotDate, BigDecimal portfolioValue, BigDecimal monthlyDeposit,
            BigDecimal monthlyWithdrawal, BigDecimal monthlyEarnings, String notes
    ) {}

    @PostMapping("/{walletId}/snapshots")
    public ResponseEntity<?> createSnapshot(@PathVariable UUID walletId, @RequestBody SnapshotCreateRequest req) {

        Optional<Wallet> walletOpt = walletService.load(PageRequest.of(0, Integer.MAX_VALUE)).stream()
                .filter(w -> w.getId().equals(walletId)).findFirst();
        if (walletOpt.isEmpty()) return ResponseEntity.notFound().build();

        WalletSnapshot snapshot = new WalletSnapshot();
        snapshot.setId(UUID.randomUUID());
        snapshot.setWallet(walletOpt.get());
        snapshot.setSnapshotDate(req.snapshotDate());
        snapshot.setPortfolioValue(req.portfolioValue());
        snapshot.setMonthlyDeposit(req.monthlyDeposit() != null ? req.monthlyDeposit() : BigDecimal.ZERO);
        snapshot.setMonthlyWithdrawal(req.monthlyWithdrawal() != null ? req.monthlyWithdrawal() : BigDecimal.ZERO);
        snapshot.setMonthlyEarnings(req.monthlyEarnings() != null ? req.monthlyEarnings() : BigDecimal.ZERO);
        snapshot.setNotes(req.notes());
        snapshot.setDeleted(false);
        snapshot.addOwner(AuthService.getLoggedUser().get());
        WalletSnapshot saved = snapshotService.save(snapshot);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new WalletSnapshotDto(saved.getId(), walletId, saved.getSnapshotDate(),
                        saved.getPortfolioValue(), saved.getMonthlyDeposit(), saved.getMonthlyWithdrawal(),
                        saved.getMonthlyEarnings(), saved.getNotes()));
    }

    @PutMapping("/{walletId}/snapshots/{snapshotId}")
    public ResponseEntity<?> updateSnapshot(@PathVariable UUID walletId, @PathVariable UUID snapshotId,
                                             @RequestBody Map<String, Object> req) {

        List<WalletSnapshot> snapshots = snapshotService.findByWalletId(walletId);
        Optional<WalletSnapshot> match = snapshots.stream().filter(s -> s.getId().equals(snapshotId)).findFirst();
        if (match.isEmpty()) return ResponseEntity.notFound().build();

        WalletSnapshot snapshot = match.get();
        if (req.containsKey("snapshotDate")) snapshot.setSnapshotDate(LocalDate.parse((String) req.get("snapshotDate")));
        if (req.containsKey("portfolioValue")) snapshot.setPortfolioValue(new BigDecimal(req.get("portfolioValue").toString()));
        if (req.containsKey("monthlyDeposit")) snapshot.setMonthlyDeposit(new BigDecimal(req.get("monthlyDeposit").toString()));
        if (req.containsKey("monthlyWithdrawal")) snapshot.setMonthlyWithdrawal(new BigDecimal(req.get("monthlyWithdrawal").toString()));
        if (req.containsKey("monthlyEarnings")) snapshot.setMonthlyEarnings(new BigDecimal(req.get("monthlyEarnings").toString()));
        if (req.containsKey("notes")) snapshot.setNotes((String) req.get("notes"));
        WalletSnapshot saved = snapshotService.save(snapshot);
        return ResponseEntity.ok(new WalletSnapshotDto(saved.getId(), walletId, saved.getSnapshotDate(),
                saved.getPortfolioValue(), saved.getMonthlyDeposit(), saved.getMonthlyWithdrawal(),
                saved.getMonthlyEarnings(), saved.getNotes()));
    }

    @DeleteMapping("/{walletId}/snapshots/{snapshotId}")
    public ResponseEntity<Void> deleteSnapshot(@PathVariable UUID walletId, @PathVariable UUID snapshotId) {
        List<WalletSnapshot> snapshots = snapshotService.findByWalletId(walletId);
        if (snapshots.stream().noneMatch(s -> s.getId().equals(snapshotId))) return ResponseEntity.notFound().build();
        walletService.deleteSnapshot(snapshotId);
        return ResponseEntity.noContent().build();
    }
}
