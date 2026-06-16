# Implementation Plan: Frontend Savings Simulation — Project Your Future Balance

**Date**: 2026-06-15
**Spec**: `DOCS/SPECS/spec-frontend-savings-simulation.md`

## Summary

Build a self-contained simulation form where the user enters income, expenses, months, current savings, and optional per-category spending reductions. Results render as a hand-written financial note showing projected savings with and without changes. Fully standalone — no budget context needed.

## Technical Context

**Language/Version**: TypeScript 5.x, React 19
**Primary Dependencies**: Vite, Tailwind CSS, React Router, TanStack Query
**Testing**: Vitest + React Testing Library + MSW
**Prerequisite**: `plan-frontend-foundation.md` (needs UI primitives, API client, routing)

## Project Structure

```text
frontend/src/
├── hooks/
│   └── use-simulation.ts               # TanStack Query mutation: simulate
├── pages/
│   └── SimulationPage.tsx              # Form + results
├── components/
│   └── simulation/
│       ├── SimulationForm.tsx           # Income, expenses, months, savings inputs
│       ├── CategoryReductionRow.tsx     # Single category + amount row
│       └── SimulationResultNote.tsx     # Financial note ledger output
└── tests/
    ├── pages/
    │   └── SimulationPage.test.tsx
    └── components/
        └── simulation/
            ├── SimulationForm.test.tsx
            └── SimulationResultNote.test.tsx
```

---

## Phase 1: API & Hooks

- [ ] T001 Add simulation endpoint to `src/api/endpoints.ts`: `simulateSavings(request)` (POST)
- [ ] T002 Add types: `SimulationRequest` (monthlyIncome, monthlyExpenses, months, currentSavings?, categoryChanges?), `CategoryChange` (category, reductionAmount), `SimulationResult` (monthlySavings, projectedSavings, adjustedMonthlyExpenses, baselineProjectedSavings?, differenceFromBaseline?)
- [ ] T003 Create `src/hooks/use-simulation.ts`: `useSimulation()` returning `{ simulate, result, isProjecting, error }`. Uses `useMutation` from TanStack Query since simulation is a POST (not cached). On success, stores result in local state. Clears result when inputs change.

---

## Phase 2: User Story 1 — Simulation Form & Results (P1)

**Goal**: Enter financial parameters, see projected savings with baseline comparison.

- [ ] T004 [US1] Create `src/components/simulation/CategoryReductionRow.tsx`: single row with ruled-line input for category name and ruled-line input for reduction amount ($). "×" button removes row with cross-out animation.
- [ ] T005 [US1] Create `src/components/simulation/SimulationForm.tsx`: ruled-line inputs for monthly income ($), monthly expenses ($), months, current savings ($) (optional, note: "leave empty for $0"). "+ Add Category" button adds a `CategoryReductionRow`. "Project" `NotebookButton` submits. Client-side validation: income ≥ 0, expenses ≥ 0, months ≥ 1, each reduction ≥ 0.
- [ ] T006 [US1] Create `src/components/simulation/SimulationResultNote.tsx`: sticky-note card showing results as a hand-written financial ledger:
  - "Monthly Savings: $X"
  - "Projected Savings (N months): $X"
  - Income, original expenses, adjusted expenses (smaller text)
  - When reductions provided: "Without Changes: $X" and "You'd Save an Extra: $X" in sage green with hand-drawn circle highlight
  - Negative values in deep brown with caution underline
  - Note when reductions are capped: "Reductions capped at total expenses" (small footnote)
- [ ] T007 [US1] Clear result on form modification: when user changes any input, previous result fades out. Re-submit required.
- [ ] T008 [US1] Write tests: basic projection renders, with current savings, with reductions (baseline appears), negative savings, expenses > income, reductions capped, client-side validation blocks invalid input

**Checkpoint**: Simulation works end-to-end. All result variants render correctly.

---

## Phase 3: Page Assembly

- [ ] T009 Create `src/pages/SimulationPage.tsx`: `SimulationForm` on the left/top, `SimulationResultNote` on the right/bottom (stacks vertically on mobile). Smooth transition between form-submit and result-render.
- [ ] T010 Add route: `/simulation` → `<SimulationPage />` in `App.tsx`
- [ ] T011 Write `src/tests/pages/SimulationPage.test.tsx`: form submission, result render, clear on modify, loading state during projection

---

## Dependencies & Execution Order

- **Prerequisite**: Foundation plan only (standalone feature, no budget context)
- **Phase 1 → 2 → 3**: Sequential

## Notes

- Simulation uses `useMutation` (POST), not `useQuery` (GET), because the backend endpoint is `POST /api/simulations/savings`. The result is not cached beyond the current session.
- Category reduction rows are managed in local state: `useState<CategoryChange[]>([])`. "+ Add Category" appends an empty row. "×" removes by index.
- "Reductions capped" note: the backend caps automatically. The frontend can show the note when `adjustedMonthlyExpenses === 0` and any reduction was specified.
- The result note is a `StickyNoteCard` with extra ledger-line styling (thin horizontal lines mimicking ruled paper behind the text).
- Same-currency simulation uses no exchange rate data. The simulation is purely financial math — no external API dependency beyond the backend endpoint.
