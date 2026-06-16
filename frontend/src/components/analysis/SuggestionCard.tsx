import type { OptimizationSuggestion } from '@/api/types';
import StickyNoteCard from '@/components/ui/StickyNoteCard';

export default function SuggestionCard({ suggestion, top }: { suggestion: OptimizationSuggestion; top?: boolean }) {
  return (
    <StickyNoteCard
      accent={top ? '#9D6638' : '#B0BA99'}
      className={top ? 'scale-105' : ''}
    >
      <div className="font-hand space-y-2">
        <h3 className="text-xl text-ink">{suggestion.categoryName}</h3>
        <p className="text-sage">
          <span className="text-base">${suggestion.monthlySaving.toLocaleString()}/month</span>
          {' · '}
          <span className="text-lg font-bold">${suggestion.annualSaving.toLocaleString()}/year</span>
        </p>
        <p className="text-sm text-brown leading-relaxed">{suggestion.reasoning}</p>
        <span className="inline-block text-xs text-brown/60 border border-brown/20 rounded-sketch px-2 py-0.5">
          {suggestion.suggestedReductionPercent}% reduction
        </span>
      </div>
    </StickyNoteCard>
  );
}
