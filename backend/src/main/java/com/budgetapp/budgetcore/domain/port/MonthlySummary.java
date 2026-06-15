package com.budgetapp.budgetcore.domain.port;

import java.math.BigDecimal;

public record MonthlySummary(
        String month,
        BigDecimal totalAmount,
        long count
) {
}
