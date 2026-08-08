/**
 * ResumeAnalytics — visualizes resume ATS health.
 *
 * Shows an animated ATS circular ring, the resume completion progress, the
 * quality status, the last review date, and a couple of actionable
 * suggestions from the dashboard response. Pure presentation.
 *
 * @author DevLaunch
 */

import React, { useMemo } from 'react';
import { FileText, CalendarDays, Sparkles, TrendingUp, TrendingDown } from 'lucide-react';
import { AnalyticsCard } from './AnalyticsCard';
import { useCountUp } from '../../../hooks/useCountUp';
import { formatDate } from '../../../utils/date';
import type { DashboardResponse } from '../../../types/dashboard';

interface ResumeAnalyticsProps {
  /** The aggregated dashboard data. */
  data: DashboardResponse;
  /** Whether the data is still loading. */
  loading?: boolean;
}

/** Animated ATS circular ring coloured by score quality. */
const AtsRing: React.FC<{ score: number | null }> = ({ score }) => {
  const animated = useCountUp(score ?? 0, 1100);
  const size = 108;
  const strokeWidth = 11;
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference - (animated / 100) * circumference;

  const color =
    score === null ? '#9ca3af' : score >= 80 ? '#10b981' : score >= 60 ? '#8b5cf6' : score >= 40 ? '#f59e0b' : '#ef4444';

  return (
    <div className="relative inline-flex items-center justify-center">
      <svg width={size} height={size} className="-rotate-90">
        <circle cx={size / 2} cy={size / 2} r={radius} fill="none" stroke="#eef0f4" strokeWidth={strokeWidth} />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke={color}
          strokeWidth={strokeWidth}
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
        />
      </svg>
      <div className="absolute flex flex-col items-center">
        {score !== null ? (
          <>
            <span className="text-2xl font-bold tracking-tight text-gray-900">
              {Math.round(animated)}
            </span>
            <span className="text-[9px] font-medium uppercase tracking-wider text-gray-400">
              / 100
            </span>
          </>
        ) : (
          <span className="text-xl font-bold text-gray-300">—</span>
        )}
      </div>
    </div>
  );
};

export const ResumeAnalytics: React.FC<ResumeAnalyticsProps> = ({ data, loading = false }) => {
  const score = data.atsScore;
  const status = data.resumeQualityStatus ?? 'Not reviewed yet';

  const suggestions = useMemo(() => {
    const list = data.readinessRecommendations.length > 0
      ? data.readinessRecommendations
      : data.readinessImprovements;
    return list.slice(0, 2);
  }, [data]);

  const trendTone =
    score === null
      ? 'text-gray-400'
      : score >= 80
        ? 'text-emerald-600'
        : score >= 60
          ? 'text-violet-600'
          : score >= 40
            ? 'text-amber-600'
            : 'text-red-500';

  return (
    <AnalyticsCard
      title="Resume Analytics"
      subtitle="ATS compatibility & completeness"
      icon={<FileText className="h-5 w-5" />}
      tone="success"
      loading={loading}
    >
      <div className="flex items-center gap-4">
        <AtsRing score={score} />

        <div className="min-w-0 flex-1">
          <span
            className={`inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-xs font-semibold ${
              score === null
                ? 'bg-gray-100 text-gray-500'
                : score >= 80
                  ? 'bg-emerald-100 text-emerald-700'
                  : score >= 60
                    ? 'bg-violet-100 text-violet-700'
                    : score >= 40
                      ? 'bg-amber-100 text-amber-700'
                      : 'bg-red-100 text-red-700'
            }`}
          >
            {status}
          </span>

          {/* Trend indicator */}
          <div className="mt-2 flex items-center gap-1.5 text-xs">
            {score !== null && score >= 60 ? (
              <>
                <TrendingUp className={`h-3.5 w-3.5 ${trendTone}`} />
                <span className={trendTone}>Strong ATS profile</span>
              </>
            ) : score !== null ? (
              <>
                <TrendingDown className={`h-3.5 w-3.5 ${trendTone}`} />
                <span className={trendTone}>Room for improvement</span>
              </>
            ) : (
              <span className="text-gray-400">Run a review to score your resume</span>
            )}
          </div>

          {/* Completion */}
          <div className="mt-3">
            <div className="flex items-center justify-between text-[11px] text-gray-400">
              <span>Completion</span>
              <span className="font-semibold text-gray-700">{data.resumeCompletion}%</span>
            </div>
            <div className="mt-1 h-1.5 w-full overflow-hidden rounded-full bg-gray-100">
              <div
                className="h-full rounded-full bg-gradient-to-r from-violet-500 to-emerald-500 transition-all duration-1000"
                style={{ width: `${Math.min(100, Math.max(0, data.resumeCompletion))}%` }}
              />
            </div>
          </div>

          {/* Review date */}
          <p className="mt-2.5 flex items-center gap-1.5 text-[11px] text-gray-400">
            <CalendarDays className="h-3 w-3" />
            Last reviewed {data.atsReviewedAt ? formatDate(data.atsReviewedAt) : 'never'}
          </p>
        </div>
      </div>

      {/* Suggestions */}
      {suggestions.length > 0 && (
        <div className="mt-4">
          <p className="mb-2 flex items-center gap-1.5 text-[11px] font-semibold uppercase tracking-wide text-gray-400">
            <Sparkles className="h-3 w-3 text-violet-500" /> Suggested Next Steps
          </p>
          <ul className="space-y-2">
            {suggestions.map((suggestion) => (
              <li
                key={suggestion}
                className="flex items-start gap-2 rounded-xl border border-gray-100 bg-gray-50/60 px-3 py-2 text-xs leading-relaxed text-gray-600"
              >
                <span className="mt-1 h-1.5 w-1.5 shrink-0 rounded-full bg-violet-500" />
                {suggestion}
              </li>
            ))}
          </ul>
        </div>
      )}
    </AnalyticsCard>
  );
};
