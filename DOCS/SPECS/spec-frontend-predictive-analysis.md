# Feature Specification: Frontend Predictive Analysis — Smart Spending Insights

**Created**: 2026-06-15

## User Scenarios & Testing

### User Story 1 - View Optimization Suggestions (Priority: P1)

The user opens the Analysis view for a budget and sees personalized suggestions on where they could cut spending. Each suggestion identifies a category where recent spending exceeds the historical average by more than 10%, shows the dollar amounts, and ranks suggestions by annual saving impact. Suggestions render as sticky notes pinned to the notebook page.

**Why this priority**: This is the entire feature — a read-only insight view. No user input beyond selecting a budget. The value is in the actionable advice, presented in a warm, encouraging notebook style rather than a clinical report.

**Independent Test**: Open the Analysis view for a budget with 3+ months of expense data where Food spending has increased. Verify at least one suggestion card appears with category name, current monthly average, suggested reduction, monthly and annual savings, and reasoning text. Testable with seeded expense data spanning multiple months.

**Acceptance Scenarios**:

1. **Scenario**: View optimization suggestions with flagged categories
   - **Given** a budget has 3 months of data where Food spending went from $500 → $520 → $620 (avg $546.67, recent $620 = 13.4% above avg)
   - **When** the user opens the Analysis view
   - **Then** a suggestion sticky note renders at the top: "Food" as the title, "$73.33/month · $879.96/year" in sage green as the savings, and the reasoning text: "Food spending ($620.00) is 13.4% above your 3-month average ($546.67). Reducing to $546.67 would save $73.33/month." Below, a small note shows the suggested reduction percentage.

2. **Scenario**: Multiple suggestions sorted by annual saving
   - **Given** Entertainment and Food are both flagged, with Entertainment having a higher annual saving ($1,200) than Food ($880)
   - **When** the suggestions render
   - **Then** the Entertainment suggestion card appears above the Food card. The sorting is visually reinforced by the higher-saving card having a slightly larger sticky-note size or a star accent.

3. **Scenario**: Maximum 5 suggestions displayed
   - **Given** 7 categories are flagged as overspent
   - **When** the suggestions render
   - **Then** only the top 5 (by annual saving) appear. A small note at the bottom reads: "Showing top 5 opportunities" in a hand-written footnote style.

4. **Scenario**: No overspending detected
   - **Given** all categories are within 10% of their historical averages
   - **When** the user opens Analysis
   - **Then** a warm message renders in the center of the page: "You're doing great! All your spending is on track." with a small hand-drawn star or check mark. The total potential annual saving displays as $0.00.

5. **Scenario**: View total potential annual saving
   - **Given** suggestions show $879.96 and $1,200.00 in annual savings
   - **When** the header area renders
   - **Then** a summary line shows "Potential Annual Savings: $2,079.96" in large hand-written text with a subtle sage green highlight

6. **Scenario**: Not enough data for analysis
   - **Given** the budget has fewer than 2 months of expense data
   - **When** the user opens Analysis
   - **Then** a friendly message: "Need at least 2 months of data to analyze your patterns. Keep logging expenses!" with a small calendar sketch

7. **Scenario**: No expenses at all
   - **Given** the budget has no expenses
   - **When** the user opens Analysis
   - **Then** the standard empty state: "No expense data available — start logging your spending to get insights"

---

### Edge Cases

- What happens when the user switches budgets? The analysis view refreshes for the new budget context with a skeleton loading state.
- What about the exact 10% threshold? A category at exactly 10% above average is NOT flagged. The frontend doesn't need special logic — it just renders whatever suggestions the backend returns.
- What about very long reasoning strings? The reasoning text wraps naturally within the sticky-note card. For extreme cases (very long category names + large numbers), the font size scales down slightly to fit.
- What about a budget with data across many months but only one category has spending? Each category still needs 2+ months of data to be analyzed. Categories with only 1 month are silently excluded by the backend; the frontend just renders returned suggestions.

## Requirements

### Functional Requirements

- **FR-AN-001**: System MUST render optimization suggestions as sticky-note cards, each showing category name, monthly saving, annual saving, and reasoning text
- **FR-AN-002**: Suggestions MUST be sorted by annual saving descending (highest first), with visual emphasis on the top suggestion
- **FR-AN-003**: System MUST display at most 5 suggestions, with a footnote when more are excluded
- **FR-AN-004**: Each suggestion card MUST show: category name (title), suggested reduction percentage, monthly saving amount, annual saving amount, and the full reasoning sentence
- **FR-AN-005**: Annual saving amounts MUST be highlighted in sage green (#B0BA99) on the cards
- **FR-AN-006**: A summary header MUST display the total potential annual saving (sum of all suggestion annual savings)
- **FR-AN-007**: When no overspending is detected, a warm encouragement message with a hand-drawn star icon MUST display
- **FR-AN-008**: When data is insufficient (fewer than 2 months), a friendly prompt to log more expenses MUST display
- **FR-AN-009**: When no expenses exist, the standard empty state MUST display
- **FR-AN-010**: Selecting a different budget MUST refresh the analysis with a loading skeleton
- **FR-AN-011**: All views MUST use the foundation notebook theme, shared loading skeletons, and shared error states

### Key Entities (Frontend State)

- **OptimizationSuggestion**: categoryId, categoryName, currentMonthlyAvg, suggestedReductionPercent, monthlySaving, annualSaving, reasoning
- **AnalysisResult**: suggestions (list, max 5), message (string), totalPotentialAnnualSaving
- **UI State**: selectedBudgetId, isLoadingAnalysis, analysisResult

## Success Criteria

### Measurable Outcomes

- **SC-AN-001**: The analysis view loads within 1 second when backend data is available (p95)
- **SC-AN-002**: A user can understand each suggestion's impact (how much they'd save) from the card alone without reading the full reasoning text
- **SC-AN-003**: The "no overspending" and "insufficient data" states are equally polished as the suggestions view — no state feels like an afterthought
- **SC-AN-004**: The view works for budgets with 2 months up to 24+ months of expense data without performance degradation
