import RuledInput from '@/components/ui/RuledInput';

interface CategoryReductionRowProps {
  index: number;
  category: string;
  amount: string;
  onChange: (index: number, field: 'category' | 'amount', value: string) => void;
  onRemove: (index: number) => void;
}

export default function CategoryReductionRow({ index, category, amount, onChange, onRemove }: CategoryReductionRowProps) {
  return (
    <div className="flex gap-2 items-end">
      <RuledInput
        label="Category"
        value={category}
        onChange={(e) => onChange(index, 'category', e.target.value)}
        className="w-36"
      />
      <RuledInput
        label="Reduction $"
        type="number"
        value={amount}
        onChange={(e) => onChange(index, 'amount', e.target.value)}
        className="w-28"
      />
      <button
        className="font-hand text-sm text-red-ink hover:opacity-70 cursor-pointer mb-2"
        onClick={() => onRemove(index)}
      >
        ×
      </button>
    </div>
  );
}
