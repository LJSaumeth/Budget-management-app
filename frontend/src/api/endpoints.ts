import { apiFetch, ApiError } from './client';
import type { Budget, BudgetRequest, Expense, ExpenseRequest, Category, CategoryRequest } from './types';

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
