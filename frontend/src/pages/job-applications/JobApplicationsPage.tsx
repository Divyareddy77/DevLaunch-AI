/**
 * JobApplicationsPage — the main job applications page.
 *
 * Offers two synchronized views: the classic list/grid view and a Kanban
 * board view. Both share the same search, filters, and sorting, and stay in
 * sync — dragging a card on the board updates the status, and any mutation
 * refreshes both views. Also includes richer analytics, an application
 * detail dialog, and placement quick actions (move status, schedule
 * interview, notes, attachments).
 *
 * @author DevLaunch
 */

import React, { useEffect, useState, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Plus,
  Briefcase,
  RefreshCw,
  ArrowUpDown,
  LayoutGrid,
  Kanban,
  BarChart3,
} from 'lucide-react';
import toast from 'react-hot-toast';
import { jobApplicationService } from '../../services/job-application.service';
import { JobApplicationCard } from '../../components/job-applications/JobApplicationCard';
import { JobFilters, EMPTY_ADVANCED_FILTERS, type AdvancedJobFilters } from '../../components/job-applications/JobFilters';
import { JobBoardView } from '../../components/job-applications/JobBoardView';
import { ApplicationDetailModal } from '../../components/job-applications/ApplicationDetailModal';
import { ApplicationAnalytics } from '../../components/job-applications/ApplicationAnalytics';
import { MoveStatusModal } from '../../components/job-applications/MoveStatusModal';
import { ScheduleInterviewModal } from '../../components/job-applications/ScheduleInterviewModal';
import { InterviewNotesModal } from '../../components/job-applications/InterviewNotesModal';
import { AttachmentsModal } from '../../components/job-applications/AttachmentsModal';
import { StatusBadge } from '../../components/job-applications/StatusBadge';
import { Button } from '../../components/ui/Button';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { Modal } from '../../components/ui/Modal';
import { ROUTES } from '../../constants/routes';
import { MESSAGES } from '../../constants/messages';
import { applicationMatchesKeyword } from '../../utils/jobApplication';
import {
  APPLICATION_STATUS_LABELS,
  type JobApplicationResponse,
  type ApplicationStatusEnum,
} from '../../types/job-application';
import type { ApplicationAnalytics as ApplicationAnalyticsData } from '../../types/job-application';
import type { ScheduleInterviewRequest } from '../../types/job-application';

type SortField = 'applicationDate' | 'companyName';
type SortDir = 'asc' | 'desc';
type ViewMode = 'list' | 'board';

export const JobApplicationsPage: React.FC = () => {
  const navigate = useNavigate();

  // Data state
  const [applications, setApplications] = useState<JobApplicationResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // View mode
  const [viewMode, setViewMode] = useState<ViewMode>('list');

  // Filter state
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<ApplicationStatusEnum | null>(null);
  const [advancedFilters, setAdvancedFilters] = useState<AdvancedJobFilters>(EMPTY_ADVANCED_FILTERS);

  // Sort state
  const [sortField, setSortField] = useState<SortField>('applicationDate');
  const [sortDir, setSortDir] = useState<SortDir>('desc');

  // Delete state
  const [deleteTarget, setDeleteTarget] = useState<number | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  // Detail / quick-action state
  const [detailApp, setDetailApp] = useState<JobApplicationResponse | null>(null);
  const [moveTarget, setMoveTarget] = useState<JobApplicationResponse | null>(null);
  const [scheduleTarget, setScheduleTarget] = useState<JobApplicationResponse | null>(null);
  const [notesTarget, setNotesTarget] = useState<JobApplicationResponse | null>(null);
  const [attachmentsTarget, setAttachmentsTarget] = useState<JobApplicationResponse | null>(null);
  const [isStatusUpdating, setIsStatusUpdating] = useState(false);

  // Analytics state
  const [analytics, setAnalytics] = useState<ApplicationAnalyticsData | null>(null);
  const [showAnalytics, setShowAnalytics] = useState(false);
  const [isAnalyticsLoading, setIsAnalyticsLoading] = useState(false);

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

  // Keep the open detail dialog in sync when the applications list refreshes
  // (e.g. after scheduling an interview inside the dialog).
  useEffect(() => {
    if (!detailApp) return;
    const fresh = applications.find((app) => app.id === detailApp.id);
    if (fresh) setDetailApp(fresh);
  }, [applications, detailApp]);

  // Keep the two views in sync after in-modal mutations.
  useEffect(() => {
    const handleRefresh = () => {
      fetchApplications();
    };
    const handleOpenResume = (event: Event) => {
      const resumeId = (event as CustomEvent<number>).detail;
      if (resumeId) navigate(ROUTES.RESUME_EDIT(resumeId));
    };
    window.addEventListener('job-applications:refresh', handleRefresh);
    window.addEventListener('job-applications:open-resume', handleOpenResume);
    return () => {
      window.removeEventListener('job-applications:refresh', handleRefresh);
      window.removeEventListener('job-applications:open-resume', handleOpenResume);
    };
  }, [fetchApplications, navigate]);

  const fetchAnalytics = useCallback(async () => {
    setIsAnalyticsLoading(true);
    try {
      const data = await jobApplicationService.getAnalytics();
      setAnalytics(data);
    } catch {
      toast.error('Failed to load analytics.');
    } finally {
      setIsAnalyticsLoading(false);
    }
  }, []);

  const toggleAnalytics = useCallback(() => {
    if (!analytics && !isAnalyticsLoading) {
      fetchAnalytics();
    }
    setShowAnalytics((show) => !show);
  }, [analytics, isAnalyticsLoading, fetchAnalytics]);

  // ─── Filtering & sorting ───
  const filteredAndSorted = useMemo(() => {
    let result = [...applications];

    // Status filter
    if (statusFilter) {
      result = result.filter((a) => a.status === statusFilter);
    }

    // Keyword search (company, role, location, notes, recruiter, technology)
    if (searchQuery.trim()) {
      result = result.filter((a) => applicationMatchesKeyword(a, searchQuery));
    }

    // Advanced filters
    const {
      company,
      role,
      location,
      salary,
      workMode,
      priority,
      applicationDateFrom,
      applicationDateTo,
      interviewDateFrom,
      interviewDateTo,
    } = advancedFilters;

    if (company.trim()) {
      result = result.filter((a) => a.companyName.toLowerCase().includes(company.toLowerCase()));
    }
    if (role.trim()) {
      result = result.filter((a) => a.jobRole.toLowerCase().includes(role.toLowerCase()));
    }
    if (location.trim()) {
      result = result.filter((a) =>
        (a.companyLocation ?? '').toLowerCase().includes(location.toLowerCase()),
      );
    }
    if (salary.trim()) {
      const q = salary.toLowerCase();
      result = result.filter((a) => (a.salary ?? '').toLowerCase().includes(q));
    }
    if (workMode) {
      result = result.filter((a) => a.workMode === workMode);
    }
    if (priority) {
      result = result.filter((a) => a.priority === priority);
    }
    if (applicationDateFrom) {
      result = result.filter((a) => (a.applicationDate ?? '') >= applicationDateFrom);
    }
    if (applicationDateTo) {
      result = result.filter((a) => (a.applicationDate ?? '') <= applicationDateTo);
    }
    if (interviewDateFrom || interviewDateTo) {
      result = result.filter((a) =>
        a.interviews.some((interview) => {
          if (interview.cancelled) return false;
          if (interviewDateFrom && interview.scheduledDate < interviewDateFrom) return false;
          if (interviewDateTo && interview.scheduledDate > interviewDateTo) return false;
          return true;
        }),
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
  }, [applications, statusFilter, searchQuery, advancedFilters, sortField, sortDir]);

  // ─── Status change (board drag / move modal) ───
  const handleStatusChange = useCallback(
    async (applicationId: number, status: ApplicationStatusEnum) => {
      setIsStatusUpdating(true);
      try {
        const updated = await jobApplicationService.updateStatus(applicationId, status);
        setApplications((prev) => prev.map((app) => (app.id === applicationId ? updated : app)));
        setMoveTarget(null);
        toast.success(`Moved to ${APPLICATION_STATUS_LABELS[status]}`);
      } catch (err: unknown) {
        const message =
          err instanceof Error ? err.message : MESSAGES.SAVE_ERROR('job application');
        toast.error(message);
      } finally {
        setIsStatusUpdating(false);
      }
    },
    [],
  );

  // ─── Interview scheduling ───
  const handleScheduleSubmit = useCallback(
    async (data: ScheduleInterviewRequest) => {
      if (!scheduleTarget) return;
      try {
        await jobApplicationService.scheduleInterview(scheduleTarget.id, data);
        toast.success('Interview scheduled');
        setScheduleTarget(null);
        fetchApplications();
      } catch (err: unknown) {
        const message =
          err instanceof Error ? err.message : MESSAGES.SAVE_ERROR('interview');
        toast.error(message);
      }
    },
    [scheduleTarget, fetchApplications],
  );

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
    <div className="mx-auto max-w-7xl">
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
                setStatusFilter(statusFilter === status ? null : status)
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

      {/* Filters + view switch */}
      <div className="mb-6">
        <div className="flex flex-col gap-3">
          <JobFilters
            searchQuery={searchQuery}
            onSearchChange={setSearchQuery}
            statusFilter={statusFilter}
            onStatusFilterChange={setStatusFilter}
            advancedFilters={advancedFilters}
            onAdvancedFiltersChange={setAdvancedFilters}
          />
          <div className="flex flex-wrap items-center justify-between gap-3">
            {/* View switch */}
            <div className="inline-flex items-center rounded-lg border border-gray-300 bg-white p-0.5">
              <button
                onClick={() => setViewMode('list')}
                className={`inline-flex items-center gap-1.5 rounded-md px-3 py-1.5 text-xs font-medium transition-colors ${
                  viewMode === 'list'
                    ? 'bg-indigo-600 text-white shadow-sm'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                <LayoutGrid className="h-3.5 w-3.5" />
                List View
              </button>
              <button
                onClick={() => setViewMode('board')}
                className={`inline-flex items-center gap-1.5 rounded-md px-3 py-1.5 text-xs font-medium transition-colors ${
                  viewMode === 'board'
                    ? 'bg-indigo-600 text-white shadow-sm'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                <Kanban className="h-3.5 w-3.5" />
                Board View
              </button>
            </div>

            <div className="flex flex-wrap items-center gap-2">
              <button
                onClick={toggleAnalytics}
                className={`inline-flex items-center gap-1.5 rounded-lg border px-3 py-1.5 text-xs font-medium transition-colors ${
                  showAnalytics
                    ? 'border-indigo-300 bg-indigo-50 text-indigo-700'
                    : 'border-gray-300 bg-white text-gray-600 hover:bg-gray-50'
                }`}
              >
                <BarChart3 className="h-3.5 w-3.5" />
                Analytics
              </button>

              {/* Sort controls (list view only) */}
              {viewMode === 'list' && (
                <div className="flex items-center gap-1">
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
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Analytics panel */}
      {showAnalytics && (
        <div className="mb-6">
          {isAnalyticsLoading || !analytics ? (
            <div className="h-64 animate-pulse rounded-xl border border-gray-200 bg-white" />
          ) : (
            <ApplicationAnalytics analytics={analytics} />
          )}
        </div>
      )}

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
              setAdvancedFilters(EMPTY_ADVANCED_FILTERS);
            }}
            className="mt-2 text-xs font-medium text-indigo-600 hover:text-indigo-800"
          >
            Clear all filters
          </button>
        </div>
      ) : viewMode === 'board' ? (
        /* ─── Board view ─── */
        <JobBoardView
          applications={filteredAndSorted}
          onStatusChange={handleStatusChange}
          onView={(id) => {
            const app = applications.find((a) => a.id === id);
            if (app) setDetailApp(app);
          }}
          onEdit={(id) => navigate(ROUTES.JOB_APPLICATION_EDIT(id))}
          onMoveStatus={setMoveTarget}
          onSchedule={setScheduleTarget}
          onNotes={setNotesTarget}
          onAttachments={setAttachmentsTarget}
          onDelete={setDeleteTarget}
        />
      ) : (
        /* ─── List view ─── */
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {filteredAndSorted.map((app) => (
            <JobApplicationCard
              key={app.id}
              application={app}
              onView={(id) => {
                const target = applications.find((a) => a.id === id);
                if (target) setDetailApp(target);
              }}
              onEdit={(id) => navigate(ROUTES.JOB_APPLICATION_EDIT(id))}
              onDelete={(id) => setDeleteTarget(id)}
              onMoveStatus={setMoveTarget}
              onSchedule={setScheduleTarget}
              onNotes={setNotesTarget}
              onAttachments={setAttachmentsTarget}
            />
          ))}
        </div>
      )}

      {/* Detail modal */}
      {detailApp && (
        <ApplicationDetailModal
          isOpen={detailApp !== null}
          onClose={() => setDetailApp(null)}
          application={detailApp}
          onEdit={(id) => navigate(ROUTES.JOB_APPLICATION_EDIT(id))}
        />
      )}

      {/* Move status modal */}
      {moveTarget && (
        <MoveStatusModal
          isOpen={moveTarget !== null}
          onClose={() => setMoveTarget(null)}
          currentStatus={moveTarget.status}
          isSubmitting={isStatusUpdating}
          onSubmit={(status) => handleStatusChange(moveTarget.id, status)}
        />
      )}

      {/* Schedule interview modal */}
      {scheduleTarget && (
        <ScheduleInterviewModal
          isOpen={scheduleTarget !== null}
          onClose={() => setScheduleTarget(null)}
          companyName={scheduleTarget.companyName}
          isSubmitting={false}
          onSubmit={handleScheduleSubmit}
        />
      )}

      {/* Notes modal */}
      {notesTarget && (
        <InterviewNotesModal
          isOpen={notesTarget !== null}
          onClose={() => setNotesTarget(null)}
          applicationId={notesTarget.id}
          onChange={() => {
            fetchApplications();
          }}
        />
      )}

      {/* Attachments modal */}
      {attachmentsTarget && (
        <AttachmentsModal
          isOpen={attachmentsTarget !== null}
          onClose={() => setAttachmentsTarget(null)}
          applicationId={attachmentsTarget.id}
          onChange={() => {
            fetchApplications();
          }}
        />
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
