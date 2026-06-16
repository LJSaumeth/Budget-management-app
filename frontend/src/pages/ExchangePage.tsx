import { useState } from 'react';
import ConverterForm from '@/components/exchange/ConverterForm';
import RatesGrid from '@/components/exchange/RatesGrid';
import HandDrawnDivider from '@/components/ui/HandDrawnDivider';

export default function ExchangePage() {
  const [tab, setTab] = useState<'convert' | 'rates'>('convert');

  return (
    <div>
      <div className="flex gap-4 mb-4">
        <button
          className={`font-hand text-lg px-4 py-1 rounded-sketch transition-colors cursor-pointer
            ${tab === 'convert'
              ? 'bg-sage text-paper'
              : 'text-brown hover:bg-sage/30'}`}
          onClick={() => setTab('convert')}
        >
          Convert
        </button>
        <button
          className={`font-hand text-lg px-4 py-1 rounded-sketch transition-colors cursor-pointer
            ${tab === 'rates'
              ? 'bg-sage text-paper'
              : 'text-brown hover:bg-sage/30'}`}
          onClick={() => setTab('rates')}
        >
          Browse Rates
        </button>
      </div>

      <HandDrawnDivider />

      {tab === 'convert' ? <ConverterForm /> : <RatesGrid />}
    </div>
  );
}
