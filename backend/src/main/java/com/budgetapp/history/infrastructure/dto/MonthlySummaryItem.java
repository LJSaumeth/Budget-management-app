package com.budgetapp.history.infrastructure.dto;

import java.math.BigDecimal;

public record MonthlySummaryItem(
        String month,
        BigDecimal totalAmount,
        long count
) {
}
