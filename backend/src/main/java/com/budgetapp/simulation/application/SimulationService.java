package com.budgetapp.simulation.application;

import com.budgetapp.simulation.domain.CategoryChange;
import com.budgetapp.simulation.infrastructure.dto.SimulationRequest;
import com.budgetapp.simulation.infrastructure.dto.SimulationResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class SimulationService {

    private static final int SCALE = 2;

    public SimulationResponse simulate(SimulationRequest request) {
        BigDecimal monthlyIncome = request.monthlyIncome();
        BigDecimal monthlyExpenses = request.monthlyExpenses();
        int months = request.months();
        BigDecimal currentSavings = request.currentSavings() != null
                ? request.currentSavings()
                : BigDecimal.ZERO;
        List<CategoryChange> expectedChanges = request.expectedChanges() != null
                ? request.expectedChanges()
                : List.of();

        BigDecimal adjustedExpenses = applyReductions(monthlyExpenses, expectedChanges);

        BigDecimal adjustedMonthlySavings = monthlyIncome.subtract(adjustedExpenses);

        BigDecimal projectedSavings = currentSavings
                .add(adjustedMonthlySavings.multiply(BigDecimal.valueOf(months)))
                .setScale(SCALE, RoundingMode.HALF_UP);

        if (expectedChanges.isEmpty()) {
            return new SimulationResponse(
                    adjustedMonthlySavings.setScale(SCALE, RoundingMode.HALF_UP),
                    projectedSavings,
                    months,
                    monthlyIncome.setScale(SCALE, RoundingMode.HALF_UP),
                    monthlyExpenses.setScale(SCALE, RoundingMode.HALF_UP),
                    adjustedExpenses.setScale(SCALE, RoundingMode.HALF_UP),
                    currentSavings.setScale(SCALE, RoundingMode.HALF_UP),
                    null,
                    null
            );
        }

        BigDecimal baselineMonthlySavings = monthlyIncome.subtract(monthlyExpenses);
        BigDecimal baselineProjectedSavings = currentSavings
                .add(baselineMonthlySavings.multiply(BigDecimal.valueOf(months)))
                .setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal differenceFromBaseline = projectedSavings.subtract(baselineProjectedSavings)
                .setScale(SCALE, RoundingMode.HALF_UP);

        return new SimulationResponse(
                adjustedMonthlySavings.setScale(SCALE, RoundingMode.HALF_UP),
                projectedSavings,
                months,
                monthlyIncome.setScale(SCALE, RoundingMode.HALF_UP),
                monthlyExpenses.setScale(SCALE, RoundingMode.HALF_UP),
                adjustedExpenses.setScale(SCALE, RoundingMode.HALF_UP),
                currentSavings.setScale(SCALE, RoundingMode.HALF_UP),
                baselineProjectedSavings,
                differenceFromBaseline
        );
    }

    private BigDecimal applyReductions(BigDecimal monthlyExpenses, List<CategoryChange> changes) {
        BigDecimal remaining = monthlyExpenses;

        for (CategoryChange change : changes) {
            BigDecimal reduction = change.reductionAmount() != null ? change.reductionAmount() : BigDecimal.ZERO;
            if (reduction.compareTo(BigDecimal.ZERO) < 0) {
                reduction = BigDecimal.ZERO;
            }
            reduction = reduction.min(remaining);
            remaining = remaining.subtract(reduction);
        }

        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        return remaining;
    }
}
