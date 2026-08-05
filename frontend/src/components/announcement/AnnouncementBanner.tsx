/**
 * AnnouncementBanner — displays active platform announcements.
 *
 * Renders the announcements passed by the parent as a stacked, per-item
 * dismissible list. Dismissed announcement IDs persist in localStorage,
 * so dismissing an announcement hides it across reloads. Renders nothing
 * when there is nothing left to show.
 *
 * @author DevLaunch
 */

import React, { useMemo, useState } from 'react';
import { Megaphone, X, CalendarDays } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { formatDate } from '../../utils/date';
import { STORAGE_KEYS } from '../../constants/storage';
import type { Announcement } from '../../types/announcement';

interface AnnouncementBannerProps {
  /** The active announcements to display. */
  announcements: Announcement[];
}

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

  const visible = useMemo(
    () => announcements.filter((announcement) => !dismissedIds.includes(announcement.id)),
    [announcements, dismissedIds],
  );

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

  return (
    <Card
      className="mb-8"
      header={
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-indigo-100">
              <Megaphone className="h-4 w-4 text-indigo-600" />
            </div>
            <div>
              <h2 className="text-sm font-semibold text-gray-900">Announcements</h2>
              <p className="text-xs text-gray-400">
                Latest updates from the DevLaunch team
              </p>
            </div>
          </div>
          <Badge variant="primary" size="sm">
            {visible.length} new
          </Badge>
        </div>
      }
    >
      <ul className="divide-y divide-gray-100">
        {visible.map((announcement) => (
          <li
            key={announcement.id}
            className="flex items-start justify-between gap-4 py-3 first:pt-0 last:pb-0"
          >
            <div className="min-w-0">
              <div className="flex flex-wrap items-center gap-2">
                <h3 className="text-sm font-semibold text-gray-900">
                  {announcement.title}
                </h3>
                <span className="inline-flex items-center gap-1 text-xs text-gray-400">
                  <CalendarDays className="h-3 w-3" />
                  {formatDate(announcement.createdAt)}
                </span>
              </div>
              <p className="mt-1 whitespace-pre-wrap text-sm text-gray-600">
                {announcement.content}
              </p>
              <p className="mt-1 text-xs text-gray-400">
                By {announcement.createdByName}
              </p>
            </div>
            <button
              onClick={() => handleDismiss(announcement.id)}
              title="Dismiss announcement"
              aria-label={`Dismiss announcement: ${announcement.title}`}
              className="flex-shrink-0 rounded-lg p-1 text-gray-300 transition-colors hover:bg-gray-100 hover:text-gray-500"
            >
              <X className="h-4 w-4" />
            </button>
          </li>
        ))}
      </ul>
    </Card>
  );
};
