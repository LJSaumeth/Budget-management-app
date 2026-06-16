package com.budgetapp.exchange.infrastructure.rest;

import com.budgetapp.exchange.application.ExchangeService;
import com.budgetapp.exchange.infrastructure.dto.ConversionResult;
import com.budgetapp.shared.exception.ExchangeApiException;
import com.budgetapp.shared.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExchangeController.class)
@Import(GlobalExceptionHandler.class)
class ExchangeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExchangeService exchangeService;

    @Test
    void shouldReturn200_whenGetRates() throws Exception {
        Map<String, BigDecimal> rates = Map.of(
                "EUR", new BigDecimal("0.92"),
                "GBP", new BigDecimal("0.79")
        );

        when(exchangeService.getRates(eq("USD"))).thenReturn(rates);
        when(exchangeService.getCacheTimestamp(eq("USD"))).thenReturn(Instant.parse("2026-06-15T12:00:00Z"));

        mockMvc.perform(get("/api/exchange/rates")
                        .param("base", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.base").value("USD"))
                .andExpect(jsonPath("$.rates.EUR").value(0.92))
                .andExpect(jsonPath("$.rates.GBP").value(0.79))
                .andExpect(jsonPath("$.fetchedAt").value("2026-06-15T12:00:00Z"));
    }

    @Test
    void shouldReturn400_whenMissingBaseParam() throws Exception {
        mockMvc.perform(get("/api/exchange/rates"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200_whenConvert() throws Exception {
        ConversionResult result = new ConversionResult(
                "USD", "EUR",
                new BigDecimal("100.00"),
                new BigDecimal("0.92"),
                new BigDecimal("92.00"),
                Instant.parse("2026-06-15T12:00:00Z")
        );

        when(exchangeService.convert(new BigDecimal("100"), "USD", "EUR")).thenReturn(result);

        mockMvc.perform(get("/api/exchange/convert")
                        .param("amount", "100")
                        .param("from", "USD")
                        .param("to", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("USD"))
                .andExpect(jsonPath("$.to").value("EUR"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.rate").value(0.92))
                .andExpect(jsonPath("$.result").value(92.00))
                .andExpect(jsonPath("$.fetchedAt").value("2026-06-15T12:00:00Z"));
    }

    @Test
    void shouldReturn400_whenAmountNotPositive() throws Exception {
        when(exchangeService.convert(new BigDecimal("0"), "USD", "EUR"))
                .thenThrow(new IllegalArgumentException("Amount must be greater than 0"));

        mockMvc.perform(get("/api/exchange/convert")
                        .param("amount", "0")
                        .param("from", "USD")
                        .param("to", "EUR"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400_whenUnsupportedCurrency() throws Exception {
        when(exchangeService.convert(new BigDecimal("100"), "USD", "XYZ"))
                .thenThrow(new IllegalArgumentException("Unsupported currency: XYZ"));

        mockMvc.perform(get("/api/exchange/convert")
                        .param("amount", "100")
                        .param("from", "USD")
                        .param("to", "XYZ"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400_whenBlankBaseCurrency() throws Exception {
        when(exchangeService.getRates(eq("")))
                .thenThrow(new IllegalArgumentException("Currency must not be blank"));

        mockMvc.perform(get("/api/exchange/rates")
                        .param("base", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn503_whenExchangeApiFails() throws Exception {
        when(exchangeService.getRates(eq("USD")))
                .thenThrow(new ExchangeApiException("Exchange rate service unavailable"));

        mockMvc.perform(get("/api/exchange/rates")
                        .param("base", "USD"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void shouldReturn503_whenConvertApiFails() throws Exception {
        when(exchangeService.convert(new BigDecimal("100"), "USD", "EUR"))
                .thenThrow(new ExchangeApiException("Exchange rate service unavailable"));

        mockMvc.perform(get("/api/exchange/convert")
                        .param("amount", "100")
                        .param("from", "USD")
                        .param("to", "EUR"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void shouldReturn200_whenSameCurrencyConversion() throws Exception {
        ConversionResult result = new ConversionResult(
                "USD", "USD",
                new BigDecimal("100.00"),
                new BigDecimal("1.00"),
                new BigDecimal("100.00"),
                Instant.now()
        );

        when(exchangeService.convert(new BigDecimal("100"), "USD", "USD")).thenReturn(result);

        mockMvc.perform(get("/api/exchange/convert")
                        .param("amount", "100")
                        .param("from", "USD")
                        .param("to", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rate").value(1.00))
                .andExpect(jsonPath("$.result").value(100.00));
    }
}
