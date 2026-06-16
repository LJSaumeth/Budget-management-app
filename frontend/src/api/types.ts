export interface Budget {
  id: number;
  name: string;
  totalAmount: number;
  currency: string;
  spentAmount: number;
  remainingAmount: number;
  createdAt?: string;
}

export interface BudgetRequest {
  name: string;
  totalAmount: number;
  currency: string;
}

export interface Expense {
  id: number;
  budgetId: number;
  categoryId: number;
  categoryName: string;
  amount: number;
  description: string;
  date: string;
}

export interface ExpenseRequest {
  budgetId: number;
  categoryId: number;
  amount: number;
  description: string;
  date: string;
}

export interface Category {
  id: number;
  name: string;
}

export interface CategoryRequest {
  name: string;
}

export interface ConversionResult {
  from: string;
  to: string;
  amount: number;
  rate: number;
  result: number;
  fetchedAt: string;
}

export interface RatesResponse {
  base: string;
  rates: Record<string, number>;
  fetchedAt: string;
}
