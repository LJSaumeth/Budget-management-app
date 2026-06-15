# Implementation Plan: Expense History & Summary

**Date**: 2026-06-15
**Spec**: `DOCS/SPECS/spec-expense-history.md`

## Summary

Implement a read-only history module that queries expenses with date range, category, and text-search filters, plus aggregated summaries by category or month. Depends on `ExpenseQueryPort` from the budget-core module.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot Web, Spring Data domain (`Page`, `Pageable`, `Sort`)
**Storage**: Reads from H2 via `ExpenseQueryPort` (no new tables)
**Prerequisite**: `plan-budget-core.md` fully complete (Budget + Expense + Category entities, `ExpenseQueryPort` interface and implementation)

## Project Structure

### Source Code (additions to `backend/`)

```text
src/main/java/com/budgetapp/history/
├── application/
│   └── HistoryService.java
└── infrastructure/
    ├── rest/
    │   └── HistoryController.java
    └── dto/
        ├── ExpenseHistoryPage.java
        ├── ExpenseFilterRequest.java
        ├── SummaryResponse.java
        ├── CategorySummaryItem.java
        └── MonthlySummaryItem.java

src/test/java/com/budgetapp/history/
├── application/
│   └── HistoryServiceTest.java
└── infrastructure/
    └── rest/
        └── HistoryControllerTest.java
```

No `domain/` package — this module owns no entities, only DTOs and query logic.

---

## Clean Code Guidelines

### Naming & Style
- **Classes/Interfaces**: `PascalCase` — `HistoryService`, `HistoryController`, `ExpenseHistoryPage`
- **Methods**: `camelCase` — `getHistory()`, `getSummary()`, `toExpenseResponse()`
- **Constants**: `UPPER_SNAKE_CASE` — `MAX_PAGE_SIZE`, `DEFAULT_PAGE`, `VALID_GROUP_BY`
- **Packages**: `lowercase` without hyphens — `com.budgetapp.history.infrastructure.dto`
- **DTOs**: Explicit suffix — `ExpenseFilterRequest`, `SummaryResponse`, `CategorySummaryItem`, `MonthlySummaryItem`

### Single Responsibility
- **HistoryService**: Only query orchestration — validates filters, calls port, maps domain objects to DTOs. No persistence.
- **HistoryController**: Only HTTP — parses query params with defaults, delegates to service, returns paginated/summary responses.
- **DTOs**: Pure data carriers — no behavior, no JPA annotations, no business logic.
- This module has no `domain/` package — it owns no entities. All data flows through the `ExpenseQueryPort` interface.
- If a class exceeds ~300 lines, extract the DTO mapping logic into a separate `ExpenseMapper` class.

### Clean Methods
- Maximum ~30 lines per method; extract filter validation, DTO mapping, and pagination building to private methods.
- **Guard clauses first**: validate `groupBy` value, check budget existence, cap page size at `MAX_PAGE_SIZE`.
- The `getSummary` method should branch clearly by `groupBy` type — `switch` or if-else, not nested logic.
- Percentage calculation: handle division by zero (return 0% for all categories when total spending is 0).

### SOLID Principles
- **S**: Service handles one use case at a time — history vs summary are separate public methods.
- **O**: New summary types can be added by extending the `groupBy` switch, not modifying existing logic.
- **L**: The port implementation is interchangeable — `HistoryService` depends on the `ExpenseQueryPort` interface.
- **I**: `ExpenseQueryPort` exposes only what this module needs — `findExpenses`, `summarizeByCategory`, `summarizeByMonth`.
- **D**: Depend only on `ExpenseQueryPort` interface — never inject `ExpenseRepository` directly.

### Spring-Specific
- **Constructor injection** — inject `ExpenseQueryPort` and `BudgetRepository` via constructor.
- `@Value("${app.pagination.max-page-size:100}")` for the page size cap.
- `@Transactional(readOnly = true)` on all service methods — this module never writes.
- `@RestControllerAdvice` handles validation errors (400) and budget-not-found (404).

### Error Handling
- Validate `groupBy` ∈ {"category", "month"} at method entry — throw `IllegalArgumentException` for invalid values.
- Budget not found: `ResourceNotFoundException` → 404 via handler.
- `startDate > endDate`: validation in `ExpenseFilterRequest` with `@AssertTrue`.
- Never return raw domain entities through the REST layer — always map to DTOs.

### Configurable Values
- `app.pagination.default-page-size` (20), `app.pagination.max-page-size` (100) in `application.yml`.
- Group under `app.pagination:` prefix with `@ConfigurationProperties`.
- NEVER hardcode page sizes or sort orders in the controller.

### Testing
- Tests named `should_returnPaginatedHistory_when_multipleFiltersApplied()`, `should_returnEmptySummary_when_noExpenses()`
- AAA: Arrange (mock port returns sample data) → Act (call service) → Assert (verify DTO fields, pagination metadata).
- `@WebMvcTest(HistoryController.class)` for REST layer; mock `HistoryService` in controller tests.
- Service tests mock `ExpenseQueryPort` and `BudgetRepository` — verify port method calls, not SQL.
- Verify `totalElements` and `totalPages` match the mocked dataset size.

---

## Phase 1: Setup

- [ ] T001 Create package `com.budgetapp.history` with `application/`, `infrastructure/rest/`, `infrastructure/dto/`
- [ ] T002 Verify `budgetcore.domain.port.ExpenseQueryPort` is available and injectable

---

## Phase 2: DTOs

- [ ] T003 Create `ExpenseFilterRequest.java` — record: `Long budgetId` (from path), `LocalDate startDate`, `LocalDate endDate`, `Long categoryId`, `String search` (all optional except budgetId). Add `@AssertTrue` validation that `startDate` is not after `endDate`.
- [ ] T004 Create `ExpenseHistoryPage.java` — record: `List<ExpenseResponse> content`, `long totalElements`, `int totalPages`, `int page`, `int size`. Reuse `ExpenseResponse` from budget-core.
- [ ] T005 Create `CategorySummaryItem.java` — record: `Long categoryId`, `String categoryName`, `BigDecimal totalAmount`, `long count`, `BigDecimal percentage`
- [ ] T006 Create `MonthlySummaryItem.java` — record: `String month` (format "2026-06"), `BigDecimal totalAmount`, `long count`
- [ ] T007 Create `SummaryResponse.java` — record: `String groupBy`, `List<?> items` (polymorphic: List<CategorySummaryItem> or List<MonthlySummaryItem>)

---

## Phase 3: Application Service

- [ ] T008 Create `HistoryService.java` — `@Service`, injects `ExpenseQueryPort`:
  - `getHistory(Long budgetId, ExpenseFilterRequest filter, int page, int size)` → `ExpenseHistoryPage`
    - Validate budget exists (call port or delegate — port's `findExpenses` with invalid budgetId returns empty Page; add explicit check via `BudgetRepository` injected here or just rely on the port returning empty + doc behavior). Decision: inject `BudgetRepository` and throw 404 if budget not found.
    - Cap size at 100
    - Create `Pageable` with `Sort.by("date").descending()`
    - Map port's `Page<Expense>` to `ExpenseHistoryPage` with `ExpenseResponse` mapping
  - `getSummary(Long budgetId, String groupBy, int year, LocalDate start, LocalDate end)` → `SummaryResponse`
    - Validate budget exists (404)
    - Validate `groupBy` is "category" or "month" (400)
    - If "category": call `port.summarizeByCategory()`, calculate percentages, return `CategorySummaryItem` list
    - If "month": call `port.summarizeByMonth()`, return `MonthlySummaryItem` list. If year not provided, default to current year.

---

## Phase 4: REST Controller

- [ ] T009 Create `HistoryController.java` — `@RestController @RequestMapping("/api/expenses")`:
  - `GET /history?budgetId={id}&startDate=&endDate=&categoryId=&search=&page=0&size=20` → 200 with `ExpenseHistoryPage`
  - `GET /summary?budgetId={id}&groupBy=category|month&year=&startDate=&endDate=` → 200 with `SummaryResponse`
  - Validation errors mapped by `GlobalExceptionHandler` (400)
  - Budget not found → 404

---

## Phase 5: Tests

- [ ] T010 Create `HistoryControllerTest.java` — `@WebMvcTest`:
  - History: paginated result, all filters, empty result, budgetId=99 → 404, size>100 → capped
  - Summary: category grouping, month grouping with year, invalid groupBy → 400, budgetId=99 → 404, startDate>endDate → 400
- [ ] T011 Create `HistoryServiceTest.java` — unit tests with mocked `ExpenseQueryPort` and `BudgetRepository`:
  - Pagination metadata correctness (totalElements, totalPages)
  - Percentage calculation accuracy (sums to 100% within rounding)
  - Filter parameter passthrough

---

## Dependencies & Execution Order

- **Prerequisite**: `plan-budget-core.md` fully complete (Phase 1-6)
- **Phase 1 → 2 → 3 → 4 → 5**: Sequential within this plan
- **Executes after**: `feature/predictive-analysis` is merged to master. This is the final backend module — no further plans depend on it.

## Notes

- `ExpenseResponse` mapping: create a private mapper method in `HistoryService` that converts domain `Expense` to DTO. Do not expose domain entities through the REST layer.
- The `percentage` field in `CategorySummaryItem` should be calculated as `(categoryTotal / overallTotal) * 100`. Handle division by zero (return 0 for all when total is 0).
- This module does NOT inject `ExpenseRepository` directly — it goes through the port to respect module boundaries.
