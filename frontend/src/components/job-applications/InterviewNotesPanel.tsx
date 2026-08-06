/**
 * InterviewNotesPanel — private multi-entry notes for an application.
 *
 * Lists every note (newest first) and lets the user add a new entry or
 * remove an existing one. Notes persist per application.
 *
 * @author DevLaunch
 */

import React, { useState } from 'react';
import { StickyNote, Trash2, Plus, Inbox } from 'lucide-react';
import { formatRelativeTime } from '../../utils/date';
import type { InterviewNote } from '../../types/job-application';

interface InterviewNotesPanelProps {
  /** The notes of the application, newest first. */
  notes: InterviewNote[];
  /** Whether the notes are still loading. */
  loading?: boolean;
  /** Adds a new note. */
  onAdd: (content: string) => Promise<void>;
  /** Deletes a note. */
  onDelete: (note: InterviewNote) => Promise<void>;
}

export const InterviewNotesPanel: React.FC<InterviewNotesPanelProps> = ({
  notes,
  loading = false,
  onAdd,
  onDelete,
}) => {
  const [draft, setDraft] = useState('');
  const [isAdding, setIsAdding] = useState(false);
  const [deletingId, setDeletingId] = useState<number | null>(null);

  const handleAdd = async () => {
    const content = draft.trim();
    if (!content || isAdding) return;
    setIsAdding(true);
    try {
      await onAdd(content);
      setDraft('');
    } finally {
      setIsAdding(false);
    }
  };

  if (loading) {
    return (
      <div className="space-y-3 animate-pulse">
        {[0, 1].map((i) => (
          <div key={i} className="h-16 rounded-lg bg-gray-200" />
        ))}
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Add note */}
      <div className="rounded-xl border border-gray-200 bg-white p-3">
        <textarea
          rows={2}
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
          placeholder="What was asked? What do you need to revise? e.g. Asked Java Streams…"
          className="block w-full resize-none rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 placeholder-gray-400 transition-colors focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
        />
        <div className="mt-2 flex justify-end">
          <button
            onClick={handleAdd}
            disabled={!draft.trim() || isAdding}
            className="inline-flex items-center gap-1.5 rounded-lg bg-indigo-600 px-3 py-1.5 text-xs font-medium text-white transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            <Plus className="h-3.5 w-3.5" />
            {isAdding ? 'Adding…' : 'Add Note'}
          </button>
        </div>
      </div>

      {/* Notes list */}
      {notes.length === 0 ? (
        <div className="flex flex-col items-center py-6 text-center">
          <Inbox className="mb-2 h-8 w-8 text-gray-300" />
          <p className="text-sm font-medium text-gray-500">No notes yet</p>
          <p className="mt-0.5 text-xs text-gray-400">
            Capture interview questions and revision points as they happen.
          </p>
        </div>
      ) : (
        <ul className="space-y-2">
          {notes.map((note) => (
            <li
              key={note.id}
              className="group flex items-start justify-between gap-3 rounded-xl border border-gray-200 bg-white p-3.5 transition-colors hover:border-gray-300"
            >
              <div className="flex items-start gap-2.5 min-w-0">
                <span className="mt-0.5 flex h-7 w-7 flex-shrink-0 items-center justify-center rounded-lg bg-amber-100 text-amber-600">
                  <StickyNote className="h-3.5 w-3.5" />
                </span>
                <div className="min-w-0">
                  <p className="text-sm text-gray-800">{note.content}</p>
                  <p className="mt-0.5 text-[11px] text-gray-400">
                    {formatRelativeTime(note.createdAt)}
                  </p>
                </div>
              </div>
              <button
                onClick={async () => {
                  setDeletingId(note.id);
                  try {
                    await onDelete(note);
                  } finally {
                    setDeletingId(null);
                  }
                }}
                disabled={deletingId === note.id}
                className="rounded-lg p-1.5 text-gray-300 opacity-0 transition-all hover:bg-red-50 hover:text-red-500 group-hover:opacity-100 disabled:opacity-50"
                aria-label="Delete note"
                title="Delete note"
              >
                <Trash2 className="h-3.5 w-3.5" />
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};
