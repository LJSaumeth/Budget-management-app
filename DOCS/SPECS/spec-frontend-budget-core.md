# Feature Specification: Frontend Budget Core — Dashboard & Expense Ledger

**Created**: 2026-06-15

## User Scenarios & Testing

### User Story 1 - Budget Dashboard (Notebook Index) (Priority: P1)

The user lands on the dashboard and sees all their budgets displayed as notebook-style cards. Each card shows the budget name, total amount, currency, amount spent, and remaining balance. The user can create a new budget page or open an existing one.

**Why this priority**: Budgets are the root entity. Every other view nests inside a budget context. Without this view, the app has no data to display and no navigation target for other features.

**Independent Test**: Start the frontend against a running backend with seeded budgets. Verify budget cards render with notebook styling. Create a budget via the UI form, confirm it appears. Click a card to navigate into the budget. Delete a budget and confirm removal. Delivers value as a standalone budget manager.

**Acceptance Scenarios**:

1. **Scenario**: View all budgets on landing
   - **Given** 3 budgets exist in the backend
   - **When** the user opens the app
   - **Then** 3 sticky-note cards render on the cream paper background, each showing name, currency code, and a mini progress bar (spent vs total) using the hand-drawn style

2. **Scenario**: Create a new budget
   - **Given** the user is on the dashboard
   - **When** they click the "+ New Budget" button
   - **Then** an inline form appears with ruled-line inputs for name, total amount, and currency (defaults to USD). On submit, a new sticky-note card appears with a subtle page-appear animation.

3. **Scenario**: Edit a budget
   - **Given** a budget card named "Monthly Budget" exists
   - **When** the user clicks the edit (pen) icon on the card
   - **Then** the card flips or expands inline to show editable fields. On save, the card updates.

4. **Scenario**: Navigate into a budget
   - **Given** a budget card exists
   - **When** the user clicks the card body
   - **Then** the view transitions with a page-turn animation to the budget's expense ledger view

5. **Scenario**: Delete a budget
   - **Given** a budget card exists
   - **When** the user clicks the delete icon
   - **Then** a confirmation appears (hand-written note style), and on confirm the card fades and is removed

6. **Scenario**: Empty dashboard
   - **Given** no budgets exist
   - **When** the app loads
   - **Then** the standard empty state renders: open notebook illustration, "Your notebook is empty — create your first budget to get started", and a prominent "+ New Budget" button

---

### User Story 2 - Expense Ledger (Budget Detail Page) (Priority: P1)

The user opens a budget and sees a notebook page showing the budget header (name, amounts, balance) and a list of expenses styled as handwritten ledger entries. They can add, edit, and delete expenses. The balance updates reactively after each change.

**Why this priority**: Expense tracking is the core daily action. After creating a budget, the user immediately needs to log spending. Without this view, the budget manager is just a name holder.

**Independent Test**: Open a budget with pre-seeded expenses. Verify ledger renders with notebook styling. Add an expense via the form, confirm it appears and balance updates. Edit an amount, confirm the update. Delete an expense, confirm removal. Testable with a single budget.

**Acceptance Scenarios**:

1. **Scenario**: View budget detail header
   - **Given** the user opens budget "Monthly Budget" ($5000 total, $1500 spent)
   - **When** the page loads
   - **Then** a hand-drawn header area shows: budget name in large hand-written text, "Spent $1,500 / $5,000" with a mini progress sketch, and "$3,500 remaining" highlighted in sage green

2. **Scenario**: View expenses as ledger rows
   - **Given** the budget has 5 expenses across categories
   - **When** the ledger renders
   - **Then** each expense appears as a row with hand-drawn line separators, showing date (left), category tag in sage green, description, and amount (right-aligned). Rows alternate with subtle paper-toned shading.

3. **Scenario**: Add an expense
   - **Given** the user is on the budget page
   - **When** they click "+ Add Expense"
   - **Then** an inline form unfolds like paper: ruled-line inputs for amount, a category dropdown (stylized as a paper slip), a description field, and date. On submit, the new expense appears at the top with an ink-write animation and the balance header updates.

4. **Scenario**: Edit an expense
   - **Given** an expense exists in the ledger
   - **When** the user clicks the edit (pen) icon
   - **Then** the row becomes editable inline with ruled-line inputs. Changes save on blur or Enter, and the balance header updates reactively.

5. **Scenario**: Delete an expense
   - **Given** an expense exists in the ledger
   - **When** the user clicks the delete (crossed-out) icon
   - **Then** the row fades out, the balance header updates, and a subtle paper-tear animation plays

6. **Scenario**: Empty ledger
   - **Given** a budget has no expenses yet
   - **When** the user opens it
   - **Then** the ledger area shows "No expenses yet — start logging your spending" with a prominent "+ Add Expense" CTA, styled as an empty notebook page waiting to be filled

---

### User Story 3 - Category Management (Priority: P2)

The user views available categories and can add custom categories for their budgets. Pre-seeded categories (Food, Transport, Entertainment, Housing, Health, Education, Shopping, Utilities, Other) are always available.

**Why this priority**: Categories are needed for expense categorization but the system comes with 9 pre-seeded defaults. Custom category creation is a convenience, not a blocker.

**Independent Test**: View the category list. Add a new custom category. Verify it appears in the expense form dropdown. Testable with a running backend.

**Acceptance Scenarios**:

1. **Scenario**: View pre-seeded categories
   - **Given** the backend has 9 default categories seeded
   - **When** the user opens the Categories section
   - **Then** all 9 categories appear as small tags with hand-drawn borders, each with a distinct color from the extended notebook palette (muted variations of the 4 core colors)

2. **Scenario**: Add a custom category
   - **Given** the user needs a "Pets" category
   - **When** they type "Pets" in the add-category input and submit
   - **Then** a new category tag appears in the list, and "Pets" is available in the expense form's category dropdown

---

### Edge Cases

- What happens when the user tries to delete a budget that has expenses? The backend cascades the deletion. The frontend shows a confirmation warning: "This will delete the budget and all its expenses and limits. This can't be undone."
- What happens when the user adds an expense that exceeds the budget? The remaining balance turns negative and renders in deep brown (#4E220F) with a subtle underline, but the expense is still recorded.
- What happens on very large expense lists? The ledger implements infinite scroll or pagination (default 20 per page), with a "Load more" button styled as a torn-paper edge at the bottom.

## Requirements

### Functional Requirements

- **FR-BC-001**: System MUST display all budgets as sticky-note cards on the dashboard, each showing name, totalAmount, currency, spentAmount, and remainingAmount
- **FR-BC-002**: Users MUST be able to create a budget with name, totalAmount, and currency via a ruled-line form
- **FR-BC-003**: Users MUST be able to edit a budget's name and totalAmount inline from the dashboard
- **FR-BC-004**: Users MUST be able to delete a budget with a confirmation note
- **FR-BC-005**: Clicking a budget card MUST navigate to that budget's expense ledger with a page-turn transition
- **FR-BC-006**: The budget detail header MUST show name, total spent, total amount, and remaining balance with a hand-drawn progress indicator
- **FR-BC-007**: System MUST render expenses as notebook ledger rows with date, category tag, description, and amount
- **FR-BC-008**: Users MUST be able to add an expense with amount, category, description, and date via an inline folding form
- **FR-BC-009**: Users MUST be able to edit an expense inline from the ledger
- **FR-BC-010**: Users MUST be able to delete an expense with row fade-out animation
- **FR-BC-011**: The budget balance header MUST update reactively after any expense CRUD operation
- **FR-BC-012**: System MUST display pre-seeded and custom categories as styled tags
- **FR-BC-013**: Users MUST be able to create custom categories
- **FR-BC-014**: The expense form's category dropdown MUST include all available categories
- **FR-BC-015**: Large expense lists MUST paginate with a styled "Load more" control
- **FR-BC-016**: All views MUST use the foundation notebook theme, shared loading skeletons, and shared empty/error states

### Key Entities (Frontend State)

- **BudgetCard**: id, name, totalAmount, currency, spentAmount, remainingAmount
- **ExpenseRow**: id, budgetId, categoryId, categoryName, amount, description, date
- **CategoryTag**: id, name
- **UI State**: selectedBudgetId, isCreatingBudget, isEditingBudget, editingExpenseId

## Success Criteria

### Measurable Outcomes

- **SC-BC-001**: A user can create a budget and log their first expense in under 1 minute from app launch
- **SC-BC-002**: The dashboard renders 20 budget cards without layout reflow or jank (60fps)
- **SC-BC-003**: Expense CRUD operations reflect in the UI within 500ms of the backend response
- **SC-BC-004**: The budget balance header updates without requiring a page refresh after any expense change
