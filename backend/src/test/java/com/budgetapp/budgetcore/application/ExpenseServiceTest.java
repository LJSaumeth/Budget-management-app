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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    void shouldCreateExpense_whenValidRequest() {
        Budget budget = new Budget("Monthly Budget", new BigDecimal("5000.00"), "USD");
        budget.setId(1L);
        Category category = new Category("Food");
        category.setId(2L);
        ExpenseRequest request = new ExpenseRequest(1L, 2L, new BigDecimal("45.50"),
                "Grocery shopping", LocalDate.of(2026, 6, 15));

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> {
            Expense e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        ExpenseResponse response = expenseService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("45.50"));
        assertThat(response.categoryName()).isEqualTo("Food");
        assertThat(response.description()).isEqualTo("Grocery shopping");
    }

    @Test
    void shouldThrow_whenBudgetNotFound() {
        ExpenseRequest request = new ExpenseRequest(99L, 2L, new BigDecimal("10.00"),
                "Test", LocalDate.of(2026, 6, 15));

        when(budgetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Budget");
    }

    @Test
    void shouldThrow_whenCategoryNotFound() {
        Budget budget = new Budget("Test", new BigDecimal("100.00"), "USD");
        budget.setId(1L);
        ExpenseRequest request = new ExpenseRequest(1L, 99L, new BigDecimal("10.00"),
                "Test", LocalDate.of(2026, 6, 15));

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category");
    }

    @Test
    void shouldGetExpenseById() {
        Budget budget = new Budget("Test", new BigDecimal("100.00"), "USD");
        budget.setId(1L);
        Category category = new Category("Food");
        category.setId(2L);
        Expense expense = new Expense(budget, category, new BigDecimal("45.50"),
                "Grocery", LocalDate.of(2026, 6, 15));
        expense.setId(1L);

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        ExpenseResponse response = expenseService.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.categoryName()).isEqualTo("Food");
    }

    @Test
    void shouldThrow_whenExpenseNotFound() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Expense");
    }

    @Test
    void shouldDeleteExpense() {
        Budget budget = new Budget("Test", new BigDecimal("100.00"), "USD");
        budget.setId(1L);
        Category category = new Category("Food");
        category.setId(2L);
        Expense expense = new Expense(budget, category, new BigDecimal("45.50"),
                "Grocery", LocalDate.of(2026, 6, 15));
        expense.setId(1L);

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        expenseService.delete(1L);

        verify(expenseRepository, times(1)).delete(expense);
    }
}
