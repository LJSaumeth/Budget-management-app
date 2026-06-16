import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBudget } from '@/context/BudgetContext';
import { useUpdateBudget, useDeleteBudget } from '@/hooks/use-budgets';
import type { Budget, BudgetRequest } from '@/api/types';
import StickyNoteCard from '@/components/ui/StickyNoteCard';
import SketchProgressBar from '@/components/ui/SketchProgressBar';
import RuledInput from '@/components/ui/RuledInput';
import NotebookButton from '@/components/ui/NotebookButton';

interface BudgetCardProps {
  budget: Budget;
}

export default function BudgetCard({ budget }: BudgetCardProps) {
  const navigate = useNavigate();
  const { setSelectedBudgetId } = useBudget();
  const updateBudget = useUpdateBudget();
  const deleteBudget = useDeleteBudget();
  const [editing, setEditing] = useState(false);
  const [name, setName] = useState(budget.name);
  const [amount, setAmount] = useState(budget.totalAmount.toString());
  const [showConfirm, setShowConfirm] = useState(false);

  const spent = budget.totalAmount - (budget.remainingAmount ?? 0);
  const pct = budget.totalAmount > 0 ? (spent / budget.totalAmount) * 100 : 0;

  const handleEdit = () => {
    if (!editing) {
      setEditing(true);
      return;
    }
    updateBudget.mutate({
      id: budget.id,
      data: { name, totalAmount: parseFloat(amount), currency: budget.currency } as BudgetRequest,
    });
    setEditing(false);
  };

  const handleDelete = () => {
    deleteBudget.mutate(budget.id);
    setShowConfirm(false);
  };

  const handleOpen = () => {
    setSelectedBudgetId(budget.id);
    navigate(`/budgets/${budget.id}`);
  };

  return (
    <StickyNoteCard accent="#B0BA99">
      <div className="flex flex-col gap-2">
        {editing ? (
          <div className="flex flex-col gap-2">
            <RuledInput value={name} onChange={(e) => setName(e.target.value)} />
            <RuledInput
              type="number"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
            />
            <div className="flex gap-2">
              <NotebookButton size="sm" onClick={handleEdit}>Save</NotebookButton>
              <NotebookButton size="sm" variant="secondary" onClick={() => setEditing(false)}>
                Cancel
              </NotebookButton>
            </div>
          </div>
        ) : (
          <>
            <h3 className="font-hand text-2xl text-ink cursor-pointer hover:text-brown" onClick={handleOpen}>
              {budget.name}
            </h3>
            <p className="font-hand text-brown text-sm">
              {budget.currency} {budget.totalAmount.toLocaleString()}
            </p>
            <SketchProgressBar
              value={pct}
              label={`Spent ${spent.toLocaleString()} / ${budget.totalAmount.toLocaleString()}`}
            />
            <p className="font-hand text-sage text-sm">
              {budget.remainingAmount.toLocaleString()} remaining
            </p>
            <div className="flex gap-2 mt-1">
              <NotebookButton size="sm" variant="secondary" onClick={() => setEditing(true)}>
                Edit
              </NotebookButton>
              <NotebookButton
                size="sm"
                variant="danger"
                onClick={() => setShowConfirm(true)}
              >
                Delete
              </NotebookButton>
            </div>
          </>
        )}

        {showConfirm && (
          <div className="mt-2 p-2 border border-red-ink rounded-sketch bg-paper">
            <p className="font-hand text-sm text-red-ink mb-2">
              Delete this budget and all its expenses?
            </p>
            <div className="flex gap-2">
              <NotebookButton size="sm" variant="danger" onClick={handleDelete}>
                Yes, delete
              </NotebookButton>
              <NotebookButton size="sm" variant="secondary" onClick={() => setShowConfirm(false)}>
                Cancel
              </NotebookButton>
            </div>
          </div>
        )}
      </div>
    </StickyNoteCard>
  );
}
