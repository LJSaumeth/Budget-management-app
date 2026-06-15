package com.budgetapp.history.application;

import com.budgetapp.budgetcore.domain.Expense;
import com.budgetapp.budgetcore.domain.port.CategorySummary;
import com.budgetapp.budgetcore.domain.port.ExpenseFilter;
import com.budgetapp.budgetcore.domain.port.ExpenseQueryPort;
import com.budgetapp.budgetcore.domain.port.MonthlySummary;
import com.budgetapp.budgetcore.infrastructure.persistence.BudgetRepository;
import com.budgetapp.history.infrastructure.dto.ExpenseFilterRequest;
import com.budgetapp.history.infrastructure.dto.ExpenseHistoryPage;
import com.budgetapp.history.infrastructure.dto.SummaryResponse;
import com.budgetapp.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @Mock
    private ExpenseQueryPort expenseQueryPort;

    @Mock
    private BudgetRepository budgetRepository;

    private HistoryService historyService;

    @BeforeEach
    void setUp() {
        historyService = new HistoryService(expenseQueryPort, budgetRepository);
    }

    @Test
    void shouldReturnPaginatedHistory_whenValidRequest() {
        when(budgetRepository.existsById(1L)).thenReturn(true);

        Expense expense = createExpense(1L, 1L, 1L, "Food", "Groceries", "2026-01-15");

        when(expenseQueryPort.findExpenses(eq(1L), any(ExpenseFilter.class), any(Pageable.class)))
                .thenAnswer(inv -> {
                    Pageable p = inv.getArgument(2);
                    return new PageImpl<>(List.of(expense), p, 1L);
                });

        ExpenseHistoryPage result = historyService.getHistory(1L, new ExpenseFilterRequest(null, null, null, null), 0, 20);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.page()).isEqualTo(0);
        assertThat(result.size()).isEqualTo(20);
        assertThat(result.content().get(0).description()).isEqualTo("Groceries");
    }

    @Test
    void shouldReturnCorrectPaginationMetadata_whenMultiplePages() {
        when(budgetRepository.existsById(1L)).thenReturn(true);

        Expense expense1 = createExpense(1L, 1L, 1L, "Food", "Item 1", "2026-01-01");
        Expense expense2 = createExpense(2L, 1L, 2L, "Transport", "Item 2", "2026-01-02");
        List<Expense> expenses = List.of(expense1, expense2);

        when(expenseQueryPort.findExpenses(eq(1L), any(ExpenseFilter.class), any(Pageable.class)))
                .thenAnswer(inv -> {
                    Pageable p = inv.getArgument(2);
                    return new PageImpl<>(expenses, p, 5L);
                });

        ExpenseHistoryPage result = historyService.getHistory(1L, new ExpenseFilterRequest(null, null, null, null), 0, 2);

        assertThat(result.totalElements()).isEqualTo(5);
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.content()).hasSize(2);
    }

    @Test
    void shouldReturnEmptyPage_whenNoExpenses() {
        when(budgetRepository.existsById(1L)).thenReturn(true);

        when(expenseQueryPort.findExpenses(eq(1L), any(ExpenseFilter.class), any(Pageable.class)))
                .thenAnswer(inv -> {
                    Pageable p = inv.getArgument(2);
                    return new PageImpl<>(List.of(), p, 0L);
                });

        ExpenseHistoryPage result = historyService.getHistory(1L, new ExpenseFilterRequest(null, null, null, null), 0, 20);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(0);
        assertThat(result.totalPages()).isEqualTo(0);
    }

    @Test
    void shouldThrow_whenBudgetNotFound_forHistory() {
        when(budgetRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> historyService.getHistory(99L, new ExpenseFilterRequest(null, null, null, null), 0, 20))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Budget");
    }

    @Test
    void shouldCapPageSize_whenExceedsMax() {
        when(budgetRepository.existsById(1L)).thenReturn(true);

        when(expenseQueryPort.findExpenses(eq(1L), any(ExpenseFilter.class), any(Pageable.class)))
                .thenAnswer(inv -> {
                    Pageable p = inv.getArgument(2);
                    return new PageImpl<>(List.of(), p, 0L);
                });

        ExpenseHistoryPage result = historyService.getHistory(1L, new ExpenseFilterRequest(null, null, null, null), 0, 200);

        assertThat(result.size()).isEqualTo(100);
    }

    @Test
    void shouldReturnSummaryByCategory_whenValidRequest() {
        when(budgetRepository.existsById(1L)).thenReturn(true);

        List<CategorySummary> summaries = List.of(
                new CategorySummary(1L, "Food", new BigDecimal("200.00"), 2),
                new CategorySummary(2L, "Transport", new BigDecimal("100.00"), 1)
        );
        when(expenseQueryPort.summarizeByCategory(1L, null, null)).thenReturn(summaries);

        SummaryResponse result = historyService.getSummary(1L, "category", null, null, null);

        assertThat(result.groupBy()).isEqualTo("category");
        List<?> items = result.items();
        assertThat(items).hasSize(2);
        assertThat(items.get(0)).isInstanceOf(com.budgetapp.history.infrastructure.dto.CategorySummaryItem.class);
    }

    @Test
    void shouldCalculatePercentages_whenSummaryByCategory() {
        when(budgetRepository.existsById(1L)).thenReturn(true);

        List<CategorySummary> summaries = List.of(
                new CategorySummary(1L, "Food", new BigDecimal("250.00"), 2),
                new CategorySummary(2L, "Transport", new BigDecimal("100.00"), 1)
        );
        when(expenseQueryPort.summarizeByCategory(1L, null, null)).thenReturn(summaries);

        SummaryResponse result = historyService.getSummary(1L, "category", null, null, null);

        List<?> items = result.items();
        com.budgetapp.history.infrastructure.dto.CategorySummaryItem item0 =
                (com.budgetapp.history.infrastructure.dto.CategorySummaryItem) items.get(0);
        com.budgetapp.history.infrastructure.dto.CategorySummaryItem item1 =
                (com.budgetapp.history.infrastructure.dto.CategorySummaryItem) items.get(1);

        assertThat(item0.percentage()).isEqualByComparingTo(new BigDecimal("71.43"));
        assertThat(item1.percentage()).isEqualByComparingTo(new BigDecimal("28.57"));
    }

    @Test
    void shouldReturnZeroPercentage_whenTotalIsZero() {
        when(budgetRepository.existsById(1L)).thenReturn(true);

        List<CategorySummary> summaries = List.of(
                new CategorySummary(1L, "Food", BigDecimal.ZERO, 0)
        );
        when(expenseQueryPort.summarizeByCategory(1L, null, null)).thenReturn(summaries);

        SummaryResponse result = historyService.getSummary(1L, "category", null, null, null);

        List<?> items = result.items();
        com.budgetapp.history.infrastructure.dto.CategorySummaryItem item =
                (com.budgetapp.history.infrastructure.dto.CategorySummaryItem) items.get(0);
        assertThat(item.percentage()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnEmptySummary_whenNoExpenses() {
        when(budgetRepository.existsById(1L)).thenReturn(true);

        when(expenseQueryPort.summarizeByCategory(1L, null, null)).thenReturn(List.of());

        SummaryResponse result = historyService.getSummary(1L, "category", null, null, null);

        assertThat(result.items()).isEmpty();
    }

    @Test
    void shouldReturnSummaryByMonth_whenValidRequest() {
        when(budgetRepository.existsById(1L)).thenReturn(true);

        List<MonthlySummary> summaries = List.of(
                new MonthlySummary("2026-06", new BigDecimal("300.00"), 3),
                new MonthlySummary("2026-05", new BigDecimal("500.00"), 5)
        );
        when(expenseQueryPort.summarizeByMonth(eq(1L), eq(2026), eq(null), eq(null)))
                .thenReturn(summaries);

        SummaryResponse result = historyService.getSummary(1L, "month", 2026, null, null);

        assertThat(result.groupBy()).isEqualTo("month");
        List<?> items = result.items();
        assertThat(items).hasSize(2);
        assertThat(items.get(0)).isInstanceOf(com.budgetapp.history.infrastructure.dto.MonthlySummaryItem.class);
    }

    @Test
    void shouldDefaultToCurrentYear_whenYearNotProvidedForMonthSummary() {
        when(budgetRepository.existsById(1L)).thenReturn(true);

        when(expenseQueryPort.summarizeByMonth(eq(1L), eq(java.time.Year.now().getValue()), eq(null), eq(null)))
                .thenReturn(List.of());

        SummaryResponse result = historyService.getSummary(1L, "month", null, null, null);

        assertThat(result.items()).isEmpty();
    }

    @Test
    void shouldThrow_whenInvalidGroupBy() {
        when(budgetRepository.existsById(1L)).thenReturn(true);

        assertThatThrownBy(() -> historyService.getSummary(1L, "week", null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("groupBy must be 'category' or 'month'");
    }

    @Test
    void shouldThrow_whenBudgetNotFound_forSummary() {
        when(budgetRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> historyService.getSummary(99L, "category", null, null, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Budget");
    }

    @Test
    void shouldPassDateRangeFilterToSummaryByCategory() {
        when(budgetRepository.existsById(1L)).thenReturn(true);

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 3, 31);

        when(expenseQueryPort.summarizeByCategory(1L, start, end)).thenReturn(List.of());

        SummaryResponse result = historyService.getSummary(1L, "category", null, start, end);

        assertThat(result.items()).isEmpty();
    }

    private Expense createExpense(Long id, Long budgetId, Long categoryId, String categoryName,
                                  String description, String date) {
        com.budgetapp.budgetcore.domain.Budget budget = new com.budgetapp.budgetcore.domain.Budget();
        budget.setId(budgetId);

        com.budgetapp.budgetcore.domain.Category category = new com.budgetapp.budgetcore.domain.Category();
        category.setId(categoryId);
        category.setName(categoryName);

        Expense expense = new Expense(budget, category, new BigDecimal("100.00"), description,
                LocalDate.parse(date));
        expense.setId(id);
        return expense;
    }
}
