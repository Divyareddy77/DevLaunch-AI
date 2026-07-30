/**
 * JobApplicationCard — dashboard widget summarising the user's
 * job application activity.
 *
 * Displays the total number of applications and those that have
 * reached the interview stage, with a link to the full tracker.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Briefcase, ExternalLink, Send, CalendarCheck } from 'lucide-react';
import { DashboardCard } from './DashboardCard';

interface JobApplicationCardProps {
  /** Total number of job applications submitted. */
  totalApplications: number | null;
  /** Number of applications that reached the interview stage. */
  interviewApplications: number | null;
  /** Whether the card data is still loading. */
  loading?: boolean;
  /** Error message to display if data could not be loaded. */
  error?: string | null;
  /** Callback to navigate to the full job tracker page. */
  onViewAll?: () => void;
}

export const JobApplicationCard: React.FC<JobApplicationCardProps> = ({
  totalApplications,
  interviewApplications,
  loading = false,
  error = null,
  onViewAll,
}) => {
  return (
    <DashboardCard
      title="Job Applications"
      icon={<Briefcase className="h-5 w-5" />}
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
        <div className="grid grid-cols-2 gap-4">
          <div className="rounded-lg bg-indigo-50 p-3 text-center">
            <div className="flex items-center justify-center gap-1">
              <Send className="h-4 w-4 text-indigo-500" />
              <span className="text-2xl font-bold text-indigo-700">
                {totalApplications ?? 0}
              </span>
            </div>
            <p className="mt-0.5 text-xs font-medium text-indigo-600">Total Applied</p>
          </div>

          <div className="rounded-lg bg-emerald-50 p-3 text-center">
            <div className="flex items-center justify-center gap-1">
              <CalendarCheck className="h-4 w-4 text-emerald-500" />
              <span className="text-2xl font-bold text-emerald-700">
                {interviewApplications ?? 0}
              </span>
            </div>
            <p className="mt-0.5 text-xs font-medium text-emerald-600">Interviews</p>
          </div>
        </div>
      )}
    </DashboardCard>
  );
};
