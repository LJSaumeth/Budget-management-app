package com.budgetapp.budgetcore.infrastructure.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record BudgetRequest(
        @NotBlank(message = "Name is required")
        String name,

        @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
        BigDecimal totalAmount,

        String currency
) {
}
