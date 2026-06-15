package com.budgetapp.limits.infrastructure.rest;

import com.budgetapp.limits.application.LimitService;
import com.budgetapp.limits.domain.Period;
import com.budgetapp.limits.infrastructure.dto.LimitRequest;
import com.budgetapp.limits.infrastructure.dto.LimitResponse;
import com.budgetapp.limits.infrastructure.dto.LimitStatusResponse;
import com.budgetapp.shared.exception.ConflictException;
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

@WebMvcTest(LimitController.class)
@Import(GlobalExceptionHandler.class)
class LimitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LimitService limitService;

    @Test
    void shouldReturn201_whenCreateLimit() throws Exception {
        LimitRequest request = new LimitRequest(1L, new BigDecimal("500.00"), Period.MONTHLY, 80);
        LimitResponse response = new LimitResponse(1L, 1L, new BigDecimal("500.00"),
                Period.MONTHLY, 80, LocalDateTime.now());

        when(limitService.create(any(LimitRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/limits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.budgetId").value(1))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.period").value("MONTHLY"))
                .andExpect(jsonPath("$.warningThresholdPercent").value(80));
    }

    @Test
    void shouldReturn400_whenAmountIsZero() throws Exception {
        LimitRequest request = new LimitRequest(1L, new BigDecimal("0.00"), Period.MONTHLY, 80);

        mockMvc.perform(post("/api/limits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400_whenBudgetIdIsNull() throws Exception {
        LimitRequest request = new LimitRequest(null, new BigDecimal("500.00"), Period.MONTHLY, 80);

        mockMvc.perform(post("/api/limits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200_whenGetLimitById() throws Exception {
        LimitResponse response = new LimitResponse(1L, 1L, new BigDecimal("500.00"),
                Period.MONTHLY, 80, LocalDateTime.now());

        when(limitService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/limits/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.period").value("MONTHLY"));
    }

    @Test
    void shouldReturn404_whenLimitNotFound() throws Exception {
        when(limitService.getById(99L)).thenThrow(new ResourceNotFoundException("BudgetLimit", 99L));

        mockMvc.perform(get("/api/limits/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn200_whenGetLimitsByBudget() throws Exception {
        LimitResponse response = new LimitResponse(1L, 1L, new BigDecimal("500.00"),
                Period.WEEKLY, 80, LocalDateTime.now());

        when(limitService.getByBudget(1L)).thenReturn(java.util.List.of(response));

        mockMvc.perform(get("/api/limits?budgetId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].period").value("WEEKLY"));
    }

    @Test
    void shouldReturn200_whenUpdateLimit() throws Exception {
        LimitRequest request = new LimitRequest(1L, new BigDecimal("1000.00"), Period.ANNUAL, 70);
        LimitResponse response = new LimitResponse(1L, 1L, new BigDecimal("1000.00"),
                Period.ANNUAL, 70, LocalDateTime.now());

        when(limitService.update(eq(1L), any(LimitRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/limits/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(1000.00))
                .andExpect(jsonPath("$.period").value("ANNUAL"))
                .andExpect(jsonPath("$.warningThresholdPercent").value(70));
    }

    @Test
    void shouldReturn204_whenDeleteLimit() throws Exception {
        doNothing().when(limitService).delete(1L);

        mockMvc.perform(delete("/api/limits/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404_whenDeleteNonExistentLimit() throws Exception {
        doThrow(new ResourceNotFoundException("BudgetLimit", 99L)).when(limitService).delete(99L);

        mockMvc.perform(delete("/api/limits/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn200_whenGetStatus() throws Exception {
        LimitStatusResponse status = new LimitStatusResponse(
                1L, 1L, new BigDecimal("1000.00"), new BigDecimal("500.00"),
                new BigDecimal("500.00"), new BigDecimal("50.00"), "OK",
                Period.MONTHLY, LocalDate.now().withDayOfMonth(1),
                LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1));

        when(limitService.getStatus(1L)).thenReturn(status);

        mockMvc.perform(get("/api/limits/1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.limitId").value(1))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.percentageUsed").value(50.00))
                .andExpect(jsonPath("$.period").value("MONTHLY"));
    }

    @Test
    void shouldReturn404_whenGetStatusForNonExistentLimit() throws Exception {
        when(limitService.getStatus(99L)).thenThrow(new ResourceNotFoundException("BudgetLimit", 99L));

        mockMvc.perform(get("/api/limits/99/status"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn409_whenDuplicatePeriod() throws Exception {
        LimitRequest request = new LimitRequest(1L, new BigDecimal("500.00"), Period.MONTHLY, 80);

        when(limitService.create(any(LimitRequest.class)))
                .thenThrow(new ConflictException("A limit already exists for budget 1 with period MONTHLY"));

        mockMvc.perform(post("/api/limits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
