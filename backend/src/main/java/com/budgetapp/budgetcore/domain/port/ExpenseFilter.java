package com.budgetapp.budgetcore.domain.port;

import java.time.LocalDate;

public record ExpenseFilter(
        Long categoryId,
        LocalDate startDate,
        LocalDate endDate,
        String search
) {
}
