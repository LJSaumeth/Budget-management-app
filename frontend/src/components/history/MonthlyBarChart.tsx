import { useEffect, useRef } from 'react';
import rough from 'roughjs';
import type { MonthlySummaryItem } from '@/api/types';
import EmptyState from '@/components/ui/EmptyState';

const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

export default function MonthlyBarChart({ data }: { data: MonthlySummaryItem[] }) {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas || data.length === 0) return;
    const rc = rough.canvas(canvas);
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    ctx.clearRect(0, 0, 700, 300);

    const maxAmount = Math.max(...data.map((d) => d.totalAmount), 1);
    const barW = 40;
    const gap = 15;
    const startX = 60;
    const baseY = 260;

    data.forEach((item, i) => {
      const x = startX + i * (barW + gap);
      const monthIdx = parseInt(item.month.split('-')[1]) - 1;

      if (item.totalAmount > 0) {
        const h = (item.totalAmount / maxAmount) * 200;
        const y = baseY - h;
        rc.rectangle(x, y, barW, h, {
          fill: '#9D6638',
          fillStyle: 'solid',
        });
        rc.rectangle(x, y, barW, h, {
          stroke: '#4E220F',
          strokeWidth: 1,
        });
      }

      ctx.font = '12px Patrick Hand';
      ctx.fillStyle = '#4E220F';
      ctx.fillText(MONTHS[monthIdx] ?? '', x + barW / 2 - 10, baseY + 20);

      if (item.totalAmount > 0) {
        ctx.font = '10px Patrick Hand';
        ctx.fillStyle = '#9D6638';
        ctx.fillText(`$${item.totalAmount}`, x + barW / 2 - 15, baseY - (item.totalAmount / maxAmount) * 200 - 5);
      }
    });
  }, [data]);

  if (data.length === 0) {
    return <EmptyState title="No monthly data" description="Add expenses to see monthly trends." />;
  }

  return (
    <div className="flex justify-center">
      <canvas ref={canvasRef} width={700} height={300} className="max-w-full" />
    </div>
  );
}
