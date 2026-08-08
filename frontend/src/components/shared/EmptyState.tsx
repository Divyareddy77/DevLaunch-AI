/**
 * EmptyState — a consistent, premium empty-state block used across the
 * dashboard widgets.
 *
 * Renders a soft gradient icon "illustration", a title, an optional
 * description, and an optional action button so empty states guide the
 * user forward instead of showing plain text.
 *
 * @author DevLaunch
 */

import React, { type ReactNode } from 'react';
import { ArrowRight, type LucideIcon } from 'lucide-react';
import { CARD_TONES, type CardTone } from '../dashboard/cardTones';

interface EmptyStateProps {
  /** The icon used as the illustration. */
  icon: LucideIcon;
  /** Semantic tone controlling the gradient illustration + accent. */
  tone?: CardTone;
  /** Short headline. */
  title: string;
  /** Optional supporting message. */
  description?: string;
  /** Optional action button label. */
  actionLabel?: string;
  /** Optional action callback. */
  onAction?: () => void;
  /** Optional extra content below the description (e.g. a hint chip). */
  hint?: ReactNode;
  /** Compact mode — tighter padding and smaller illustration for use inside cards that already have other content. */
  compact?: boolean;
  /** Additional CSS classes. */
  className?: string;
}

export const EmptyState: React.FC<EmptyStateProps> = ({
  icon: Icon,
  tone = 'gray',
  title,
  description,
  actionLabel,
  onAction,
  hint,
  compact = false,
  className = '',
}) => {
  const t = CARD_TONES[tone];

  return (
    <div
      className={`flex flex-col items-center text-center ${compact ? 'px-3 py-4' : 'px-4 py-7'} ${className}`}
      role="status"
    >
      {/* Gradient icon illustration */}
      <div className={`relative ${compact ? 'mb-2' : 'mb-3'}`}>
        <span
          className={`absolute inset-0 rounded-2xl opacity-60 blur-md ${t.soft}`}
          aria-hidden="true"
        />
        <span
          className={`relative flex items-center justify-center rounded-2xl border border-gray-100 bg-gradient-to-br from-white to-gray-50 shadow-sm ${t.text} ${
            compact ? 'h-10 w-10' : 'h-14 w-14'
          }`}
        >
          <Icon className={compact ? 'h-5 w-5' : 'h-6 w-6'} />
        </span>
      </div>

      <p className="text-sm font-semibold text-gray-800">{title}</p>
      {description && (
        <p className="mt-1 max-w-[240px] text-xs leading-relaxed text-gray-400">
          {description}
        </p>
      )}

      {actionLabel && onAction && (
        <button
          type="button"
          onClick={onAction}
          className={`inline-flex items-center gap-1.5 rounded-lg px-3.5 py-2 text-xs font-medium shadow-sm transition-all duration-200 hover:-translate-y-0.5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 focus-visible:ring-offset-2 ${t.chip} ${
            compact ? 'mt-3' : 'mt-4'
          }`}
        >
          {actionLabel}
          <ArrowRight className="h-3.5 w-3.5" />
        </button>
      )}

      {hint && <div className="mt-3">{hint}</div>}
    </div>
  );
};
