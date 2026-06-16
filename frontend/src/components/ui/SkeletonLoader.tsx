interface SkeletonLoaderProps {
  width?: string;
  height?: string;
  className?: string;
}

export default function SkeletonLoader({
  width = '100%',
  height = '20px',
  className = '',
}: SkeletonLoaderProps) {
  return (
    <div
      className={`animate-pulse rounded-sketch border border-brown/15 border-dashed ${className}`}
      style={{ width, height }}
    >
      <div className="h-full w-full bg-brown/5 rounded-sketch" />
    </div>
  );
}
