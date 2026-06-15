package com.budgetapp.limits.infrastructure.dto;

import com.budgetapp.limits.domain.Period;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LimitResponse(
        Long id,
        Long budgetId,
        BigDecimal amount,
        Period period,
        int warningThresholdPercent,
        LocalDateTime createdAt
) {
}
