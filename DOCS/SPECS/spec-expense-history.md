# Feature Specification: Expense History & Summary

**Created**: 2026-06-15

## User Scenarios & Testing

### User Story 1 - View Expense History with Filters (Priority: P1)

The user views a paginated, filterable list of past expenses for a given budget. Filters include date range, category, and free-text search on the description field.

**Why this priority**: This is the primary way the user reviews where their money went. Without history, the user would have to manually re-read each expense record. The summary view (P2) is a derived aggregation on top.

**Independent Test**: With existing expenses in the database, send `GET /api/expenses/history?budgetId=1&startDate=2026-01-01&endDate=2026-12-31&categoryId=2&search=grocery&page=0&size=20`. Returns a paginated list matching the filters. Testable with pre-seeded data and no other modules.

**Acceptance Scenarios**:

1. **Scenario**: View all expenses for a budget, no filters
   - **Given** budget ID 1 has 50 expenses
   - **When** the user sends `GET /api/expenses/history?budgetId=1`
   - **Then** the system returns 200 with the first page (default page=0, size=20), total count = 50, total pages = 3

2. **Scenario**: Filter by date range
   - **Given** budget ID 1 has expenses from January to June 2026
   - **When** the user sends `GET /api/expenses/history?budgetId=1&startDate=2026-03-01&endDate=2026-04-30`
   - **Then** the system returns only expenses within March and April 2026

3. **Scenario**: Filter by category
   - **Given** budget ID 1 has expenses across Food, Transport, and Entertainment
   - **When** the user sends `GET /api/expenses/history?budgetId=1&categoryId=2`
   - **Then** the system returns only expenses with categoryId=2

4. **Scenario**: Search by description text
   - **Given** expenses exist with descriptions "Grocery at Walmart" and "Grocery at Costco"
   - **When** the user sends `GET /api/expenses/history?budgetId=1&search=grocery`
   - **Then** the system returns both expenses (case-insensitive partial match)

5. **Scenario**: Paginate through results
   - **Given** budget ID 1 has 50 expenses
   - **When** the user sends `GET /api/expenses/history?budgetId=1&page=1&size=20`
   - **Then** the system returns page 2 (items 21-40)

6. **Scenario**: Budget does not exist
   - **Given** no budget with ID 99 exists
   - **When** the user sends `GET /api/expenses/history?budgetId=99`
   - **Then** the system returns 404

---

### User Story 2 - View Expense Summary by Category or Month (Priority: P2)

The user views an aggregated summary of expenses for a budget: totals grouped by category, or totals grouped by month. This gives the user a high-level breakdown without scanning individual expenses.

**Why this priority**: Aggregation is valuable for understanding spending patterns at a glance, but is derived entirely from the raw expense data already accessible via the history endpoint.

**Independent Test**: Send `GET /api/expenses/summary?budgetId=1&groupBy=category` and receive an array of `{ categoryName, totalAmount, count, percentage }` objects. Testable with seeded expense data.

**Acceptance Scenarios**:

1. **Scenario**: Summary grouped by category
   - **Given** budget ID 1 has expenses: Food $200, Food $50, Transport $100
   - **When** the user sends `GET /api/expenses/summary?budgetId=1&groupBy=category`
   - **Then** the system returns `[ { "categoryName": "Food", "totalAmount": 250.00, "count": 2, "percentage": 71.4 }, { "categoryName": "Transport", "totalAmount": 100.00, "count": 1, "percentage": 28.6 } ]`

2. **Scenario**: Summary grouped by month
   - **Given** budget ID 1 has expenses in January ($300) and February ($500)
   - **When** the user sends `GET /api/expenses/summary?budgetId=1&groupBy=month&year=2026`
   - **Then** the system returns `[ { "month": "2026-01", "totalAmount": 300.00, "count": ... }, { "month": "2026-02", "totalAmount": 500.00, "count": ... } ]`

3. **Scenario**: Summary with date range filter
   - **Given** expenses span January to June
   - **When** the user sends `GET /api/expenses/summary?budgetId=1&groupBy=category&startDate=2026-01-01&endDate=2026-03-31`
   - **Then** the system returns aggregates for only Q1 expenses

4. **Scenario**: Invalid groupBy value
   - **When** the user sends `GET /api/expenses/summary?budgetId=1&groupBy=week`
   - **Then** the system returns 400 with `"groupBy must be 'category' or 'month'"`

---

### Edge Cases

- What happens when startDate is after endDate? Return 400 with a validation error.
- What happens when page size exceeds 100? Cap it at 100 maximum to prevent abuse.
- What happens when a budget has zero expenses? History returns an empty list with total count 0. Summary returns an empty array.
- What happens when search string is empty or whitespace-only? Treat as no search filter applied.

## Requirements

### Functional Requirements

- **FR-EH-001**: System MUST return paginated expense history for a given budget ID
- **FR-EH-002**: System MUST support optional filters: startDate, endDate, categoryId, and search (description text)
- **FR-EH-003**: System MUST support pagination with page (0-indexed) and size parameters, defaulting to page=0, size=20
- **FR-EH-004**: System MUST cap page size at 100
- **FR-EH-005**: System MUST return total count of matching items and total pages alongside results
- **FR-EH-006**: System MUST return expenses sorted by date descending (newest first)
- **FR-EH-007**: System MUST return 404 when the budget ID does not exist
- **FR-EH-008**: System MUST return 400 when startDate > endDate
- **FR-EH-009**: System MUST support summary grouped by 'category' or 'month' with optional date range filter
- **FR-EH-010**: System MUST return 400 for invalid groupBy values
- **FR-EH-011**: System MUST include percentage of total spending in category-grouped summaries
- **FR-EH-012**: Search MUST be case-insensitive partial match on expense description

### Key Entities

This feature introduces no new persistent entities. It reads from **Expense**, **Budget**, and **Category** defined in spec-budget-core.md.

- **ExpenseHistoryPage** (DTO): content (List<Expense>), totalElements (long), totalPages (int), page (int), size (int)
- **ExpenseSummary** (DTO): groupKey (String), totalAmount (BigDecimal), count (int), percentage (BigDecimal, only for category grouping)

### Port Dependency

This module depends on a read-only port exposed by the `budget-core` module:

```java
public interface ExpenseQueryPort {
    Page<Expense> findExpenses(Long budgetId, ExpenseFilter filter, Pageable pageable);
    List<CategorySummary> summarizeByCategory(Long budgetId, LocalDate start, LocalDate end);
    List<MonthlySummary> summarizeByMonth(Long budgetId, int year, LocalDate start, LocalDate end);
}
```

### API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/expenses/history?budgetId=&startDate=&endDate=&categoryId=&search=&page=&size=` | Paginated expense list |
| GET | `/api/expenses/summary?budgetId=&groupBy=&year=&startDate=&endDate=` | Aggregated summary |

## Success Criteria

### Measurable Outcomes

- **SC-EH-001**: History queries return within 500ms for up to 10,000 expenses (p95).
- **SC-EH-002**: Summary queries return within 500ms for up to 10,000 expenses (p95).
- **SC-EH-003**: All filter combinations produce correct results (no missed or double-counted expenses).
- **SC-EH-004**: Pagination metadata (total elements, total pages) matches the exact filtered count.
