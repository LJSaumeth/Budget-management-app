package com.budgetapp.limits.application;

import com.budgetapp.budgetcore.domain.port.ExpenseQueryPort;
import com.budgetapp.budgetcore.infrastructure.persistence.BudgetRepository;
import com.budgetapp.limits.domain.BudgetLimit;
import com.budgetapp.limits.domain.Period;
import com.budgetapp.limits.infrastructure.dto.LimitRequest;
import com.budgetapp.limits.infrastructure.dto.LimitResponse;
import com.budgetapp.limits.infrastructure.dto.LimitStatusResponse;
import com.budgetapp.limits.infrastructure.persistence.BudgetLimitRepository;
import com.budgetapp.shared.exception.ConflictException;
import com.budgetapp.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LimitServiceTest {

    @Mock
    private BudgetLimitRepository budgetLimitRepository;

    @Mock
    private ExpenseQueryPort expenseQueryPort;

    @Mock
    private BudgetRepository budgetRepository;

    private LimitService limitService;

    @BeforeEach
    void setUp() {
        limitService = new LimitService(budgetLimitRepository, expenseQueryPort, budgetRepository, 80);
    }

    @Test
    void shouldCreateLimit_whenValidRequest() {
        LimitRequest request = new LimitRequest(1L, new BigDecimal("500.00"), Period.MONTHLY, 80);
        BudgetLimit limit = new BudgetLimit(1L, new BigDecimal("500.00"), Period.MONTHLY, 80);
        limit.setId(1L);
        limit.setCreatedAt(LocalDateTime.now());

        when(budgetRepository.existsById(1L)).thenReturn(true);
        when(budgetLimitRepository.findByBudgetIdAndPeriod(1L, Period.MONTHLY)).thenReturn(Optional.empty());
        when(budgetLimitRepository.save(any(BudgetLimit.class))).thenReturn(limit);

        LimitResponse response = limitService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.budgetId()).isEqualTo(1L);
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(response.period()).isEqualTo(Period.MONTHLY);
        assertThat(response.warningThresholdPercent()).isEqualTo(80);
        verify(budgetLimitRepository).save(any(BudgetLimit.class));
    }

    @Test
    void shouldUseDefaultThreshold_whenThresholdIsNull() {
        LimitRequest request = new LimitRequest(1L, new BigDecimal("500.00"), Period.MONTHLY, null);
        BudgetLimit limit = new BudgetLimit(1L, new BigDecimal("500.00"), Period.MONTHLY, 80);
        limit.setId(1L);

        when(budgetRepository.existsById(1L)).thenReturn(true);
        when(budgetLimitRepository.findByBudgetIdAndPeriod(1L, Period.MONTHLY)).thenReturn(Optional.empty());
        when(budgetLimitRepository.save(any(BudgetLimit.class))).thenReturn(limit);

        LimitResponse response = limitService.create(request);

        assertThat(response.warningThresholdPercent()).isEqualTo(80);
    }

    @Test
    void shouldThrow_whenBudgetNotFound() {
        LimitRequest request = new LimitRequest(99L, new BigDecimal("500.00"), Period.MONTHLY, 80);

        when(budgetRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> limitService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Budget");
    }

    @Test
    void shouldThrow_whenDuplicatePeriod() {
        LimitRequest request = new LimitRequest(1L, new BigDecimal("500.00"), Period.MONTHLY, 80);

        when(budgetRepository.existsById(1L)).thenReturn(true);
        when(budgetLimitRepository.findByBudgetIdAndPeriod(1L, Period.MONTHLY))
                .thenReturn(Optional.of(new BudgetLimit()));

        assertThatThrownBy(() -> limitService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldReturnLimit_whenFound() {
        BudgetLimit limit = new BudgetLimit(1L, new BigDecimal("500.00"), Period.MONTHLY, 80);
        limit.setId(1L);
        limit.setCreatedAt(LocalDateTime.now());

        when(budgetLimitRepository.findById(1L)).thenReturn(Optional.of(limit));

        LimitResponse response = limitService.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    void shouldThrow_whenLimitNotFound() {
        when(budgetLimitRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> limitService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("BudgetLimit");
    }

    @Test
    void shouldReturnLimitsByBudget() {
        BudgetLimit limit1 = new BudgetLimit(1L, new BigDecimal("500.00"), Period.WEEKLY, 80);
        limit1.setId(1L);
        limit1.setCreatedAt(LocalDateTime.now());
        BudgetLimit limit2 = new BudgetLimit(1L, new BigDecimal("2000.00"), Period.MONTHLY, 75);
        limit2.setId(2L);
        limit2.setCreatedAt(LocalDateTime.now());

        when(budgetLimitRepository.findByBudgetId(1L)).thenReturn(List.of(limit1, limit2));

        List<LimitResponse> responses = limitService.getByBudget(1L);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).period()).isEqualTo(Period.WEEKLY);
        assertThat(responses.get(1).period()).isEqualTo(Period.MONTHLY);
    }

    @Test
    void shouldUpdateLimit() {
        BudgetLimit existing = new BudgetLimit(1L, new BigDecimal("500.00"), Period.MONTHLY, 80);
        existing.setId(1L);
        LimitRequest request = new LimitRequest(1L, new BigDecimal("1000.00"), Period.WEEKLY, 70);

        when(budgetLimitRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(budgetRepository.existsById(1L)).thenReturn(true);
        when(budgetLimitRepository.findByBudgetIdAndPeriod(1L, Period.WEEKLY)).thenReturn(Optional.empty());
        when(budgetLimitRepository.save(any(BudgetLimit.class))).thenReturn(existing);

        LimitResponse response = limitService.update(1L, request);

        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(response.period()).isEqualTo(Period.WEEKLY);
        assertThat(response.warningThresholdPercent()).isEqualTo(70);
    }

    @Test
    void shouldAllowUpdate_sameBudgetAndPeriod() {
        BudgetLimit existing = new BudgetLimit(1L, new BigDecimal("500.00"), Period.MONTHLY, 80);
        existing.setId(1L);
        LimitRequest request = new LimitRequest(1L, new BigDecimal("1000.00"), Period.MONTHLY, 70);

        when(budgetLimitRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(budgetRepository.existsById(1L)).thenReturn(true);
        when(budgetLimitRepository.findByBudgetIdAndPeriod(1L, Period.MONTHLY))
                .thenReturn(Optional.of(existing));
        when(budgetLimitRepository.save(any(BudgetLimit.class))).thenReturn(existing);

        LimitResponse response = limitService.update(1L, request);

        assertThat(response.period()).isEqualTo(Period.MONTHLY);
    }

    @Test
    void shouldThrow_whenUpdateDuplicatePeriod() {
        BudgetLimit existing = new BudgetLimit(1L, new BigDecimal("500.00"), Period.MONTHLY, 80);
        existing.setId(1L);
        BudgetLimit other = new BudgetLimit(1L, new BigDecimal("300.00"), Period.WEEKLY, 70);
        other.setId(2L);
        LimitRequest request = new LimitRequest(1L, new BigDecimal("1000.00"), Period.WEEKLY, 80);

        when(budgetLimitRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(budgetRepository.existsById(1L)).thenReturn(true);
        when(budgetLimitRepository.findByBudgetIdAndPeriod(1L, Period.WEEKLY)).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> limitService.update(1L, request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldDeleteLimit() {
        BudgetLimit limit = new BudgetLimit(1L, new BigDecimal("500.00"), Period.MONTHLY, 80);
        limit.setId(1L);

        when(budgetLimitRepository.findById(1L)).thenReturn(Optional.of(limit));

        limitService.delete(1L);

        verify(budgetLimitRepository).delete(limit);
    }

    @Test
    void shouldThrow_whenDeleteNonExistentLimit() {
        when(budgetLimitRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> limitService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldReturnOkStatus_whenBelowThreshold() {
        BudgetLimit limit = new BudgetLimit(1L, new BigDecimal("1000.00"), Period.MONTHLY, 80);
        limit.setId(1L);

        when(budgetLimitRepository.findById(1L)).thenReturn(Optional.of(limit));
        when(expenseQueryPort.sumExpensesByPeriod(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("500.00"));

        LimitStatusResponse status = limitService.getStatus(1L);

        assertThat(status.status()).isEqualTo("OK");
        assertThat(status.limitAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(status.spent()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(status.remaining()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(status.percentageUsed()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(status.period()).isEqualTo(Period.MONTHLY);
    }

    @Test
    void shouldReturnWarningStatus_whenAtThreshold() {
        BudgetLimit limit = new BudgetLimit(1L, new BigDecimal("1000.00"), Period.MONTHLY, 80);
        limit.setId(1L);

        when(budgetLimitRepository.findById(1L)).thenReturn(Optional.of(limit));
        when(expenseQueryPort.sumExpensesByPeriod(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("800.00"));

        LimitStatusResponse status = limitService.getStatus(1L);

        assertThat(status.status()).isEqualTo("WARNING");
        assertThat(status.percentageUsed()).isEqualByComparingTo(new BigDecimal("80.00"));
    }

    @Test
    void shouldReturnExceededStatus_whenOverLimit() {
        BudgetLimit limit = new BudgetLimit(1L, new BigDecimal("1000.00"), Period.MONTHLY, 80);
        limit.setId(1L);

        when(budgetLimitRepository.findById(1L)).thenReturn(Optional.of(limit));
        when(expenseQueryPort.sumExpensesByPeriod(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("1200.00"));

        LimitStatusResponse status = limitService.getStatus(1L);

        assertThat(status.status()).isEqualTo("EXCEEDED");
        assertThat(status.percentageUsed()).isEqualByComparingTo(new BigDecimal("120.00"));
        assertThat(status.remaining()).isEqualByComparingTo(new BigDecimal("-200.00"));
    }

    @Test
    void shouldReturnOk_whenNoExpenses() {
        BudgetLimit limit = new BudgetLimit(1L, new BigDecimal("1000.00"), Period.MONTHLY, 80);
        limit.setId(1L);

        when(budgetLimitRepository.findById(1L)).thenReturn(Optional.of(limit));
        when(expenseQueryPort.sumExpensesByPeriod(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);

        LimitStatusResponse status = limitService.getStatus(1L);

        assertThat(status.status()).isEqualTo("OK");
        assertThat(status.percentageUsed()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(status.remaining()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    void shouldThrow_whenGetStatusForUnknownLimit() {
        when(budgetLimitRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> limitService.getStatus(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldReturnWeeklyPeriodBounds() {
        BudgetLimit limit = new BudgetLimit(1L, new BigDecimal("500.00"), Period.WEEKLY, 80);
        limit.setId(1L);

        when(budgetLimitRepository.findById(1L)).thenReturn(Optional.of(limit));
        when(expenseQueryPort.sumExpensesByPeriod(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("100.00"));

        LimitStatusResponse status = limitService.getStatus(1L);

        assertThat(status.periodStart().getDayOfWeek()).isEqualTo(java.time.DayOfWeek.MONDAY);
        assertThat(status.periodEnd().getDayOfWeek()).isEqualTo(java.time.DayOfWeek.SUNDAY);
    }

    @Test
    void shouldReturnMonthlyPeriodBounds() {
        BudgetLimit limit = new BudgetLimit(1L, new BigDecimal("500.00"), Period.MONTHLY, 80);
        limit.setId(1L);

        when(budgetLimitRepository.findById(1L)).thenReturn(Optional.of(limit));
        when(expenseQueryPort.sumExpensesByPeriod(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("100.00"));

        LimitStatusResponse status = limitService.getStatus(1L);

        assertThat(status.periodStart().getDayOfMonth()).isEqualTo(1);
        assertThat(status.periodEnd()).isEqualTo(status.periodStart().with(java.time.temporal.TemporalAdjusters.lastDayOfMonth()));
    }

    @Test
    void shouldReturnAnnualPeriodBounds() {
        BudgetLimit limit = new BudgetLimit(1L, new BigDecimal("500.00"), Period.ANNUAL, 80);
        limit.setId(1L);

        when(budgetLimitRepository.findById(1L)).thenReturn(Optional.of(limit));
        when(expenseQueryPort.sumExpensesByPeriod(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("100.00"));

        LimitStatusResponse status = limitService.getStatus(1L);

        assertThat(status.periodStart().getMonthValue()).isEqualTo(1);
        assertThat(status.periodStart().getDayOfMonth()).isEqualTo(1);
        assertThat(status.periodEnd().getMonthValue()).isEqualTo(12);
        assertThat(status.periodEnd().getDayOfMonth()).isEqualTo(31);
    }
}
