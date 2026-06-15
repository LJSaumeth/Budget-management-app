# Feature Specification: Exchange Rate

**Created**: 2026-06-15

## User Scenarios & Testing

### User Story 1 - Convert Budget Amount Between Currencies (Priority: P1)

The user enters an amount, a source currency, and a target currency, and the system returns the converted amount using the latest exchange rate from the Fawazahmed0 API.

**Why this priority**: This is the core value of the feature — knowing what your budget is worth in another currency. The cached rates endpoint (P2) is a convenience on top.

**Independent Test**: With the backend running and internet access, send `GET /api/exchange/convert?amount=100&from=USD&to=EUR`. The response contains the converted amount. Mock the external API to test offline — the adapter layer makes this independently testable.

**Acceptance Scenarios**:

1. **Scenario**: Convert USD to EUR
   - **Given** the Fawazahmed0 API is reachable and the USD-EUR rate is 0.92
   - **When** the user sends `GET /api/exchange/convert?amount=100&from=USD&to=EUR`
   - **Then** the system returns 200 with `{ "from": "USD", "to": "EUR", "amount": 100, "rate": 0.92, "result": 92.00, "fetchedAt": "..." }`

2. **Scenario**: Convert with invalid currency code
   - **Given** the user sends `GET /api/exchange/convert?amount=100&from=USD&to=XYZ`
   - **When** the external API returns a 404 or the currency code is not found
   - **Then** the system returns 400 with `{ "error": "Unsupported currency: XYZ" }`

3. **Scenario**: Convert with negative amount
   - **Given** the user sends `GET /api/exchange/convert?amount=-100&from=USD&to=EUR`
   - **Then** the system returns 400 with `{ "error": "Amount must be greater than 0" }`

4. **Scenario**: External API is unreachable
   - **Given** the Fawazahmed0 API is down
   - **When** the user requests a conversion
   - **Then** the system returns 503 with `{ "error": "Exchange rate service unavailable" }`

---

### User Story 2 - View Current Exchange Rates for All Supported Currencies (Priority: P2)

The user can fetch all current exchange rates for a given base currency, allowing them to browse rates without converting a specific amount.

**Why this priority**: A convenience view that shows all rates at once. Less critical than the direct conversion; the user gets the same information via individual convert calls.

**Independent Test**: Send `GET /api/exchange/rates?base=USD` and receive a map of currency codes to rates. Testable with a mocked external API response.

**Acceptance Scenarios**:

1. **Scenario**: Get all rates for USD as base
   - **Given** the Fawazahmed0 API is reachable
   - **When** the user sends `GET /api/exchange/rates?base=USD`
   - **Then** the system returns 200 with `{ "base": "USD", "rates": { "EUR": 0.92, "GBP": 0.79, ... }, "fetchedAt": "..." }`

2. **Scenario**: Get all rates for unsupported base currency
   - **When** the user sends `GET /api/exchange/rates?base=ABC`
   - **Then** the system returns 400 with an error message

---

### Edge Cases

- What happens when the user converts between the same currency (USD to USD)? Return the same amount with rate 1.0, do not call the external API.
- What happens when the external API returns a 429 (rate limited)? Return 503 and log the event. The H2 database stores no rate-limiting state.
- What happens when the external API response is malformed or missing fields? Return 502 with a descriptive error.
- What happens when the same rate is requested multiple times in one day? The system SHOULD cache the rate in memory for 10 hours since the Fawazahmed0 API updates rates only once daily. A configurable TTL avoids redundant API calls.

## Requirements

### Functional Requirements

- **FR-ER-001**: System MUST fetch exchange rates from `https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/{currency}.json`
- **FR-ER-002**: System MUST support conversion between any two currencies available in the API response
- **FR-ER-003**: System MUST cache exchange rates in memory for a configurable TTL (default 10 hours) to reduce external API calls
- **FR-ER-004**: System MUST return 400 for invalid or unsupported currency codes
- **FR-ER-005**: System MUST return 503 when the external API is unreachable, timed out, or returns a non-2xx response
- **FR-ER-006**: System MUST return 400 for conversion amounts ≤ 0
- **FR-ER-007**: System MUST short-circuit same-currency conversions (return amount as-is, rate = 1.0) without calling the external API
- **FR-ER-008**: System MUST set a timeout of 10 seconds on external API calls
- **FR-ER-009**: System MUST log all external API call failures for debugging

### Key Entities

- **ExchangeRate** (in-memory cache entry, not persisted to DB): baseCurrency (String), targetCurrency (String), rate (BigDecimal), fetchedAt (Instant), ttlSeconds (int)

### API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/exchange/convert?amount=&from=&to=` | Convert an amount between two currencies |
| GET | `/api/exchange/rates?base=` | Get all exchange rates for a base currency |

### External API Integration

- **Base URL**: `https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/{currency}.json`
- **Response format**: `{ "date": "2026-06-15", "{currency}": { "eur": 0.92, "gbp": 0.79, ... } }`
- **Timeout**: 10 seconds
- **Retry**: No automatic retry; the user must re-request

## Success Criteria

### Measurable Outcomes

- **SC-ER-001**: Conversion requests return within 2 seconds when the external API is healthy (p95 < 1.5s).
- **SC-ER-002**: Cached conversions return within 200ms (p95) without hitting the external API.
- **SC-ER-003**: Same-currency conversions return within 100ms.
- **SC-ER-004**: External API failures produce clear, non-crashing error responses (no 500 due to unhandled IO exceptions).
