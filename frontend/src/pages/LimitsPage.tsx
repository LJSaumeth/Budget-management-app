import { useState } from 'react';
import { useBudget } from '@/context/BudgetContext';
import { useLimits, useLimitStatuses } from '@/hooks/use-limits';
import LimitCard from '@/components/limits/LimitCard';
import LimitForm from '@/components/limits/LimitForm';
import SkeletonLoader from '@/components/ui/SkeletonLoader';
import EmptyState from '@/components/ui/EmptyState';
import ErrorState from '@/components/ui/ErrorState';
import NotebookButton from '@/components/ui/NotebookButton';
import HandDrawnDivider from '@/components/ui/HandDrawnDivider';

export default function LimitsPage() {
  const { selectedBudgetId } = useBudget();
  const { data: limits, isLoading } = useLimits(selectedBudgetId);
  const { data: statuses } = useLimitStatuses(selectedBudgetId);
  const [showForm, setShowForm] = useState(false);

  if (!selectedBudgetId) {
    return <ErrorState message="Select a budget from the dashboard first to set limits." />;
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="font-hand text-3xl text-ink">Spending Limits</h1>
        <NotebookButton onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancel' : '+ Set Limit'}
        </NotebookButton>
      </div>

      {showForm && <LimitForm onDone={() => setShowForm(false)} />}

      <HandDrawnDivider />

      {isLoading && (
        <div className="space-y-3">
          <SkeletonLoader height="100px" />
          <SkeletonLoader height="100px" />
        </div>
      )}

      {limits && limits.length === 0 && (
        <EmptyState
          title="No spending limits"
          description="Set limits to track your spending and get warnings before you go over budget."
          actionLabel="+ Set Limit"
          onAction={() => setShowForm(true)}
        />
      )}

      <div className="space-y-3 mt-4">
        {limits?.map((limit) => (
          <LimitCard
            key={limit.id}
            limit={limit}
            status={statuses?.find((s) => s.limitId === limit.id)}
          />
        ))}
      </div>
    </div>
  );
}
