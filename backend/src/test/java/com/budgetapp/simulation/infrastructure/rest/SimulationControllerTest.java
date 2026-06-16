package com.budgetapp.simulation.infrastructure.rest;

import com.budgetapp.simulation.application.SimulationService;
import com.budgetapp.simulation.infrastructure.dto.SimulationRequest;
import com.budgetapp.simulation.infrastructure.dto.SimulationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SimulationController.class)
class SimulationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SimulationService simulationService;

    @Test
    void shouldReturn200_whenValidRequest() throws Exception {
        SimulationRequest request = new SimulationRequest(
                new BigDecimal("5000"), new BigDecimal("3500"), 12, null, null);

        SimulationResponse response = new SimulationResponse(
                new BigDecimal("1500.00"),
                new BigDecimal("18000.00"),
                12,
                new BigDecimal("5000.00"),
                new BigDecimal("3500.00"),
                new BigDecimal("3500.00"),
                new BigDecimal("0.00"),
                null,
                null
        );

        when(simulationService.simulate(any(SimulationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/simulations/savings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectedSavings").value(18000.00))
                .andExpect(jsonPath("$.monthlySavings").value(1500.00))
                .andExpect(jsonPath("$.months").value(12));
    }

    @Test
    void shouldReturn400_whenMonthlyIncomeNegative() throws Exception {
        String body = """
                {
                    "monthlyIncome": -100,
                    "monthlyExpenses": 3500,
                    "months": 12
                }""";

        mockMvc.perform(post("/api/simulations/savings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400_whenMonthlyExpensesNegative() throws Exception {
        String body = """
                {
                    "monthlyIncome": 5000,
                    "monthlyExpenses": -100,
                    "months": 12
                }""";

        mockMvc.perform(post("/api/simulations/savings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400_whenMonthsZero() throws Exception {
        String body = """
                {
                    "monthlyIncome": 5000,
                    "monthlyExpenses": 3500,
                    "months": 0
                }""";

        mockMvc.perform(post("/api/simulations/savings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400_whenMonthsNegative() throws Exception {
        String body = """
                {
                    "monthlyIncome": 5000,
                    "monthlyExpenses": 3500,
                    "months": -1
                }""";

        mockMvc.perform(post("/api/simulations/savings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200_withCurrentSavings() throws Exception {
        SimulationRequest request = new SimulationRequest(
                new BigDecimal("5000"), new BigDecimal("3500"), 12,
                new BigDecimal("2000"), null);

        SimulationResponse response = new SimulationResponse(
                new BigDecimal("1500.00"),
                new BigDecimal("20000.00"),
                12,
                new BigDecimal("5000.00"),
                new BigDecimal("3500.00"),
                new BigDecimal("3500.00"),
                new BigDecimal("2000.00"),
                null,
                null
        );

        when(simulationService.simulate(any(SimulationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/simulations/savings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectedSavings").value(20000.00))
                .andExpect(jsonPath("$.currentSavings").value(2000.00));
    }

    @Test
    void shouldReturn200_withCategoryChanges() throws Exception {
        SimulationRequest request = new SimulationRequest(
                new BigDecimal("5000"), new BigDecimal("3500"), 12,
                null,
                List.of());

        SimulationResponse response = new SimulationResponse(
                new BigDecimal("1500.00"),
                new BigDecimal("18000.00"),
                12,
                new BigDecimal("5000.00"),
                new BigDecimal("3500.00"),
                new BigDecimal("3200.00"),
                new BigDecimal("0.00"),
                new BigDecimal("18000.00"),
                new BigDecimal("3600.00")
        );

        when(simulationService.simulate(any(SimulationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/simulations/savings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adjustedMonthlyExpenses").value(3200.00))
                .andExpect(jsonPath("$.baselineProjectedSavings").value(18000.00))
                .andExpect(jsonPath("$.differenceFromBaseline").value(3600.00));
    }
}
