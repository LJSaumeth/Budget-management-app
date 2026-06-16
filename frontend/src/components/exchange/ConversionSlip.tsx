import type { ConversionResult } from '@/api/types';

export default function ConversionSlip({ result }: { result: ConversionResult }) {
  return (
    <div className="p-4 bg-paper rounded-card border border-brown/30 shadow-md max-w-sm">
      <div className="font-hand text-brown text-sm mb-1">Exchange Slip</div>
      <div className="font-hand text-2xl text-ink">
        {result.amount.toLocaleString()} {result.from}
      </div>
      <div className="font-hand text-3xl text-sage my-1">
        → {result.result.toLocaleString()} {result.to}
      </div>
      <div className="font-hand text-sm text-brown">
        Rate: {result.rate}
      </div>
      {result.fetchedAt && (
        <div className="font-hand text-xs text-brown/50 mt-2">
          Rate from {new Date(result.fetchedAt).toLocaleString()}
        </div>
      )}
      {result.from === result.to && result.rate === 1 && (
        <div className="font-hand text-xs text-brown/60 mt-1">
          Same currency — no rate lookup needed.
        </div>
      )}
    </div>
  );
}
