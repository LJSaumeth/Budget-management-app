package com.budgetapp.exchange.domain;

import java.math.BigDecimal;
import java.util.Map;

public interface ExchangeRateProvider {
    Map<String, BigDecimal> fetchRates(String currency);
}
