interface ValidationHintProps {
  message: string;
}

export default function ValidationHint({ message }: ValidationHintProps) {
  return (
    <p className="font-hand text-red-ink text-xs underline decoration-red-ink/50 mt-1">
      {message}
    </p>
  );
}
