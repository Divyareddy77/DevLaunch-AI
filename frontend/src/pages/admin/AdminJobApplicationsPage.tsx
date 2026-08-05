/**
 * AdminJobApplicationsPage — job application management for administrators.
 *
 * Shows a status-breakdown bar, a paginated table of every application
 * with owner information, and permanent deletion.
 *
 * @author DevLaunch
 */

import React, { useCallback, useEffect, useState } from 'react';
import { Briefcase, RefreshCw, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';
import { adminService } from '../../services/admin.service';
import { StatusBadge } from '../../components/job-applications/StatusBadge';
import { Button } from '../../components/ui/Button';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { ConfirmDeleteModal } from '../../components/admin/ConfirmDeleteModal';
import { AdminPagination } from '../../components/admin/AdminPagination';
import { formatDate } from '../../utils/date';
import { formatNumber } from '../../utils/format';
import { getErrorMessage } from '../../utils/error';
import { MESSAGES } from '../../constants/messages';
import {
  APPLICATION_STATUSES,
  type ApplicationStatusEnum,
} from '../../types/job-application';
import type { AdminJobApplicationResponse, AdminJobApplicationStats } from '../../types/admin';

const PAGE_SIZE = 10;

export const AdminJobApplicationsPage: React.FC = () => {
  const [applications, setApplications] = useState<AdminJobApplicationResponse[]>([]);
  const [stats, setStats] = useState<AdminJobApplicationStats>({});
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [deleteTarget, setDeleteTarget] = useState<AdminJobApplicationResponse | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const fetchApplications = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [pageData, statsData] = await Promise.all([
        adminService.getJobApplications({ page, size: PAGE_SIZE }),
        adminService.getJobApplicationStats(),
      ]);
      setApplications(pageData.content);
      setTotalElements(pageData.totalElements);
      setTotalPages(pageData.totalPages);
      setStats(statsData);
    } catch (err: unknown) {
      setError(getErrorMessage(err, MESSAGES.LOAD_ERROR('job applications')));
    } finally {
      setIsLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchApplications();
  }, [fetchApplications]);

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setIsDeleting(true);
    try {
      await adminService.deleteJobApplication(deleteTarget.id);
      toast.success(MESSAGES.DELETE_SUCCESS('Job application'));
      setApplications((prev) => prev.filter((a) => a.id !== deleteTarget.id));
      setTotalElements((prev) => prev - 1);
      setDeleteTarget(null);
      fetchApplications();
    } catch (err: unknown) {
      toast.error(getErrorMessage(err, MESSAGES.SAVE_ERROR('job application deletion')));
    } finally {
      setIsDeleting(false);
    }
  };

  if (isLoading && applications.length === 0) {
    return <LoadingScreen />;
  }

  return (
    <div className="mx-auto max-w-6xl">
      {/* Header */}
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900 sm:text-2xl">Job Applications</h1>
          <p className="mt-1 text-sm text-gray-500">
            {formatNumber(totalElements)} {totalElements === 1 ? 'application' : 'applications'}{' '}
            across the platform.
          </p>
        </div>
        <Button variant="outline" size="sm" onClick={fetchApplications}>
          <RefreshCw className="h-4 w-4" />
          Refresh
        </Button>
      </div>

      {/* Status breakdown */}
      <div className="mb-6 flex flex-wrap gap-2">
        {APPLICATION_STATUSES.map((status) => {
          const count = stats[status] ?? 0;
          return (
            <div
              key={status}
              className="inline-flex items-center gap-2 rounded-full bg-gray-100 px-3 py-1.5"
            >
              <StatusBadge status={status} size="sm" />
              <span className="text-xs font-semibold text-gray-700">{count}</span>
            </div>
          );
        })}
      </div>

      {error && (
        <div className="mb-4">
          <ErrorMessage message={error} onRetry={fetchApplications} />
        </div>
      )}

      {/* Table */}
      <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-gray-100 bg-gray-50">
              <tr className="text-xs uppercase tracking-wide text-gray-400">
                <th className="px-4 py-3 font-medium">Company</th>
                <th className="px-4 py-3 font-medium">Role</th>
                <th className="px-4 py-3 font-medium">Status</th>
                <th className="px-4 py-3 font-medium">Owner</th>
                <th className="px-4 py-3 font-medium">Applied</th>
                <th className="px-4 py-3 text-right font-medium">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {applications.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-4 py-12 text-center">
                    <Briefcase className="mx-auto mb-3 h-10 w-10 text-gray-300" />
                    <p className="text-sm font-medium text-gray-500">No job applications yet</p>
                  </td>
                </tr>
              ) : (
                applications.map((application) => (
                  <tr key={application.id} className="transition-colors hover:bg-gray-50">
                    <td className="px-4 py-3">
                      <p className="font-medium text-gray-900">{application.companyName}</p>
                      {application.companyLocation && (
                        <p className="text-xs text-gray-400">{application.companyLocation}</p>
                      )}
                    </td>
                    <td className="px-4 py-3 text-gray-700">{application.jobRole}</td>
                    <td className="px-4 py-3">
                      <StatusBadge status={application.status as ApplicationStatusEnum} size="sm" />
                    </td>
                    <td className="px-4 py-3">
                      <p className="text-gray-700">{application.userName}</p>
                      <p className="text-xs text-gray-400">{application.userEmail}</p>
                    </td>
                    <td className="px-4 py-3 text-xs text-gray-400">
                      {application.applicationDate
                        ? formatDate(application.applicationDate)
                        : '—'}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center justify-end gap-1">
                        <button
                          onClick={() => setDeleteTarget(application)}
                          title="Delete application"
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
        resource="job application"
        targetName={
          deleteTarget
            ? `${deleteTarget.companyName} — ${deleteTarget.jobRole}`
            : undefined
        }
        isLoading={isDeleting}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
      />
    </div>
  );
};
