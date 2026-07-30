/**
 * StudyPlannerPage — the main study planner page.
 *
 * Combines a monthly calendar view with a filterable, searchable,
 * sortable list of study tasks. Supports CRUD operations with
 * loading, error, empty, and filtered-empty states.
 *
 * @author DevLaunch
 */

import React, { useEffect, useState, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, BookOpen, RefreshCw, ArrowUpDown } from 'lucide-react';
import toast from 'react-hot-toast';
import { studyPlannerService } from '../../services/study-planner.service';
import { StudyPlannerCard } from '../../components/study-planner/StudyPlannerCard';
import { StudyFilters } from '../../components/study-planner/StudyFilters';
import { CalendarView } from '../../components/study-planner/CalendarView';
import { Button } from '../../components/ui/Button';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { Modal } from '../../components/ui/Modal';
import { ROUTES } from '../../constants/routes';
import { MESSAGES } from '../../constants/messages';
import type {
  StudyPlannerResponse,
  StudyPriorityEnum,
  StudyStatusEnum,
} from '../../types/study-planner';

type SortField = 'studyDate' | 'priority' | 'title';
type SortDir = 'asc' | 'desc';

export const StudyPlannerPage: React.FC = () => {
  const navigate = useNavigate();

  // Data state
  const [tasks, setTasks] = useState<StudyPlannerResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Filter state
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<StudyStatusEnum | null>(null);
  const [priorityFilter, setPriorityFilter] = useState<StudyPriorityEnum | null>(null);

  // Calendar state
  const now = new Date();
  const [currentMonth, setCurrentMonth] = useState(now.getMonth());
  const [currentYear, setCurrentYear] = useState(now.getFullYear());
  const [selectedDate, setSelectedDate] = useState<string | null>(null);

  // Sort state
  const [sortField, setSortField] = useState<SortField>('studyDate');
  const [sortDir, setSortDir] = useState<SortDir>('desc');

  // Delete state
  const [deleteTarget, setDeleteTarget] = useState<number | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const priorityOrder: Record<StudyPriorityEnum, number> = {
    HIGH: 3,
    MEDIUM: 2,
    LOW: 1,
  };

  // ─── Data fetching ───
  const fetchTasks = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await studyPlannerService.getAll();
      setTasks(data);
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : MESSAGES.LOAD_ERROR('study tasks');
      setError(message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchTasks();
  }, [fetchTasks]);

  // ─── Task dates for calendar ───
  const taskDates = useMemo(() => tasks.map((t) => t.studyDate), [tasks]);

  // ─── Filtering & sorting ───
  const filteredAndSorted = useMemo(() => {
    let result = [...tasks];

    // Status filter
    if (statusFilter) {
      result = result.filter((t) => t.status === statusFilter);
    }

    // Priority filter
    if (priorityFilter) {
      result = result.filter((t) => t.priority === priorityFilter);
    }

    // Date filter (from calendar)
    if (selectedDate) {
      result = result.filter((t) => t.studyDate === selectedDate);
    }

    // Search filter
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase().trim();
      result = result.filter(
        (t) =>
          t.title.toLowerCase().includes(q) ||
          (t.description ?? '').toLowerCase().includes(q),
      );
    }

    // Sort
    result.sort((a, b) => {
      let cmp: number;
      if (sortField === 'title') {
        cmp = a.title.localeCompare(b.title);
      } else if (sortField === 'priority') {
        cmp = priorityOrder[a.priority] - priorityOrder[b.priority];
      } else {
        // studyDate
        cmp = a.studyDate.localeCompare(b.studyDate);
      }
      return sortDir === 'asc' ? cmp : -cmp;
    });

    return result;
  }, [tasks, statusFilter, priorityFilter, selectedDate, searchQuery, sortField, sortDir]);

  // ─── Delete ───
  const handleDelete = async () => {
    if (deleteTarget === null) return;
    setIsDeleting(true);
    try {
      await studyPlannerService.delete(deleteTarget);
      toast.success(MESSAGES.DELETE_SUCCESS('Study task'));
      setTasks((prev) => prev.filter((t) => t.id !== deleteTarget));
      setDeleteTarget(null);
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : MESSAGES.SAVE_ERROR('study task');
      toast.error(message);
    } finally {
      setIsDeleting(false);
    }
  };

  const toggleSort = (field: SortField) => {
    if (sortField === field) {
      setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortField(field);
      setSortDir('desc');
    }
  };

  // ─── Calendar navigation ───
  const handlePrevMonth = () => {
    if (currentMonth === 0) {
      setCurrentMonth(11);
      setCurrentYear((y) => y - 1);
    } else {
      setCurrentMonth((m) => m - 1);
    }
  };

  const handleNextMonth = () => {
    if (currentMonth === 11) {
      setCurrentMonth(0);
      setCurrentYear((y) => y + 1);
    } else {
      setCurrentMonth((m) => m + 1);
    }
  };

  // ─── Loading state ───
  if (isLoading) {
    return <LoadingScreen />;
  }

  // ─── Error state ───
  if (error) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh]">
        <ErrorMessage message={error} onRetry={fetchTasks} />
      </div>
    );
  }

  // ─── Empty state ───
  if (tasks.length === 0) {
    return (
      <div className="mx-auto max-w-3xl">
        <div className="flex flex-col items-center justify-center rounded-2xl border-2 border-dashed border-gray-200 bg-white px-6 py-16 text-center">
          <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-emerald-100">
            <BookOpen className="h-8 w-8 text-emerald-600" />
          </div>
          <h2 className="mb-2 text-xl font-semibold text-gray-900">
            No Study Tasks Yet
          </h2>
          <p className="mb-6 max-w-sm text-sm text-gray-500">
            {MESSAGES.NO_STUDY_PLANS}
          </p>
          <Button onClick={() => navigate(ROUTES.STUDY_PLANNER_CREATE)}>
            <Plus className="h-4 w-4" />
            Plan Your First Study Session
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
          <h1 className="text-xl font-bold text-gray-900">Study Planner</h1>
          <p className="mt-1 text-sm text-gray-500">
            Plan and track your study sessions. You have {tasks.length}{' '}
            {tasks.length === 1 ? 'task' : 'tasks'}.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={fetchTasks}>
            <RefreshCw className="h-4 w-4" />
            Refresh
          </Button>
          <Button onClick={() => navigate(ROUTES.STUDY_PLANNER_CREATE)}>
            <Plus className="h-4 w-4" />
            Add Task
          </Button>
        </div>
      </div>

      {/* Layout: calendar sidebar + task list */}
      <div className="flex flex-col gap-6 lg:flex-row">
        {/* Calendar */}
        <div className="w-full lg:w-80 lg:flex-shrink-0">
          <CalendarView
            taskDates={taskDates}
            selectedDate={selectedDate}
            onDateSelect={setSelectedDate}
            currentMonth={currentMonth}
            currentYear={currentYear}
            onPrevMonth={handlePrevMonth}
            onNextMonth={handleNextMonth}
          />

          {/* Filter summary when a date is selected */}
          {selectedDate && (
            <div className="mt-3 text-center">
              <p className="text-xs text-gray-500">
                Showing tasks for{' '}
                <span className="font-medium text-gray-700">{selectedDate}</span>
              </p>
              <button
                onClick={() => setSelectedDate(null)}
                className="mt-1 text-xs font-medium text-indigo-600 hover:text-indigo-800 transition-colors"
              >
                Show all dates
              </button>
            </div>
          )}
        </div>

        {/* Task list */}
        <div className="flex-1 min-w-0">
          {/* Filters */}
          <div className="mb-4">
            <StudyFilters
              searchQuery={searchQuery}
              onSearchChange={setSearchQuery}
              statusFilter={statusFilter}
              onStatusFilterChange={setStatusFilter}
              priorityFilter={priorityFilter}
              onPriorityFilterChange={setPriorityFilter}
            />
          </div>

          {/* Sort controls */}
          <div className="mb-4 flex items-center gap-2">
            <span className="text-xs text-gray-400">Sort by:</span>
            {(['studyDate', 'priority', 'title'] as SortField[]).map((field) => (
              <button
                key={field}
                onClick={() => toggleSort(field)}
                className={`inline-flex items-center gap-1 rounded-lg px-2.5 py-1.5 text-xs font-medium transition-colors ${
                  sortField === field
                    ? 'bg-emerald-100 text-emerald-700'
                    : 'text-gray-500 hover:bg-gray-100'
                }`}
              >
                <ArrowUpDown className="h-3 w-3" />
                {field === 'studyDate' ? 'Date' : field === 'priority' ? 'Priority' : 'Title'}
                {sortField === field && (
                  <span className="text-[10px]">{sortDir === 'asc' ? '↑' : '↓'}</span>
                )}
              </button>
            ))}
          </div>

          {/* Task grid or filtered-empty */}
          {filteredAndSorted.length === 0 ? (
            <div className="flex flex-col items-center justify-center rounded-xl border border-gray-200 bg-white py-12 text-center">
              <BookOpen className="mb-3 h-10 w-10 text-gray-300" />
              <p className="text-sm font-medium text-gray-500">
                No tasks match your criteria.
              </p>
              <button
                onClick={() => {
                  setSearchQuery('');
                  setStatusFilter(null);
                  setPriorityFilter(null);
                  setSelectedDate(null);
                }}
                className="mt-2 text-xs font-medium text-indigo-600 hover:text-indigo-800"
              >
                Clear all filters
              </button>
            </div>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
              {filteredAndSorted.map((task) => (
                <StudyPlannerCard
                  key={task.id}
                  task={task}
                  onEdit={(id) => navigate(ROUTES.STUDY_PLANNER_EDIT(id))}
                  onDelete={(id) => setDeleteTarget(id)}
                />
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Delete confirmation modal */}
      <Modal
        isOpen={deleteTarget !== null}
        onClose={() => setDeleteTarget(null)}
        title="Delete Study Task"
        closeOnBackdrop={false}
      >
        <p className="text-sm text-gray-600">
          {MESSAGES.DELETE_CONFIRM('study task')}
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
