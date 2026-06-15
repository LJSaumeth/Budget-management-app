# Implementation Plan: Saving Simulation

**Date**: 2026-06-15
**Spec**: `DOCS/SPECS/spec-saving-simulation.md`

## Summary

Implement a stateless savings projection engine. Accepts monthly income, monthly expenses, optional current savings, optional per-category spending reductions, and a time horizon — returns projected savings. No database reads or writes; pure computation.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot Web (only for REST endpoint)
**Storage**: None — stateless computation, no H2 tables
**Prerequisites**: `plan-budget-core.md` Phase 2 complete (project scaffold, shared exceptions). No port dependency for the basic case.

## Project Structure

### Source Code (additions to `backend/`)

```text
src/main/java/com/budgetapp/simulation/
├── domain/
│   └── CategoryChange.java           # Value object: category, reductionAmount
├── application/
│   └── SimulationService.java        # Pure computation
└── infrastructure/
    ├── rest/
    │   └── SimulationController.java
    └── dto/
        ├── SimulationRequest.java
        └── SimulationResponse.java

src/test/java/com/budgetapp/simulation/
├── application/
│   └── SimulationServiceTest.java
└── infrastructure/
    └── rest/
        └── SimulationControllerTest.java
```

---

## Clean Code Guidelines

### Naming & Style
- **Classes/Interfaces**: `PascalCase` — `SimulationService`, `SimulationController`, `SimulationRequest`
- **Methods**: `camelCase` — `simulate()`, `calculateProjectedSavings()`, `applyCategoryReductions()`
- **Constants**: `UPPER_SNAKE_CASE` — `MIN_MONTHS`, `DECIMAL_SCALE`
- **Packages**: `lowercase` without hyphens — `com.budgetapp.simulation.application`
- **DTOs**: Explicit suffix — `SimulationRequest`, `SimulationResponse`, `CategoryChange` (value object, no suffix)

### Single Responsibility
- **SimulationService**: Only computation — pure function: receives DTO, returns DTO. No DB, no HTTP, no side effects.
- **SimulationController**: Only HTTP — parses `@Valid` request body, delegates to service, returns 200.
- **DTOs**: Pure data carriers with Bean Validation annotations. No behavior.
- This is the simplest module — a single pure function. If logic grows beyond the algorithm, extract helper methods.
- If a class exceeds ~300 lines, extract reduction capping and baseline calculation into separate methods.

### Clean Methods
- Maximum ~30 lines per method; extract the reduction-application loop into `applyReductions()` private method.
- **Guard clauses first**: validate inputs at the DTO level with `@DecimalMin`, `@Min` — service assumes valid input.
- The `simulate` method should be a pipeline: adjust expenses → calculate baseline → calculate adjusted → build response.
- Use `BigDecimal` for all monetary math — never `double` or `float`. Use `BigDecimal.ZERO`, `BigDecimal.valueOf(int)`.
- One level of abstraction: the service method orchestrates steps, each step is a private method.

### SOLID Principles
- **S**: Service is a pure function with one purpose — project savings.
- **O**: New projection types (compound interest, variable income) can be added as new methods or new DTO variants.
- **L**: Not applicable — no inheritance in this module.
- **I**: Not applicable — no interfaces defined; the module is self-contained.
- **D**: Not applicable — no external dependencies beyond Spring Web for the controller.

### Spring-Specific
- **Constructor injection** — though the service has no dependencies, still use constructor for consistency.
- `@Service` on `SimulationService` — stateless, thread-safe, can be a singleton.
- `@Valid` on request body triggers Bean Validation before the service is called.
- No `@Transactional` needed — no database access.
- No `@Value` needed — all inputs come from the request.

### Error Handling
- Validation errors caught by `@Valid` + `GlobalExceptionHandler` → 400. Service never sees invalid input.
- The service does not throw exceptions — it computes even with edge cases (negative savings, zero inputs).
- Reduction caps are applied silently in the service — no error, just math.

### Configurable Values
- No configurable values needed — all inputs are request-driven.
- If a default months value or max months cap is added later, use `@Value` in the controller.

### Testing
- Tests named `should_projectSavingsWithCurrentBalance_when_currentSavingsProvided()`, `should_capReductions_when_exceedTotalExpenses()`
- AAA: Arrange (create request DTO) → Act (call service) → Assert (verify response fields).
- `@WebMvcTest(SimulationController.class)` for REST; mock service in controller tests.
- Service tests are pure unit tests — no mocks needed (no dependencies). Just instantiate and call.
- Verify: positive savings, negative savings (income < expenses), zero months → validation error (controller test).
- Test with `BigDecimal` exact comparisons: `assertThat(result.getProjectedSavings()).isEqualByComparingTo(expected)`.

---

## Phase 1: Setup

- [ ] T001 Create package `com.budgetapp.simulation`

---

## Phase 2: Domain

- [ ] T002 Create `CategoryChange.java` — record: `String category`, `BigDecimal reductionAmount`. `reductionAmount` must be ≥ 0 (validated in service).

---

## Phase 3: DTOs

- [ ] T003 Create `SimulationRequest.java` — record:
  - `@DecimalMin("0") BigDecimal monthlyIncome`
  - `@DecimalMin("0") BigDecimal monthlyExpenses`
  - `@Min(1) int months`
  - `BigDecimal currentSavings` (optional, default 0, can be negative for debt)
  - `List<CategoryChange> expectedChanges` (optional, default empty)
  - Note: `currentSavings` has no `@DecimalMin` constraint because negative (debt) is allowed
- [ ] T004 Create `SimulationResponse.java` — record:
  - `BigDecimal monthlySavings` (income - adjustedExpenses)
  - `BigDecimal projectedSavings` (currentSavings + monthlySavings × months)
  - `int months`
  - `BigDecimal monthlyIncome`
  - `BigDecimal monthlyExpenses` (original, before changes)
  - `BigDecimal adjustedMonthlyExpenses` (after changes applied)
  - `BigDecimal currentSavings`
  - `BigDecimal baselineProjectedSavings` (present only when changes provided)
  - `BigDecimal differenceFromBaseline` (present only when changes provided)

---

## Phase 4: Application Service

- [ ] T005 Create `SimulationService.java` — `@Service`:
  - `simulate(SimulationRequest request)` → `SimulationResponse`

  Algorithm:
  1. Extract: `monthlyIncome`, `monthlyExpenses`, `months`, `currentSavings` (default 0), `expectedChanges` (default empty)
  2. Calculate adjusted expenses:
     - Start with `monthlyExpenses`
     - For each `CategoryChange`:
       - Cap `reductionAmount` at `monthlyExpenses` (can't reduce more than total expenses)
       - Subtract capped amount from adjusted total
     - Ensure adjusted expenses ≥ 0 (floor at 0)
  3. Baseline: `baselineMonthlySavings = monthlyIncome - monthlyExpenses`
  4. Adjusted: `adjustedMonthlySavings = monthlyIncome - adjustedExpenses`
  5. `projectedSavings = currentSavings + (adjustedMonthlySavings × months)`
  6. If changes given:
     - `baselineProjectedSavings = currentSavings + (baselineMonthlySavings × months)`
     - `differenceFromBaseline = projectedSavings - baselineProjectedSavings`
  7. Return response

---

## Phase 5: REST Controller

- [ ] T006 Create `SimulationController.java` — `@RestController @RequestMapping("/api/simulations")`:
  - `POST /savings` → 200 with `SimulationResponse`
  - Validation errors (monthlyIncome < 0, months ≤ 0, etc.) → 400 via `@Valid` + `GlobalExceptionHandler`

---

## Phase 6: Tests

- [ ] T007 Create `SimulationServiceTest.java` — JUnit 5:
  - Basic projection: 5000 income, 3500 expenses, 12 months → 18000 savings
  - With currentSavings: add 2000 → 20000
  - Negative currentSavings (debt): -1000 → subtract from projection
  - With category changes: total reduction 300 → monthlySavings increases by 300
  - Reduction capped at total expenses: try to reduce 5000 when expenses are 3500 → expenses floor at 0
  - Expenses > income → negative monthlySavings (valid)
  - All zeros → zero response
  - Changes without baseline fields → baselineProjectedSavings and difference included
  - Changes empty list → same as no changes (baseline fields absent)
  - months=0 → validation error (caught by controller, not service)
- [ ] T008 Create `SimulationControllerTest.java` — `@WebMvcTest`:
  - Valid request → 200
  - months=0 → 400
  - monthlyIncome=-1 → 400
  - monthlyExpenses=-1 → 400

---

## Dependencies & Execution Order

- **Prerequisite**: `plan-budget-core.md` Phase 2 (shared exceptions + project scaffold)
- **Phase 1 → 2 → 3 → 4 → 5 → 6**: Sequential
- **Executes after**: `feature/budget-limit` is merged to master. This is the most isolated module (no DB, no ports).

## Notes

- Use `BigDecimal` for all monetary values to avoid floating-point errors. Use `BigDecimal.ZERO`, `BigDecimal.valueOf(int)`.
- The `reductionAmount` cap at total expenses is a safety measure. In practice, each individual category reduction should logically be capped at that category's spending, but since we don't query category spending here (stateless), we cap at total expenses.
- `@DecimalMin("0")` uses `inclusive = true` by default, so 0 is accepted.
- This module is the simplest to implement — its main value is correctness of the math and clear validation messages.
