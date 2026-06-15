# Implementation Plan: Predictive Analysis & Optimization Suggestions

**Date**: 2026-06-15
**Spec**: `DOCS/SPECS/spec-predictive-analysis.md`

## Summary

Implement a read-only analysis engine that examines spending patterns across months, identifies categories where recent spending exceeds historical averages by ≥ 10%, and generates concrete optimization suggestions with dollar amounts. Depends on `ExpenseQueryPort` for aggregated spending data.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot Web
**Storage**: No new tables — reads from H2 via `ExpenseQueryPort`
**Prerequisite**: `plan-budget-core.md` fully complete (with `getCategoryMonthlySpending` on port)

## Project Structure

### Source Code (additions to `backend/`)

```text
src/main/java/com/budgetapp/analysis/
├── application/
│   └── AnalysisService.java
└── infrastructure/
    ├── rest/
    │   └── AnalysisController.java
    └── dto/
        ├── OptimizationSuggestion.java
        └── AnalysisResponse.java

src/test/java/com/budgetapp/analysis/
├── application/
│   └── AnalysisServiceTest.java
└── infrastructure/
    └── rest/
        └── AnalysisControllerTest.java
```

---

## Clean Code Guidelines

### Naming & Style
- **Classes/Interfaces**: `PascalCase` — `AnalysisService`, `AnalysisController`, `OptimizationSuggestion`
- **Methods**: `camelCase` — `analyze()`, `flagOverspending()`, `calculateMonthlyAverage()`, `hasSufficientData()`
- **Constants**: `UPPER_SNAKE_CASE` — `MIN_MONTHS_FOR_ANALYSIS`, `OVERSPEND_THRESHOLD_PERCENT`, `MAX_SUGGESTIONS`
- **Packages**: `lowercase` without hyphens — `com.budgetapp.analysis.application`
- **DTOs**: Explicit suffix — `AnalysisResponse`, `OptimizationSuggestion` (value object, no suffix needed)

### Single Responsibility
- **AnalysisService**: Only the analysis algorithm — fetches data via port, runs the 7-step pipeline, builds response.
- **AnalysisController**: Only HTTP — parses query param, delegates, returns analysis response.
- **DTOs**: Pure data carriers — `OptimizationSuggestion` has no behavior, `AnalysisResponse` aggregates results.
- This module owns no entities — it's a read-only analysis engine.
- If a class exceeds ~300 lines, extract each pipeline step into a named private method.

### Clean Methods
- Maximum ~30 lines per method. The `analyze()` method orchestrates 7 steps; each step is a private method:
  - `validateBudget()`, `fetchSpendingData()`, `checkDataSufficiency()`, `groupByCategory()`, `flagOverspending()`, `sortAndCap()`, `buildResponse()`
- **Guard clauses first**: budget exists (404), sufficient data (message), no expenses (message) — return early.
- Use `BigDecimal.compareTo()` for comparisons: `recentMonthTotal.compareTo(average.multiply(THRESHOLD)) > 0`.
- Percentage calculations: `recentMonthTotal.subtract(average).divide(recentMonthTotal, DECIMAL_SCALE, RoundingMode.HALF_UP)`.

### SOLID Principles
- **S**: The 7-step pipeline ensures each method has a single responsibility.
- **O**: The algorithm is extensible — new analysis types can be added as new `analyze*()` methods.
- **L**: The `ExpenseQueryPort` implementation is swappable without changing `AnalysisService`.
- **I**: `ExpenseQueryPort.getCategoryMonthlySpending()` exposes exactly what analysis needs — no more, no less.
- **D**: Depend only on `ExpenseQueryPort` and `BudgetRepository` interfaces — never inject repositories from other modules.

### Spring-Specific
- **Constructor injection** — inject `ExpenseQueryPort` and `BudgetRepository`.
- `@Value("${analysis.min-months:2}")` — minimum months threshold from config.
- `@Value("${analysis.overspend-threshold-percent:10}")` — overspend threshold (default 10%).
- `@Value("${analysis.max-suggestions:5}")` — max suggestions to return.
- `@Transactional(readOnly = true)` on the `analyze` method.

### Error Handling
- Budget not found → `ResourceNotFoundException` → 404.
- Insufficient data → return empty suggestions with descriptive message (not an error).
- No expenses → return empty suggestions with "No expense data available" message.
- Port query failures propagate as runtime exceptions → 500 via handler (shouldn't happen with H2).

### Configurable Values
- `analysis.min-months` (2), `analysis.overspend-threshold-percent` (10), `analysis.max-suggestions` (5) in `application.yml`.
- Group under `analysis:` prefix with `@ConfigurationProperties`.
- NEVER hardcode the 10% threshold or the 2-month minimum in the algorithm.

### Testing
- Tests named `should_flagOverspending_when_recentMonthExceedsAverageBy10Percent()`, `should_returnEmptySuggestions_when_insufficientData()`
- AAA: Arrange (mock port returns sample `CategoryMonthlySpending` data) → Act (call analyze) → Assert (verify flagged categories, amounts, message).
- `@WebMvcTest(AnalysisController.class)` for REST; mock `AnalysisService` in controller tests.
- Service tests mock `ExpenseQueryPort` and `BudgetRepository` — no real database.
- Test exact threshold boundaries: 10.0% above → NOT flagged (strictly greater), 10.01% → flagged.
- Verify sort order: highest annualSaving first. Verify cap: only 5 suggestions returned when 7 categories are flagged.
- Test edge: category with 0 in some months (average drops), category with single month data (skipped).

---

## Phase 1: Setup

- [ ] T001 Create package `com.budgetapp.analysis` with sub-packages
- [ ] T002 Verify `ExpenseQueryPort.getCategoryMonthlySpending(Long budgetId)` is available

---

## Phase 2: DTOs

- [ ] T003 Create `OptimizationSuggestion.java` — record:
  - `Long categoryId`
  - `String categoryName`
  - `BigDecimal currentMonthlyAvg`
  - `BigDecimal suggestedReductionPercent`
  - `BigDecimal monthlySaving`
  - `BigDecimal annualSaving`
  - `String reasoning` (e.g., "Food spending ($620) is 24% above your 3-month average ($500). Reducing to $500 would save $120/month.")
- [ ] T004 Create `AnalysisResponse.java` — record:
  - `List<OptimizationSuggestion> suggestions`
  - `String message` (present when suggestions empty, e.g., "Insufficient data...")
  - `BigDecimal totalPotentialAnnualSaving` (sum of all suggestion annualSavings)

---

## Phase 3: Application Service

- [ ] T005 Create `AnalysisService.java` — `@Service`, injects `ExpenseQueryPort`, `BudgetRepository`:

  `analyze(Long budgetId)` → `AnalysisResponse`

  **Algorithm** (7 steps):

  1. **Validate budget exists** — `BudgetRepository.findById()` → 404 if not found

  2. **Fetch data** — `ExpenseQueryPort.getCategoryMonthlySpending(budgetId)` returns `List<CategoryMonthlySpending>` (categoryId, categoryName, year, month, total)

  3. **Check sufficiency** — Count distinct (year, month) pairs. If < 2 distinct months → return empty with "Insufficient data. At least 2 months of expense history are needed for analysis."

  4. **Group and filter** — Group by `categoryId`. For each category:
     - Skip if < 2 total expenses (or use a distinct check on the count from the port)
     - Collect monthly totals into a list, sorted by (year, month) descending

  5. **Calculate averages** — For each category:
     - `overallMonthlyAvg` = sum of all monthly totals / number of months
     - `recentMonthTotal` = total for the most recent (year, month)

  6. **Flag overspending** — If `recentMonthTotal > overallMonthlyAvg × 1.10`:
     - `suggestedReductionPercent = ((recentMonthTotal - overallMonthlyAvg) / recentMonthTotal) × 100`
     - `monthlySaving = recentMonthTotal - overallMonthlyAvg`
     - `annualSaving = monthlySaving × 12`
     - `reasoning = "[categoryName] spending ([$recentMonthTotal]) is [percent]% above your [months]-month average ([$average]). Reducing to [$average] would save [$monthlySaving]/month."`
     - Add to suggestions list

  7. **Sort and cap** — Sort suggestions by `annualSaving` descending. Limit to top 5.

  8. **Return** — `AnalysisResponse` with suggestions list. If list is empty, message = "No significant optimization opportunities found. All categories are within 10% of their historical average."

  9. **Compute** `totalPotentialAnnualSaving` = sum of all `annualSaving` values.

---

## Phase 4: REST Controller

- [ ] T006 Create `AnalysisController.java` — `@RestController @RequestMapping("/api/analysis")`:
  - `GET /suggestions?budgetId={id}` → 200 with `AnalysisResponse`
  - Budget not found → 404
  - Missing/invalid budgetId → 400

---

## Phase 5: Tests

- [ ] T007 Create `AnalysisServiceTest.java`:
  - **Sufficient data, one overspent category**: 3 months, Food: [500, 520, 620] — Food flagged (620 > avg × 1.10)
  - **All categories within 10%**: No suggestions, message returned
  - **Less than 2 months of data**: "Insufficient data" message
  - **Category with < 2 expenses**: Skipped in analysis
  - **Exact 10% threshold**: recent = avg × 1.10 exactly — NOT flagged (strictly greater)
  - **Multiple flagged categories**: Verify sort by annualSaving descending, capped at 5
  - **Total potential annual saving sum**: matches manual sum
  - **Empty budget (no expenses at all)**: "No expense data available" message
- [ ] T008 Create `AnalysisControllerTest.java` — `@WebMvcTest`:
  - Valid budgetId → 200 with suggestions
  - Non-existent budgetId → 404
  - Missing budgetId param → 400

---

## Dependencies & Execution Order

- **Prerequisite**: `plan-budget-core.md` fully complete (with `getCategoryMonthlySpending` port method working)
- **Phase 1 → 2 → 3 → 4 → 5**: Sequential
- **Executes after**: `feature/saving-simulation` is merged to master.

## Notes

- The `CategoryMonthlySpending` record is defined in `budgetcore/domain/port/ExpenseQueryPort`. It must contain `categoryId`, `categoryName`, `year`, `month`, `total`.
- Use `BigDecimal` comparisons with `compareTo()`: `recentMonthTotal.compareTo(overallMonthlyAvg.multiply(new BigDecimal("1.10"))) > 0`
- The `reasoning` string is generated client-side-friendly — formatted currency values and clear recommendations.
- Percentage calculation uses `RoundingMode.HALF_UP` with scale 1 for `suggestedReductionPercent` and scale 2 for monetary values.
- The port query for `getCategoryMonthlySpending` should return one row per (category, month) with the summed total for that month. Ensure the `ExpenseQueryPortImpl` in budget-core implements this with a proper JPA query grouping by category and month.
- If a category has months with no spending (0), include those months to keep the average accurate. If a category only has expenses in 1 out of 3 months, the average drops accordingly — this is the correct behavior.
