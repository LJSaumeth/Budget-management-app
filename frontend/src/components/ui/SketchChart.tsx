import { useEffect, useRef } from 'react';
import rough from 'roughjs';

type RoughCanvas = ReturnType<typeof rough.canvas>;

type DrawFn = (rc: RoughCanvas, canvas: HTMLCanvasElement) => void;

interface SketchChartProps {
  width: number;
  height: number;
  draw: DrawFn;
  className?: string;
}

export default function SketchChart({
  width,
  height,
  draw,
  className = '',
}: SketchChartProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const rc = rough.canvas(canvas);
    const ctx = canvas.getContext('2d');
    if (ctx) ctx.clearRect(0, 0, width, height);
    draw(rc, canvas);
  }, [draw, width, height]);

  return (
    <canvas
      ref={canvasRef}
      width={width}
      height={height}
      className={className}
    />
  );
}
