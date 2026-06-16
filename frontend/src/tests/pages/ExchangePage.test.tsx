import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import ExchangePage from '@/pages/ExchangePage';

function renderPage() {
  const qc = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return render(
    <QueryClientProvider client={qc}>
      <ExchangePage />
    </QueryClientProvider>,
  );
}

function clickConvert() {
  const buttons = screen.getAllByText('Convert');
  const formBtn = buttons.find((b) => b.tagName === 'BUTTON' && b.textContent === 'Convert' && !b.className.includes('text-lg'));
  fireEvent.click(formBtn ?? buttons[buttons.length - 1]);
}

describe('ExchangePage', () => {
  it('renders converter form by default', () => {
    renderPage();
    expect(screen.getByText('Currency Converter')).toBeInTheDocument();
  });

  it('switches to rates tab', () => {
    renderPage();
    fireEvent.click(screen.getByText('Browse Rates'));
    expect(screen.getByText('View Rates')).toBeInTheDocument();
  });

  it('shows conversion result on submit', async () => {
    renderPage();
    const amountInput = screen.getByLabelText('Amount');
    fireEvent.change(amountInput, { target: { value: '100' } });
    clickConvert();

    await waitFor(() => {
      expect(screen.getByText('Exchange Slip')).toBeInTheDocument();
    });
  });

  it('shows same-currency result instantly', async () => {
    renderPage();
    const amountInput = screen.getByLabelText('Amount');
    const toInput = screen.getByLabelText('To');
    fireEvent.change(amountInput, { target: { value: '100' } });
    fireEvent.change(toInput, { target: { value: 'USD' } });
    clickConvert();

    await waitFor(() => {
      expect(screen.getByText('Same currency — no rate lookup needed.')).toBeInTheDocument();
    });
  });

  it('loads rates grid on view rates', async () => {
    renderPage();
    fireEvent.click(screen.getByText('Browse Rates'));
    fireEvent.click(screen.getByText('View Rates'));

    await waitFor(() => {
      expect(screen.getByText('EUR')).toBeInTheDocument();
      expect(screen.getByText('GBP')).toBeInTheDocument();
      expect(screen.getByText('JPY')).toBeInTheDocument();
    });
  });
});
