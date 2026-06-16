package com.budgetapp.exchange.application;

import com.budgetapp.exchange.domain.ExchangeRateProvider;
import com.budgetapp.exchange.infrastructure.dto.ConversionResult;
import com.budgetapp.shared.exception.ExchangeApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeServiceTest {

    @Mock
    private ExchangeRateProvider rateProvider;

    private ExchangeService service;

    @BeforeEach
    void setUp() {
        service = new ExchangeService(rateProvider, 10);
    }

    @Test
    void should_returnRates_when_upstreamApiResponds() {
        Map<String, BigDecimal> rates = Map.of(
                "EUR", new BigDecimal("0.92"),
                "GBP", new BigDecimal("0.79")
        );
        when(rateProvider.fetchRates(eq("USD"))).thenReturn(rates);

        Map<String, BigDecimal> result = service.getRates("USD");

        assertThat(result).containsEntry("EUR", new BigDecimal("0.92"));
        assertThat(result).containsEntry("GBP", new BigDecimal("0.79"));
    }

    @Test
    void should_returnCachedRate_when_withinTtl() {
        Map<String, BigDecimal> rates = Map.of("EUR", new BigDecimal("0.92"));
        when(rateProvider.fetchRates(eq("USD"))).thenReturn(rates);

        service.getRates("USD");
        service.getRates("USD");

        verify(rateProvider, times(1)).fetchRates(eq("USD"));
    }

    @Test
    void should_convertAmount_betweenCurrencies() {
        Map<String, BigDecimal> rates = Map.of("EUR", new BigDecimal("0.92"));
        when(rateProvider.fetchRates(eq("USD"))).thenReturn(rates);

        ConversionResult result = service.convert(new BigDecimal("100"), "USD", "EUR");

        assertThat(result.from()).isEqualTo("USD");
        assertThat(result.to()).isEqualTo("EUR");
        assertThat(result.rate()).isEqualByComparingTo("0.92");
        assertThat(result.result()).isEqualByComparingTo("92.00");
        assertThat(result.amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void should_returnSameAmount_when_sameCurrencyConversion() {
        ConversionResult result = service.convert(new BigDecimal("100"), "USD", "usd");

        assertThat(result.rate()).isEqualByComparingTo("1");
        assertThat(result.result()).isEqualByComparingTo("100.00");
        verifyNoInteractions(rateProvider);
    }

    @Test
    void should_throwException_when_amountZeroOrNegative() {
        assertThatThrownBy(() -> service.convert(BigDecimal.ZERO, "USD", "EUR"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("greater than 0");

        assertThatThrownBy(() -> service.convert(new BigDecimal("-10"), "USD", "EUR"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("greater than 0");
    }

    @Test
    void should_throwException_when_targetCurrencyNotInRates() {
        Map<String, BigDecimal> rates = Map.of("EUR", new BigDecimal("0.92"));
        when(rateProvider.fetchRates(eq("USD"))).thenReturn(rates);

        assertThatThrownBy(() -> service.convert(new BigDecimal("100"), "USD", "GBP"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported currency");
    }

    @Test
    void should_throwException_when_blankCurrency() {
        assertThatThrownBy(() -> service.getRates(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");

        assertThatThrownBy(() -> service.getRates(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void should_normalizeCurrencyToUppercase() {
        Map<String, BigDecimal> rates = Map.of("EUR", new BigDecimal("0.92"));
        when(rateProvider.fetchRates(eq("USD"))).thenReturn(rates);

        Map<String, BigDecimal> result = service.getRates("usd");

        assertThat(result).containsKey("EUR");
        verify(rateProvider).fetchRates(eq("USD"));
    }

    @Test
    void should_propagateExchangeApiException_fromProvider() {
        when(rateProvider.fetchRates(eq("USD")))
                .thenThrow(new ExchangeApiException("API down"));

        assertThatThrownBy(() -> service.getRates("USD"))
                .isInstanceOf(ExchangeApiException.class)
                .hasMessageContaining("API down");
    }

    @Test
    void should_useCachedRates_whenFetchingWithinTtl() {
        Map<String, BigDecimal> rates = Map.of("EUR", new BigDecimal("0.92"));
        when(rateProvider.fetchRates(eq("USD"))).thenReturn(rates);

        ExchangeService freshService = new ExchangeService(rateProvider, 10);
        freshService.getRates("USD");

        Map<String, BigDecimal> result = freshService.getRates("usd");

        assertThat(result).containsKey("EUR");
        verify(rateProvider, times(1)).fetchRates(eq("USD"));
    }

    @Test
    void should_returnConvertResultWithFetchedAtTimestamp() {
        Map<String, BigDecimal> rates = Map.of("EUR", new BigDecimal("0.92"));
        when(rateProvider.fetchRates(eq("USD"))).thenReturn(rates);

        ConversionResult result = service.convert(new BigDecimal("100"), "USD", "EUR");

        assertThat(result.fetchedAt()).isNotNull();
        assertThat(result.fetchedAt()).isBeforeOrEqualTo(Instant.now());
    }
}
