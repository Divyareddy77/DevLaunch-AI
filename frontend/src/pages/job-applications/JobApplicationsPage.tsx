/**
 * JobApplicationsPage — the main job applications list page.
 *
 * Displays all applications with client-side search, status filter,
 * and sorting. Handles loading, error, empty, and filtered-empty states.
 *
 * @author DevLaunch
 */

import React, { useEffect, useState, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Briefcase, RefreshCw, ArrowUpDown } from 'lucide-react';
import toast from 'react-hot-toast';
import { jobApplicationService } from '../../services/job-application.service';
import { JobApplicationCard } from '../../components/job-applications/JobApplicationCard';
import { JobFilters } from '../../components/job-applications/JobFilters';
import { StatusBadge } from '../../components/job-applications/StatusBadge';
import { Button } from '../../components/ui/Button';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { Modal } from '../../components/ui/Modal';
import { ROUTES } from '../../constants/routes';
import { MESSAGES } from '../../constants/messages';
import {
  APPLICATION_STATUS_LABELS,
  type JobApplicationResponse,
  type ApplicationStatusEnum,
} from '../../types/job-application';

type SortField = 'applicationDate' | 'companyName';
type SortDir = 'asc' | 'desc';

export const JobApplicationsPage: React.FC = () => {
  const navigate = useNavigate();

  // Data state
  const [applications, setApplications] = useState<JobApplicationResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Filter state
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<ApplicationStatusEnum | null>(null);

  // Sort state
  const [sortField, setSortField] = useState<SortField>('applicationDate');
  const [sortDir, setSortDir] = useState<SortDir>('desc');

  // Delete state
  const [deleteTarget, setDeleteTarget] = useState<number | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  // ─── Data fetching ───
  const fetchApplications = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await jobApplicationService.getAll();
      setApplications(data);
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : MESSAGES.LOAD_ERROR('job applications');
      setError(message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchApplications();
  }, [fetchApplications]);

  // ─── Filtering & sorting ───
  const filteredAndSorted = useMemo(() => {
    let result = [...applications];

    // Status filter
    if (statusFilter) {
      result = result.filter((a) => a.status === statusFilter);
    }

    // Search filter (company name or job role)
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase().trim();
      result = result.filter(
        (a) =>
          a.companyName.toLowerCase().includes(q) ||
          a.jobRole.toLowerCase().includes(q),
      );
    }

    // Sort
    result.sort((a, b) => {
      let cmp: number;
      if (sortField === 'companyName') {
        cmp = a.companyName.localeCompare(b.companyName);
      } else {
        // applicationDate
        const aDate = a.applicationDate ?? '';
        const bDate = b.applicationDate ?? '';
        cmp = aDate.localeCompare(bDate);
      }
      return sortDir === 'asc' ? cmp : -cmp;
    });

    return result;
  }, [applications, statusFilter, searchQuery, sortField, sortDir]);

  // ─── Delete ───
  const handleDelete = async () => {
    if (deleteTarget === null) return;
    setIsDeleting(true);
    try {
      await jobApplicationService.delete(deleteTarget);
      toast.success(MESSAGES.DELETE_SUCCESS('Job application'));
      setApplications((prev) => prev.filter((a) => a.id !== deleteTarget));
      setDeleteTarget(null);
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : MESSAGES.SAVE_ERROR('job application');
      toast.error(message);
    } finally {
      setIsDeleting(false);
    }
  };

  // ─── Toggle sort direction or change field ───
  const toggleSort = (field: SortField) => {
    if (sortField === field) {
      setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortField(field);
      setSortDir('desc');
    }
  };

  // ─── Stats summary ───
  const stats = useMemo(() => {
    const total = applications.length;
    const counts: Partial<Record<ApplicationStatusEnum, number>> = {};
    for (const app of applications) {
      counts[app.status] = (counts[app.status] ?? 0) + 1;
    }
    return { total, counts };
  }, [applications]);

  // ─── Loading state ───
  if (isLoading) {
    return <LoadingScreen />;
  }

  // ─── Error state ───
  if (error) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh]">
        <ErrorMessage message={error} onRetry={fetchApplications} />
      </div>
    );
  }

  // ─── Empty state (no applications at all) ───
  if (applications.length === 0) {
    return (
      <div className="mx-auto max-w-3xl">
        <div className="flex flex-col items-center justify-center rounded-2xl border-2 border-dashed border-gray-200 bg-white px-6 py-16 text-center">
          <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-indigo-100">
            <Briefcase className="h-8 w-8 text-indigo-600" />
          </div>
          <h2 className="mb-2 text-xl font-semibold text-gray-900">
            No Applications Yet
          </h2>
          <p className="mb-6 max-w-sm text-sm text-gray-500">
            {MESSAGES.NO_JOB_APPLICATIONS}
          </p>
          <Button onClick={() => navigate(ROUTES.JOB_APPLICATION_CREATE)}>
            <Plus className="h-4 w-4" />
            Add Your First Application
          </Button>
        </div>
      </div>
    );
  }

  // ─── Data state ───
  return (
    <div className="mx-auto max-w-6xl">
      {/* Page header */}
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Job Applications</h1>
          <p className="mt-1 text-sm text-gray-500">
            Track your job search. You have {applications.length}{' '}
            {applications.length === 1 ? 'application' : 'applications'}.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={fetchApplications}>
            <RefreshCw className="h-4 w-4" />
            Refresh
          </Button>
          <Button onClick={() => navigate(ROUTES.JOB_APPLICATION_CREATE)}>
            <Plus className="h-4 w-4" />
            Add Application
          </Button>
        </div>
      </div>

      {/* Status summary bar */}
      <div className="mb-6 flex flex-wrap gap-2">
        {(Object.keys(APPLICATION_STATUS_LABELS) as ApplicationStatusEnum[]).map((status) => {
          const count = stats.counts[status] ?? 0;
          return (
            <button
              key={status}
              onClick={() =>
                setStatusFilter(
                  statusFilter === status ? null : status,
                )
              }
              className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1.5 text-xs font-medium transition-colors ${
                statusFilter === status
                  ? 'bg-indigo-100 text-indigo-700 ring-1 ring-indigo-300'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              <StatusBadge status={status} size="sm" />
              <span>{count}</span>
            </button>
          );
        })}
      </div>

      {/* Filters */}
      <div className="mb-6">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex-1">
            <JobFilters
              searchQuery={searchQuery}
              onSearchChange={setSearchQuery}
              statusFilter={statusFilter}
              onStatusFilterChange={setStatusFilter}
            />
          </div>
          <div className="flex items-center gap-2">
            <span className="text-xs text-gray-400">Sort by:</span>
            <button
              onClick={() => toggleSort('applicationDate')}
              className={`inline-flex items-center gap-1 rounded-lg px-2.5 py-1.5 text-xs font-medium transition-colors ${
                sortField === 'applicationDate'
                  ? 'bg-indigo-100 text-indigo-700'
                  : 'text-gray-500 hover:bg-gray-100'
              }`}
            >
              <ArrowUpDown className="h-3 w-3" />
              Date
              {sortField === 'applicationDate' && (
                <span className="text-[10px]">{sortDir === 'asc' ? '↑' : '↓'}</span>
              )}
            </button>
            <button
              onClick={() => toggleSort('companyName')}
              className={`inline-flex items-center gap-1 rounded-lg px-2.5 py-1.5 text-xs font-medium transition-colors ${
                sortField === 'companyName'
                  ? 'bg-indigo-100 text-indigo-700'
                  : 'text-gray-500 hover:bg-gray-100'
              }`}
            >
              <ArrowUpDown className="h-3 w-3" />
              Company
              {sortField === 'companyName' && (
                <span className="text-[10px]">{sortDir === 'asc' ? '↑' : '↓'}</span>
              )}
            </button>
          </div>
        </div>
      </div>

      {/* Filtered-empty state */}
      {filteredAndSorted.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-xl border border-gray-200 bg-white py-12 text-center">
          <Briefcase className="mb-3 h-10 w-10 text-gray-300" />
          <p className="text-sm font-medium text-gray-500">
            No applications match your search criteria.
          </p>
          <button
            onClick={() => {
              setSearchQuery('');
              setStatusFilter(null);
            }}
            className="mt-2 text-xs font-medium text-indigo-600 hover:text-indigo-800"
          >
            Clear all filters
          </button>
        </div>
      ) : (
        /* Application grid */
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {filteredAndSorted.map((app) => (
            <JobApplicationCard
              key={app.id}
              application={app}
              onView={(id) => navigate(ROUTES.JOB_APPLICATION_EDIT(id))}
              onEdit={(id) => navigate(ROUTES.JOB_APPLICATION_EDIT(id))}
              onDelete={(id) => setDeleteTarget(id)}
            />
          ))}
        </div>
      )}

      {/* Delete confirmation modal */}
      <Modal
        isOpen={deleteTarget !== null}
        onClose={() => setDeleteTarget(null)}
        title="Delete Application"
        closeOnBackdrop={false}
      >
        <p className="text-sm text-gray-600">
          {MESSAGES.DELETE_CONFIRM('job application')}
        </p>
        <div className="mt-6 flex justify-end gap-2">
          <Button variant="ghost" onClick={() => setDeleteTarget(null)} disabled={isDeleting}>
            Cancel
          </Button>
          <Button variant="danger" onClick={handleDelete} loading={isDeleting}>
            Delete
          </Button>
        </div>
      </Modal>
    </div>
  );
};
