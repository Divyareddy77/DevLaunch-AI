/**
 * JobFilters — search bar, status filter, and advanced filters for the
 * job applications list.
 *
 * The keyword search covers company, role, location, notes, recruiter, and
 * technology. The status dropdown matches the existing list view, and the
 * collapsible "More filters" panel adds company, role, location, salary,
 * work mode, priority, application date, and interview date filtering.
 *
 * @author DevLaunch
 */

import React, { useState } from 'react';
import { Search, SlidersHorizontal, X } from 'lucide-react';
import {
  APPLICATION_PRIORITIES,
  APPLICATION_PRIORITY_LABELS,
  APPLICATION_STATUSES,
  APPLICATION_STATUS_LABELS,
  WORK_MODES,
  WORK_MODE_LABELS,
  type ApplicationPriorityEnum,
  type ApplicationStatusEnum,
  type WorkModeEnum,
} from '../../types/job-application';

/** The advanced (non-status, non-keyword) filter values. */
export interface AdvancedJobFilters {
  company: string;
  role: string;
  location: string;
  salary: string;
  workMode: WorkModeEnum | '';
  priority: ApplicationPriorityEnum | '';
  applicationDateFrom: string;
  applicationDateTo: string;
  interviewDateFrom: string;
  interviewDateTo: string;
}

/** A blank advanced filter set. */
export const EMPTY_ADVANCED_FILTERS: AdvancedJobFilters = {
  company: '',
  role: '',
  location: '',
  salary: '',
  workMode: '',
  priority: '',
  applicationDateFrom: '',
  applicationDateTo: '',
  interviewDateFrom: '',
  interviewDateTo: '',
};

/** Counts how many advanced filters are actively set. */
export function activeAdvancedFilterCount(filters: AdvancedJobFilters): number {
  return Object.values(filters).filter((value) => value !== '').length;
}

interface JobFiltersProps {
  /** Current search query string. */
  searchQuery: string;
  /** Callback when the search query changes. */
  onSearchChange: (query: string) => void;
  /** Currently selected status filter, or null for "all". */
  statusFilter: ApplicationStatusEnum | null;
  /** Callback when the status filter changes. */
  onStatusFilterChange: (status: ApplicationStatusEnum | null) => void;
  /** Current advanced filter values. */
  advancedFilters?: AdvancedJobFilters;
  /** Callback when any advanced filter changes. */
  onAdvancedFiltersChange?: (filters: AdvancedJobFilters) => void;
  /** Clears every active filter. */
  onClearFilters?: () => void;
}

const inputClass =
  'w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 transition-colors focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500';

export const JobFilters: React.FC<JobFiltersProps> = ({
  searchQuery,
  onSearchChange,
  statusFilter,
  onStatusFilterChange,
  advancedFilters,
  onAdvancedFiltersChange,
  onClearFilters,
}) => {
  const [showAdvanced, setShowAdvanced] = useState(false);
  const activeCount = advancedFilters ? activeAdvancedFilterCount(advancedFilters) : 0;

  const update = (patch: Partial<AdvancedJobFilters>) => {
    onAdvancedFiltersChange?.({ ...(advancedFilters ?? EMPTY_ADVANCED_FILTERS), ...patch });
  };

  return (
    <div className="flex flex-col gap-3">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
        {/* Search */}
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="Search company, role, location, notes, recruiter, tech…"
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            className="w-full rounded-lg border border-gray-300 bg-white py-2 pl-10 pr-3 text-sm text-gray-900 placeholder-gray-400 transition-colors focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
          />
        </div>

        {/* Status filter */}
        <div className="flex flex-shrink-0 items-center gap-2">
          <select
            value={statusFilter ?? ''}
            onChange={(e) => {
              const val = e.target.value;
              onStatusFilterChange(val === '' ? null : (val as ApplicationStatusEnum));
            }}
            className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 transition-colors focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500 sm:w-44"
          >
            <option value="">All Statuses</option>
            {APPLICATION_STATUSES.map((status) => (
              <option key={status} value={status}>
                {APPLICATION_STATUS_LABELS[status]}
              </option>
            ))}
          </select>

          <button
            onClick={() => setShowAdvanced((show) => !show)}
            className={`inline-flex items-center gap-1.5 rounded-lg border px-3 py-2 text-sm font-medium transition-colors ${
              showAdvanced || activeCount > 0
                ? 'border-indigo-300 bg-indigo-50 text-indigo-700'
                : 'border-gray-300 bg-white text-gray-600 hover:bg-gray-50'
            }`}
            aria-expanded={showAdvanced}
          >
            <SlidersHorizontal className="h-4 w-4" />
            Filters
            {activeCount > 0 && (
              <span className="rounded-full bg-indigo-600 px-1.5 py-0.5 text-[10px] font-semibold text-white">
                {activeCount}
              </span>
            )}
          </button>
        </div>
      </div>

      {/* Advanced filters panel */}
      {showAdvanced && (
        <div className="rounded-xl border border-gray-200 bg-gray-50/60 p-4">
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
            <input
              type="text"
              placeholder="Company"
              value={advancedFilters?.company ?? ''}
              onChange={(e) => update({ company: e.target.value })}
              className={inputClass}
            />
            <input
              type="text"
              placeholder="Role"
              value={advancedFilters?.role ?? ''}
              onChange={(e) => update({ role: e.target.value })}
              className={inputClass}
            />
            <input
              type="text"
              placeholder="Location"
              value={advancedFilters?.location ?? ''}
              onChange={(e) => update({ location: e.target.value })}
              className={inputClass}
            />
            <input
              type="text"
              placeholder="Salary (e.g. 80,000)"
              value={advancedFilters?.salary ?? ''}
              onChange={(e) => update({ salary: e.target.value })}
              className={inputClass}
            />
            <select
              value={advancedFilters?.workMode ?? ''}
              onChange={(e) => update({ workMode: e.target.value as WorkModeEnum | '' })}
              className={inputClass}
            >
              <option value="">All Work Modes</option>
              {WORK_MODES.map((mode) => (
                <option key={mode} value={mode}>
                  {WORK_MODE_LABELS[mode]}
                </option>
              ))}
            </select>
            <select
              value={advancedFilters?.priority ?? ''}
              onChange={(e) =>
                update({ priority: e.target.value as ApplicationPriorityEnum | '' })
              }
              className={inputClass}
            >
              <option value="">All Priorities</option>
              {APPLICATION_PRIORITIES.map((priority) => (
                <option key={priority} value={priority}>
                  {APPLICATION_PRIORITY_LABELS[priority]}
                </option>
              ))}
            </select>
            <div>
              <label className="mb-1 block text-[11px] font-medium text-gray-500">
                Applied From
              </label>
              <input
                type="date"
                value={advancedFilters?.applicationDateFrom ?? ''}
                onChange={(e) => update({ applicationDateFrom: e.target.value })}
                className={inputClass}
              />
            </div>
            <div>
              <label className="mb-1 block text-[11px] font-medium text-gray-500">
                Applied To
              </label>
              <input
                type="date"
                value={advancedFilters?.applicationDateTo ?? ''}
                onChange={(e) => update({ applicationDateTo: e.target.value })}
                className={inputClass}
              />
            </div>
            <div>
              <label className="mb-1 block text-[11px] font-medium text-gray-500">
                Interview From
              </label>
              <input
                type="date"
                value={advancedFilters?.interviewDateFrom ?? ''}
                onChange={(e) => update({ interviewDateFrom: e.target.value })}
                className={inputClass}
              />
            </div>
            <div>
              <label className="mb-1 block text-[11px] font-medium text-gray-500">
                Interview To
              </label>
              <input
                type="date"
                value={advancedFilters?.interviewDateTo ?? ''}
                onChange={(e) => update({ interviewDateTo: e.target.value })}
                className={inputClass}
              />
            </div>
          </div>

          <div className="mt-3 flex justify-end">
            <button
              onClick={() => {
                onAdvancedFiltersChange?.(EMPTY_ADVANCED_FILTERS);
                onSearchChange('');
                onStatusFilterChange(null);
                onClearFilters?.();
              }}
              className="inline-flex items-center gap-1 text-xs font-medium text-gray-500 transition-colors hover:text-gray-700"
            >
              <X className="h-3.5 w-3.5" />
              Clear all filters
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
