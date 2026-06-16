# Feature Specification: Frontend Budget Limits — Spending Caps & Status

**Created**: 2026-06-15

## User Scenarios & Testing

### User Story 1 - Set and Manage Spending Limits (Priority: P1)

The user creates weekly, monthly, or annual spending caps on a budget. Each limit shows an amount, a period, and a warning threshold percentage. The user can view all limits for a budget, edit them, or delete them.

**Why this priority**: Limits are the core of this feature. Without the ability to create and manage limits, the status indicators (P2) have nothing to display. The user needs to set caps before they can track against them.

**Independent Test**: Create a $1000 monthly limit on a budget. Verify it appears in the limits list with its period, amount, and threshold. Edit the amount to $1200 and verify the update. Delete the limit and confirm removal. Testable with a single budget — no expenses needed for CRUD.

**Acceptance Scenarios**:

1. **Scenario**: View limits for a budget
   - **Given** a budget has a $1000 monthly limit and a $4000 annual limit
   - **When** the user opens the Limits view for that budget
   - **Then** two limit cards render, each showing the amount (large hand-written number), period (small tag), warning threshold percentage, and creation date. Cards have the sticky-note styling.

2. **Scenario**: Create a spending limit
   - **Given** the user is on the Limits view
   - **When** they click "+ Set Limit" and fill in amount ($1000), period (Monthly), and threshold (80%)
   - **Then** a new sticky-note limit card appears. The period selector is a set of three hand-drawn toggle buttons (Weekly / Monthly / Annual) where the active one is filled in with sage green.

3. **Scenario**: Edit a limit
   - **Given** a $1000 monthly limit exists
   - **When** the user clicks the edit icon and changes the amount to $1200
   - **Then** the limit card updates, and if a status indicator is displayed, it recalculates immediately

4. **Scenario**: Delete a limit
   - **Given** a limit card exists
   - **When** the user clicks delete and confirms
   - **Then** the card fades and is removed

5. **Scenario**: Prevent duplicate limits per period
   - **Given** a Monthly limit already exists for the budget
   - **When** the user tries to create another Monthly limit
   - **Then** a red-ink annotation appears: "A monthly limit already exists for this budget"

6. **Scenario**: Empty limits state
   - **Given** a budget has no limits
   - **When** the user opens the Limits view
   - **Then** an empty notebook page shows: "No spending limits set — add a limit to keep your budget on track" with a "+ Set Limit" CTA

---

### User Story 2 - View Limit Status (Priority: P2)

For each limit, the user sees a hand-drawn progress bar showing how much has been spent in the current period versus the limit amount. The bar changes color and shows a status label: OK (below warning threshold), WARNING (at or above threshold), or EXCEEDED (over 100%).

**Why this priority**: The status view is the actionable part of limits. The user creates limits (P1) specifically to see these statuses. Secondary because limits must exist first.

**Independent Test**: Set a $1000 monthly limit with 80% threshold on a budget with $600 spent this month. Verify the progress bar shows 60% filled in warm brown, and the status is "OK". Add expenses to push spending to $850, verify bar turns darker and status changes to "WARNING". Testable with a limit + expenses.

**Acceptance Scenarios**:

1. **Scenario**: Limit is OK (below warning threshold)
   - **Given** a $1000 monthly limit with 80% warning, and $600 spent this month
   - **When** the status indicator renders
   - **Then** the progress bar shows 60% filled in warm brown (#9D6638). The fill has rough/sketch edges like it was colored in with a pen. Below the bar, a hand-written label reads "OK — $400 remaining" in sage green.

2. **Scenario**: Limit is in WARNING
   - **Given** a $1000 monthly limit with 80% warning, and $850 spent
   - **When** the status renders
   - **Then** the bar shows 85% filled, the fill darkens to deep brown (#4E220F), and the label reads "WARNING — $150 remaining". The word "WARNING" has a subtle hand-drawn underline.

3. **Scenario**: Limit is EXCEEDED
   - **Given** a $1000 monthly limit and $1100 spent
   - **When** the status renders
   - **Then** the bar shows 100% filled (with a slight overfill visual — the pen stroke extends slightly past the bar boundary). The label reads "EXCEEDED — $100 over budget" in deep brown with a double-underline for emphasis.

4. **Scenario**: Limit with zero spending
   - **Given** a $1000 monthly limit and $0 spent this month
   - **When** the status renders
   - **Then** the progress bar is empty (just the faint ruled-line outline), and the label reads "OK — $1000 remaining"

5. **Scenario**: Weekly vs monthly vs annual period display
   - **Given** limits exist for all three periods
   - **When** the status indicators render
   - **Then** each shows the period name and the date range it covers (e.g., "Monthly · Jun 1 – Jun 30, 2026"), rendered as a small parenthetical note under the status label

---

### Edge Cases

- What happens when a limit's warning threshold is 100%? The WARNING state never triggers — the bar transitions directly from OK to EXCEEDED at 100%. The status label only shows "OK" or "EXCEEDED".
- What about limit amount of $0? The backend may reject or accept this. If accepted, the status is immediately EXCEEDED since any spending exceeds 0. The progress bar shows 100% full.
- What happens on period boundaries (end of month/week/year)? The backend calculates period start/end. The frontend just displays what the backend returns — no date math in the frontend.

## Requirements

### Functional Requirements

- **FR-LM-001**: System MUST display all limits for a budget as sticky-note cards showing amount, period, threshold, and creation date
- **FR-LM-002**: Users MUST be able to create a limit with amount, period (Weekly/Monthly/Annual), and warning threshold percentage
- **FR-LM-003**: The period selector MUST render as hand-drawn toggle buttons (three options, active one filled with sage green)
- **FR-LM-004**: Users MUST be able to edit a limit's amount and threshold inline
- **FR-LM-005**: Users MUST be able to delete a limit with confirmation
- **FR-LM-006**: System MUST prevent creating a duplicate limit for the same budget and period, showing a red-ink warning
- **FR-LM-007**: System MUST render a hand-drawn progress bar for each limit showing spent vs limit as a rough pen-fill
- **FR-LM-008**: The progress bar MUST display three states: OK (sage/brown fill, below threshold), WARNING (deep brown fill, at or above threshold), EXCEEDED (deep brown with overfill, above 100%)
- **FR-LM-009**: Status labels MUST include the remaining or over-budget amount
- **FR-LM-010**: Each limit MUST display its period coverage date range (e.g., "Jun 1 – Jun 30, 2026")
- **FR-LM-011**: All views MUST use the foundation notebook theme, shared loading skeletons, and shared empty/error states

### Key Entities (Frontend State)

- **BudgetLimit**: id, budgetId, amount, period (WEEKLY/MONTHLY/ANNUAL), warningThresholdPercent, createdAt
- **LimitStatus**: limitId, budgetId, limitAmount, spentAmount, remainingAmount, percentageUsed, status (OK/WARNING/EXCEEDED), period, periodStart, periodEnd
- **UI State**: activeBudgetId, isEditingLimitId, selectedPeriod

## Success Criteria

### Measurable Outcomes

- **SC-LM-001**: Creating a limit takes under 10 seconds from clicking "Set Limit" to seeing the new card
- **SC-LM-002**: Progress bar percentage values are accurate to 2 decimal places matching the backend calculation
- **SC-LM-003**: The WARNING→EXCEEDED transition is visually distinct enough that a user can tell the difference at a glance
- **SC-LM-004**: All three period types (weekly, monthly, annual) display their correct date ranges without frontend date math errors
