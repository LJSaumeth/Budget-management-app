import { useBudget } from '@/hooks/use-budgets';
import SketchProgressBar from '@/components/ui/SketchProgressBar';
import SkeletonLoader from '@/components/ui/SkeletonLoader';

interface BudgetHeaderProps {
  budgetId: number;
}

export default function BudgetHeader({ budgetId }: BudgetHeaderProps) {
  const { data: budget, isLoading } = useBudget(budgetId);

  if (isLoading) return <SkeletonLoader height="100px" />;
  if (!budget) return null;

  const spent = budget.totalAmount - budget.remainingAmount;
  const pct = budget.totalAmount > 0 ? (spent / budget.totalAmount) * 100 : 0;

  return (
    <div className="mb-6 p-4 bg-paper rounded-card border-l-4 border-sage">
      <h1 className="font-hand text-3xl text-ink">{budget.name}</h1>
      <p className="font-hand text-brown text-sm mb-2">
        {budget.currency}
      </p>
      <SketchProgressBar
        value={pct}
        label={`Spent ${spent.toLocaleString()} / ${budget.totalAmount.toLocaleString()}`}
      />
      <p className="font-hand text-sage text-lg mt-1">
        {budget.remainingAmount.toLocaleString()} {budget.currency} remaining
      </p>
    </div>
  );
}
