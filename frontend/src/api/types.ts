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

export interface ExpenseHistoryPage {
  content: Expense[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface CategorySummaryItem {
  categoryId: number;
  categoryName: string;
  totalAmount: number;
  expenseCount: number;
  percentage: number;
}

export interface MonthlySummaryItem {
  month: string;
  totalAmount: number;
  expenseCount: number;
}

export interface BudgetLimit {
  id: number;
  budgetId: number;
  amount: number;
  period: 'WEEKLY' | 'MONTHLY' | 'ANNUAL';
  warningThresholdPercent: number;
  createdAt: string;
}

export interface LimitRequest {
  budgetId: number;
  amount: number;
  period: 'WEEKLY' | 'MONTHLY' | 'ANNUAL';
  warningThresholdPercent: number;
}

export interface LimitStatus {
  limitId: number;
  budgetId: number;
  limitAmount: number;
  spentAmount: number;
  remainingAmount: number;
  percentageUsed: number;
  status: 'OK' | 'WARNING' | 'EXCEEDED';
  period: string;
  periodStart: string;
  periodEnd: string;
}
