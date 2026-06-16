import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import * as api from '@/api/endpoints';
import type { LimitRequest } from '@/api/types';

export function useLimits(budgetId: number | null) {
  return useQuery({
    queryKey: ['limits', budgetId],
    queryFn: () => api.getLimits(budgetId!),
    enabled: budgetId !== null,
  });
}

export function useLimitStatuses(budgetId: number | null) {
  const { data: limits } = useLimits(budgetId);

  return useQuery({
    queryKey: ['limits', 'statuses', budgetId],
    queryFn: async () => {
      if (!limits) return [];
      return Promise.all(limits.map((l) => api.getLimitStatus(l.id)));
    },
    enabled: !!limits && limits.length > 0,
  });
}

export function useCreateLimit() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: LimitRequest) => api.createLimit(data),
    onSuccess: (_, vars) => {
      qc.invalidateQueries({ queryKey: ['limits', vars.budgetId] });
      qc.invalidateQueries({ queryKey: ['limits', 'statuses', vars.budgetId] });
    },
  });
}

export function useUpdateLimit() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: LimitRequest }) => api.updateLimit(id, data),
    onSuccess: (_, vars) => {
      qc.invalidateQueries({ queryKey: ['limits', vars.data.budgetId] });
      qc.invalidateQueries({ queryKey: ['limits', 'statuses', vars.data.budgetId] });
    },
  });
}

export function useDeleteLimit() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (vars: { id: number; budgetId: number }) => api.deleteLimit(vars.id),
    onSuccess: (_, vars) => {
      qc.invalidateQueries({ queryKey: ['limits', vars.budgetId] });
      qc.invalidateQueries({ queryKey: ['limits', 'statuses', vars.budgetId] });
    },
  });
}
