/**
 * AdminStudyPlannersPage — study plan management for administrators.
 *
 * Lists every study plan entry on the platform with priority/status
 * badges, owner information, and permanent deletion.
 *
 * @author DevLaunch
 */

import React, { useCallback, useEffect, useState } from 'react';
import { CalendarCheck, RefreshCw, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';
import { adminService } from '../../services/admin.service';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { ConfirmDeleteModal } from '../../components/admin/ConfirmDeleteModal';
import { AdminPagination } from '../../components/admin/AdminPagination';
import { formatDate } from '../../utils/date';
import { formatNumber } from '../../utils/format';
import { getErrorMessage } from '../../utils/error';
import { MESSAGES } from '../../constants/messages';
import type { AdminStudyPlannerResponse } from '../../types/admin';

const PAGE_SIZE = 10;

const priorityVariant: Record<string, 'default' | 'warning' | 'danger'> = {
  LOW: 'default',
  MEDIUM: 'warning',
  HIGH: 'danger',
};

const statusVariant: Record<string, 'default' | 'info' | 'success'> = {
  PENDING: 'default',
  IN_PROGRESS: 'info',
  COMPLETED: 'success',
};

export const AdminStudyPlannersPage: React.FC = () => {
  const [plans, setPlans] = useState<AdminStudyPlannerResponse[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [deleteTarget, setDeleteTarget] = useState<AdminStudyPlannerResponse | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const fetchPlans = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await adminService.getStudyPlans({ page, size: PAGE_SIZE });
      setPlans(data.content);
      setTotalElements(data.totalElements);
      setTotalPages(data.totalPages);
    } catch (err: unknown) {
      setError(getErrorMessage(err, MESSAGES.LOAD_ERROR('study plans')));
    } finally {
      setIsLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchPlans();
  }, [fetchPlans]);

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setIsDeleting(true);
    try {
      await adminService.deleteStudyPlan(deleteTarget.id);
      toast.success(MESSAGES.DELETE_SUCCESS('Study plan'));
      setPlans((prev) => prev.filter((p) => p.id !== deleteTarget.id));
      setTotalElements((prev) => prev - 1);
      setDeleteTarget(null);
    } catch (err: unknown) {
      toast.error(getErrorMessage(err, MESSAGES.SAVE_ERROR('study plan deletion')));
    } finally {
      setIsDeleting(false);
    }
  };

  if (isLoading && plans.length === 0) {
    return <LoadingScreen />;
  }

  return (
    <div className="mx-auto max-w-6xl">
      {/* Header */}
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900 sm:text-2xl">Study Plans</h1>
          <p className="mt-1 text-sm text-gray-500">
            {formatNumber(totalElements)} {totalElements === 1 ? 'entry' : 'entries'} across the
            platform.
          </p>
        </div>
        <Button variant="outline" size="sm" onClick={fetchPlans}>
          <RefreshCw className="h-4 w-4" />
          Refresh
        </Button>
      </div>

      {error && (
        <div className="mb-4">
          <ErrorMessage message={error} onRetry={fetchPlans} />
        </div>
      )}

      {/* Table */}
      <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-gray-100 bg-gray-50">
              <tr className="text-xs uppercase tracking-wide text-gray-400">
                <th className="px-4 py-3 font-medium">Title</th>
                <th className="px-4 py-3 font-medium">Date</th>
                <th className="px-4 py-3 font-medium">Priority</th>
                <th className="px-4 py-3 font-medium">Status</th>
                <th className="px-4 py-3 font-medium">Owner</th>
                <th className="px-4 py-3 text-right font-medium">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {plans.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-4 py-12 text-center">
                    <CalendarCheck className="mx-auto mb-3 h-10 w-10 text-gray-300" />
                    <p className="text-sm font-medium text-gray-500">No study plans yet</p>
                  </td>
                </tr>
              ) : (
                plans.map((plan) => (
                  <tr key={plan.id} className="transition-colors hover:bg-gray-50">
                    <td className="px-4 py-3">
                      <p className="font-medium text-gray-900">{plan.title}</p>
                      {plan.description && (
                        <p className="max-w-xs truncate text-xs text-gray-400">
                          {plan.description}
                        </p>
                      )}
                    </td>
                    <td className="px-4 py-3 text-xs text-gray-400">
                      {formatDate(plan.studyDate)}
                    </td>
                    <td className="px-4 py-3">
                      <Badge variant={priorityVariant[plan.priority] ?? 'default'} size="sm">
                        {plan.priority}
                      </Badge>
                    </td>
                    <td className="px-4 py-3">
                      <Badge variant={statusVariant[plan.status] ?? 'default'} size="sm">
                        {plan.status.replace('_', ' ')}
                      </Badge>
                    </td>
                    <td className="px-4 py-3">
                      <p className="text-gray-700">{plan.userName}</p>
                      <p className="text-xs text-gray-400">{plan.userEmail}</p>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center justify-end gap-1">
                        <button
                          onClick={() => setDeleteTarget(plan)}
                          title="Delete study plan"
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
            totalPages={totalPages}
            totalElements={totalElements}
            onPageChange={setPage}
          />
        </div>
      </div>

      {/* Delete confirmation modal */}
      <ConfirmDeleteModal
        isOpen={deleteTarget !== null}
        resource="study plan"
        targetName={deleteTarget?.title}
        isLoading={isDeleting}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
      />
    </div>
  );
};
