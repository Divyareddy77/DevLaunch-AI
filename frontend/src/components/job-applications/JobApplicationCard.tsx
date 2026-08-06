/**
 * JobApplicationCard — displays a single job application in the list view.
 *
 * Shows the company logo, role, salary, location, status badge, applied
 * date, upcoming interview badge, priority badge, and notes/attachment
 * indicators, with view/edit/delete actions plus optional placement quick
 * actions (move status, schedule, notes, attachments). Clicking the card
 * body opens the detail view.
 *
 * @author DevLaunch
 */

import React from 'react';
import {
  Calendar,
  MapPin,
  Briefcase,
  DollarSign,
  ExternalLink,
  Paperclip,
  StickyNote,
  CalendarClock,
  Edit3,
  Trash2,
} from 'lucide-react';
import { Card } from '../ui/Card';
import { StatusBadge } from './StatusBadge';
import { PriorityBadge } from './PriorityBadge';
import { CompanyLogo } from './CompanyLogo';
import { JobQuickActions } from './JobQuickActions';
import { formatDate, formatTime } from '../../utils/date';
import { interviewCountdownLabel } from '../../utils/jobApplication';
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
  /** Opens the move-status dialog (optional, placement feature). */
  onMoveStatus?: (application: JobApplicationResponse) => void;
  /** Opens the schedule-interview dialog (optional, placement feature). */
  onSchedule?: (application: JobApplicationResponse) => void;
  /** Opens the notes dialog (optional, placement feature). */
  onNotes?: (application: JobApplicationResponse) => void;
  /** Opens the attachments dialog (optional, placement feature). */
  onAttachments?: (application: JobApplicationResponse) => void;
}

export const JobApplicationCard: React.FC<JobApplicationCardProps> = ({
  application,
  onView,
  onEdit,
  onDelete,
  onMoveStatus,
  onSchedule,
  onNotes,
  onAttachments,
}) => {
  const hasQuickActions = Boolean(onMoveStatus || onSchedule || onNotes || onAttachments);

  return (
    <Card className="group transition-all hover:shadow-md hover:border-primary-200" padded={false}>
      <div
        role="button"
        tabIndex={0}
        onClick={() => onView(application.id)}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            onView(application.id);
          }
        }}
        className="block w-full cursor-pointer text-left"
      >
        <div className="p-5">
          {/* Header row */}
          <div className="flex items-start justify-between gap-3">
            <div className="flex items-start gap-3 min-w-0">
              <CompanyLogo
                companyName={application.companyName}
                website={application.companyWebsite ?? application.jobUrl}
                size="md"
              />
              <div className="min-w-0">
                <h3 className="truncate text-sm font-semibold text-gray-900">
                  {application.companyName}
                </h3>
                <p className="truncate text-xs text-gray-500">{application.jobRole}</p>
              </div>
            </div>
            <div className="flex flex-shrink-0 flex-col items-end gap-1">
              <StatusBadge status={application.status} size="sm" />
              <PriorityBadge priority={application.priority} size="sm" />
            </div>
          </div>

          {/* Detail rows */}
          <div className="mt-3 space-y-1.5">
            {application.companyLocation && (
              <div className="flex items-center gap-1.5 text-xs text-gray-500">
                <MapPin className="h-3.5 w-3.5 flex-shrink-0 text-gray-400" />
                <span className="truncate">{application.companyLocation}</span>
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
                <span>Applied: {formatDate(application.applicationDate)}</span>
              </div>
            )}
          </div>

          {/* Upcoming interview badge */}
          {application.upcomingInterview && (
            <div className="mt-3 flex items-center gap-1.5 rounded-lg bg-blue-50 px-2.5 py-1.5 text-xs font-medium text-blue-700">
              <CalendarClock className="h-3.5 w-3.5 flex-shrink-0" />
              <span className="truncate">
                {application.upcomingInterview.title} ·{' '}
                {interviewCountdownLabel(application.upcomingInterview)}
                {application.upcomingInterview.scheduledTime
                  ? ` · ${formatTime(application.upcomingInterview.scheduledTime)}`
                  : ''}
              </span>
            </div>
          )}

          {/* Notes preview */}
          {application.notes && (
            <p className="mt-3 line-clamp-2 text-xs text-gray-500 italic">
              &ldquo;{application.notes}&rdquo;
            </p>
          )}

          {/* Job URL link */}
          {application.jobUrl && (
            <div className="mt-2">
              <a
                href={application.jobUrl}
                target="_blank"
                rel="noopener noreferrer"
                onClick={(e) => e.stopPropagation()}
                className="inline-flex items-center gap-1 text-xs font-medium text-indigo-600 hover:text-indigo-800 transition-colors"
              >
                <ExternalLink className="h-3 w-3" />
                View Job Posting
              </a>
            </div>
          )}

          {/* Attachment / note indicators */}
          {(application.attachmentCount > 0 || application.notesCount > 0) && (
            <div className="mt-3 flex items-center gap-3">
              {application.notesCount > 0 && (
                <span className="inline-flex items-center gap-1 text-[11px] font-medium text-gray-400">
                  <StickyNote className="h-3 w-3" />
                  {application.notesCount} note{application.notesCount === 1 ? '' : 's'}
                </span>
              )}
              {application.attachmentCount > 0 && (
                <span className="inline-flex items-center gap-1 text-[11px] font-medium text-gray-400">
                  <Paperclip className="h-3 w-3" />
                  {application.attachmentCount} file{application.attachmentCount === 1 ? '' : 's'}
                </span>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Actions footer */}
      <div className="flex items-center justify-between border-t border-gray-100 px-5 py-3">
        <button
          onClick={() => onView(application.id)}
          className="inline-flex items-center gap-1 text-xs font-medium text-indigo-600 hover:text-indigo-800 transition-colors"
        >
          <CalendarClock className="h-3.5 w-3.5" />
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
          {hasQuickActions && (
            <JobQuickActions
              onView={() => onView(application.id)}
              onEdit={() => onEdit(application.id)}
              onMoveStatus={() => onMoveStatus?.(application)}
              onSchedule={() => onSchedule?.(application)}
              onNotes={() => onNotes?.(application)}
              onAttachments={() => onAttachments?.(application)}
              onDelete={() => onDelete(application.id)}
            />
          )}
        </div>
      </div>
    </Card>
  );
};
