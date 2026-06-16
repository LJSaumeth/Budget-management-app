package com.budgetapp.analysis.application;

import com.budgetapp.analysis.infrastructure.dto.AnalysisResponse;
import com.budgetapp.analysis.infrastructure.dto.OptimizationSuggestion;
import com.budgetapp.budgetcore.domain.port.CategoryMonthlySpending;
import com.budgetapp.budgetcore.domain.port.ExpenseQueryPort;
import com.budgetapp.budgetcore.infrastructure.persistence.BudgetRepository;
import com.budgetapp.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock
    private ExpenseQueryPort expenseQueryPort;

    @Mock
    private BudgetRepository budgetRepository;

    @Test
    void should_flagOverspending_when_recentMonthExceedsAverageBy10Percent() {
        AnalysisService service = new AnalysisService(expenseQueryPort, budgetRepository, 2, 10, 5);

        List<CategoryMonthlySpending> data = List.of(
                new CategoryMonthlySpending(1L, "Food", 2026, 3, new BigDecimal("500.00")),
                new CategoryMonthlySpending(1L, "Food", 2026, 4, new BigDecimal("520.00")),
                new CategoryMonthlySpending(1L, "Food", 2026, 5, new BigDecimal("620.00"))
        );

        when(budgetRepository.existsById(eq(1L))).thenReturn(true);
        when(expenseQueryPort.getCategoryMonthlySpending(eq(1L))).thenReturn(data);

        AnalysisResponse response = service.analyze(1L);

        assertThat(response.suggestions()).hasSize(1);
        OptimizationSuggestion suggestion = response.suggestions().get(0);
        assertThat(suggestion.categoryId()).isEqualTo(1L);
        assertThat(suggestion.categoryName()).isEqualTo("Food");
        assertThat(suggestion.monthlySaving()).isGreaterThan(BigDecimal.ZERO);
        assertThat(suggestion.annualSaving()).isGreaterThan(BigDecimal.ZERO);
        assertThat(suggestion.reasoning()).contains("Food", "above", "average", "save");
        assertThat(suggestion.suggestedReductionPercent()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void should_returnEmptySuggestions_when_allCategoriesWithin10Percent() {
        AnalysisService service = new AnalysisService(expenseQueryPort, budgetRepository, 2, 10, 5);

        List<CategoryMonthlySpending> data = List.of(
                new CategoryMonthlySpending(1L, "Food", 2026, 3, new BigDecimal("500.00")),
                new CategoryMonthlySpending(1L, "Food", 2026, 4, new BigDecimal("520.00")),
                new CategoryMonthlySpending(1L, "Food", 2026, 5, new BigDecimal("540.00"))
        );

        when(budgetRepository.existsById(eq(1L))).thenReturn(true);
        when(expenseQueryPort.getCategoryMonthlySpending(eq(1L))).thenReturn(data);

        AnalysisResponse response = service.analyze(1L);

        assertThat(response.suggestions()).isEmpty();
        assertThat(response.message()).contains("within");
        assertThat(response.totalPotentialAnnualSaving()).isEqualByComparingTo("0.00");
    }

    @Test
    void should_returnEmptySuggestions_when_insufficientData() {
        AnalysisService service = new AnalysisService(expenseQueryPort, budgetRepository, 2, 10, 5);

        List<CategoryMonthlySpending> data = List.of(
                new CategoryMonthlySpending(1L, "Food", 2026, 5, new BigDecimal("500.00"))
        );

        when(budgetRepository.existsById(eq(1L))).thenReturn(true);
        when(expenseQueryPort.getCategoryMonthlySpending(eq(1L))).thenReturn(data);

        AnalysisResponse response = service.analyze(1L);

        assertThat(response.suggestions()).isEmpty();
        assertThat(response.message()).contains("Insufficient data");
        assertThat(response.totalPotentialAnnualSaving()).isEqualByComparingTo("0.00");
    }

    @Test
    void should_skipCategory_when_onlyOneMonthOfData() {
        AnalysisService service = new AnalysisService(expenseQueryPort, budgetRepository, 2, 10, 5);

        List<CategoryMonthlySpending> data = List.of(
                new CategoryMonthlySpending(1L, "Food", 2026, 5, new BigDecimal("500.00")),
                new CategoryMonthlySpending(1L, "Food", 2026, 6, new BigDecimal("520.00")),
                new CategoryMonthlySpending(2L, "Transport", 2026, 6, new BigDecimal("3000.00"))
        );

        when(budgetRepository.existsById(eq(1L))).thenReturn(true);
        when(expenseQueryPort.getCategoryMonthlySpending(eq(1L))).thenReturn(data);

        AnalysisResponse response = service.analyze(1L);

        assertThat(response.suggestions()).isEmpty();
        assertThat(response.message()).contains("within");
    }

    @Test
    void should_notFlag_when_exact10PercentThreshold() {
        AnalysisService service = new AnalysisService(expenseQueryPort, budgetRepository, 2, 10, 5);

        List<CategoryMonthlySpending> data = List.of(
                new CategoryMonthlySpending(1L, "Food", 2026, 5, new BigDecimal("900.00")),
                new CategoryMonthlySpending(1L, "Food", 2026, 6, new BigDecimal("1100.00"))
        );

        when(budgetRepository.existsById(eq(1L))).thenReturn(true);
        when(expenseQueryPort.getCategoryMonthlySpending(eq(1L))).thenReturn(data);

        AnalysisResponse response = service.analyze(1L);

        assertThat(response.suggestions()).isEmpty();
        assertThat(response.message()).contains("within");
    }

    @Test
    void should_flag_when_slightlyAboveExactThreshold() {
        AnalysisService service = new AnalysisService(expenseQueryPort, budgetRepository, 2, 10, 5);

        List<CategoryMonthlySpending> data = List.of(
                new CategoryMonthlySpending(1L, "Food", 2026, 5, new BigDecimal("900.00")),
                new CategoryMonthlySpending(1L, "Food", 2026, 6, new BigDecimal("1110.00"))
        );

        when(budgetRepository.existsById(eq(1L))).thenReturn(true);
        when(expenseQueryPort.getCategoryMonthlySpending(eq(1L))).thenReturn(data);

        AnalysisResponse response = service.analyze(1L);

        assertThat(response.suggestions()).hasSize(1);
    }

    @Test
    void should_sortByAnnualSavingDescending_and_capAtMaxSuggestions() {
        AnalysisService service = new AnalysisService(expenseQueryPort, budgetRepository, 2, 10, 3);

        List<CategoryMonthlySpending> data = List.of(
                new CategoryMonthlySpending(1L, "Food", 2026, 5, new BigDecimal("100.00")),
                new CategoryMonthlySpending(1L, "Food", 2026, 6, new BigDecimal("130.00")),
                new CategoryMonthlySpending(2L, "Transport", 2026, 5, new BigDecimal("200.00")),
                new CategoryMonthlySpending(2L, "Transport", 2026, 6, new BigDecimal("260.00")),
                new CategoryMonthlySpending(3L, "Entertainment", 2026, 5, new BigDecimal("300.00")),
                new CategoryMonthlySpending(3L, "Entertainment", 2026, 6, new BigDecimal("390.00")),
                new CategoryMonthlySpending(4L, "Housing", 2026, 5, new BigDecimal("400.00")),
                new CategoryMonthlySpending(4L, "Housing", 2026, 6, new BigDecimal("520.00"))
        );

        when(budgetRepository.existsById(eq(1L))).thenReturn(true);
        when(expenseQueryPort.getCategoryMonthlySpending(eq(1L))).thenReturn(data);

        AnalysisResponse response = service.analyze(1L);

        assertThat(response.suggestions()).hasSize(3);
        assertThat(response.suggestions().get(0).categoryName()).isEqualTo("Housing");
        assertThat(response.suggestions().get(1).categoryName()).isEqualTo("Entertainment");
        assertThat(response.suggestions().get(2).categoryName()).isEqualTo("Transport");
    }

    @Test
    void should_computeTotalPotentialAnnualSaving() {
        AnalysisService service = new AnalysisService(expenseQueryPort, budgetRepository, 2, 10, 5);

        List<CategoryMonthlySpending> data = List.of(
                new CategoryMonthlySpending(1L, "Food", 2026, 5, new BigDecimal("100.00")),
                new CategoryMonthlySpending(1L, "Food", 2026, 6, new BigDecimal("130.00")),
                new CategoryMonthlySpending(2L, "Transport", 2026, 5, new BigDecimal("200.00")),
                new CategoryMonthlySpending(2L, "Transport", 2026, 6, new BigDecimal("260.00"))
        );

        when(budgetRepository.existsById(eq(1L))).thenReturn(true);
        when(expenseQueryPort.getCategoryMonthlySpending(eq(1L))).thenReturn(data);

        AnalysisResponse response = service.analyze(1L);

        assertThat(response.totalPotentialAnnualSaving()).isGreaterThan(BigDecimal.ZERO);
        assertThat(response.totalPotentialAnnualSaving()).isEqualByComparingTo(
                response.suggestions().stream()
                        .map(OptimizationSuggestion::annualSaving)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );
    }

    @Test
    void should_returnNoExpenseData_when_emptyData() {
        AnalysisService service = new AnalysisService(expenseQueryPort, budgetRepository, 2, 10, 5);

        when(budgetRepository.existsById(eq(1L))).thenReturn(true);
        when(expenseQueryPort.getCategoryMonthlySpending(eq(1L))).thenReturn(List.of());

        AnalysisResponse response = service.analyze(1L);

        assertThat(response.suggestions()).isEmpty();
        assertThat(response.message()).isEqualTo("No expense data available");
        assertThat(response.totalPotentialAnnualSaving()).isEqualByComparingTo("0.00");
    }

    @Test
    void should_throwResourceNotFound_when_budgetDoesNotExist() {
        AnalysisService service = new AnalysisService(expenseQueryPort, budgetRepository, 2, 10, 5);

        when(budgetRepository.existsById(eq(99L))).thenReturn(false);

        assertThatThrownBy(() -> service.analyze(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Budget");
    }

    @Test
    void should_notFlagCategoryWithExactlyTwoMonthsWithinThreshold() {
        AnalysisService service = new AnalysisService(expenseQueryPort, budgetRepository, 2, 10, 5);

        List<CategoryMonthlySpending> data = List.of(
                new CategoryMonthlySpending(1L, "Food", 2026, 5, new BigDecimal("100.00")),
                new CategoryMonthlySpending(1L, "Food", 2026, 6, new BigDecimal("105.00"))
        );

        when(budgetRepository.existsById(eq(1L))).thenReturn(true);
        when(expenseQueryPort.getCategoryMonthlySpending(eq(1L))).thenReturn(data);

        AnalysisResponse response = service.analyze(1L);

        assertThat(response.suggestions()).isEmpty();
    }
}
