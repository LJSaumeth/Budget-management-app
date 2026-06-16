import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import * as api from '@/api/endpoints';
import type { ExpenseRequest } from '@/api/types';

export function useExpenses(budgetId: number | null) {
  return useQuery({
    queryKey: ['expenses', budgetId],
    queryFn: () => api.getExpenses(budgetId!),
    enabled: budgetId !== null,
  });
}

export function useCreateExpense() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: ExpenseRequest) => api.createExpense(data),
    onSuccess: (_, vars) => {
      qc.invalidateQueries({ queryKey: ['expenses', vars.budgetId] });
      qc.invalidateQueries({ queryKey: ['budgets'] });
    },
  });
}

export function useUpdateExpense() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: ExpenseRequest }) =>
      api.updateExpense(id, data),
    onSuccess: (_, vars) => {
      qc.invalidateQueries({ queryKey: ['expenses', vars.data.budgetId] });
      qc.invalidateQueries({ queryKey: ['budgets'] });
    },
  });
}

export function useDeleteExpense() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (vars: { id: number; budgetId: number }) =>
      api.deleteExpense(vars.id),
    onSuccess: (_, vars) => {
      qc.invalidateQueries({ queryKey: ['expenses', vars.budgetId] });
      qc.invalidateQueries({ queryKey: ['budgets'] });
    },
  });
}
