import { useQuery } from '@tanstack/react-query';
import * as api from '@/api/endpoints';

export interface HistoryFilter {
  startDate?: string;
  endDate?: string;
  categoryId?: number;
  search?: string;
}

export function useExpenseHistory(budgetId: number | null, filter: HistoryFilter, page: number, size = 20) {
  return useQuery({
    queryKey: ['history', budgetId, filter, page, size],
    queryFn: () => api.getExpenseHistory(budgetId!, filter, page, size),
    enabled: budgetId !== null,
  });
}

export function useCategorySummary(budgetId: number | null, start?: string, end?: string) {
  return useQuery({
    queryKey: ['history', 'category-summary', budgetId, start, end],
    queryFn: () => api.getCategorySummary(budgetId!, start, end),
    enabled: budgetId !== null,
  });
}

export function useMonthlySummary(budgetId: number | null, year: number, start?: string, end?: string) {
  return useQuery({
    queryKey: ['history', 'monthly-summary', budgetId, year, start, end],
    queryFn: () => api.getMonthlySummary(budgetId!, year, start, end),
    enabled: budgetId !== null,
  });
}
