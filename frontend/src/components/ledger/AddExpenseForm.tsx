import { useState } from 'react';
import { useBudget } from '@/context/BudgetContext';
import { useCreateExpense } from '@/hooks/use-expenses';
import { useCategories } from '@/hooks/use-categories';
import RuledInput from '@/components/ui/RuledInput';
import NotebookButton from '@/components/ui/NotebookButton';

export default function AddExpenseForm() {
  const { selectedBudgetId } = useBudget();
  const createExpense = useCreateExpense();
  const { data: categories } = useCategories();
  const [open, setOpen] = useState(false);
  const [amount, setAmount] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [description, setDescription] = useState('');
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [amountError, setAmountError] = useState('');

  const handleSubmit = () => {
    if (!amount || parseFloat(amount) <= 0) {
      setAmountError('Amount must be greater than 0');
      return;
    }
    setAmountError('');

    createExpense.mutate(
      {
        budgetId: selectedBudgetId!,
        categoryId: parseInt(categoryId || '1'),
        amount: parseFloat(amount),
        description: description || 'Expense',
        date,
      },
      {
        onSuccess: () => {
          setAmount('');
          setDescription('');
          setDate(new Date().toISOString().split('T')[0]);
          setOpen(false);
        },
      },
    );
  };

  if (!open) {
    return (
      <NotebookButton variant="secondary" onClick={() => setOpen(true)} className="mb-4">
        + Add Expense
      </NotebookButton>
    );
  }

  return (
    <div className="mb-4 p-4 bg-paper rounded-card border border-brown/20">
      <div className="flex flex-wrap gap-3 items-end">
        <RuledInput
          label="Amount"
          type="number"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          error={amountError}
          className="w-32"
        />
        <div className="flex flex-col gap-1">
          <label className="font-hand text-brown text-sm">Category</label>
          <select
            value={categoryId}
            onChange={(e) => setCategoryId(e.target.value)}
            className="bg-transparent border-0 border-b-2 border-brown/30 px-1 py-2 font-hand text-ink focus:outline-none focus:border-sage cursor-pointer"
          >
            <option value="">Select...</option>
            {categories?.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </div>
        <RuledInput
          label="Description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          className="flex-1 min-w-[150px]"
        />
        <RuledInput
          label="Date"
          type="date"
          value={date}
          onChange={(e) => setDate(e.target.value)}
          className="w-40"
        />
        <div className="flex gap-2">
          <NotebookButton size="sm" onClick={handleSubmit} loading={createExpense.isPending}>
            Add
          </NotebookButton>
          <NotebookButton size="sm" variant="secondary" onClick={() => setOpen(false)}>
            Cancel
          </NotebookButton>
        </div>
      </div>
    </div>
  );
}
