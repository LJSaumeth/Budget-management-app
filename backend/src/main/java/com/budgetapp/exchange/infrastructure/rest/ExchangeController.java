package com.budgetapp.exchange.infrastructure.rest;

import com.budgetapp.exchange.application.ExchangeService;
import com.budgetapp.exchange.infrastructure.dto.ConversionResult;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/exchange")
@Validated
public class ExchangeController {

    private final ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @GetMapping("/rates")
    public ResponseEntity<Map<String, Object>> getRates(
            @RequestParam String base) {
        Map<String, BigDecimal> rates = exchangeService.getRates(base);
        return ResponseEntity.ok(Map.of(
                "base", base.toUpperCase(),
                "rates", rates,
                "fetchedAt", exchangeService.getCacheTimestamp(base)
        ));
    }

    @GetMapping("/convert")
    public ResponseEntity<ConversionResult> convert(
            @RequestParam BigDecimal amount,
            @RequestParam String from,
            @RequestParam String to) {
        ConversionResult result = exchangeService.convert(amount, from, to);
        return ResponseEntity.ok(result);
    }
}
