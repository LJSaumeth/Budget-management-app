import { useBudget } from '@/context/BudgetContext';
import { useAnalysis } from '@/hooks/use-analysis';
import SuggestionCard from '@/components/analysis/SuggestionCard';
import SkeletonLoader from '@/components/ui/SkeletonLoader';
import EmptyState from '@/components/ui/EmptyState';
import ErrorState from '@/components/ui/ErrorState';
import HandDrawnDivider from '@/components/ui/HandDrawnDivider';

export default function AnalysisPage() {
  const { selectedBudgetId } = useBudget();
  const { data, isLoading, error } = useAnalysis(selectedBudgetId);

  if (!selectedBudgetId) {
    return <ErrorState message="Select a budget from the dashboard first to view analysis." />;
  }

  if (isLoading) {
    return (
      <div className="space-y-4">
        <SkeletonLoader height="40px" />
        <SkeletonLoader height="120px" />
        <SkeletonLoader height="120px" />
      </div>
    );
  }

  if (error) {
    return <ErrorState message={error.message} />;
  }

  const isNoData = data?.message === 'No expense data available';
  const isInsufficient = data?.message?.includes('Insufficient data');
  const suggestions = data?.suggestions ?? [];

  return (
    <div>
      <h1 className="font-hand text-3xl text-ink mb-4">Analysis</h1>

      {isNoData && (
        <EmptyState
          title="No expense data available"
          description="Start logging your spending to get insights."
        />
      )}

      {isInsufficient && (
        <div className="flex flex-col items-center py-12 text-center gap-4">
          <svg width="60" height="60" viewBox="0 0 60 60" className="text-brown/40">
            <rect x="8" y="10" width="44" height="40" rx="2" fill="none" stroke="currentColor" strokeWidth="1.5" />
            <line x1="16" y1="20" x2="30" y2="22" stroke="currentColor" strokeWidth="1" />
            <line x1="16" y1="28" x2="28" y2="30" stroke="currentColor" strokeWidth="1" />
          </svg>
          <p className="font-hand text-lg text-brown">
            Need at least 2 months of data to analyze your patterns. Keep logging expenses!
          </p>
        </div>
      )}

      {!isNoData && !isInsufficient && suggestions.length === 0 && data && (
        <div className="flex flex-col items-center py-12 text-center gap-4">
          <svg width="40" height="40" viewBox="0 0 40 40" className="text-sage">
            <path d="M 10,22 L 17,28 L 30,14" stroke="currentColor" strokeWidth="3" fill="none" />
          </svg>
          <p className="font-hand text-2xl text-sage">You're doing great!</p>
          <p className="font-hand text-brown">All your spending is on track.</p>
        </div>
      )}

      {suggestions.length > 0 && (
        <>
          <div className="mb-4 p-4 bg-sage/10 rounded-card border-l-4 border-sage">
            <p className="font-hand text-lg text-ink">
              Potential Annual Savings:{' '}
              <span className="text-sage font-bold">${data?.totalPotentialAnnualSaving?.toLocaleString() ?? '0'}</span>
            </p>
            <p className="font-hand text-sm text-brown">
              {suggestions.length} optimization {suggestions.length === 1 ? 'opportunity' : 'opportunities'} found
            </p>
          </div>

          <HandDrawnDivider />

          <div className="space-y-4 mt-4">
            {suggestions.map((s, i) => (
              <SuggestionCard key={s.categoryId} suggestion={s} top={i === 0} />
            ))}
          </div>
        </>
      )}
    </div>
  );
}
