/**
 * MockInterviewLanding — the professional landing page for the AI Mock
 * Interview module.
 *
 * Shows a hero with the user's interview readiness level and headline
 * statistics (total interviews, best score, average, last interview,
 * streak), followed by the interview category cards with bank size,
 * difficulty, estimated duration, previous best score, and last attempt.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Award, TrendingUp, CalendarCheck, Flame, ArrowRight, Sparkles } from 'lucide-react';
import { Badge } from '../ui/Badge';
import { Spinner } from '../ui/Spinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import { MockInterviewEmptyState } from './MockInterviewEmptyState';
import {
  INTERVIEW_CATEGORIES,
  INTERVIEW_CATEGORY_META,
  MINUTES_PER_QUESTION,
} from '../../constants/interview';
import { formatDate } from '../../utils/date';
import { enumToLabel, getScoreBadgeVariant } from '../../utils/format';
import { InterviewCategory } from '../../types/ai';
import type { InterviewCategoryStats, InterviewHistoryResponse } from '../../types/ai';

interface MockInterviewLandingProps {
  /** The aggregate interview history, or null while loading. */
  history: InterviewHistoryResponse | null;
  /** The per-category statistics, or null while loading. */
  categories: InterviewCategoryStats[] | null;
  /** Whether the landing data is loading. */
  loading: boolean;
  /** A load error message, if any. */
  error: string | null;
  /** Called to retry loading the landing data. */
  onRetry: () => void;
  /** Called when a category card is clicked. */
  onSelectCategory: (category: InterviewCategory) => void;
}

/** A single hero statistic tile. */
const HeroStat: React.FC<{
  icon: React.ComponentType<{ className?: string }>;
  iconClassName: string;
  label: string;
  value: string;
}> = ({ icon: Icon, iconClassName, label, value }) => (
  <div className="flex items-center gap-3 rounded-xl bg-white/80 px-4 py-3 shadow-sm backdrop-blur">
    <div className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-lg ${iconClassName}`}>
      <Icon className="h-5 w-5" />
    </div>
    <div>
      <p className="text-lg font-bold leading-tight text-gray-900">{value}</p>
      <p className="text-xs text-gray-500">{label}</p>
    </div>
  </div>
);

export const MockInterviewLanding: React.FC<MockInterviewLandingProps> = ({
  history,
  categories,
  loading,
  error,
  onRetry,
  onSelectCategory,
}) => {
  if (loading && !history) {
    return (
      <div className="flex justify-center py-16">
        <Spinner label="Loading interview stats…" />
      </div>
    );
  }

  if (error && !history) {
    return <ErrorMessage message={error} onRetry={onRetry} />;
  }

  const hasInterviews = (history?.totalInterviews ?? 0) > 0;
  const estimatedMinutes = Math.round(10 * MINUTES_PER_QUESTION);

  return (
    <div className="space-y-8">
      {!hasInterviews ? (
        <MockInterviewEmptyState onStart={() => onSelectCategory(InterviewCategory.JAVA)} />
      ) : (
        /* ---- Hero ---- */
        <div className="overflow-hidden rounded-2xl bg-gradient-to-br from-primary-700 via-primary-600 to-indigo-600 p-6 shadow-lg sm:p-8">
          <div className="flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
            <div className="lg:max-w-sm">
              <span className="inline-flex items-center gap-1.5 rounded-full bg-white/15 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-white">
                <Sparkles className="h-3.5 w-3.5" />
                Interview Readiness
              </span>
              <h2 className="mt-3 text-3xl font-bold text-white">
                {history?.readinessLevel ?? 'Getting Started'}
              </h2>
              <p className="mt-1.5 text-sm text-primary-100">
                {history && history.totalInterviews > 0
                  ? `Average score ${history.averageScore.toFixed(1)}/100 across ${history.totalInterviews} ${
                      history.totalInterviews === 1 ? 'interview' : 'interviews'
                    }.`
                  : 'Complete your first interview to unlock your readiness level.'}
              </p>
            </div>

            <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-5">
              <HeroStat
                icon={Award}
                iconClassName="bg-white/15 text-white"
                label="Interviews"
                value={String(history?.totalInterviews ?? 0)}
              />
              <HeroStat
                icon={TrendingUp}
                iconClassName="bg-white/15 text-white"
                label="Best score"
                value={history?.bestScore != null ? `${history.bestScore}` : '—'}
              />
              <HeroStat
                icon={Award}
                iconClassName="bg-white/15 text-white"
                label="Average"
                value={history ? history.averageScore.toFixed(1) : '—'}
              />
              <HeroStat
                icon={CalendarCheck}
                iconClassName="bg-white/15 text-white"
                label="Last interview"
                value={history?.lastInterviewAt ? formatDate(history.lastInterviewAt) : '—'}
              />
              <HeroStat
                icon={Flame}
                iconClassName="bg-white/15 text-white"
                label="Current streak"
                value={history?.currentStreak ? `${history.currentStreak}d` : '0d'}
              />
            </div>
          </div>
        </div>
      )}

      {/* ---- Category cards ---- */}
      <div>
        <div className="mb-4 flex items-end justify-between gap-4">
          <div>
            <h3 className="text-lg font-semibold text-gray-900">Choose an interview track</h3>
            <p className="mt-0.5 text-sm text-gray-500">
              Pick a category to customise your session — difficulty, length, and mode.
            </p>
          </div>
          <span className="hidden text-xs text-gray-400 sm:block">
            ~{estimatedMinutes} min · AI-generated questions
          </span>
        </div>

        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {INTERVIEW_CATEGORIES.map((category) => {
            const meta = INTERVIEW_CATEGORY_META[category];
            const stats = categories?.find((entry) => entry.interviewType === category);
            const Icon = meta.icon;

            return (
              <button
                key={category}
                type="button"
                onClick={() => onSelectCategory(category)}
                className="group flex flex-col rounded-xl border border-gray-200 bg-white p-5 text-left shadow-sm transition-all hover:-translate-y-0.5 hover:border-primary-200 hover:shadow-md"
              >
                <div className="flex items-center justify-between gap-3">
                  <div
                    className={`flex h-11 w-11 items-center justify-center rounded-xl ${meta.iconClassName}`}
                  >
                    <Icon className="h-5 w-5" />
                  </div>
                  <ArrowRight className="h-4 w-4 text-gray-300 transition-transform group-hover:translate-x-1 group-hover:text-primary-500" />
                </div>

                <div className="mt-3 flex items-center gap-2">
                  <h4 className="text-base font-semibold text-gray-900">{meta.label}</h4>
                  <Badge variant="default" size="sm">
                    {meta.difficultyLabel}
                  </Badge>
                </div>
                <p className="mt-1 flex-1 text-xs leading-relaxed text-gray-500">
                  {meta.description}
                </p>

                <dl className="mt-4 grid grid-cols-2 gap-x-3 gap-y-2 border-t border-gray-100 pt-3 text-xs">
                  <div>
                    <dt className="text-gray-400">Question bank</dt>
                    <dd className="mt-0.5 font-semibold text-gray-700">
                      {stats?.questionBankSize ?? '—'} questions
                    </dd>
                  </div>
                  <div>
                    <dt className="text-gray-400">Est. duration</dt>
                    <dd className="mt-0.5 font-semibold text-gray-700">~{estimatedMinutes} min</dd>
                  </div>
                  <div>
                    <dt className="text-gray-400">Previous best</dt>
                    <dd className="mt-0.5 font-semibold text-gray-700">
                      {stats?.previousBestScore != null ? (
                        <Badge
                          variant={getScoreBadgeVariant(stats.previousBestScore)}
                          size="sm"
                        >
                          {stats.previousBestScore}/100
                        </Badge>
                      ) : (
                        'Not attempted'
                      )}
                    </dd>
                  </div>
                  <div>
                    <dt className="text-gray-400">Last attempt</dt>
                    <dd className="mt-0.5 font-semibold text-gray-700">
                      {stats?.lastAttemptAt ? formatDate(stats.lastAttemptAt) : 'Never'}
                    </dd>
                  </div>
                </dl>

                <div className="mt-4 flex items-center justify-between">
                  <span className="text-xs text-gray-400">
                    {stats?.attemptCount
                      ? `${stats.attemptCount} ${stats.attemptCount === 1 ? 'attempt' : 'attempts'}`
                      : `Category: ${enumToLabel(category)}`}
                  </span>
                  <span className="text-xs font-medium text-primary-600 opacity-0 transition-opacity group-hover:opacity-100">
                    Start now →
                  </span>
                </div>
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
};
