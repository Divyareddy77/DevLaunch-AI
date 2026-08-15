/**
 * LeetCodeStatsCard — summary statistics for a LeetCode user.
 *
 * Displays total, easy, medium, and hard problems solved, plus acceptance
 * rate and global ranking as a responsive grid of stat tiles with distinct
 * colour coding and hover lift. Includes a one-line overview.
 *
 * @author DevLaunch
 */

import React from 'react';
import {
  Target,
  CheckCircle2,
  TrendingUp,
  Zap,
  Percent,
  Trophy,
  Sparkles,
  type LucideIcon,
} from 'lucide-react';
import { Card } from '../ui/Card';
import { formatNumber } from '../../utils/format';

interface LeetCodeStatsCardProps {
  /** The LeetCode username these stats belong to. */
  username?: string;
  /** Total number of problems solved. */
  totalSolved: number;
  /** Number of easy-difficulty problems solved. */
  easySolved: number;
  /** Number of medium-difficulty problems solved. */
  mediumSolved: number;
  /** Number of hard-difficulty problems solved. */
  hardSolved: number;
  /** Overall acceptance rate as a percentage, or null. */
  acceptanceRate: number | null;
  /** Global ranking, or null. */
  ranking: number | null;
}

type StatColor = 'indigo' | 'emerald' | 'amber' | 'red' | 'sky' | 'violet';

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
  indigo: 'bg-indigo-100 text-indigo-600 group-hover:bg-indigo-600 group-hover:text-white',
  emerald: 'bg-emerald-100 text-emerald-600 group-hover:bg-emerald-600 group-hover:text-white',
  amber: 'bg-amber-100 text-amber-600 group-hover:bg-amber-500 group-hover:text-white',
  red: 'bg-red-100 text-red-600 group-hover:bg-red-500 group-hover:text-white',
  sky: 'bg-sky-100 text-sky-600 group-hover:bg-sky-500 group-hover:text-white',
  violet: 'bg-violet-100 text-violet-600 group-hover:bg-violet-500 group-hover:text-white',
};

const StatTile: React.FC<StatTileProps> = ({ icon: Icon, label, value, color }) => (
  <div className="group flex items-center gap-3 rounded-xl border border-gray-100 bg-gray-50/70 p-4 transition-all duration-200 hover:-translate-y-0.5 hover:border-gray-200 hover:bg-white hover:shadow-md">
    <div
      className={`flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-lg transition-colors duration-200 ${iconColorMap[color]}`}
    >
      <Icon className="h-5 w-5" />
    </div>
    <div className="min-w-0">
      <p className="truncate text-xl font-bold text-gray-900">{value}</p>
      <p className="truncate text-xs text-gray-500">{label}</p>
    </div>
  </div>
);

export const LeetCodeStatsCard: React.FC<LeetCodeStatsCardProps> = ({
  username,
  totalSolved,
  easySolved,
  mediumSolved,
  hardSolved,
  acceptanceRate,
  ranking,
}) => {
  const summary =
    `${totalSolved} problems solved — ${easySolved} easy, ${mediumSolved} medium, ${hardSolved} hard` +
    (acceptanceRate != null ? `, ${acceptanceRate.toFixed(1)}% acceptance` : '') +
    (ranking != null ? `, ranked #${formatNumber(ranking)} globally` : '') +
    '.';

  return (
    <Card
      header={
        <div className="flex flex-wrap items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <Sparkles className="h-5 w-5 text-orange-500" />
            <h2 className="text-base font-semibold text-gray-900">Coding Progress overview</h2>
          </div>
          {username && (
            <span className="rounded-full bg-orange-50 px-2.5 py-0.5 text-xs font-semibold text-orange-600 ring-1 ring-inset ring-orange-100">
              @{username}
            </span>
          )}
        </div>
      }
    >
      <div className="grid grid-cols-2 gap-3 lg:grid-cols-3 xl:grid-cols-6">
        <StatTile icon={Target} label="Total Solved" value={formatNumber(totalSolved)} color="indigo" />
        <StatTile icon={CheckCircle2} label="Easy Solved" value={formatNumber(easySolved)} color="emerald" />
        <StatTile icon={TrendingUp} label="Medium Solved" value={formatNumber(mediumSolved)} color="amber" />
        <StatTile icon={Zap} label="Hard Solved" value={formatNumber(hardSolved)} color="red" />
        <StatTile
          icon={Percent}
          label="Acceptance Rate"
          value={acceptanceRate != null ? `${acceptanceRate.toFixed(1)}%` : '—'}
          color="sky"
        />
        <StatTile
          icon={Trophy}
          label="Global Ranking"
          value={ranking != null ? `#${formatNumber(ranking)}` : '—'}
          color="violet"
        />
      </div>

      <p className="mt-4 rounded-xl bg-gray-50/80 px-4 py-3 text-sm leading-relaxed text-gray-500">
        <span className="font-semibold text-gray-700">Overview —</span> {summary}
      </p>
    </Card>
  );
};
