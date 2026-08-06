/**
 * InterviewHistoryCard — the authenticated user's mock interview history.
 *
 * Each interview card shows the category, date, difficulty, duration,
 * score, communication and confidence levels, and a status, with actions
 * to view the full report, retake the category, or delete the session.
 *
 * @author DevLaunch
 */

import React from 'react';
import { History, FileText, RotateCcw, Trash2 } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { Button } from '../ui/Button';
import { Spinner } from '../ui/Spinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import { formatDate } from '../../utils/date';
import { enumToLabel, getScoreBadgeVariant, formatDuration } from '../../utils/format';
import { MESSAGES } from '../../constants/messages';
import type {
  InterviewCategory,
  InterviewHistoryItem,
  InterviewHistoryResponse,
} from '../../types/ai';

interface InterviewHistoryCardProps {
  /** The history response, or null while loading. */
  data: InterviewHistoryResponse | null;
  /** Whether the history is currently being loaded. */
  loading: boolean;
  /** A load error message, if any. */
  error: string | null;
  /** Called to retry loading the history. */
  onRetry: () => void;
  /** Called to view a session's full report. */
  onViewReport: (item: InterviewHistoryItem) => void;
  /** Called to retake a session's category. */
  onRetake: (category: InterviewCategory) => void;
  /** Called to delete a session. */
  onDelete: (sessionId: string) => void;
  /** The session currently being deleted, if any. */
  deletingSessionId?: string | null;
}

/** Maps an interview category to its Badge variant. */
const categoryVariant: Record<InterviewCategory, 'default' | 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
  HR: 'info',
  JAVA: 'warning',
  SPRING_BOOT: 'success',
  SQL: 'primary',
  REACT: 'danger',
};

/** Derives a short status label and tone from the overall score. */
function sessionStatus(score: number): { label: string; tone: 'success' | 'warning' | 'danger' } {
  if (score >= 80) return { label: 'Excellent', tone: 'success' };
  if (score >= 65) return { label: 'Good', tone: 'warning' };
  return { label: 'Needs Work', tone: 'danger' };
}

/** A small labelled progress bar for a dimension score. */
const DimensionLine: React.FC<{ label: string; value: number | null | undefined }> = ({
  label,
  value,
}) => {
  const score = value ?? 0;
  return (
    <div className="min-w-[110px]">
      <div className="flex items-center justify-between gap-2">
        <span className="text-[11px] text-gray-400">{label}</span>
        <span className="text-[11px] font-semibold text-gray-600">{value ?? '—'}</span>
      </div>
      <div className="mt-0.5 h-1 w-full overflow-hidden rounded-full bg-gray-100">
        <div
          className="h-full rounded-full bg-indigo-500"
          style={{ width: `${value == null ? 0 : score}%` }}
        />
      </div>
    </div>
  );
};

export const InterviewHistoryCard: React.FC<InterviewHistoryCardProps> = ({
  data,
  loading,
  error,
  onRetry,
  onViewReport,
  onRetake,
  onDelete,
  deletingSessionId,
}) => {
  return (
    <Card
      header={
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-indigo-100">
            <History className="h-5 w-5 text-indigo-600" />
          </div>
          <h3 className="text-sm font-semibold text-gray-900">Interview History</h3>
          {data && data.totalInterviews > 0 && (
            <Badge variant="default" size="sm">
              {data.totalInterviews} total
            </Badge>
          )}
        </div>
      }
    >
      {loading && !data ? (
        <div className="flex justify-center py-8">
          <Spinner label="Loading history…" />
        </div>
      ) : error && !data ? (
        <ErrorMessage message={error} onRetry={onRetry} />
      ) : data && data.history.length === 0 ? (
        <p className="py-6 text-center text-sm text-gray-400">
          {MESSAGES.NO_INTERVIEW_HISTORY}
        </p>
      ) : (
        data && (
          <ul className="space-y-3">
            {data.history.map((item) => {
              const status = sessionStatus(item.overallScore);
              return (
                <li
                  key={item.sessionId}
                  className="rounded-xl border border-gray-100 p-4 transition-colors hover:border-gray-200 hover:bg-gray-50/50"
                >
                  {/* Header */}
                  <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                    <div className="flex flex-wrap items-center gap-2">
                      <Badge variant={categoryVariant[item.interviewType]}>
                        {enumToLabel(item.interviewType)}
                      </Badge>
                      {item.difficulty && (
                        <Badge variant="default" size="sm">
                          {enumToLabel(item.difficulty)}
                        </Badge>
                      )}
                      <span className="text-xs text-gray-400">
                        {formatDate(item.completedAt)}
                      </span>
                    </div>
                    <div className="flex items-center gap-2">
                      <Badge variant={getScoreBadgeVariant(item.overallScore)} size="sm">
                        {item.overallScore}/100
                      </Badge>
                      <Badge variant={status.tone} size="sm">
                        {status.label}
                      </Badge>
                    </div>
                  </div>

                  {/* Details */}
                  <div className="mt-3 flex flex-wrap items-center gap-x-6 gap-y-2">
                    <span className="text-xs text-gray-500">
                      {formatDuration(item.durationSeconds)} · {item.questionCount} questions
                    </span>
                    {item.timed != null && (
                      <span className="text-xs text-gray-400">
                        {item.timed ? 'Timed' : 'Untimed'}
                      </span>
                    )}
                    <div className="flex gap-4">
                      <DimensionLine label="Communication" value={item.communicationScore} />
                      <DimensionLine label="Confidence" value={item.confidenceScore} />
                    </div>
                  </div>

                  {/* Actions */}
                  <div className="mt-3 flex flex-wrap items-center gap-2 border-t border-gray-100 pt-3">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => onViewReport(item)}
                    >
                      <FileText className="h-3.5 w-3.5" />
                      View Report
                    </Button>
                    <Button
                      variant="secondary"
                      size="sm"
                      onClick={() => onRetake(item.interviewType)}
                    >
                      <RotateCcw className="h-3.5 w-3.5" />
                      Retake
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="ml-auto text-red-500 hover:bg-red-50 hover:text-red-700"
                      loading={deletingSessionId === item.sessionId}
                      onClick={() => onDelete(item.sessionId)}
                    >
                      <Trash2 className="h-3.5 w-3.5" />
                      Delete
                    </Button>
                  </div>
                </li>
              );
            })}
          </ul>
        )
      )}
    </Card>
  );
};
