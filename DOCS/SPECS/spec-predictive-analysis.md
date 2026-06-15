# Feature Specification: Predictive Analysis & Optimization Suggestions

**Created**: 2026-06-15

## User Scenarios & Testing

### User Story 1 - Receive Budget Optimization Suggestions (Priority: P1)

The user requests optimization suggestions for a budget. The system analyzes past spending data and returns actionable recommendations — categories where spending is above average, areas where small reductions would yield significant long-term savings, and the top spending categories by percentage.

**Why this priority**: This is the sole deliverable of this feature. It provides the user with data-driven insights to adjust their spending habits. There are no sub-features.

**Independent Test**: Seed a budget with 3 months of expenses across categories with clear overspending in one category. Send `GET /api/analysis/suggestions?budgetId=1`. Verify the response flags the overspent category and provides concrete reduction amounts with projected savings. Testable with seeded data; depends on the ExpenseQueryPort from budget-core to fetch expense data.

**Acceptance Scenarios**:

1. **Scenario**: Identify a category consistently over the average
   - **Given** budget ID 1 has 3 months of data: Food $600, $620, $610 (avg $610), Transport $200, $180, $190 (avg $190), Entertainment $400, $500, $480 (avg $460)
   - **When** the user sends `GET /api/analysis/suggestions?budgetId=1`
   - **Then** the system returns suggestions including Entertainment flagged as "high variance" with a suggestion to cap at $430 (reducing 6.5% to match the lower months)

2. **Scenario**: Suggest reduction that yields significant long-term savings
   - **Given** the analysis runs over 12 months of data
   - **When** the system identifies a category where a 10% reduction would save $50/month
   - **Then** the suggestion includes `{ "category": "...", "currentMonthlyAvg": 500, "suggestedReductionPercent": 10, "monthlySaving": 50, "annualSaving": 600 }`

3. **Scenario**: Budget has insufficient data for analysis
   - **Given** budget ID 1 has expenses for less than 2 different months
   - **When** the user requests suggestions
   - **Then** the system returns `{ "suggestions": [], "message": "Insufficient data. At least 2 months of expense history are needed for analysis." }`

4. **Scenario**: Budget has zero expenses
   - **Given** budget ID 1 exists but has no expenses
   - **When** the user requests suggestions
   - **Then** the system returns `{ "suggestions": [], "message": "No expense data available for this budget." }`

5. **Scenario**: Budget does not exist
   - **Given** no budget with ID 99
   - **When** the user requests suggestions
   - **Then** the system returns 404

---

### Edge Cases

- What happens when all categories have identical spending averages? Return suggestions with "no significant optimization opportunities found" message.
- What happens when a category has only one expense in the entire history? Skip that category in analysis (insufficient data for trend).
- What happens when the budget's currency is changed after expenses were recorded? The analysis uses the budget's current currency. Historical amounts retain their original numeric values — the system does not attempt to convert old expense amounts to the new currency.
- What happens when there are dozens of categories? Return top N suggestions ordered by potential savings impact, capped at 5 suggestions.
- What happens when the budget has hundreds or thousands of expenses? Analysis runs on the H2 database with aggregation queries; large datasets should still respond within acceptable time.

## Requirements

### Functional Requirements

- **FR-PA-001**: System MUST analyze expenses across all months for the given budget
- **FR-PA-002**: System MUST compute per-category monthly averages and variance
- **FR-PA-003**: System MUST identify categories where the most recent month exceeds the historical average by ≥ 10%
- **FR-PA-004**: System MUST generate suggestions with a suggested reduction percentage (capped at the overspend amount)
- **FR-PA-005**: System MUST calculate monthly and annual savings for each suggestion
- **FR-PA-006**: System MUST require at least 2 distinct months of expense data before producing suggestions
- **FR-PA-007**: System MUST cap output to at most 5 suggestions, ordered by annual savings descending
- **FR-PA-008**: System MUST return a human-readable message when no suggestions can be generated
- **FR-PA-009**: System MUST skip categories with fewer than 2 expenses in the analysis period
- **FR-PA-010**: System MUST NOT modify any data — this is a read-only analysis endpoint

### Key Entities

- **OptimizationSuggestion** (DTO): categoryId (Long), categoryName (String), currentMonthlyAvg (BigDecimal), suggestedReductionPercent (BigDecimal), monthlySaving (BigDecimal), annualSaving (BigDecimal), reasoning (String — human-readable explanation)
- **AnalysisResponse** (DTO): suggestions (List<OptimizationSuggestion>), message (String, present when suggestions list is empty), totalPotentialAnnualSaving (BigDecimal, sum of all suggestion annualSavings)

### Algorithm Design

The analysis follows a simple multi-step algorithm:

1. **Fetch data**: Query all expenses for the budget grouped by category and month, via the ExpenseQueryPort
2. **Filter**: Exclude categories with < 2 expenses total
3. **Calculate**: For each category, compute monthly average across all months
4. **Compare**: For each category, compare the most recent month's spending to the overall monthly average
5. **Flag**: If recent month > (average × 1.10), generate a suggestion
   - suggestedReductionPercent = ((recentMonth - average) / recentMonth) × 100
   - monthlySaving = recentMonth - average
   - annualSaving = monthlySaving × 12
6. **Sort**: Rank by annualSaving descending
7. **Cap**: Return top 5

### Port Dependency

```java
public interface ExpenseQueryPort {
    // Returns expenses grouped by category and month for a budget
    List<CategoryMonthlySpending> getCategoryMonthlySpending(Long budgetId);
    
    record CategoryMonthlySpending(Long categoryId, String categoryName, int year, int month, BigDecimal total) {}
}
```

### API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/analysis/suggestions?budgetId=` | Get optimization suggestions for a budget |

## Success Criteria

### Measurable Outcomes

- **SC-PA-001**: Analysis returns within 500ms for budgets with up to 10,000 expenses (p95).
- **SC-PA-002**: Flagged categories demonstrably exceed their historical average (no false positives from single-month spikes that regress to mean).
- **SC-PA-003**: Suggestions include concrete dollar amounts, not vague advice (e.g., "reduce Food by $73/month" not "spend less on food").
- **SC-PA-004**: When no actionable suggestions exist, the response message clearly explains why.
