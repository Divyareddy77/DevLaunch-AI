/**
 * JobQuickActions — a dropdown of quick actions for a job application card.
 *
 * Offers View, Edit, Move Status, Schedule Interview, Add Notes,
 * Upload Resume (attachments), and Delete — the same actions used by the
 * list card and the board card.
 *
 * @author DevLaunch
 */

import React, { useEffect, useRef, useState } from 'react';
import {
  MoreVertical,
  Eye,
  Edit3,
  ArrowLeftRight,
  CalendarClock,
  StickyNote,
  Paperclip,
  Trash2,
} from 'lucide-react';

interface JobQuickActionsProps {
  /** Opens the application detail view. */
  onView: () => void;
  /** Navigates to the edit page. */
  onEdit: () => void;
  /** Opens the move-status dialog. */
  onMoveStatus: () => void;
  /** Opens the schedule-interview dialog. */
  onSchedule: () => void;
  /** Opens the interview notes dialog. */
  onNotes: () => void;
  /** Opens the attachments dialog. */
  onAttachments: () => void;
  /** Triggers the delete confirmation. */
  onDelete: () => void;
  /** Aligns the dropdown to the right edge of the trigger. */
  align?: 'left' | 'right';
}

export const JobQuickActions: React.FC<JobQuickActionsProps> = ({
  onView,
  onEdit,
  onMoveStatus,
  onSchedule,
  onNotes,
  onAttachments,
  onDelete,
  align = 'right',
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  // Close on outside click and Escape
  useEffect(() => {
    if (!isOpen) return;
    const handlePointerDown = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setIsOpen(false);
    };
    document.addEventListener('mousedown', handlePointerDown);
    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('mousedown', handlePointerDown);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen]);

  const run = (action: () => void) => {
    setIsOpen(false);
    action();
  };

  const itemClass =
    'flex w-full items-center gap-2 px-3 py-2 text-left text-xs font-medium text-gray-700 transition-colors hover:bg-gray-50 hover:text-gray-900';

  return (
    <div ref={containerRef} className="relative flex-shrink-0">
      <button
        onClick={() => setIsOpen((open) => !open)}
        className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600"
        aria-label="Application quick actions"
        title="Quick actions"
        type="button"
      >
        <MoreVertical className="h-4 w-4" />
      </button>

      {isOpen && (
        <div
          className={`absolute top-full z-30 mt-1 w-48 overflow-hidden rounded-xl border border-gray-200 bg-white py-1 shadow-lg ${
            align === 'right' ? 'right-0' : 'left-0'
          }`}
          role="menu"
        >
          <button type="button" className={itemClass} onClick={() => run(onView)} role="menuitem">
            <Eye className="h-3.5 w-3.5 text-gray-400" />
            View Details
          </button>
          <button type="button" className={itemClass} onClick={() => run(onEdit)} role="menuitem">
            <Edit3 className="h-3.5 w-3.5 text-gray-400" />
            Edit
          </button>
          <button
            type="button"
            className={itemClass}
            onClick={() => run(onMoveStatus)}
            role="menuitem"
          >
            <ArrowLeftRight className="h-3.5 w-3.5 text-gray-400" />
            Move Status
          </button>
          <button
            type="button"
            className={itemClass}
            onClick={() => run(onSchedule)}
            role="menuitem"
          >
            <CalendarClock className="h-3.5 w-3.5 text-gray-400" />
            Schedule Interview
          </button>
          <button type="button" className={itemClass} onClick={() => run(onNotes)} role="menuitem">
            <StickyNote className="h-3.5 w-3.5 text-gray-400" />
            Add Notes
          </button>
          <button
            type="button"
            className={itemClass}
            onClick={() => run(onAttachments)}
            role="menuitem"
          >
            <Paperclip className="h-3.5 w-3.5 text-gray-400" />
            Upload Resume / Docs
          </button>
          <div className="my-1 border-t border-gray-100" />
          <button
            type="button"
            className="flex w-full items-center gap-2 px-3 py-2 text-left text-xs font-medium text-red-600 transition-colors hover:bg-red-50"
            onClick={() => run(onDelete)}
            role="menuitem"
          >
            <Trash2 className="h-3.5 w-3.5" />
            Delete
          </button>
        </div>
      )}
    </div>
  );
};
