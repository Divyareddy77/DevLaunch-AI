/**
 * JobBoardView — Kanban board for the job tracker.
 *
 * Renders one column per pipeline status (Wishlist → Rejected). Cards are
 * draggable between columns using the native HTML5 drag-and-drop API (no
 * extra dependency) and dropping a card on a column updates the
 * application's status automatically.
 *
 * Desktop shows all six columns; tablet and mobile scroll horizontally so
 * the board stays usable on any screen.
 *
 * @author DevLaunch
 */

import React, { useMemo, useState } from 'react';
import {
  APPLICATION_STATUSES,
  APPLICATION_STATUS_LABELS,
  type ApplicationStatusEnum,
  type JobApplicationResponse,
} from '../../types/job-application';
import { JobBoardCard } from './JobBoardCard';

interface JobBoardViewProps {
  /** The (already filtered) applications to display. */
  applications: JobApplicationResponse[];
  /** Called when a card is dropped on a different column. */
  onStatusChange: (applicationId: number, status: ApplicationStatusEnum) => void;
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

/** Column accent styles per status. */
const columnAccent: Record<ApplicationStatusEnum, string> = {
  WISHLIST: 'border-t-gray-300',
  APPLIED: 'border-t-indigo-400',
  ASSESSMENT: 'border-t-amber-400',
  INTERVIEW: 'border-t-blue-400',
  OFFER: 'border-t-emerald-400',
  REJECTED: 'border-t-red-400',
};

const columnDot: Record<ApplicationStatusEnum, string> = {
  WISHLIST: 'bg-gray-300',
  APPLIED: 'bg-indigo-400',
  ASSESSMENT: 'bg-amber-400',
  INTERVIEW: 'bg-blue-400',
  OFFER: 'bg-emerald-400',
  REJECTED: 'bg-red-400',
};

export const JobBoardView: React.FC<JobBoardViewProps> = ({
  applications,
  onStatusChange,
  onView,
  onEdit,
  onMoveStatus,
  onSchedule,
  onNotes,
  onAttachments,
  onDelete,
}) => {
  // Track the application currently being dragged and the hovered column.
  const [draggingId, setDraggingId] = useState<number | null>(null);
  const [overColumn, setOverColumn] = useState<ApplicationStatusEnum | null>(null);

  const byStatus = useMemo(() => {
    const grouped: Record<ApplicationStatusEnum, JobApplicationResponse[]> = {
      WISHLIST: [],
      APPLIED: [],
      ASSESSMENT: [],
      INTERVIEW: [],
      OFFER: [],
      REJECTED: [],
    };
    for (const app of applications) {
      grouped[app.status].push(app);
    }
    return grouped;
  }, [applications]);

  const handleDrop = (targetStatus: ApplicationStatusEnum) => {
    if (draggingId === null) return;
    const source = applications.find((app) => app.id === draggingId);
    if (source && source.status !== targetStatus) {
      onStatusChange(draggingId, targetStatus);
    }
    setDraggingId(null);
    setOverColumn(null);
  };

  return (
    <div className="-mx-4 overflow-x-auto px-4 pb-4 sm:-mx-6 sm:px-6">
      <div className="flex min-w-max gap-4">
        {APPLICATION_STATUSES.map((status) => {
          const apps = byStatus[status];
          const isOver = overColumn === status;
          return (
            <section
              key={status}
              onDragOver={(e) => {
                e.preventDefault();
                e.dataTransfer.dropEffect = 'move';
                setOverColumn(status);
              }}
              onDragLeave={() => setOverColumn((current) => (current === status ? null : current))}
              onDrop={(e) => {
                e.preventDefault();
                handleDrop(status);
              }}
              className={`flex w-72 flex-shrink-0 flex-col rounded-xl border border-t-2 bg-gray-50/80 transition-colors ${
                columnAccent[status]
              } ${isOver ? 'border-indigo-300 bg-indigo-50/60 ring-2 ring-indigo-200' : 'border-gray-200'}`}
            >
              {/* Column header */}
              <div className="flex items-center justify-between px-3.5 py-3">
                <div className="flex items-center gap-2">
                  <span className={`h-2 w-2 rounded-full ${columnDot[status]}`} />
                  <h3 className="text-xs font-semibold uppercase tracking-wide text-gray-700">
                    {APPLICATION_STATUS_LABELS[status]}
                  </h3>
                </div>
                <span className="rounded-full bg-white px-2 py-0.5 text-[11px] font-semibold text-gray-500 ring-1 ring-gray-200">
                  {apps.length}
                </span>
              </div>

              {/* Column body */}
              <div
                className={`flex-1 space-y-2.5 overflow-y-auto px-3 pb-3 ${apps.length === 0 ? 'min-h-[120px]' : ''}`}
              >
                {apps.length === 0 && (
                  <div
                    className={`flex h-full min-h-[100px] items-center justify-center rounded-lg border border-dashed text-xs text-gray-300 transition-colors ${
                      isOver ? 'border-indigo-300 bg-indigo-100/40 text-indigo-400' : 'border-gray-200'
                    }`}
                  >
                    {isOver ? 'Drop here' : 'No applications'}
                  </div>
                )}
                {apps.map((app) => (
                  <JobBoardCard
                    key={app.id}
                    application={app}
                    onDragStart={(id) => setDraggingId(id)}
                    onDragEnd={() => {
                      setDraggingId(null);
                      setOverColumn(null);
                    }}
                    onView={onView}
                    onEdit={onEdit}
                    onMoveStatus={onMoveStatus}
                    onSchedule={onSchedule}
                    onNotes={onNotes}
                    onAttachments={onAttachments}
                    onDelete={onDelete}
                  />
                ))}
              </div>
            </section>
          );
        })}
      </div>
    </div>
  );
};
