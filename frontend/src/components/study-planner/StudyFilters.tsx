/**
 * StudyFilters — search bar, status filter, and priority filter for tasks.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Search } from 'lucide-react';
import {
  STUDY_PRIORITIES,
  STUDY_PRIORITY_LABELS,
  STUDY_STATUSES,
  STUDY_STATUS_LABELS,
  type StudyPriorityEnum,
  type StudyStatusEnum,
} from '../../types/study-planner';

interface StudyFiltersProps {
  searchQuery: string;
  onSearchChange: (query: string) => void;
  statusFilter: StudyStatusEnum | null;
  onStatusFilterChange: (status: StudyStatusEnum | null) => void;
  priorityFilter: StudyPriorityEnum | null;
  onPriorityFilterChange: (priority: StudyPriorityEnum | null) => void;
}

export const StudyFilters: React.FC<StudyFiltersProps> = ({
  searchQuery,
  onSearchChange,
  statusFilter,
  onStatusFilterChange,
  priorityFilter,
  onPriorityFilterChange,
}) => {
  return (
    <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
      {/* Search */}
      <div className="relative flex-1">
        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
        <input
          type="text"
          placeholder="Search by title or description…"
          value={searchQuery}
          onChange={(e) => onSearchChange(e.target.value)}
          className="w-full rounded-lg border border-gray-300 bg-white py-2 pl-10 pr-3 text-sm text-gray-900 placeholder-gray-400 transition-colors focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
        />
      </div>

      {/* Status filter */}
      <select
        value={statusFilter ?? ''}
        onChange={(e) => {
          const val = e.target.value;
          onStatusFilterChange(val === '' ? null : (val as StudyStatusEnum));
        }}
        className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 transition-colors focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500 sm:w-40"
      >
        <option value="">All Statuses</option>
        {STUDY_STATUSES.map((s) => (
          <option key={s} value={s}>{STUDY_STATUS_LABELS[s]}</option>
        ))}
      </select>

      {/* Priority filter */}
      <select
        value={priorityFilter ?? ''}
        onChange={(e) => {
          const val = e.target.value;
          onPriorityFilterChange(val === '' ? null : (val as StudyPriorityEnum));
        }}
        className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 transition-colors focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500 sm:w-40"
      >
        <option value="">All Priorities</option>
        {STUDY_PRIORITIES.map((p) => (
          <option key={p} value={p}>{STUDY_PRIORITY_LABELS[p]}</option>
        ))}
      </select>
    </div>
  );
};
