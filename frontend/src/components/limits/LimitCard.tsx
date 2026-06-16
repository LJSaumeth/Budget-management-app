import { useState } from 'react';
import { useDeleteLimit, useUpdateLimit } from '@/hooks/use-limits';
import type { BudgetLimit, LimitStatus, LimitRequest } from '@/api/types';
import LimitStatusBar from './LimitStatusBar';
import RuledInput from '@/components/ui/RuledInput';
import NotebookButton from '@/components/ui/NotebookButton';

interface LimitCardProps {
  limit: BudgetLimit;
  status?: LimitStatus;
}

export default function LimitCard({ limit, status }: LimitCardProps) {
  const deleteLimit = useDeleteLimit();
  const updateLimit = useUpdateLimit();
  const [editing, setEditing] = useState(false);
  const [amount, setAmount] = useState(limit.amount.toString());
  const [threshold, setThreshold] = useState(limit.warningThresholdPercent.toString());

  const handleSave = () => {
    updateLimit.mutate({
      id: limit.id,
      data: {
        budgetId: limit.budgetId,
        amount: parseFloat(amount),
        period: limit.period,
        warningThresholdPercent: parseInt(threshold) || 80,
      } as LimitRequest,
    });
    setEditing(false);
  };

  return (
    <div className="p-4 bg-paper rounded-card border-l-4 border-sage shadow-sm">
      {editing ? (
        <div className="flex flex-col gap-2">
          <RuledInput label="Amount" type="number" value={amount} onChange={(e) => setAmount(e.target.value)} />
          <RuledInput label="Warning %" type="number" value={threshold} onChange={(e) => setThreshold(e.target.value)} />
          <div className="flex gap-2">
            <NotebookButton size="sm" onClick={handleSave}>Save</NotebookButton>
            <NotebookButton size="sm" variant="secondary" onClick={() => setEditing(false)}>Cancel</NotebookButton>
          </div>
        </div>
      ) : (
        <div className="flex flex-col gap-2">
          <div className="flex items-center justify-between">
            <div>
              <span className="font-hand text-2xl text-ink">{limit.amount.toLocaleString()}</span>
              <span className="font-hand text-sm text-brown ml-2">
                {limit.period[0] + limit.period.slice(1).toLowerCase()}
              </span>
            </div>
            <div className="flex gap-2">
              <button className="font-hand text-xs text-brown hover:text-sage cursor-pointer" onClick={() => setEditing(true)}>
                edit
              </button>
              <button className="font-hand text-xs text-red-ink hover:opacity-70 cursor-pointer"
                onClick={() => deleteLimit.mutate({ id: limit.id, budgetId: limit.budgetId })}>
                delete
              </button>
            </div>
          </div>
          {status && <LimitStatusBar status={status} />}
        </div>
      )}
    </div>
  );
}
