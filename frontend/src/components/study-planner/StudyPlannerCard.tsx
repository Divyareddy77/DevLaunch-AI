/**
 * StudyPlannerCard — displays a single study task in the list.
 *
 * Shows title, description, date, time, priority badge, status badge,
 * a status-coloured completion indicator, and edit/delete actions with
 * a hover-lift animation.
 *
 * @author DevLaunch
 */

import React from 'react';
import {
  BookOpen,
  Calendar,
  Clock,
  Edit3,
  Trash2,
  CheckCircle2,
  Loader2,
  Circle,
} from 'lucide-react';
import { Card } from '../ui/Card';
import { PriorityBadge } from './PriorityBadge';
import { StatusBadge } from './StatusBadge';
import type { StudyPlannerResponse, StudyStatusEnum } from '../../types/study-planner';

interface StudyPlannerCardProps {
  task: StudyPlannerResponse;
  onEdit: (id: number) => void;
  onDelete: (id: number) => void;
}

/** Status accent — icon tile + left rail + completion glyph. */
const statusStyles: Record<
  StudyStatusEnum,
  { tile: string; rail: string; glyph: React.ReactNode }
> = {
  COMPLETED: {
    tile: 'bg-emerald-100 text-emerald-600',
    rail: 'bg-emerald-500',
    glyph: <CheckCircle2 className="h-4 w-4 text-emerald-500" />,
  },
  IN_PROGRESS: {
    tile: 'bg-amber-100 text-amber-600',
    rail: 'bg-amber-400',
    glyph: <Loader2 className="h-4 w-4 animate-spin text-amber-500" />,
  },
  PENDING: {
    tile: 'bg-gray-100 text-gray-500',
    rail: 'bg-gray-300',
    glyph: <Circle className="h-4 w-4 text-gray-300" />,
  },
};

export const StudyPlannerCard: React.FC<StudyPlannerCardProps> = ({
  task,
  onEdit,
  onDelete,
}) => {
  const formatTime = (time: string | null) => {
    if (!time) return null;
    try {
      // time comes as HH:mm:ss or HH:mm — trim to HH:mm
      return time.slice(0, 5);
    } catch {
      return time;
    }
  };

  const styles = statusStyles[task.status];
  const isCompleted = task.status === 'COMPLETED';

  return (
    <Card
      className="card-lift group relative overflow-hidden hover:border-gray-200"
      padded={false}
    >
      {/* Status rail */}
      <span className={`absolute inset-y-0 left-0 w-1 ${styles.rail}`} aria-hidden="true" />

      <div className="p-4 pl-5">
        {/* Header row */}
        <div className="flex items-start justify-between gap-3">
          <div className="flex min-w-0 items-start gap-3">
            <div
              className={`flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-lg ${styles.tile}`}
            >
              <BookOpen className="h-5 w-5" />
            </div>
            <div className="min-w-0">
              <h3
                className={`truncate text-sm font-semibold ${
                  isCompleted ? 'text-gray-400 line-through decoration-gray-300' : 'text-gray-900'
                }`}
              >
                {task.title}
              </h3>
              <div className="mt-1 flex flex-wrap items-center gap-2">
                <PriorityBadge priority={task.priority} size="sm" />
                <StatusBadge status={task.status} size="sm" />
              </div>
            </div>
          </div>

          {/* Completion glyph */}
          <span className="flex-shrink-0 pt-0.5" aria-hidden="true">
            {styles.glyph}
          </span>
        </div>

        {/* Description preview */}
        {task.description && (
          <p className="mt-3 line-clamp-2 text-sm text-gray-500">{task.description}</p>
        )}

        {/* Date & time */}
        <div className="mt-3 flex flex-wrap gap-x-4 gap-y-1.5">
          <div className="flex items-center gap-1.5 text-xs text-gray-500">
            <Calendar className="h-3.5 w-3.5 flex-shrink-0 text-gray-400" />
            <span>{task.studyDate}</span>
          </div>
          {(task.startTime || task.endTime) && (
            <div className="flex items-center gap-1.5 text-xs text-gray-500">
              <Clock className="h-3.5 w-3.5 flex-shrink-0 text-gray-400" />
              <span>
                {formatTime(task.startTime) ?? '?'}
                {task.endTime ? ` — ${formatTime(task.endTime)}` : ''}
              </span>
            </div>
          )}
        </div>
      </div>

      {/* Actions footer */}
      <div className="flex items-center justify-end gap-1 border-t border-gray-100 bg-gray-50/60 px-4 py-2 pl-5">
        <button
          onClick={() => onEdit(task.id)}
          className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-700"
          aria-label="Edit task"
          title="Edit"
        >
          <Edit3 className="h-4 w-4" />
        </button>
        <button
          onClick={() => onDelete(task.id)}
          className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-red-50 hover:text-red-500"
          aria-label="Delete task"
          title="Delete"
        >
          <Trash2 className="h-4 w-4" />
        </button>
      </div>
    </Card>
  );
};
