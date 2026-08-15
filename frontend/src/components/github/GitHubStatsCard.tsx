/**
 * GitHubStatsCard — summary statistics for a GitHub user.
 *
 * Displays repository count, followers, following, public gists, and top
 * language as a responsive grid of stat tiles, plus a one-line profile
 * overview. Tiles lift on hover for a more premium feel.
 *
 * @author DevLaunch
 */

import React from 'react';
import {
  BookMarked,
  Users,
  UserPlus,
  Code2,
  FileCode2,
  Sparkles,
  type LucideIcon,
} from 'lucide-react';
import { Card } from '../ui/Card';
import { formatNumber } from '../../utils/format';

interface GitHubStatsCardProps {
  /** The GitHub username these stats belong to. */
  username?: string;
  /** Total number of public repositories. */
  repositories: number;
  /** Number of followers. */
  followers: number;
  /** Number of users this user follows. */
  following: number;
  /** Number of public gists. */
  publicGists?: number;
  /** The user's most-used programming language, or null. */
  topLanguage: string | null;
}

type StatColor = 'indigo' | 'emerald' | 'blue' | 'purple' | 'teal';

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
  blue: 'bg-blue-100 text-blue-600 group-hover:bg-blue-600 group-hover:text-white',
  purple: 'bg-purple-100 text-purple-600 group-hover:bg-purple-600 group-hover:text-white',
  teal: 'bg-teal-100 text-teal-600 group-hover:bg-teal-600 group-hover:text-white',
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

export const GitHubStatsCard: React.FC<GitHubStatsCardProps> = ({
  username,
  repositories,
  followers,
  following,
  publicGists = 0,
  topLanguage,
}) => {
  const summary = username
    ? `${repositories} public ${
        repositories === 1 ? 'repository' : 'repositories'
      }, ${followers} followers, ${following} following, and ${publicGists} public ${
        publicGists === 1 ? 'gist' : 'gists'
      }.`
    : null;

  return (
    <Card
      header={
        <div className="flex flex-wrap items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <Sparkles className="h-5 w-5 text-indigo-500" />
            <h2 className="text-base font-semibold text-gray-900">Profile Overview</h2>
          </div>
          {username && (
            <span className="rounded-full bg-indigo-50 px-2.5 py-0.5 text-xs font-semibold text-indigo-600 ring-1 ring-inset ring-indigo-100">
              @{username}
            </span>
          )}
        </div>
      }
    >
      <div className="grid grid-cols-2 gap-3 lg:grid-cols-5">
        <StatTile icon={BookMarked} label="Repositories" value={formatNumber(repositories)} color="indigo" />
        <StatTile icon={Users} label="Followers" value={formatNumber(followers)} color="emerald" />
        <StatTile icon={UserPlus} label="Following" value={formatNumber(following)} color="blue" />
        <StatTile icon={FileCode2} label="Public Gists" value={formatNumber(publicGists)} color="teal" />
        <StatTile icon={Code2} label="Top Language" value={topLanguage ?? '—'} color="purple" />
      </div>

      {summary && (
        <p className="mt-4 rounded-xl bg-gray-50/80 px-4 py-3 text-sm leading-relaxed text-gray-500">
          <span className="font-semibold text-gray-700">Contribution summary —</span> {summary}
        </p>
      )}
    </Card>
  );
};
