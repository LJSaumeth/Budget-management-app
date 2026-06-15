package com.budgetapp.budgetcore.application;

import com.budgetapp.budgetcore.domain.Budget;
import com.budgetapp.budgetcore.domain.Category;
import com.budgetapp.budgetcore.domain.Expense;
import com.budgetapp.budgetcore.infrastructure.dto.ExpenseRequest;
import com.budgetapp.budgetcore.infrastructure.dto.ExpenseResponse;
import com.budgetapp.budgetcore.infrastructure.persistence.BudgetRepository;
import com.budgetapp.budgetcore.infrastructure.persistence.CategoryRepository;
import com.budgetapp.budgetcore.infrastructure.persistence.ExpenseRepository;
import com.budgetapp.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseService(ExpenseRepository expenseRepository,
                          BudgetRepository budgetRepository,
                          CategoryRepository categoryRepository) {
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ExpenseResponse create(ExpenseRequest request) {
        Budget budget = budgetRepository.findById(request.budgetId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget", request.budgetId()));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));

        Expense expense = new Expense(budget, category, request.amount(), request.description(), request.date());
        Expense saved = expenseRepository.save(expense);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));
        return toResponse(expense);
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseRequest request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));

        if (!expense.getBudget().getId().equals(request.budgetId())) {
            Budget budget = budgetRepository.findById(request.budgetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Budget", request.budgetId()));
            expense.setBudget(budget);
        }

        if (!expense.getCategory().getId().equals(request.categoryId())) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));
            expense.setCategory(category);
        }

        expense.setAmount(request.amount());
        expense.setDescription(request.description());
        expense.setDate(request.date());

        Expense saved = expenseRepository.save(expense);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));
        expenseRepository.delete(expense);
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getBudget().getId(),
                expense.getCategory().getId(),
                expense.getCategory().getName(),
                expense.getAmount(),
                expense.getDescription(),
                expense.getDate(),
                expense.getCreatedAt()
        );
    }
}
