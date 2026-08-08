/**
 * Shared colour tone system for dashboard cards.
 *
 * Maps a semantic tone (AI purple, success green, GitHub blue, job orange,
 * LeetCode red, achievement gold, ...) to Tailwind classes used for icon
 * chips, progress bars, and accent text. Keeps colour usage consistent
 * across the redesigned dashboard.
 *
 * @author DevLaunch
 */

export type CardTone =
  | 'primary'
  | 'violet'
  | 'success'
  | 'warning'
  | 'danger'
  | 'info'
  | 'orange'
  | 'gold'
  | 'gray';

export interface ToneStyles {
  /** Icon chip background + icon colour. */
  chip: string;
  /** Progress bar fill colour. */
  bar: string;
  /** Accent text colour. */
  text: string;
  /** Soft tinted background. */
  soft: string;
}

export const CARD_TONES: Record<CardTone, ToneStyles> = {
  primary: {
    chip: 'bg-primary-50 text-primary-600',
    bar: 'bg-primary-500',
    text: 'text-primary-600',
    soft: 'bg-primary-50',
  },
  violet: {
    chip: 'bg-violet-50 text-violet-600',
    bar: 'bg-violet-500',
    text: 'text-violet-600',
    soft: 'bg-violet-50',
  },
  success: {
    chip: 'bg-emerald-50 text-emerald-600',
    bar: 'bg-emerald-500',
    text: 'text-emerald-600',
    soft: 'bg-emerald-50',
  },
  warning: {
    chip: 'bg-amber-50 text-amber-600',
    bar: 'bg-amber-500',
    text: 'text-amber-600',
    soft: 'bg-amber-50',
  },
  danger: {
    chip: 'bg-red-50 text-red-600',
    bar: 'bg-red-500',
    text: 'text-red-600',
    soft: 'bg-red-50',
  },
  info: {
    chip: 'bg-blue-50 text-blue-600',
    bar: 'bg-blue-500',
    text: 'text-blue-600',
    soft: 'bg-blue-50',
  },
  orange: {
    chip: 'bg-orange-50 text-orange-600',
    bar: 'bg-orange-500',
    text: 'text-orange-600',
    soft: 'bg-orange-50',
  },
  gold: {
    chip: 'bg-gradient-to-br from-amber-100 to-orange-100 text-amber-700',
    bar: 'bg-gradient-to-r from-amber-400 to-orange-400',
    text: 'text-amber-600',
    soft: 'bg-amber-50',
  },
  gray: {
    chip: 'bg-gray-100 text-gray-600',
    bar: 'bg-gray-400',
    text: 'text-gray-600',
    soft: 'bg-gray-50',
  },
};
