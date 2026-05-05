package com.bervan.toolsapp.views.investtrackapp;

import com.bervan.common.service.AuthService;
import com.bervan.investtrack.model.StockPriceAlert;
import com.bervan.investtrack.model.StockPriceAlertConfig;
import com.bervan.investtrack.service.StockPriceAlertConfigService;
import com.bervan.investtrack.service.StockPriceAlertService;
import com.bervan.common.config.EntityConfigValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/invest-track/stock-alerts")
public class StockAlertRestController {

    private final StockPriceAlertService alertService;
    private final StockPriceAlertConfigService configService;
    private final EntityConfigValidator validator;

    public StockAlertRestController(StockPriceAlertService alertService,
                                     StockPriceAlertConfigService configService,
                                     EntityConfigValidator validator) {
        this.alertService = alertService;
        this.configService = configService;
        this.validator = validator;
    }

    record AlertConfigDto(
            UUID id, BigDecimal price, String operator, Integer amountOfNotifications,
            Integer checkIntervalMinutes, Integer anotherNotificationEachPercentage,
            LocalDateTime previouslyNotifiedDate, BigDecimal previouslyNotifiedPrice
    ) {}

    record StockAlertDto(
            UUID id, String name, String symbol, String exchange, List<String> emails, AlertConfigDto config
    ) {}

    record StockAlertRequest(
            String name, String symbol, String exchange, List<String> emails,
            BigDecimal price, String operator, Integer amountOfNotifications,
            Integer checkIntervalMinutes, Integer anotherNotificationEachPercentage
    ) {}

    record ValidationErrorResponse(List<EntityConfigValidator.FieldError> errors) {}

    private AlertConfigDto configToDto(StockPriceAlertConfig c) {
        if (c == null) return null;
        return new AlertConfigDto(c.getId(), c.getPrice(), c.getOperator(), c.getAmountOfNotifications(),
                c.getCheckIntervalMinutes(), c.getAnotherNotificationEachPercentage(),
                c.getPreviouslyNotifiedDate(), c.getPreviouslyNotifiedPrice());
    }

    private StockAlertDto toDto(StockPriceAlert a) {
        return new StockAlertDto(a.getId(), a.getName(), a.getSymbol(), a.getExchange(),
                a.getEmails() != null ? new ArrayList<>(a.getEmails()) : List.of(),
                configToDto(a.getStockPriceAlertConfig()));
    }

    @GetMapping
    public ResponseEntity<Page<StockAlertDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        Set<StockPriceAlert> all = alertService.load(PageRequest.of(0, Integer.MAX_VALUE));
        List<StockAlertDto> dtos = all.stream()
                .map(this::toDto)
                .sorted(Comparator.comparing(StockAlertDto::symbol, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        int total = dtos.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        return ResponseEntity.ok(new PageImpl<>(dtos.subList(from, to), PageRequest.of(page, size), total));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody StockAlertRequest req) {

        Map<String, Object> fields = new LinkedHashMap<>();
        if (req.name() != null) fields.put("name", req.name());
        if (req.symbol() != null) fields.put("symbol", req.symbol());
        if (req.exchange() != null) fields.put("exchange", req.exchange());

        List<EntityConfigValidator.FieldError> errors = validator.validate("StockPriceAlert", fields);
        if (!errors.isEmpty()) return ResponseEntity.badRequest().body(new ValidationErrorResponse(errors));

        StockPriceAlertConfig config = new StockPriceAlertConfig();
        config.setId(UUID.randomUUID());
        config.setPrice(req.price());
        config.setOperator(req.operator());
        config.setAmountOfNotifications(req.amountOfNotifications() != null ? req.amountOfNotifications() : 1);
        config.setCheckIntervalMinutes(req.checkIntervalMinutes() != null ? req.checkIntervalMinutes() : 60);
        config.setAnotherNotificationEachPercentage(req.anotherNotificationEachPercentage() != null ? req.anotherNotificationEachPercentage() : 10);
        config.setDeleted(false);
        config.addOwner(AuthService.getLoggedUser().get());

        StockPriceAlert alert = new StockPriceAlert();
        alert.setId(UUID.randomUUID());
        alert.setName(req.name());
        alert.setSymbol(req.symbol());
        alert.setExchange(req.exchange());
        alert.setEmails(req.emails() != null ? req.emails() : new ArrayList<>());
        alert.setStockPriceAlertConfig(config);
        alert.setDeleted(false);
        alert.addOwner(AuthService.getLoggedUser().get());
        config.setStockPriceAlert(alert);

        StockPriceAlert saved = alertService.save(alert);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody StockAlertRequest req) {

        Optional<StockPriceAlert> match = alertService.load(PageRequest.of(0, Integer.MAX_VALUE)).stream()
                .filter(a -> a.getId().equals(id)).findFirst();
        if (match.isEmpty()) return ResponseEntity.notFound().build();

        StockPriceAlert alert = match.get();
        if (req.name() != null) alert.setName(req.name());
        if (req.symbol() != null) alert.setSymbol(req.symbol());
        if (req.exchange() != null) alert.setExchange(req.exchange());
        if (req.emails() != null) alert.setEmails(req.emails());

        StockPriceAlertConfig config = alert.getStockPriceAlertConfig();
        if (config == null) {
            config = new StockPriceAlertConfig();
            config.setId(UUID.randomUUID());
            config.setDeleted(false);
            config.addOwner(AuthService.getLoggedUser().get());
            config.setStockPriceAlert(alert);
            alert.setStockPriceAlertConfig(config);
        }
        if (req.price() != null) config.setPrice(req.price());
        if (req.operator() != null) config.setOperator(req.operator());
        if (req.amountOfNotifications() != null) config.setAmountOfNotifications(req.amountOfNotifications());
        if (req.checkIntervalMinutes() != null) config.setCheckIntervalMinutes(req.checkIntervalMinutes());
        if (req.anotherNotificationEachPercentage() != null) config.setAnotherNotificationEachPercentage(req.anotherNotificationEachPercentage());

        StockPriceAlert saved = alertService.save(alert);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        Optional<StockPriceAlert> match = alertService.load(PageRequest.of(0, Integer.MAX_VALUE)).stream()
                .filter(a -> a.getId().equals(id)).findFirst();
        if (match.isEmpty()) return ResponseEntity.notFound().build();
        alertService.delete(match.get());
        return ResponseEntity.noContent().build();
    }
}
