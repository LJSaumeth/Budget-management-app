package com.budgetapp.budgetcore.infrastructure.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BudgetResponse(
        Long id,
        String name,
        BigDecimal totalAmount,
        String currency,
        BigDecimal remainingAmount,
        LocalDateTime createdAt
) {
}
