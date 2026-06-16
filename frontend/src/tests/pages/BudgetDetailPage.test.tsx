import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BudgetProvider } from '@/context/BudgetContext';
import BudgetDetailPage from '@/pages/BudgetDetailPage';

function renderPage(budgetId: string) {
  const qc = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return render(
    <QueryClientProvider client={qc}>
      <BudgetProvider>
        <MemoryRouter initialEntries={[`/budgets/${budgetId}`]}>
          <Routes>
            <Route path="/budgets/:id" element={<BudgetDetailPage />} />
          </Routes>
        </MemoryRouter>
      </BudgetProvider>
    </QueryClientProvider>,
  );
}

describe('BudgetDetailPage', () => {
  it('renders budget header with data', async () => {
    renderPage('1');

    await waitFor(() => {
      expect(screen.getByText('Monthly Budget')).toBeInTheDocument();
    });

    expect(screen.getByText(/3[,.]500\s+USD\s+remaining/)).toBeInTheDocument();
  });

  it('renders expense rows', async () => {
    renderPage('1');

    await waitFor(() => {
      expect(screen.getByText('Grocery shopping')).toBeInTheDocument();
      expect(screen.getByText('Bus pass')).toBeInTheDocument();
    });
  });

  it('shows add expense button', async () => {
    renderPage('1');

    await waitFor(() => {
      expect(screen.getByText('+ Add Expense')).toBeInTheDocument();
    });
  });
});
