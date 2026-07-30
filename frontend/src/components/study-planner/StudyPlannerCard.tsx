/**
 * StudyPlannerCard — displays a single study task in the list.
 *
 * Shows title, description, date, time, priority badge, status badge,
 * and edit/delete actions.
 *
 * @author DevLaunch
 */

import React from 'react';
import { BookOpen, Calendar, Clock, Edit3, Trash2 } from 'lucide-react';
import { Card } from '../ui/Card';
import { PriorityBadge } from './PriorityBadge';
import { StatusBadge } from './StatusBadge';
import type { StudyPlannerResponse } from '../../types/study-planner';

interface StudyPlannerCardProps {
  task: StudyPlannerResponse;
  onEdit: (id: number) => void;
  onDelete: (id: number) => void;
}

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

  return (
    <Card className="group transition-all hover:shadow-md hover:border-primary-200" padded={false}>
      <div className="p-5">
        {/* Header row */}
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-start gap-3 min-w-0">
            <div className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-lg bg-emerald-100 text-emerald-600">
              <BookOpen className="h-5 w-5" />
            </div>
            <div className="min-w-0">
              <h3 className="truncate text-sm font-semibold text-gray-900">
                {task.title}
              </h3>
              <div className="mt-0.5 flex flex-wrap items-center gap-2">
                <PriorityBadge priority={task.priority} size="sm" />
                <StatusBadge status={task.status} size="sm" />
              </div>
            </div>
          </div>
        </div>

        {/* Description preview */}
        {task.description && (
          <p className="mt-3 line-clamp-2 text-sm text-gray-500">
            {task.description}
          </p>
        )}

        {/* Date & time */}
        <div className="mt-3 space-y-1">
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
      <div className="flex items-center justify-end border-t border-gray-100 px-5 py-3 gap-1">
        <button
          onClick={() => onEdit(task.id)}
          className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors"
          aria-label="Edit task"
          title="Edit"
        >
          <Edit3 className="h-4 w-4" />
        </button>
        <button
          onClick={() => onDelete(task.id)}
          className="rounded-lg p-1.5 text-gray-400 hover:bg-red-50 hover:text-red-500 transition-colors"
          aria-label="Delete task"
          title="Delete"
        >
          <Trash2 className="h-4 w-4" />
        </button>
      </div>
    </Card>
  );
};
