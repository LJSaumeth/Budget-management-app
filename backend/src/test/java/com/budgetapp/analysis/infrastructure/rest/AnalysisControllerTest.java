package com.budgetapp.analysis.infrastructure.rest;

import com.budgetapp.analysis.application.AnalysisService;
import com.budgetapp.analysis.infrastructure.dto.AnalysisResponse;
import com.budgetapp.analysis.infrastructure.dto.OptimizationSuggestion;
import com.budgetapp.shared.exception.GlobalExceptionHandler;
import com.budgetapp.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalysisController.class)
@Import(GlobalExceptionHandler.class)
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalysisService analysisService;

    @Test
    void shouldReturn200_whenValidBudgetId() throws Exception {
        OptimizationSuggestion suggestion = new OptimizationSuggestion(
                1L, "Food",
                new BigDecimal("546.67"),
                new BigDecimal("11.8"),
                new BigDecimal("73.33"),
                new BigDecimal("879.96"),
                "Food spending ($620.00) is 11.8% above your 3-month average ($546.67). "
                        + "Reducing to $546.67 would save $73.33/month."
        );
        AnalysisResponse response = new AnalysisResponse(
                List.of(suggestion),
                "Analysis complete. Found 1 optimization opportunities.",
                new BigDecimal("879.96")
        );

        when(analysisService.analyze(eq(1L))).thenReturn(response);

        mockMvc.perform(get("/api/analysis/suggestions")
                        .param("budgetId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions.length()").value(1))
                .andExpect(jsonPath("$.suggestions[0].categoryId").value(1))
                .andExpect(jsonPath("$.suggestions[0].categoryName").value("Food"))
                .andExpect(jsonPath("$.suggestions[0].monthlySaving").value(73.33))
                .andExpect(jsonPath("$.suggestions[0].annualSaving").value(879.96))
                .andExpect(jsonPath("$.totalPotentialAnnualSaving").value(879.96))
                .andExpect(jsonPath("$.message").value("Analysis complete. Found 1 optimization opportunities."));
    }

    @Test
    void shouldReturn200_whenEmptySuggestions() throws Exception {
        AnalysisResponse response = new AnalysisResponse(
                List.of(),
                "No significant optimization opportunities found. All categories are within 10% of their historical average.",
                new BigDecimal("0.00")
        );

        when(analysisService.analyze(eq(1L))).thenReturn(response);

        mockMvc.perform(get("/api/analysis/suggestions")
                        .param("budgetId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions.length()").value(0))
                .andExpect(jsonPath("$.totalPotentialAnnualSaving").value(0.00))
                .andExpect(jsonPath("$.message").value(
                        "No significant optimization opportunities found. All categories are within 10% of their historical average."));
    }

    @Test
    void shouldReturn404_whenBudgetNotFound() throws Exception {
        when(analysisService.analyze(eq(99L)))
                .thenThrow(new ResourceNotFoundException("Budget", 99L));

        mockMvc.perform(get("/api/analysis/suggestions")
                        .param("budgetId", "99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400_whenMissingBudgetId() throws Exception {
        mockMvc.perform(get("/api/analysis/suggestions"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200_whenInsufficientData() throws Exception {
        AnalysisResponse response = new AnalysisResponse(
                List.of(),
                "Insufficient data. At least 2 months of expense history are needed for analysis.",
                new BigDecimal("0.00")
        );

        when(analysisService.analyze(eq(1L))).thenReturn(response);

        mockMvc.perform(get("/api/analysis/suggestions")
                        .param("budgetId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions.length()").value(0))
                .andExpect(jsonPath("$.message").value(
                        "Insufficient data. At least 2 months of expense history are needed for analysis."));
    }
}
