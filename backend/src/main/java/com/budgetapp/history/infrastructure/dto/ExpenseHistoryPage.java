package com.budgetapp.history.infrastructure.dto;

import com.budgetapp.budgetcore.infrastructure.dto.ExpenseResponse;

import java.util.List;

public record ExpenseHistoryPage(
        List<ExpenseResponse> content,
        long totalElements,
        int totalPages,
        int page,
        int size
) {
}
