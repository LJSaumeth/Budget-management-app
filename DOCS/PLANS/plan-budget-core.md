# Implementation Plan: Budget Core (Budget CRUD + Expense Tracking + Categories)

**Date**: 2026-06-15
**Spec**: `DOCS/SPECS/spec-budget-core.md`

## Summary

Implement the foundational budget-core module — the heart of the application. This module provides full CRUD for budgets, expenses, and user-defined categories, plus a read-only port (`ExpenseQueryPort`) through which other modules access expense data. This plan also covers the shared project scaffold since budget-core is the first module to be built.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot 3.5+, Spring Data JPA, H2, Lombok, Jakarta Validation
**Storage**: H2 embedded (file-based: `./data/budgetdb`)
**Testing**: JUnit 5 + MockMvc + H2 in-memory test DB
**Target Platform**: Local JVM (single-user desktop app)
**Project Type**: Single Gradle project with package-level module separation
**Architecture**: Hexagonal modular monolith — ports/adapters pattern
**Constraints**: No external services, no authentication, embedded-only

## Project Structure

### Documentation (this feature)

```text
DOCS/
├── SPECS/
│   └── spec-budget-core.md
└── PLANS/
    └── plan-budget-core.md          # This file
```

### Source Code (repository root)

```text
backend/
├── build.gradle
├── src/main/java/com/budgetapp/
│   ├── BudgetApplication.java
│   ├── shared/
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── ResourceNotFoundException.java
│   │   │   └── ConflictException.java
│   │   └── config/
│   │       └── CorsConfig.java
│   └── budgetcore/
│       ├── domain/
│       │   ├── Budget.java
│       │   ├── Expense.java
│       │   ├── Category.java
│       │   └── port/
│       │       └── ExpenseQueryPort.java
│       ├── application/
│       │   ├── BudgetService.java
│       │   ├── ExpenseService.java
│       │   └── CategoryService.java
│       └── infrastructure/
│           ├── persistence/
│           │   ├── BudgetRepository.java
│           │   ├── ExpenseRepository.java
│           │   ├── CategoryRepository.java
│           │   └── ExpenseQueryPortImpl.java
│           ├── rest/
│           │   ├── BudgetController.java
│           │   ├── ExpenseController.java
│           │   └── CategoryController.java
│           └── dto/
│               ├── BudgetRequest.java
│               ├── ExpenseRequest.java
│               ├── CategoryRequest.java
│               ├── BudgetResponse.java
│               ├── ExpenseResponse.java
│               └── CategoryResponse.java
├── src/main/resources/
│   ├── application.yml
│   └── data.sql
└── src/test/java/com/budgetapp/
    └── budgetcore/
        ├── application/
        │   ├── BudgetServiceTest.java
        │   ├── ExpenseServiceTest.java
        │   └── CategoryServiceTest.java
        └── infrastructure/
            └── rest/
                ├── BudgetControllerTest.java
                ├── ExpenseControllerTest.java
                └── CategoryControllerTest.java
```

**Structure Decision**: Single Gradle project with package-level module separation. A multi-module Gradle build adds overhead without benefit for a single-deployable monolith. Module boundaries are enforced by convention — each module owns its `domain/`, `application/`, and `infrastructure/` packages, and cross-module access goes through ports only.

---

## Clean Code Guidelines

### Naming & Style
- **Classes/Interfaces**: `PascalCase` — `BudgetService`, `ExpenseRepository`, `ExpenseQueryPort`
- **Methods**: `camelCase` — `findById()`, `calculateRemainingAmount()`, `existsByNameIgnoreCase()`
- **Constants**: `UPPER_SNAKE_CASE` — `MAX_PAGE_SIZE`, `DEFAULT_PAGE`, `DEFAULT_CURRENCY`
- **Packages**: `lowercase` without hyphens — `com.budgetapp.budgetcore.domain`, `com.budgetapp.shared.exception`
- **DTOs**: Explicit suffix — `BudgetRequest`, `ExpenseResponse`, `CategoryRequest`

### Single Responsibility
- Each class must have ONE reason to change
- **Controllers**: Only HTTP — parse requests, delegate to services, return responses. Never call repositories directly.
- **Services**: Only business logic — never access `HttpServletRequest`, never build SQL strings. Inject ports (interfaces), not implementations.
- **Repositories**: Only data access — query methods, no business logic, no validation beyond JPA constraints.
- **Domain/Entities**: Only data + basic validations (`@NotBlank`, `@DecimalMin`). Timestamps via `@PrePersist`/`@PreUpdate`, not in services.
- If a class exceeds ~300 lines, extract responsibilities to a delegate class.
- Separate packages by domain (feature-based): `budgetcore/`, `exchange/`, `history/`, not by layer (`controllers/`, `services/`).

### Clean Methods
- Maximum ~30 lines per method; extract logical blocks to private methods with descriptive names.
- **Guard clauses first**: `if (budget == null) throw new ResourceNotFoundException("Budget", id)` — return or throw early.
- One level of abstraction per method: don't mix request validation with entity mapping with persistence.
- Avoid more than 3 levels of indentation; use `Optional`, `Stream`, or helper methods.
- Query methods must be side-effect-free; command methods must document the effect (e.g., cascade delete).

### SOLID Principles
- **S**: Single Responsibility (see above)
- **O**: Open to extension via interface ports (`ExpenseQueryPort`), closed to modification of domain entities.
- **L**: Liskov Substitution — port implementations must be 100% interchangeable (in-memory mock ↔ JPA impl).
- **I**: Interface Segregation — `ExpenseQueryPort` exposes only read methods; write operations belong to `BudgetService`.
- **D**: Depend on abstractions — inject `ExpenseQueryPort` (interface), not `ExpenseQueryPortImpl` (class).

### Spring-Specific
- **Constructor injection** (no `@Autowired` on fields) — dependencies explicit and immutable.
- `@Value` or `@ConfigurationProperties` for configurable values (e.g., `app.default-page-size=20`); never hardcode in code.
- `@Transactional` only on services, with `readOnly = true` on read methods, no unnecessary propagation.
- `@RestControllerAdvice` + `@ExceptionHandler` for centralized HTTP error handling (already in `GlobalExceptionHandler`).
- Bean Validation (`@Valid`, `@NotNull`, `@NotBlank`, `@DecimalMin`) on DTO request classes, not on entities.

### Error Handling
- Throw domain-specific exceptions: `ResourceNotFoundException`, `ConflictException` — never generic `RuntimeException`.
- `GlobalExceptionHandler` maps domain exceptions to HTTP codes: 400, 404, 409, 500.
- Never expose stack traces to the client; log internally, return user-friendly messages.
- Validate preconditions at the top of each method with guard clauses.

### Configurable Values
- `MAX_PAGE_SIZE` (100), `DEFAULT_PAGE_SIZE` (20), `DEFAULT_CURRENCY` ("USD") in `application.yml`.
- Group related config with `@ConfigurationProperties` (e.g., `BudgetProperties`), not scattered `@Value`.
- H2 file path, server port, JPA settings already in `application.yml` — keep them there.
- NEVER hardcode magic numbers, URLs, or credentials in source code.

### Testing
- Tests named `should_expectedBehavior_when_condition()` — e.g., `shouldReturn404_whenBudgetNotFound()`
- AAA: Arrange → Act → Assert, never mix phases.
- One test = one concept; mock only external dependencies, never the system under test.
- `@WebMvcTest(BudgetController.class)` for controller layer; `@ExtendWith(MockitoExtension.class)` for service unit tests.
- No `@SpringBootTest` for unit tests — use it only for full integration smoke tests if needed.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and toolchain configuration

- [ ] T001 Create `backend/` directory structure per the layout above
- [ ] T002 Initialize Gradle project with `build.gradle` — Spring Boot plugin, dependencies (spring-boot-starter-web, spring-boot-starter-data-jpa, h2, lombok, spring-boot-starter-validation, spring-boot-starter-test)
- [ ] T003 Create `BudgetApplication.java` — Spring Boot entry point with `@SpringBootApplication`
- [ ] T004 Configure `application.yml` — H2 file-based datasource (`jdbc:h2:file:./data/budgetdb`), JPA `ddl-auto: update`, H2 console enabled, server port 8080
- [ ] T005 Create `data.sql` — seed the 9 default categories (Food, Transport, Entertainment, Housing, Health, Education, Shopping, Utilities, Other) using `INSERT ... WHERE NOT EXISTS` to be idempotent
- [ ] T006 Update `.gitignore` — add H2 data directory (`data/`), Gradle build (`build/`), IDE files (`.idea/`, `*.iml`), keep existing Java patterns

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared error handling and port interface that all modules depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T007 Create `shared/exception/ResourceNotFoundException.java` — extends `RuntimeException`, takes entity name + id
- [ ] T008 Create `shared/exception/ConflictException.java` — extends `RuntimeException`, takes message
- [ ] T009 Create `shared/exception/GlobalExceptionHandler.java` — `@RestControllerAdvice`, maps exceptions to RFC 7807-style responses:
  - `ResourceNotFoundException` → 404
  - `MethodArgumentNotValidException` → 400
  - `ConflictException` → 409
  - `DataIntegrityViolationException` → 409
  - `Exception` → 500
- [ ] T010 Create `shared/config/CorsConfig.java` — allow all origins for local dev (will be tightened in PH7 when React frontend is built)
- [ ] T011 Define `budgetcore/domain/port/ExpenseQueryPort.java` — interface with methods needed by downstream modules:
  - `Page<Expense> findExpenses(Long budgetId, ExpenseFilter filter, Pageable pageable)`
  - `List<CategorySummary> summarizeByCategory(Long budgetId, LocalDate start, LocalDate end)`
  - `List<MonthlySummary> summarizeByMonth(Long budgetId, int year, LocalDate start, LocalDate end)`
  - `BigDecimal sumExpensesByPeriod(Long budgetId, LocalDate start, LocalDate end)`
  - `List<CategoryMonthlySpending> getCategoryMonthlySpending(Long budgetId)`

**Checkpoint**: Foundation ready — user story implementation can begin

---

## Phase 3: User Story 1 — Create and Manage Budgets (Priority: P1) 🎯 MVP

**Goal**: Users can create, read, update, and delete budgets

**Independent Test**: Start backend, `curl POST /api/budgets`, then `GET`, `PUT`, `DELETE`. Verify via `MockMvc` integration tests.

### Domain

- [ ] T012 [US1] Create `Budget.java` — JPA entity with `@Entity`, fields: `id` (Long, `@Id @GeneratedValue`), `name` (String, `@NotBlank`), `totalAmount` (BigDecimal, `@DecimalMin("0.01")`), `currency` (String, `@NotBlank`, default "USD"), `createdAt` (LocalDateTime), `updatedAt` (LocalDateTime). Use `@PrePersist` / `@PreUpdate` for timestamps.

### Persistence

- [ ] T013 [US1] Create `BudgetRepository.java` — `@Repository` interface extending `JpaRepository<Budget, Long>`

### DTOs

- [ ] T014 [US1] Create `BudgetRequest.java` — record with `@NotBlank String name`, `@DecimalMin("0.01") BigDecimal totalAmount`, `String currency`
- [ ] T015 [US1] Create `BudgetResponse.java` — record: id, name, totalAmount, currency, remainingAmount (computed), createdAt

### Application

- [ ] T016 [US1] Create `BudgetService.java` — `@Service`, methods:
  - `create(BudgetRequest)` → `BudgetResponse` — save entity, return response
  - `getById(Long)` → `BudgetResponse` — find or throw `ResourceNotFoundException`
  - `getAll()` → `List<BudgetResponse>` — findAll, map to response
  - `update(Long, BudgetRequest)` → `BudgetResponse` — find, update fields, save
  - `delete(Long)` → void — find or throw, delete (cascade handled by JPA)

### REST

- [ ] T017 [US1] Create `BudgetController.java` — `@RestController @RequestMapping("/api/budgets")`, endpoints:
  - `POST /` → 201
  - `GET /` → 200
  - `GET /{id}` → 200
  - `PUT /{id}` → 200
  - `DELETE /{id}` → 204

### Tests

- [ ] T018 [US1] Create `BudgetControllerTest.java` — `@WebMvcTest(BudgetController.class)`, MockMvc tests for all 5 endpoints covering success + 404 + 400 cases
- [ ] T019 [US1] Create `BudgetServiceTest.java` — unit tests with mocked repository

**Checkpoint**: Budget CRUD fully functional. Test with `./gradlew test --tests "*BudgetControllerTest"`

---

## Phase 4: User Story 3 — Manage Custom Categories (Priority: P2)

**Goal**: Users can create, rename, and delete categories. Deletion fails if category is referenced by any expense.

**Why before US2**: Categories must exist before expenses can be created (expenses reference categoryId).

**Independent Test**: CRUD calls against `/api/categories` with MockMvc. Delete a category with expenses → 409.

### Domain

- [ ] T020 [US3] Create `Category.java` — JPA entity, fields: `id` (Long, `@Id @GeneratedValue`), `name` (String, `@NotBlank`, `@Column(unique = true)`). Add `@OneToMany(mappedBy = "category")` for validation of deletion constraint.

### Persistence

- [ ] T021 [US3] Create `CategoryRepository.java` — extends `JpaRepository<Category, Long>`, add `existsByNameIgnoreCase(String name)`

### DTOs

- [ ] T022 [US3] Create `CategoryRequest.java` — record with `@NotBlank String name`
- [ ] T023 [US3] Create `CategoryResponse.java` — record: id, name

### Application

- [ ] T024 [US3] Create `CategoryService.java` — `@Service`, methods:
  - `create(CategoryRequest)` → `CategoryResponse` — check duplicate name (409), save
  - `getAll()` → `List<CategoryResponse>`
  - `update(Long, CategoryRequest)` → `CategoryResponse` — check duplicate, update
  - `delete(Long)` → void — find, check if has expenses (409), delete

### REST

- [ ] T025 [US3] Create `CategoryController.java` — `@RestController @RequestMapping("/api/categories")`

### Tests

- [ ] T026 [US3] Create `CategoryControllerTest.java` — MockMvc tests: CRUD success, duplicate name → 409, delete in-use → 409
- [ ] T027 [US3] Create `CategoryServiceTest.java`

**Checkpoint**: Categories CRUD working. Test with `./gradlew test --tests "*CategoryControllerTest"`

---

## Phase 5: User Story 2 — Record Expenses Against a Budget (Priority: P1)

**Goal**: Users can create, read, update, and delete expenses linked to a budget and category.

**Depends on**: Budgets (Phase 3) and Categories (Phase 4) existing.

**Independent Test**: With a budget and category seeded, CRUD expense endpoints. MockMvc integration tests.

### Domain

- [ ] T028 [US2] Create `Expense.java` — JPA entity, fields: `id` (Long, `@Id @GeneratedValue`), `budget` (`@ManyToOne(fetch = LAZY) Budget`), `category` (`@ManyToOne(fetch = LAZY) Category`), `amount` (BigDecimal, `@DecimalMin("0.01")`), `description` (String), `date` (LocalDate, `@NotNull`), `createdAt` (LocalDateTime). Cascade: on budget delete, expenses cascade-remove via `@OnDelete(action = CASCADE)` or JPA orphanRemoval.

### Persistence

- [ ] T029 [US2] Create `ExpenseRepository.java` — extends `JpaRepository<Expense, Long>`. Add custom queries:
  - `@Query` for filtered pagination (budgetId, startDate, endDate, categoryId, description search)
  - `@Query` for sum by budget + date range
  - `@Query` for sum by budget + category + date range
  - `@Query` for monthly spending grouped by category

### DTOs

- [ ] T030 [US2] Create `ExpenseRequest.java` — record: `@NotNull Long budgetId`, `@NotNull Long categoryId`, `@DecimalMin("0.01") BigDecimal amount`, `String description`, `@NotNull LocalDate date`
- [ ] T031 [US2] Create `ExpenseResponse.java` — record: id, budgetId, categoryId, categoryName, amount, description, date, createdAt

### Persistence (Port Implementation)

- [ ] T032 [US2] Create `ExpenseQueryPortImpl.java` — `@Repository`, implements `ExpenseQueryPort`. Delegates to `ExpenseRepository` custom queries. This is the concrete adapter that all downstream modules depend on via the port interface.

### Application

- [ ] T033 [US2] Create `ExpenseService.java` — `@Service`, methods:
  - `create(ExpenseRequest)` → `ExpenseResponse` — validate budget + category exist (404/400), save
  - `getById(Long)` → `ExpenseResponse`
  - `update(Long, ExpenseRequest)` → `ExpenseResponse`
  - `delete(Long)` → void

### REST

- [ ] T034 [US2] Create `ExpenseController.java` — `@RestController @RequestMapping("/api/expenses")`

### Tests

- [ ] T035 [US2] Create `ExpenseControllerTest.java` — MockMvc: CRUD success, non-existent budgetId → 404, non-existent categoryId → 400, amount ≤ 0 → 400
- [ ] T036 [US2] Create `ExpenseServiceTest.java`

**Checkpoint**: Full budget-core module operational. Run `./gradlew test` — all 6 test classes pass.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [ ] T037 Run full test suite: `./gradlew test`
- [ ] T038 Verify H2 console accessible at `http://localhost:8080/h2-console`
- [ ] T039 Verify `data.sql` seeds categories on clean startup
- [ ] T040 Verify cascade delete: deleting a budget removes its expenses

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup — BLOCKS all user stories
- **Budgets (Phase 3)**: Depends on Foundational
- **Categories (Phase 4)**: Depends on Foundational — can run in parallel with Phase 3
- **Expenses (Phase 5)**: Depends on Phase 3 AND Phase 4 completion
- **Polish (Phase 6)**: Depends on all stories

### Within Each Phase

- Domain entities before repositories
- Repositories before services
- Services before controllers
- Tests written alongside (per task, not after)

## Notes

- `ExpenseFilter`, `CategorySummary`, `MonthlySummary`, `CategoryMonthlySpending` are DTOs/records used by the port interface — create them inside `budgetcore/domain/port/` alongside the interface.
- `remainingAmount` on `BudgetResponse` is computed: `totalAmount - sum(expense.amount where expense.budgetId = budget.id)`. Do this in the service, not in a JPA formula.
- Use `@Transactional` on service methods that modify data.
- Currency codes are free-form strings (not a restricted enum) — this keeps the module boundary clean; the exchange module handles validation against actual API currencies.
- The `ExpenseQueryPort` interface lives in `budgetcore/domain/port/` and is implemented by `ExpenseQueryPortImpl` in `budgetcore/infrastructure/persistence/`. Downstream modules inject the interface, Spring wires the impl.
