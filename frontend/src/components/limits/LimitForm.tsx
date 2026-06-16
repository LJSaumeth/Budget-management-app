import { useState } from 'react';
import { useBudget } from '@/context/BudgetContext';
import { useCreateLimit } from '@/hooks/use-limits';
import type { LimitRequest } from '@/api/types';
import RuledInput from '@/components/ui/RuledInput';
import NotebookButton from '@/components/ui/NotebookButton';
import ValidationHint from '@/components/ui/ValidationHint';

export default function LimitForm({ onDone }: { onDone: () => void }) {
  const { selectedBudgetId } = useBudget();
  const createLimit = useCreateLimit();
  const [amount, setAmount] = useState('');
  const [period, setPeriod] = useState<LimitRequest['period']>('MONTHLY');
  const [threshold, setThreshold] = useState('80');
  const [error, setError] = useState('');

  const handleSubmit = () => {
    if (!amount || parseFloat(amount) <= 0) {
      setError('Amount must be greater than 0');
      return;
    }
    createLimit.mutate(
      {
        budgetId: selectedBudgetId!,
        amount: parseFloat(amount),
        period,
        warningThresholdPercent: parseInt(threshold) || 80,
      },
      {
        onSuccess: () => {
          setAmount('');
          onDone();
        },
      },
    );
  };

  return (
    <div className="p-4 bg-paper rounded-card border border-brown/20 mb-4">
      <h3 className="font-hand text-xl text-brown mb-3">Set Spending Limit</h3>
      <div className="flex flex-wrap gap-3 items-end">
        <RuledInput
          label="Amount"
          type="number"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          className="w-32"
        />
        <div className="flex flex-col gap-1">
          <label className="font-hand text-brown text-sm">Period</label>
          <div className="flex gap-1">
            {(['WEEKLY', 'MONTHLY', 'ANNUAL'] as const).map((p) => (
              <button
                key={p}
                className={`font-hand text-sm px-2 py-1 rounded-sketch cursor-pointer transition-colors
                  ${period === p ? 'bg-sage text-paper' : 'text-brown border border-brown hover:bg-sage/30'}`}
                onClick={() => setPeriod(p)}
              >
                {p[0] + p.slice(1).toLowerCase()}
              </button>
            ))}
          </div>
        </div>
        <RuledInput
          label="Warning %"
          type="number"
          value={threshold}
          onChange={(e) => setThreshold(e.target.value)}
          className="w-24"
        />
        <NotebookButton onClick={handleSubmit} loading={createLimit.isPending}>
          Set Limit
        </NotebookButton>
      </div>
      {error && <ValidationHint message={error} />}
    </div>
  );
}
