/**
 * GitHubStatsCard — summary statistics for a GitHub user.
 *
 * Displays repository count, followers, following, and top language
 * as a responsive grid of stat tiles.
 *
 * @author DevLaunch
 */

import React from 'react';
import { BookMarked, Users, UserPlus, Code2, type LucideIcon } from 'lucide-react';
import { Card } from '../ui/Card';
import { formatNumber } from '../../utils/format';

interface GitHubStatsCardProps {
  /** Total number of public repositories. */
  repositories: number;
  /** Number of followers. */
  followers: number;
  /** Number of users this user follows. */
  following: number;
  /** The user's most-used programming language, or null. */
  topLanguage: string | null;
}

type StatColor = 'indigo' | 'emerald' | 'blue' | 'purple';

/** A single statistic tile. */
interface StatTileProps {
  /** Lucide icon rendered in the tile. */
  icon: LucideIcon;
  /** Label describing the statistic. */
  label: string;
  /** The display value (already formatted). */
  value: string;
  /** Colour variant for the icon container. */
  color: StatColor;
}

const iconColorMap: Record<StatColor, string> = {
  indigo: 'bg-indigo-100 text-indigo-600',
  emerald: 'bg-emerald-100 text-emerald-600',
  blue: 'bg-blue-100 text-blue-600',
  purple: 'bg-purple-100 text-purple-600',
};

const StatTile: React.FC<StatTileProps> = ({ icon: Icon, label, value, color }) => (
  <div className="flex items-center gap-3 rounded-xl bg-gray-50 p-4">
    <div
      className={`flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-lg ${iconColorMap[color]}`}
    >
      <Icon className="h-5 w-5" />
    </div>
    <div className="min-w-0">
      <p className="truncate text-xl font-bold text-gray-900">{value}</p>
      <p className="truncate text-xs text-gray-500">{label}</p>
    </div>
  </div>
);

export const GitHubStatsCard: React.FC<GitHubStatsCardProps> = ({
  repositories,
  followers,
  following,
  topLanguage,
}) => {
  return (
    <Card>
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatTile icon={BookMarked} label="Repositories" value={formatNumber(repositories)} color="indigo" />
        <StatTile icon={Users} label="Followers" value={formatNumber(followers)} color="emerald" />
        <StatTile icon={UserPlus} label="Following" value={formatNumber(following)} color="blue" />
        <StatTile icon={Code2} label="Top Language" value={topLanguage ?? '—'} color="purple" />
      </div>
    </Card>
  );
};
