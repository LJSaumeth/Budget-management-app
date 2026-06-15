# Feature Specification: Budget Limits & Warnings

**Created**: 2026-06-15

## User Scenarios & Testing

### User Story 1 - Set and Manage Spending Limits (Priority: P1)

The user defines a spending limit for a budget, choosing a period (weekly, monthly, or annual) and a cap amount. The user can create, view, update, and delete limits.

**Why this priority**: Setting a limit is the core action. Without a defined limit, there is nothing to warn against. The warning status (P2) is derived from this.

**Independent Test**: Create a limit for a budget via `POST /api/limits`, retrieve it, update the amount, delete it. All CRUD operations are testable with just a budget present — no expenses needed to verify limit creation.

**Acceptance Scenarios**:

1. **Scenario**: Set a monthly spending limit
   - **Given** budget ID 1 exists with no limits set
   - **When** the user sends `POST /api/limits` with `{ "budgetId": 1, "amount": 2000.00, "period": "MONTHLY", "warningThresholdPercent": 80 }`
   - **Then** the system returns 201 with the created limit

2. **Scenario**: Set multiple limits for the same budget (different periods)
   - **Given** budget ID 1 already has a MONTHLY limit
   - **When** the user creates a WEEKLY limit for the same budget
   - **Then** the system returns 201 — multiple limits per budget are allowed when periods differ

3. **Scenario**: Set duplicate period for the same budget
   - **Given** budget ID 1 already has a MONTHLY limit
   - **When** the user tries to create another MONTHLY limit for budget ID 1
   - **Then** the system returns 409 Conflict

4. **Scenario**: Update a limit amount
   - **Given** a MONTHLY limit of $2000 exists
   - **When** the user sends `PUT /api/limits/{id}` with `{ "amount": 2500.00 }`
   - **Then** the system returns 200 with the updated limit

5. **Scenario**: Delete a limit
   - **Given** a limit exists
   - **When** the user sends `DELETE /api/limits/{id}`
   - **Then** the system returns 204

---

### User Story 2 - Check Limit Status and Receive Warnings (Priority: P2)

The user queries the status of a budget limit: how much has been spent in the current period, the percentage used, and whether a warning should be triggered (spending exceeds the warning threshold).

**Why this priority**: This is the value delivery of the feature — telling the user they're approaching or exceeding their self-imposed limit. Depends on limits being set and expenses existing.

**Independent Test**: Seed a budget with a limit of $1000 (warning at 80%) and $850 in expenses for the current period. Query `GET /api/limits/{id}/status`. Verify the response shows "warning" status, 85% used. Testable with seeded data.

**Acceptance Scenarios**:

1. **Scenario**: Spending is below warning threshold
   - **Given** a MONTHLY limit of $1000 with 80% warning threshold, and $600 spent this month
   - **When** the user sends `GET /api/limits/1/status`
   - **Then** the system returns `{ "limitId": 1, "limitAmount": 1000, "spent": 600, "remaining": 400, "percentageUsed": 60, "status": "OK" }`

2. **Scenario**: Spending exceeds warning threshold but is under limit
   - **Given** a MONTHLY limit of $1000 with 80% warning threshold, and $850 spent this month
   - **When** the user queries the status
   - **Then** the system returns status "WARNING" and percentageUsed = 85

3. **Scenario**: Spending exceeds the limit
   - **Given** a MONTHLY limit of $1000 and $1200 spent this month
   - **When** the user queries the status
   - **Then** the system returns status "EXCEEDED" and percentageUsed = 120

4. **Scenario**: Limit exists but budget has no expenses in the current period
   - **Given** a WEEKLY limit of $500 and no expenses this week
   - **When** the user queries the status
   - **Then** the system returns status "OK", spent = 0, percentageUsed = 0

5. **Scenario**: Limit does not exist
   - **Given** no limit with ID 99
   - **When** the user sends `GET /api/limits/99/status`
   - **Then** the system returns 404

---

### Edge Cases

- What happens when the limit amount is zero or negative? Return 400.
- What happens when the warning threshold is outside 1-100? Return 400 — "warningThresholdPercent must be between 1 and 100".
- What happens when expenses span across period boundaries (e.g., month transitions)? Only expenses whose date falls within the current period count toward the limit. The period is determined by the current date at query time.
- What happens when the limit period is WEEKLY and the query happens mid-week? Count expenses from Monday to Sunday of the current week.
- What happens when the limit is deleted while a status query is in-flight? The query returns 404 (the read is not snapshot-isolated from writes at this scale).

## Requirements

### Functional Requirements

- **FR-BL-001**: System MUST allow creating, reading, updating, and deleting budget limits
- **FR-BL-002**: System MUST support three period types: WEEKLY, MONTHLY, ANNUAL
- **FR-BL-003**: System MUST enforce one limit per budget per period (no duplicate period for same budget)
- **FR-BL-004**: System MUST validate limit amount > 0
- **FR-BL-005**: System MUST validate warningThresholdPercent between 1 and 100 (default 80)
- **FR-BL-006**: System MUST return 400 for budget ID that does not exist when creating a limit
- **FR-BL-007**: System MUST compute spent amount from expenses within the current period (week/month/year) based on query date
- **FR-BL-008**: System MUST return status: OK (percentageUsed < threshold), WARNING (threshold ≤ percentageUsed < 100), EXCEEDED (percentageUsed ≥ 100)
- **FR-BL-009**: System MUST calculate WEEKLY period as Monday 00:00 to Sunday 23:59 of the current week
- **FR-BL-010**: System MUST cascade-delete limits when the parent budget is deleted (enforced by budget-core module FK constraint)

### Key Entities

- **BudgetLimit**: id (Long), budgetId (Long, FK to Budget, required), amount (BigDecimal, required, > 0), period (Enum: WEEKLY/MONTHLY/ANNUAL, required), warningThresholdPercent (int, 1-100, default 80), createdAt (LocalDateTime, auto), updatedAt (LocalDateTime, auto)
- **LimitStatus** (DTO): limitId (Long), limitAmount (BigDecimal), spent (BigDecimal), remaining (BigDecimal), percentageUsed (BigDecimal), status (Enum: OK/WARNING/EXCEEDED), period (String), periodStart (LocalDate), periodEnd (LocalDate)

### Port Dependency

```java
public interface ExpenseQueryPort {
    BigDecimal sumExpensesByPeriod(Long budgetId, LocalDate periodStart, LocalDate periodEnd);
}
```

### API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/limits` | Create a budget limit |
| GET | `/api/limits/{id}` | Get limit by ID |
| GET | `/api/limits?budgetId=` | List all limits for a budget |
| PUT | `/api/limits/{id}` | Update a limit |
| DELETE | `/api/limits/{id}` | Delete a limit |
| GET | `/api/limits/{id}/status` | Get current status with spent vs limit comparison |

## Success Criteria

### Measurable Outcomes

- **SC-BL-001**: Status queries return within 200ms (p95) — single aggregation query.
- **SC-BL-002**: Warning threshold triggers at the exact configured percentage.
- **SC-BL-003**: Period boundary calculations are correct for all three period types (tested across month/year transitions).
