package com.budgetapp.exchange.infrastructure.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ConversionResult(
        String from,
        String to,
        BigDecimal amount,
        BigDecimal rate,
        BigDecimal result,
        Instant fetchedAt
) {
}
