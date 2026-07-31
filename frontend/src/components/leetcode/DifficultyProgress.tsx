/**
 * DifficultyProgress — visualises a LeetCode user's solved-problem
 * breakdown by difficulty.
 *
 * Renders three animated progress bars (Easy, Medium, Hard) with
 * distinct colours, each showing the solved count and its share of
 * the total solved problems.
 *
 * @author DevLaunch
 */

import React from 'react';
import { BarChart3 } from 'lucide-react';
import { Card } from '../ui/Card';
import { formatNumber } from '../../utils/format';

interface DifficultyProgressProps {
  /** Total number of problems solved. */
  totalSolved: number;
  /** Number of easy-difficulty problems solved. */
  easySolved: number;
  /** Number of medium-difficulty problems solved. */
  mediumSolved: number;
  /** Number of hard-difficulty problems solved. */
  hardSolved: number;
}

interface DifficultyRow {
  /** Difficulty label. */
  label: string;
  /** Number of problems solved at this difficulty. */
  solved: number;
  /** Tailwind background colour for the progress bar and dot. */
  barColor: string;
}

export const DifficultyProgress: React.FC<DifficultyProgressProps> = ({
  totalSolved,
  easySolved,
  mediumSolved,
  hardSolved,
}) => {
  // Difficulty configuration — Easy green, Medium amber, Hard red.
  const rows: DifficultyRow[] = [
    { label: 'Easy', solved: easySolved, barColor: 'bg-emerald-500' },
    { label: 'Medium', solved: mediumSolved, barColor: 'bg-amber-500' },
    { label: 'Hard', solved: hardSolved, barColor: 'bg-red-500' },
  ];

  const percentageOf = (solved: number): number =>
    totalSolved > 0 ? Math.round((solved / totalSolved) * 100) : 0;

  return (
    <Card
      className="overflow-hidden"
      header={
        <div className="flex items-center gap-2">
          <BarChart3 className="h-5 w-5 text-orange-500" />
          <h2 className="text-lg font-semibold text-gray-900">Difficulty Analysis</h2>
        </div>
      }
    >
      <ul className="space-y-5">
        {rows.map((row) => {
          const percentage = percentageOf(row.solved);

          return (
            <li key={row.label}>
              <div className="flex items-center justify-between text-sm">
                <span className="inline-flex items-center gap-2 font-medium text-gray-700">
                  <span className={`h-2.5 w-2.5 rounded-full ${row.barColor}`} />
                  {row.label}
                </span>
                <span className="text-gray-500">
                  {formatNumber(row.solved)} solved · {percentage}%
                </span>
              </div>
              <div className="mt-1.5 h-2.5 w-full overflow-hidden rounded-full bg-gray-100">
                <div
                  className={`h-full rounded-full ${row.barColor} transition-all duration-700`}
                  style={{ width: `${percentage}%` }}
                />
              </div>
            </li>
          );
        })}
      </ul>
    </Card>
  );
};
