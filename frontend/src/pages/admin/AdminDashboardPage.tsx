/**
 * AdminDashboardPage — the admin module overview.
 *
 * Displays platform-wide statistics as cards, a bar chart of content by
 * module, and the most recent registrations and interviews.
 *
 * @author DevLaunch
 */

import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Users,
  UserCheck,
  UserX,
  FileText,
  Briefcase,
  CalendarCheck,
  Bot,
  FileSearch,
  RefreshCw,
  UserPlus,
  MessageSquare,
} from 'lucide-react';
import { adminService } from '../../services/admin.service';
import { StatsCard } from '../../components/dashboard/StatsCard';
import { BarChart, type BarChartDataPoint } from '../../components/charts';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { formatNumber } from '../../utils/format';
import { formatRelativeTime } from '../../utils/date';
import { getErrorMessage } from '../../utils/error';
import { MESSAGES } from '../../constants/messages';
import type { AdminDashboardResponse } from '../../types/admin';

export const AdminDashboardPage: React.FC = () => {
  const [dashboard, setDashboard] = useState<AdminDashboardResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchDashboard = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await adminService.getDashboard();
      setDashboard(data);
    } catch (err: unknown) {
      setError(getErrorMessage(err, MESSAGES.LOAD_ERROR('admin dashboard')));
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchDashboard();
  }, [fetchDashboard]);

  const moduleChartData: BarChartDataPoint[] = useMemo(() => {
    if (!dashboard) return [];
    return [
      { name: 'Users', value: dashboard.totalUsers },
      { name: 'Resumes', value: dashboard.totalResumes },
      { name: 'Applications', value: dashboard.totalJobApplications },
      { name: 'Study Plans', value: dashboard.totalStudyPlans },
      { name: 'Interviews', value: dashboard.totalInterviewSessions },
      { name: 'AI Reviews', value: dashboard.totalResumeReviews },
    ];
  }, [dashboard]);

  if (isLoading) {
    return <LoadingScreen />;
  }

  if (error || !dashboard) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center">
        <ErrorMessage message={error ?? 'Dashboard unavailable'} onRetry={fetchDashboard} />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-6xl">
      {/* Header */}
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900 sm:text-2xl">Admin Dashboard</h1>
          <p className="mt-1 text-sm text-gray-500">
            Platform-wide overview across all DevLaunch modules.
          </p>
        </div>
        <Button variant="outline" size="sm" onClick={fetchDashboard}>
          <RefreshCw className="h-4 w-4" />
          Refresh
        </Button>
      </div>

      {/* Stat cards */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatsCard
          value={formatNumber(dashboard.totalUsers)}
          label="Total Users"
          icon={<Users className="h-6 w-6" />}
          color="indigo"
        />
        <StatsCard
          value={formatNumber(dashboard.activeUsers)}
          label="Active Users"
          icon={<UserCheck className="h-6 w-6" />}
          color="emerald"
        />
        <StatsCard
          value={formatNumber(dashboard.inactiveUsers)}
          label="Inactive Users"
          icon={<UserX className="h-6 w-6" />}
          color="rose"
        />
        <StatsCard
          value={formatNumber(dashboard.totalResumes)}
          label="Resumes"
          icon={<FileText className="h-6 w-6" />}
          color="blue"
        />
        <StatsCard
          value={formatNumber(dashboard.totalJobApplications)}
          label="Job Applications"
          icon={<Briefcase className="h-6 w-6" />}
          color="amber"
        />
        <StatsCard
          value={formatNumber(dashboard.totalStudyPlans)}
          label="Study Plans"
          icon={<CalendarCheck className="h-6 w-6" />}
          color="purple"
        />
        <StatsCard
          value={formatNumber(dashboard.totalInterviewSessions)}
          label="Mock Interviews"
          icon={<Bot className="h-6 w-6" />}
          color="indigo"
        />
        <StatsCard
          value={formatNumber(dashboard.totalResumeReviews)}
          label="AI Resume Reviews"
          icon={<FileSearch className="h-6 w-6" />}
          color="emerald"
        />
      </div>

      {/* Chart + recent activity */}
      <div className="mt-6 grid gap-6 lg:grid-cols-5">
        {/* Content by module */}
        <div className="rounded-xl border border-gray-200 bg-white p-5 shadow-sm lg:col-span-2">
          <BarChart
            data={moduleChartData}
            title="Content by Module"
            yAxisLabel="Count"
          />
        </div>

        {/* Recent registrations */}
        <div className="rounded-xl border border-gray-200 bg-white p-5 shadow-sm lg:col-span-3">
          <div className="mb-4 flex items-center justify-between">
            <h4 className="text-sm font-semibold text-gray-700">Recent Registrations</h4>
            <UserPlus className="h-4 w-4 text-gray-400" />
          </div>
          {dashboard.recentRegistrations.length === 0 ? (
            <p className="py-8 text-center text-sm text-gray-400">No registrations yet.</p>
          ) : (
            <ul className="divide-y divide-gray-100">
              {dashboard.recentRegistrations.map((user) => (
                <li key={user.id} className="flex items-center justify-between py-2.5">
                  <div className="flex items-center gap-3">
                    <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary-100 text-xs font-semibold text-primary-600">
                      {user.firstName.charAt(0)}
                      {user.lastName.charAt(0)}
                    </div>
                    <div>
                      <p className="text-sm font-medium text-gray-900">
                        {user.firstName} {user.lastName}
                      </p>
                      <p className="text-xs text-gray-400">{user.email}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <Badge variant={user.role === 'ADMIN' ? 'primary' : 'default'} size="sm">
                      {user.role}
                    </Badge>
                    <span className="text-xs text-gray-400">
                      {formatRelativeTime(user.createdAt)}
                    </span>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      {/* Recent interviews */}
      <div className="mt-6 rounded-xl border border-gray-200 bg-white p-5 shadow-sm">
        <div className="mb-4 flex items-center justify-between">
          <h4 className="text-sm font-semibold text-gray-700">Recent Mock Interviews</h4>
          <MessageSquare className="h-4 w-4 text-gray-400" />
        </div>
        {dashboard.recentInterviews.length === 0 ? (
          <p className="py-8 text-center text-sm text-gray-400">No interviews completed yet.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-gray-100 text-xs uppercase tracking-wide text-gray-400">
                  <th className="py-2 pr-4 font-medium">User</th>
                  <th className="py-2 pr-4 font-medium">Category</th>
                  <th className="py-2 pr-4 font-medium">Score</th>
                  <th className="py-2 pr-4 font-medium">Questions</th>
                  <th className="py-2 font-medium">Completed</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {dashboard.recentInterviews.map((session) => (
                  <tr key={session.id}>
                    <td className="py-2.5 pr-4 text-gray-700">
                      <span className="font-medium text-gray-900">{session.userName}</span>
                      <span className="block text-xs text-gray-400">{session.userEmail}</span>
                    </td>
                    <td className="py-2.5 pr-4">
                      <Badge variant="info" size="sm">
                        {session.interviewType}
                      </Badge>
                    </td>
                    <td className="py-2.5 pr-4 font-semibold text-gray-900">
                      {session.overallScore}/100
                    </td>
                    <td className="py-2.5 pr-4 text-gray-600">{session.questionCount}</td>
                    <td className="py-2.5 text-xs text-gray-400">
                      {formatRelativeTime(session.completedAt)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
