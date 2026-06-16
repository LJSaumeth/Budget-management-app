interface NotebookButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  loading?: boolean;
}

const sizeClasses = {
  sm: 'px-3 py-1 text-sm',
  md: 'px-5 py-2 text-base',
  lg: 'px-7 py-3 text-lg',
};

const variantClasses = {
  primary: 'bg-sage text-paper hover:bg-brown',
  secondary: 'bg-paper text-brown border border-brown hover:bg-sage/30',
  danger: 'bg-red-ink text-paper hover:opacity-80',
};

export default function NotebookButton({
  variant = 'primary',
  size = 'md',
  loading = false,
  className = '',
  children,
  disabled,
  ...props
}: NotebookButtonProps) {
  return (
    <button
      className={`font-hand rounded-sketch transition-colors cursor-pointer
        ${sizeClasses[size]} ${variantClasses[variant]}
        ${disabled || loading ? 'opacity-50 cursor-not-allowed' : ''}
        ${className}`}
      disabled={disabled || loading}
      {...props}
    >
      {loading ? '...' : children}
    </button>
  );
}
