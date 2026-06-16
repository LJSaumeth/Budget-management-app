# Implementation Plan: Frontend Predictive Analysis — Smart Spending Insights

**Date**: 2026-06-15
**Spec**: `DOCS/SPECS/spec-frontend-predictive-analysis.md`

## Summary

Build a read-only analysis view showing optimization suggestions as sticky notes sorted by annual saving impact. Each suggestion identifies an overspent category with reasoning text and dollar amounts. Includes states for no overspending (encouragement message), insufficient data (prompt to log more), and empty (no expenses). Depends on budget context.

## Technical Context

**Language/Version**: TypeScript 5.x, React 19
**Primary Dependencies**: Vite, Tailwind CSS, React Router, TanStack Query
**Testing**: Vitest + React Testing Library + MSW
**Prerequisite**: `plan-frontend-foundation.md` + `plan-frontend-budget-core.md` (needs BudgetContext)

## Project Structure

```text
frontend/src/
├── hooks/
│   └── use-analysis.ts                 # TanStack Query: analyze budget
├── pages/
│   └── AnalysisPage.tsx                # Suggestions list + header summary
├── components/
│   └── analysis/
│       ├── SuggestionCard.tsx           # Single sticky-note suggestion
│       └── AnalysisHeader.tsx           # Total potential annual saving
└── tests/
    ├── pages/
    │   └── AnalysisPage.test.tsx
    └── components/
        └── analysis/
            └── SuggestionCard.test.tsx
```

---

## Phase 1: API & Hooks

- [ ] T001 Add analysis endpoint to `src/api/endpoints.ts`: `getAnalysis(budgetId)` (GET `/api/analysis/suggestions?budgetId=`)
- [ ] T002 Add types: `OptimizationSuggestion` (categoryId, categoryName, currentMonthlyAvg, suggestedReductionPercent, monthlySaving, annualSaving, reasoning), `AnalysisResponse` (suggestions, message, totalPotentialAnnualSaving)
- [ ] T003 Create `src/hooks/use-analysis.ts`: `useAnalysis(budgetId)` — TanStack Query hook. Enabled only when `budgetId` is non-null. Returns `{ data, isLoading, error }`.

---

## Phase 2: User Story 1 — View Optimization Suggestions (P1)

**Goal**: Display suggestion sticky notes sorted by annual saving, with header showing total potential saving.

- [ ] T004 [US1] Create `src/components/analysis/SuggestionCard.tsx`: sticky-note card with:
  - Title: category name (large hand-written)
  - Savings line: "$X/month · $Y/year" in sage green
  - Reasoning paragraph in hand-written font
  - Reduction percentage as a small badge
  - Cards have subtle rotation variance (`rotate(-0.3deg)`, `rotate(0.2deg)`) to look like real pinned notes
- [ ] T005 [US1] Create `src/components/analysis/AnalysisHeader.tsx`: shows "Potential Annual Savings: $X" in large hand-written text with sage green highlight. Below: "X optimization opportunities" in smaller text.
- [ ] T006 [US1] Sort cards by `annualSaving` descending (backend already returns sorted, but sort client-side as defense). Top card gets slightly larger or a subtle star accent.
- [ ] T007 [US1] Max 5 suggestions: if backend returns less, show them all. Footnote: "Showing top N opportunities" when applicable.
- [ ] T008 [US1] Write tests: multiple suggestions sorted correctly, top card has emphasis, max 5 limit, reasoning text renders, monthly/annual amounts display correctly

**Checkpoint**: Suggestions render with correct data, sorting, and notebook sticky-note styling.

---

## Phase 3: Non-Ideal States

**Goal**: Handle no-overspending, insufficient-data, and no-expenses states gracefully.

- [ ] T009 Implement "no overspending" state: when `suggestions` is empty and `message` indicates all within threshold, render centered warm message: "You're doing great! All your spending is on track." with hand-drawn star SVG icon. `totalPotentialAnnualSaving` shows $0.00.
- [ ] T010 Implement "insufficient data" state: when `message` contains "Insufficient data", render prompt with small calendar sketch: "Need at least 2 months of data to analyze your patterns. Keep logging expenses!"
- [ ] T011 Implement "no expenses" state: when `message` is "No expense data available", use standard `EmptyState` component.
- [ ] T012 Write tests: no overspending shows encouragement, insufficient data shows prompt, no expenses shows EmptyState

**Checkpoint**: All three non-ideal states render with the same polish as the suggestions view.

---

## Phase 4: Page Assembly

- [ ] T013 Create `src/pages/AnalysisPage.tsx`: `AnalysisHeader` at top, list of `SuggestionCard` below (or appropriate state for non-ideal conditions). Reads `selectedBudgetId` from `useBudget()`. Shows `SkeletonLoader` cards while loading.
- [ ] T014 Add route: `/analysis` → `<AnalysisPage />` in `App.tsx`
- [ ] T015 Write `src/tests/pages/AnalysisPage.test.tsx`: suggestions render, loading skeleton, empty/insufficient data states, error state, budget context integration

---

## Dependencies & Execution Order

- **Prerequisite**: Foundation plan + Budget-core plan (needs `useBudget()` for selected budget)
- **Phase 1 → 2 → 3 → 4**: Sequential. Phase 3 handles non-ideal states after the happy path works.

## Notes

- The analysis endpoint is a simple GET. TanStack Query's `staleTime` can be set to 0 (always refetch on mount) since analysis results depend on expense data that may have changed.
- Suggestion cards get CSS `transform: rotate(var(--rotation))` where `--rotation` is a random inline style applied per card (between -0.5deg and 0.5deg). This gives the "real pinned notes" look.
- The hand-drawn star SVG for the "doing great" state: a small inline SVG path drawn with rough-js or a pre-made hand-drawn star path.
- The backend returns the reasoning string fully formed. The frontend just renders it — no string manipulation needed.
- Annual saving amounts use `Intl.NumberFormat` for thousand separators (e.g., "$1,200.00" not "$1200.00").
- Total potential annual saving in the header is the sum of all suggestion `annualSaving` values from the backend response, displayed with the same formatting.
