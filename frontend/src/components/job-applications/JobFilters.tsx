/**
 * JobFilters — search bar and status filter for the job applications list.
 *
 * Provides text search (company name / job role) and status dropdown
 * filtering in a responsive layout.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Search } from 'lucide-react';
import {
  APPLICATION_STATUSES,
  APPLICATION_STATUS_LABELS,
  type ApplicationStatusEnum,
} from '../../types/job-application';

interface JobFiltersProps {
  /** Current search query string. */
  searchQuery: string;
  /** Callback when the search query changes. */
  onSearchChange: (query: string) => void;
  /** Currently selected status filter, or null for "all". */
  statusFilter: ApplicationStatusEnum | null;
  /** Callback when the status filter changes. */
  onStatusFilterChange: (status: ApplicationStatusEnum | null) => void;
}

export const JobFilters: React.FC<JobFiltersProps> = ({
  searchQuery,
  onSearchChange,
  statusFilter,
  onStatusFilterChange,
}) => {
  return (
    <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
      {/* Search */}
      <div className="relative flex-1">
        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
        <input
          type="text"
          placeholder="Search by company or role…"
          value={searchQuery}
          onChange={(e) => onSearchChange(e.target.value)}
          className="w-full rounded-lg border border-gray-300 bg-white py-2 pl-10 pr-3 text-sm text-gray-900 placeholder-gray-400 transition-colors focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
        />
      </div>

      {/* Status filter */}
      <div className="flex-shrink-0">
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
      </div>
    </div>
  );
};
