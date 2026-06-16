package com.budgetapp.budgetcore.application;

import com.budgetapp.budgetcore.domain.Budget;
import com.budgetapp.budgetcore.infrastructure.dto.BudgetRequest;
import com.budgetapp.budgetcore.infrastructure.dto.BudgetResponse;
import com.budgetapp.budgetcore.infrastructure.persistence.BudgetRepository;
import com.budgetapp.budgetcore.infrastructure.persistence.ExpenseRepository;
import com.budgetapp.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    public BudgetService(BudgetRepository budgetRepository, ExpenseRepository expenseRepository) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public BudgetResponse create(BudgetRequest request) {
        Budget budget = new Budget(
                request.name(),
                request.totalAmount(),
                request.currency() != null ? request.currency() : "USD"
        );
        Budget saved = budgetRepository.save(budget);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getAll() {
        return budgetRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BudgetResponse getById(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
        return toResponse(budget);
    }

    @Transactional
    public BudgetResponse update(Long id, BudgetRequest request) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
        budget.setName(request.name());
        budget.setTotalAmount(request.totalAmount());
        if (request.currency() != null) {
            budget.setCurrency(request.currency());
        }
        Budget saved = budgetRepository.save(budget);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
        budgetRepository.delete(budget);
    }

    private BudgetResponse toResponse(Budget budget) {
        BigDecimal spent = expenseRepository.sumByBudgetAndDateRange(budget.getId(), null, null);
        BigDecimal remaining = budget.getTotalAmount().subtract(spent);
        return new BudgetResponse(
                budget.getId(),
                budget.getName(),
                budget.getTotalAmount(),
                budget.getCurrency(),
                remaining,
                budget.getCreatedAt()
        );
    }
}
