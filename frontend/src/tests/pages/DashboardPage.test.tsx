import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, beforeEach } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BudgetProvider } from '@/context/BudgetContext';
import DashboardPage from '@/pages/DashboardPage';

beforeEach(() => {
  const qc = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  render(
    <QueryClientProvider client={qc}>
      <BudgetProvider>
        <BrowserRouter>
          <DashboardPage />
        </BrowserRouter>
      </BudgetProvider>
    </QueryClientProvider>,
  );
});

describe('DashboardPage', () => {
  it('renders budgets from API', async () => {
    await waitFor(() => {
      expect(screen.getByText('Monthly Budget')).toBeInTheDocument();
    });
    expect(screen.getByText(/USD\s+5[,.]000/)).toBeInTheDocument();
  });

  it('shows create form on button click', async () => {
    await waitFor(() => {
      expect(screen.getByText('Monthly Budget')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('+ New Budget'));

    expect(screen.getByText('Create Budget')).toBeInTheDocument();
    expect(screen.getByLabelText('Name')).toBeInTheDocument();
    expect(screen.getByLabelText('Total Amount')).toBeInTheDocument();
  });
});
