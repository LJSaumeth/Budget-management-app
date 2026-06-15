package com.budgetapp.budgetcore.infrastructure.rest;

import com.budgetapp.budgetcore.application.BudgetService;
import com.budgetapp.budgetcore.infrastructure.dto.BudgetRequest;
import com.budgetapp.budgetcore.infrastructure.dto.BudgetResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetResponse create(@Valid @RequestBody BudgetRequest request) {
        return budgetService.create(request);
    }

    @GetMapping
    public List<BudgetResponse> getAll() {
        return budgetService.getAll();
    }

    @GetMapping("/{id}")
    public BudgetResponse getById(@PathVariable Long id) {
        return budgetService.getById(id);
    }

    @PutMapping("/{id}")
    public BudgetResponse update(@PathVariable Long id, @Valid @RequestBody BudgetRequest request) {
        return budgetService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        budgetService.delete(id);
    }
}
