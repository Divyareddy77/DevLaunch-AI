/**
 * EmptyReviewState — empty state for the AI Resume Review page,
 * shown before a review has been run or when the user has no resumes.
 *
 * @author DevLaunch
 */

import React from 'react';
import { type LucideIcon } from 'lucide-react';
import { Button } from '../ui/Button';

interface EmptyReviewStateProps {
  /** The icon to display in the coloured circle. */
  icon: LucideIcon;
  /** Background/text classes for the icon circle. */
  iconClassName: string;
  /** The primary empty-state title. */
  title: string;
  /** Supporting description text. */
  description: string;
  /** Optional label for the call-to-action button. */
  actionLabel?: string;
  /** Optional callback invoked when the action button is clicked. */
  onAction?: () => void;
  /** Optional additional CSS classes. */
  className?: string;
}

export const EmptyReviewState: React.FC<EmptyReviewStateProps> = ({
  icon: Icon,
  iconClassName,
  title,
  description,
  actionLabel,
  onAction,
  className = '',
}) => {
  return (
    <div
      className={`flex flex-col items-center justify-center rounded-xl border border-dashed border-gray-300 bg-white px-6 py-12 text-center ${className}`}
    >
      <div
        className={`mb-4 flex h-14 w-14 items-center justify-center rounded-2xl ${iconClassName}`}
      >
        <Icon className="h-7 w-7" />
      </div>
      <h3 className="mb-1.5 text-lg font-semibold text-gray-900">{title}</h3>
      <p className="mb-5 max-w-md text-sm text-gray-500">{description}</p>
      {actionLabel && onAction && (
        <Button variant="primary" size="sm" onClick={onAction}>
          {actionLabel}
        </Button>
      )}
    </div>
  );
};
