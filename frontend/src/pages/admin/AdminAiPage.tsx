/**
 * AdminAiPage — AI module monitoring for administrators.
 *
 * Two tabs: AI Resume Review history (scores, target role, user) and
 * Mock Interview history (category, score, question count) with the
 * ability to delete individual interview sessions.
 *
 * @author DevLaunch
 */

import React, { useCallback, useEffect, useState } from 'react';
import { FileSearch, Bot, RefreshCw, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';
import { adminService } from '../../services/admin.service';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { ConfirmDeleteModal } from '../../components/admin/ConfirmDeleteModal';
import { AdminPagination } from '../../components/admin/AdminPagination';
import { formatDate, formatRelativeTime } from '../../utils/date';
import { getScoreBadgeVariant } from '../../utils/format';
import { getErrorMessage } from '../../utils/error';
import { MESSAGES } from '../../constants/messages';
import type {
  AdminInterviewSessionResponse,
  AdminResumeReviewResponse,
} from '../../types/admin';

type Tab = 'reviews' | 'interviews';

const PAGE_SIZE = 10;

export const AdminAiPage: React.FC = () => {
  const [tab, setTab] = useState<Tab>('reviews');

  // Resume review history state
  const [reviews, setReviews] = useState<AdminResumeReviewResponse[]>([]);
  const [reviewsTotal, setReviewsTotal] = useState(0);
  const [reviewsPages, setReviewsPages] = useState(0);

  // Interview history state
  const [interviews, setInterviews] = useState<AdminInterviewSessionResponse[]>([]);
  const [interviewsTotal, setInterviewsTotal] = useState(0);
  const [interviewsPages, setInterviewsPages] = useState(0);

  const [page, setPage] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [deleteTarget, setDeleteTarget] = useState<AdminInterviewSessionResponse | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const fetchReviews = useCallback(async () => {
    const data = await adminService.getResumeReviews({ page, size: PAGE_SIZE });
    setReviews(data.content);
    setReviewsTotal(data.totalElements);
    setReviewsPages(data.totalPages);
  }, [page]);

  const fetchInterviews = useCallback(async () => {
    const data = await adminService.getInterviewSessions({ page, size: PAGE_SIZE });
    setInterviews(data.content);
    setInterviewsTotal(data.totalElements);
    setInterviewsPages(data.totalPages);
  }, [page]);

  const fetchActiveTab = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      if (tab === 'reviews') {
        await fetchReviews();
      } else {
        await fetchInterviews();
      }
    } catch (err: unknown) {
      setError(getErrorMessage(err, MESSAGES.LOAD_ERROR('AI reports')));
    } finally {
      setIsLoading(false);
    }
  }, [tab, fetchReviews, fetchInterviews]);

  useEffect(() => {
    fetchActiveTab();
  }, [fetchActiveTab]);

  const handleDeleteInterview = async () => {
    if (!deleteTarget) return;
    setIsDeleting(true);
    try {
      await adminService.deleteInterviewSession(deleteTarget.id);
      toast.success(MESSAGES.DELETE_SUCCESS('Interview session'));
      setInterviews((prev) => prev.filter((i) => i.id !== deleteTarget.id));
      setInterviewsTotal((prev) => prev - 1);
      setDeleteTarget(null);
    } catch (err: unknown) {
      toast.error(getErrorMessage(err, MESSAGES.SAVE_ERROR('interview deletion')));
    } finally {
      setIsDeleting(false);
    }
  };

  if (isLoading && (tab === 'reviews' ? reviews.length === 0 : interviews.length === 0)) {
    return <LoadingScreen />;
  }

  return (
    <div className="mx-auto max-w-6xl">
      {/* Header */}
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900 sm:text-2xl">AI Reports</h1>
          <p className="mt-1 text-sm text-gray-500">
            Monitor AI resume reviews and mock interview activity.
          </p>
        </div>
        <Button variant="outline" size="sm" onClick={fetchActiveTab}>
          <RefreshCw className="h-4 w-4" />
          Refresh
        </Button>
      </div>

      {/* Tabs */}
      <div className="mb-6 flex gap-2">
        <button
          onClick={() => {
            setTab('reviews');
            setPage(0);
          }}
          className={`inline-flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-medium transition-colors ${
            tab === 'reviews'
              ? 'bg-primary-600 text-white shadow-sm'
              : 'bg-white text-gray-600 hover:bg-gray-100'
          }`}
        >
          <FileSearch className="h-4 w-4" />
          Resume Reviews
        </button>
        <button
          onClick={() => {
            setTab('interviews');
            setPage(0);
          }}
          className={`inline-flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-medium transition-colors ${
            tab === 'interviews'
              ? 'bg-primary-600 text-white shadow-sm'
              : 'bg-white text-gray-600 hover:bg-gray-100'
          }`}
        >
          <Bot className="h-4 w-4" />
          Mock Interviews
        </button>
      </div>

      {error && (
        <div className="mb-4">
          <ErrorMessage message={error} onRetry={fetchActiveTab} />
        </div>
      )}

      {/* Resume reviews table */}
      {tab === 'reviews' && (
        <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-gray-100 bg-gray-50">
                <tr className="text-xs uppercase tracking-wide text-gray-400">
                  <th className="px-4 py-3 font-medium">Resume</th>
                  <th className="px-4 py-3 font-medium">Target Role</th>
                  <th className="px-4 py-3 font-medium">Resume Score</th>
                  <th className="px-4 py-3 font-medium">ATS Score</th>
                  <th className="px-4 py-3 font-medium">User</th>
                  <th className="px-4 py-3 font-medium">Date</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {reviews.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="px-4 py-12 text-center">
                      <FileSearch className="mx-auto mb-3 h-10 w-10 text-gray-300" />
                      <p className="text-sm font-medium text-gray-500">No resume reviews yet</p>
                    </td>
                  </tr>
                ) : (
                  reviews.map((review) => (
                    <tr key={review.id} className="transition-colors hover:bg-gray-50">
                      <td className="px-4 py-3 font-medium text-gray-900">
                        {review.resumeTitle}
                      </td>
                      <td className="px-4 py-3">
                        {review.targetRole ? (
                          <Badge variant="info" size="sm">
                            {review.targetRole}
                          </Badge>
                        ) : (
                          <span className="text-xs text-gray-400">—</span>
                        )}
                      </td>
                      <td className="px-4 py-3">
                        <Badge variant={getScoreBadgeVariant(review.resumeScore)} size="sm">
                          {review.resumeScore}/100
                        </Badge>
                      </td>
                      <td className="px-4 py-3">
                        <Badge variant={getScoreBadgeVariant(review.atsScore)} size="sm">
                          {review.atsScore}/100
                        </Badge>
                      </td>
                      <td className="px-4 py-3">
                        <p className="text-gray-700">{review.userName}</p>
                        <p className="text-xs text-gray-400">{review.userEmail}</p>
                      </td>
                      <td className="px-4 py-3 text-xs text-gray-400">
                        {formatDate(review.createdAt)}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
          <div className="border-t border-gray-100 px-4 py-3">
            <AdminPagination
              page={page}
              totalPages={reviewsPages}
              totalElements={reviewsTotal}
              onPageChange={setPage}
            />
          </div>
        </div>
      )}

      {/* Interview history table */}
      {tab === 'interviews' && (
        <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-gray-100 bg-gray-50">
                <tr className="text-xs uppercase tracking-wide text-gray-400">
                  <th className="px-4 py-3 font-medium">User</th>
                  <th className="px-4 py-3 font-medium">Category</th>
                  <th className="px-4 py-3 font-medium">Score</th>
                  <th className="px-4 py-3 font-medium">Questions</th>
                  <th className="px-4 py-3 font-medium">Completed</th>
                  <th className="px-4 py-3 text-right font-medium">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {interviews.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="px-4 py-12 text-center">
                      <Bot className="mx-auto mb-3 h-10 w-10 text-gray-300" />
                      <p className="text-sm font-medium text-gray-500">
                        No mock interviews yet
                      </p>
                    </td>
                  </tr>
                ) : (
                  interviews.map((session) => (
                    <tr key={session.id} className="transition-colors hover:bg-gray-50">
                      <td className="px-4 py-3">
                        <p className="text-gray-700">{session.userName}</p>
                        <p className="text-xs text-gray-400">{session.userEmail}</p>
                      </td>
                      <td className="px-4 py-3">
                        <Badge variant="info" size="sm">
                          {session.interviewType}
                        </Badge>
                      </td>
                      <td className="px-4 py-3">
                        <Badge variant={getScoreBadgeVariant(session.overallScore)} size="sm">
                          {session.overallScore}/100
                        </Badge>
                      </td>
                      <td className="px-4 py-3 text-gray-600">{session.questionCount}</td>
                      <td className="px-4 py-3 text-xs text-gray-400">
                        {formatRelativeTime(session.completedAt)}
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex items-center justify-end gap-1">
                          <button
                            onClick={() => setDeleteTarget(session)}
                            title="Delete interview"
                            className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-red-50 hover:text-red-600"
                          >
                            <Trash2 className="h-4 w-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
          <div className="border-t border-gray-100 px-4 py-3">
            <AdminPagination
              page={page}
              totalPages={interviewsPages}
              totalElements={interviewsTotal}
              onPageChange={setPage}
            />
          </div>
        </div>
      )}

      {/* Delete confirmation modal */}
      <ConfirmDeleteModal
        isOpen={deleteTarget !== null}
        resource="interview session"
        targetName={deleteTarget ? `${deleteTarget.interviewType} (${deleteTarget.overallScore}/100)` : undefined}
        isLoading={isDeleting}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDeleteInterview}
      />
    </div>
  );
};
