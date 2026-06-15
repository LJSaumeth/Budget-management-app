package com.budgetapp.budgetcore.infrastructure.rest;

import com.budgetapp.budgetcore.application.ExpenseService;
import com.budgetapp.budgetcore.infrastructure.dto.ExpenseRequest;
import com.budgetapp.budgetcore.infrastructure.dto.ExpenseResponse;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
@Import(GlobalExceptionHandler.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExpenseService expenseService;

    @Test
    void shouldReturn201_whenCreateExpense() throws Exception {
        ExpenseRequest request = new ExpenseRequest(1L, 2L, new BigDecimal("45.50"),
                "Grocery shopping", LocalDate.of(2026, 6, 15));
        ExpenseResponse response = new ExpenseResponse(1L, 1L, 2L, "Food", new BigDecimal("45.50"),
                "Grocery shopping", LocalDate.of(2026, 6, 15), LocalDateTime.now());

        when(expenseService.create(any(ExpenseRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(45.50))
                .andExpect(jsonPath("$.categoryName").value("Food"));
    }

    @Test
    void shouldReturn400_whenAmountIsZero() throws Exception {
        ExpenseRequest request = new ExpenseRequest(1L, 2L, new BigDecimal("0.00"),
                "Test", LocalDate.of(2026, 6, 15));

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400_whenBudgetIdIsNull() throws Exception {
        ExpenseRequest request = new ExpenseRequest(null, 2L, new BigDecimal("10.00"),
                "Test", LocalDate.of(2026, 6, 15));

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400_whenCategoryIdIsNull() throws Exception {
        ExpenseRequest request = new ExpenseRequest(1L, null, new BigDecimal("10.00"),
                "Test", LocalDate.of(2026, 6, 15));

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200_whenGetExpenseById() throws Exception {
        ExpenseResponse response = new ExpenseResponse(1L, 1L, 2L, "Food", new BigDecimal("45.50"),
                "Grocery", LocalDate.of(2026, 6, 15), LocalDateTime.now());

        when(expenseService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/expenses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Grocery"));
    }

    @Test
    void shouldReturn404_whenExpenseNotFound() throws Exception {
        when(expenseService.getById(99L)).thenThrow(new ResourceNotFoundException("Expense", 99L));

        mockMvc.perform(get("/api/expenses/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn200_whenUpdateExpense() throws Exception {
        ExpenseRequest request = new ExpenseRequest(1L, 2L, new BigDecimal("55.00"),
                "Updated description", LocalDate.of(2026, 6, 15));
        ExpenseResponse response = new ExpenseResponse(1L, 1L, 2L, "Food", new BigDecimal("55.00"),
                "Updated description", LocalDate.of(2026, 6, 15), LocalDateTime.now());

        when(expenseService.update(eq(1L), any(ExpenseRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void shouldReturn204_whenDeleteExpense() throws Exception {
        doNothing().when(expenseService).delete(1L);

        mockMvc.perform(delete("/api/expenses/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404_whenExpenseNotFoundOnDelete() throws Exception {
        doThrow(new ResourceNotFoundException("Expense", 99L)).when(expenseService).delete(99L);

        mockMvc.perform(delete("/api/expenses/99"))
                .andExpect(status().isNotFound());
    }
}
