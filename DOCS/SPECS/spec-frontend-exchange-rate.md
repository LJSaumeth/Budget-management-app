# Feature Specification: Frontend Exchange Rate — Currency Converter & Rates

**Created**: 2026-06-15

## User Scenarios & Testing

### User Story 1 - Convert Amount Between Currencies (Priority: P1)

The user enters an amount, selects source and target currencies, and sees the converted result rendered as a currency exchange slip tucked into the notebook. The slip shows the original amount, rate, converted amount, and when the rate was fetched.

**Why this priority**: This is the core value of the exchange feature — knowing what your budget is worth in another currency. The rates browser (P2) is a convenience view on top.

**Independent Test**: Enter 100 USD to EUR. Verify the result card shows the converted amount, rate, and timestamp. Test same-currency conversion (USD to USD). Test with the backend running and with a mocked API failure to verify error state. Testable without any budgets or expenses.

**Acceptance Scenarios**:

1. **Scenario**: Convert USD to EUR
   - **Given** the backend reports a USD-EUR rate of 0.92
   - **When** the user enters amount 100, selects USD → EUR, and clicks "Convert"
   - **Then** a currency-slip card appears: a warm brown border, showing "100.00 USD → 92.00 EUR" with the rate (0.92) and a tiny timestamp note. The card has a slight paper-curl shadow.

2. **Scenario**: Same-currency conversion (no API call)
   - **Given** the user selects USD → USD
   - **When** they click Convert
   - **Then** the result shows "100.00 USD = 100.00 USD" with rate "1.00". The fetchedAt timestamp is not displayed since no API call was made. A small hand-written note reads: "Same currency — no rate lookup needed."

3. **Scenario**: Invalid amount
   - **Given** the user enters amount 0 or a negative number
   - **When** they submit
   - **Then** the amount field shows a red-ink validation mark: "Amount must be greater than 0"

4. **Scenario**: Unsupported currency
   - **Given** the user enters XYZ as a target currency
   - **When** they submit
   - **Then** the target currency field shows a red-ink validation mark: "Unsupported currency: XYZ"

5. **Scenario**: API unavailable (503)
   - **Given** the exchange rate API is down
   - **When** the user attempts a conversion
   - **Then** the standard error state renders: paper-tear graphic, "The currency service is taking a nap — try again in a moment", and a paperclip-styled retry button

6. **Scenario**: Clear form after conversion
   - **Given** a conversion result is displayed
   - **When** the user modifies the amount or currency
   - **Then** the previous result card fades, replaced by skeleton placeholders until the new result arrives

---

### User Story 2 - Browse All Rates for a Currency (Priority: P2)

The user can view all exchange rates for a given base currency. The rates display as a collection of tiny cards, styled like international postage stamps pinned into the notebook.

**Why this priority**: Convenience view showing all rates at once. The user gets the same information via individual convert calls. Less critical than direct conversion.

**Independent Test**: Enter "USD" as the base currency. Verify a grid of rate cards appears with at least 20+ currency pairs. Each card shows the target currency code and its rate. Testable with the backend running.

**Acceptance Scenarios**:

1. **Scenario**: View rates for USD
   - **Given** the backend returns rates for USD across 150+ currencies
   - **When** the user types "USD" in the base currency field and clicks "View Rates"
   - **Then** a grid of small currency cards renders. Each card is a tiny sage-green-bordered rectangle with the currency code in large hand-written text and the rate below in smaller text. Cards arrange in a wrapping grid like postage stamps. The fetchedAt timestamp appears as a small note at the top of the grid.

2. **Scenario**: Switch base currency
   - **Given** USD rates are displayed
   - **When** the user types "EUR" and clicks View Rates
   - **Then** the card grid updates to show EUR-based rates, with a brief fade-transition

3. **Scenario**: Base currency not supported
   - **Given** the user types "ABC"
   - **When** they click View Rates
   - **Then** the red-ink validation mark appears: "Currency ABC is not supported"

4. **Scenario**: Rate grid with many currencies
   - **Given** 150+ rates exist for the base currency
   - **When** the grid renders
   - **Then** cards wrap naturally across rows. No horizontal scrolling needed. Cards maintain a consistent size regardless of currency code length.

---

### Edge Cases

- What happens when the user enters a lowercase currency code? The backend normalizes to uppercase. The frontend should transform input to uppercase automatically as the user types.
- What about cached rates? If the backend returns a cached rate (within 10-hour TTL), the fetchedAt reflects the original fetch time. The frontend displays it as a small note: "Rate from [timestamp]" — no special UI distinction needed.
- What about very long rate numbers (e.g., 0.00000013 BTC)? The rate displays with up to 8 decimal places for very small values, and 2 decimal places for standard rates. The amount field always uses 2 decimal places.

## Requirements

### Functional Requirements

- **FR-EX-001**: System MUST render a currency converter with amount, from-currency, and to-currency inputs using ruled-line inputs and paper-slip dropdowns
- **FR-EX-002**: The conversion result MUST render as a currency-slip card showing from, to, amount, rate, result, and fetchedAt
- **FR-EX-003**: Same-currency conversions MUST return the result instantly without showing a fetchedAt timestamp
- **FR-EX-004**: Currency inputs MUST auto-uppercase and validate against supported currency codes
- **FR-EX-005**: System MUST render a rates browser that displays all rates for a base currency as a postage-stamp-style card grid
- **FR-EX-006**: Each rate card MUST show the target currency code and rate value
- **FR-EX-007**: The rates grid MUST wrap responsively — 2 columns on mobile, up to 6 on desktop
- **FR-EX-008**: The fetchedAt timestamp MUST display as a small hand-written note near the result or grid header
- **FR-EX-009**: Validation errors MUST use red-ink annotation style near the offending field
- **FR-EX-010**: All API error states MUST use the standard paper-tear error component with retry
- **FR-EX-011**: The entire exchange view MUST use the foundation notebook theme

### Key Entities (Frontend State)

- **ConversionInput**: amount (BigDecimal), fromCurrency (string), toCurrency (string)
- **ConversionResult**: from, to, amount, rate, result, fetchedAt (Instant)
- **RatesView**: baseCurrency, rates map (string → BigDecimal), fetchedAt
- **UI State**: isLoading, errorMessage, lastConversion, activeBaseCurrency

## Success Criteria

### Measurable Outcomes

- **SC-EX-001**: A conversion result appears within 1 second of form submission when the backend responds normally
- **SC-EX-002**: Same-currency conversions display instantly (no network request, <50ms)
- **SC-EX-003**: The rates grid renders 100+ currency cards without layout jank or scroll lag
- **SC-EX-004**: The converter and rates browser share the same view without requiring page navigation — toggling between them is seamless
