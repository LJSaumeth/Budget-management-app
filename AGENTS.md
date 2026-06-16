# AGENTS.md

## Commands

All commands run from the `backend/` directory.

```bash
./gradlew build          # Compile + run all tests
./gradlew test           # Run tests only
./gradlew bootRun        # Start dev server (port 8080, H2 console at /h2-console)
./gradlew test --tests "com.budgetapp.budgetcore.application.BudgetServiceTest"  # Single test class
```

## Tech Stack

- **Backend**: Java 25, Spring Boot 3.5.0, Lombok, H2 (embedded file DB at `./data/budgetdb`)
- **Build**: Single Gradle project (`backend/build.gradle`) — NOT a multi-module build
- **Architecture**: Hexagonal (ports/adapters) — modules are Java packages, not separate Gradle subprojects
- **Test**: JUnit 5 + Mockito + AssertJ assertions.
  - Service tests: `@ExtendWith(MockitoExtension.class)`, no Spring context
  - Controller tests: `@WebMvcTest` + `@MockitoBean`, uses Spring context

## Module Map

Module directories from the AGENTS.md plan map to Java packages under `com.budgetapp`:

| Module (plan) | Java package | Status |
|---|---|---|
| budget-core | `budgetcore` | Done |
| exchange | `exchange` | Not started |
| limits | `limits` | Done |
| simulation | `simulation` | Done |
| analysis | `analysis` | Done |
| history | `history` | Done |
| shared | `shared` | Done (cross-cutting) |

Each module follows: `domain/` → `application/` → `infrastructure/`. Domain ports (interfaces like `ExpenseQueryPort`) live in `domain/port/`. Infrastructure adapters (REST controllers, JPA repos) implement them.

**Exception**: `history` has no `domain/` directory — it depends on `budgetcore.domain.port.*` (especially `ExpenseQueryPort`) instead of defining its own.

Remaining modules to implement: exchange.

## Key Conventions

- **DTOs**: `infrastructure/dto/` — Request/Response records for REST endpoints
- **Ports**: Domain interfaces in `domain/port/` — these are what other modules depend on
- **Seed data**: `src/main/resources/data.sql` uses `MERGE` statements (idempotent) to pre-seed categories
- **No separate test config**: tests share `application.yml`; use `@ExtendWith(MockitoExtension.class)` for unit tests, no Spring context
- **Currency**: defaults to `USD` (`app.default-currency` in application.yml)

## Documentation Flow

Specs (`DOCS/SPECS/spec-*.md`) → Plans (`DOCS/PLANS/plan-*.md`) → Implement on a feature branch per plan.

1 feature = 1 spec = 1 plan. Branch naming: `feature/<module-name>`.
