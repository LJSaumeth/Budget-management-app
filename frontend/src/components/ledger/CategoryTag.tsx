interface CategoryTagProps {
  name: string;
  id: number;
}

const CATEGORY_COLORS = [
  '#9D6638', '#B0BA99', '#4E220F', '#C9B99A', '#7A8B6E',
  '#6B3A2A', '#8BA870', '#5C3D2E', '#A0987A',
];

export default function CategoryTag({ name }: CategoryTagProps) {
  const color = CATEGORY_COLORS[name.length % CATEGORY_COLORS.length];

  return (
    <span
      className="inline-block font-hand text-xs px-2 py-0.5 rounded-sketch border"
      style={{ borderColor: color, color, backgroundColor: `${color}10` }}
    >
      {name}
    </span>
  );
}
