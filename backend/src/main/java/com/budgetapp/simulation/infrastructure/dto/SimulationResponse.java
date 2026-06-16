package com.budgetapp.simulation.infrastructure.dto;

import java.math.BigDecimal;

public record SimulationResponse(
        BigDecimal monthlySavings,
        BigDecimal projectedSavings,
        int months,
        BigDecimal monthlyIncome,
        BigDecimal monthlyExpenses,
        BigDecimal adjustedMonthlyExpenses,
        BigDecimal currentSavings,
        BigDecimal baselineProjectedSavings,
        BigDecimal differenceFromBaseline
) {
}
