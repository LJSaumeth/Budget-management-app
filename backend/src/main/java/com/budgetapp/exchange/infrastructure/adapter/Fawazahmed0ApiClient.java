package com.budgetapp.exchange.infrastructure.adapter;

import com.budgetapp.exchange.domain.ExchangeRateProvider;
import com.budgetapp.shared.exception.ExchangeApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class Fawazahmed0ApiClient implements ExchangeRateProvider {

    private static final Logger log = LoggerFactory.getLogger(Fawazahmed0ApiClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public Fawazahmed0ApiClient(
            @Value("${exchange.api.base-url}") String baseUrl,
            @Value("${exchange.api.timeout-seconds:10}") int timeoutSeconds,
            ObjectMapper objectMapper) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, BigDecimal> fetchRates(String currency) {
        String currencyLower = currency.toLowerCase();
        String path = "/currencies/" + currencyLower + ".json";

        try {
            String json = restClient.get()
                    .uri(path)
                    .retrieve()
                    .body(String.class);

            return parseRates(json, currencyLower);
        } catch (ExchangeApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch exchange rates for {}: {}", currency, e.getMessage());
            throw new ExchangeApiException("Exchange rate service unavailable", e);
        }
    }

    private Map<String, BigDecimal> parseRates(String json, String currency) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode ratesNode = root.get(currency);

            if (ratesNode == null) {
                throw new ExchangeApiException("Unsupported currency: " + currency.toUpperCase());
            }

            Map<String, BigDecimal> rates = new HashMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = ratesNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                rates.put(entry.getKey().toUpperCase(), new BigDecimal(entry.getValue().asText()));
            }

            return rates;
        } catch (ExchangeApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse exchange rate response: {}", e.getMessage());
            throw new ExchangeApiException("Failed to parse exchange rate data", e);
        }
    }
}
