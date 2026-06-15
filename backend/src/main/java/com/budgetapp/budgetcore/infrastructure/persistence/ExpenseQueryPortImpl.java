package com.budgetapp.budgetcore.infrastructure.persistence;

import com.budgetapp.budgetcore.domain.Expense;
import com.budgetapp.budgetcore.domain.port.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public class ExpenseQueryPortImpl implements ExpenseQueryPort {

    private final ExpenseRepository expenseRepository;

    public ExpenseQueryPortImpl(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Override
    public Page<Expense> findExpenses(Long budgetId, ExpenseFilter filter, Pageable pageable) {
        return expenseRepository.findFiltered(
                budgetId,
                filter != null ? filter.categoryId() : null,
                filter != null ? filter.startDate() : null,
                filter != null ? filter.endDate() : null,
                filter != null ? filter.search() : null,
                pageable
        );
    }

    @Override
    public List<CategorySummary> summarizeByCategory(Long budgetId, LocalDate start, LocalDate end) {
        return expenseRepository.summarizeByCategory(budgetId, start, end);
    }

    @Override
    public List<MonthlySummary> summarizeByMonth(Long budgetId, int year, LocalDate start, LocalDate end) {
        return expenseRepository.summarizeByMonth(budgetId, year, start, end);
    }

    @Override
    public BigDecimal sumExpensesByPeriod(Long budgetId, LocalDate start, LocalDate end) {
        return expenseRepository.sumByBudgetAndDateRange(budgetId, start, end);
    }

    @Override
    public List<CategoryMonthlySpending> getCategoryMonthlySpending(Long budgetId) {
        return expenseRepository.findMonthlySpendingByCategory(budgetId);
    }
}
