package com.budgetapp.budgetcore.infrastructure.rest;

import com.budgetapp.budgetcore.application.BudgetService;
import com.budgetapp.budgetcore.infrastructure.dto.BudgetRequest;
import com.budgetapp.budgetcore.infrastructure.dto.BudgetResponse;
import com.budgetapp.shared.exception.GlobalExceptionHandler;
import com.budgetapp.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BudgetController.class)
@Import(GlobalExceptionHandler.class)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BudgetService budgetService;

    @Test
    void shouldReturn201_whenCreateBudget() throws Exception {
        BudgetRequest request = new BudgetRequest("Monthly Budget", new BigDecimal("5000.00"), "USD");
        BudgetResponse response = new BudgetResponse(1L, "Monthly Budget", new BigDecimal("5000.00"),
                "USD", new BigDecimal("5000.00"), LocalDateTime.now());

        when(budgetService.create(any(BudgetRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Monthly Budget"))
                .andExpect(jsonPath("$.totalAmount").value(5000.00));
    }

    @Test
    void shouldReturn400_whenNameIsBlank() throws Exception {
        BudgetRequest request = new BudgetRequest("", new BigDecimal("5000.00"), "USD");

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400_whenAmountIsZero() throws Exception {
        BudgetRequest request = new BudgetRequest("Test", new BigDecimal("0.00"), "USD");

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200_whenGetAllBudgets() throws Exception {
        BudgetResponse response = new BudgetResponse(1L, "Monthly Budget", new BigDecimal("5000.00"),
                "USD", new BigDecimal("5000.00"), LocalDateTime.now());

        when(budgetService.getAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/budgets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Monthly Budget"));
    }

    @Test
    void shouldReturn200_whenGetBudgetById() throws Exception {
        BudgetResponse response = new BudgetResponse(1L, "Monthly Budget", new BigDecimal("5000.00"),
                "USD", new BigDecimal("5000.00"), LocalDateTime.now());

        when(budgetService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/budgets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Monthly Budget"));
    }

    @Test
    void shouldReturn404_whenBudgetNotFound() throws Exception {
        when(budgetService.getById(99L)).thenThrow(new ResourceNotFoundException("Budget", 99L));

        mockMvc.perform(get("/api/budgets/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn200_whenUpdateBudget() throws Exception {
        BudgetRequest request = new BudgetRequest("Updated Budget", new BigDecimal("6000.00"), "USD");
        BudgetResponse response = new BudgetResponse(1L, "Updated Budget", new BigDecimal("6000.00"),
                "USD", new BigDecimal("6000.00"), LocalDateTime.now());

        when(budgetService.update(eq(1L), any(BudgetRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/budgets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Budget"))
                .andExpect(jsonPath("$.totalAmount").value(6000.00));
    }

    @Test
    void shouldReturn204_whenDeleteBudget() throws Exception {
        doNothing().when(budgetService).delete(1L);

        mockMvc.perform(delete("/api/budgets/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404_whenDeleteNonExistentBudget() throws Exception {
        doThrow(new ResourceNotFoundException("Budget", 99L)).when(budgetService).delete(99L);

        mockMvc.perform(delete("/api/budgets/99"))
                .andExpect(status().isNotFound());
    }
}
