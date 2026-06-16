import { useState } from 'react';
import { useUpdateExpense, useDeleteExpense } from '@/hooks/use-expenses';
import type { Expense, ExpenseRequest } from '@/api/types';
import CategoryTag from './CategoryTag';
import RuledInput from '@/components/ui/RuledInput';
import NotebookButton from '@/components/ui/NotebookButton';

interface ExpenseRowProps {
  expense: Expense;
}

export default function ExpenseRow({ expense }: ExpenseRowProps) {
  const updateExpense = useUpdateExpense();
  const deleteExpense = useDeleteExpense();
  const [editing, setEditing] = useState(false);
  const [amount, setAmount] = useState(expense.amount.toString());
  const [description, setDescription] = useState(expense.description);

  const handleSave = () => {
    updateExpense.mutate({
      id: expense.id,
      data: {
        budgetId: expense.budgetId,
        categoryId: expense.categoryId,
        amount: parseFloat(amount),
        description,
        date: expense.date,
      } as ExpenseRequest,
    });
    setEditing(false);
  };

  const handleDelete = () => {
    deleteExpense.mutate({ id: expense.id, budgetId: expense.budgetId });
  };

  return (
    <div className="flex items-center gap-3 py-2 border-b border-brown/15">
      {editing ? (
        <div className="flex flex-1 items-center gap-2">
          <RuledInput
            type="number"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            className="w-24"
          />
          <RuledInput
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            className="flex-1"
          />
          <NotebookButton size="sm" onClick={handleSave}>Save</NotebookButton>
          <NotebookButton size="sm" variant="secondary" onClick={() => setEditing(false)}>
            Cancel
          </NotebookButton>
        </div>
      ) : (
        <>
          <span className="font-hand text-sm text-brown w-24">
            {new Date(expense.date).toLocaleDateString('en-US', {
              month: 'short',
              day: 'numeric',
            })}
          </span>
          <CategoryTag name={expense.categoryName} id={expense.categoryId} />
          <span className="font-hand text-brown flex-1">{expense.description}</span>
          <span className="font-hand text-ink font-bold w-24 text-right">
            {expense.amount.toLocaleString()}
          </span>
          <div className="flex gap-1">
            <button
              className="font-hand text-xs text-brown hover:text-sage cursor-pointer"
              onClick={() => setEditing(true)}
            >
              edit
            </button>
            <button
              className="font-hand text-xs text-red-ink hover:opacity-70 cursor-pointer"
              onClick={handleDelete}
            >
              delete
            </button>
          </div>
        </>
      )}
    </div>
  );
}
