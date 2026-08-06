/**
 * AtsScoreCard — the hero card of the professional ATS report.
 *
 * Renders the overall ATS score as a large circular progress ring with
 * a quality status badge, plus the secondary resume quality score as a
 * compact progress bar.
 *
 * @author DevLaunch
 */

import React from 'react';
import { ShieldCheck } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';

interface AtsScoreCardProps {
  /** The overall ATS score from 0–100. */
  atsScore: number;
  /** The secondary overall resume quality score from 0–100. */
  resumeScore: number;
}

/** Maps an ATS score to its display colour. */
function ringColor(score: number): string {
  if (score >= 80) return '#10b981'; // emerald
  if (score >= 60) return '#6366f1'; // indigo
  if (score >= 40) return '#f59e0b'; // amber
  return '#ef4444'; // red
}

/** Derives the quality status label and badge variant from the score. */
function statusOf(score: number): { label: string; variant: 'success' | 'warning' | 'danger' | 'info' } {
  if (score >= 80) return { label: 'Excellent', variant: 'success' };
  if (score >= 60) return { label: 'Good', variant: 'info' };
  if (score >= 40) return { label: 'Needs Improvement', variant: 'warning' };
  return { label: 'Action Required', variant: 'danger' };
}

export const AtsScoreCard: React.FC<AtsScoreCardProps> = ({ atsScore, resumeScore }) => {
  const size = 148;
  const strokeWidth = 12;
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference - (Math.max(0, Math.min(100, atsScore)) / 100) * circumference;
  const status = statusOf(atsScore);

  return (
    <Card>
      <div className="flex flex-col items-center gap-6 sm:flex-row sm:justify-between">
        <div className="text-center sm:text-left">
          <div className="flex items-center gap-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-violet-100">
              <ShieldCheck className="h-4 w-4 text-violet-600" />
            </div>
            <h2 className="text-base font-semibold text-gray-900">Overall ATS Score</h2>
          </div>
          <p className="mt-1 max-w-sm text-sm text-gray-500">
            How well your resume matches automated screening systems and the target role.
          </p>
          <div className="mt-3">
            <Badge variant={status.variant}>{status.label}</Badge>
          </div>

          {/* Secondary resume quality score */}
          <div className="mt-5 max-w-xs">
            <div className="mb-1 flex items-center justify-between">
              <span className="text-xs font-medium text-gray-500">Resume Quality</span>
              <span className="text-xs font-medium text-gray-700">{resumeScore}/100</span>
            </div>
            <div className="h-1.5 w-full overflow-hidden rounded-full bg-gray-100">
              <div
                className="h-1.5 rounded-full bg-violet-500 transition-all duration-700"
                style={{ width: `${Math.max(0, Math.min(100, resumeScore))}%` }}
              />
            </div>
          </div>
        </div>

        {/* Score ring */}
        <div className="relative inline-flex items-center justify-center">
          <svg width={size} height={size} className="-rotate-90">
            <circle
              cx={size / 2}
              cy={size / 2}
              r={radius}
              fill="none"
              stroke="#e5e7eb"
              strokeWidth={strokeWidth}
            />
            <circle
              cx={size / 2}
              cy={size / 2}
              r={radius}
              fill="none"
              stroke={ringColor(atsScore)}
              strokeWidth={strokeWidth}
              strokeLinecap="round"
              strokeDasharray={circumference}
              strokeDashoffset={offset}
              className="transition-all duration-700 ease-out"
            />
          </svg>
          <div className="absolute flex flex-col items-center">
            <span
              className="text-4xl font-bold"
              style={{ color: ringColor(atsScore) }}
            >
              {Math.round(atsScore)}
            </span>
            <span className="text-xs font-medium text-gray-400">out of 100</span>
          </div>
        </div>
      </div>
    </Card>
  );
};
