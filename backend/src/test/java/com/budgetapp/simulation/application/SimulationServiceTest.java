package com.budgetapp.simulation.application;

import com.budgetapp.simulation.domain.CategoryChange;
import com.budgetapp.simulation.infrastructure.dto.SimulationRequest;
import com.budgetapp.simulation.infrastructure.dto.SimulationResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimulationServiceTest {

    private final SimulationService service = new SimulationService();

    @Test
    void should_projectSavings_withBasicInput() {
        SimulationRequest request = new SimulationRequest(
                new BigDecimal("5000"), new BigDecimal("3500"), 12, null, null);

        SimulationResponse response = service.simulate(request);

        assertThat(response.projectedSavings()).isEqualByComparingTo("18000.00");
        assertThat(response.monthlySavings()).isEqualByComparingTo("1500.00");
        assertThat(response.months()).isEqualTo(12);
    }

    @Test
    void should_projectSavings_withCurrentBalance() {
        SimulationRequest request = new SimulationRequest(
                new BigDecimal("5000"), new BigDecimal("3500"), 12,
                new BigDecimal("2000"), null);

        SimulationResponse response = service.simulate(request);

        assertThat(response.projectedSavings()).isEqualByComparingTo("20000.00");
        assertThat(response.currentSavings()).isEqualByComparingTo("2000.00");
    }

    @Test
    void should_projectSavings_withNegativeCurrentSavings_debt() {
        SimulationRequest request = new SimulationRequest(
                new BigDecimal("5000"), new BigDecimal("3500"), 12,
                new BigDecimal("-1000"), null);

        SimulationResponse response = service.simulate(request);

        assertThat(response.projectedSavings()).isEqualByComparingTo("17000.00");
        assertThat(response.currentSavings()).isEqualByComparingTo("-1000.00");
    }

    @Test
    void should_applyCategoryReductions_toIncreaseMonthlySavings() {
        List<CategoryChange> changes = List.of(
                new CategoryChange("Food", new BigDecimal("200")),
                new CategoryChange("Transport", new BigDecimal("100"))
        );
        SimulationRequest request = new SimulationRequest(
                new BigDecimal("5000"), new BigDecimal("3500"), 12, null, changes);

        SimulationResponse response = service.simulate(request);

        assertThat(response.adjustedMonthlyExpenses()).isEqualByComparingTo("3200.00");
        assertThat(response.monthlySavings()).isEqualByComparingTo("1800.00");
        assertThat(response.projectedSavings()).isEqualByComparingTo("21600.00");
    }

    @Test
    void should_capReductions_when_exceedTotalExpenses() {
        List<CategoryChange> changes = List.of(
                new CategoryChange("Housing", new BigDecimal("5000"))
        );
        SimulationRequest request = new SimulationRequest(
                new BigDecimal("5000"), new BigDecimal("3500"), 12, null, changes);

        SimulationResponse response = service.simulate(request);

        assertThat(response.adjustedMonthlyExpenses()).isEqualByComparingTo("0.00");
        assertThat(response.monthlySavings()).isEqualByComparingTo("5000.00");
        assertThat(response.projectedSavings()).isEqualByComparingTo("60000.00");
    }

    @Test
    void should_projectNegativeSavings_whenExpensesExceedIncome() {
        SimulationRequest request = new SimulationRequest(
                new BigDecimal("3000"), new BigDecimal("5000"), 12, null, null);

        SimulationResponse response = service.simulate(request);

        assertThat(response.monthlySavings()).isEqualByComparingTo("-2000.00");
        assertThat(response.projectedSavings()).isEqualByComparingTo("-24000.00");
    }

    @Test
    void should_returnZeros_when_allInputsZero() {
        SimulationRequest request = new SimulationRequest(
                BigDecimal.ZERO, BigDecimal.ZERO, 1, null, null);

        SimulationResponse response = service.simulate(request);

        assertThat(response.monthlySavings()).isEqualByComparingTo("0.00");
        assertThat(response.projectedSavings()).isEqualByComparingTo("0.00");
    }

    @Test
    void should_includeBaselineFields_when_changesProvided() {
        List<CategoryChange> changes = List.of(
                new CategoryChange("Food", new BigDecimal("300"))
        );
        SimulationRequest request = new SimulationRequest(
                new BigDecimal("5000"), new BigDecimal("3500"), 12, null, changes);

        SimulationResponse response = service.simulate(request);

        assertThat(response.baselineProjectedSavings()).isNotNull();
        assertThat(response.baselineProjectedSavings()).isEqualByComparingTo("18000.00");
        assertThat(response.differenceFromBaseline()).isNotNull();
        assertThat(response.differenceFromBaseline()).isEqualByComparingTo("3600.00");
    }

    @Test
    void should_excludeBaselineFields_when_noChanges() {
        SimulationRequest request = new SimulationRequest(
                new BigDecimal("5000"), new BigDecimal("3500"), 12, null, null);

        SimulationResponse response = service.simulate(request);

        assertThat(response.baselineProjectedSavings()).isNull();
        assertThat(response.differenceFromBaseline()).isNull();
    }

    @Test
    void should_excludeBaselineFields_when_emptyChangesList() {
        SimulationRequest request = new SimulationRequest(
                new BigDecimal("5000"), new BigDecimal("3500"), 12, null, List.of());

        SimulationResponse response = service.simulate(request);

        assertThat(response.baselineProjectedSavings()).isNull();
        assertThat(response.differenceFromBaseline()).isNull();
    }

    @Test
    void should_capMultipleReductions_when_cumulativeExceedsExpenses() {
        List<CategoryChange> changes = List.of(
                new CategoryChange("Food", new BigDecimal("2000")),
                new CategoryChange("Transport", new BigDecimal("2000"))
        );
        SimulationRequest request = new SimulationRequest(
                new BigDecimal("5000"), new BigDecimal("3000"), 12, null, changes);

        SimulationResponse response = service.simulate(request);

        assertThat(response.adjustedMonthlyExpenses()).isEqualByComparingTo("0.00");
        assertThat(response.projectedSavings()).isEqualByComparingTo("60000.00");
    }
}
