/**
 * EmptyGitHubState — a friendly empty state used on the GitHub
 * Analytics page.
 *
 * Shown when no username has been searched yet or when a searched
 * user has no public repositories to display.
 *
 * @author DevLaunch
 */

import React, { type ReactNode } from 'react';
import { Github } from 'lucide-react';

interface EmptyGitHubStateProps {
  /** Heading displayed in the empty state. */
  title?: string;
  /** Supporting description below the heading. */
  description?: string;
  /** Optional custom icon rendered in the circle. Defaults to the GitHub icon. */
  icon?: ReactNode;
  /** Optional action (e.g. a button) rendered below the description. */
  action?: ReactNode;
  /** Optional additional CSS classes. */
  className?: string;
}

const DEFAULT_TITLE = 'Search GitHub Analytics';
const DEFAULT_DESCRIPTION =
  'Enter a GitHub username to explore their public profile, repositories, and language statistics.';

export const EmptyGitHubState: React.FC<EmptyGitHubStateProps> = ({
  title = DEFAULT_TITLE,
  description = DEFAULT_DESCRIPTION,
  icon,
  action,
  className = '',
}) => {
  return (
    <div
      className={`flex flex-col items-center justify-center rounded-2xl border-2 border-dashed border-gray-200 bg-white px-6 py-16 text-center ${className}`}
    >
      <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-gray-100">
        {icon ?? <Github className="h-8 w-8 text-gray-400" />}
      </div>
      <h2 className="mb-2 text-xl font-semibold text-gray-900">{title}</h2>
      <p className="mb-6 max-w-sm text-sm text-gray-500">{description}</p>
      {action}
    </div>
  );
};
