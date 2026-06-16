package com.budgetapp.limits.infrastructure.rest;

import com.budgetapp.limits.application.LimitService;
import com.budgetapp.limits.infrastructure.dto.LimitRequest;
import com.budgetapp.limits.infrastructure.dto.LimitResponse;
import com.budgetapp.limits.infrastructure.dto.LimitStatusResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/limits")
public class LimitController {

    private final LimitService limitService;

    public LimitController(LimitService limitService) {
        this.limitService = limitService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LimitResponse create(@Valid @RequestBody LimitRequest request) {
        return limitService.create(request);
    }

    @GetMapping("/{id}")
    public LimitResponse getById(@PathVariable Long id) {
        return limitService.getById(id);
    }

    @GetMapping
    public List<LimitResponse> getByBudget(@RequestParam Long budgetId) {
        return limitService.getByBudget(budgetId);
    }

    @PutMapping("/{id}")
    public LimitResponse update(@PathVariable Long id, @Valid @RequestBody LimitRequest request) {
        return limitService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        limitService.delete(id);
    }

    @GetMapping("/{id}/status")
    public LimitStatusResponse getStatus(@PathVariable Long id) {
        return limitService.getStatus(id);
    }
}
