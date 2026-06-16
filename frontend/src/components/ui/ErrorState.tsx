import NotebookButton from './NotebookButton';

interface ErrorStateProps {
  message?: string;
  onRetry?: () => void;
}

export default function ErrorState({
  message = "Something came unbound — let's try again",
  onRetry,
}: ErrorStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center gap-4">
      <svg width="80" height="60" viewBox="0 0 80 60" className="text-brown/40">
        <path
          d="M 10,10 L 35,8 L 50,15 L 70,10 L 65,30 L 40,28 L 25,35 L 15,30 Z"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.5"
        />
        <path
          d="M 50,15 L 55,12 L 58,18"
          fill="none"
          stroke="currentColor"
          strokeWidth="1"
        />
      </svg>
      <p className="font-hand text-lg text-brown">{message}</p>
      {onRetry && (
        <NotebookButton variant="secondary" onClick={onRetry}>
          Try again
        </NotebookButton>
      )}
    </div>
  );
}
