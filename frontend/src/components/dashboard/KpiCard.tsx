/**
 * KpiCard — a modern quick-statistics card for the dashboard.
 *
 * Shows a large animated value, an icon chip, a label, a small subtitle,
 * and an optional progress indicator. The whole card is clickable and
 * navigates to the corresponding module. Hover lifts the card and reveals
 * a coloured top accent for a premium SaaS feel.
 *
 * @author DevLaunch
 */

import React, { type ReactNode } from 'react';
import { ArrowUpRight } from 'lucide-react';
import { CARD_TONES, type CardTone } from './cardTones';

interface KpiCardProps {
  /** Card label shown under the value. */
  label: string;
  /** Large value (number, CountUp, or a placeholder like '—'). */
  value: ReactNode;
  /** Module icon rendered in the top chip. */
  icon: ReactNode;
  /** Semantic tone controlling chip, bar, and accent colours. */
  tone?: CardTone;
  /** Small supporting text below the label. */
  subtitle?: ReactNode;
  /** 0–100 progress indicator; omit to hide the bar. */
  progress?: number | null;
  /** Tone for the progress bar; defaults to `tone`. */
  barTone?: CardTone;
  /** Navigates to the module page when the card is clicked. */
  onClick?: () => void;
  /** Optional additional CSS classes. */
  className?: string;
}

export const KpiCard: React.FC<KpiCardProps> = ({
  label,
  value,
  icon,
  tone = 'primary',
  subtitle,
  progress,
  barTone,
  onClick,
  className = '',
}) => {
  const t = CARD_TONES[tone];
  const bar = CARD_TONES[barTone ?? tone];
  const clamped =
    progress === null || progress === undefined
      ? null
      : Math.min(100, Math.max(0, progress));

  return (
    <button
      type="button"
      onClick={onClick}
      className={`group relative flex w-full flex-col overflow-hidden rounded-2xl border border-gray-100 bg-white p-4 text-left shadow-[0_1px_3px_rgba(16,24,40,0.06)] transition-all duration-300 hover:-translate-y-1 hover:border-gray-200 hover:shadow-[0_16px_40px_-12px_rgba(16,24,40,0.18)] sm:p-5 ${className}`}
    >
      {/* Coloured top accent revealed on hover */}
      <span
        aria-hidden="true"
        className={`pointer-events-none absolute inset-x-0 top-0 h-1 origin-left scale-x-0 transition-transform duration-300 ease-out group-hover:scale-x-100 ${bar.bar}`}
      />

      {/* Icon + external link affordance */}
      <span className="flex items-start justify-between">
        <span
          className={`flex h-10 w-10 items-center justify-center rounded-xl transition-transform duration-300 group-hover:scale-105 ${t.chip}`}
        >
          {icon}
        </span>
        <ArrowUpRight
          aria-hidden="true"
          className="h-4 w-4 text-gray-300 transition-all duration-300 group-hover:-translate-y-0.5 group-hover:translate-x-0.5 group-hover:text-gray-500"
        />
      </span>

      {/* Value */}
      <span className="mt-3 block text-2xl font-bold tracking-tight text-gray-900 sm:text-3xl">
        {value}
      </span>

      {/* Label */}
      <span className="mt-0.5 block text-sm font-semibold text-gray-800">{label}</span>

      {/* Subtitle */}
      {subtitle && (
        <span className="mt-1 block line-clamp-2 text-xs leading-relaxed text-gray-400">
          {subtitle}
        </span>
      )}

      {/* Progress indicator */}
      {clamped !== null && (
        <span className="mt-auto block pt-3">
          <span className="block h-1.5 w-full overflow-hidden rounded-full bg-gray-100">
            <span
              className={`block h-full rounded-full transition-[width] duration-1000 ease-out ${bar.bar}`}
              style={{ width: `${clamped}%` }}
            />
          </span>
        </span>
      )}
    </button>
  );
};
