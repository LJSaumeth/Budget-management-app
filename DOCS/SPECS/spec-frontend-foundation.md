# Feature Specification: Frontend Foundation — Notebook Theme & Shell

**Created**: 2026-06-15

## User Scenarios & Testing

### User Story 1 - Notebook Shell & Navigation (Priority: P1)

The user opens the app and immediately perceives it as a notebook — paper-textured background, hand-written typography, and sketch-like UI elements. A sidebar or top-level navigation lets them flip between budget dashboard, exchange tools, limits, simulation, and analysis. Every view transition feels like turning a notebook page.

**Why this priority**: The shell is the first thing every user sees. Without it there is no app to navigate. The notebook aesthetic is the product's identity — it must be established first so every subsequent feature spec can reference it as given.

**Independent Test**: Launch the frontend with a static navigation shell (no backend required). Verify the paper background renders, the color palette is correct, the navigation links exist, and clicking them transitions views. The shell is testable with placeholder content in each view.

**Acceptance Scenarios**:

1. **Scenario**: App loads with notebook aesthetic
   - **Given** the user navigates to the app URL
   - **When** the page renders
   - **Then** the background is cream paper (#F7F1DE), headings use a hand-written font, and the overall feel is warm and cozy — no default browser styling remains visible

2. **Scenario**: Navigate between views
   - **Given** the app shell is loaded with the Budgets view active
   - **When** the user clicks "Exchange" in the navigation
   - **Then** the view transitions with a page-turn effect (CSS transform), and the Exchange view is displayed

3. **Scenario**: Active navigation state
   - **Given** the user is on the Budgets view
   - **When** they look at the navigation
   - **Then** the "Budgets" nav item is visually underlined with a hand-drawn line in sage green (#B0BA99)

---

### User Story 2 - Design System: Colors, Typography & Primitives (Priority: P1)

All UI elements consistently use the notebook palette and hand-drawn aesthetic. Buttons look like inked doodles, inputs like ruled paper lines, cards like sticky notes, and dividers like pen strokes. There is no visual element that breaks the notebook metaphor.

**Why this priority**: Consistency across all features depends on a shared design system. Every other frontend spec references these primitives. Without it, each feature would re-invent the look and the notebook theme would fragment.

**Independent Test**: Render a component gallery page showing every primitive (button, input, card, divider, chart, skeleton loader, empty state, error state). Verify they all use only the 4-color palette and hand-drawn styling. No backend required.

**Acceptance Scenarios**:

1. **Scenario**: Button styles
   - **Given** a primary action button exists
   - **When** the user sees it
   - **Then** the button has a sketch-like outline (uneven border-radius, slight rotation), warm brown text (#9D6638) on sage green (#B0BA99) background, and a hand-written font. On hover, it slightly darkens like a fresh pen stroke.

2. **Scenario**: Form inputs
   - **Given** a form with text and number inputs
   - **When** the user views the form
   - **Then** each input field looks like a ruled notebook line — thin horizontal line in warm brown (#9D6638) with no visible border box. The placeholder text is in the hand-written font, slightly lighter. On focus, a small hand-drawn underline appears.

3. **Scenario**: Cards and containers
   - **Given** content is placed in a card
   - **When** the card renders
   - **Then** it has a faint cream-to-sage gradient background simulating a sticky note, with a slightly irregular border (achieved via CSS filter or SVG filter) and a subtle paper shadow. Corners are gently curved, not sharp.

4. **Scenario**: Dividers
   - **Given** sections within a view need separation
   - **When** the divider renders
   - **Then** it appears as a hand-drawn horizontal line — a thin SVG path with slight wiggle, in warm brown (#9D6638) at partial opacity

5. **Scenario**: Charts
   - **Given** a chart renders (bar, donut, or progress)
   - **When** the user views it
   - **Then** chart strokes have a rough/sketch quality (achieved via SVG rough-js or CSS tricks), using the notebook palette colors. No glossy gradients or perfect geometric shapes.

---

### User Story 3 - Shared State Patterns: Loading, Empty & Error (Priority: P1)

Every view must handle three non-ideal states gracefully and consistently: loading (skeleton placeholders), empty (friendly illustration with call-to-action), and error (themed message with retry). No blank screens, no browser alerts, no unstyled error text.

**Why this priority**: These states occur in every feature. Defining them once ensures every feature spec can reference "display the standard loading skeleton" or "show the standard empty state" without re-specifying the design.

**Independent Test**: Manually trigger each state in any view. Verify the skeleton uses hand-drawn dotted outlines, the empty state shows the notebook illustration + CTA, and the error shows the paper-tear graphic + retry button. Testable with mocked API delays and failures.

**Acceptance Scenarios**:

1. **Scenario**: Loading state
   - **Given** the app is fetching data from the backend
   - **When** the user waits for content
   - **Then** skeleton placeholders render as dashed/dotted outlines of the expected content shape in warm brown on cream background, with a subtle ink-fill animation when data arrives — no spinning circles

2. **Scenario**: Empty state
   - **Given** a view has no data to display (e.g., no budgets exist)
   - **When** the view renders
   - **Then** a centered illustration of an empty notebook page or quill appears, with descriptive text in hand-written font and a prominent call-to-action button. The message is friendly and encouraging, not clinical.

3. **Scenario**: Error state
   - **Given** the backend API returns an error (500, 503, network failure)
   - **When** the view fails to load
   - **Then** a paper-tear illustration appears at the top of the view with the message "Something came unbound — let's try again" and a retry button styled as a paperclip. The error never shows raw status codes or stack traces.

4. **Scenario**: Form validation errors
   - **Given** the user submits a form with invalid data
   - **When** validation fails
   - **Then** errors appear as small red-ink annotations near the offending field, styled like a teacher's correction mark with a hand-written font. The field's underline turns to red.

---

### Edge Cases

- What happens when the browser doesn't support the chosen hand-written font? The Tailwind config falls back to a system serif font that still reads as "warm" (Georgia or similar).
- What happens on high-contrast / accessibility mode? The hand-drawn borders must still meet 3:1 contrast minimum against the cream background. Text in #4E220F on #F7F1DE achieves ~10:1.
- What about dark mode? Out of scope for v1 — the notebook metaphor only works in light mode. A system-level "prefers-color-scheme: dark" will be ignored.

## Requirements

### Functional Requirements

- **FR-FND-001**: System MUST use a 4-color palette: #F7F1DE (paper background), #B0BA99 (sage accent/green), #9D6638 (warm brown for strokes/borders), #4E220F (dark brown for primary text)
- **FR-FND-002**: System MUST load and apply a hand-written Google Font (Patrick Hand or Indie Flower) as the primary heading and UI text font
- **FR-FND-003**: System MUST provide a responsive sidebar or tab navigation with links to all feature views: Budgets, History, Exchange, Limits, Simulation, Analysis
- **FR-FND-004**: View transitions MUST use a CSS page-turn effect (3D transform with perspective) when navigating between views
- **FR-FND-005**: Buttons MUST have irregular/sketch-like borders, sage background, and warm brown text
- **FR-FND-006**: Form inputs MUST render as single ruled notebook lines with no visible border box
- **FR-FND-007**: Cards MUST have a subtle sticky-note appearance with irregular borders and paper shadow
- **FR-FND-008**: Section dividers MUST render as hand-drawn horizontal lines (rough SVG path)
- **FR-FND-009**: Charts and data visualization elements MUST use rough/sketch-style strokes, not perfect geometric shapes
- **FR-FND-010**: Loading skeletons MUST render as dashed/dotted outlines with ink-fill transition on data arrival
- **FR-FND-011**: Empty states MUST show a notebook-themed illustration, descriptive text, and a CTA button
- **FR-FND-012**: Error states MUST show a paper-tear graphic, friendly message, and retry action — never raw error details
- **FR-FND-013**: Form validation errors MUST appear as red-ink annotations near the offending field
- **FR-FND-014**: The app MUST be responsive from 320px to 1920px, with the notebook layout adapting naturally

### Key Entities

- **NotebookTheme**: CSS custom properties / Tailwind config defining colors, fonts, border-radius curves, shadow tokens
- **NavigationState**: currentView, previousView (for transition direction), activeNavItem
- **SharedComponent**: NotebookButton, RuledInput, StickyNoteCard, HandDrawnDivider, SketchChart wrapper, SkeletonLoader, EmptyState, ErrorState, ValidationHint

## Success Criteria

### Measurable Outcomes

- **SC-FND-001**: A new developer can implement a new feature view (e.g., a new form + card) using only the shared primitives without writing custom styling — the notebook theme is fully inherited
- **SC-FND-002**: The app renders its first paint (navigation shell + background) in under 1.5 seconds on a 3G connection
- **SC-FND-003**: Every view in the app (7 features) achieves visual consistency — a screenshot of any two views side-by-side is immediately recognizable as the same notebook application
- **SC-FND-004**: 100% of API error states display a themed message instead of a blank white screen or browser alert
