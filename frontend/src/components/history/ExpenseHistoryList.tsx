import type { ExpenseHistoryPage } from '@/api/types';
import ExpenseRow from '@/components/ledger/ExpenseRow';
import NotebookButton from '@/components/ui/NotebookButton';
import SkeletonLoader from '@/components/ui/SkeletonLoader';
import EmptyState from '@/components/ui/EmptyState';

interface ExpenseHistoryListProps {
  data: ExpenseHistoryPage | undefined;
  isLoading: boolean;
  page: number;
  onPageChange: (page: number) => void;
}

export default function ExpenseHistoryList({ data, isLoading, page, onPageChange }: ExpenseHistoryListProps) {
  if (isLoading) {
    return (
      <div className="space-y-2">
        {[1, 2, 3, 4, 5].map((i) => (
          <SkeletonLoader key={i} height="40px" />
        ))}
      </div>
    );
  }

  if (!data || data.content.length === 0) {
    return (
      <EmptyState
        title="No expenses match your filters"
        description="Try a different date range or category."
      />
    );
  }

  return (
    <div>
      {data.content.map((e) => (
        <ExpenseRow key={e.id} expense={e} />
      ))}

      {data.totalPages > 1 && (
        <div className="flex items-center justify-between mt-4 pt-2 border-t border-brown/15">
          <span className="font-hand text-sm text-brown">
            Page {page + 1} of {data.totalPages}
          </span>
          <div className="flex gap-2">
            <NotebookButton
              size="sm"
              variant="secondary"
              onClick={() => onPageChange(page - 1)}
              disabled={page === 0}
            >
              Previous
            </NotebookButton>
            <NotebookButton
              size="sm"
              variant="secondary"
              onClick={() => onPageChange(page + 1)}
              disabled={page >= data.totalPages - 1}
            >
              Next
            </NotebookButton>
          </div>
        </div>
      )}
    </div>
  );
}
