import type { LimitStatus } from '@/api/types';
import SketchProgressBar from '@/components/ui/SketchProgressBar';

export default function LimitStatusBar({ status }: { status: LimitStatus }) {
  const pct = status.percentageUsed;
  const periodLabel = status.period[0] + status.period.slice(1).toLowerCase();

  const barColor = pct >= 100 ? '#4E220F' : '#9D6638';
  const labelColor = status.status === 'OK' ? '#B0BA99' : '#4E220F';
  const overfill = pct > 100;

  return (
    <div>
      <SketchProgressBar value={Math.min(pct, 100)} color={barColor} />
      {overfill && (
        <div className="h-1 w-1.5 bg-ink rounded-sm relative -top-4" style={{ left: '100%' }} />
      )}
      <p className="font-hand text-sm mt-1" style={{ color: labelColor }}>
        {status.status === 'OK' && `OK — ${status.remainingAmount.toLocaleString()} remaining`}
        {status.status === 'WARNING' && `WARNING — ${status.remainingAmount.toLocaleString()} remaining`}
        {status.status === 'EXCEEDED' && `EXCEEDED — ${Math.abs(status.remainingAmount).toLocaleString()} over budget`}
      </p>
      <p className="font-hand text-xs text-brown/50">
        {periodLabel} · {status.periodStart} – {status.periodEnd}
      </p>
    </div>
  );
}
