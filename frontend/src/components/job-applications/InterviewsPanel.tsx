/**
 * InterviewsPanel — lists the scheduled interviews of an application.
 *
 * Highlights the next upcoming interview, shows meeting links and
 * interviewers, and offers edit / cancel actions for each entry.
 *
 * @author DevLaunch
 */

import React from 'react';
import {
  CalendarClock,
  Video,
  User,
  StickyNote,
  XCircle,
  Pencil,
  ExternalLink,
  Inbox,
} from 'lucide-react';
import { formatDate, formatTime } from '../../utils/date';
import { interviewCountdownLabel, todayKey } from '../../utils/jobApplication';
import type { InterviewSchedule } from '../../types/job-application';

interface InterviewsPanelProps {
  /** The date-ordered interviews of the application. */
  interviews: InterviewSchedule[];
  /** Whether the interviews are still loading. */
  loading?: boolean;
  /** Opens the schedule dialog for a new interview. */
  onSchedule: () => void;
  /** Opens the schedule dialog pre-filled with an existing interview. */
  onEdit: (interview: InterviewSchedule) => void;
  /** Cancels an interview. */
  onCancel: (interview: InterviewSchedule) => void;
}

export const InterviewsPanel: React.FC<InterviewsPanelProps> = ({
  interviews,
  loading = false,
  onSchedule,
  onEdit,
  onCancel,
}) => {
  if (loading) {
    return (
      <div className="space-y-3 animate-pulse">
        {[0, 1].map((i) => (
          <div key={i} className="h-20 rounded-lg bg-gray-200" />
        ))}
      </div>
    );
  }

  if (interviews.length === 0) {
    return (
      <div className="flex flex-col items-center py-8 text-center">
        <Inbox className="mb-2 h-8 w-8 text-gray-300" />
        <p className="text-sm font-medium text-gray-500">No interviews scheduled</p>
        <p className="mt-0.5 text-xs text-gray-400">
          Schedule a phone, technical, or HR round to track it here.
        </p>
        <button
          onClick={onSchedule}
          className="mt-3 inline-flex items-center gap-1.5 rounded-lg bg-indigo-600 px-3 py-1.5 text-xs font-medium text-white transition-colors hover:bg-indigo-700"
        >
          <CalendarClock className="h-3.5 w-3.5" />
          Schedule Interview
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {interviews.map((interview) => {
        const isUpcoming = !interview.cancelled && interview.scheduledDate >= todayKey();
        return (
          <div
            key={interview.id}
            className={`rounded-xl border p-4 transition-colors ${
              interview.cancelled
                ? 'border-gray-100 bg-gray-50 opacity-60'
                : isUpcoming
                  ? 'border-blue-100 bg-blue-50/40'
                  : 'border-gray-200 bg-white'
            }`}
          >
            <div className="flex items-start justify-between gap-3">
              <div className="flex items-center gap-2.5">
                <span
                  className={`flex h-9 w-9 items-center justify-center rounded-lg ${
                    interview.cancelled
                      ? 'bg-gray-100 text-gray-500'
                      : 'bg-blue-100 text-blue-600'
                  }`}
                >
                  <CalendarClock className="h-4 w-4" />
                </span>
                <div>
                  <div className="flex flex-wrap items-center gap-2">
                    <p className="text-sm font-semibold text-gray-900">{interview.title}</p>
                    {interview.round && (
                      <span className="rounded-full bg-gray-100 px-2 py-0.5 text-[10px] font-medium text-gray-500">
                        {interview.round}
                      </span>
                    )}
                    {isUpcoming && (
                      <span className="rounded-full bg-blue-100 px-2 py-0.5 text-[10px] font-medium text-blue-700">
                        {interviewCountdownLabel(interview)}
                      </span>
                    )}
                    {interview.cancelled && (
                      <span className="rounded-full bg-red-100 px-2 py-0.5 text-[10px] font-medium text-red-700">
                        Cancelled
                      </span>
                    )}
                  </div>
                  <p className="mt-0.5 text-xs text-gray-500">
                    {formatDate(interview.scheduledDate)}
                    {interview.scheduledTime ? ` at ${formatTime(interview.scheduledTime)}` : ''}
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-1">
                <button
                  onClick={() => onEdit(interview)}
                  className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600"
                  aria-label="Edit interview"
                  title="Edit interview"
                >
                  <Pencil className="h-3.5 w-3.5" />
                </button>
                {!interview.cancelled && (
                  <button
                    onClick={() => onCancel(interview)}
                    className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-red-50 hover:text-red-500"
                    aria-label="Cancel interview"
                    title="Cancel interview"
                  >
                    <XCircle className="h-3.5 w-3.5" />
                  </button>
                )}
              </div>
            </div>

            <div className="mt-2.5 space-y-1.5 pl-11">
              {interview.interviewer && (
                <p className="flex items-center gap-1.5 text-xs text-gray-600">
                  <User className="h-3 w-3 text-gray-400" />
                  {interview.interviewer}
                </p>
              )}
              {interview.meetingLink && (
                <a
                  href={interview.meetingLink}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-1.5 text-xs font-medium text-blue-600 hover:text-blue-800"
                >
                  <Video className="h-3 w-3" />
                  {interview.meetingLink}
                  <ExternalLink className="h-2.5 w-2.5" />
                </a>
              )}
              {interview.notes && (
                <p className="flex items-start gap-1.5 text-xs text-gray-500">
                  <StickyNote className="mt-0.5 h-3 w-3 flex-shrink-0 text-gray-400" />
                  <span className="italic">{interview.notes}</span>
                </p>
              )}
            </div>
          </div>
        );
      })}

      <button
        onClick={onSchedule}
        className="inline-flex items-center gap-1.5 rounded-lg border border-dashed border-gray-300 px-3 py-2 text-xs font-medium text-gray-500 transition-colors hover:border-indigo-300 hover:bg-indigo-50 hover:text-indigo-600"
      >
        <CalendarClock className="h-3.5 w-3.5" />
        Schedule another interview
      </button>
    </div>
  );
};
