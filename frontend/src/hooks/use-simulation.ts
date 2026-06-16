import { useMutation } from '@tanstack/react-query';
import * as api from '@/api/endpoints';
import type { SimulationRequest, SimulationResult } from '@/api/types';

export function useSimulation() {
  return useMutation<SimulationResult, Error, SimulationRequest>({
    mutationFn: (data) => api.simulateSavings(data),
  });
}
