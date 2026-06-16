# AGENTS.md

## Commands

### Backend (from `backend/`)

```bash
./gradlew build          # Compile + run all tests
./gradlew test           # Run tests only
./gradlew bootRun        # Start dev server (port 8080, H2 console at /h2-console)
./gradlew test --tests "com.budgetapp.budgetcore.application.BudgetServiceTest"  # Single test class
```

### Frontend (from `frontend/`)

```bash
npm run dev              # Start Vite dev server (port 5173, proxies /api → :8080)
npm run build            # Production build
npm test                 # Run vitest + RTL tests
npm test -- --run        # Run tests once (no watch mode)
npx vitest path/to/file  # Single test file
```

## Tech Stack

- **Backend**: Java 25, Spring Boot 3.5.0, Lombok, H2 (embedded file DB at `./data/budgetdb`)
- **Build**: Single Gradle project (`backend/build.gradle`) — NOT a multi-module build
- **Architecture**: Hexagonal (ports/adapters) — modules are Java packages, not separate Gradle subprojects
- **Frontend**: React 19, TypeScript, Vite, Tailwind CSS 4, TanStack Query 5, rough-js, React Router 7
- **Test**: vitest + React Testing Library + MSW (Mock Service Worker) for API mocking
- **Dev server**: Vite proxies `/api` → `http://localhost:8080` (backend must be running)

## Module Map

Module directories from the AGENTS.md plan map to Java packages under `com.budgetapp`:

| Module (plan) | Java package | Status |
|---|---|---|
| budget-core | `budgetcore` | Done |
| exchange | `exchange` | Done |
| limits | `limits` | Done |
| simulation | `simulation` | Done |
| analysis | `analysis` | Done |
| history | `history` | Done |
| shared | `shared` | Done (cross-cutting) |

Each module follows: `domain/` → `application/` → `infrastructure/`. Domain ports (interfaces like `ExpenseQueryPort`) live in `domain/port/`. Infrastructure adapters (REST controllers, JPA repos) implement them.

**Exception**: `history` has no `domain/` directory — it depends on `budgetcore.domain.port.*` (especially `ExpenseQueryPort`) instead of defining its own.

All modules implemented.

## Frontend Module Map

Feature plans from the DOCS map to feature directories under `frontend/src/`:

| Feature (plan) | Directory | Status | Depends on |
|---|---|---|---|
| foundation | `frontend/` (scaffold + theme) | Done | — |
| budget-core | `frontend/src/pages/DashboardPage.tsx`, `BudgetDetailPage.tsx` | Done | foundation |
| exchange | `frontend/src/pages/ExchangePage.tsx` | Done | foundation |
| history | `frontend/src/pages/HistoryPage.tsx` | Done | foundation, budget-core |
| limits | `frontend/src/pages/LimitsPage.tsx` | Not started | foundation, budget-core |
| simulation | `frontend/src/pages/SimulationPage.tsx` | Not started | foundation |
| analysis | `frontend/src/pages/AnalysisPage.tsx` | Not started | foundation, budget-core |

### Implementation Order (Frontend)

```
1. foundation (blocking prerequisite — theme, shell, UI primitives, API client)
       │
2. budget-core (establishes BudgetContext — needed by history, limits, analysis)
       │
   ┌───┼───┐
   │   │   │
3. exchange  4. history  5. limits
 (standalone)            │
                         │
6. simulation            7. analysis
 (standalone)
```

Exchange and simulation are standalone (no budget context). History, limits, and analysis need the BudgetContext from budget-core. Build foundation first, then budget-core, then the rest in any order.

Branch naming: `feature/frontend-<module-name>`.

## Key Conventions

- **DTOs**: `infrastructure/dto/` — Request/Response records for REST endpoints
- **Ports**: Domain interfaces in `domain/port/` — these are what other modules depend on
- **Seed data**: `src/main/resources/data.sql` uses `MERGE` statements (idempotent) to pre-seed categories
- **No separate test config**: tests share `application.yml`; use `@ExtendWith(MockitoExtension.class)` for unit tests, no Spring context
- **Currency**: defaults to `USD` (`app.default-currency` in application.yml)

## Documentation Flow

Specs (`DOCS/SPECS/spec-*.md`) → Plans (`DOCS/PLANS/plan-*.md`) → Implement on a feature branch per plan.

1 feature = 1 spec = 1 plan. Branch naming: `feature/<module-name>`.
