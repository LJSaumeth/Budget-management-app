import { useState } from 'react';
import { useBudget } from '@/context/BudgetContext';
import { useExpenseHistory, useCategorySummary, useMonthlySummary } from '@/hooks/use-history';
import type { HistoryFilter } from '@/hooks/use-history';
import HistoryFilters from '@/components/history/HistoryFilters';
import ExpenseHistoryList from '@/components/history/ExpenseHistoryList';
import CategoryDonutChart from '@/components/history/CategoryDonutChart';
import MonthlyBarChart from '@/components/history/MonthlyBarChart';
import SkeletonLoader from '@/components/ui/SkeletonLoader';
import ErrorState from '@/components/ui/ErrorState';
import HandDrawnDivider from '@/components/ui/HandDrawnDivider';

export default function HistoryPage() {
  const { selectedBudgetId } = useBudget();
  const [tab, setTab] = useState<'history' | 'category' | 'month'>('history');
  const [filter, setFilter] = useState<HistoryFilter>({});
  const [page, setPage] = useState(0);
  const [year, setYear] = useState(new Date().getFullYear());

  const { data: history, isLoading: historyLoading } = useExpenseHistory(
    selectedBudgetId,
    filter,
    page,
  );

  const { data: catData, isLoading: catLoading } = useCategorySummary(
    selectedBudgetId,
    filter.startDate || undefined,
    filter.endDate || undefined,
  );

  const { data: monthData, isLoading: monthLoading } = useMonthlySummary(
    selectedBudgetId,
    year,
    filter.startDate || undefined,
    filter.endDate || undefined,
  );

  if (!selectedBudgetId) {
    return (
      <ErrorState message="Select a budget from the dashboard first to view history." />
    );
  }

  return (
    <div>
      <h1 className="font-hand text-3xl text-ink mb-4">History</h1>

      <HistoryFilters
        filter={{
          startDate: filter.startDate ?? '',
          endDate: filter.endDate ?? '',
          categoryId: filter.categoryId?.toString() ?? '',
          search: filter.search ?? '',
        }}
        onChange={(f) => {
          setFilter({
            startDate: f.startDate || undefined,
            endDate: f.endDate || undefined,
            categoryId: f.categoryId ? Number(f.categoryId) : undefined,
            search: f.search || undefined,
          });
          setPage(0);
        }}
      />

      <div className="flex gap-4 mb-4">
        {(['history', 'category', 'month'] as const).map((t) => (
          <button
            key={t}
            className={`font-hand text-lg px-4 py-1 rounded-sketch transition-colors cursor-pointer
              ${tab === t ? 'bg-sage text-paper' : 'text-brown hover:bg-sage/30'}`}
            onClick={() => setTab(t)}
          >
            {t === 'history' ? 'History' : t === 'category' ? 'By Category' : 'By Month'}
          </button>
        ))}
      </div>

      <HandDrawnDivider />

      {tab === 'history' && (
        <ExpenseHistoryList
          data={history}
          isLoading={historyLoading}
          page={page}
          onPageChange={setPage}
        />
      )}

      {tab === 'category' && (
        catLoading ? <SkeletonLoader height="300px" /> : <CategoryDonutChart data={catData ?? []} />
      )}

      {tab === 'month' && (
        <div>
          <div className="flex items-center gap-2 mb-2">
            <button
              className="font-hand text-brown hover:text-sage cursor-pointer"
              onClick={() => setYear(y => y - 1)}
            >
              ←
            </button>
            <span className="font-hand text-lg text-ink">{year}</span>
            <button
              className="font-hand text-brown hover:text-sage cursor-pointer"
              onClick={() => setYear(y => y + 1)}
            >
              →
            </button>
          </div>
          {monthLoading ? <SkeletonLoader height="300px" /> : <MonthlyBarChart data={monthData ?? []} />}
        </div>
      )}
    </div>
  );
}
