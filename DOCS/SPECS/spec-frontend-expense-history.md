# Feature Specification: Frontend Expense History & Summaries

**Created**: 2026-06-15

## User Scenarios & Testing

### User Story 1 - Expense History Browser (Priority: P1)

The user browses a paginated, filterable list of past expenses for their selected budget. Filters include date range, category, and free-text search. The list renders in the notebook ledger style with hand-drawn filter controls.

**Why this priority**: This is how users review where their money went across all time. Without history, they'd have to scroll the expense ledger manually. The summary charts (P2) are derived aggregations on top of this data.

**Independent Test**: With expenses in the backend, navigate to the History tab for a budget. Apply date range, category, and search filters. Verify pagination works across pages. Testable with a single budget and pre-seeded expenses.

**Acceptance Scenarios**:

1. **Scenario**: View unfiltered expense history
   - **Given** budget ID 1 has 50 expenses
   - **When** the user opens History
   - **Then** the first 20 expenses render as ledger rows with pagination controls at the bottom styled as torn-paper page edges, showing "Page 1 of 3"

2. **Scenario**: Filter by date range
   - **Given** expenses span January to June 2026
   - **When** the user sets start date to March 1 and end date to April 30 (using ruled-line date inputs)
   - **Then** only March-April expenses render in the ledger

3. **Scenario**: Filter by category
   - **Given** expenses exist across Food, Transport, and Entertainment
   - **When** the user selects "Food" from a category dropdown styled as paper slips
   - **Then** only Food expenses render

4. **Scenario**: Search by description
   - **Given** expenses include "Grocery at Walmart" and "Grocery at Costco"
   - **When** the user types "grocery" in the search field (ruled-line input)
   - **Then** both expenses render, with the matching text portion subtly underlined in sage green

5. **Scenario**: Paginate through results
   - **Given** 50 matching expenses exist
   - **When** the user clicks the torn-paper "Next page" control
   - **Then** items 21-40 render, page indicator updates, and the "Previous page" edge becomes active

6. **Scenario**: No results match filters
   - **Given** no expenses match the current filters
   - **When** the ledger area renders
   - **Then** an empty notebook page appears: "No expenses match your filters — try a different date range or category"

---

### User Story 2 - Category Spending Summary (Priority: P2)

The user views a visual breakdown of spending by category for a date range. A hand-drawn donut chart shows each category's proportion of total spending with percentage labels.

**Why this priority**: Visual summaries reveal patterns that raw lists hide. Valuable but secondary to the raw history list — users can manually calculate proportions from the ledger.

**Independent Test**: With expense data across categories, select the "By Category" summary view. Verify the donut chart segments match expected proportions. Verify segments are color-coded and labeled. Testable with pre-seeded data.

**Acceptance Scenarios**:

1. **Scenario**: View category donut chart
   - **Given** spending is Food $250, Transport $100, Entertainment $50 (total $400)
   - **When** the user selects "By Category" summary
   - **Then** a rough/sketch donut chart renders with 3 segments: Food (~62.5% in warm brown), Transport (~25% in sage green), Entertainment (~12.5% in light cream contrast). Each segment has a hand-drawn outline and percentage label. A legend below shows category names with colored pen dots.

2. **Scenario**: Single category (100%)
   - **Given** all spending is in Food only
   - **When** the donut chart renders
   - **Then** the chart is a single full ring segment in warm brown, labeled "Food — 100%"

3. **Scenario**: No data for category summary
   - **Given** the budget has no expenses in the selected range
   - **When** the category summary renders
   - **Then** an empty state shows: "No spending in this date range" with a suggestion to broaden the filter

---

### User Story 3 - Monthly Spending Summary (Priority: P2)

The user views spending broken down by month as a hand-drawn bar chart. Each month's bar shows total spending, with optional year selection.

**Why this priority**: Month-over-month trends help users spot seasonal patterns. Secondary to both the history list and category breakdown.

**Independent Test**: With multi-month expense data, select the "By Month" summary. Verify bars match monthly totals. Test with year filter. Testable with pre-seeded data.

**Acceptance Scenarios**:

1. **Scenario**: View monthly bar chart
   - **Given** spending is January $300, February $450, March $500 (2026)
   - **When** the user selects "By Month" summary with year 2026
   - **Then** a hand-drawn bar chart renders with 3 bars. Each bar has rough/sketch fill in warm brown (#9D6638), uneven tops, and labels showing the month below and amount above in the hand-written font. A faint ruled-paper grid appears behind the bars.

2. **Scenario**: Switch year
   - **Given** expenses span 2025 and 2026
   - **When** the user changes the year selector
   - **Then** the bars update to show the selected year's monthly breakdown

3. **Scenario**: Month with no spending
   - **Given** April has $0 expenses
   - **When** the bar chart renders
   - **Then** April's bar is rendered as a faint dotted outline (showing 0) to maintain the continuous monthly timeline

---

### Edge Cases

- What happens when the user applies conflicting filters (e.g., category=Food but search="gas")? The backend returns an empty list. The frontend shows the "no results" empty state.
- What about very wide date ranges (multiple years)? Pagination handles large result sets. Summaries aggregate correctly regardless of range size.
- What about edge-of-month date boundaries (Feb 28, Dec 31)? The backend handles date boundaries; the frontend passes dates as ISO strings without modification.

## Requirements

### Functional Requirements

- **FR-EH-001**: System MUST render a paginated expense history list with date range, category, and text-search filter controls
- **FR-EH-002**: Filter controls MUST use ruled-line inputs, paper-slip dropdowns, and notebook-styled date pickers
- **FR-EH-003**: Pagination controls MUST appear as torn-paper edges with page indicators
- **FR-EH-004**: System MUST render a hand-drawn donut chart for category spending breakdown
- **FR-EH-005**: Donut chart segments MUST have sketch-style outlines, percentage labels, and a labeled legend
- **FR-EH-006**: System MUST render a hand-drawn bar chart for monthly spending breakdown
- **FR-EH-007**: Bar chart bars MUST have rough/sketch fill, uneven tops, and amount labels in hand-written font
- **FR-EH-008**: The bar chart MUST support year selection to switch between years of data
- **FR-EH-009**: Months with zero spending MUST render as faint dotted outline bars (not gaps)
- **FR-EH-010**: The history view MUST support a date range filter; the summary views MUST respect the same date range
- **FR-EH-011**: All views MUST use the foundation notebook theme, shared loading skeletons, and shared empty/error states

### Key Entities (Frontend State)

- **HistoryFilter**: budgetId, categoryId, startDate, endDate, search, page, size
- **ExpenseHistoryPage**: content (expense list), totalElements, totalPages, currentPage, pageSize
- **CategorySummary**: categoryId, categoryName, totalAmount, expenseCount, percentage
- **MonthlySummary**: month label (e.g., "2026-03"), totalAmount, expenseCount

## Success Criteria

### Measurable Outcomes

- **SC-EH-001**: Applying a filter updates the history list within 1 second of the last keystroke (debounced search) or immediately on dropdown change
- **SC-EH-002**: The donut chart renders correctly with up to 9 categories (all pre-seeded categories) without label overlap
- **SC-EH-003**: The bar chart renders correctly for any year with 1-12 months of data, with all labels readable
- **SC-EH-004**: Toggling between History and Summary views preserves the selected date range filter
