interface RuledInputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
}

export default function RuledInput({
  label,
  error,
  className = '',
  id,
  ...props
}: RuledInputProps) {
  const inputId = id || (label ? label.toLowerCase().replace(/\s+/g, '-') : undefined);

  return (
    <div className="flex flex-col gap-1">
      {label && (
        <label htmlFor={inputId} className="font-hand text-brown text-sm">{label}</label>
      )}
      <input
        id={inputId}
        className={`bg-transparent border-0 border-b-2 px-1 py-2 font-hand text-ink
          placeholder:text-brown/40 focus:outline-none transition-colors
          ${error ? 'border-red-ink' : 'border-brown/30 focus:border-sage'}
          ${className}`}
        placeholder=" "
        {...props}
      />
      {error && (
        <p className="font-hand text-red-ink text-xs underline decoration-red-ink/50">
          {error}
        </p>
      )}
    </div>
  );
}
