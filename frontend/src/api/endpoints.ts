import { apiFetch, ApiError } from './client';
import type { Budget, BudgetRequest, Expense, ExpenseRequest, Category, CategoryRequest, ConversionResult, RatesResponse, ExpenseHistoryPage, CategorySummaryItem, MonthlySummaryItem, BudgetLimit, LimitRequest, LimitStatus } from './types';

export function getBudgets(): Promise<Budget[]> {
  return apiFetch<Budget[]>('/budgets');
}

export function getBudget(id: number): Promise<Budget> {
  return apiFetch<Budget>(`/budgets/${id}`);
}

export function createBudget(data: BudgetRequest): Promise<Budget> {
  return apiFetch<Budget>('/budgets', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export function updateBudget(id: number, data: BudgetRequest): Promise<Budget> {
  return apiFetch<Budget>(`/budgets/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

export function deleteBudget(id: number): Promise<void> {
  return apiFetch<void>(`/budgets/${id}`, {
    method: 'DELETE',
  });
}

export function getExpenses(budgetId: number): Promise<Expense[]> {
  return apiFetch<Expense[]>(`/expenses?budgetId=${budgetId}`);
}

export function createExpense(data: ExpenseRequest): Promise<Expense> {
  return apiFetch<Expense>('/expenses', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export function updateExpense(id: number, data: ExpenseRequest): Promise<Expense> {
  return apiFetch<Expense>(`/expenses/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

export function deleteExpense(id: number): Promise<void> {
  return apiFetch<void>(`/expenses/${id}`, {
    method: 'DELETE',
  });
}

export function getCategories(): Promise<Category[]> {
  return apiFetch<Category[]>('/categories');
}

export function createCategory(data: CategoryRequest): Promise<Category> {
  return apiFetch<Category>('/categories', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export { ApiError };

export function getRates(base: string): Promise<RatesResponse> {
  return apiFetch<RatesResponse>(`/exchange/rates?base=${encodeURIComponent(base)}`);
}

export function convert(amount: number, from: string, to: string): Promise<ConversionResult> {
  return apiFetch<ConversionResult>(
    `/exchange/convert?amount=${amount}&from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`,
  );
}

export function getExpenseHistory(
  budgetId: number,
  filter?: { startDate?: string; endDate?: string; categoryId?: number; search?: string },
  page = 0,
  size = 20,
): Promise<ExpenseHistoryPage> {
  const params = new URLSearchParams({ budgetId: String(budgetId), page: String(page), size: String(size) });
  if (filter?.startDate) params.set('startDate', filter.startDate);
  if (filter?.endDate) params.set('endDate', filter.endDate);
  if (filter?.categoryId) params.set('categoryId', String(filter.categoryId));
  if (filter?.search) params.set('search', filter.search);
  return apiFetch<ExpenseHistoryPage>(`/expenses/history?${params}`);
}

export function getCategorySummary(budgetId: number, start?: string, end?: string): Promise<CategorySummaryItem[]> {
  const params = new URLSearchParams({ budgetId: String(budgetId), groupBy: 'category' });
  if (start) params.set('startDate', start);
  if (end) params.set('endDate', end);
  return apiFetch<CategorySummaryItem[]>(`/expenses/summary?${params}`);
}

export function getMonthlySummary(budgetId: number, year: number, start?: string, end?: string): Promise<MonthlySummaryItem[]> {
  const params = new URLSearchParams({ budgetId: String(budgetId), groupBy: 'month', year: String(year) });
  if (start) params.set('startDate', start);
  if (end) params.set('endDate', end);
  return apiFetch<MonthlySummaryItem[]>(`/expenses/summary?${params}`);
}

export function getLimits(budgetId: number): Promise<BudgetLimit[]> {
  return apiFetch<BudgetLimit[]>(`/limits?budgetId=${budgetId}`);
}

export function getLimitStatus(limitId: number): Promise<LimitStatus> {
  return apiFetch<LimitStatus>(`/limits/${limitId}/status`);
}

export function createLimit(data: LimitRequest): Promise<BudgetLimit> {
  return apiFetch<BudgetLimit>('/limits', { method: 'POST', body: JSON.stringify(data) });
}

export function updateLimit(id: number, data: LimitRequest): Promise<BudgetLimit> {
  return apiFetch<BudgetLimit>(`/limits/${id}`, { method: 'PUT', body: JSON.stringify(data) });
}

export function deleteLimit(id: number): Promise<void> {
  return apiFetch<void>(`/limits/${id}`, { method: 'DELETE' });
}
