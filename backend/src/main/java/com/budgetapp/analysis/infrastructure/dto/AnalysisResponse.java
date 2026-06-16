package com.budgetapp.analysis.infrastructure.dto;

import java.math.BigDecimal;
import java.util.List;

public record AnalysisResponse(
        List<OptimizationSuggestion> suggestions,
        String message,
        BigDecimal totalPotentialAnnualSaving
) {
}
