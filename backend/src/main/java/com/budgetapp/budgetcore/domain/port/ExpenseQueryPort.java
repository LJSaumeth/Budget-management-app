package com.budgetapp.budgetcore.domain.port;

import com.budgetapp.budgetcore.domain.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseQueryPort {

    Page<Expense> findExpenses(Long budgetId, ExpenseFilter filter, Pageable pageable);

    List<CategorySummary> summarizeByCategory(Long budgetId, LocalDate start, LocalDate end);

    List<MonthlySummary> summarizeByMonth(Long budgetId, int year, LocalDate start, LocalDate end);

    BigDecimal sumExpensesByPeriod(Long budgetId, LocalDate start, LocalDate end);

    List<CategoryMonthlySpending> getCategoryMonthlySpending(Long budgetId);
}
