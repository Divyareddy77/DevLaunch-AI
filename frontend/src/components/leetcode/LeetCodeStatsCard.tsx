/**
 * LeetCodeStatsCard — summary statistics for a LeetCode user.
 *
 * Displays total, easy, medium, and hard problems solved as a
 * responsive grid of stat tiles with distinct colour coding.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Target, CheckCircle2, TrendingUp, Zap, type LucideIcon } from 'lucide-react';
import { Card } from '../ui/Card';
import { formatNumber } from '../../utils/format';

interface LeetCodeStatsCardProps {
  /** Total number of problems solved. */
  totalSolved: number;
  /** Number of easy-difficulty problems solved. */
  easySolved: number;
  /** Number of medium-difficulty problems solved. */
  mediumSolved: number;
  /** Number of hard-difficulty problems solved. */
  hardSolved: number;
}

type StatColor = 'indigo' | 'emerald' | 'amber' | 'red';

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
  amber: 'bg-amber-100 text-amber-600',
  red: 'bg-red-100 text-red-600',
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

export const LeetCodeStatsCard: React.FC<LeetCodeStatsCardProps> = ({
  totalSolved,
  easySolved,
  mediumSolved,
  hardSolved,
}) => {
  return (
    <Card>
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatTile icon={Target} label="Total Solved" value={formatNumber(totalSolved)} color="indigo" />
        <StatTile icon={CheckCircle2} label="Easy Solved" value={formatNumber(easySolved)} color="emerald" />
        <StatTile icon={TrendingUp} label="Medium Solved" value={formatNumber(mediumSolved)} color="amber" />
        <StatTile icon={Zap} label="Hard Solved" value={formatNumber(hardSolved)} color="red" />
      </div>
    </Card>
  );
};
