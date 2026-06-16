import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiFetch } from '@/api/client';

export function useApiQuery<T>(key: unknown[], url: string) {
  return useQuery<T>({
    queryKey: key,
    queryFn: () => apiFetch<T>(url),
  });
}

export function useApiMutation<T, V = unknown>(
  method: string,
  url: string,
  invalidateKeys?: unknown[][],
) {
  const queryClient = useQueryClient();

  return useMutation<T, Error, V>({
    mutationFn: (body) =>
      apiFetch<T>(url, {
        method,
        body: body ? JSON.stringify(body) : undefined,
      }),
    onSuccess: () => {
      if (invalidateKeys) {
        invalidateKeys.forEach((key) =>
          queryClient.invalidateQueries({ queryKey: key }),
        );
      }
    },
  });
}
