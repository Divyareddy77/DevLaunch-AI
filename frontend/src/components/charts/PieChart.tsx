/**
 * Generic pie chart component for displaying proportional data.
 *
 * Built on Recharts and designed to be reusable across:
 * - Dashboard (LeetCode difficulty breakdown)
 * - GitHub Analytics (language distribution)
 * - LeetCode Tracker (problem categories)
 *
 * @author DevLaunch
 */

import React from 'react';
import {
  PieChart as RechartsPieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';

/** A single data point for the pie chart. */
export interface PieChartDataPoint {
  /** Label for the segment. */
  name: string;
  /** Numeric value determining the segment size. */
  value: number;
  /** Optional hex colour for the segment. Auto-assigned if omitted. */
  color?: string;
}

interface PieChartProps {
  /** The data to visualise as pie segments. */
  data: PieChartDataPoint[];
  /** Title displayed above the chart. */
  title?: string;
  /** Whether the chart is in a loading state. */
  loading?: boolean;
  /** The inner radius for a donut chart (0 = pie, >0 = donut). */
  innerRadius?: number;
  /** Optional additional CSS classes. */
  className?: string;
}

/** Default colour palette used when a data point has no explicit color. */
const DEFAULT_COLORS = [
  '#6366f1', // indigo
  '#10b981', // emerald
  '#f59e0b', // amber
  '#ef4444', // red
  '#3b82f6', // blue
  '#8b5cf6', // purple
  '#ec4899', // pink
  '#14b8a6', // teal
];

const LoadingSkeleton: React.FC = () => (
  <div className="flex h-64 items-center justify-center">
    <div className="h-48 w-48 animate-pulse rounded-full bg-gray-200" />
  </div>
);

const EmptyState: React.FC = () => (
  <div className="flex h-64 items-center justify-center">
    <p className="text-sm text-gray-400">No data available</p>
  </div>
);

export const PieChart: React.FC<PieChartProps> = ({
  data,
  title,
  loading = false,
  innerRadius = 0,
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
          <RechartsPieChart>
            <Pie
              data={data}
              cx="50%"
              cy="50%"
              innerRadius={innerRadius}
              outerRadius={100}
              dataKey="value"
              nameKey="name"
              labelLine
              label={({ name, percent }) =>
                `${name} ${(percent * 100).toFixed(0)}%`
              }
            >
              {data.map((entry, index) => (
                <Cell
                  key={`cell-${index}`}
                  fill={entry.color ?? DEFAULT_COLORS[index % DEFAULT_COLORS.length]}
                />
              ))}
            </Pie>
            <Tooltip
              contentStyle={{
                borderRadius: '8px',
                border: '1px solid #e5e7eb',
                boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)',
              }}
            />
            <Legend
              verticalAlign="bottom"
              iconType="circle"
              iconSize={8}
              formatter={(value: string) => (
                <span className="text-xs text-gray-600">{value}</span>
              )}
            />
          </RechartsPieChart>
        </ResponsiveContainer>
      )}
    </div>
  );
};
