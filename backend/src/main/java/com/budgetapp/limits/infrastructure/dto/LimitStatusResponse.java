package com.budgetapp.limits.infrastructure.dto;

import com.budgetapp.limits.domain.Period;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LimitStatusResponse(
        Long limitId,
        Long budgetId,
        BigDecimal limitAmount,
        BigDecimal spent,
        BigDecimal remaining,
        BigDecimal percentageUsed,
        String status,
        Period period,
        LocalDate periodStart,
        LocalDate periodEnd
) {
}
