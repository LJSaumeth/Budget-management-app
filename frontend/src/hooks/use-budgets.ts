import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import * as api from '@/api/endpoints';
import type { BudgetRequest } from '@/api/types';

export function useBudgets() {
  return useQuery({
    queryKey: ['budgets'],
    queryFn: api.getBudgets,
  });
}

export function useBudget(id: number | null) {
  return useQuery({
    queryKey: ['budgets', id],
    queryFn: () => api.getBudget(id!),
    enabled: id !== null,
  });
}

export function useCreateBudget() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: BudgetRequest) => api.createBudget(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['budgets'] }),
  });
}

export function useUpdateBudget() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: BudgetRequest }) =>
      api.updateBudget(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['budgets'] }),
  });
}

export function useDeleteBudget() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.deleteBudget(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['budgets'] }),
  });
}
