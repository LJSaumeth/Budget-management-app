import { createContext, useContext, useState } from 'react';

interface BudgetContextType {
  selectedBudgetId: number | null;
  setSelectedBudgetId: (id: number | null) => void;
}

const BudgetContext = createContext<BudgetContextType | null>(null);

export function BudgetProvider({ children }: { children: React.ReactNode }) {
  const [selectedBudgetId, setSelectedBudgetId] = useState<number | null>(null);

  return (
    <BudgetContext.Provider value={{ selectedBudgetId, setSelectedBudgetId }}>
      {children}
    </BudgetContext.Provider>
  );
}

export function useBudget() {
  const ctx = useContext(BudgetContext);
  if (!ctx) throw new Error('useBudget must be used within BudgetProvider');
  return ctx;
}
