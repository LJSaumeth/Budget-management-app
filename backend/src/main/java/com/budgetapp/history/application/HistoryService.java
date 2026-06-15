package com.budgetapp.history.application;

import com.budgetapp.budgetcore.domain.Expense;
import com.budgetapp.budgetcore.domain.port.CategorySummary;
import com.budgetapp.budgetcore.domain.port.ExpenseFilter;
import com.budgetapp.budgetcore.domain.port.ExpenseQueryPort;
import com.budgetapp.budgetcore.domain.port.MonthlySummary;
import com.budgetapp.budgetcore.infrastructure.dto.ExpenseResponse;
import com.budgetapp.budgetcore.infrastructure.persistence.BudgetRepository;
import com.budgetapp.history.infrastructure.dto.CategorySummaryItem;
import com.budgetapp.history.infrastructure.dto.ExpenseFilterRequest;
import com.budgetapp.history.infrastructure.dto.ExpenseHistoryPage;
import com.budgetapp.history.infrastructure.dto.MonthlySummaryItem;
import com.budgetapp.history.infrastructure.dto.SummaryResponse;
import com.budgetapp.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class HistoryService {

    private static final Set<String> VALID_GROUP_BY = Set.of("category", "month");

    private final ExpenseQueryPort expenseQueryPort;
    private final BudgetRepository budgetRepository;

    public HistoryService(ExpenseQueryPort expenseQueryPort, BudgetRepository budgetRepository) {
        this.expenseQueryPort = expenseQueryPort;
        this.budgetRepository = budgetRepository;
    }

    public ExpenseHistoryPage getHistory(Long budgetId, ExpenseFilterRequest filter, int page, int size) {
        validateBudgetExists(budgetId);

        int cappedSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, cappedSize, Sort.by("date").descending());

        ExpenseFilter expenseFilter = new ExpenseFilter(
                filter.categoryId(),
                filter.startDate(),
                filter.endDate(),
                filter.search()
        );

        Page<Expense> expensePage = expenseQueryPort.findExpenses(budgetId, expenseFilter, pageable);

        List<ExpenseResponse> content = expensePage.getContent().stream()
                .map(this::toExpenseResponse)
                .toList();

        return new ExpenseHistoryPage(
                content,
                expensePage.getTotalElements(),
                expensePage.getTotalPages(),
                expensePage.getNumber(),
                expensePage.getSize()
        );
    }

    public SummaryResponse getSummary(Long budgetId, String groupBy, Integer year,
                                      LocalDate start, LocalDate end) {
        validateBudgetExists(budgetId);

        if (!VALID_GROUP_BY.contains(groupBy)) {
            throw new IllegalArgumentException("groupBy must be 'category' or 'month'");
        }

        if ("category".equals(groupBy)) {
            List<CategorySummary> summaries = expenseQueryPort.summarizeByCategory(budgetId, start, end);
            BigDecimal total = summaries.stream()
                    .map(CategorySummary::totalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<CategorySummaryItem> items = summaries.stream()
                    .map(s -> toCategorySummaryItem(s, total))
                    .toList();

            return new SummaryResponse(groupBy, items);
        } else {
            int effectiveYear = year != null ? year : Year.now().getValue();
            List<MonthlySummary> summaries = expenseQueryPort.summarizeByMonth(budgetId, effectiveYear, start, end);

            List<MonthlySummaryItem> items = summaries.stream()
                    .map(this::toMonthlySummaryItem)
                    .toList();

            return new SummaryResponse(groupBy, items);
        }
    }

    private void validateBudgetExists(Long budgetId) {
        if (!budgetRepository.existsById(budgetId)) {
            throw new ResourceNotFoundException("Budget", budgetId);
        }
    }

    private ExpenseResponse toExpenseResponse(Expense expense) {
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

    private CategorySummaryItem toCategorySummaryItem(CategorySummary summary, BigDecimal total) {
        BigDecimal percentage;
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            percentage = BigDecimal.ZERO;
        } else {
            percentage = summary.totalAmount()
                    .multiply(new BigDecimal("100"))
                    .divide(total, 2, RoundingMode.HALF_UP);
        }
        return new CategorySummaryItem(
                summary.categoryId(),
                summary.categoryName(),
                summary.totalAmount(),
                summary.count(),
                percentage
        );
    }

    private MonthlySummaryItem toMonthlySummaryItem(MonthlySummary summary) {
        return new MonthlySummaryItem(
                summary.month(),
                summary.totalAmount(),
                summary.count()
        );
    }
}
