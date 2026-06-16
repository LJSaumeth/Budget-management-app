import { useState } from 'react';
import RuledInput from '@/components/ui/RuledInput';
import NotebookButton from '@/components/ui/NotebookButton';
import ValidationHint from '@/components/ui/ValidationHint';
import { useConversion } from '@/hooks/use-exchange';
import ConversionSlip from './ConversionSlip';

export default function ConverterForm() {
  const [amount, setAmount] = useState('');
  const [from, setFrom] = useState('USD');
  const [to, setTo] = useState('EUR');
  const [amountError, setAmountError] = useState('');
  const [submitted, setSubmitted] = useState(false);

  const amountNum = parseFloat(amount);
  const sameCurrency = from.toUpperCase() === to.toUpperCase();

  const { data: result, isLoading, error } = useConversion(
    submitted && amountNum > 0 ? amountNum : null,
    submitted && from ? from : null,
    submitted && to ? to : null,
  );

  const handleConvert = () => {
    if (!amount || amountNum <= 0) {
      setAmountError('Amount must be greater than 0');
      return;
    }
    setAmountError('');
    setSubmitted(true);
  };

  const handleInputChange = () => {
    setSubmitted(false);
  };

  return (
    <div className="space-y-4">
      <h2 className="font-hand text-2xl text-ink">Currency Converter</h2>
      <div className="flex flex-wrap gap-3 items-end">
        <RuledInput
          label="Amount"
          type="number"
          value={amount}
          onChange={(e) => { setAmount(e.target.value); handleInputChange(); }}
          error={amountError}
          className="w-32"
        />
        <RuledInput
          label="From"
          value={from}
          onChange={(e) => { setFrom(e.target.value.toUpperCase()); handleInputChange(); }}
          className="w-24"
          maxLength={3}
        />
        <RuledInput
          label="To"
          value={to}
          onChange={(e) => { setTo(e.target.value.toUpperCase()); handleInputChange(); }}
          className="w-24"
          maxLength={3}
        />
        <NotebookButton onClick={handleConvert} loading={isLoading}>
          Convert
        </NotebookButton>
      </div>

      {sameCurrency && submitted && !isLoading && (
        <ConversionSlip
          result={{
            from: from.toUpperCase(),
            to: to.toUpperCase(),
            amount: amountNum,
            rate: 1,
            result: amountNum,
            fetchedAt: new Date().toISOString(),
          }}
        />
      )}

      {result && !sameCurrency && <ConversionSlip result={result} />}
      {error && <ValidationHint message={error.message} />}
    </div>
  );
}
