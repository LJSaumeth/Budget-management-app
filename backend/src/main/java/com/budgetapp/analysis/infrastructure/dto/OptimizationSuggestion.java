package com.budgetapp.analysis.infrastructure.dto;

import java.math.BigDecimal;

public record OptimizationSuggestion(
        Long categoryId,
        String categoryName,
        BigDecimal currentMonthlyAvg,
        BigDecimal suggestedReductionPercent,
        BigDecimal monthlySaving,
        BigDecimal annualSaving,
        String reasoning
) {
}
