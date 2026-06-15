package com.budgetapp.history.infrastructure.dto;

import java.util.List;

public record SummaryResponse(
        String groupBy,
        List<?> items
) {
}
