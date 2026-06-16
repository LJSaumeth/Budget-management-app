package com.budgetapp.analysis.application;

import com.budgetapp.analysis.infrastructure.dto.AnalysisResponse;
import com.budgetapp.analysis.infrastructure.dto.OptimizationSuggestion;
import com.budgetapp.budgetcore.domain.port.CategoryMonthlySpending;
import com.budgetapp.budgetcore.domain.port.ExpenseQueryPort;
import com.budgetapp.budgetcore.infrastructure.persistence.BudgetRepository;
import com.budgetapp.shared.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AnalysisService {

    private static final int MONEY_SCALE = 2;
    private static final int PERCENT_SCALE = 1;
    private static final int MONTHS_IN_YEAR = 12;

    private final ExpenseQueryPort expenseQueryPort;
    private final BudgetRepository budgetRepository;
    private final int minMonths;
    private final BigDecimal overspendThreshold;
    private final int maxSuggestions;

    public AnalysisService(
            ExpenseQueryPort expenseQueryPort,
            BudgetRepository budgetRepository,
            @Value("${analysis.min-months:2}") int minMonths,
            @Value("${analysis.overspend-threshold-percent:10}") int overspendThresholdPercent,
            @Value("${analysis.max-suggestions:5}") int maxSuggestions) {
        this.expenseQueryPort = expenseQueryPort;
        this.budgetRepository = budgetRepository;
        this.minMonths = minMonths;
        this.overspendThreshold = BigDecimal.ONE.add(
                new BigDecimal(overspendThresholdPercent).divide(new BigDecimal("100"), MONEY_SCALE, RoundingMode.HALF_UP));
        this.maxSuggestions = maxSuggestions;
    }

    @Transactional(readOnly = true)
    public AnalysisResponse analyze(Long budgetId) {
        validateBudget(budgetId);

        List<CategoryMonthlySpending> spendingData = fetchSpendingData(budgetId);

        if (spendingData.isEmpty()) {
            return buildResponse(List.of(), "No expense data available");
        }

        if (!hasSufficientData(spendingData)) {
            return buildResponse(List.of(),
                    "Insufficient data. At least " + minMonths + " months of expense history are needed for analysis.");
        }

        Map<Long, List<CategoryMonthlySpending>> groupedByCategory = groupByCategory(spendingData);

        List<OptimizationSuggestion> suggestions = flagOverspending(groupedByCategory);

        List<OptimizationSuggestion> sortedAndCapped = sortAndCap(suggestions);

        if (sortedAndCapped.isEmpty()) {
            return buildResponse(List.of(),
                    "No significant optimization opportunities found. All categories are within "
                            + "10% of their historical average.");
        }

        BigDecimal totalAnnualSaving = computeTotalAnnualSaving(sortedAndCapped);

        return new AnalysisResponse(
                sortedAndCapped,
                "Analysis complete. Found " + sortedAndCapped.size() + " optimization opportunities.",
                totalAnnualSaving
        );
    }

    private void validateBudget(Long budgetId) {
        if (!budgetRepository.existsById(budgetId)) {
            throw new ResourceNotFoundException("Budget", budgetId);
        }
    }

    private List<CategoryMonthlySpending> fetchSpendingData(Long budgetId) {
        return expenseQueryPort.getCategoryMonthlySpending(budgetId);
    }

    private boolean hasSufficientData(List<CategoryMonthlySpending> data) {
        Set<String> distinctMonths = data.stream()
                .map(d -> d.year() + "-" + d.month())
                .collect(Collectors.toSet());
        return distinctMonths.size() >= minMonths;
    }

    private Map<Long, List<CategoryMonthlySpending>> groupByCategory(List<CategoryMonthlySpending> data) {
        return data.stream()
                .collect(Collectors.groupingBy(CategoryMonthlySpending::categoryId));
    }

    private List<OptimizationSuggestion> flagOverspending(
            Map<Long, List<CategoryMonthlySpending>> groupedByCategory) {
        List<OptimizationSuggestion> suggestions = new ArrayList<>();

        for (Map.Entry<Long, List<CategoryMonthlySpending>> entry : groupedByCategory.entrySet()) {
            List<CategoryMonthlySpending> monthlyData = entry.getValue();

            if (monthlyData.size() < 2) {
                continue;
            }

            monthlyData.sort(Comparator
                    .comparingInt(CategoryMonthlySpending::year)
                    .thenComparingInt(CategoryMonthlySpending::month)
                    .reversed());

            BigDecimal overallMonthlyAvg = computeOverallMonthlyAvg(monthlyData);
            BigDecimal recentMonthTotal = monthlyData.get(0).total();

            if (recentMonthTotal.compareTo(overallMonthlyAvg.multiply(overspendThreshold)) > 0) {
                OptimizationSuggestion suggestion = buildSuggestion(
                        monthlyData.get(0).categoryId(),
                        monthlyData.get(0).categoryName(),
                        overallMonthlyAvg,
                        recentMonthTotal,
                        monthlyData.size()
                );
                suggestions.add(suggestion);
            }
        }

        return suggestions;
    }

    private BigDecimal computeOverallMonthlyAvg(List<CategoryMonthlySpending> monthlyData) {
        BigDecimal sum = monthlyData.stream()
                .map(CategoryMonthlySpending::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(new BigDecimal(monthlyData.size()), MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private OptimizationSuggestion buildSuggestion(
            Long categoryId, String categoryName,
            BigDecimal overallMonthlyAvg, BigDecimal recentMonthTotal, int monthCount) {

        BigDecimal difference = recentMonthTotal.subtract(overallMonthlyAvg);
        BigDecimal suggestedReductionPercent = difference
                .divide(recentMonthTotal, PERCENT_SCALE + 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(PERCENT_SCALE, RoundingMode.HALF_UP);

        BigDecimal monthlySaving = difference.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal annualSaving = monthlySaving.multiply(new BigDecimal(MONTHS_IN_YEAR))
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        String reasoning = categoryName + " spending ($" + formatMoney(recentMonthTotal)
                + ") is " + suggestedReductionPercent + "% above your " + monthCount
                + "-month average ($" + formatMoney(overallMonthlyAvg)
                + "). Reducing to $" + formatMoney(overallMonthlyAvg)
                + " would save $" + formatMoney(monthlySaving) + "/month.";

        return new OptimizationSuggestion(
                categoryId,
                categoryName,
                overallMonthlyAvg,
                suggestedReductionPercent,
                monthlySaving,
                annualSaving,
                reasoning
        );
    }

    private List<OptimizationSuggestion> sortAndCap(List<OptimizationSuggestion> suggestions) {
        return suggestions.stream()
                .sorted(Comparator.comparing(OptimizationSuggestion::annualSaving).reversed())
                .limit(maxSuggestions)
                .toList();
    }

    private BigDecimal computeTotalAnnualSaving(List<OptimizationSuggestion> suggestions) {
        return suggestions.stream()
                .map(OptimizationSuggestion::annualSaving)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private AnalysisResponse buildResponse(List<OptimizationSuggestion> suggestions, String message) {
        return new AnalysisResponse(suggestions, message, BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
    }

    private String formatMoney(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP).toString();
    }
}
