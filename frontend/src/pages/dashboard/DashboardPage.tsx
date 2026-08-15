/**
 * DashboardPage — the central overview screen.
 *
 * Aggregates key metrics from across the DevLaunch platform into a single
 * premium view: a gradient hero banner with quick actions, the placement
 * readiness centrepiece, a KPI quick-stats grid, and the detailed module
 * widgets. Displays resume completion, job application stats, study
 * progress, GitHub overview, LeetCode status, and an overall placement
 * readiness score.
 *
 * @see backend/src/main/java/com/devlaunch/dto/response/DashboardResponse.java
 * @author DevLaunch
 */

import React, { Suspense, useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  FileText,
  Bell,
  ShieldCheck,
  Briefcase,
  Mic,
  CalendarCheck,
  Github,
  Code2,
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { useNotifications } from '../../context/NotificationContext';
import { dashboardService } from '../../services/dashboard.service';
import { announcementService } from '../../services/announcement.service';
import { achievementService } from '../../services/achievement.service';
import { aiService } from '../../services/ai.service';
import { AnnouncementBanner } from '../../components/announcement/AnnouncementBanner';
import { DashboardHero } from '../../components/dashboard/DashboardHero';
import { KpiCard } from '../../components/dashboard/KpiCard';
import { CountUp } from '../../components/ui/CountUp';
import { PlacementReadinessCard } from '../../components/dashboard/PlacementReadinessCard';
import { JobApplicationCard } from '../../components/dashboard/JobApplicationCard';
import { StudyPlannerCard } from '../../components/dashboard/StudyPlannerCard';
import { GitHubCard } from '../../components/dashboard/GitHubCard';
import { LeetCodeCard } from '../../components/dashboard/LeetCodeCard';
import { MockInterviewCard } from '../../components/dashboard/MockInterviewCard';
import { AchievementsWidget } from '../../components/dashboard/ux/AchievementsWidget';
import { QuickActionsPanel } from '../../components/dashboard/ux/QuickActionsPanel';
import { AiRecommendations } from '../../components/dashboard/ux/AiRecommendations';
import { ActivityTimeline } from '../../components/dashboard/ux/ActivityTimeline';
import { NotificationsWidget } from '../../components/dashboard/ux/NotificationsWidget';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { formatDate } from '../../utils/date';
import { ROUTES } from '../../constants/routes';
import type { CardTone } from '../../components/dashboard/cardTones';
import type { DashboardResponse } from '../../types/dashboard';
import type { AchievementSummary } from '../../types/achievement';
import type { InterviewHistoryResponse } from '../../types/ai';
import type { Announcement } from '../../types/announcement';

/**
 * Analytics & Insights section — lazy-loaded so the heavy chart bundle is
 * only requested after the dashboard's primary content paints.
 */
const AnalyticsSection = React.lazy(() =>
  import('../../components/dashboard/analytics/AnalyticsSection').then((module) => ({
    default: module.AnalyticsSection,
  })),
);

/** Text colour for the latest ATS score on the dashboard card. */
function atsScoreTextColor(score: number): string {
  if (score >= 80) return 'text-emerald-600';
  if (score >= 60) return 'text-violet-600';
  if (score >= 40) return 'text-amber-600';
  return 'text-red-600';
}

/** Progress bar tone for the latest ATS score. */
function atsBarTone(score: number | null): CardTone {
  if (score === null) return 'gray';
  if (score >= 80) return 'success';
  if (score >= 60) return 'violet';
  if (score >= 40) return 'warning';
  return 'danger';
}

/**
 * Skeleton shown while the lazy-loaded Analytics & Insights section
 * (and its chart bundle) is being fetched.
 */
const AnalyticsFallback: React.FC = () => (
  <section className="animate-pulse">
    <div className="mb-4">
      <div className="h-6 w-52 rounded bg-gray-200" />
      <div className="mt-1.5 h-4 w-72 rounded bg-gray-200" />
    </div>
    <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
      {Array.from({ length: 6 }).map((_, index) => (
        <div
          key={index}
          className="h-72 rounded-2xl border border-gray-100 bg-white p-5 shadow-[0_1px_3px_rgba(16,24,40,0.06)]"
        >
          <div className="flex items-center gap-2.5">
            <div className="h-9 w-9 rounded-xl bg-gray-200" />
            <div className="space-y-1.5">
              <div className="h-3.5 w-28 rounded bg-gray-200" />
              <div className="h-3 w-20 rounded bg-gray-200" />
            </div>
          </div>
          <div className="mt-5 flex h-28 items-end gap-1.5">
            {Array.from({ length: 8 }).map((_, bar) => (
              <div
                key={bar}
                className="flex-1 rounded-t bg-gray-200"
                style={{ height: `${25 + ((bar * 31) % 55)}%` }}
              />
            ))}
          </div>
        </div>
      ))}
    </div>
  </section>
);

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const { unreadCount } = useNotifications();
  const navigate = useNavigate();

  const [data, setData] = useState<DashboardResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Supplementary data for the hero banner + analytics — failures never block the page.
  const [summary, setSummary] = useState<AchievementSummary | null>(null);
  const [summaryLoaded, setSummaryLoaded] = useState<boolean>(false);
  const [interviewHistory, setInterviewHistory] = useState<InterviewHistoryResponse | null>(null);
  const [historyLoaded, setHistoryLoaded] = useState<boolean>(false);
  const [streak, setStreak] = useState<number | null>(null);

  // Active announcements shown above the dashboard cards. Fetched alongside
  // the dashboard but failures never block the dashboard itself.
  const [announcements, setAnnouncements] = useState<Announcement[]>([]);

  useEffect(() => {
    announcementService
      .getActive()
      .then(setAnnouncements)
      .catch(() => {
        // Announcements are supplementary — ignore failures silently.
      });

    achievementService
      .getSummary()
      .then(setSummary)
      .catch(() => setSummary(null))
      .finally(() => setSummaryLoaded(true));

    aiService
      .getInterviewHistory()
      .then((history) => {
        setInterviewHistory(history);
        setStreak(history.currentStreak ?? null);
      })
      .catch(() => setStreak(null))
      .finally(() => setHistoryLoaded(true));
  }, []);

  const fetchDashboard = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const result = await dashboardService.getDashboard();
      setData(result);
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : 'Failed to load dashboard data.';
      setError(message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchDashboard();
  }, [fetchDashboard]);

  // ---- Loading state ----
  if (isLoading) {
    return <LoadingScreen />;
  }

  // ---- Error state ----
  if (error || !data) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center">
        <ErrorMessage
          message={error ?? 'Unable to load dashboard data.'}
          onRetry={fetchDashboard}
        />
      </div>
    );
  }

  // ---- Navigation callbacks ----
  const goToResumes = () => navigate(ROUTES.RESUME_LIST);
  const goToResumeReview = () => navigate(ROUTES.RESUME_REVIEW);
  const goToJobs = () => navigate(ROUTES.JOB_APPLICATION_LIST);
  const goToStudy = () => navigate(ROUTES.STUDY_PLANNER_LIST);
  const goToGitHub = () => navigate(ROUTES.GITHUB_ANALYTICS);
  const goToLeetCode = () => navigate(ROUTES.LEETCODE_TRACKER);
  const goToMockInterview = () => navigate(ROUTES.MOCK_INTERVIEW);
  const goToNotifications = () => navigate(ROUTES.NOTIFICATIONS);

  // ---- Derived KPI values ----
  const studyRate =
    data.totalStudyTasks > 0
      ? Math.round((data.completedStudyTasks / data.totalStudyTasks) * 100)
      : 0;
  const jobInterviewRate =
    data.totalJobApplications > 0
      ? (data.interviewApplications / data.totalJobApplications) * 100
      : null;
  const atsScore = data.atsScore;
  const githubConnected = data.githubUsername !== null;
  const leetcodeConnected = data.leetcodeUsername !== null;
  const hasInterviews = data.mockInterviewCount > 0;

  return (
    <div className="animate-page-enter space-y-8">
      {/* ---- Premium hero banner ---- */}
      <div className="animate-fade-in-up">
        <DashboardHero
          firstName={user?.firstName}
          data={data}
          summary={summary}
          streak={streak}
        />
      </div>

      {/* ---- Announcements (active, newest first) ---- */}
      <div className="animate-fade-in-up" style={{ animationDelay: '60ms' }}>
        <AnnouncementBanner announcements={announcements} />
      </div>

      {/* ---- Placement Readiness (centrepiece) ---- */}
      <div className="animate-fade-in-up" style={{ animationDelay: '180ms' }}>
        <PlacementReadinessCard data={data} />
      </div>

      {/* ---- Quick statistics (KPI grid) ---- */}
      <section className="animate-fade-in-up" style={{ animationDelay: '120ms' }}>
        <div className="mb-4 flex items-end justify-between">
          <div>
            <h2 className="text-lg font-semibold tracking-tight text-gray-900">Quick Statistics</h2>
            <p className="text-sm text-gray-500">Your career preparation at a glance</p>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4 md:grid-cols-3 xl:grid-cols-4">
          {/* Resume ATS Score */}
          <KpiCard
            label="ATS Score"
            icon={<ShieldCheck className="h-5 w-5" />}
            tone="violet"
            value={
              atsScore !== null ? (
                <CountUp value={atsScore} className={atsScoreTextColor(atsScore)} />
              ) : (
                <span className="text-gray-300">—</span>
              )
            }
            subtitle={
              atsScore !== null
                ? `${data.resumeQualityStatus ?? 'Reviewed'}${
                    data.atsReviewedAt
                      ? ` · reviewed ${formatDate(data.atsReviewedAt)}`
                      : ''
                  }`
                : 'Run an AI review to see your resume score'
            }
            progress={atsScore}
            barTone={atsBarTone(atsScore)}
            onClick={goToResumeReview}
          />

          {/* Resume Completion */}
          <KpiCard
            label="Resume Completion"
            icon={<FileText className="h-5 w-5" />}
            tone="success"
            value={<CountUp value={data.resumeCompletion} suffix="%" />}
            subtitle={
              data.resumeCompletion === 100
                ? 'All fields completed!'
                : `${Math.round(data.resumeCompletion / 20)} of 5 fields filled`
            }
            progress={data.resumeCompletion}
            onClick={goToResumes}
          />

          {/* Job Applications */}
          <KpiCard
            label="Job Applications"
            icon={<Briefcase className="h-5 w-5" />}
            tone="orange"
            value={<CountUp value={data.totalJobApplications} />}
            subtitle={`${data.interviewApplications} interviews · ${
              data.offerApplications ?? 0
            } offers`}
            progress={jobInterviewRate}
            onClick={goToJobs}
          />

          {/* Mock Interviews */}
          <KpiCard
            label="Mock Interviews"
            icon={<Mic className="h-5 w-5" />}
            tone="violet"
            value={<CountUp value={data.mockInterviewCount} />}
            subtitle={
              hasInterviews
                ? `Avg ${data.mockInterviewAverageScore?.toFixed(0) ?? '—'} · Best ${
                    data.mockInterviewBestScore ?? '—'
                  }`
                : 'Complete your first interview'
            }
            progress={data.mockInterviewAverageScore}
            onClick={goToMockInterview}
          />

          {/* Study Planner */}
          <KpiCard
            label="Study Planner"
            icon={<CalendarCheck className="h-5 w-5" />}
            tone="warning"
            value={<CountUp value={data.completedStudyTasks} />}
            subtitle={`${studyRate}% complete · ${data.totalStudyTasks} total tasks`}
            progress={studyRate}
            onClick={goToStudy}
          />

          {/* GitHub Analytics */}
          <KpiCard
            label="GitHub"
            icon={<Github className="h-5 w-5" />}
            tone="info"
            value={
              githubConnected ? (
                <CountUp value={data.githubRepositories} />
              ) : (
                <span className="text-gray-300">—</span>
              )
            }
            subtitle={
              githubConnected
                ? `@${data.githubUsername} · ${data.githubFollowers ?? 0} followers`
                : 'Connect your account'
            }
            onClick={goToGitHub}
          />

          {/* LeetCode Progress */}
          <KpiCard
            label="LeetCode"
            icon={<Code2 className="h-5 w-5" />}
            tone="danger"
            value={
              leetcodeConnected ? (
                <CountUp value={data.leetcodeSolved} />
              ) : (
                <span className="text-gray-300">—</span>
              )
            }
            subtitle={
              leetcodeConnected
                ? `${data.leetcodeEasySolved ?? 0}E · ${data.leetcodeMediumSolved ?? 0}M · ${
                    data.leetcodeHardSolved ?? 0
                  }H`
                : 'Connect your account'
            }
            progress={data.leetcodeAcceptanceRate}
            onClick={goToLeetCode}
          />

          {/* Notifications */}
          <KpiCard
            label="Notifications"
            icon={<Bell className="h-5 w-5" />}
            tone={unreadCount > 0 ? 'danger' : 'gray'}
            value={<CountUp value={unreadCount} />}
            subtitle={
              unreadCount === 0
                ? 'All caught up'
                : `${unreadCount === 1 ? '1 notification' : `${unreadCount} notifications`} need attention`
            }
            onClick={goToNotifications}
          />
        </div>
      </section>

      {/* ---- Analytics & Insights (lazy-loaded) ---- */}
      <Suspense fallback={<AnalyticsFallback />}>
        <AnalyticsSection
          data={data}
          history={interviewHistory}
          historyLoaded={historyLoaded}
          summary={summary}
          streak={streak}
        />
      </Suspense>

      {/* ---- Recommendations & quick actions ---- */}
      <section className="animate-fade-in-up" style={{ animationDelay: '300ms' }}>
        <div className="mb-4 flex items-end justify-between">
          <div>
            <h2 className="text-lg font-semibold tracking-tight text-gray-900">
              Recommendations
            </h2>
            <p className="text-sm text-gray-500">AI-driven next steps and shortcuts to every module</p>
          </div>
        </div>
        <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
          <QuickActionsPanel />
          <AiRecommendations data={data} className="xl:col-span-2" />
        </div>
      </section>

      {/* ---- Recent activity ---- */}
      <section className="animate-fade-in-up" style={{ animationDelay: '340ms' }}>
        <div className="mb-4 flex items-end justify-between">
          <div>
            <h2 className="text-lg font-semibold tracking-tight text-gray-900">
              Recent Activity
            </h2>
            <p className="text-sm text-gray-500">Review your latest momentum across the platform</p>
          </div>
        </div>
        <ActivityTimeline />
      </section>

      {/* ---- Detailed module widgets ---- */}
      <section className="animate-fade-in-up" style={{ animationDelay: '380ms' }}>
        <div className="mb-4 flex items-end justify-between">
          <div>
            <h2 className="text-lg font-semibold tracking-tight text-gray-900">
              Modules
            </h2>
            <p className="text-sm text-gray-500">Deep-dive into every part of DevLaunch</p>
          </div>
        </div>

        <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
          <JobApplicationCard
            totalApplications={data.totalJobApplications}
            interviewApplications={data.interviewApplications}
            offerApplications={data.offerApplications}
            assessmentApplications={data.assessmentApplications}
            recentApplications={data.recentApplications}
            upcomingInterview={data.upcomingInterview}
            onViewAll={goToJobs}
          />

          <MockInterviewCard
            count={data.mockInterviewCount}
            latestScore={data.mockInterviewLatestScore}
            averageScore={data.mockInterviewAverageScore}
            bestScore={data.mockInterviewBestScore}
            trend={data.mockInterviewTrend}
            insight={data.mockInterviewInsight}
            onViewAll={goToMockInterview}
          />

          <StudyPlannerCard
            totalTasks={data.totalStudyTasks}
            completedTasks={data.completedStudyTasks}
            onViewAll={goToStudy}
          />

          <GitHubCard
            connected={githubConnected}
            username={data.githubUsername}
            repositoryCount={data.githubRepositories}
            topLanguage={data.githubTopLanguage}
            followers={data.githubFollowers}
            following={data.githubFollowing}
            onViewAll={goToGitHub}
            onConnect={goToGitHub}
          />

          <LeetCodeCard
            connected={leetcodeConnected}
            username={data.leetcodeUsername}
            totalSolved={data.leetcodeSolved}
            easySolved={data.leetcodeEasySolved}
            mediumSolved={data.leetcodeMediumSolved}
            hardSolved={data.leetcodeHardSolved}
            acceptanceRate={data.leetcodeAcceptanceRate}
            ranking={data.leetcodeRanking}
            onViewAll={goToLeetCode}
            onConnect={goToLeetCode}
          />

          {/* Achievements gamification widget */}
          <AchievementsWidget summary={summary} loading={!summaryLoaded} />
        </div>
      </section>

      {/* ---- Notifications ---- */}
      <section className="animate-fade-in-up" style={{ animationDelay: '420ms' }}>
        <div className="mb-4 flex items-end justify-between">
          <div>
            <h2 className="text-lg font-semibold tracking-tight text-gray-900">Notifications</h2>
            <p className="text-sm text-gray-500">Stay on top of platform updates</p>
          </div>
        </div>
        <NotificationsWidget unreadCount={unreadCount} />
      </section>
    </div>
  );
};
