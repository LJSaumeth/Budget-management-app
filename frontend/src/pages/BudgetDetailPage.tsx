import { useParams } from 'react-router-dom';
import { useEffect } from 'react';
import { useBudget } from '@/context/BudgetContext';
import { useExpenses } from '@/hooks/use-expenses';
import BudgetHeader from '@/components/ledger/BudgetHeader';
import ExpenseRow from '@/components/ledger/ExpenseRow';
import AddExpenseForm from '@/components/ledger/AddExpenseForm';
import SkeletonLoader from '@/components/ui/SkeletonLoader';
import EmptyState from '@/components/ui/EmptyState';
import ErrorState from '@/components/ui/ErrorState';

export default function BudgetDetailPage() {
  const { id } = useParams<{ id: string }>();
  const budgetId = parseInt(id ?? '0');
  const { setSelectedBudgetId } = useBudget();
  const { data: expenses, isLoading, error } = useExpenses(budgetId);

  useEffect(() => {
    setSelectedBudgetId(budgetId);
  }, [budgetId, setSelectedBudgetId]);

  if (isLoading) {
    return (
      <div className="space-y-3">
        <SkeletonLoader height="100px" />
        <SkeletonLoader height="40px" />
        {[1, 2, 3, 4, 5].map((i) => (
          <SkeletonLoader key={i} height="32px" />
        ))}
      </div>
    );
  }

  if (error) {
    return <ErrorState message={error.message} />;
  }

  return (
    <div>
      <BudgetHeader budgetId={budgetId} />
      <AddExpenseForm />

      {expenses && expenses.length === 0 ? (
        <EmptyState
          title="No expenses yet"
          description="Start logging your spending to track where your money goes."
        />
      ) : (
        <div>
          {expenses?.map((e) => (
            <ExpenseRow key={e.id} expense={e} />
          ))}
        </div>
      )}
    </div>
  );
}
