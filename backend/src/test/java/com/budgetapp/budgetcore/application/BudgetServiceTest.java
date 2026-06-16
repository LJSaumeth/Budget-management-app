package com.budgetapp.budgetcore.application;

import com.budgetapp.budgetcore.domain.Budget;
import com.budgetapp.budgetcore.infrastructure.dto.BudgetRequest;
import com.budgetapp.budgetcore.infrastructure.dto.BudgetResponse;
import com.budgetapp.budgetcore.infrastructure.persistence.BudgetRepository;
import com.budgetapp.budgetcore.infrastructure.persistence.ExpenseRepository;
import com.budgetapp.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private BudgetService budgetService;

    @Test
    void shouldCreateBudget_whenValidRequest() {
        BudgetRequest request = new BudgetRequest("Monthly Budget", new BigDecimal("5000.00"), "USD");
        Budget budget = new Budget("Monthly Budget", new BigDecimal("5000.00"), "USD");
        budget.setId(1L);

        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);
        when(expenseRepository.sumByBudgetAndDateRange(eq(1L), eq(null), eq(null)))
                .thenReturn(BigDecimal.ZERO);

        BudgetResponse response = budgetService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Monthly Budget");
        assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(response.currency()).isEqualTo("USD");
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    void shouldUseDefaultCurrency_whenCurrencyIsNull() {
        BudgetRequest request = new BudgetRequest("Test", new BigDecimal("100.00"), null);
        Budget budget = new Budget("Test", new BigDecimal("100.00"), "USD");
        budget.setId(1L);

        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);
        when(expenseRepository.sumByBudgetAndDateRange(eq(1L), eq(null), eq(null)))
                .thenReturn(BigDecimal.ZERO);

        BudgetResponse response = budgetService.create(request);

        assertThat(response.currency()).isEqualTo("USD");
    }

    @Test
    void shouldReturnAllBudgets() {
        Budget budget1 = new Budget("Budget 1", new BigDecimal("100.00"), "USD");
        budget1.setId(1L);
        Budget budget2 = new Budget("Budget 2", new BigDecimal("200.00"), "EUR");
        budget2.setId(2L);

        when(budgetRepository.findAll()).thenReturn(List.of(budget1, budget2));
        when(expenseRepository.sumByBudgetAndDateRange(eq(1L), eq(null), eq(null)))
                .thenReturn(BigDecimal.ZERO);
        when(expenseRepository.sumByBudgetAndDateRange(eq(2L), eq(null), eq(null)))
                .thenReturn(BigDecimal.ZERO);

        List<BudgetResponse> responses = budgetService.getAll();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).name()).isEqualTo("Budget 1");
        assertThat(responses.get(1).name()).isEqualTo("Budget 2");
    }

    @Test
    void shouldReturnBudget_whenFound() {
        Budget budget = new Budget("Monthly Budget", new BigDecimal("5000.00"), "USD");
        budget.setId(1L);

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));
        when(expenseRepository.sumByBudgetAndDateRange(eq(1L), eq(null), eq(null)))
                .thenReturn(BigDecimal.ZERO);

        BudgetResponse response = budgetService.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Monthly Budget");
    }

    @Test
    void shouldThrow_whenBudgetNotFound() {
        when(budgetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Budget")
                .hasMessageContaining("99");
    }

    @Test
    void shouldUpdateBudget() {
        Budget existing = new Budget("Old Name", new BigDecimal("100.00"), "USD");
        existing.setId(1L);
        BudgetRequest request = new BudgetRequest("New Name", new BigDecimal("200.00"), "EUR");

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(budgetRepository.save(any(Budget.class))).thenReturn(existing);
        when(expenseRepository.sumByBudgetAndDateRange(eq(1L), eq(null), eq(null)))
                .thenReturn(BigDecimal.ZERO);

        BudgetResponse response = budgetService.update(1L, request);

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(response.currency()).isEqualTo("EUR");
    }

    @Test
    void shouldDeleteBudget() {
        Budget budget = new Budget("Test", new BigDecimal("100.00"), "USD");
        budget.setId(1L);

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));

        budgetService.delete(1L);

        verify(budgetRepository, times(1)).delete(budget);
    }

    @Test
    void shouldThrow_whenDeleteNonExistentBudget() {
        when(budgetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
