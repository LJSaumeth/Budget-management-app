import RuledInput from '@/components/ui/RuledInput';
import NotebookButton from '@/components/ui/NotebookButton';
import { useCategories } from '@/hooks/use-categories';

interface HistoryFiltersProps {
  filter: {
    startDate: string;
    endDate: string;
    categoryId: string;
    search: string;
  };
  onChange: (filter: {
    startDate: string;
    endDate: string;
    categoryId: string;
    search: string;
  }) => void;
}

export default function HistoryFilters({ filter, onChange }: HistoryFiltersProps) {
  const { data: categories } = useCategories();

  return (
    <div className="flex flex-wrap gap-3 items-end mb-4">
      <RuledInput
        label="Start Date"
        type="date"
        value={filter.startDate}
        onChange={(e) => onChange({ ...filter, startDate: e.target.value })}
        className="w-40"
      />
      <RuledInput
        label="End Date"
        type="date"
        value={filter.endDate}
        onChange={(e) => onChange({ ...filter, endDate: e.target.value })}
        className="w-40"
      />
      <div className="flex flex-col gap-1">
        <label className="font-hand text-brown text-sm">Category</label>
        <select
          value={filter.categoryId}
          onChange={(e) => onChange({ ...filter, categoryId: e.target.value })}
          className="bg-transparent border-0 border-b-2 border-brown/30 px-1 py-2 font-hand text-ink focus:outline-none focus:border-sage cursor-pointer"
        >
          <option value="">All</option>
          {categories?.map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>
      </div>
      <RuledInput
        label="Search"
        value={filter.search}
        onChange={(e) => onChange({ ...filter, search: e.target.value })}
        className="w-48"
      />
      <NotebookButton
        size="sm"
        variant="secondary"
        onClick={() => onChange({ startDate: '', endDate: '', categoryId: '', search: '' })}
      >
        Clear
      </NotebookButton>
    </div>
  );
}
