import { useEffect, useRef } from 'react';
import rough from 'roughjs';
import type { CategorySummaryItem } from '@/api/types';
import EmptyState from '@/components/ui/EmptyState';

const COLORS = ['#9D6638', '#B0BA99', '#4E220F', '#C9B99A', '#7A8B6E', '#6B3A2A', '#8BA870', '#5C3D2E', '#A0987A'];

export default function CategoryDonutChart({ data }: { data: CategorySummaryItem[] }) {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas || data.length === 0) return;
    const rc = rough.canvas(canvas);
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    ctx.clearRect(0, 0, 300, 300);

    const cx = 150, cy = 150, outerR = 100, innerR = 50;
    const total = data.reduce((s, d) => s + d.totalAmount, 0);
    let angle = -Math.PI / 2;

    data.forEach((item, i) => {
      const sliceAngle = (item.totalAmount / total) * 2 * Math.PI;
      rc.arc(cx, cy, outerR * 2, outerR * 2, angle, angle + sliceAngle, false, {
        fill: COLORS[i % COLORS.length],
        fillStyle: 'solid',
      });
      rc.arc(cx, cy, outerR * 2, outerR * 2, angle, angle + sliceAngle, false, {
        stroke: '#F7F1DE',
        strokeWidth: 2,
      });
      angle += sliceAngle;
    });

    rc.circle(cx, cy, innerR * 2, { fill: '#F7F1DE', fillStyle: 'solid' });
  }, [data]);

  if (data.length === 0) {
    return <EmptyState title="No spending data" description="Add expenses to see category breakdown." />;
  }

  return (
    <div>
      <div className="flex justify-center">
        <canvas ref={canvasRef} width={300} height={300} />
      </div>
      <div className="flex flex-wrap gap-3 justify-center mt-3">
        {data.map((item, i) => (
          <div key={item.categoryId} className="flex items-center gap-1">
            <span
              className="inline-block w-3 h-3 rounded-full"
              style={{ backgroundColor: COLORS[i % COLORS.length] }}
            />
            <span className="font-hand text-sm text-brown">
              {item.categoryName} ({item.percentage.toFixed(0)}%)
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
