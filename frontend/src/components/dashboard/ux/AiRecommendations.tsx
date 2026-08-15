/**
 * AiRecommendations — a prioritized "next steps" panel.
 *
 * Derives actionable recommendations from existing dashboard data: each
 * recommendation shows a priority badge, progress toward its goal, an
 * estimated time, and an action button that jumps into the module. All
 * progress values are real metrics from the dashboard response.
 *
 * @author DevLaunch
 */

import React, { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  ShieldCheck,
  CalendarCheck,
  Code2,
  Github,
  Mic,
  Sparkles,
  Clock3,
  ArrowRight,
  CheckCircle2,
  type LucideIcon,
} from 'lucide-react';
import { AnalyticsCard } from '../analytics/AnalyticsCard';
import { CARD_TONES, type CardTone } from '../cardTones';
import { ROUTES } from '../../../constants/routes';
import type { DashboardResponse } from '../../../types/dashboard';

interface AiRecommendationsProps {
  /** The aggregated dashboard data. */
  data: DashboardResponse;
  /** Additional CSS classes (e.g. grid column spans). */
  className?: string;
}

interface Recommendation {
  id: string;
  title: string;
  description: string;
  icon: LucideIcon;
  tone: CardTone;
  priority: 'High' | 'Medium' | 'Low';
  priorityTone: CardTone;
  progress: number;
  progressLabel: string;
  time: string;
  to: string;
}

const PRIORITY_STYLES: Record<Recommendation['priority'], string> = {
  High: 'bg-red-50 text-red-600',
  Medium: 'bg-amber-50 text-amber-600',
  Low: 'bg-gray-100 text-gray-600',
};

export const AiRecommendations: React.FC<AiRecommendationsProps> = ({ data, className }) => {
  const navigate = useNavigate();

  const recommendations = useMemo<Recommendation[]>(() => {
    const list: Recommendation[] = [];

    // Resume ATS
    if (data.atsScore === null || data.atsScore < 80) {
      list.push({
        id: 'ats',
        title: 'Improve your ATS score',
        description:
          data.atsScore === null
            ? 'Run an AI review to score your resume'
            : `Current score ${data.atsScore}/100 — re-run the review after edits`,
        icon: ShieldCheck,
        tone: 'violet',
        priority: data.atsScore === null || data.atsScore < 60 ? 'High' : 'Medium',
        priorityTone: data.atsScore === null || data.atsScore < 60 ? 'danger' : 'warning',
        progress: data.atsScore ?? 0,
        progressLabel: data.atsScore === null ? 'Not scored yet' : `${data.atsScore}/100`,
        time: 'Est. 30 min',
        to: ROUTES.RESUME_REVIEW,
      });
    }

    // Study tasks
    const pendingStudy = data.totalStudyTasks - data.completedStudyTasks;
    if (pendingStudy > 0) {
      const rate = data.totalStudyTasks > 0 ? Math.round((data.completedStudyTasks / data.totalStudyTasks) * 100) : 0;
      list.push({
        id: 'study',
        title: 'Complete pending study tasks',
        description: `${pendingStudy} task${pendingStudy === 1 ? '' : 's'} remaining this month`,
        icon: CalendarCheck,
        tone: 'warning',
        priority: rate < 40 ? 'High' : 'Medium',
        priorityTone: rate < 40 ? 'danger' : 'warning',
        progress: rate,
        progressLabel: `${rate}% complete`,
        time: 'Est. 1 hr',
        to: ROUTES.STUDY_PLANNER_LIST,
      });
    }

    // LeetCode
    if (data.leetcodeSolved < 150) {
      const progress = Math.min(100, Math.round((data.leetcodeSolved / 150) * 100));
      list.push({
        id: 'leetcode',
        title: 'Solve more LeetCode problems',
        description: `Build toward 150 solved — you're at ${data.leetcodeSolved}`,
        icon: Code2,
        tone: 'danger',
        priority: data.leetcodeSolved < 50 ? 'High' : 'Medium',
        priorityTone: data.leetcodeSolved < 50 ? 'danger' : 'warning',
        progress,
        progressLabel: `${data.leetcodeSolved} solved`,
        time: 'Est. 2 hrs/wk',
        to: ROUTES.LEETCODE_TRACKER,
      });
    }

    // GitHub
    if (data.githubRepositories < 10) {
      const progress = Math.min(100, Math.round((data.githubRepositories / 10) * 100));
      list.push({
        id: 'github',
        title: 'Add GitHub projects',
        description: `Showcase your work — ${data.githubRepositories} public repos so far`,
        icon: Github,
        tone: 'info',
        priority: 'Low',
        priorityTone: 'gray',
        progress,
        progressLabel: `${data.githubRepositories}/10 repos`,
        time: 'Est. 1 hr',
        to: ROUTES.GITHUB_ANALYTICS,
      });
    }

    // Mock interview
    if (data.mockInterviewCount < 5 || (data.mockInterviewLatestScore ?? 0) < 75) {
      list.push({
        id: 'interview',
        title: 'Take a mock interview',
        description:
          data.mockInterviewCount === 0
            ? 'Complete your first AI interview session'
            : `Latest score ${data.mockInterviewLatestScore}/100 — practice to improve`,
        icon: Mic,
        tone: 'violet',
        priority: data.mockInterviewCount === 0 ? 'High' : 'Medium',
        priorityTone: data.mockInterviewCount === 0 ? 'danger' : 'warning',
        progress: data.mockInterviewAverageScore ?? 0,
        progressLabel: `Avg ${data.mockInterviewAverageScore ?? '—'}/100`,
        time: 'Est. 45 min',
        to: ROUTES.MOCK_INTERVIEW,
      });
    }

    // High priority first, then medium, then low.
    const rank = { High: 0, Medium: 1, Low: 2 } as const;
    return list.sort((a, b) => rank[a.priority] - rank[b.priority]).slice(0, 4);
  }, [data]);

  return (
    <AnalyticsCard
      title="AI Recommendations"
      subtitle="Prioritized next steps"
      icon={<Sparkles className="h-5 w-5" />}
      tone="violet"
      className={className}
    >
      {recommendations.length === 0 ? (
        <div className="flex flex-col items-center py-6 text-center">
          <CheckCircle2 className="mb-2 h-8 w-8 text-emerald-400" />
          <p className="text-sm font-medium text-gray-700">You&apos;re on track!</p>
          <p className="mt-0.5 text-xs text-gray-400">
            No high-priority gaps detected — keep up the momentum.
          </p>
        </div>
      ) : (
        <ul className="space-y-3">
          {recommendations.map((rec) => {
            const t = CARD_TONES[rec.tone];
            return (
              <li
                key={rec.id}
                className="rounded-xl border border-gray-100 bg-gray-50/60 p-3 transition-colors hover:border-gray-200"
              >
                {/* Title row */}
                <div className="flex items-center gap-2.5">
                  <span className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-lg ${t.chip}`}>
                    <rec.icon className="h-4 w-4" />
                  </span>
                  <p className="min-w-0 flex-1 truncate text-sm font-semibold text-gray-800">
                    {rec.title}
                  </p>
                  <span
                    className={`shrink-0 rounded-full px-2 py-0.5 text-[10px] font-bold ${PRIORITY_STYLES[rec.priority]}`}
                  >
                    {rec.priority}
                  </span>
                </div>

                <p className="mt-1.5 text-xs leading-relaxed text-gray-500">{rec.description}</p>

                {/* Progress */}
                <div className="mt-2 flex items-center gap-2">
                  <div className="h-1.5 flex-1 overflow-hidden rounded-full bg-gray-100">
                    <div
                      className={`h-full rounded-full transition-all duration-1000 ${t.bar}`}
                      style={{ width: `${Math.min(100, Math.max(0, rec.progress))}%` }}
                    />
                  </div>
                  <span className="shrink-0 text-[10px] font-semibold text-gray-500">
                    {rec.progressLabel}
                  </span>
                </div>

                {/* Footer: time + action */}
                <div className="mt-2 flex items-center justify-between">
                  <span className="inline-flex items-center gap-1 text-[11px] text-gray-400">
                    <Clock3 className="h-3 w-3" />
                    {rec.time}
                  </span>
                  <button
                    type="button"
                    onClick={() => navigate(rec.to)}
                    className="inline-flex items-center gap-1 rounded-lg bg-white px-2.5 py-1 text-[11px] font-semibold text-gray-700 shadow-sm ring-1 ring-inset ring-gray-200 transition-all duration-200 hover:-translate-y-0.5 hover:text-gray-900 hover:shadow focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 focus-visible:ring-offset-1"
                  >
                    Start
                    <ArrowRight className="h-3 w-3" />
                  </button>
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </AnalyticsCard>
  );
};
