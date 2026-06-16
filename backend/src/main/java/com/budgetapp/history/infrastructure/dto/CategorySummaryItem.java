package com.budgetapp.history.infrastructure.dto;

import java.math.BigDecimal;

public record CategorySummaryItem(
        Long categoryId,
        String categoryName,
        BigDecimal totalAmount,
        long count,
        BigDecimal percentage
) {
}
