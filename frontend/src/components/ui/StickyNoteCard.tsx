interface StickyNoteCardProps {
  children: React.ReactNode;
  accent?: string;
  className?: string;
}

export default function StickyNoteCard({
  children,
  accent,
  className = '',
}: StickyNoteCardProps) {
  return (
    <div
      className={`bg-paper rounded-card p-4 shadow-md relative
        ${accent ? 'border-l-4' : ''}
        ${className}`}
      style={{
        borderColor: accent,
        transform: `rotate(${Math.random() * 0.6 - 0.3}deg)`,
        boxShadow:
          '2px 2px 4px rgba(78, 34, 15, 0.1), 4px 4px 8px rgba(78, 34, 15, 0.05)',
      }}
    >
      {children}
    </div>
  );
}
