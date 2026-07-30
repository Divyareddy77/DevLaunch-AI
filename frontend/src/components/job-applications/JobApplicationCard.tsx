/**
 * JobApplicationCard — displays a single job application in the list.
 *
 * Shows company, role, status badge, dates, and notes preview with
 * view/edit/delete actions.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Building2, Eye, Edit3, Trash2, Calendar, MapPin, Briefcase, DollarSign, ExternalLink } from 'lucide-react';
import { Card } from '../ui/Card';
import { StatusBadge } from './StatusBadge';
import type { JobApplicationResponse } from '../../types/job-application';

interface JobApplicationCardProps {
  /** The job application data to display. */
  application: JobApplicationResponse;
  /** Callback when the view button is clicked. */
  onView: (id: number) => void;
  /** Callback when the edit button is clicked. */
  onEdit: (id: number) => void;
  /** Callback when the delete button is clicked. */
  onDelete: (id: number) => void;
}

export const JobApplicationCard: React.FC<JobApplicationCardProps> = ({
  application,
  onView,
  onEdit,
  onDelete,
}) => {
  return (
    <Card className="group transition-all hover:shadow-md hover:border-primary-200" padded={false}>
      <div className="p-5">
        {/* Header row */}
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-start gap-3 min-w-0">
            <div className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-lg bg-indigo-100 text-indigo-600">
              <Building2 className="h-5 w-5" />
            </div>
            <div className="min-w-0">
              <h3 className="truncate text-sm font-semibold text-gray-900">
                {application.companyName}
              </h3>
              <p className="truncate text-xs text-gray-500">{application.jobRole}</p>
            </div>
          </div>
          <StatusBadge status={application.status} size="sm" />
        </div>

        {/* Detail rows */}
        <div className="mt-3 space-y-1.5">
          {application.companyLocation && (
            <div className="flex items-center gap-1.5 text-xs text-gray-500">
              <MapPin className="h-3.5 w-3.5 flex-shrink-0 text-gray-400" />
              <span>{application.companyLocation}</span>
            </div>
          )}
          {application.jobType && (
            <div className="flex items-center gap-1.5 text-xs text-gray-500">
              <Briefcase className="h-3.5 w-3.5 flex-shrink-0 text-gray-400" />
              <span>{application.jobType}</span>
            </div>
          )}
          {application.salary && (
            <div className="flex items-center gap-1.5 text-xs text-gray-500">
              <DollarSign className="h-3.5 w-3.5 flex-shrink-0 text-gray-400" />
              <span>{application.salary}</span>
            </div>
          )}
          {application.applicationDate && (
            <div className="flex items-center gap-1.5 text-xs text-gray-500">
              <Calendar className="h-3.5 w-3.5 flex-shrink-0 text-gray-400" />
              <span>Applied: {application.applicationDate}</span>
            </div>
          )}
        </div>

        {/* Notes preview */}
        {application.notes && (
          <p className="mt-3 line-clamp-2 text-xs text-gray-500 italic">
            "{application.notes}"
          </p>
        )}

        {/* Job URL link */}
        {application.jobUrl && (
          <div className="mt-2">
            <a
              href={application.jobUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1 text-xs font-medium text-indigo-600 hover:text-indigo-800 transition-colors"
            >
              <ExternalLink className="h-3 w-3" />
              View Job Posting
            </a>
          </div>
        )}
      </div>

      {/* Actions footer */}
      <div className="flex items-center justify-between border-t border-gray-100 px-5 py-3">
        <button
          onClick={() => onView(application.id)}
          className="inline-flex items-center gap-1 text-xs font-medium text-indigo-600 hover:text-indigo-800 transition-colors"
        >
          <Eye className="h-3.5 w-3.5" />
          Details
        </button>
        <div className="flex items-center gap-1">
          <button
            onClick={() => onEdit(application.id)}
            className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors"
            aria-label="Edit application"
            title="Edit"
          >
            <Edit3 className="h-4 w-4" />
          </button>
          <button
            onClick={() => onDelete(application.id)}
            className="rounded-lg p-1.5 text-gray-400 hover:bg-red-50 hover:text-red-500 transition-colors"
            aria-label="Delete application"
            title="Delete"
          >
            <Trash2 className="h-4 w-4" />
          </button>
        </div>
      </div>
    </Card>
  );
};
