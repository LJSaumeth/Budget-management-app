# Implementation Plan: Exchange Rate

**Date**: 2026-06-15
**Spec**: `DOCS/SPECS/spec-exchange-rate.md`

## Summary

Implement the exchange module — an outbound adapter that fetches rates from the Fawazahmed0 currency API, caches them in memory for 10 hours, and exposes a REST endpoint for currency conversion and rate listing.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot Web, Spring RestClient (built-in, no extra dep)
**Storage**: In-memory `ConcurrentHashMap` cache (no H2 persistence for rates)
**External API**: `https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/{currency}.json`
**Prerequisite**: `plan-budget-core.md` Phase 2 complete (project scaffold, shared exceptions)

## Project Structure

### Source Code (additions to `backend/`)

```text
src/main/java/com/budgetapp/exchange/
├── domain/
│   └── CachedRate.java               # Value object: base, target, rate, fetchedAt
├── application/
│   └── ExchangeService.java          # Core logic: convert, getRates, cache management
└── infrastructure/
    ├── adapter/
    │   └── Fawazahmed0ApiClient.java # Outbound HTTP adapter (port implementation)
    └── rest/
        └── ExchangeController.java   # REST endpoints

src/test/java/com/budgetapp/exchange/
├── application/
│   └── ExchangeServiceTest.java
└── infrastructure/
    └── rest/
        └── ExchangeControllerTest.java
```

---

## Clean Code Guidelines

### Naming & Style
- **Classes/Interfaces**: `PascalCase` — `ExchangeService`, `Fawazahmed0ApiClient`, `ConversionResult`
- **Methods**: `camelCase` — `convert()`, `fetchRates()`, `getCachedOrFetch()`
- **Constants**: `UPPER_SNAKE_CASE` — `CACHE_TTL_HOURS`, `API_TIMEOUT_SECONDS`, `BASE_URL`
- **Packages**: `lowercase` without hyphens — `com.budgetapp.exchange.infrastructure.adapter`
- **DTOs**: Explicit suffix — `ConversionResult` (value object), no `Request`/`Response` needed for GET endpoints

### Single Responsibility
- **Fawazahmed0ApiClient**: Only external HTTP communication — fetch JSON, parse rates map. No caching, no validation.
- **ExchangeService**: Only business logic — cache management, same-currency short-circuit, rate lookup, conversion math.
- **ExchangeController**: Only HTTP — parse query params, delegate, return DTO. No caching logic.
- **CachedRate**: Pure value object — holds rate data, no behavior.
- If a class exceeds ~300 lines, extract (e.g., separate cache logic into `RateCache` class).

### Clean Methods
- Maximum ~30 lines per method; extract cache check/eviction to private methods.
- **Guard clauses first**: `if (from.equalsIgnoreCase(to)) return sameCurrencyResult()` — return early.
- Validate inputs at method entry: amount > 0, currencies not blank.
- The `fetchRates` method should return a clean `Map<String, BigDecimal>` — parse and normalize in one place.
- Separate HTTP concerns (timeout, error handling) from JSON parsing in the adapter.

### SOLID Principles
- **S**: `Fawazahmed0ApiClient` only talks to the API; `ExchangeService` only orchestrates.
- **O**: The API adapter is replaceable — implement an interface `ExchangeRateProvider` for testability.
- **L**: Mock adapter must return the same types as the real adapter.
- **I**: If adding more rate sources later, create `ExchangeRateProvider` interface with `fetchRates(String)` only.
- **D**: `ExchangeService` depends on `ExchangeRateProvider` interface, not `Fawazahmed0ApiClient` directly.

### Spring-Specific
- **Constructor injection** — inject `RestClient.Builder` (or `RestClient`) and configuration into the adapter.
- `@Value("${exchange.cache.ttl-hours:10}")` — cache TTL from config, never hardcoded.
- `@Value("${exchange.api.base-url}")` — API base URL from config.
- `@Service` on `ExchangeService`, `@Component` on `Fawazahmed0ApiClient`.
- `@RestControllerAdvice` already handles `ExchangeApiException` → 503.

### Error Handling
- `ExchangeApiException` extends `RuntimeException` — thrown by adapter on HTTP failures, timeouts, malformed JSON.
- `GlobalExceptionHandler` maps `ExchangeApiException` → 503.
- Return 400 for invalid currencies, 400 for amount ≤ 0 — use `IllegalArgumentException` caught by handler.
- Never expose API error details or stack traces to the client.

### Configurable Values
- `exchange.cache.ttl-hours` (10), `exchange.api.timeout-seconds` (10), `exchange.api.base-url` — all in `application.yml`.
- Group under `exchange:` prefix with `@ConfigurationProperties`.
- NEVER hardcode the Fawazahmed0 URL or TTL in source code.

### Testing
- Tests named `should_returnCachedRate_when_withinTtl()`, `should_fetchFreshRate_when_cacheExpired()`
- AAA: Arrange (mock API response) → Act (call service) → Assert (verify result + mock invocation count).
- `@WebMvcTest(ExchangeController.class)` for REST layer; `@ExtendWith(MockitoExtension.class)` for service.
- Mock `Fawazahmed0ApiClient` in service tests — never call the real API from tests.
- Verify same-currency short-circuit does NOT invoke the API client (`verifyNoInteractions`).

---

## Phase 1: Setup

- [ ] T001 Add `spring-boot-starter-web` dependency (already present from budget-core — verify `RestClient` is available in Spring Boot 3.5+)
- [ ] T002 Create package `com.budgetapp.exchange` with `domain/`, `application/`, `infrastructure/adapter/`, `infrastructure/rest/`

---

## Phase 2: Domain

- [ ] T003 Create `CachedRate.java` — record: `String baseCurrency`, `String targetCurrency`, `BigDecimal rate`, `Instant fetchedAt`. Not a JPA entity — in-memory only.

---

## Phase 3: Outbound Adapter (Fawazahmed0 API)

- [ ] T004 Create `Fawazahmed0ApiClient.java` — `@Component`, uses `RestClient`:
  - `fetchRates(String currency)` → `Map<String, BigDecimal>` — GET `https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/{currency}.json`, parse JSON, extract rates map. Timeout: 10s via `RestClient.Builder.connectTimeout/readTimeout`. Throw custom `ExchangeApiException` on failure (4xx/5xx, timeout, malformed JSON).
  - Map response to `{currency}` key inside the JSON object (API returns `{ "date": "...", "usd": { "eur": 0.92, ... } }`).
- [ ] T005 Create `ExchangeApiException.java` — extends `RuntimeException`, used by the adapter for API failures. GlobalExceptionHandler maps it to 503.

---

## Phase 4: User Story 2 — View Rates (Priority: P2)

Implement rates listing first because the convert endpoint (US1) depends on the caching layer.

**Independent Test**: `GET /api/exchange/rates?base=USD` → 200 with rates map. Mock the API client.

### Application

- [ ] T006 Create `ExchangeService.java` — `@Service`:
  - `getRates(String base)` → `Map<String, BigDecimal>` — check cache, if miss call `Fawazahmed0ApiClient.fetchRates(base)`, store in `ConcurrentHashMap`, return. Cache key: base currency. TTL enforced by comparing `fetchedAt.plus(10, HOURS)` to `Instant.now()`.
  - Maintain `ConcurrentHashMap<String, Map<String, BigDecimal>>` for cache. Each entry stores the rate map with a timestamp. Cleanup stale entries on access (lazy eviction).

### REST

- [ ] T007 Create `ExchangeController.java` — `@RestController @RequestMapping("/api/exchange")`:
  - `GET /rates?base=USD` → 200 with `{ "base": "USD", "rates": {...}, "fetchedAt": "..." }`
  - Validate `base` is not blank → 400
  - Catch `ExchangeApiException` and return 503 via GlobalExceptionHandler

### Tests

- [ ] T008 Create `ExchangeServiceTest.java` — mock `Fawazahmed0ApiClient`, verify cache hit/miss logic, TTL expiry, same-currency short-circuit
- [ ] T009 Create `ExchangeControllerTest.java` — `@WebMvcTest`, mock `ExchangeService`, test valid/invalid base, mock API failure → 503

**Checkpoint**: Rate listing works. `./gradlew test --tests "*ExchangeControllerTest" --tests "*ExchangeServiceTest"`

---

## Phase 5: User Story 1 — Convert Amount (Priority: P1)

**Independent Test**: `GET /api/exchange/convert?amount=100&from=USD&to=EUR` → 200 with converted result.

### Application (add to ExchangeService)

- [ ] T010 Add `convert(BigDecimal amount, String from, String to)` → `ConversionResult`:
  - If `from.equalsIgnoreCase(to)` → return amount as-is with rate=1, skip API call
  - Validate amount > 0 → 400
  - Fetch rates for `from` via `getRates(from)` (uses cache)
  - Look up `to` in rate map → if missing, 400 "Unsupported currency"
  - Calculate: `result = amount × rate`
  - Return DTO: from, to, amount, rate, result, fetchedAt

### DTO

- [ ] T011 Create `ConversionResult.java` — record: `String from`, `String to`, `BigDecimal amount`, `BigDecimal rate`, `BigDecimal result`, `Instant fetchedAt`

### REST (add to ExchangeController)

- [ ] T012 Add `GET /convert?amount=&from=&to=` endpoint
  - Validate params: amount > 0, from and to not blank → 400
  - Delegate to `ExchangeService.convert()`

### Tests

- [ ] T013 Add convert tests to `ExchangeControllerTest.java`: valid conversion, amount ≤ 0 → 400, unsupported currency → 400, same-currency → 200 (no API call)
- [ ] T014 Add convert tests to `ExchangeServiceTest.java`: same-currency bypass, missing rate in map, cache TTL validation

**Checkpoint**: Full exchange module operational. `./gradlew test --tests "*ExchangeControllerTest" --tests "*ExchangeServiceTest"`

---

## Phase 6: Polish

- [ ] T015 Manual integration test: start app, call `/api/exchange/convert?amount=100&from=USD&to=EUR` with real API, verify response
- [ ] T016 Verify same-currency call returns instantly (no HTTP call logged)
- [ ] T017 Verify second call within 10h returns cached result (no HTTP call logged)
- [ ] T018 Kill network, call endpoint → verify 503 not 500

---

## Dependencies & Execution Order

- **Prerequisite**: `plan-budget-core.md` Phase 2 (project scaffold, `GlobalExceptionHandler`)
- **Phase 1-2-3-4-5-6**: Sequential within this plan
- **Executes after**: `feature/budget-core` is merged to master. Exchange is self-contained (no port dependency).

## Notes

- The Fawazahmed0 API URL uses `@latest` tag which auto-redirects to the latest version. RestClient should follow redirects (default behavior).
- The API returns lowercase currency codes (e.g., `"eur"`). Normalize to uppercase in the adapter.
- Cache is purely in-memory and lost on restart — acceptable since rates only update daily and the app fetches fresh on first request.
- No database tables created by this module.
