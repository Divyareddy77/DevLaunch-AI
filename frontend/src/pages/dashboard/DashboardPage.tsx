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
  Code2,
  Link2,
  Award,
  Target,
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { dashboardService } from '../../services/dashboard.service';
import { DashboardCard } from '../../components/dashboard/DashboardCard';
import { JobApplicationCard } from '../../components/dashboard/JobApplicationCard';
import { StudyPlannerCard } from '../../components/dashboard/StudyPlannerCard';
import { GitHubCard } from '../../components/dashboard/GitHubCard';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { ROUTES } from '../../constants/routes';
import type { DashboardResponse } from '../../types/dashboard';

/** Mapping from score to display colour for the placement readiness ring. */
function scoreColor(score: number): string {
  if (score >= 80) return '#10b981'; // emerald
  if (score >= 60) return '#6366f1'; // indigo
  if (score >= 40) return '#f59e0b'; // amber
  return '#ef4444'; // red
}

/** Background tint for the score ring. */
function scoreBgColor(score: number): string {
  if (score >= 80) return 'text-emerald-600';
  if (score >= 60) return 'text-indigo-600';
  if (score >= 40) return 'text-amber-600';
  return 'text-red-600';
}

/**
 * SVG circular progress ring component.
 * Renders a donut ring with the score value in the centre.
 */
const ProgressRing: React.FC<{ score: number; size?: number; strokeWidth?: number }> = ({
  score,
  size = 120,
  strokeWidth = 10,
}) => {
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference - (score / 100) * circumference;
  const color = scoreColor(score);

  return (
    <div className="relative inline-flex items-center justify-center">
      <svg width={size} height={size} className="-rotate-90">
        {/* Background circle */}
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke="#e5e7eb"
          strokeWidth={strokeWidth}
        />
        {/* Progress arc */}
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
          className="transition-all duration-700 ease-out"
        />
      </svg>
      <span className={`absolute text-3xl font-bold ${scoreBgColor(score)}`}>
        {score}
      </span>
    </div>
  );
};

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [data, setData] = useState<DashboardResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

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

      {/* ---- Hero: Placement Readiness ---- */}
      <div className="mb-8 overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm">
        <div className="flex flex-col items-center gap-6 px-6 py-8 sm:flex-row sm:justify-between sm:px-10">
          <div className="text-center sm:text-left">
            <h2 className="text-lg font-semibold text-gray-900">Placement Readiness</h2>
            <p className="mt-1 text-sm text-gray-500">
              Your overall score is calculated from resume completeness, job
              applications, study progress, GitHub presence, and LeetCode activity.
            </p>
          </div>
          <div className="flex flex-col items-center gap-1">
            <ProgressRing score={data.placementReadiness} size={120} strokeWidth={10} />
            <p className="mt-1 text-xs font-medium text-gray-400">
              out of 100
            </p>
          </div>
        </div>
      </div>

      {/* ---- Metrics grid ---- */}
      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
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
          onViewAll={goToJobs}
        />

        {/* Study Planner */}
        <StudyPlannerCard
          totalTasks={data.totalStudyTasks}
          completedTasks={data.completedStudyTasks}
          onViewAll={goToStudy}
        />

        {/* GitHub Analytics */}
        <GitHubCard
          repositoryCount={data.githubRepositories}
          topLanguage={data.githubTopLanguage}
          followers={null}
          following={null}
          onViewAll={goToGitHub}
        />

        {/* LeetCode Progress — show "Coming Soon" instead of 0 */}
        <DashboardCard
          title="LeetCode Progress"
          icon={<Code2 className="h-5 w-5" />}
          action={
            <button
              onClick={goToLeetCode}
              className="text-xs font-medium text-indigo-600 hover:text-indigo-800 transition-colors"
            >
              View Tracker
            </button>
          }
        >
          {data.leetcodeSolved > 0 ? (
            <div className="flex flex-col items-center">
              <span className="text-4xl font-bold text-gray-900">
                {data.leetcodeSolved}
              </span>
              <p className="mt-1 text-sm text-gray-500">problems solved</p>
            </div>
          ) : (
            <div className="flex flex-col items-center py-4">
              <Link2 className="mb-2 h-8 w-8 text-gray-300" />
              <p className="text-sm font-medium text-gray-500">No LeetCode account linked</p>
              <p className="mt-0.5 text-xs text-gray-400">
                Connect your LeetCode username to track progress
              </p>
            </div>
          )}
        </DashboardCard>

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
      </div>
    </div>
  );
};
