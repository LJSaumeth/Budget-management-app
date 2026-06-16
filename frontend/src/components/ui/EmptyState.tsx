import NotebookButton from './NotebookButton';

interface EmptyStateProps {
  title: string;
  description?: string;
  actionLabel?: string;
  onAction?: () => void;
}

export default function EmptyState({
  title,
  description,
  actionLabel,
  onAction,
}: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center gap-4">
      <svg width="80" height="100" viewBox="0 0 80 100" className="text-brown/40">
        <rect
          x="5"
          y="5"
          width="70"
          height="90"
          rx="3"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.5"
        />
        <line x1="15" y1="25" x2="55" y2="28" stroke="currentColor" strokeWidth="1" />
        <line x1="15" y1="35" x2="50" y2="37" stroke="currentColor" strokeWidth="1" />
        <line x1="15" y1="45" x2="58" y2="47" stroke="currentColor" strokeWidth="1" />
        <line x1="15" y1="55" x2="40" y2="56" stroke="currentColor" strokeWidth="1" />
        <line x1="15" y1="65" x2="52" y2="66" stroke="currentColor" strokeWidth="1" />
      </svg>
      <h3 className="font-hand text-2xl text-ink">{title}</h3>
      {description && (
        <p className="font-hand text-brown max-w-md">{description}</p>
      )}
      {actionLabel && onAction && (
        <NotebookButton onClick={onAction}>{actionLabel}</NotebookButton>
      )}
    </div>
  );
}
