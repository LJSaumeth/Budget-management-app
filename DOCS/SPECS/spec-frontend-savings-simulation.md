# Feature Specification: Frontend Savings Simulation — Project Your Future Balance

**Created**: 2026-06-15

## User Scenarios & Testing

### User Story 1 - Simulate Savings Projection (Priority: P1)

The user enters their monthly income, monthly expenses, number of months, current savings (optional), and optional per-category spending reductions. The system calculates and displays: projected savings, monthly savings amount, adjusted expenses, and — when reductions are provided — a baseline projection without changes plus the difference.

**Why this priority**: This is the entire feature — a single form-to-result flow. There are no secondary views or management UIs. The simulation is useful as a standalone planning tool regardless of whether the user has budgets or expenses in the system.

**Independent Test**: Enter income $5000, expenses $3500, 12 months. Verify projected savings = $18,000. Add current savings of $2000, verify $20,000. Add a $300 Food reduction, verify $21,600 and baseline/difference fields appear. Testable without any existing budgets or expenses — pure form input.

**Acceptance Scenarios**:

1. **Scenario**: Basic projection (no reductions, no current savings)
   - **Given** the user opens the Simulation view
   - **When** they enter monthly income $5000, monthly expenses $3500, months 12, and click "Project"
   - **Then** a results ledger appears, styled as a hand-written financial note: "Monthly Savings: $1,500.00", "Projected Savings (12 months): $18,000.00". The income, expenses, and adjusted expenses are listed below in smaller text. No baseline fields appear since no reductions were provided.

2. **Scenario**: Projection with current savings
   - **Given** the user enters current savings of $2000 alongside income/expenses
   - **When** they click Project
   - **Then** the results show projected savings of $20,000.00, and the current savings amount is noted in the results ledger.

3. **Scenario**: Projection with spending reductions
   - **Given** the user adds a Food reduction of $300/month
   - **When** they click Project
   - **Then** the results show adjusted expenses ($3,200) vs original ($3,500), and two extra lines appear: "Without Changes: $18,000.00" and "You'd Save an Extra: $3,600.00". The extra savings amount is highlighted in sage green with a subtle hand-drawn circle around it.

4. **Scenario**: Projection with negative savings (debt)
   - **Given** the user enters current savings of -$1000 (debt)
   - **When** they project
   - **Then** the projected savings correctly subtracts the debt ($17,000). The current savings line renders in deep brown (#4E220F) with a hand-drawn caution underline.

5. **Scenario**: Expenses exceed income (negative monthly savings)
   - **Given** income $3000, expenses $5000
   - **When** the user projects
   - **Then** the monthly savings shows "-$2,000.00" and the projected total is negative. The amounts render in deep brown with the caution underline.

6. **Scenario**: Reduction exceeds total expenses (capped)
   - **Given** expenses are $3500 and the user enters a $5000 Food reduction
   - **When** they project
   - **Then** adjusted expenses floor at $0.00, monthly savings equals the full income. No error — the math silently caps. A small note appears: "Reductions capped at total expenses."

7. **Scenario**: Add and remove category reduction rows
   - **Given** the user is filling in expected changes
   - **When** they click "+ Add Category" (stylized as a small hand-drawn plus sign)
   - **Then** a new row appears with a category name input and reduction amount input. An "×" button on each row removes it with a cross-out animation.

---

### Edge Cases

- What happens when the user enters months = 0? The backend rejects it with a 400 validation error. The frontend shows a red-ink annotation: "Months must be at least 1" before the user can submit (client-side validation catches it first).
- What about extremely large values (e.g., $999,999,999)? The math still works. The result displays with thousand separators in the hand-written style for readability.
- What about fractional cents? All monetary inputs accept up to 2 decimal places. Results display with exactly 2 decimal places.
- What about very long simulation periods (120 months = 10 years)? The math works correctly. The results display without special formatting for large time spans.
- What happens when no category name is entered in a reduction row? The backend still processes it (the name is just a label). The frontend should require a non-empty name before allowing submission.

## Requirements

### Functional Requirements

- **FR-SM-001**: System MUST render a simulation form with ruled-line inputs for monthly income, monthly expenses, months, and current savings
- **FR-SM-002**: Users MUST be able to add and remove per-category spending reduction rows dynamically
- **FR-SM-003**: Each reduction row MUST have a category name input and a reduction amount input
- **FR-SM-004**: The form MUST validate that monthly income, monthly expenses, and reduction amounts are non-negative, and months is at least 1, with red-ink annotations on failure
- **FR-SM-005**: System MUST display simulation results in a hand-written financial note ledger
- **FR-SM-006**: Results MUST show monthly savings, projected savings, months, original expenses, adjusted expenses, and current savings
- **FR-SM-007**: When spending reductions are provided, results MUST also show baseline projected savings (without changes) and the difference from baseline, with the extra savings highlighted in sage green
- **FR-SM-008**: Negative values (debt, negative monthly savings) MUST render in deep brown with a hand-drawn caution underline
- **FR-SM-009**: A small note MUST appear when reductions are capped at total expenses
- **FR-SM-010**: The projection button MUST trigger a loading skeleton on the result area while the backend computes
- **FR-SM-011**: Modifying any form input MUST clear the previous result (subtle fade animation) until the user clicks Project again
- **FR-SM-012**: All views MUST use the foundation notebook theme, shared form inputs, and shared error states

### Key Entities (Frontend State)

- **SimulationInput**: monthlyIncome, monthlyExpenses, months, currentSavings (nullable), categoryChanges (list of {category, reductionAmount})
- **SimulationResult**: monthlySavings, projectedSavings, months, monthlyIncome, monthlyExpenses, adjustedMonthlyExpenses, currentSavings, baselineProjectedSavings (nullable), differenceFromBaseline (nullable)
- **UI State**: isProjecting, lastResult, categoryChangeRows

## Success Criteria

### Measurable Outcomes

- **SC-SM-001**: A basic projection (income + expenses + months only) returns results within 500ms of clicking Project
- **SC-SM-002**: Adding or removing 10 category reduction rows performs smoothly without input lag or layout reflow
- **SC-SM-003**: The baseline vs adjusted comparison is visually clear enough that a user can understand the impact of their reductions at a glance
- **SC-SM-004**: The form prevents submission with invalid data (negative amounts, zero months) before any backend call is made
