/**
 * Generic line chart component for visualising trends over time.
 *
 * Built on Recharts and designed to be reusable across:
 * - Dashboard (placement readiness trend)
 * - LeetCode Tracker (solved problems over time)
 * - GitHub Analytics (contribution activity)
 *
 * @author DevLaunch
 */

import React from 'react';
import {
  LineChart as RechartsLineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';

/** A single data point for the line chart. */
export interface LineChartDataPoint {
  /** The label on the X axis (typically a date or category). */
  name: string;
  /** The primary numeric value. */
  value: number;
  /** Optional secondary value for a second line. */
  value2?: number;
}

interface LineChartProps {
  /** The time-series data to visualise. */
  data: LineChartDataPoint[];
  /** Title displayed above the chart. */
  title?: string;
  /** Label for the X axis. */
  xAxisLabel?: string;
  /** Label for the Y axis. */
  yAxisLabel?: string;
  /** Whether the chart is in a loading state. */
  loading?: boolean;
  /** Colour of the primary line. */
  lineColor?: string;
  /** Colour of the secondary line. */
  lineColor2?: string;
  /** Label for the primary line in the legend. */
  lineLabel?: string;
  /** Label for the secondary line in the legend. */
  lineLabel2?: string;
  /** Whether to show dots on the lines. */
  showDots?: boolean;
  /** Optional additional CSS classes. */
  className?: string;
}

const LoadingSkeleton: React.FC = () => (
  <div className="flex h-64 items-center justify-center">
    <div className="h-48 w-full animate-pulse rounded bg-gray-200" />
  </div>
);

const EmptyState: React.FC = () => (
  <div className="flex h-64 items-center justify-center">
    <p className="text-sm text-gray-400">No trend data available</p>
  </div>
);

export const LineChart: React.FC<LineChartProps> = ({
  data,
  title,
  xAxisLabel,
  yAxisLabel,
  loading = false,
  lineColor = '#6366f1',
  lineColor2 = '#10b981',
  lineLabel,
  lineLabel2,
  showDots = true,
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
          <RechartsLineChart
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
            <Line
              type="monotone"
              dataKey="value"
              stroke={lineColor}
              strokeWidth={2}
              dot={showDots ? { r: 3, fill: lineColor, strokeWidth: 0 } : false}
              activeDot={{ r: 5, fill: lineColor, strokeWidth: 2, stroke: '#fff' }}
              name={lineLabel ?? 'Value'}
            />
            {data[0]?.value2 !== undefined && (
              <Line
                type="monotone"
                dataKey="value2"
                stroke={lineColor2}
                strokeWidth={2}
                dot={showDots ? { r: 3, fill: lineColor2, strokeWidth: 0 } : false}
                activeDot={{ r: 5, fill: lineColor2, strokeWidth: 2, stroke: '#fff' }}
                name={lineLabel2 ?? 'Value 2'}
              />
            )}
            {(lineLabel || lineLabel2) && (
              <Legend
                verticalAlign="bottom"
                iconType="line"
                iconSize={12}
                formatter={(value: string) => (
                  <span className="text-xs text-gray-600">{value}</span>
                )}
              />
            )}
          </RechartsLineChart>
        </ResponsiveContainer>
      )}
    </div>
  );
};
