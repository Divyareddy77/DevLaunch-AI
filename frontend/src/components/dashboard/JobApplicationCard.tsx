/**
 * JobApplicationCard — dashboard widget summarising the user's job
 * application activity.
 *
 * Shows the next upcoming interview (with a countdown), the most recent
 * applications, and offer/interview/assessment counts, with a link to the
 * full tracker.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Briefcase, ExternalLink, Send, CalendarCheck, Award, FileSearch } from 'lucide-react';
import { DashboardCard } from './DashboardCard';
import { EmptyState } from '../shared/EmptyState';
import { formatDate, formatTime } from '../../utils/date';
import { interviewCountdownLabel } from '../../utils/jobApplication';
import type { RecentApplication, UpcomingInterview } from '../../types/dashboard';

interface JobApplicationCardProps {
  /** Total number of job applications submitted. */
  totalApplications: number | null;
  /** Number of applications that reached the interview stage. */
  interviewApplications: number | null;
  /** Number of applications that reached the offer stage. */
  offerApplications?: number | null;
  /** Number of applications in the assessment stage. */
  assessmentApplications?: number | null;
  /** The three most recently added applications. */
  recentApplications?: RecentApplication[];
  /** The next upcoming interview, or null. */
  upcomingInterview?: UpcomingInterview | null;
  /** Whether the card data is still loading. */
  loading?: boolean;
  /** Error message to display if data could not be loaded. */
  error?: string | null;
  /** Callback to navigate to the full job tracker page. */
  onViewAll?: () => void;
}

const statusColorMap: Record<RecentApplication['status'], string> = {
  WISHLIST: 'bg-gray-100 text-gray-600',
  APPLIED: 'bg-indigo-100 text-indigo-700',
  ASSESSMENT: 'bg-amber-100 text-amber-700',
  INTERVIEW: 'bg-blue-100 text-blue-700',
  OFFER: 'bg-emerald-100 text-emerald-700',
  REJECTED: 'bg-red-100 text-red-700',
};

const statusLabelMap: Record<RecentApplication['status'], string> = {
  WISHLIST: 'Wishlist',
  APPLIED: 'Applied',
  ASSESSMENT: 'Assessment',
  INTERVIEW: 'Interview',
  OFFER: 'Offer',
  REJECTED: 'Rejected',
};

export const JobApplicationCard: React.FC<JobApplicationCardProps> = ({
  totalApplications,
  interviewApplications,
  offerApplications,
  assessmentApplications,
  recentApplications = [],
  upcomingInterview,
  loading = false,
  error = null,
  onViewAll,
}) => {
  return (
    <DashboardCard
      title="Job Applications"
      icon={<Briefcase className="h-5 w-5" />}
      tone="orange"
      action={
        onViewAll && (
          <button
            onClick={onViewAll}
            className="flex items-center gap-1 text-xs font-medium text-indigo-600 hover:text-indigo-800 transition-colors"
          >
            View All
            <ExternalLink className="h-3 w-3" />
          </button>
        )
      }
      loading={loading}
    >
      {error ? (
        <p className="text-sm text-red-500">{error}</p>
      ) : (
        <div className="space-y-4">
          {/* Upcoming interview */}
          {upcomingInterview ? (
            <div className="rounded-xl border border-blue-100 bg-blue-50/60 p-3">
              <p className="text-[10px] font-semibold uppercase tracking-wide text-blue-500">
                Upcoming Interview
              </p>
              <p className="mt-1 text-sm font-bold text-gray-900">
                {upcomingInterview.companyName}
              </p>
              <p className="text-xs text-gray-500">{upcomingInterview.jobRole}</p>
              <div className="mt-2 flex items-center justify-between">
                <span className="inline-flex items-center gap-1 rounded-full bg-blue-100 px-2 py-0.5 text-[10px] font-semibold text-blue-700">
                  <CalendarCheck className="h-3 w-3" />
                  {interviewCountdownLabel(upcomingInterview)}
                </span>
                <span className="text-xs font-medium text-gray-600">
                  {upcomingInterview.scheduledTime
                    ? formatTime(upcomingInterview.scheduledTime)
                    : formatDate(upcomingInterview.scheduledDate)}
                </span>
              </div>
            </div>
          ) : (
            <div className="rounded-xl border border-dashed border-gray-200 bg-gray-50/50 p-3 text-center">
              <p className="text-xs font-medium text-gray-500">No upcoming interviews</p>
              <p className="mt-0.5 text-[11px] text-gray-400">
                Schedule rounds to see them here
              </p>
            </div>
          )}

          {/* Recent applications */}
          <div>
            <p className="mb-1.5 text-[10px] font-semibold uppercase tracking-wide text-gray-400">
              Recent Applications
            </p>
            {recentApplications.length === 0 ? (
              <EmptyState
                icon={Briefcase}
                tone="orange"
                compact
                title="No applications yet"
                description="Add your first application to start tracking your search."
                actionLabel="Add Application"
                onAction={onViewAll}
              />
            ) : (
              <ul className="space-y-1.5">
                {recentApplications.map((app) => (
                  <li key={app.id} className="flex items-center justify-between gap-2">
                    <div className="flex items-center gap-2 min-w-0">
                      <span
                        className={`flex h-6 w-6 flex-shrink-0 items-center justify-center rounded-md text-[10px] font-bold ${statusColorMap[app.status]}`}
                      >
                        {app.companyName.charAt(0).toUpperCase()}
                      </span>
                      <span className="truncate text-xs font-medium text-gray-700">
                        {app.companyName}
                      </span>
                    </div>
                    <span className="flex-shrink-0 text-[10px] text-gray-400">
                      {statusLabelMap[app.status]}
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </div>

          {/* Counts */}
          <div className="grid grid-cols-4 gap-2">
            <div className="rounded-lg bg-indigo-50 p-2 text-center">
              <span className="text-lg font-bold text-indigo-700">{totalApplications ?? 0}</span>
              <p className="mt-0.5 flex items-center justify-center gap-1 text-[10px] font-medium text-indigo-600">
                <Send className="h-3 w-3" /> Total
              </p>
            </div>
            <div className="rounded-lg bg-blue-50 p-2 text-center">
              <span className="text-lg font-bold text-blue-700">{interviewApplications ?? 0}</span>
              <p className="mt-0.5 flex items-center justify-center gap-1 text-[10px] font-medium text-blue-600">
                <CalendarCheck className="h-3 w-3" /> Interviews
              </p>
            </div>
            <div className="rounded-lg bg-amber-50 p-2 text-center">
              <span className="text-lg font-bold text-amber-700">
                {assessmentApplications ?? 0}
              </span>
              <p className="mt-0.5 flex items-center justify-center gap-1 text-[10px] font-medium text-amber-600">
                <FileSearch className="h-3 w-3" /> Assessments
              </p>
            </div>
            <div className="rounded-lg bg-emerald-50 p-2 text-center">
              <span className="text-lg font-bold text-emerald-700">{offerApplications ?? 0}</span>
              <p className="mt-0.5 flex items-center justify-center gap-1 text-[10px] font-medium text-emerald-600">
                <Award className="h-3 w-3" /> Offers
              </p>
            </div>
          </div>
        </div>
      )}
    </DashboardCard>
  );
};
