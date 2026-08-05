/**
 * AdminPagination — a compact server-side pagination control.
 *
 * Renders previous/next buttons with a "Page X of Y · N items" summary
 * for the admin list pages backed by PagedResponse.
 *
 * @author DevLaunch
 */

import React from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { formatNumber } from '../../utils/format';

interface AdminPaginationProps {
  /** The current zero-based page number. */
  page: number;
  /** The total number of pages. */
  totalPages: number;
  /** The total number of items across all pages. */
  totalElements: number;
  /** Callback with the new zero-based page number. */
  onPageChange: (page: number) => void;
}

export const AdminPagination: React.FC<AdminPaginationProps> = ({
  page,
  totalPages,
  totalElements,
  onPageChange,
}) => {
  if (totalPages <= 1) {
    return (
      <p className="text-xs text-gray-400">
        {formatNumber(totalElements)} {totalElements === 1 ? 'item' : 'items'}
      </p>
    );
  }

  const hasPrevious = page > 0;
  const hasNext = page < totalPages - 1;

  return (
    <div className="flex flex-col items-center justify-between gap-3 sm:flex-row">
      <p className="text-xs text-gray-400">
        Page {page + 1} of {totalPages} · {formatNumber(totalElements)}{' '}
        {totalElements === 1 ? 'item' : 'items'}
      </p>
      <div className="flex items-center gap-2">
        <button
          onClick={() => onPageChange(page - 1)}
          disabled={!hasPrevious}
          className="inline-flex items-center gap-1 rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-xs font-medium text-gray-700 transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-40"
        >
          <ChevronLeft className="h-3.5 w-3.5" />
          Previous
        </button>
        <button
          onClick={() => onPageChange(page + 1)}
          disabled={!hasNext}
          className="inline-flex items-center gap-1 rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-xs font-medium text-gray-700 transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-40"
        >
          Next
          <ChevronRight className="h-3.5 w-3.5" />
        </button>
      </div>
    </div>
  );
};
