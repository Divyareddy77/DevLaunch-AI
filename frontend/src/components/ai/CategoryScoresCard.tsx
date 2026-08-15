/**
 * CategoryScoresCard — the weighted category breakdown of the ATS score.
 *
 * Renders one progress bar per category with its achieved and maximum
 * points. The category scores always sum to the overall ATS score.
 *
 * @author DevLaunch
 */

import React from 'react';
import { PieChart } from 'lucide-react';
import { Card } from '../ui/Card';
import type { CategoryScore } from '../../types/ai';

interface CategoryScoresCardProps {
  /** The weighted category breakdown of the ATS score. */
  categoryScores: CategoryScore[];
}

/** Maps a score ratio to bar fill and text colours. */
function ratioClasses(ratio: number): { bar: string; text: string } {
  if (ratio >= 0.8) return { bar: 'bg-emerald-500', text: 'text-emerald-600' };
  if (ratio >= 0.6) return { bar: 'bg-indigo-500', text: 'text-indigo-600' };
  if (ratio >= 0.4) return { bar: 'bg-amber-500', text: 'text-amber-600' };
  return { bar: 'bg-red-500', text: 'text-red-600' };
}

export const CategoryScoresCard: React.FC<CategoryScoresCardProps> = ({ categoryScores }) => {
  return (
    <Card
      header={
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-indigo-100">
            <PieChart className="h-5 w-5 text-indigo-600" />
          </div>
          <div>
            <h3 className="text-sm font-semibold text-gray-900">Category Scores</h3>
            <p className="text-xs text-gray-400">Weighted breakdown that sums to your ATS score.</p>
          </div>
        </div>
      }
    >
      {categoryScores.length === 0 ? (
        <p className="text-sm text-gray-400">No category breakdown available.</p>
      ) : (
        <ul className="grid gap-x-8 gap-y-4 sm:grid-cols-2">
          {categoryScores.map((item) => {
            const ratio = item.maxScore > 0 ? item.score / item.maxScore : 0;
            const colors = ratioClasses(ratio);
            return (
              <li key={item.category}>
                <div className="mb-1 flex items-center justify-between gap-3">
                  <span className="text-sm font-medium text-gray-700">{item.category}</span>
                  <span className={`text-sm font-semibold ${colors.text}`}>
                    {item.score}
                    <span className="ml-0.5 text-xs font-normal text-gray-400">
                      / {item.maxScore}
                    </span>
                  </span>
                </div>
                <div className="h-2 w-full overflow-hidden rounded-full bg-gray-100">
                  <div
                    className={`h-2 rounded-full transition-all duration-700 ${colors.bar}`}
                    style={{ width: `${Math.round(ratio * 100)}%` }}
                  />
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </Card>
  );
};
