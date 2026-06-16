# Implementation Plan: Frontend Foundation — Notebook Theme & Shell

**Date**: 2026-06-15
**Spec**: `DOCS/SPECS/spec-frontend-foundation.md`

## Summary

Initialize the React + TypeScript frontend with Vite, configure Tailwind CSS with the 4-color notebook palette, establish the navigation shell, and build every shared primitive (buttons, inputs, cards, dividers, skeletons, empty/error states, sketch charts). This is the foundation that all 6 feature modules depend on.

## Technical Context

**Language/Version**: TypeScript 5.x, React 19
**Primary Dependencies**: Vite, Tailwind CSS 4, React Router 7, TanStack Query 5, rough-js
**Testing**: Vitest + React Testing Library + MSW (Mock Service Worker)
**Target Platform**: Browser (Chrome, Firefox, Safari, Edge latest 2 versions)
**Project Type**: Single-page web application
**Performance Goals**: First paint <1.5s on 3G, page transitions <300ms
**Constraints**: Light-mode only (notebook metaphor), responsive 320px–1920px, no dark mode for v1
**Scale/Scope**: 7 views, ~15 shared components, 1 API client layer

## Project Structure

### Documentation (this feature)

```text
DOCS/
├── SPECS/
│   ├── spec-frontend-foundation.md
│   └── spec-frontend-*.md
└── PLANS/
    ├── plan-frontend-foundation.md     # This file
    └── plan-frontend-*.md
```

### Source Code (repository root)

```text
frontend/
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
├── tailwind.config.ts
├── public/
│   └── fonts/
│       └── (Google Fonts loaded via CSS @import)
├── src/
│   ├── main.tsx                        # React entry point
│   ├── App.tsx                         # Router + QueryClient + Layout shell
│   ├── index.css                       # Tailwind directives + font imports
│   ├── theme/
│   │   └── notebook.ts                 # Color palette, font families, border tokens
│   ├── api/
│   │   └── client.ts                   # Base fetch wrapper, error normalization, base URL
│   ├── hooks/
│   │   ├── use-api.ts                  # Generic TanStack Query hook factory
│   │   └── use-budget-context.ts       # Active budget context hook
│   ├── context/
│   │   └── BudgetContext.tsx           # Selected budget ID provider
│   ├── components/
│   │   ├── layout/
│   │   │   ├── AppShell.tsx            # Sidebar + main content area
│   │   │   ├── Sidebar.tsx             # Navigation links with hand-drawn active underline
│   │   │   └── PageTransition.tsx      # CSS page-turn animation wrapper
│   │   └── ui/
│   │       ├── NotebookButton.tsx       # Sketch-border button, sage fill
│   │       ├── RuledInput.tsx           # Single-line text/number input
│   │       ├── StickyNoteCard.tsx       # Irregular-border card wrapper
│   │       ├── HandDrawnDivider.tsx     # SVG rough horizontal line
│   │       ├── SketchProgressBar.tsx    # Rough-filled progress indicator
│   │       ├── SkeletonLoader.tsx       # Dashed outline placeholder
│   │       ├── EmptyState.tsx           # Illustration + message + CTA
│   │       ├── ErrorState.tsx           # Paper-tear graphic + retry
│   │       ├── ValidationHint.tsx       # Red-ink error annotation
│   │       └── SketchChart.tsx          # rough-js canvas/SVG wrapper
│   └── pages/
│       └── (placeholder pages for each feature — replaced by feature plans)
└── tests/
    ├── setup.ts                         # MSW server, RTL configuration
    ├── mocks/
    │   └── handlers.ts                  # MSW request handlers
    └── components/
        └── ui/                          # UI component tests
```

**Structure Decision**: Single Vite React project in `frontend/`. Components organized by responsibility: `layout/` for shell, `ui/` for reusable primitives, `pages/` for route-level views. API layer in `api/` with a single client module. Theme config centralized in `theme/`.

---

## Phase 1: Project Scaffold

**Purpose**: Initialize the Vite + React + TypeScript project with all tooling

- [ ] T001 Run `npm create vite@latest frontend -- --template react-ts` to scaffold the project
- [ ] T002 Install dependencies: `tailwindcss @tailwindcss/vite`, `react-router-dom`, `@tanstack/react-query`, `roughjs`
- [ ] T003 Install dev dependencies: `vitest @testing-library/react @testing-library/jest-dom @testing-library/user-event msw jsdom`
- [ ] T004 Configure `vite.config.ts`: Tailwind plugin, proxy `/api` → `http://localhost:8080`, test environment `jsdom`
- [ ] T005 Configure `tailwind.config.ts`: extend theme with `notebook` colors (`#F7F1DE`, `#B0BA99`, `#9D6638`, `#4E220F`) and hand-written font family using `@import url('https://fonts.googleapis.com/css2?family=Patrick+Hand&display=swap')`
- [ ] T006 Configure `tsconfig.json`: strict mode, path aliases `@/` → `src/`
- [ ] T007 Configure `vitest.config.ts`: include `src/tests/setup.ts`, globals
- [ ] T008 Write `src/index.css`: Tailwind imports, font import, base body style with cream background
- [ ] T009 Write `src/theme/notebook.ts`: export color constants, font tokens, border-radius tokens

**Checkpoint**: `npm run dev` starts, blank cream page renders with custom font loaded.

---

## Phase 2: API Client & Routing

**Purpose**: Establish the communication layer with the backend and the navigation skeleton

- [ ] T010 Create `src/api/client.ts`: generic `apiFetch<T>(url, options)` wrapper using `fetch`, with base URL from env, error normalization (throw typed errors on non-2xx), JSON parsing. Handle 400/404/500/503 uniformly.
- [ ] T011 Create `src/context/BudgetContext.tsx`: React context holding `selectedBudgetId`, provider with `useState`, exported hook `useBudget()`
- [ ] T012 Create `src/hooks/use-api.ts`: thin TanStack Query wrapper — `useApiQuery<T>(key, url)` and `useApiMutation<T>(method, url)` — using `apiFetch` from T010
- [ ] T013 Create `src/components/layout/Sidebar.tsx`: vertical nav with hand-drawn links (Budgets, History, Exchange, Limits, Simulation, Analysis). Active link underlined with rough SVG stroke in sage green. Collapses to icons on mobile.
- [ ] T014 Create `src/components/layout/PageTransition.tsx`: CSS `perspective` + `rotateY` transition wrapper. Direction-aware (forward/back based on navigation depth).
- [ ] T015 Create `src/components/layout/AppShell.tsx`: sidebar + main content area with page-transition wrapper
- [ ] T016 Wire up `src/App.tsx`: `QueryClientProvider` + `BudgetProvider` + `BrowserRouter` → routes for each feature (placeholder pages), `<AppShell>` as layout wrapper
- [ ] T017 Write `src/main.tsx`: mount `<App />` to `#root`

**Checkpoint**: Navigation works, clicking sidebar items transitions between placeholder pages with the page-turn effect. All routes resolve.

---

## Phase 3: Shared UI Primitives (Design System)

**Purpose**: Build every reusable UI component that feature modules will compose

- [ ] T018 Create `src/components/ui/NotebookButton.tsx`: `<button>` with sketch-like border (SVG filter or multiple box-shadows for rough edge), sage background on hover, warm brown text, hand-written font. Props: `variant` (primary/secondary/danger), `size` (sm/md/lg), `loading`.
- [ ] T019 Create `src/components/ui/RuledInput.tsx`: `<input>` with single bottom border in warm brown, no box border, hand-written font placeholder, focus state with sage underline animation. Props: `label`, `error`, `type` (text/number/date).
- [ ] T020 Create `src/components/ui/StickyNoteCard.tsx`: `<div>` wrapper with faint paper texture background, irregular border (CSS `filter: url(#rough-edge)` or multiple `box-shadow` offsets), subtle rotation (`transform: rotate(-0.5deg)`), paper shadow. Props: `children`, `accent` (color for left-edge accent strip).
- [ ] T021 Create `src/components/ui/HandDrawnDivider.tsx`: inline SVG `<path>` with rough-js generated wiggle stroke in warm brown at low opacity. Used as horizontal rule between sections.
- [ ] T022 Create `src/components/ui/SketchProgressBar.tsx`: horizontal bar with rough-js SVG fill. Props: `value` (0–100), `color`, `label`. Fill has sketch-like edges. Shows label below (e.g., "60% spent").
- [ ] T023 Create `src/components/ui/SkeletonLoader.tsx`: dashed/dotted outline rectangles matching expected content shape. CSS animation: dash-offset movement for subtle loading pulse. Ink-fill transition when real content replaces it.
- [ ] T024 Create `src/components/ui/EmptyState.tsx`: centered flex column with SVG illustration (empty notebook page or quill), hand-written title text, descriptive subtitle, and a `<NotebookButton>` CTA.
- [ ] T025 Create `src/components/ui/ErrorState.tsx`: paper-tear SVG at top, hand-written message ("Something came unbound"), retry `<NotebookButton>` styled as paperclip. Props: `message`, `onRetry`.
- [ ] T026 Create `src/components/ui/ValidationHint.tsx`: small red-ink text near an input, styled with hand-written font in #cc3333, with a subtle underline. Used below `RuledInput` when `error` prop is set.
- [ ] T027 Create `src/components/ui/SketchChart.tsx`: rough-js canvas/SVG wrapper component. Props: `width`, `height`, `draw(canvas: RoughCanvas)` callback. Renders a rough-style container that child components (bar chart, donut chart) draw into.

**Checkpoint**: All 10 UI primitives render correctly in isolation. A component gallery page (dev-only) shows each variant.

---

## Phase 4: Test Infrastructure

**Purpose**: Setup MSW, test utilities, and write tests for shared components

- [ ] T028 Create `src/tests/setup.ts`: configure `@testing-library/jest-dom`, initialize MSW server before all tests, clean up after
- [ ] T029 Create `src/tests/mocks/handlers.ts`: define MSW handlers for all backend API endpoints with happy-path default responses (budgets list, expenses, etc.)
- [ ] T030 Write `src/tests/components/ui/NotebookButton.test.tsx`: renders, handles click, shows loading state, applies variant classes
- [ ] T031 Write `src/tests/components/ui/RuledInput.test.tsx`: renders with label, shows error state, fires onChange
- [ ] T032 Write `src/tests/components/ui/StickyNoteCard.test.tsx`: renders children, applies accent color
- [ ] T033 Write `src/tests/components/ui/SkeletonLoader.test.tsx`: renders dashed outline, matches snapshot dimensions
- [ ] T034 Write `src/tests/components/ui/EmptyState.test.tsx`: renders message and CTA, CTA button fires onClick
- [ ] T035 Write `src/tests/components/ui/ErrorState.test.tsx`: renders message, retry button fires onRetry
- [ ] T036 Write `src/tests/App.test.tsx`: renders without crashing, sidebar links exist, navigation changes view (smoke test)

**Checkpoint**: `npm run test` passes. All shared components tested. MSW ready for feature tests.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Scaffold)**: No dependencies — start immediately
- **Phase 2 (API + Routing)**: Depends on Phase 1
- **Phase 3 (UI Primitives)**: Depends on Phase 1 (can overlap with Phase 2)
- **Phase 4 (Tests)**: Depends on Phase 2 + Phase 3

### This Plan's Role

The foundation plan MUST be fully implemented before any feature plan can begin. Features depend on:
- The Tailwind theme and notebook UI primitives
- The API client and TanStack Query hooks
- The BudgetContext provider
- The AppShell with navigation

## Notes

- The `rough-js` library takes a canvas context and draws sketch-like shapes. Wrap it in a React component that provides a canvas ref, then call rough drawing methods in `useEffect`.
- The hand-written font (Patrick Hand) is loaded via Google Fonts CSS `@import` in `index.css`. Fallback: `Georgia, serif`.
- The page-turn animation uses CSS `perspective: 1000px` on parent and `transform: rotateY(0deg)` → `rotateY(-90deg)` via CSS transition. Direction is determined by navigation depth tracking in a `useRef`.
- TanStack Query's `QueryClientProvider` wraps the entire app. Api hooks use `useQuery` and `useMutation` internally. Cache time: 5 minutes for GET, instant invalidation for POST/PUT/DELETE.
- MSW intercepts fetch calls in tests. Handlers live in `src/tests/mocks/handlers.ts` and are shared across all feature test files.
- The `useBudget()` context is read by any view that needs to know which budget is active. The sidebar or dashboard sets it.
