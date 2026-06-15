package com.budgetapp.budgetcore.infrastructure.persistence;

import com.budgetapp.budgetcore.domain.Expense;
import com.budgetapp.budgetcore.domain.port.CategoryMonthlySpending;
import com.budgetapp.budgetcore.domain.port.CategorySummary;
import com.budgetapp.budgetcore.domain.port.MonthlySummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("""
            SELECT e FROM Expense e
            JOIN FETCH e.category
            WHERE e.budget.id = :budgetId
            AND (:categoryId IS NULL OR e.category.id = :categoryId)
            AND (:startDate IS NULL OR e.date >= :startDate)
            AND (:endDate IS NULL OR e.date <= :endDate)
            AND (:search IS NULL OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Expense> findFiltered(@Param("budgetId") Long budgetId,
                               @Param("categoryId") Long categoryId,
                               @Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate,
                               @Param("search") String search,
                               Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.budget.id = :budgetId
            AND (:start IS NULL OR e.date >= :start)
            AND (:end IS NULL OR e.date <= :end)
            """)
    BigDecimal sumByBudgetAndDateRange(@Param("budgetId") Long budgetId,
                                       @Param("start") LocalDate start,
                                       @Param("end") LocalDate end);

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.budget.id = :budgetId
            AND e.category.id = :categoryId
            AND (:start IS NULL OR e.date >= :start)
            AND (:end IS NULL OR e.date <= :end)
            """)
    BigDecimal sumByBudgetCategoryAndDateRange(@Param("budgetId") Long budgetId,
                                                @Param("categoryId") Long categoryId,
                                                @Param("start") LocalDate start,
                                                @Param("end") LocalDate end);

    @Query("""
            SELECT new com.budgetapp.budgetcore.domain.port.CategoryMonthlySpending(
                c.id, c.name, YEAR(e.date), MONTH(e.date), SUM(e.amount))
            FROM Expense e
            JOIN e.category c
            WHERE e.budget.id = :budgetId
            GROUP BY c.id, c.name, YEAR(e.date), MONTH(e.date)
            ORDER BY YEAR(e.date) DESC, MONTH(e.date) DESC
            """)
    List<CategoryMonthlySpending> findMonthlySpendingByCategory(@Param("budgetId") Long budgetId);

    @Query("""
            SELECT new com.budgetapp.budgetcore.domain.port.CategorySummary(
                c.id, c.name, COALESCE(SUM(e.amount), 0), COUNT(e))
            FROM Expense e
            JOIN e.category c
            WHERE e.budget.id = :budgetId
            AND (:start IS NULL OR e.date >= :start)
            AND (:end IS NULL OR e.date <= :end)
            GROUP BY c.id, c.name
            ORDER BY SUM(e.amount) DESC
            """)
    List<CategorySummary> summarizeByCategory(@Param("budgetId") Long budgetId,
                                               @Param("start") LocalDate start,
                                               @Param("end") LocalDate end);

    @Query("""
            SELECT new com.budgetapp.budgetcore.domain.port.MonthlySummary(
                CONCAT(CAST(YEAR(e.date) AS string), '-', CAST(MONTH(e.date) AS string)),
                COALESCE(SUM(e.amount), 0),
                COUNT(e))
            FROM Expense e
            WHERE e.budget.id = :budgetId
            AND (:year IS NULL OR YEAR(e.date) = :year)
            AND (:start IS NULL OR e.date >= :start)
            AND (:end IS NULL OR e.date <= :end)
            GROUP BY YEAR(e.date), MONTH(e.date)
            ORDER BY YEAR(e.date) DESC, MONTH(e.date) DESC
            """)
    List<MonthlySummary> summarizeByMonth(@Param("budgetId") Long budgetId,
                                           @Param("year") Integer year,
                                           @Param("start") LocalDate start,
                                           @Param("end") LocalDate end);

    boolean existsByCategoryId(Long categoryId);
}
