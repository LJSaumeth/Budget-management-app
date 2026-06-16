# Implementation Plan: Frontend Exchange Rate — Currency Converter & Rates Browser

**Date**: 2026-06-15
**Spec**: `DOCS/SPECS/spec-frontend-exchange-rate.md`

## Summary

Build a currency converter widget (amount + from/to + result slip) and a rates browser (postage-stamp-style currency grid). Both share the same view as sibling sections. No budget context needed — this feature is fully standalone.

## Technical Context

**Language/Version**: TypeScript 5.x, React 19
**Primary Dependencies**: Vite, Tailwind CSS, React Router, TanStack Query
**Testing**: Vitest + React Testing Library + MSW
**Prerequisite**: `plan-frontend-foundation.md` (needs UI primitives, API client, routing)

## Project Structure

```text
frontend/src/
├── hooks/
│   └── use-exchange.ts                 # TanStack Query: convert, getRates
├── pages/
│   └── ExchangePage.tsx                # Tab: Converter | Rates Browser
├── components/
│   └── exchange/
│       ├── ConverterForm.tsx           # Amount, from/to, submit button
│       ├── ConversionSlip.tsx          # Result card (currency-slip style)
│       └── RatesGrid.tsx               # Postage-stamp cards grid
└── tests/
    ├── pages/
    │   └── ExchangePage.test.tsx
    └── components/
        └── exchange/
            ├── ConverterForm.test.tsx
            └── RatesGrid.test.tsx
```

---

## Phase 1: API & Hooks

- [ ] T001 Add exchange endpoints to `src/api/endpoints.ts`: `getRates(base)`, `convert(amount, from, to)`
- [ ] T002 Add types: `ConversionResult`, `RatesResponse` (base, rates map, fetchedAt)
- [ ] T003 Create `src/hooks/use-exchange.ts`: `useRates(base)`, `useConversion(amount, from, to)` — `useConversion` is a query (GET), enabled only when all three params are valid. Same-currency short-circuit handled client-side: if `from === to`, return identity result without API call.

---

## Phase 2: User Story 1 — Currency Converter (P1)

**Goal**: Amount + from/to inputs → result slip card.

- [ ] T004 [US1] Create `src/components/exchange/ConverterForm.tsx`: three ruled-line inputs (amount, from currency, to currency). From/to inputs auto-uppercase and validate (3 letters, alpha). "Convert" `NotebookButton`. Client-side validation: amount > 0, currencies not blank, currencies exist.
- [ ] T005 [US1] Create `src/components/exchange/ConversionSlip.tsx`: sticky-note card showing "X FROM → Y TO", rate, result (large), fetchedAt timestamp as small note. Same-currency note: "Same currency — no rate lookup needed." Clears on form modification.
- [ ] T006 [US1] Handle validation errors via `ValidationHint` component near the offending field
- [ ] T007 [US1] Write tests: valid conversion renders slip, same-currency skips API, amount≤0 shows validation, unsupported currency shows validation, API 503 shows ErrorState

**Checkpoint**: Converter works end-to-end. Same-currency shortcut works without HTTP call.

---

## Phase 3: User Story 2 — Rates Browser (P2)

**Goal**: View all rates for a base currency as postage-stamp grid.

- [ ] T008 [US2] Create `src/components/exchange/RatesGrid.tsx`: receives rates map and fetchedAt. Renders a responsive grid of tiny cards (2 cols mobile, 4 tablet, 6 desktop). Each card: sage-green-bordered container, currency code (large text), rate value (smaller text). `<HandDrawnDivider>` above grid. FetchedAt note at top.
- [ ] T009 [US2] Write tests: renders all currencies, grid layout correct, fetchedAt displays, empty rates shows EmptyState

**Checkpoint**: Rates browser displays properly. Switching base currency refreshes grid.

---

## Phase 4: Page Assembly

- [ ] T010 Create `src/pages/ExchangePage.tsx`: tab bar (Convert | Browse Rates). Each tab renders its component. Converter results persist until form changes. Rates grid updates on base currency change.
- [ ] T011 Add route: `/exchange` → `<ExchangePage />` in `App.tsx`
- [ ] T012 Write `src/tests/pages/ExchangePage.test.tsx`: tab switching, converter-to-rates flow, error states

---

## Dependencies & Execution Order

- **Prerequisite**: Foundation plan only (no budget context needed)
- **Phase 1 → 2 → 3 → 4**: Sequential

## Notes

- Same-currency shortcut: `if (from.toUpperCase() === to.toUpperCase())` → return identity `ConversionResult` without calling the API. TanStack Query's `enabled` option set to `false` for same-currency.
- Currency auto-uppercase: `onChange` handler transforms input value with `.toUpperCase()`.
- The rates grid uses CSS grid: `grid-template-columns: repeat(auto-fill, minmax(100px, 1fr))` for responsive wrapping.
- Postage-stamp cards get a subtle CSS `filter: drop-shadow()` and a slightly irregular border via `border-radius` with varying values (e.g., `border-radius: 4px 3px 5px 3px`).
- The converter and rates browser share no state — they are independent sub-views in the same page. The tab just toggles visibility.
