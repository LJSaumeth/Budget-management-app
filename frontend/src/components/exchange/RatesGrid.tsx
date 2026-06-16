import { useState } from 'react';
import RuledInput from '@/components/ui/RuledInput';
import NotebookButton from '@/components/ui/NotebookButton';
import SkeletonLoader from '@/components/ui/SkeletonLoader';
import ErrorState from '@/components/ui/ErrorState';
import HandDrawnDivider from '@/components/ui/HandDrawnDivider';
import { useRates } from '@/hooks/use-exchange';

export default function RatesGrid() {
  const [searchBase, setSearchBase] = useState('USD');
  const [submitted, setSubmitted] = useState(false);

  const { data, isLoading, error } = useRates(submitted ? searchBase : null);

  const handleViewRates = () => {
    if (!searchBase.trim()) return;
    setSubmitted(true);
  };

  return (
    <div className="space-y-4">
      <h2 className="font-hand text-2xl text-ink">Browse Rates</h2>
      <div className="flex gap-2 items-end">
        <RuledInput
          label="Base Currency"
          value={searchBase}
          onChange={(e) => setSearchBase(e.target.value.toUpperCase())}
          className="w-32"
          maxLength={3}
        />
        <NotebookButton onClick={handleViewRates} loading={isLoading}>
          View Rates
        </NotebookButton>
      </div>

      <HandDrawnDivider />

      {isLoading && (
        <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-2">
          {Array.from({ length: 12 }).map((_, i) => (
            <SkeletonLoader key={i} height="60px" />
          ))}
        </div>
      )}

      {error && <ErrorState message={error.message} />}

      {data && (
        <>
          {data.fetchedAt && (
            <p className="font-hand text-xs text-brown/50">
              Rates from {new Date(data.fetchedAt).toLocaleString()}
            </p>
          )}
          <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-2">
            {Object.entries(data.rates).map(([currency, rate]) => (
              <div
                key={currency}
                className="p-2 bg-paper rounded-card border border-sage/30 hover:border-sage transition-colors text-center"
              >
                <div className="font-hand text-lg text-ink">{currency}</div>
                <div className="font-hand text-sm text-brown">{rate}</div>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
