/**
 * TimelinePanel — vertical milestone timeline for a job application.
 *
 * Renders the application's history (added, status changes, interview
 * scheduling/cancelling, attachments) with a per-type icon, the event
 * title, the date and time it occurred, and optional notes.
 *
 * @author DevLaunch
 */

import React from 'react';
import {
  Check,
  Send,
  FileSearch,
  CalendarCheck,
  Award,
  XCircle,
  CalendarClock,
  CalendarX,
  Paperclip,
  RefreshCw,
} from 'lucide-react';
import { formatDate, formatTime } from '../../utils/date';
import type { TimelineEvent, TimelineEventType } from '../../types/job-application';

interface TimelinePanelProps {
  /** The ordered timeline events (oldest first). */
  events: TimelineEvent[];
  /** Whether the timeline is still loading. */
  loading?: boolean;
}

const eventIconMap: Record<TimelineEventType, React.ReactNode> = {
  ADDED: <Check className="h-3.5 w-3.5" />,
  APPLIED: <Send className="h-3.5 w-3.5" />,
  ASSESSMENT: <FileSearch className="h-3.5 w-3.5" />,
  INTERVIEW: <CalendarCheck className="h-3.5 w-3.5" />,
  OFFER: <Award className="h-3.5 w-3.5" />,
  REJECTED: <XCircle className="h-3.5 w-3.5" />,
  STATUS_UPDATED: <RefreshCw className="h-3.5 w-3.5" />,
  INTERVIEW_SCHEDULED: <CalendarClock className="h-3.5 w-3.5" />,
  INTERVIEW_CANCELLED: <CalendarX className="h-3.5 w-3.5" />,
  ATTACHMENT_ADDED: <Paperclip className="h-3.5 w-3.5" />,
};

const eventColorMap: Record<TimelineEventType, string> = {
  ADDED: 'bg-gray-100 text-gray-600',
  APPLIED: 'bg-indigo-100 text-indigo-600',
  ASSESSMENT: 'bg-amber-100 text-amber-600',
  INTERVIEW: 'bg-blue-100 text-blue-600',
  OFFER: 'bg-emerald-100 text-emerald-600',
  REJECTED: 'bg-red-100 text-red-600',
  STATUS_UPDATED: 'bg-gray-100 text-gray-600',
  INTERVIEW_SCHEDULED: 'bg-blue-100 text-blue-600',
  INTERVIEW_CANCELLED: 'bg-red-100 text-red-600',
  ATTACHMENT_ADDED: 'bg-violet-100 text-violet-600',
};

export const TimelinePanel: React.FC<TimelinePanelProps> = ({ events, loading = false }) => {
  if (loading) {
    return (
      <div className="space-y-3 animate-pulse">
        {[0, 1, 2].map((i) => (
          <div key={i} className="flex gap-3">
            <div className="h-8 w-8 rounded-full bg-gray-200" />
            <div className="flex-1 space-y-1.5">
              <div className="h-3 w-1/3 rounded bg-gray-200" />
              <div className="h-3 w-1/2 rounded bg-gray-200" />
            </div>
          </div>
        ))}
      </div>
    );
  }

  if (events.length === 0) {
    return (
      <p className="py-6 text-center text-sm text-gray-400">
        No timeline events yet — they appear automatically as your application progresses.
      </p>
    );
  }

  return (
    <ol className="relative ml-1 space-y-5 border-l border-gray-200 pl-5">
      {events.map((event) => (
        <li key={event.id} className="relative">
          <span
            className={`absolute -left-[27px] flex h-6 w-6 items-center justify-center rounded-full ring-4 ring-white ${eventColorMap[event.eventType]}`}
          >
            {eventIconMap[event.eventType]}
          </span>
          <div className="flex flex-col gap-0.5">
            <div className="flex items-baseline justify-between gap-2">
              <p className="text-sm font-semibold text-gray-900">{event.title}</p>
              <p className="flex-shrink-0 text-xs text-gray-400">
                {formatDate(event.occurredAt)}
                <span className="ml-1 text-gray-300">
                  {formatTime(extractTime(event.occurredAt))}
                </span>
              </p>
            </div>
            {event.notes && <p className="text-xs text-gray-500">{event.notes}</p>}
          </div>
        </li>
      ))}
    </ol>
  );
};

/** Extracts the HH:mm portion of an ISO timestamp for the time label. */
function extractTime(iso: string): string | null {
  const match = iso.match(/T(\d{2}:\d{2})/);
  return match ? match[1] : null;
}
