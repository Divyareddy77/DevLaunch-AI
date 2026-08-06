/**
 * DashboardPage — the central overview screen.
 *
 * Aggregates key metrics from across the DevLaunch platform into
 * a single view. Displays resume completion, job application stats,
 * study progress, GitHub overview, LeetCode status, and an overall
 * placement readiness score.
 *
 * @see backend/src/main/java/com/devlaunch/dto/response/DashboardResponse.java
 * @author DevLaunch
 */

import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  FileText,
  Award,
  Target,
  Bell,
  ShieldCheck,
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { useNotifications } from '../../context/NotificationContext';
import { dashboardService } from '../../services/dashboard.service';
import { announcementService } from '../../services/announcement.service';
import { AnnouncementBanner } from '../../components/announcement/AnnouncementBanner';
import { DashboardCard } from '../../components/dashboard/DashboardCard';
import { PlacementReadinessCard } from '../../components/dashboard/PlacementReadinessCard';
import { JobApplicationCard } from '../../components/dashboard/JobApplicationCard';
import { StudyPlannerCard } from '../../components/dashboard/StudyPlannerCard';
import { GitHubCard } from '../../components/dashboard/GitHubCard';
import { LeetCodeCard } from '../../components/dashboard/LeetCodeCard';
import { MockInterviewCard } from '../../components/dashboard/MockInterviewCard';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { formatDate } from '../../utils/date';
import { ROUTES } from '../../constants/routes';
import type { DashboardResponse } from '../../types/dashboard';
import type { Announcement } from '../../types/announcement';

/** Text colour for the latest ATS score on the dashboard card. */
function atsScoreTextColor(score: number): string {
  if (score >= 80) return 'text-emerald-600';
  if (score >= 60) return 'text-indigo-600';
  if (score >= 40) return 'text-amber-600';
  return 'text-red-600';
}

/** Badge colour for the resume quality status on the dashboard card. */
function atsScoreBadgeColor(score: number): string {
  if (score >= 80) return 'bg-emerald-100 text-emerald-700';
  if (score >= 60) return 'bg-indigo-100 text-indigo-700';
  if (score >= 40) return 'bg-amber-100 text-amber-700';
  return 'bg-red-100 text-red-700';
}

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const { unreadCount } = useNotifications();
  const navigate = useNavigate();

  const [data, setData] = useState<DashboardResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

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
  const goToJobs = () => navigate(ROUTES.JOB_APPLICATION_LIST);
  const goToStudy = () => navigate(ROUTES.STUDY_PLANNER_LIST);
  const goToGitHub = () => navigate(ROUTES.GITHUB_ANALYTICS);
  const goToLeetCode = () => navigate(ROUTES.LEETCODE_TRACKER);
  const goToMockInterview = () => navigate(ROUTES.MOCK_INTERVIEW);

  return (
    <div>
      {/* ---- Welcome header ---- */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900 sm:text-3xl">
          Welcome back, {user?.firstName ?? 'User'}
        </h1>
        <p className="mt-1 text-sm text-gray-500">
          Here&apos;s an overview of your career preparation progress.
        </p>
      </div>

      {/* ---- Announcements (active, newest first) ---- */}
      <AnnouncementBanner announcements={announcements} />

      {/* ---- Hero: Placement Readiness (enhanced) ---- */}
      <PlacementReadinessCard data={data} />

      {/* ---- Metrics grid ---- */}
      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {/* Resume ATS Score */}
        <DashboardCard
          title="Resume ATS Score"
          icon={<ShieldCheck className="h-5 w-5" />}
          action={
            <button
              onClick={() => navigate(ROUTES.RESUME_REVIEW)}
              className="text-xs font-medium text-indigo-600 hover:text-indigo-800 transition-colors"
            >
              Run Review
            </button>
          }
        >
          {data.atsScore !== null ? (
            <div className="flex flex-col items-center">
              <span
                className={`text-4xl font-bold ${atsScoreTextColor(data.atsScore)}`}
              >
                {data.atsScore}
                <span className="ml-1 text-base font-medium text-gray-400">/ 100</span>
              </span>
              <span
                className={`mt-1.5 rounded-full px-2.5 py-0.5 text-xs font-medium ${atsScoreBadgeColor(data.atsScore)}`}
              >
                {data.resumeQualityStatus ?? 'Reviewed'}
              </span>
              <p className="mt-2 text-xs text-gray-400">
                Last review {data.atsReviewedAt ? formatDate(data.atsReviewedAt) : '—'}
              </p>
            </div>
          ) : (
            <div className="flex flex-col items-center py-4">
              <ShieldCheck className="mb-2 h-8 w-8 text-gray-300" />
              <p className="text-sm font-medium text-gray-500">No ATS review yet</p>
              <p className="mt-0.5 text-xs text-gray-400">
                Run an AI review to see your resume score
              </p>
            </div>
          )}
        </DashboardCard>

        {/* Resume Completion */}
        <DashboardCard
          title="Resume Completion"
          icon={<FileText className="h-5 w-5" />}
          action={
            <button
              onClick={goToResumes}
              className="text-xs font-medium text-indigo-600 hover:text-indigo-800 transition-colors"
            >
              View Resumes
            </button>
          }
        >
          <div className="flex flex-col items-center">
            <span className="text-4xl font-bold text-gray-900">
              {data.resumeCompletion}%
            </span>
            <p className="mt-1 text-xs text-gray-400">
              {data.resumeCompletion === 100
                ? 'All fields completed!'
                : `${Math.round(data.resumeCompletion / 20)} of 5 fields filled`}
            </p>
            <div className="mt-4 h-2.5 w-full overflow-hidden rounded-full bg-gray-100">
              <div
                className="h-full rounded-full bg-indigo-500 transition-all duration-700"
                style={{ width: `${data.resumeCompletion}%` }}
              />
            </div>
          </div>
        </DashboardCard>

        {/* Job Applications */}
        <JobApplicationCard
          totalApplications={data.totalJobApplications}
          interviewApplications={data.interviewApplications}
          offerApplications={data.offerApplications}
          assessmentApplications={data.assessmentApplications}
          recentApplications={data.recentApplications}
          upcomingInterview={data.upcomingInterview}
          onViewAll={goToJobs}
        />

        {/* Mock Interviews */}
        <MockInterviewCard
          count={data.mockInterviewCount}
          latestScore={data.mockInterviewLatestScore}
          averageScore={data.mockInterviewAverageScore}
          bestScore={data.mockInterviewBestScore}
          trend={data.mockInterviewTrend}
          insight={data.mockInterviewInsight}
          onViewAll={goToMockInterview}
        />

        {/* Study Planner */}
        <StudyPlannerCard
          totalTasks={data.totalStudyTasks}
          completedTasks={data.completedStudyTasks}
          onViewAll={goToStudy}
        />

        {/* GitHub Analytics */}
        <GitHubCard
          connected={data.githubUsername !== null}
          username={data.githubUsername}
          repositoryCount={data.githubRepositories}
          topLanguage={data.githubTopLanguage}
          followers={data.githubFollowers}
          following={data.githubFollowing}
          onViewAll={goToGitHub}
          onConnect={goToGitHub}
        />

        {/* LeetCode Progress */}
        <LeetCodeCard
          connected={data.leetcodeUsername !== null}
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

        {/* Achievements summary placeholder */}
        <DashboardCard
          title="Achievements"
          icon={<Award className="h-5 w-5" />}
        >
          <div className="flex flex-col items-center py-4">
            <Target className="mb-2 h-8 w-8 text-gray-300" />
            <p className="text-sm font-medium text-gray-500">Start your journey</p>
            <p className="mt-0.5 text-xs text-gray-400">
              Complete tasks across modules to earn achievements
            </p>
          </div>
        </DashboardCard>

        {/* Notifications summary */}
        <DashboardCard
          title="Notifications"
          icon={<Bell className="h-5 w-5" />}
          action={
            <button
              onClick={() => navigate(ROUTES.NOTIFICATIONS)}
              className="text-xs font-medium text-indigo-600 hover:text-indigo-800 transition-colors"
            >
              View All
            </button>
          }
        >
          <div className="flex flex-col items-center">
            <span
              className={`text-4xl font-bold ${
                unreadCount > 0 ? 'text-red-500' : 'text-gray-900'
              }`}
            >
              {unreadCount}
            </span>
            <p className="mt-1 text-xs text-gray-400">
              {unreadCount === 0
                ? 'unread · all caught up'
                : `unread ${unreadCount === 1 ? 'notification' : 'notifications'}`}
            </p>
          </div>
        </DashboardCard>
      </div>
    </div>
  );
};
