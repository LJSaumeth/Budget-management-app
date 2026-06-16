# Implementation Plan: Frontend Budget Core — Dashboard & Expense Ledger

**Date**: 2026-06-15
**Spec**: `DOCS/SPECS/spec-frontend-budget-core.md`

## Summary

Build the budget dashboard (notebook index page) showing all budgets as sticky-note cards, and the expense ledger page showing expenses as hand-written rows. Includes budget CRUD, expense CRUD, category management, and real-time balance updates. This is the first feature built on the foundation — it establishes the page pattern and data-fetching pattern that all other features follow.

## Technical Context

**Language/Version**: TypeScript 5.x, React 19
**Primary Dependencies**: Vite, Tailwind CSS, React Router, TanStack Query
**Storage**: N/A — all data from backend API (`/api/budgets`, `/api/expenses`, `/api/categories`)
**Testing**: Vitest + React Testing Library + MSW
**Target Platform**: Browser
**Prerequisite**: `plan-frontend-foundation.md` fully complete

## Project Structure

### Source Code (additions to `frontend/`)

```text
frontend/src/
├── api/
│   └── endpoints.ts                    # Budget, expense, category endpoint functions
├── hooks/
│   ├── use-budgets.ts                  # TanStack Query: list, create, update, delete
│   ├── use-expenses.ts                 # TanStack Query: per-budget CRUD
│   └── use-categories.ts              # TanStack Query: list, create
├── pages/
│   ├── DashboardPage.tsx               # Budget cards grid + create form
│   └── BudgetDetailPage.tsx            # Budget header + expense ledger
├── components/
│   ├── dashboard/
│   │   └── BudgetCard.tsx              # Sticky-note budget summary card
│   └── ledger/
│       ├── BudgetHeader.tsx            # Name, amounts, balance progress bar
│       ├── ExpenseRow.tsx              # Single ledger entry with edit/delete
│       ├── AddExpenseForm.tsx          # Inline folding form
│       └── CategoryTag.tsx             # Small colored category label
└── tests/
    ├── pages/
    │   ├── DashboardPage.test.tsx      # Render budgets, create, delete
    │   └── BudgetDetailPage.test.tsx   # Render ledger, CRUD operations
    └── components/
        ├── BudgetCard.test.tsx
        └── ExpenseRow.test.tsx
```

---

## Phase 1: API Layer

**Purpose**: Define typed endpoint functions for budget-core backend APIs

- [ ] T001 Create `src/api/endpoints.ts`: add typed functions `getBudgets()`, `createBudget()`, `updateBudget()`, `deleteBudget()`, `getExpenses(budgetId)`, `createExpense()`, `updateExpense()`, `deleteExpense()`, `getCategories()`, `createCategory()` using the `apiFetch` wrapper from foundation
- [ ] T002 Add TypeScript interfaces in `src/api/types.ts`: `Budget`, `Expense`, `Category`, `BudgetRequest`, `ExpenseRequest`, `CategoryRequest`

---

## Phase 2: Data Hooks

**Purpose**: TanStack Query hooks for all data operations

- [ ] T003 Create `src/hooks/use-budgets.ts`: `useBudgets()` (list), `useBudget(id)`, `useCreateBudget()`, `useUpdateBudget()`, `useDeleteBudget()` — each with proper query key invalidation
- [ ] T004 Create `src/hooks/use-expenses.ts`: `useExpenses(budgetId)`, `useCreateExpense()`, `useUpdateExpense()`, `useDeleteExpense()` — invalidate expense list on mutation. Pass `budgetId` from `useBudget()` context.
- [ ] T005 Create `src/hooks/use-categories.ts`: `useCategories()`, `useCreateCategory()`

---

## Phase 3: User Story 1 — Budget Dashboard (P1)

**Goal**: Landing page showing all budgets as sticky-note cards, with create/edit/delete and navigation into budget detail.

**Independent Test**: Render dashboard with MSW mocking 3 budgets. Create a new budget, verify card appears. Click card, verify navigation to detail page. Delete a budget, verify removal.

- [ ] T006 [US1] Create `src/components/dashboard/BudgetCard.tsx`: sticky-note card showing name (large hand-written), total amount, spent/remaining mini progress bar (`SketchProgressBar`). Edit (pen icon) triggers inline edit. Delete (cross-out icon) triggers confirmation. Click body navigates to `/budgets/:id`.
- [ ] T007 [US1] Create `src/pages/DashboardPage.tsx`: grid of `BudgetCard` components using `useBudgets()`. "+ New Budget" button opens inline create form (name, amount, currency). Loading state uses `SkeletonLoader`. Empty state uses `EmptyState`.
- [ ] T008 [US1] Add route: `/` → `<DashboardPage />` in `App.tsx`
- [ ] T009 [US1] Write `src/tests/pages/DashboardPage.test.tsx`: renders budgets from MSW, creates budget via form, navigates on card click, confirms delete, empty state renders when no budgets

**Checkpoint**: Dashboard fully functional — CRUD works, navigation to budget detail works.

---

## Phase 4: User Story 2 — Expense Ledger (P1)

**Goal**: Budget detail page with header (name, spent, remaining) and expense ledger with add/edit/delete. Balance updates reactively.

**Independent Test**: Navigate to budget page with MSW returning 5 expenses. Add expense, verify row appears and balance updates. Edit amount, verify update. Delete row, verify removal.

- [ ] T010 [US2] Create `src/components/ledger/BudgetHeader.tsx`: shows budget name (large text), "Spent $X / $Y" with `SketchProgressBar`, remaining in sage green. Fetches budget by ID.
- [ ] T011 [US2] Create `src/components/ledger/ExpenseRow.tsx`: single ledger row — date (left), `CategoryTag` (sage green), description, amount (right-aligned). Alternating paper-tone background. Edit icon → inline edit mode. Delete icon → fade out.
- [ ] T012 [US2] Create `src/components/ledger/AddExpenseForm.tsx`: inline folding form (CSS height transition). Ruled-line inputs for amount, category dropdown, description, date. Submit adds expense and collapses form with ink-write animation on new row.
- [ ] T013 [US2] Create `src/pages/BudgetDetailPage.tsx`: `BudgetHeader` + "Add Expense" button + list of `ExpenseRow` components. Handles loading (skeleton rows), empty (no expenses CTA), error states.
- [ ] T014 [US2] Add route: `/budgets/:id` → `<BudgetDetailPage />` in `App.tsx`. Set `selectedBudgetId` via `useBudget()` context.
- [ ] T015 [US2] Write `src/tests/pages/BudgetDetailPage.test.tsx`: renders header + ledger, adds expense, edits expense, deletes expense, balance updates, empty state, pagination
- [ ] T016 [US2] Write `src/tests/components/ExpenseRow.test.tsx`: renders data, edit mode, delete triggers callback

**Checkpoint**: Full expense tracking works. User can navigate Dashboard → Budget Detail → log expenses → back.

---

## Phase 5: User Story 3 — Category Management (P2)

**Goal**: Display available categories as styled tags. Allow adding custom categories.

**Independent Test**: View category list from the expense form. Add a new category, verify it appears in the dropdown.

- [ ] T017 [US3] Create `src/components/ledger/CategoryTag.tsx`: small tag with hand-drawn border, category name, colored accent from an extended muted palette derived from notebook colors
- [ ] T018 [US3] Add category section to `BudgetDetailPage.tsx`: small expandable area showing all categories as `CategoryTag`. "Add Category" input creates via `useCreateCategory()`.
- [ ] T019 [US3] Wire categories into `AddExpenseForm.tsx` category dropdown
- [ ] T020 [US3] Write `src/tests/components/CategoryTag.test.tsx`: renders name, applies color

**Checkpoint**: Categories visible and usable in expense form. Custom categories work.

---

## Dependencies & Execution Order

- **Prerequisite**: `plan-frontend-foundation.md` fully complete (all UI primitives, API client, routing, theme)
- **Phase 1 → 2 → 3 → 4 → 5**: Sequential within this plan
- **US1 (Dashboard)** and **US2 (Ledger)** are both P1 and can be developed in order. US3 (Categories) is P2.

## Notes

- The budget balance header updates reactively via TanStack Query invalidation: after any expense mutation, call `queryClient.invalidateQueries({ queryKey: ['expenses', budgetId] })` and `queryClient.invalidateQueries({ queryKey: ['budget', budgetId] })`.
- The expense ledger paginates by default (20 per page). Use the backend's pagination params (`page`, `size`). The "Load more" button triggers `fetchNextPage` from TanStack Query's `useInfiniteQuery`.
- Budget card amounts display with 2 decimal places and thousand separators via `Intl.NumberFormat`.
- Category colors: pre-seeded categories get fixed colors from the notebook palette. Custom categories get a random color from an extended muted palette that doesn't clash with notebook colors.
