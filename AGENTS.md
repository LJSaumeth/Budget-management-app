# AGENTS.md

## Tech Stack

- **Backend**: Java 25, Spring Boot (latest), Lombok, H2 (embedded, local-only)
- **Frontend**: React (TBD — Phase 4+)
- **Architecture**: Hexagonal modular monolith (ports/adapters pattern, not microservices)
- **API**: REST
- **Code style**: Clean Code guidelines

## Project Phases

Development follows a strict spec → plan → implement flow, split into 7 phases:

| Phase | Scope | Status |
|-------|-------|--------|
| PH1 | Backend specs | Done |
| PH2 | Backend plans | Done |
| PH3 | Backend implementation | Pending |
| PH4 | Frontend specs | Pending |
| PH5 | Frontend plans | Pending |
| PH6 | Frontend implementation | Pending |
| PH7 | Frontend-backend sync | Pending |

**Rule**: 1 feature = 1 spec = 1 plan. Each spec is a complete, independently testable unit.

## Documentation Workflow

Before writing code, features are planned using two templates:

- **`DOCS/SPECS/spec-template.md`** — Feature specification with prioritized user stories, acceptance criteria, functional requirements, and success criteria.
- **`DOCS/PLANS/plan-template.md`** — Implementation plan derived from a spec, covering tech stack, project structure, phased tasks, and execution order.

Follow the flow: fill in a spec first, then create a plan from it.

## Module Boundaries (Backend)

Each spec maps to a module in the hexagonal monolith. Modules communicate through **ports (interfaces)** wired by Spring DI — no inter-service HTTP.

```
backend/
├── budget-core/          # Budget + Expense + Category CRUD
├── exchange/             # Fawazahmed0 currency API adapter
├── history/              # Expense history queries (read-only, depends on budget-core port)
├── limits/               # Budget limits & warnings (depends on budget-core port)
├── simulation/           # Savings projection (stateless)
├── analysis/             # Predictive suggestions (depends on budget-core port)
└── shared/               # Cross-cutting: exceptions, validation, H2 config
```

Each module follows: `domain/` → `application/` → `infrastructure/`.

## Specs & Plans

| # | File | Feature |
|---|------|---------|
| 1 | `DOCS/SPECS/spec-budget-core.md` | Budget CRUD + Expense Tracking + Categories |
| 2 | `DOCS/SPECS/spec-exchange-rate.md` | Exchange Rate (Fawazahmed0 API) |
| 3 | `DOCS/SPECS/spec-expense-history.md` | Expense History & Summary |
| 4 | `DOCS/SPECS/spec-budget-limit.md` | Budget Limits & Warnings |
| 5 | `DOCS/SPECS/spec-saving-simulation.md` | Saving Simulation |
| 6 | `DOCS/SPECS/spec-predictive-analysis.md` | Predictive Analysis & Optimization |

Plans mirror specs: `DOCS/PLANS/plan-*.md`. Implementation order: 1 → 2 → 4 → 5 → 6 → 3.

## Branch Strategy

### PH1–PH2: Specs & Plans (documentation only)

- PH1 and PH2 work was done on the `DOCS` branch.
- Once all specs and plans are approved, merge `DOCS` into `master` via PR.

### PH3: Backend Implementation

- Merge `DOCS` into `master` first.
- Create one feature branch per plan from `master`:

| Order | Branch | Plan |
|-------|--------|------|
| 1st | `feature/budget-core` | `DOCS/PLANS/plan-budget-core.md` |
| 2nd | `feature/exchange-rate` | `DOCS/PLANS/plan-exchange-rate.md` |
| 3rd | `feature/budget-limit` | `DOCS/PLANS/plan-budget-limit.md` |
| 4th | `feature/saving-simulation` | `DOCS/PLANS/plan-saving-simulation.md` |
| 5th | `feature/predictive-analysis` | `DOCS/PLANS/plan-predictive-analysis.md` |
| 6th | `feature/expense-history` | `DOCS/PLANS/plan-expense-history.md` |

- Plans 2 through 5 (exchange, limits, simulation, analysis) are implemented sequentially before plan 6 (history) — history depends on budget-core being fully complete and acts as the final backend module.
- Each feature branch merges to `master` via PR when its implementation is done and tests pass.

### PH4–PH5: Frontend Specs & Plans

- Same pattern: create a `frontend-docs` branch (or reuse `DOCS`), write frontend specs and plans, merge to `master`.

### PH6: Frontend Implementation

- One feature branch per frontend component, matching the backend feature structure.
- Naming: `feature/frontend/<feature-name>`.

### PH7: Frontend-Backend Sync

- A dedicated `feature/frontend-sync` branch for integration work.

## .gitignore

The current `.gitignore` is a Java template placeholder. Replace it with patterns appropriate for the tech stack (e.g., `node_modules`, `.env`, `dist`, `build/`, `*.class`, `data/` for H2 database files).
