/**
 * ActivityTimeline — the dashboard's recent activity feed.
 *
 * Builds a modern timeline from the existing XP-history ledger, mapping
 * each XP reason to an icon and accent tone. Every entry shows the
 * activity icon, title, description, relative timestamp, and XP earned —
 * all real data from the gamification API.
 *
 * @author DevLaunch
 */

import React, { useEffect, useMemo, useState } from 'react';
import {
  FileText,
  FileSearch,
  Briefcase,
  Mic,
  CalendarCheck,
  Github,
  Code2,
  Target,
  Trophy,
  History,
  type LucideIcon,
} from 'lucide-react';
import { achievementService } from '../../../services/achievement.service';
import { AnalyticsCard } from '../analytics/AnalyticsCard';
import { EmptyState } from '../../shared/EmptyState';
import { formatRelativeTime } from '../../../utils/date';
import { CARD_TONES, type CardTone } from '../cardTones';
import type { XpHistoryEntry, XpReason } from '../../../types/achievement';

interface ActivityTimelineProps {
  /** Additional CSS classes (e.g. grid column spans). */
  className?: string;
}

/** How many of the most recent activities to show. */
const TIMELINE_LIMIT = 7;

/** Icon + tone per XP reason, mirroring the platform's activity types. */
const REASON_META: Record<XpReason, { icon: LucideIcon; tone: CardTone }> = {
  RESUME_CREATED: { icon: FileText, tone: 'info' },
  RESUME_REVIEWED: { icon: FileSearch, tone: 'violet' },
  JOB_APPLICATION_CREATED: { icon: Briefcase, tone: 'orange' },
  INTERVIEW_COMPLETED: { icon: Mic, tone: 'violet' },
  STUDY_TASK_COMPLETED: { icon: CalendarCheck, tone: 'warning' },
  GITHUB_CONNECTED: { icon: Github, tone: 'info' },
  LEETCODE_SYNCED: { icon: Code2, tone: 'danger' },
  PLACEMENT_UPDATED: { icon: Target, tone: 'primary' },
  ACHIEVEMENT_UNLOCKED: { icon: Trophy, tone: 'gold' },
};

export const ActivityTimeline: React.FC<ActivityTimelineProps> = ({ className }) => {
  const [entries, setEntries] = useState<XpHistoryEntry[] | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    achievementService
      .getHistory()
      .then((history) => setEntries(history.slice(0, TIMELINE_LIMIT)))
      .catch(() => setEntries(null))
      .finally(() => setLoading(false));
  }, []);

  // Newest first — the API already returns newest-first, but be explicit.
  // Spread into a fresh array so the sort never mutates the state array.
  const items = useMemo(
    () => [...(entries ?? [])].sort((a, b) => b.createdAt.localeCompare(a.createdAt)),
    [entries],
  );

  return (
    <AnalyticsCard
      title="Recent Activity"
      subtitle="Your latest momentum across the platform"
      icon={<History className="h-5 w-5" />}
      tone="primary"
      loading={loading}
      className={className}
    >
      {!loading && items.length === 0 ? (
        <EmptyState
          icon={History}
          tone="gray"
          title="No activity yet"
          description="Complete tasks across modules and your recent activity will show up here."
        />
      ) : (
        <ol className="relative">
          {items.map((entry, index) => {
            const meta = REASON_META[entry.reason] ?? REASON_META.PLACEMENT_UPDATED;
            const Icon = meta.icon;
            const t = CARD_TONES[meta.tone];
            const isLast = index === items.length - 1;

            return (
              <li key={entry.id} className="relative flex gap-3 pb-4 last:pb-0">
                {/* Timeline rail */}
                {!isLast && (
                  <span
                    className="absolute bottom-0 left-[17px] top-10 w-px bg-gray-100"
                    aria-hidden="true"
                  />
                )}
                <span
                  className={`relative z-10 flex h-9 w-9 shrink-0 items-center justify-center rounded-xl ring-4 ring-white ${t.chip}`}
                >
                  <Icon className="h-4 w-4" />
                </span>

                <div className="min-w-0 flex-1 pt-0.5">
                  <div className="flex items-baseline justify-between gap-2">
                    <p className="truncate text-sm font-medium text-gray-800">
                      {entry.description || 'Activity'}
                    </p>
                    <span className="shrink-0 text-[11px] text-gray-400">
                      {formatRelativeTime(entry.createdAt)}
                    </span>
                  </div>
                  <div className="mt-0.5 flex items-center justify-between gap-2">
                    <p className="truncate text-xs text-gray-500">{entry.reason.replace(/_/g, ' ').toLowerCase()}</p>
                    <span className="inline-flex shrink-0 items-center gap-0.5 rounded-full bg-emerald-50 px-1.5 py-0.5 text-[10px] font-bold text-emerald-600">
                      +{entry.amount} XP
                    </span>
                  </div>
                </div>
              </li>
            );
          })}
        </ol>
      )}
    </AnalyticsCard>
  );
};
