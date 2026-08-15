/**
 * AnnouncementBanner — compact auto-sliding announcement carousel.
 *
 * Replaces the previous tall stacked list with a slim single-line strip
 * that auto-rotates through active announcements (pausing on hover).
 * Dismissed announcement IDs persist in localStorage. Renders nothing when
 * there is nothing left to show.
 *
 * @author DevLaunch
 */

import React, { useEffect, useMemo, useState } from 'react';
import { Megaphone, X, CalendarDays } from 'lucide-react';
import { Badge } from '../ui/Badge';
import { formatDate } from '../../utils/date';
import { STORAGE_KEYS } from '../../constants/storage';
import type { Announcement } from '../../types/announcement';

interface AnnouncementBannerProps {
  /** The active announcements to display. */
  announcements: Announcement[];
}

/** How long each announcement is shown before rotating (ms). */
const ROTATION_MS = 5000;

/** Reads the persisted set of dismissed announcement IDs. */
function readDismissedIds(): number[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.DISMISSED_ANNOUNCEMENTS);
    if (!raw) return [];
    const parsed: unknown = JSON.parse(raw);
    return Array.isArray(parsed)
      ? parsed.filter((id): id is number => typeof id === 'number')
      : [];
  } catch {
    return [];
  }
}

/** Persists the set of dismissed announcement IDs. */
function writeDismissedIds(ids: number[]): void {
  localStorage.setItem(STORAGE_KEYS.DISMISSED_ANNOUNCEMENTS, JSON.stringify(ids));
}

export const AnnouncementBanner: React.FC<AnnouncementBannerProps> = ({ announcements }) => {
  const [dismissedIds, setDismissedIds] = useState<number[]>(readDismissedIds);
  const [index, setIndex] = useState(0);
  const [paused, setPaused] = useState(false);

  const visible = useMemo(
    () => announcements.filter((announcement) => !dismissedIds.includes(announcement.id)),
    [announcements, dismissedIds],
  );

  // Keep the index in range when the list changes.
  useEffect(() => {
    if (index >= visible.length && visible.length > 0) {
      setIndex(0);
    }
  }, [index, visible.length]);

  // Auto-rotate, pausing while hovered or when there's nothing to rotate.
  useEffect(() => {
    if (visible.length <= 1 || paused) return;
    const id = window.setInterval(() => {
      setIndex((current) => (current + 1) % visible.length);
    }, ROTATION_MS);
    return () => window.clearInterval(id);
  }, [visible.length, paused]);

  const handleDismiss = (id: number) => {
    setDismissedIds((prev) => {
      const next = [...prev, id];
      writeDismissedIds(next);
      return next;
    });
  };

  if (visible.length === 0) {
    return null;
  }

  const current = visible[Math.min(index, visible.length - 1)];

  return (
    <div
      role="region"
      aria-label="Announcements"
      className="flex items-center gap-3 rounded-2xl border border-indigo-100 bg-gradient-to-r from-indigo-50/80 via-white to-white px-4 py-3 shadow-[0_1px_3px_rgba(16,24,40,0.05)]"
      onMouseEnter={() => setPaused(true)}
      onMouseLeave={() => setPaused(false)}
    >
      <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-indigo-100 text-indigo-600">
        <Megaphone className="h-4 w-4" />
      </span>

      {/* Rotating announcement */}
      <div className="min-w-0 flex-1" aria-live="polite">
        <div key={current.id} className="animate-slide-in min-w-0">
          <div className="flex flex-wrap items-center gap-x-2 gap-y-0.5">
            <p className="truncate text-sm font-semibold text-gray-900">{current.title}</p>
            <span className="inline-flex shrink-0 items-center gap-1 text-[11px] text-gray-400">
              <CalendarDays className="h-3 w-3" />
              {formatDate(current.createdAt)}
            </span>
          </div>
          <p
            className="truncate text-xs text-gray-500"
            title={current.content}
          >
            {current.content}
          </p>
        </div>
      </div>

      {/* Carousel dots */}
      {visible.length > 1 && (
        <div className="hidden shrink-0 items-center gap-1 sm:flex" role="tablist" aria-label="Announcement navigation">
          {visible.map((announcement, dotIndex) => (
            <button
              key={announcement.id}
              type="button"
              role="tab"
              aria-selected={dotIndex === index}
              aria-label={`Show announcement ${dotIndex + 1}: ${announcement.title}`}
              onClick={() => setIndex(dotIndex)}
              className={`h-1.5 rounded-full transition-all duration-300 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 ${
                dotIndex === index ? 'w-4 bg-indigo-500' : 'w-1.5 bg-indigo-200 hover:bg-indigo-300'
              }`}
            />
          ))}
        </div>
      )}

      <Badge variant="primary" size="sm" className="hidden md:inline-flex">
        {visible.length} new
      </Badge>

      <button
        onClick={() => handleDismiss(current.id)}
        title="Dismiss announcement"
        aria-label={`Dismiss announcement: ${current.title}`}
        className="flex-shrink-0 rounded-lg p-1.5 text-gray-300 transition-colors hover:bg-indigo-50 hover:text-gray-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500"
      >
        <X className="h-4 w-4" />
      </button>
    </div>
  );
};
