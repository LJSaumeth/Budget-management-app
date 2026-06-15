# Feature Specification: Budget Core (Budget CRUD + Expense Tracking + Categories)

**Created**: 2026-06-15

## User Scenarios & Testing

### User Story 1 - Create and Manage Budgets (Priority: P1)

The user creates a named budget with a total amount and currency, then can view, edit, or delete it. This is the foundation — nothing else works without a budget.

**Why this priority**: Budgets are the root entity. Expenses, limits, history, simulations, and analysis all reference a budget. Without budgets, the app has no data to operate on.

**Independent Test**: Create a budget via `POST /api/budgets`, retrieve it via `GET /api/budgets/{id}`, update the amount, and delete it. Verify all four operations return correct HTTP statuses and payloads. Fully testable with curl or an HTTP client against the running backend; no other module required.

**Acceptance Scenarios**:

1. **Scenario**: Create a new budget
   - **Given** the user has no budgets
   - **When** they send `POST /api/budgets` with `{ "name": "Monthly Budget", "totalAmount": 5000.00, "currency": "USD" }`
   - **Then** the system returns 201 with the created budget including a generated ID, and the budget is persisted in H2

2. **Scenario**: Retrieve a budget by ID
   - **Given** a budget with ID 1 exists
   - **When** the user sends `GET /api/budgets/1`
   - **Then** the system returns 200 with the full budget object

3. **Scenario**: List all budgets
   - **Given** three budgets exist
   - **When** the user sends `GET /api/budgets`
   - **Then** the system returns 200 with an array of all three budgets

4. **Scenario**: Update a budget
   - **Given** a budget with ID 1 and amount 5000 exists
   - **When** the user sends `PUT /api/budgets/1` with `{ "totalAmount": 6000.00 }`
   - **Then** the system returns 200 with the updated budget, and subsequent GET reflects 6000

5. **Scenario**: Delete a budget and its associated data
   - **Given** a budget with ID 1 that has expenses and limits
   - **When** the user sends `DELETE /api/budgets/1`
   - **Then** the system returns 204, the budget and all its child data (expenses, limits) are removed

---

### User Story 2 - Record Expenses Against a Budget (Priority: P1)

The user records where money was spent: amount, category, description, and date. Each expense belongs to one budget.

**Why this priority**: Expenses are the core transactional data. Without expense tracking, the app is just a budget name holder — no history, no limits checking, no simulation, no analysis.

**Independent Test**: With a budget already created, send `POST /api/expenses` to create an expense, retrieve it, update the amount, and delete it. All operations are testable against a single budget with no other module.

**Acceptance Scenarios**:

1. **Scenario**: Add an expense to a budget
   - **Given** a budget with ID 1 exists with amount 5000 USD
   - **When** the user sends `POST /api/expenses` with `{ "budgetId": 1, "categoryId": 2, "amount": 45.50, "description": "Grocery shopping", "date": "2026-06-15" }`
   - **Then** the system returns 201 with the created expense, and the budget's calculated spent amount reflects the deduction

2. **Scenario**: Add an expense to a non-existent budget
   - **Given** no budget with ID 99 exists
   - **When** the user sends `POST /api/expenses` with `{ "budgetId": 99, ... }`
   - **Then** the system returns 404 with an error message

3. **Scenario**: Add an expense with a non-existent category
   - **Given** budget ID 1 exists, but category ID 99 does not
   - **When** the user sends `POST /api/expenses` with `{ "budgetId": 1, "categoryId": 99, ... }`
   - **Then** the system returns 400 with a validation error

4. **Scenario**: Update an expense description
   - **Given** an expense with ID 1 exists
   - **When** the user sends `PUT /api/expenses/1` with `{ "description": "Updated description" }`
   - **Then** the system returns 200 with the updated expense

5. **Scenario**: Delete an expense
   - **Given** an expense with ID 1 exists
   - **When** the user sends `DELETE /api/expenses/1`
   - **Then** the system returns 204 and the expense is removed

---

### User Story 3 - Manage Custom Categories (Priority: P2)

The user creates, renames, and deletes custom spending categories (e.g., "Food", "Rent", "Subscription"). Categories are global across all budgets.

**Why this priority**: Categories enable classification of expenses, which powers history grouping, limit enforcement per category, and analysis. The feature can be deferred slightly because a hardcoded default set could work temporarily, but user-defined categories are specified.

**Independent Test**: Create a category, list all categories, rename one, and delete an unused category. Testable with CRUD calls against `/api/categories` with no dependency on budgets or expenses being present. Deleting a category that has expenses assigned should be rejected.

**Acceptance Scenarios**:

1. **Scenario**: Create a custom category
   - **Given** the default categories exist (Food, Transport, Entertainment, etc.)
   - **When** the user sends `POST /api/categories` with `{ "name": "Investments" }`
   - **Then** the system returns 201 with the created category

2. **Scenario**: List all categories
   - **Given** categories exist
   - **When** the user sends `GET /api/categories`
   - **Then** the system returns 200 with all categories

3. **Scenario**: Delete an unused category
   - **Given** a category "Investments" has no expenses associated
   - **When** the user sends `DELETE /api/categories/{id}`
   - **Then** the system returns 204

4. **Scenario**: Attempt to delete a category with existing expenses
   - **Given** a category "Food" has expenses referencing it
   - **When** the user sends `DELETE /api/categories/{id}`
   - **Then** the system returns 409 Conflict with an error message "Category is in use"

5. **Scenario**: Duplicate category name
   - **Given** a category named "Food" already exists
   - **When** the user sends `POST /api/categories` with `{ "name": "Food" }`
   - **Then** the system returns 409 Conflict

---

### Edge Cases

- What happens when a budget amount is updated to a value lower than current total expenses? The system MUST accept the update but the budget will show a negative remaining balance.
- What happens when an expense date is in the future? The system MUST accept it (user may plan future expenses).
- What happens when expense amount is zero or negative? The system MUST reject amounts ≤ 0 with a 400 validation error.
- What happens when deleting a budget currently referenced by an open simulation or analysis request? The budget is deleted and any subsequent query for that budget returns 404.
- What happens when a budget's currency is changed after expenses exist? The update is accepted. Historical expense amounts retain their original numeric values — the system does not convert existing expenses to the new currency. The user is responsible for understanding that old expenses remain in the previous currency.

## Requirements

### Functional Requirements

- **FR-BC-001**: System MUST allow creating, reading, updating, and deleting budgets
- **FR-BC-002**: System MUST allow creating, reading, updating, and deleting expenses linked to a budget
- **FR-BC-003**: System MUST allow creating, reading, updating, and deleting categories
- **FR-BC-004**: System MUST validate that an expense references an existing budget and category
- **FR-BC-005**: System MUST prevent deletion of a category that has expenses referencing it
- **FR-BC-006**: System MUST cascade-delete all expenses and limits when a budget is deleted
- **FR-BC-007**: System MUST reject expense amounts ≤ 0
- **FR-BC-008**: System MUST enforce unique category names (case-insensitive)
- **FR-BC-009**: System MUST persist all data to H2 database with schema auto-creation on startup
- **FR-BC-010**: System MUST seed default categories on first run (Food, Transport, Entertainment, Housing, Health, Education, Shopping, Utilities, Other)
- **FR-BC-011**: System MUST allow updating a budget's currency. Historical expense amounts are not converted to the new currency.

### Key Entities

- **Budget**: id (Long), name (String, required), totalAmount (BigDecimal, required, > 0), currency (String, required, default "USD"), createdAt (LocalDateTime, auto), updatedAt (LocalDateTime, auto)
- **Expense**: id (Long), budgetId (Long, FK to Budget, required), categoryId (Long, FK to Category, required), amount (BigDecimal, required, > 0), description (String), date (LocalDate, required), createdAt (LocalDateTime, auto)
- **Category**: id (Long), name (String, required, unique case-insensitive)

### API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/budgets` | Create a budget |
| GET | `/api/budgets` | List all budgets |
| GET | `/api/budgets/{id}` | Get budget by ID (includes computed remaining amount) |
| PUT | `/api/budgets/{id}` | Update a budget |
| DELETE | `/api/budgets/{id}` | Delete a budget and its children |
| POST | `/api/expenses` | Create an expense |
| GET | `/api/expenses/{id}` | Get expense by ID |
| PUT | `/api/expenses/{id}` | Update an expense |
| DELETE | `/api/expenses/{id}` | Delete an expense |
| POST | `/api/categories` | Create a category |
| GET | `/api/categories` | List all categories |
| PUT | `/api/categories/{id}` | Rename a category |
| DELETE | `/api/categories/{id}` | Delete a category (fails if in use) |

## Success Criteria

### Measurable Outcomes

- **SC-BC-001**: User can create a budget with name, amount, and currency in under 3 seconds from the frontend (p95 < 200ms backend response).
- **SC-BC-002**: User can record an expense in under 3 seconds (p95 < 200ms).
- **SC-BC-003**: All CRUD operations return correct HTTP status codes and error messages for invalid input.
- **SC-BC-004**: Deleting a budget removes all associated expenses and limits without orphaned data.
- **SC-BC-005**: Category name uniqueness is enforced regardless of casing ("FOOD" and "food" conflict).
