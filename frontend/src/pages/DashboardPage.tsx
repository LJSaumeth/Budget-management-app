import { useState } from 'react';
import { useBudgets, useCreateBudget } from '@/hooks/use-budgets';
import BudgetCard from '@/components/dashboard/BudgetCard';
import NotebookButton from '@/components/ui/NotebookButton';
import RuledInput from '@/components/ui/RuledInput';
import SkeletonLoader from '@/components/ui/SkeletonLoader';
import EmptyState from '@/components/ui/EmptyState';
import ErrorState from '@/components/ui/ErrorState';
import HandDrawnDivider from '@/components/ui/HandDrawnDivider';

export default function DashboardPage() {
  const { data: budgets, isLoading, error } = useBudgets();
  const createBudget = useCreateBudget();
  const [showForm, setShowForm] = useState(false);
  const [name, setName] = useState('');
  const [amount, setAmount] = useState('');
  const [nameError, setNameError] = useState('');
  const [amountError, setAmountError] = useState('');

  const handleCreate = () => {
    let valid = true;
    if (!name.trim()) {
      setNameError('Name is required');
      valid = false;
    } else setNameError('');
    if (!amount || parseFloat(amount) <= 0) {
      setAmountError('Amount must be greater than 0');
      valid = false;
    } else setAmountError('');
    if (!valid) return;

    createBudget.mutate(
      { name: name.trim(), totalAmount: parseFloat(amount), currency: 'USD' },
      {
        onSuccess: () => {
          setName('');
          setAmount('');
          setShowForm(false);
        },
      },
    );
  };

  if (isLoading) {
    return (
      <div className="space-y-4">
        <SkeletonLoader height="40px" />
        <SkeletonLoader height="200px" />
        <SkeletonLoader height="200px" />
      </div>
    );
  }

  if (error) {
    return <ErrorState message={error.message} onRetry={() => window.location.reload()} />;
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="font-hand text-3xl text-ink">My Budgets</h1>
        <NotebookButton onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancel' : '+ New Budget'}
        </NotebookButton>
      </div>

      {showForm && (
        <div className="mb-6 p-4 bg-paper rounded-card border border-brown/20">
          <h2 className="font-hand text-xl text-brown mb-3">Create Budget</h2>
          <div className="flex flex-col gap-3 max-w-md">
            <RuledInput
              label="Name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              error={nameError}
            />
            <RuledInput
              label="Total Amount"
              type="number"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              error={amountError}
            />
            <NotebookButton
              onClick={handleCreate}
              loading={createBudget.isPending}
            >
              Create
            </NotebookButton>
          </div>
        </div>
      )}

      <HandDrawnDivider />

      {budgets && budgets.length === 0 ? (
        <EmptyState
          title="Your notebook is empty"
          description="Create your first budget to get started tracking your expenses."
          actionLabel="+ New Budget"
          onAction={() => setShowForm(true)}
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mt-4">
          {budgets?.map((b) => (
            <BudgetCard key={b.id} budget={b} />
          ))}
        </div>
      )}
    </div>
  );
}
