import { useState } from 'react';
import { useSimulation } from '@/hooks/use-simulation';
import RuledInput from '@/components/ui/RuledInput';
import NotebookButton from '@/components/ui/NotebookButton';
import ValidationHint from '@/components/ui/ValidationHint';
import CategoryReductionRow from './CategoryReductionRow';
import SimulationResultNote from './SimulationResultNote';

interface ChangeRow {
  category: string;
  amount: string;
}

export default function SimulationForm() {
  const simulation = useSimulation();
  const [income, setIncome] = useState('5000');
  const [expenses, setExpenses] = useState('3500');
  const [months, setMonths] = useState('12');
  const [savings, setSavings] = useState('');
  const [changes, setChanges] = useState<ChangeRow[]>([]);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const handleChange = (index: number, field: 'category' | 'amount', value: string) => {
    const updated = [...changes];
    updated[index] = { ...updated[index], [field]: value };
    setChanges(updated);
  };

  const handleSubmit = () => {
    const errs: Record<string, string> = {};
    if (!income || parseFloat(income) < 0) errs.income = 'Must be 0 or more';
    if (!expenses || parseFloat(expenses) < 0) errs.expenses = 'Must be 0 or more';
    if (!months || parseInt(months) < 1) errs.months = 'Must be at least 1';
    setErrors(errs);
    if (Object.keys(errs).length > 0) return;

    simulation.mutate({
      monthlyIncome: parseFloat(income),
      monthlyExpenses: parseFloat(expenses),
      months: parseInt(months),
      currentSavings: savings ? parseFloat(savings) : undefined,
      expectedChanges: changes
        .filter((c) => c.category && c.amount)
        .map((c) => ({ category: c.category, reductionAmount: parseFloat(c.amount) })),
    });
  };

  return (
    <div className="space-y-4">
      <h2 className="font-hand text-2xl text-ink">Savings Simulation</h2>

      <div className="flex flex-wrap gap-3 items-end">
        <RuledInput label="Monthly Income" type="number" value={income} onChange={(e) => setIncome(e.target.value)} error={errors.income} className="w-36" />
        <RuledInput label="Monthly Expenses" type="number" value={expenses} onChange={(e) => setExpenses(e.target.value)} error={errors.expenses} className="w-36" />
        <RuledInput label="Months" type="number" value={months} onChange={(e) => setMonths(e.target.value)} error={errors.months} className="w-24" />
        <RuledInput label="Current Savings" type="number" value={savings} onChange={(e) => setSavings(e.target.value)} className="w-36" />
      </div>

      {changes.length > 0 && (
        <div className="space-y-2">
          <h3 className="font-hand text-lg text-brown">Spending Reductions</h3>
          {changes.map((c, i) => (
            <CategoryReductionRow key={i} index={i} category={c.category} amount={c.amount} onChange={handleChange} onRemove={(i) => setChanges((cs) => cs.filter((_, idx) => idx !== i))} />
          ))}
        </div>
      )}

      <div className="flex gap-2">
        <NotebookButton variant="secondary" onClick={() => setChanges([...changes, { category: '', amount: '' }])}>
          + Add Category
        </NotebookButton>
        <NotebookButton onClick={handleSubmit} loading={simulation.isPending}>
          Project
        </NotebookButton>
      </div>

      {simulation.error && <ValidationHint message={simulation.error.message} />}

      {simulation.data && <SimulationResultNote result={simulation.data} />}
    </div>
  );
}
