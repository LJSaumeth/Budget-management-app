interface SketchProgressBarProps {
  value: number;
  label?: string;
  color?: string;
}

export default function SketchProgressBar({
  value,
  label,
  color = '#9D6638',
}: SketchProgressBarProps) {
  const clamped = Math.min(100, Math.max(0, value));

  return (
    <div className="flex flex-col gap-1">
      <div className="h-4 w-full bg-brown/10 rounded-sketch overflow-hidden relative">
        <div
          className="h-full transition-all duration-500 rounded-sketch"
          style={{
            width: `${clamped}%`,
            backgroundColor: color,
          }}
        />
        {value > 100 && (
          <div
            className="h-full absolute top-0 rounded-sketch"
            style={{
              left: '100%',
              width: '4px',
              backgroundColor: '#4E220F',
            }}
          />
        )}
      </div>
      {label && (
        <p className="font-hand text-sm text-brown">{label}</p>
      )}
    </div>
  );
}
