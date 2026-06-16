package com.budgetapp.budgetcore.domain.port;

import java.math.BigDecimal;

public record CategorySummary(
        Long categoryId,
        String categoryName,
        BigDecimal totalAmount,
        long count
) {
}
