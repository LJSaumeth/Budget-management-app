# Feature Specification: Saving Simulation

**Created**: 2026-06-15

## User Scenarios & Testing

### User Story 1 - Simulate Savings Based on Current Habits (Priority: P1)

The user requests a savings projection: given their current average monthly income, average monthly spending, and a number of months, the system calculates projected total savings over that period assuming habits stay the same.

**Why this priority**: This is the core simulation. The user sees "if I keep doing what I'm doing, I'll have $X in N months." The what-if variant (P2) is a bonus on top.

**Independent Test**: Send `POST /api/simulations/savings` with `{ "monthlyIncome": 5000, "monthlyExpenses": 3500, "months": 12 }`. Verify the response shows projected savings of $18,000 ($1,500 × 12). No database dependency — this is a stateless computation.

**Acceptance Scenarios**:

1. **Scenario**: Project savings for 12 months with current habits
   - **Given** the user has $5,000 monthly income and $3,500 monthly expenses
   - **When** the user sends `POST /api/simulations/savings` with `{ "monthlyIncome": 5000, "monthlyExpenses": 3500, "months": 12 }`
   - **Then** the system returns 200 with `{ "monthlySavings": 1500, "projectedSavings": 18000, "months": 12, "monthlyIncome": 5000, "monthlyExpenses": 3500 }`

2. **Scenario**: Project savings with current balance included
   - **Given** same as above, plus current savings of $2000
   - **When** the user includes `"currentSavings": 2000` in the request
   - **Then** the system returns `{ ..., "currentSavings": 2000, "projectedSavings": 20000 }`

3. **Scenario**: Zero months requested
   - **When** the user sends `months: 0`
   - **Then** the system returns 400 — "months must be greater than 0"

4. **Scenario**: Negative monthly income
   - **When** the user sends `monthlyIncome: -100`
   - **Then** the system returns 400 — "monthlyIncome must be >= 0"

5. **Scenario**: Monthly expenses exceed monthly income
   - **When** the user sends `monthlyIncome: 3000, monthlyExpenses: 4000, months: 6`
   - **Then** the system still returns 200 with `monthlySavings: -1000, projectedSavings: -6000` — the simulation reflects reality even if negative

---

### User Story 2 - Simulate with Hypothetical Changes (Priority: P2)

The user can include optional monthly spending changes per category to see "what if I cut X by Y amount?" — the simulation shows the difference those changes make compared to keeping current habits.

**Why this priority**: Adds decision-support value. The user can model specific behavior changes (e.g., "spend $200 less on food") and see the projected impact.

**Independent Test**: Send a simulation request with `expectedChanges: [{ "category": "Food", "reductionAmount": 200 }]`. Verify the projection reflects the reduced expense. Testable with no database — stateless.

**Acceptance Scenarios**:

1. **Scenario**: Simulate cutting one category
   - **Given** baseline monthly expenses of $3500, including $800 on Food
   - **When** the user sends `{ "monthlyIncome": 5000, "monthlyExpenses": 3500, "months": 12, "expectedChanges": [{ "category": "Food", "reductionAmount": 200 }] }`
   - **Then** the system returns `{ "monthlySavings": 1700, "projectedSavings": 20400, "baselineProjectedSavings": 18000, "differenceFromBaseline": 2400 }`

2. **Scenario**: Simulate multiple category changes
   - **When** the user sends changes for Food (-$200) and Entertainment (-$100)
   - **Then** the system adjusts monthly expenses down by $300 total

3. **Scenario**: Reduction exceeds current category spending
   - **When** the user sends `{ "category": "Food", "reductionAmount": 1000 }` but Food only costs $800/month
   - **Then** the system caps the reduction at the category's current amount (can't save more than you spend)

4. **Scenario**: Empty expectedChanges array
   - **When** the user sends `expectedChanges: []`
   - **Then** the system treats it the same as current-habits-only (P1 behavior)

---

### Edge Cases

- What happens when months is very large (e.g., 1200 months = 100 years)? The system accepts it. No cap needed for a simple multiplication.
- What happens when all fields are zero? Return 200 with all zeros — valid simulation.
- What happens when monthlyIncome is zero and monthlyExpenses > 0? Returns negative savings projection — reflects real situation.
- What happens when currentSavings is negative (debt)? The system MUST accept negative currentSavings and subtract from the projection.

## Requirements

### Functional Requirements

- **FR-SS-001**: System MUST compute projected savings as `currentSavings + (monthlyIncome - adjustedMonthlyExpenses) × months`
- **FR-SS-002**: System MUST accept optional currentSavings (defaults to 0)
- **FR-SS-003**: System MUST accept optional expectedChanges — a list of per-category reductions
- **FR-SS-004**: System MUST cap individual category reductions at the category's current spending amount
- **FR-SS-005**: System MUST include both baselineProjectedSavings (no changes) and projectedSavings (with changes) in response when expectedChanges are provided
- **FR-SS-006**: System MUST include differenceFromBaseline when expectedChanges are provided
- **FR-SS-007**: System MUST validate month > 0
- **FR-SS-008**: System MUST validate monthlyIncome >= 0
- **FR-SS-009**: System MUST validate monthlyExpenses >= 0
- **FR-SS-010**: System MUST accept negative currentSavings (representing debt)

### Key Entities

This feature introduces no persistent entities. All inputs and outputs are stateless request/response DTOs.

- **SavingsSimulationRequest** (DTO): monthlyIncome (BigDecimal), monthlyExpenses (BigDecimal), months (int), currentSavings (BigDecimal, optional, default 0), expectedChanges (List<CategoryChange>, optional)
- **CategoryChange** (DTO): category (String), reductionAmount (BigDecimal)
- **SavingsSimulationResponse** (DTO): monthlySavings (BigDecimal), projectedSavings (BigDecimal), months (int), monthlyIncome (BigDecimal), monthlyExpenses (BigDecimal), currentSavings (BigDecimal), baselineProjectedSavings (BigDecimal, present only when changes given), differenceFromBaseline (BigDecimal, present only when changes given)

### Port Dependency

```java
public interface ExpenseQueryPort {
    BigDecimal sumExpensesByCategory(Long budgetId, Long categoryId, LocalDate start, LocalDate end);
}
```

Note: This port is only needed for the "auto-fill current spending by category" enhancement. For the initial spec, the user manually provides the monthlyExpenses and per-category changes in the request body.

### API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/simulations/savings` | Run a savings projection |

## Success Criteria

### Measurable Outcomes

- **SC-SS-001**: Simulation returns within 100ms (p95) — no DB queries in the basic case.
- **SC-SS-002**: Projected savings calculation matches manual math within 2 decimal places for all valid inputs.
- **SC-SS-003**: Category reduction caps prevent negative per-category spending.
