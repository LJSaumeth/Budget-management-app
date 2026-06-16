import { useQuery } from '@tanstack/react-query';
import * as api from '@/api/endpoints';

export function useRates(base: string | null) {
  return useQuery({
    queryKey: ['exchange', 'rates', base],
    queryFn: () => api.getRates(base!),
    enabled: base !== null && base.length > 0,
  });
}

export function useConversion(
  amount: number | null,
  from: string | null,
  to: string | null,
) {
  const sameCurrency = from && to && from.toUpperCase() === to.toUpperCase();

  return useQuery({
    queryKey: ['exchange', 'convert', amount, from, to],
    queryFn: () => api.convert(amount!, from!, to!),
    enabled: amount !== null && from !== null && to !== null && amount > 0 && !!from && !!to && !sameCurrency,
  });
}
