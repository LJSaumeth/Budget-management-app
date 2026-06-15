package com.budgetapp.budgetcore.infrastructure.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenseResponse(
        Long id,
        Long budgetId,
        Long categoryId,
        String categoryName,
        BigDecimal amount,
        String description,
        LocalDate date,
        LocalDateTime createdAt
) {
}
