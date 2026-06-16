package com.budgetapp.budgetcore.domain.port;

import java.math.BigDecimal;

public record CategoryMonthlySpending(
        Long categoryId,
        String categoryName,
        int year,
        int month,
        BigDecimal total
) {
}
