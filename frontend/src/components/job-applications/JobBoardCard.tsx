/**
 * JobBoardCard — compact draggable card for the Kanban board view.
 *
 * Shows the company logo, role, key details, priority, upcoming interview
 * badge, and notes/attachment indicators, with quick actions in a
 * dropdown. The whole card is draggable so it can be moved between board
 * columns.
 *
 * @author DevLaunch
 */

import React from 'react';
import { MapPin, DollarSign, Paperclip, StickyNote, CalendarClock, GripVertical } from 'lucide-react';
import { CompanyLogo } from './CompanyLogo';
import { PriorityBadge } from './PriorityBadge';
import { JobQuickActions } from './JobQuickActions';
import { formatDate, formatTime } from '../../utils/date';
import { interviewCountdownLabel } from '../../utils/jobApplication';
import type { JobApplicationResponse } from '../../types/job-application';

interface JobBoardCardProps {
  /** The application to display. */
  application: JobApplicationResponse;
  /** Called when a drag starts with the application ID. */
  onDragStart: (applicationId: number) => void;
  /** Called when a drag ends. */
  onDragEnd: () => void;
  /** Opens the detail view. */
  onView: (id: number) => void;
  /** Navigates to the edit page. */
  onEdit: (id: number) => void;
  /** Opens the move-status dialog. */
  onMoveStatus: (application: JobApplicationResponse) => void;
  /** Opens the schedule-interview dialog. */
  onSchedule: (application: JobApplicationResponse) => void;
  /** Opens the notes dialog. */
  onNotes: (application: JobApplicationResponse) => void;
  /** Opens the attachments dialog. */
  onAttachments: (application: JobApplicationResponse) => void;
  /** Triggers the delete confirmation. */
  onDelete: (id: number) => void;
}

export const JobBoardCard: React.FC<JobBoardCardProps> = ({
  application,
  onDragStart,
  onDragEnd,
  onView,
  onEdit,
  onMoveStatus,
  onSchedule,
  onNotes,
  onAttachments,
  onDelete,
}) => {
  // A real drag fires the onDrag handler repeatedly; a plain click never
  // does. This flag lets us swallow the stray click some browsers emit
  // after a drop so the detail modal does not pop open unexpectedly.
  const draggedRef = React.useRef(false);

  return (
    <div
      draggable
      onDragStart={(e) => {
        draggedRef.current = false;
        e.dataTransfer.effectAllowed = 'move';
        e.dataTransfer.setData('text/plain', String(application.id));
        onDragStart(application.id);
      }}
      onDrag={() => {
        draggedRef.current = true;
      }}
      onDragEnd={onDragEnd}
      onClick={() => {
        if (draggedRef.current) {
          draggedRef.current = false;
          return;
        }
        onView(application.id);
      }}
      className="group cursor-grab rounded-xl border border-gray-200 bg-white p-3.5 shadow-sm transition-all hover:-translate-y-0.5 hover:border-indigo-200 hover:shadow-md active:cursor-grabbing"
    >
      {/* Header */}
      <div className="flex items-start justify-between gap-2">
        <div className="flex items-center gap-2 min-w-0">
          <CompanyLogo
            companyName={application.companyName}
            website={application.companyWebsite ?? application.jobUrl}
            size="sm"
          />
          <div className="min-w-0">
            <p className="truncate text-sm font-semibold text-gray-900">
              {application.companyName}
            </p>
            <p className="truncate text-xs text-gray-500">{application.jobRole}</p>
          </div>
        </div>
        <div className="flex items-center gap-0.5" onClick={(e) => e.stopPropagation()}>
          <GripVertical className="h-4 w-4 text-gray-200 group-hover:text-gray-300" />
          <JobQuickActions
            onView={() => onView(application.id)}
            onEdit={() => onEdit(application.id)}
            onMoveStatus={() => onMoveStatus(application)}
            onSchedule={() => onSchedule(application)}
            onNotes={() => onNotes(application)}
            onAttachments={() => onAttachments(application)}
            onDelete={() => onDelete(application.id)}
          />
        </div>
      </div>

      {/* Details */}
      <div className="mt-2.5 space-y-1">
        {application.salary && (
          <p className="flex items-center gap-1.5 text-xs text-gray-600">
            <DollarSign className="h-3 w-3 flex-shrink-0 text-gray-400" />
            <span className="truncate">{application.salary}</span>
          </p>
        )}
        {application.companyLocation && (
          <p className="flex items-center gap-1.5 text-xs text-gray-500">
            <MapPin className="h-3 w-3 flex-shrink-0 text-gray-400" />
            <span className="truncate">{application.companyLocation}</span>
          </p>
        )}
        <p className="flex items-center gap-1.5 text-[11px] text-gray-400">
          Applied {formatDate(application.applicationDate)}
        </p>
      </div>

      {/* Badges */}
      <div className="mt-2.5 flex flex-wrap items-center gap-1.5">
        <PriorityBadge priority={application.priority} size="sm" />
        {application.upcomingInterview && (
          <span className="inline-flex items-center gap-1 rounded-full bg-blue-100 px-2 py-0.5 text-[10px] font-medium text-blue-700">
            <CalendarClock className="h-3 w-3" />
            {interviewCountdownLabel(application.upcomingInterview)}
            {application.upcomingInterview.scheduledTime
              ? ` · ${formatTime(application.upcomingInterview.scheduledTime)}`
              : ''}
          </span>
        )}
        <span className="ml-auto flex items-center gap-2 text-gray-300">
          {application.notesCount > 0 && (
            <span
              className="inline-flex items-center gap-0.5 text-[10px] font-medium text-gray-400"
              title={`${application.notesCount} note${application.notesCount === 1 ? '' : 's'}`}
            >
              <StickyNote className="h-3 w-3" />
              {application.notesCount}
            </span>
          )}
          {application.attachmentCount > 0 && (
            <span
              className="inline-flex items-center gap-0.5 text-[10px] font-medium text-gray-400"
              title={`${application.attachmentCount} attachment${application.attachmentCount === 1 ? '' : 's'}`}
            >
              <Paperclip className="h-3 w-3" />
              {application.attachmentCount}
            </span>
          )}
        </span>
      </div>
    </div>
  );
};
