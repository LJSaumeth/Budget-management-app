package com.budgetapp.history.infrastructure.rest;

import com.budgetapp.history.application.HistoryService;
import com.budgetapp.history.infrastructure.dto.ExpenseFilterRequest;
import com.budgetapp.history.infrastructure.dto.ExpenseHistoryPage;
import com.budgetapp.history.infrastructure.dto.SummaryResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/expenses")
@Validated
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/history")
    public ResponseEntity<ExpenseHistoryPage> getHistory(
            @RequestParam Long budgetId,
            @Valid ExpenseFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ExpenseHistoryPage result = historyService.getHistory(budgetId, filter, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/summary")
    public ResponseEntity<SummaryResponse> getSummary(
            @RequestParam Long budgetId,
            @RequestParam String groupBy,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        SummaryResponse result = historyService.getSummary(budgetId, groupBy, year, startDate, endDate);
        return ResponseEntity.ok(result);
    }
}
