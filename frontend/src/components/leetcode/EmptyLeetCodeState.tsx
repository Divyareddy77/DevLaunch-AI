/**
 * EmptyLeetCodeState — a friendly empty state used on the LeetCode
 * Tracker page.
 *
 * Shown when no username has been searched yet or when a searched
 * user could not be found.
 *
 * @author DevLaunch
 */

import React, { type ReactNode } from 'react';
import { Code2 } from 'lucide-react';

interface EmptyLeetCodeStateProps {
  /** Heading displayed in the empty state. */
  title?: string;
  /** Supporting description below the heading. */
  description?: string;
  /** Optional custom icon rendered in the circle. Defaults to the Code2 icon. */
  icon?: ReactNode;
  /** Optional action (e.g. a button) rendered below the description. */
  action?: ReactNode;
  /** Optional additional CSS classes. */
  className?: string;
}

const DEFAULT_TITLE = 'Track LeetCode Progress';
const DEFAULT_DESCRIPTION =
  'Enter a LeetCode username to view their solved problems, difficulty breakdown, and global ranking.';

export const EmptyLeetCodeState: React.FC<EmptyLeetCodeStateProps> = ({
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
      <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-amber-100">
        {icon ?? <Code2 className="h-8 w-8 text-amber-500" />}
      </div>
      <h2 className="mb-2 text-xl font-semibold text-gray-900">{title}</h2>
      <p className="mb-6 max-w-sm text-sm text-gray-500">{description}</p>
      {action}
    </div>
  );
};
