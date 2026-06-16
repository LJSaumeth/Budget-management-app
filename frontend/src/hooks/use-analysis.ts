import { useQuery } from '@tanstack/react-query';
import * as api from '@/api/endpoints';

export function useAnalysis(budgetId: number | null) {
  return useQuery({
    queryKey: ['analysis', budgetId],
    queryFn: () => api.getAnalysis(budgetId!),
    enabled: budgetId !== null,
    staleTime: 0,
  });
}
