package com.budgetapp.exchange.application;

import com.budgetapp.exchange.domain.ExchangeRateProvider;
import com.budgetapp.exchange.infrastructure.dto.ConversionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExchangeService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeService.class);
    private static final int SCALE = 2;

    private final ExchangeRateProvider rateProvider;
    private final int cacheTtlHours;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public ExchangeService(
            ExchangeRateProvider rateProvider,
            @Value("${exchange.cache.ttl-hours:10}") int cacheTtlHours) {
        this.rateProvider = rateProvider;
        this.cacheTtlHours = cacheTtlHours;
    }

    public Map<String, BigDecimal> getRates(String base) {
        String normalized = normalizeCurrency(base);

        CacheEntry cached = cache.get(normalized);
        if (cached != null && !isExpired(cached)) {
            log.debug("Cache hit for base currency: {}", normalized);
            return cached.rates;
        }

        log.debug("Cache miss for base currency: {}, fetching from API", normalized);
        Map<String, BigDecimal> rates = rateProvider.fetchRates(normalized);
        cache.put(normalized, new CacheEntry(rates, Instant.now()));
        return rates;
    }

    public ConversionResult convert(BigDecimal amount, String from, String to) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        String fromNormalized = normalizeCurrency(from);
        String toNormalized = normalizeCurrency(to);

        if (fromNormalized.equals(toNormalized)) {
            return new ConversionResult(
                    fromNormalized, toNormalized,
                    amount.setScale(SCALE, RoundingMode.HALF_UP),
                    BigDecimal.ONE,
                    amount.setScale(SCALE, RoundingMode.HALF_UP),
                    Instant.now()
            );
        }

        Map<String, BigDecimal> rates = getRates(fromNormalized);

        BigDecimal rate = rates.get(toNormalized);
        if (rate == null) {
            throw new IllegalArgumentException("Unsupported currency: " + toNormalized);
        }

        Instant fetchedAt = cache.containsKey(fromNormalized)
                ? cache.get(fromNormalized).fetchedAt
                : Instant.now();

        BigDecimal result = amount.multiply(rate).setScale(SCALE, RoundingMode.HALF_UP);

        return new ConversionResult(
                fromNormalized, toNormalized,
                amount.setScale(SCALE, RoundingMode.HALF_UP),
                rate,
                result,
                fetchedAt
        );
    }

    public Instant getCacheTimestamp(String base) {
        CacheEntry entry = cache.get(normalizeCurrency(base));
        return entry != null ? entry.fetchedAt : null;
    }

    private boolean isExpired(CacheEntry entry) {
        return ChronoUnit.HOURS.between(entry.fetchedAt, Instant.now()) >= cacheTtlHours;
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency must not be blank");
        }
        return currency.trim().toUpperCase();
    }

    private record CacheEntry(Map<String, BigDecimal> rates, Instant fetchedAt) {
    }
}
