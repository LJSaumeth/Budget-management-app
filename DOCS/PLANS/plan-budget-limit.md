# Implementation Plan: Budget Limits & Warnings

**Date**: 2026-06-15
**Spec**: `DOCS/SPECS/spec-budget-limit.md`

## Summary

Implement budget limits — users set WEEKLY, MONTHLY, or ANNUAL spending caps per budget with a configurable warning threshold. A status endpoint computes current period spending and returns OK, WARNING, or EXCEEDED. Depends on `ExpenseQueryPort` for period spending aggregation.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot Web, Spring Data JPA
**Storage**: New H2 table `budget_limit` (auto-created by JPA)
**Prerequisite**: `plan-budget-core.md` fully complete (Budget + Expense entities, `ExpenseQueryPort`)

## Project Structure

### Source Code (additions to `backend/`)

```text
src/main/java/com/budgetapp/limits/
├── domain/
│   └── BudgetLimit.java              # JPA entity + Period enum
├── application/
│   └── LimitService.java
└── infrastructure/
    ├── persistence/
    │   └── BudgetLimitRepository.java
    ├── rest/
    │   └── LimitController.java
    └── dto/
        ├── LimitRequest.java
        ├── LimitResponse.java
        └── LimitStatusResponse.java

src/test/java/com/budgetapp/limits/
├── application/
│   └── LimitServiceTest.java
└── infrastructure/
    └── rest/
        └── LimitControllerTest.java
```

---

## Clean Code Guidelines

### Naming & Style
- **Classes/Interfaces**: `PascalCase` — `LimitService`, `BudgetLimitRepository`, `LimitStatusResponse`
- **Methods**: `camelCase` — `getStatus()`, `calculatePeriodBounds()`, `findByBudgetIdAndPeriod()`
- **Constants**: `UPPER_SNAKE_CASE` — `DEFAULT_WARNING_THRESHOLD`, `PERIOD_WEEKLY`, `PERIOD_MONTHLY`
- **Packages**: `lowercase` without hyphens — `com.budgetapp.limits.domain`, `com.budgetapp.limits.infrastructure.rest`
- **DTOs**: Explicit suffix — `LimitRequest`, `LimitResponse`, `LimitStatusResponse`
- **Enums**: `Period` — `WEEKLY`, `MONTHLY`, `ANNUAL` (singular, not plural)

### Single Responsibility
- **BudgetLimit**: JPA entity — only fields, constraints (`@DecimalMin`, `@NotNull`), timestamps. No period math.
- **LimitService**: Business logic — CRUD orchestration, duplicate detection, period boundary calculation, status computation.
- **LimitController**: Only HTTP — parses requests, delegates, returns DTOs. No period math.
- **BudgetLimitRepository**: Only data access — find by budget+period, list by budget. No validation.
- If a class exceeds ~300 lines, extract the period calculation logic into a `PeriodCalculator` utility class.

### Clean Methods
- Maximum ~30 lines per method; extract period start/end calculation into `calculatePeriodStart(LocalDate, Period)` and `calculatePeriodEnd`.
- **Guard clauses first**: check budget exists on create, check duplicate period on create, check limit exists on update/delete.
- The `getStatus` method should be a clean pipeline: find limit → calculate period → query spent → compute percentages → determine status.
- Use `java.time.temporal.TemporalAdjusters` for period math — `previousOrSame(DayOfWeek.MONDAY)`, `lastDayOfMonth()`.

### SOLID Principles
- **S**: Period calculation is delegated to `PeriodCalculator` — `LimitService` focuses on orchestration.
- **O**: New period types can be added by extending `PeriodCalculator` without touching `LimitService`.
- **L**: The port implementation (`ExpenseQueryPort.sumExpensesByPeriod`) must accurately filter by date range.
- **I**: `ExpenseQueryPort` exposes `sumExpensesByPeriod` — no need for full expense objects for status checks.
- **D**: `LimitService` depends on `ExpenseQueryPort` interface, not `ExpenseQueryPortImpl`.

### Spring-Specific
- **Constructor injection** — inject `BudgetLimitRepository`, `ExpenseQueryPort`, `BudgetRepository`.
- `@Value("${limits.default-warning-threshold:80}")` — default threshold from config.
- `@Transactional` on create/update/delete; `@Transactional(readOnly = true)` on getById/getStatus.
- Unique constraint: `@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"budgetId", "period"}))` on entity.
- `DataIntegrityViolationException` from duplicate insert → mapped to 409 by `GlobalExceptionHandler`.

### Error Handling
- Throw `ResourceNotFoundException` for non-existent limit or budget.
- Throw `ConflictException` for duplicate budget+period combination.
- `IllegalArgumentException` for threshold outside 1-100, amount ≤ 0.
- Never expose H2 constraint violation messages to the client — catch and translate.

### Configurable Values
- `limits.default-warning-threshold` (80) in `application.yml`.
- WEEKLY period start day (MONDAY) — could be configurable via `limits.week-start-day`.
- NEVER hardcode period calculation rules — use `TemporalAdjusters` from `java.time`.

### Testing
- Tests named `should_returnWarningStatus_when_spendingExceedsThreshold()`, `should_returnExceededStatus_when_overLimit()`
- AAA: Arrange (seed limit + mock spent amount) → Act (call getStatus) → Assert (verify status, percentage).
- `@WebMvcTest(LimitController.class)` for REST; mock `LimitService` in controller tests.
- Service tests mock `ExpenseQueryPort.sumExpensesByPeriod` and `BudgetLimitRepository`.
- Test period boundary edge cases: month transitions, Dec 31 → Jan 1, leap years.

---

## Phase 1: Setup

- [ ] T001 Create package `com.budgetapp.limits` with `domain/`, `application/`, `infrastructure/` sub-packages
- [ ] T002 Verify `budgetcore.domain.port.ExpenseQueryPort` with `sumExpensesByPeriod` method is available

---

## Phase 2: Domain

- [ ] T003 Create `Period` enum — `WEEKLY`, `MONTHLY`, `ANNUAL`
- [ ] T004 Create `BudgetLimit.java` — JPA entity:
  - `id` (Long, `@Id @GeneratedValue`)
  - `budgetId` (Long, `@Column(nullable = false)`)
  - `amount` (BigDecimal, `@DecimalMin("0.01")`)
  - `period` (`@Enumerated(STRING)`, `@NotNull`)
  - `warningThresholdPercent` (int, default 80, `@Min(1) @Max(100)`)
  - `createdAt`, `updatedAt` (LocalDateTime, `@PrePersist/@PreUpdate`)
  - Unique constraint: `@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"budgetId", "period"}))`

---

## Phase 3: Persistence

- [ ] T005 Create `BudgetLimitRepository.java` — extends `JpaRepository<BudgetLimit, Long>`:
  - `Optional<BudgetLimit> findByBudgetIdAndPeriod(Long budgetId, Period period)` — for duplicate check
  - `List<BudgetLimit> findByBudgetId(Long budgetId)` — list all limits for a budget
  - `void deleteByBudgetId(Long budgetId)` — cascade cleanup (alternative: JPA handles via BudgetService.delete)

---

## Phase 4: DTOs

- [ ] T006 Create `LimitRequest.java` — record: `@NotNull Long budgetId`, `@DecimalMin("0.01") BigDecimal amount`, `@NotNull Period period`, `@Min(1) @Max(100) int warningThresholdPercent` (default 80)
- [ ] T007 Create `LimitResponse.java` — record: id, budgetId, amount, period, warningThresholdPercent, createdAt
- [ ] T008 Create `LimitStatusResponse.java` — record: limitId, budgetId, limitAmount, spent, remaining, percentageUsed, status (OK/WARNING/EXCEEDED), period, periodStart (LocalDate), periodEnd (LocalDate)

---

## Phase 5: Application Service

- [ ] T009 Create `LimitService.java` — `@Service`, injects `BudgetLimitRepository`, `ExpenseQueryPort`, `BudgetRepository`:
  - `create(LimitRequest)` → `LimitResponse`
    - Validate budget exists (404)
    - Check no duplicate budgetId+period (409)
    - Save and return
  - `getById(Long)` → `LimitResponse` — find or 404
  - `getByBudget(Long budgetId)` → `List<LimitResponse>`
  - `update(Long id, LimitRequest)` → `LimitResponse` — find, update fields, save
  - `delete(Long id)` → void — find or 404, delete
  - `getStatus(Long limitId)` → `LimitStatusResponse`
    - Find limit or 404
    - Calculate current period bounds based on `Period` and `LocalDate.now()`:
      - WEEKLY: Monday 00:00 to Sunday 23:59
      - MONTHLY: 1st to last day of current month
      - ANNUAL: Jan 1 to Dec 31 of current year
    - Call `ExpenseQueryPort.sumExpensesByPeriod(budgetId, start, end)` → BigDecimal spent
    - Compute: percentageUsed = (spent / limitAmount) × 100, remaining = limitAmount - spent
    - Determine status: OK (< threshold%), WARNING (threshold% ≤ pct < 100), EXCEEDED (≥ 100%)
    - Return full `LimitStatusResponse`

---

## Phase 6: REST Controller

- [ ] T010 Create `LimitController.java` — `@RestController @RequestMapping("/api/limits")`:
  - `POST /` → 201
  - `GET /{id}` → 200
  - `GET /?budgetId=` → 200
  - `PUT /{id}` → 200
  - `DELETE /{id}` → 204
  - `GET /{id}/status` → 200
  - Duplicate period → 409
  - Budget not found → 404
  - Validation errors → 400

---

## Phase 7: Tests

- [ ] T011 Create `LimitControllerTest.java` — `@WebMvcTest`:
  - CRUD success cases
  - Duplicate period → 409
  - Non-existent budget → 404
  - Status: below threshold (OK), above threshold (WARNING), above limit (EXCEEDED)
  - Status with no expenses → 0% spent
  - Invalid threshold (0, 101) → 400
- [ ] T012 Create `LimitServiceTest.java` — unit tests:
  - Period boundary calculation correctness (week, month, year edge cases)
  - Percentage rounding
  - Status transitions at exact threshold boundaries

---

## Dependencies & Execution Order

- **Prerequisite**: `plan-budget-core.md` fully complete
- **Phase 1 → 2 → 3 → 4 → 5 → 6 → 7**: Sequential within this plan
- **Executes after**: `feature/exchange-rate` is merged to master.

## Notes

- Period boundary calculation uses `java.time.DayOfWeek.MONDAY` and `TemporalAdjusters`. Test with edge dates (Dec 31 → Jan 1, month transitions, leap years).
- The `ExpenseQueryPort.sumExpensesByPeriod` method must filter expenses by `date BETWEEN start AND end` — ensure this is implemented correctly in the port implementation (budget-core).
- `remaining` can be negative when spent exceeds limit — this is intentional and displayed as-is.
