/**
 * ScoreCard — a single score tile (e.g. Resume Score, ATS Score)
 * with an icon, big number, and animated progress bar.
 *
 * @author DevLaunch
 */

import React from 'react';
import { type LucideIcon } from 'lucide-react';
import { Card } from '../ui/Card';

interface ScoreCardProps {
  /** The score label (e.g. "Resume Score"). */
  label: string;
  /** The score value from 0–100. */
  value: number;
  /** Icon rendered inside the coloured circle. */
  icon: LucideIcon;
  /** Background/text classes for the icon circle. */
  iconClassName: string;
  /** Background classes for the progress bar fill. */
  barClassName: string;
  /** Optional helper text shown below the score. */
  hint?: string;
}

export const ScoreCard: React.FC<ScoreCardProps> = ({
  label,
  value,
  icon: Icon,
  iconClassName,
  barClassName,
  hint,
}) => {
  return (
    <Card>
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm font-medium text-gray-500">{label}</p>
          <p className="mt-1 text-4xl font-bold text-gray-900">
            {Math.round(value)}
            <span className="ml-1 text-base font-medium text-gray-400">/ 100</span>
          </p>
          {hint && <p className="mt-1 text-xs text-gray-400">{hint}</p>}
        </div>
        <div
          className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-xl ${iconClassName}`}
        >
          <Icon className="h-5 w-5" />
        </div>
      </div>

      <div className="mt-4 h-2 w-full overflow-hidden rounded-full bg-gray-100">
        <div
          className={`h-2 rounded-full transition-all duration-700 ${barClassName}`}
          style={{ width: `${Math.max(0, Math.min(100, value))}%` }}
        />
      </div>
    </Card>
  );
};
