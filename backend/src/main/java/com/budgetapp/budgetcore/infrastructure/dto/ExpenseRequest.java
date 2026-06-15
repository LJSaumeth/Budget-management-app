package com.budgetapp.budgetcore.infrastructure.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        @NotNull(message = "Budget ID is required")
        Long budgetId,

        @NotNull(message = "Category ID is required")
        Long categoryId,

        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        String description,

        @NotNull(message = "Date is required")
        LocalDate date
) {
}
