# Implementation Plan: Frontend Expense History & Summaries

**Date**: 2026-06-15
**Spec**: `DOCS/SPECS/spec-frontend-expense-history.md`

## Summary

Build the expense history browser with date-range, category, and text-search filters, plus paginated ledger results. Add two summary views: a hand-drawn donut chart for category breakdown and a hand-drawn bar chart for monthly spending. Uses rough-js for sketch-style chart rendering.

## Technical Context

**Language/Version**: TypeScript 5.x, React 19
**Primary Dependencies**: Vite, Tailwind CSS, React Router, TanStack Query, rough-js
**Testing**: Vitest + React Testing Library + MSW
**Prerequisite**: `plan-frontend-foundation.md` + `plan-frontend-budget-core.md` (needs BudgetContext for selected budget)

## Project Structure

```text
frontend/src/
├── hooks/
│   └── use-history.ts                  # TanStack Query: history, category summary, monthly summary
├── pages/
│   └── HistoryPage.tsx                 # Tab container: history list | category summary | monthly summary
├── components/
│   └── history/
│       ├── HistoryFilters.tsx          # Date range, category dropdown, search input
│       ├── ExpenseHistoryList.tsx      # Paginated ledger rows
│       ├── CategoryDonutChart.tsx      # rough-js donut chart with legend
│       └── MonthlyBarChart.tsx         # rough-js bar chart with year selector
└── tests/
    ├── pages/
    │   └── HistoryPage.test.tsx
    └── components/
        └── history/
            ├── CategoryDonutChart.test.tsx
            └── MonthlyBarChart.test.tsx
```

---

## Phase 1: API & Hooks

- [ ] T001 Add history endpoints to `src/api/endpoints.ts`: `getExpenseHistory(budgetId, filter)`, `getCategorySummary(budgetId, start, end)`, `getMonthlySummary(budgetId, year, start, end)`
- [ ] T002 Add types: `ExpenseFilter`, `ExpenseHistoryPage`, `CategorySummaryItem`, `MonthlySummaryItem`
- [ ] T003 Create `src/hooks/use-history.ts`: `useExpenseHistory(budgetId, filter, page, size)`, `useCategorySummary(budgetId, dateRange)`, `useMonthlySummary(budgetId, year, dateRange)`. Date range defaults to last 12 months.

---

## Phase 2: User Story 1 — Expense History Browser (P1)

**Goal**: Paginated, filterable expense list with notebook-styled filter controls.

- [ ] T004 [US1] Create `src/components/history/HistoryFilters.tsx`: ruled-line date inputs (start/end), category paper-slip dropdown, search ruled input. Filters applied on change with debounce (300ms for search). Clear filters button.
- [ ] T005 [US1] Create `src/components/history/ExpenseHistoryList.tsx`: paginated ledger rows using `ExpenseRow` from budget-core. Torn-paper pagination controls at bottom showing "Page X of Y". Uses `useInfiniteQuery`.
- [ ] T006 [US1] Write tests: filter by date range, filter by category, search by text, paginate, no results state

**Checkpoint**: History browser functional. Filters and pagination work independently.

---

## Phase 3: User Story 2 — Category Donut Chart (P2)

**Goal**: Hand-drawn donut chart showing spending proportion by category.

- [ ] T007 [US2] Create `src/components/history/CategoryDonutChart.tsx`: receives `CategorySummaryItem[]`. Uses `SketchChart` foundation component. Draws donut chart with rough-js: arcs for each category's proportion, sketch-like outlines, percentage labels positioned outside arcs. Sage green legend below with category names and pen-dot color indicators. Percentage labels use hand-written font.
- [ ] T008 [US2] Single-category edge case: full ring. Zero-data: inline empty state.
- [ ] T009 [US2] Write test: renders segments matching data, legend shows all categories, single category renders full ring

**Checkpoint**: Donut chart renders with correct proportions and notebook aesthetic.

---

## Phase 4: User Story 3 — Monthly Bar Chart (P2)

**Goal**: Hand-drawn bar chart showing spending by month for a selected year.

- [ ] T010 [US3] Create `src/components/history/MonthlyBarChart.tsx`: receives `MonthlySummaryItem[]`. Draws bars with rough-js: each bar has sketch fill in warm brown, uneven top edge, month label below (e.g., "Jan"), amount label above. Faint ruled-paper grid lines behind bars (horizontal lines at round amounts). Year selector (dropdown or left/right arrows).
- [ ] T011 [US3] Zero-value months render as faint dotted outlines (not gaps).
- [ ] T012 [US3] Write test: renders bars for each month, zero month shows dotted outline, year switch updates bars

**Checkpoint**: Bar chart renders with correct monthly totals and notebook aesthetic.

---

## Phase 5: Page Assembly

- [ ] T013 Create `src/pages/HistoryPage.tsx`: tab bar (History | By Category | By Month) using hand-drawn toggle buttons (like limit period selector). Each tab renders its respective component. Date range filter shared across all tabs. Reads `selectedBudgetId` from `useBudget()`.
- [ ] T014 Add route: `/history` → `<HistoryPage />` with redirect if no budget selected
- [ ] T015 Write `src/tests/pages/HistoryPage.test.tsx`: tab switching, filter sharing, loading/empty/error states per tab

---

## Dependencies & Execution Order

- **Prerequisite**: Foundation plan + Budget-core plan (needs `ExpenseRow`, `useBudget()`)
- **Phase 1 → 2 → 3/4 → 5**: Sequential. Phases 3 and 4 can be parallel.

## Notes

- rough-js donut arcs: compute start/end angles from percentage data, draw arcs with `rc.arc()`. For the "hole" in the donut, draw the outer arc segments and leave center empty.
- rough-js bar chart: each bar is a `rc.rectangle(x, y, width, height)`. Vary the seed for each bar to get slightly different rough edges.
- Date range defaults to the last 12 months when no filter is set. Use `Date` constructor and `toISOString().split('T')[0]` for ISO date strings.
- The torn-paper pagination uses CSS `clip-path` with a slightly jagged polygon or a rough SVG border on the pagination bar.
