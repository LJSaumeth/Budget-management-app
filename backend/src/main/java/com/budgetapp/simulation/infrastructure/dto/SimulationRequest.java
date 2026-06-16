package com.budgetapp.simulation.infrastructure.dto;

import com.budgetapp.simulation.domain.CategoryChange;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.List;

public record SimulationRequest(
        @DecimalMin("0") BigDecimal monthlyIncome,
        @DecimalMin("0") BigDecimal monthlyExpenses,
        @Min(1) int months,
        BigDecimal currentSavings,
        @Valid List<CategoryChange> expectedChanges
) {
}
