package com.budgetapp.exchange.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record CachedRate(
        String baseCurrency,
        String targetCurrency,
        BigDecimal rate,
        Instant fetchedAt
) {
}
