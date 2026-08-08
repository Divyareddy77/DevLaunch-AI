/**
 * AnalyticsSection — the Analytics & Insights layer of the dashboard.
 *
 * Fetches the supplementary analytics data (job analytics, study tasks,
 * GitHub repositories/languages) alongside the already-loaded dashboard
 * and interview-history data, then renders a grid of premium analytics
 * cards plus an AI-style insights panel. All fetches are supplementary —
 * failures never block the section, which falls back to the dashboard
 * response fields.
 *
 * Lazy-loaded from the dashboard page to keep the initial render light.
 *
 * @author DevLaunch
 */

import React, { useEffect, useMemo, useState } from 'react';
import {
  Sparkles,
  TrendingUp,
  TrendingDown,
  ShieldCheck,
  Mic,
  CalendarCheck,
  Code2,
  Github,
  Flame,
  Briefcase,
  Target,
  type LucideIcon,
} from 'lucide-react';
import { jobApplicationService } from '../../../services/job-application.service';
import { studyPlannerService } from '../../../services/study-planner.service';
import { githubService } from '../../../services/github.service';
import { AnalyticsCard } from './AnalyticsCard';
import { PlacementAnalytics } from './PlacementAnalytics';
import { InterviewAnalytics } from './InterviewAnalytics';
import { JobTrackerAnalytics } from './JobTrackerAnalytics';
import { StudyAnalytics } from './StudyAnalytics';
import { GitHubAnalytics } from './GitHubAnalytics';
import { LeetCodeAnalytics } from './LeetCodeAnalytics';
import { ResumeAnalytics } from './ResumeAnalytics';
import { CARD_TONES, type CardTone } from '../cardTones';
import type { DashboardResponse } from '../../../types/dashboard';
import type { AchievementSummary } from '../../../types/achievement';
import type { InterviewHistoryResponse } from '../../../types/ai';
import type { ApplicationAnalytics } from '../../../types/job-application';
import type { StudyPlannerResponse } from '../../../types/study-planner';
import type { RepositoryResponse, LanguageStatisticsResponse } from '../../../types/github';

interface AnalyticsSectionProps {
  /** The aggregated dashboard data (always available). */
  data: DashboardResponse;
  /** Interview history fetched by the dashboard page, or null. */
  history: InterviewHistoryResponse | null;
  /** Whether the interview-history fetch has settled. */
  historyLoaded?: boolean;
  /** Achievement summary fetched by the dashboard page, or null. */
  summary: AchievementSummary | null;
  /** Current practice streak fetched by the dashboard page, or null. */
  streak: number | null;
}

/** A single generated insight. */
interface Insight {
  id: string;
  icon: LucideIcon;
  tone: CardTone;
  title: string;
  text: string;
}

/** Memoized card wrappers to avoid re-renders when only loading flags change. */
const Placement = React.memo(PlacementAnalytics);
const Interview = React.memo(InterviewAnalytics);
const JobTracker = React.memo(JobTrackerAnalytics);
const Study = React.memo(StudyAnalytics);
const GitHub = React.memo(GitHubAnalytics);
const LeetCode = React.memo(LeetCodeAnalytics);
const Resume = React.memo(ResumeAnalytics);

export const AnalyticsSection: React.FC<AnalyticsSectionProps> = ({
  data,
  history,
  historyLoaded = true,
  summary,
  streak,
}) => {
  const [jobAnalytics, setJobAnalytics] = useState<ApplicationAnalytics | null>(null);
  const [jobLoading, setJobLoading] = useState(true);
  const [studyTasks, setStudyTasks] = useState<StudyPlannerResponse[] | null>(null);
  const [studyLoading, setStudyLoading] = useState(true);
  const [repos, setRepos] = useState<RepositoryResponse[] | null>(null);
  const [reposLoading, setReposLoading] = useState(data.githubUsername !== null);
  const [languages, setLanguages] = useState<LanguageStatisticsResponse[] | null>(null);

  useEffect(() => {
    jobApplicationService
      .getAnalytics()
      .then(setJobAnalytics)
      .catch(() => setJobAnalytics(null))
      .finally(() => setJobLoading(false));

    studyPlannerService
      .getAll()
      .then(setStudyTasks)
      .catch(() => setStudyTasks(null))
      .finally(() => setStudyLoading(false));
  }, []);

  useEffect(() => {
    if (!data.githubUsername) {
      setReposLoading(false);
      return;
    }
    githubService
      .getRepositories(data.githubUsername)
      .then(setRepos)
      .catch(() => setRepos(null))
      .finally(() => setReposLoading(false));
    githubService
      .getLanguageStatistics(data.githubUsername)
      .then(setLanguages)
      .catch(() => setLanguages(null));
  }, [data.githubUsername]);

  // ---- AI-style insights generated from real data ----
  const insights = useMemo<Insight[]>(() => {
    const list: Insight[] = [];

    const interviewsThisWeek = (history?.history ?? []).filter((item) => {
      const date = new Date(item.completedAt);
      const weekAgo = Date.now() - 7 * 24 * 60 * 60 * 1000;
      return !Number.isNaN(date.getTime()) && date.getTime() >= weekAgo;
    }).length;

    if (data.readinessChange != null && data.readinessChange !== 0) {
      const up = data.readinessChange > 0;
      list.push({
        id: 'readiness',
        icon: up ? TrendingUp : TrendingDown,
        tone: up ? 'success' : 'danger',
        title: up ? 'Placement momentum' : 'Readiness dipped',
        text: up
          ? `You're ${data.readinessChange} points closer to placement readiness.`
          : `Your readiness is down ${Math.abs(data.readinessChange)} points since the last update.`,
      });
    }

    if (data.atsScore != null) {
      list.push({
        id: 'ats',
        icon: ShieldCheck,
        tone: 'violet',
        title: 'Resume strength',
        text: `Your resume scores ${data.atsScore}/100 on ATS — ${data.resumeQualityStatus ?? 'reviewed'}.`,
      });
    }

    if (interviewsThisWeek > 0) {
      list.push({
        id: 'interviews-week',
        icon: Mic,
        tone: 'violet',
        title: 'Practice habit',
        text: `You completed ${interviewsThisWeek} mock interview${interviewsThisWeek === 1 ? '' : 's'} this week.`,
      });
    } else if (data.mockInterviewCount > 0) {
      list.push({
        id: 'interviews-total',
        icon: Mic,
        tone: 'violet',
        title: 'Interview experience',
        text: `You've completed ${data.mockInterviewCount} mock interview${data.mockInterviewCount === 1 ? '' : 's'} so far.`,
      });
    }

    if (streak !== null && streak > 0) {
      list.push({
        id: 'streak',
        icon: Flame,
        tone: 'gold',
        title: 'Consistency',
        text: `You're on a ${streak}-day practice streak. Keep it alive!`,
      });
    }

    if (data.totalStudyTasks > 0) {
      const rate = Math.round((data.completedStudyTasks / data.totalStudyTasks) * 100);
      list.push({
        id: 'study',
        icon: CalendarCheck,
        tone: 'warning',
        title: 'Study consistency',
        text: `Your study plan is ${rate}% complete — ${data.completedStudyTasks} of ${data.totalStudyTasks} tasks done.`,
      });
    }

    if (data.leetcodeSolved > 0) {
      list.push({
        id: 'leetcode',
        icon: Code2,
        tone: 'danger',
        title: 'Problem solving',
        text: `You've solved ${data.leetcodeSolved} LeetCode problems — ${data.leetcodeEasySolved ?? 0} easy, ${data.leetcodeMediumSolved ?? 0} medium, ${data.leetcodeHardSolved ?? 0} hard.`,
      });
    }

    if (data.githubRepositories > 0) {
      list.push({
        id: 'github',
        icon: Github,
        tone: 'info',
        title: 'Open-source presence',
        text: `Your GitHub profile has ${data.githubRepositories} public repositories.`,
      });
    }

    if (jobAnalytics?.offerRate != null && jobAnalytics.offerRate > 0) {
      list.push({
        id: 'offers',
        icon: Briefcase,
        tone: 'success',
        title: 'Pipeline wins',
        text: `Your job application offer rate is ${jobAnalytics.offerRate}%.`,
      });
    }

    if (history?.successRate != null) {
      list.push({
        id: 'success-rate',
        icon: Target,
        tone: 'success',
        title: 'Interview success',
        text: `${history.successRate}% of your interview sessions scored 70 or higher.`,
      });
    }

    return list.slice(0, 6);
  }, [data, history, streak, jobAnalytics]);

  return (
    <section className="animate-fade-in-up">
      <div className="mb-4 flex items-end justify-between">
        <div>
          <h2 className="flex items-center gap-2 text-lg font-semibold tracking-tight text-gray-900">
            <Sparkles className="h-5 w-5 text-violet-500" />
            Analytics &amp; Insights
          </h2>
          <p className="text-sm text-gray-500">
            Track your progress with rich, real-time visualizations
          </p>
        </div>
        {summary?.level != null && (
          <span className="hidden rounded-full bg-violet-50 px-3 py-1 text-xs font-semibold text-violet-600 sm:inline-flex">
            Level {summary.level}
          </span>
        )}
      </div>

      <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
        <Placement data={data} />
        <Interview data={data} history={history} loading={!historyLoaded} />
        <JobTracker data={data} analytics={jobAnalytics} loading={jobLoading} />

        <Study data={data} tasks={studyTasks} loading={studyLoading} />
        <GitHub data={data} repos={repos} languages={languages} loading={reposLoading} />
        <LeetCode data={data} />

        <Resume data={data} />

        {/* AI Insights panel */}
        <div className="xl:col-span-2">
          <AnalyticsCard
            title="AI Insights"
            subtitle="Smart observations from your activity"
            icon={<Sparkles className="h-5 w-5" />}
            tone="violet"
          >
            {insights.length === 0 ? (
              <div className="flex flex-col items-center py-8 text-center">
                <Sparkles className="mb-2 h-8 w-8 text-gray-300" />
                <p className="text-sm font-medium text-gray-500">No insights yet</p>
                <p className="mt-0.5 text-xs text-gray-400">
                  Complete activities across modules to unlock smart observations
                </p>
              </div>
            ) : (
              <div className="grid gap-3 sm:grid-cols-2">
                {insights.map((insight) => {
                  const t = CARD_TONES[insight.tone];
                  return (
                    <div
                      key={insight.id}
                      className="flex items-start gap-3 rounded-xl border border-gray-100 bg-gray-50/60 p-3 transition-colors hover:border-gray-200"
                    >
                      <span className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-lg ${t.chip}`}>
                        <insight.icon className="h-4 w-4" />
                      </span>
                      <div className="min-w-0">
                        <p className="text-xs font-semibold text-gray-800">{insight.title}</p>
                        <p className="mt-0.5 text-xs leading-relaxed text-gray-500">
                          {insight.text}
                        </p>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </AnalyticsCard>
        </div>
      </div>
    </section>
  );
};
