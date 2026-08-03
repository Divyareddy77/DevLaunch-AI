/**
 * InterviewHistoryCard — the authenticated user's mock interview history.
 *
 * Shows summary statistics (total interviews, average score) and a list
 * of completed sessions with their category, completion date, score, and
 * question count. Handles its own loading, error, and empty states.
 *
 * @author DevLaunch
 */

import React from 'react';
import { History, CalendarCheck, Award } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { Spinner } from '../ui/Spinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import { formatDate } from '../../utils/date';
import { enumToLabel, getScoreBadgeVariant } from '../../utils/format';
import { MESSAGES } from '../../constants/messages';
import type { InterviewCategory, InterviewHistoryResponse } from '../../types/ai';

interface InterviewHistoryCardProps {
  /** The history response, or null while loading. */
  data: InterviewHistoryResponse | null;
  /** Whether the history is currently being loaded. */
  loading: boolean;
  /** A load error message, if any. */
  error: string | null;
  /** Called to retry loading the history. */
  onRetry: () => void;
}

/** Maps an interview category to its Badge variant. */
const categoryVariant: Record<InterviewCategory, 'default' | 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
  HR: 'info',
  JAVA: 'warning',
  SPRING_BOOT: 'success',
  SQL: 'primary',
  REACT: 'danger',
};

export const InterviewHistoryCard: React.FC<InterviewHistoryCardProps> = ({
  data,
  loading,
  error,
  onRetry,
}) => {
  return (
    <Card
      header={
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-indigo-100">
            <History className="h-5 w-5 text-indigo-600" />
          </div>
          <h3 className="text-sm font-semibold text-gray-900">Interview History</h3>
        </div>
      }
    >
      {loading ? (
        <div className="flex justify-center py-8">
          <Spinner label="Loading history…" />
        </div>
      ) : error ? (
        <ErrorMessage message={error} onRetry={onRetry} />
      ) : data && data.history.length === 0 ? (
        <p className="py-6 text-center text-sm text-gray-400">
          {MESSAGES.NO_INTERVIEW_HISTORY}
        </p>
      ) : (
        data && (
          <div className="space-y-4">
            {/* Summary stats */}
            <div className="grid grid-cols-2 gap-3">
              <div className="flex items-center gap-3 rounded-lg bg-gray-50 p-3">
                <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-indigo-100">
                  <CalendarCheck className="h-4 w-4 text-indigo-600" />
                </div>
                <div>
                  <p className="text-lg font-bold text-gray-900">{data.totalInterviews}</p>
                  <p className="text-xs text-gray-500">Total interviews</p>
                </div>
              </div>
              <div className="flex items-center gap-3 rounded-lg bg-gray-50 p-3">
                <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-emerald-100">
                  <Award className="h-4 w-4 text-emerald-600" />
                </div>
                <div>
                  <p className="text-lg font-bold text-gray-900">
                    {data.averageScore.toFixed(1)}
                  </p>
                  <p className="text-xs text-gray-500">Average score</p>
                </div>
              </div>
            </div>

            {/* Session list */}
            <ul className="space-y-2.5">
              {data.history.map((item) => (
                <li
                  key={item.sessionId}
                  className="flex flex-col gap-2 rounded-lg border border-gray-100 p-3 sm:flex-row sm:items-center sm:justify-between"
                >
                  <div className="flex items-center gap-3">
                    <Badge variant={categoryVariant[item.interviewType]}>
                      {enumToLabel(item.interviewType)}
                    </Badge>
                    <p className="text-sm text-gray-500">
                      {formatDate(item.completedAt)} · {item.questionCount} questions
                    </p>
                  </div>
                  <Badge variant={getScoreBadgeVariant(item.overallScore)} size="sm">
                    {item.overallScore}/100
                  </Badge>
                </li>
              ))}
            </ul>
          </div>
        )
      )}
    </Card>
  );
};
