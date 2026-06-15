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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class LimitService {

    private final BudgetLimitRepository budgetLimitRepository;
    private final ExpenseQueryPort expenseQueryPort;
    private final BudgetRepository budgetRepository;
    private final int defaultWarningThreshold;

    public LimitService(
            BudgetLimitRepository budgetLimitRepository,
            ExpenseQueryPort expenseQueryPort,
            BudgetRepository budgetRepository,
            @Value("${limits.default-warning-threshold:80}") int defaultWarningThreshold) {
        this.budgetLimitRepository = budgetLimitRepository;
        this.expenseQueryPort = expenseQueryPort;
        this.budgetRepository = budgetRepository;
        this.defaultWarningThreshold = defaultWarningThreshold;
    }

    @Transactional
    public LimitResponse create(LimitRequest request) {
        if (!budgetRepository.existsById(request.budgetId())) {
            throw new ResourceNotFoundException("Budget", request.budgetId());
        }

        budgetLimitRepository.findByBudgetIdAndPeriod(request.budgetId(), request.period())
                .ifPresent(existing -> {
                    throw new ConflictException(
                            "A limit already exists for budget " + request.budgetId() + " with period " + request.period());
                });

        int threshold = request.warningThresholdPercent() != null
                ? request.warningThresholdPercent()
                : defaultWarningThreshold;

        BudgetLimit limit = new BudgetLimit(request.budgetId(), request.amount(), request.period(), threshold);
        BudgetLimit saved = budgetLimitRepository.save(limit);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LimitResponse getById(Long id) {
        BudgetLimit limit = budgetLimitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BudgetLimit", id));
        return toResponse(limit);
    }

    @Transactional(readOnly = true)
    public List<LimitResponse> getByBudget(Long budgetId) {
        return budgetLimitRepository.findByBudgetId(budgetId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public LimitResponse update(Long id, LimitRequest request) {
        BudgetLimit limit = budgetLimitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BudgetLimit", id));

        if (!budgetRepository.existsById(request.budgetId())) {
            throw new ResourceNotFoundException("Budget", request.budgetId());
        }

        budgetLimitRepository.findByBudgetIdAndPeriod(request.budgetId(), request.period())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new ConflictException(
                                "A limit already exists for budget " + request.budgetId() + " with period " + request.period());
                    }
                });

        limit.setBudgetId(request.budgetId());
        limit.setAmount(request.amount());
        limit.setPeriod(request.period());
        limit.setWarningThresholdPercent(
                request.warningThresholdPercent() != null ? request.warningThresholdPercent() : defaultWarningThreshold);

        BudgetLimit saved = budgetLimitRepository.save(limit);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        BudgetLimit limit = budgetLimitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BudgetLimit", id));
        budgetLimitRepository.delete(limit);
    }

    @Transactional(readOnly = true)
    public LimitStatusResponse getStatus(Long limitId) {
        BudgetLimit limit = budgetLimitRepository.findById(limitId)
                .orElseThrow(() -> new ResourceNotFoundException("BudgetLimit", limitId));

        LocalDate today = LocalDate.now();
        LocalDate start = calculatePeriodStart(today, limit.getPeriod());
        LocalDate end = calculatePeriodEnd(today, limit.getPeriod());

        BigDecimal spent = expenseQueryPort.sumExpensesByPeriod(limit.getBudgetId(), start, end);

        BigDecimal percentageUsed = limit.getAmount().compareTo(BigDecimal.ZERO) > 0
                ? spent.multiply(new BigDecimal("100")).divide(limit.getAmount(), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal remaining = limit.getAmount().subtract(spent);

        String status;
        if (percentageUsed.compareTo(new BigDecimal("100")) >= 0) {
            status = "EXCEEDED";
        } else if (percentageUsed.compareTo(BigDecimal.valueOf(limit.getWarningThresholdPercent())) >= 0) {
            status = "WARNING";
        } else {
            status = "OK";
        }

        return new LimitStatusResponse(
                limit.getId(),
                limit.getBudgetId(),
                limit.getAmount(),
                spent,
                remaining,
                percentageUsed,
                status,
                limit.getPeriod(),
                start,
                end
        );
    }

    private LocalDate calculatePeriodStart(LocalDate today, Period period) {
        return switch (period) {
            case WEEKLY -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTHLY -> today.withDayOfMonth(1);
            case ANNUAL -> LocalDate.of(today.getYear(), 1, 1);
        };
    }

    private LocalDate calculatePeriodEnd(LocalDate today, Period period) {
        return switch (period) {
            case WEEKLY -> today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            case MONTHLY -> today.with(TemporalAdjusters.lastDayOfMonth());
            case ANNUAL -> LocalDate.of(today.getYear(), 12, 31);
        };
    }

    private LimitResponse toResponse(BudgetLimit limit) {
        return new LimitResponse(
                limit.getId(),
                limit.getBudgetId(),
                limit.getAmount(),
                limit.getPeriod(),
                limit.getWarningThresholdPercent(),
                limit.getCreatedAt()
        );
    }
}
