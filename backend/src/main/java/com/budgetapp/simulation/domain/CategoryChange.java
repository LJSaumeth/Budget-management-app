package com.budgetapp.simulation.domain;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record CategoryChange(
        String category,
        @DecimalMin("0") BigDecimal reductionAmount
) {
}
