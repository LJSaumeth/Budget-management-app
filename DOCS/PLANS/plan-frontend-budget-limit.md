# Implementation Plan: Frontend Budget Limits — Spending Caps & Status

**Date**: 2026-06-15
**Spec**: `DOCS/SPECS/spec-frontend-budget-limit.md`

## Summary

Build the limits management view with sticky-note cards for each limit, a create/edit form with period toggle buttons, and hand-drawn progress bars per limit showing OK/WARNING/EXCEEDED status. Depends on the budget context to know which budget's limits to display.

## Technical Context

**Language/Version**: TypeScript 5.x, React 19
**Primary Dependencies**: Vite, Tailwind CSS, React Router, TanStack Query, rough-js
**Testing**: Vitest + React Testing Library + MSW
**Prerequisite**: `plan-frontend-foundation.md` + `plan-frontend-budget-core.md` (needs BudgetContext)

## Project Structure

```text
frontend/src/
├── hooks/
│   └── use-limits.ts                   # TanStack Query: limits CRUD, status
├── pages/
│   └── LimitsPage.tsx                  # Limits list + create form
├── components/
│   └── limits/
│       ├── LimitCard.tsx               # Sticky-note with amount, period, actions
│       ├── LimitStatusBar.tsx          # Hand-drawn progress bar with status label
│       └── LimitForm.tsx               # Create/edit form with period toggles
└── tests/
    ├── pages/
    │   └── LimitsPage.test.tsx
    └── components/
        └── limits/
            ├── LimitCard.test.tsx
            └── LimitStatusBar.test.tsx
```

---

## Phase 1: API & Hooks

- [ ] T001 Add limits endpoints to `src/api/endpoints.ts`: `getLimits(budgetId)`, `createLimit()`, `updateLimit(id)`, `deleteLimit(id)`, `getLimitStatus(limitId)`
- [ ] T002 Add types: `BudgetLimit`, `LimitRequest`, `LimitStatus` (limitId, spent, remaining, percentageUsed, status, periodStart, periodEnd)
- [ ] T003 Create `src/hooks/use-limits.ts`: `useLimits(budgetId)`, `useLimitStatuses(budgetId)` (fetches status for all limits), `useCreateLimit()`, `useUpdateLimit()`, `useDeleteLimit()`

---

## Phase 2: User Story 1 — Set and Manage Limits (P1)

**Goal**: CRUD for spending limits with period toggle UI and duplicate prevention.

- [ ] T004 [US1] Create `src/components/limits/LimitForm.tsx`: ruled-line input for amount, three hand-drawn toggle buttons for period (Weekly/Monthly/Annual — active one filled in sage green), input for warning threshold (%). Validate: amount > 0, threshold 1–100. Duplicate period check via existing limits list.
- [ ] T005 [US1] Create `src/components/limits/LimitCard.tsx`: sticky-note card showing amount (large), period tag, threshold, creation date. Edit (pen icon) switches card to `LimitForm` in edit mode. Delete (cross-out) triggers confirmation note.
- [ ] T006 [US1] Write tests: create limit renders card, edit updates amount, delete removes card, duplicate period shows validation error, empty state with no limits

**Checkpoint**: Limits CRUD works. Period toggle UI is intuitive.

---

## Phase 3: User Story 2 — Limit Status (P2)

**Goal**: Hand-drawn progress bars per limit showing spent vs limit with status label.

- [ ] T007 [US2] Create `src/components/limits/LimitStatusBar.tsx`: uses `SketchProgressBar` from foundation with rough-js fill. Three visual states:
  - **OK**: fill in warm brown (#9D6638), label "OK — $X remaining" in sage green
  - **WARNING**: fill darkens to deep brown (#4E220F), label "WARNING — $X remaining" with hand-drawn underline
  - **EXCEEDED**: fill at 100% with slight overfill (pen stroke extends past bar end), label "EXCEEDED — $X over budget" with double-underline
  - Shows period and date range as small note below ("Monthly · Jun 1 – Jun 30, 2026")
- [ ] T008 [US2] Integrate `LimitStatusBar` into `LimitCard`: each card now shows status below the limit amount
- [ ] T009 [US2] Write tests: OK state at 60%, WARNING at 85%, EXCEEDED at 110%, correct remaining/over amounts, period date range displayed

**Checkpoint**: Progress bars render correctly for all three states with notebook aesthetic.

---

## Phase 4: Page Assembly

- [ ] T010 Create `src/pages/LimitsPage.tsx`: list of `LimitCard` components. "+ Set Limit" button opens `LimitForm` inline (collapsed by default, unfolds). Uses `useLimits()` and `useBudget()` for context.
- [ ] T011 Add route: `/limits` → `<LimitsPage />` in `App.tsx`
- [ ] T012 Write `src/tests/pages/LimitsPage.test.tsx`: renders limits, creates new, status bars show, empty state, loading state

---

## Dependencies & Execution Order

- **Prerequisite**: Foundation plan + Budget-core plan (needs `useBudget()` for selected budget)
- **Phase 1 → 2 → 3 → 4**: Sequential. Phase 3 depends on Phase 2 (status needs existing limits to display).

## Notes

- The period toggle is three `<button>` elements styled identically, with the active one having a sage green fill (`bg-[#B0BA99] text-[#F7F1DE]`). Click handler sets state.
- Duplicate period check: before creating, check if `limits.some(l => l.period === selectedPeriod)`. Show `ValidationHint` if duplicate.
- Progress bar overfill for EXCEEDED: when `percentageUsed > 100`, fill the bar to 100% width and render a small jagged extension past the right edge (CSS `transform: translateX(2px)` or an extra rough-js line).
- The status date range display uses the `periodStart` and `periodEnd` fields returned by the backend's status endpoint. The frontend formats them with `Intl.DateTimeFormat` for a readable format (e.g., "Jun 1 – Jun 30, 2026").
- Re-fetch limit statuses after any expense mutation to keep bars current. Use TanStack Query's `refetchInterval` or manual invalidation when navigating to the Limits page.
