package com.budgetapp.history.infrastructure.rest;

import com.budgetapp.budgetcore.infrastructure.dto.ExpenseResponse;
import com.budgetapp.history.application.HistoryService;
import com.budgetapp.history.infrastructure.dto.CategorySummaryItem;
import com.budgetapp.history.infrastructure.dto.ExpenseHistoryPage;
import com.budgetapp.history.infrastructure.dto.MonthlySummaryItem;
import com.budgetapp.history.infrastructure.dto.SummaryResponse;
import com.budgetapp.shared.exception.GlobalExceptionHandler;
import com.budgetapp.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HistoryController.class)
@Import(GlobalExceptionHandler.class)
class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HistoryService historyService;

    @Test
    void shouldReturn200_whenGetHistoryWithValidParams() throws Exception {
        ExpenseResponse expense = new ExpenseResponse(1L, 1L, 1L, "Food",
                new BigDecimal("50.00"), "Grocery", LocalDate.of(2026, 1, 15), null);
        ExpenseHistoryPage page = new ExpenseHistoryPage(List.of(expense), 1, 1, 0, 20);

        when(historyService.getHistory(eq(1L), any(), eq(0), eq(20))).thenReturn(page);

        mockMvc.perform(get("/api/expenses/history")
                        .param("budgetId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].categoryName").value("Food"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void shouldReturn200_whenGetHistoryWithAllFilters() throws Exception {
        ExpenseHistoryPage page = new ExpenseHistoryPage(List.of(), 0, 0, 0, 10);

        when(historyService.getHistory(eq(1L), any(), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/api/expenses/history")
                        .param("budgetId", "1")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-12-31")
                        .param("categoryId", "2")
                        .param("search", "grocery")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldReturn200_whenGetHistoryWithCustomPagination() throws Exception {
        ExpenseHistoryPage page = new ExpenseHistoryPage(List.of(), 50, 3, 1, 20);

        when(historyService.getHistory(eq(1L), any(), eq(1), eq(20))).thenReturn(page);

        mockMvc.perform(get("/api/expenses/history")
                        .param("budgetId", "1")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(50))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void shouldReturn404_whenBudgetNotFound_forHistory() throws Exception {
        when(historyService.getHistory(eq(99L), any(), eq(0), eq(20)))
                .thenThrow(new ResourceNotFoundException("Budget", 99L));

        mockMvc.perform(get("/api/expenses/history")
                        .param("budgetId", "99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn200_whenGetSummaryByCategory() throws Exception {
        CategorySummaryItem food = new CategorySummaryItem(1L, "Food",
                new BigDecimal("250.00"), 2, new BigDecimal("71.43"));
        CategorySummaryItem transport = new CategorySummaryItem(2L, "Transport",
                new BigDecimal("100.00"), 1, new BigDecimal("28.57"));
        SummaryResponse summary = new SummaryResponse("category", List.of(food, transport));

        when(historyService.getSummary(eq(1L), eq("category"), isNull(), isNull(), isNull()))
                .thenReturn(summary);

        mockMvc.perform(get("/api/expenses/summary")
                        .param("budgetId", "1")
                        .param("groupBy", "category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupBy").value("category"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].categoryName").value("Food"))
                .andExpect(jsonPath("$.items[0].totalAmount").value(250.00))
                .andExpect(jsonPath("$.items[0].percentage").value(71.43))
                .andExpect(jsonPath("$.items[1].categoryName").value("Transport"));
    }

    @Test
    void shouldReturn200_whenGetSummaryByMonthWithYear() throws Exception {
        MonthlySummaryItem jan = new MonthlySummaryItem("2026-01", new BigDecimal("300.00"), 4);
        MonthlySummaryItem feb = new MonthlySummaryItem("2026-02", new BigDecimal("500.00"), 6);
        SummaryResponse summary = new SummaryResponse("month", List.of(jan, feb));

        when(historyService.getSummary(eq(1L), eq("month"), eq(2026), isNull(), isNull()))
                .thenReturn(summary);

        mockMvc.perform(get("/api/expenses/summary")
                        .param("budgetId", "1")
                        .param("groupBy", "month")
                        .param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupBy").value("month"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].month").value("2026-01"))
                .andExpect(jsonPath("$.items[0].totalAmount").value(300.00));
    }

    @Test
    void shouldReturn200_whenGetSummaryWithDateRange() throws Exception {
        SummaryResponse summary = new SummaryResponse("category", List.of());

        when(historyService.getSummary(eq(1L), eq("category"), isNull(),
                eq(LocalDate.of(2026, 1, 1)), eq(LocalDate.of(2026, 3, 31))))
                .thenReturn(summary);

        mockMvc.perform(get("/api/expenses/summary")
                        .param("budgetId", "1")
                        .param("groupBy", "category")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void shouldReturn400_whenInvalidGroupBy() throws Exception {
        when(historyService.getSummary(eq(1L), eq("week"), isNull(), isNull(), isNull()))
                .thenThrow(new IllegalArgumentException("groupBy must be 'category' or 'month'"));

        mockMvc.perform(get("/api/expenses/summary")
                        .param("budgetId", "1")
                        .param("groupBy", "week"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404_whenBudgetNotFound_forSummary() throws Exception {
        when(historyService.getSummary(eq(99L), eq("category"), isNull(), isNull(), isNull()))
                .thenThrow(new ResourceNotFoundException("Budget", 99L));

        mockMvc.perform(get("/api/expenses/summary")
                        .param("budgetId", "99")
                        .param("groupBy", "category"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnEmptyItems_whenNoExpenses() throws Exception {
        SummaryResponse summary = new SummaryResponse("category", List.of());

        when(historyService.getSummary(eq(1L), eq("category"), isNull(), isNull(), isNull()))
                .thenReturn(summary);

        mockMvc.perform(get("/api/expenses/summary")
                        .param("budgetId", "1")
                        .param("groupBy", "category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }
}
