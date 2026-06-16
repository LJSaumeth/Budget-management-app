import type { SimulationResult } from '@/api/types';
import StickyNoteCard from '@/components/ui/StickyNoteCard';

export default function SimulationResultNote({ result }: { result: SimulationResult }) {
  return (
    <StickyNoteCard accent="#B0BA99">
      <div className="font-hand space-y-2">
        <h3 className="text-xl text-ink">Projection</h3>
        <div className="space-y-1">
          <p className="text-lg">
            Monthly Savings:{' '}
            <span className={result.monthlySavings < 0 ? 'text-ink underline decoration-red-ink' : 'text-sage'}>
              ${result.monthlySavings.toLocaleString()}
            </span>
          </p>
          <p className="text-2xl">
            Projected ({result.months} months):{' '}
            <span className={result.projectedSavings < 0 ? 'text-ink underline decoration-red-ink' : 'text-sage'}>
              ${result.projectedSavings.toLocaleString()}
            </span>
          </p>
        </div>
        <div className="text-sm text-brown space-y-0.5">
          <p>Income: ${result.monthlyIncome.toLocaleString()}</p>
          <p>Expenses: ${result.monthlyExpenses.toLocaleString()}</p>
          <p>Adjusted: ${result.adjustedMonthlyExpenses.toLocaleString()}</p>
        </div>
        {result.differenceFromBaseline != null && (
          <div className="pt-2 border-t border-brown/15">
            <p className="text-sm">
              Without changes:{' '}
              <span className="text-brown">${result.baselineProjectedSavings?.toLocaleString()}</span>
            </p>
            <p className="text-lg">
              Extra savings:{' '}
              <span className="text-sage">${result.differenceFromBaseline.toLocaleString()}</span>
            </p>
          </div>
        )}
        {result.adjustedMonthlyExpenses === 0 && result.monthlyExpenses > 0 && (
          <p className="text-xs text-brown/60">Reductions capped at total expenses.</p>
        )}
      </div>
    </StickyNoteCard>
  );
}
