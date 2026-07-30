/**
 * Generic bar chart component for comparing values across categories.
 *
 * Built on Recharts and designed to be reusable across:
 * - Dashboard (weekly study activity)
 * - GitHub Analytics (stars per repository)
 * - LeetCode Tracker (monthly problem-solving trends)
 *
 * @author DevLaunch
 */

import React from 'react';
import {
  BarChart as RechartsBarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';

/** A single data point for the bar chart. */
export interface BarChartDataPoint {
  /** The category/group label on the X axis. */
  name: string;
  /** The numeric value for the primary bar. */
  value: number;
  /** Optional secondary value for stacked/grouped bars. */
  value2?: number;
}

interface BarChartProps {
  /** The data to visualise as bars. */
  data: BarChartDataPoint[];
  /** Title displayed above the chart. */
  title?: string;
  /** Label for the X axis. */
  xAxisLabel?: string;
  /** Label for the Y axis. */
  yAxisLabel?: string;
  /** Whether the chart is in a loading state. */
  loading?: boolean;
  /** Colour of the primary bars. */
  barColor?: string;
  /** Colour of secondary bars (for stacked/grouped visualisations). */
  barColor2?: string;
  /** Whether to stack the bars. */
  stacked?: boolean;
  /** Optional additional CSS classes. */
  className?: string;
}

const LoadingSkeleton: React.FC = () => (
  <div className="flex h-64 items-end justify-center gap-3 px-4">
    {Array.from({ length: 7 }).map((_, i) => (
      <div
        key={i}
        className="w-8 animate-pulse rounded-t bg-gray-200"
        style={{ height: `${Math.random() * 80 + 20}%` }}
      />
    ))}
  </div>
);

const EmptyState: React.FC = () => (
  <div className="flex h-64 items-center justify-center">
    <p className="text-sm text-gray-400">No data available</p>
  </div>
);

export const BarChart: React.FC<BarChartProps> = ({
  data,
  title,
  xAxisLabel,
  yAxisLabel,
  loading = false,
  barColor = '#6366f1',
  barColor2 = '#a5b4fc',
  stacked = false,
  className = '',
}) => {
  return (
    <div className={className}>
      {title && (
        <h4 className="mb-3 text-sm font-semibold text-gray-700">{title}</h4>
      )}

      {loading ? (
        <LoadingSkeleton />
      ) : !data || data.length === 0 ? (
        <EmptyState />
      ) : (
        <ResponsiveContainer width="100%" height={260}>
          <RechartsBarChart
            data={data}
            margin={{ top: 5, right: 20, left: 0, bottom: 5 }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" vertical={false} />
            <XAxis
              dataKey="name"
              tick={{ fontSize: 12, fill: '#6b7280' }}
              axisLine={{ stroke: '#e5e7eb' }}
              tickLine={false}
              label={
                xAxisLabel
                  ? { value: xAxisLabel, position: 'insideBottom', offset: -5, style: { fontSize: 12, fill: '#9ca3af' } }
                  : undefined
              }
            />
            <YAxis
              tick={{ fontSize: 12, fill: '#6b7280' }}
              axisLine={false}
              tickLine={false}
              label={
                yAxisLabel
                  ? { value: yAxisLabel, angle: -90, position: 'insideLeft', style: { fontSize: 12, fill: '#9ca3af' } }
                  : undefined
              }
            />
            <Tooltip
              contentStyle={{
                borderRadius: '8px',
                border: '1px solid #e5e7eb',
                boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)',
              }}
            />
            <Bar
              dataKey="value"
              fill={barColor}
              radius={[4, 4, 0, 0]}
              maxBarSize={48}
            />
            {data[0]?.value2 !== undefined && (
              <Bar
                dataKey="value2"
                fill={barColor2}
                radius={[4, 4, 0, 0]}
                maxBarSize={48}
                stackId={stacked ? 'stack' : undefined}
              />
            )}
          </RechartsBarChart>
        </ResponsiveContainer>
      )}
    </div>
  );
};
