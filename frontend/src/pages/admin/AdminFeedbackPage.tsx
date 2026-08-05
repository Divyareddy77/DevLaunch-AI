/**
 * AdminFeedbackPage — feedback management for administrators.
 *
 * Lists every user feedback entry with the author and date, and allows
 * permanent deletion.
 *
 * @author DevLaunch
 */

import React, { useCallback, useEffect, useState } from 'react';
import { MessageSquare, RefreshCw, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';
import { adminService } from '../../services/admin.service';
import { Button } from '../../components/ui/Button';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { ConfirmDeleteModal } from '../../components/admin/ConfirmDeleteModal';
import { AdminPagination } from '../../components/admin/AdminPagination';
import { formatRelativeTime } from '../../utils/date';
import { formatNumber } from '../../utils/format';
import { getErrorMessage } from '../../utils/error';
import { MESSAGES } from '../../constants/messages';
import type { AdminFeedbackResponse } from '../../types/admin';

const PAGE_SIZE = 10;

export const AdminFeedbackPage: React.FC = () => {
  const [feedback, setFeedback] = useState<AdminFeedbackResponse[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [deleteTarget, setDeleteTarget] = useState<AdminFeedbackResponse | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const fetchFeedback = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await adminService.getFeedback({ page, size: PAGE_SIZE });
      setFeedback(data.content);
      setTotalElements(data.totalElements);
      setTotalPages(data.totalPages);
    } catch (err: unknown) {
      setError(getErrorMessage(err, MESSAGES.LOAD_ERROR('feedback')));
    } finally {
      setIsLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchFeedback();
  }, [fetchFeedback]);

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setIsDeleting(true);
    try {
      await adminService.deleteFeedback(deleteTarget.id);
      toast.success(MESSAGES.DELETE_SUCCESS('Feedback'));
      setFeedback((prev) => prev.filter((f) => f.id !== deleteTarget.id));
      setTotalElements((prev) => prev - 1);
      setDeleteTarget(null);
    } catch (err: unknown) {
      toast.error(getErrorMessage(err, MESSAGES.SAVE_ERROR('feedback deletion')));
    } finally {
      setIsDeleting(false);
    }
  };

  if (isLoading && feedback.length === 0) {
    return <LoadingScreen />;
  }

  return (
    <div className="mx-auto max-w-4xl">
      {/* Header */}
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900 sm:text-2xl">Feedback</h1>
          <p className="mt-1 text-sm text-gray-500">
            {formatNumber(totalElements)} {totalElements === 1 ? 'message' : 'messages'} from
            users.
          </p>
        </div>
        <Button variant="outline" size="sm" onClick={fetchFeedback}>
          <RefreshCw className="h-4 w-4" />
          Refresh
        </Button>
      </div>

      {error && (
        <div className="mb-4">
          <ErrorMessage message={error} onRetry={fetchFeedback} />
        </div>
      )}

      {/* Feedback list */}
      {feedback.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-2xl border-2 border-dashed border-gray-200 bg-white px-6 py-16 text-center">
          <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-indigo-100">
            <MessageSquare className="h-8 w-8 text-indigo-600" />
          </div>
          <h2 className="mb-2 text-xl font-semibold text-gray-900">No Feedback Yet</h2>
          <p className="max-w-sm text-sm text-gray-500">
            User feedback will appear here as soon as it is submitted.
          </p>
        </div>
      ) : (
        <ul className="space-y-4">
          {feedback.map((entry) => (
            <li
              key={entry.id}
              className="rounded-xl border border-gray-200 bg-white p-5 shadow-sm transition-shadow hover:shadow-md"
            >
              <div className="flex items-start justify-between gap-4">
                <div className="min-w-0">
                  <p className="whitespace-pre-wrap text-sm text-gray-700">{entry.message}</p>
                  <p className="mt-3 text-xs text-gray-400">
                    {entry.userName} · {entry.userEmail} · {formatRelativeTime(entry.createdAt)}
                  </p>
                </div>
                <button
                  onClick={() => setDeleteTarget(entry)}
                  title="Delete feedback"
                  className="flex-shrink-0 rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-red-50 hover:text-red-600"
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}

      {/* Pagination */}
      <div className="mt-4">
        <AdminPagination
          page={page}
          totalPages={totalPages}
          totalElements={totalElements}
          onPageChange={setPage}
        />
      </div>

      {/* Delete confirmation modal */}
      <ConfirmDeleteModal
        isOpen={deleteTarget !== null}
        resource="feedback"
        isLoading={isDeleting}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
      />
    </div>
  );
};
