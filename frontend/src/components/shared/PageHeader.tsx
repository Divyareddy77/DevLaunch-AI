/**
 * PageHeader — a consistent header for every module page.
 *
 * Renders a "Back to Dashboard" link above the page title (navigating to
 * the dashboard while preserving normal browser back behaviour), the page
 * title with an optional badge, a short description, and an optional
 * actions slot for page-level buttons.
 *
 * @author DevLaunch
 */

import React, { type ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { ROUTES } from '../../constants/routes';

interface PageHeaderProps {
  /** The page title. */
  title: string;
  /** Short page description shown under the title. */
  description?: string;
  /** Optional badge rendered inline next to the title. */
  badge?: ReactNode;
  /** Optional action controls rendered on the right (buttons etc.). */
  actions?: ReactNode;
  /** Additional CSS classes for the header row. */
  className?: string;
}

export const PageHeader: React.FC<PageHeaderProps> = ({
  title,
  description,
  badge,
  actions,
  className = '',
}) => {
  return (
    <header className={`mb-6 ${className}`}>
      {/* Back to Dashboard link */}
      <Link
        to={ROUTES.DASHBOARD}
        className="group mb-3 inline-flex items-center gap-1.5 text-xs font-medium text-gray-400 transition-colors hover:text-primary-600"
      >
        <ArrowLeft className="h-3.5 w-3.5 transition-transform duration-200 group-hover:-translate-x-0.5" />
        Back to Dashboard
      </Link>

      <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div className="min-w-0">
          <div className="flex flex-wrap items-center gap-2.5">
            <h1 className="text-xl font-bold tracking-tight text-gray-900 sm:text-2xl">
              {title}
            </h1>
            {badge}
          </div>
          {description && (
            <p className="mt-1 max-w-2xl text-sm leading-relaxed text-gray-500">
              {description}
            </p>
          )}
        </div>

        {actions && (
          <div className="flex flex-shrink-0 flex-wrap items-center gap-2">
            {actions}
          </div>
        )}
      </div>
    </header>
  );
};
