/**
 * GitHubAnalytics — visualizes the user's GitHub presence.
 *
 * Builds a cumulative repository-growth area chart from the live
 * repository timestamps, plus follower/following stats and a top-language
 * distribution. All values originate from the GitHub service — nothing is
 * fabricated here.
 *
 * @author DevLaunch
 */

import React, { useMemo } from 'react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import { Github, GitFork, Link2, Code2 } from 'lucide-react';
import { AnalyticsCard } from './AnalyticsCard';
import { CountUp } from '../../ui/CountUp';
import { safeParseDate, shortDateLabel } from './analyticsUtils';
import type { DashboardResponse } from '../../../types/dashboard';
import type { RepositoryResponse, LanguageStatisticsResponse } from '../../../types/github';

interface GitHubAnalyticsProps {
  /** The aggregated dashboard data. */
  data: DashboardResponse;
  /** Live repositories, or null while loading/unavailable. */
  repos: RepositoryResponse[] | null;
  /** Language statistics, or null while loading/unavailable. */
  languages: LanguageStatisticsResponse[] | null;
  /** Whether the GitHub data is still loading. */
  loading?: boolean;
}

const StatTile: React.FC<{ label: string; value: React.ReactNode }> = ({ label, value }) => (
  <div className="rounded-xl border border-gray-100 bg-gray-50/60 px-2.5 py-2 text-center">
    <p className="text-lg font-bold text-gray-900">{value}</p>
    <p className="mt-0.5 text-[10px] font-medium uppercase tracking-wide text-gray-400">
      {label}
    </p>
  </div>
);

export const GitHubAnalytics: React.FC<GitHubAnalyticsProps> = ({
  data,
  repos,
  languages,
  loading = false,
}) => {
  const connected = data.githubUsername !== null;

  const growthData = useMemo(() => {
    if (!repos || repos.length === 0) return [];
    const sorted = [...repos].sort((a, b) => a.createdAt.localeCompare(b.createdAt));
    return sorted.map((repo, index) => ({
      name: shortDateLabel(safeParseDate(repo.createdAt) ?? new Date()),
      repos: index + 1,
    }));
  }, [repos]);

  const languageData = useMemo(() => {
    if (!languages || languages.length === 0) return [];
    const total = languages.reduce((sum, l) => sum + l.repositoryCount, 0);
    const top = [...languages]
      .sort((a, b) => b.repositoryCount - a.repositoryCount)
      .slice(0, 5);
    return top.map((l) => ({
      name: l.language,
      count: l.repositoryCount,
      pct: total > 0 ? Math.round((l.repositoryCount / total) * 100) : 0,
    }));
  }, [languages]);

  return (
    <AnalyticsCard
      title="GitHub Analytics"
      subtitle={connected ? `@${data.githubUsername}` : 'Connect to unlock analytics'}
      icon={<Github className="h-5 w-5" />}
      tone="info"
      loading={loading}
    >
      {!connected ? (
        <div className="flex flex-col items-center py-8 text-center">
          <div className="mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-blue-50">
            <Github className="h-6 w-6 text-blue-400" />
          </div>
          <p className="text-sm font-medium text-gray-700">No GitHub account connected</p>
          <p className="mt-1 max-w-[220px] text-xs leading-relaxed text-gray-400">
            Connect your account to visualize repository growth and language usage.
          </p>
          <span className="mt-3 inline-flex items-center gap-1.5 text-xs font-medium text-blue-600">
            <Link2 className="h-3.5 w-3.5" /> Connect from the GitHub page
          </span>
        </div>
      ) : (
        <div>
          {/* Headline stats */}
          <div className="grid grid-cols-3 gap-2.5">
            <StatTile
              label="Repos"
              value={<CountUp value={data.githubRepositories} />}
            />
            <StatTile label="Followers" value={data.githubFollowers ?? '—'} />
            <StatTile label="Following" value={data.githubFollowing ?? '—'} />
          </div>

          {/* Top language chip */}
          {data.githubTopLanguage && (
            <div className="mt-3 inline-flex items-center gap-1.5 rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold text-blue-700">
              <Code2 className="h-3.5 w-3.5" />
              {data.githubTopLanguage}
            </div>
          )}

          {/* Repository growth */}
          {growthData.length > 1 ? (
            <div className="mt-3">
              <p className="mb-1.5 text-[11px] font-semibold uppercase tracking-wide text-gray-400">
                Repository Growth
              </p>
              <ResponsiveContainer width="100%" height={110}>
                <AreaChart
                  data={growthData}
                  margin={{ top: 6, right: 4, left: -30, bottom: 0 }}
                >
                  <defs>
                    <linearGradient id="githubGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#3b82f6" stopOpacity={0.35} />
                      <stop offset="100%" stopColor="#3b82f6" stopOpacity={0.02} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" vertical={false} />
                  <XAxis
                    dataKey="name"
                    tick={{ fontSize: 10, fill: '#9ca3af' }}
                    axisLine={{ stroke: '#e5e7eb' }}
                    tickLine={false}
                    interval="preserveStartEnd"
                  />
                  <YAxis
                    allowDecimals={false}
                    tick={{ fontSize: 10, fill: '#9ca3af' }}
                    axisLine={false}
                    tickLine={false}
                  />
                  <Tooltip
                    contentStyle={{
                      borderRadius: '10px',
                      border: '1px solid #e5e7eb',
                      boxShadow: '0 8px 24px -8px rgba(16,24,40,0.18)',
                      fontSize: '12px',
                    }}
                    formatter={(value: number) => [`${value}`, 'Repositories']}
                  />
                  <Area
                    type="monotone"
                    dataKey="repos"
                    stroke="#3b82f6"
                    strokeWidth={2.5}
                    fill="url(#githubGradient)"
                    dot={false}
                    activeDot={{ r: 5, fill: '#3b82f6', strokeWidth: 2, stroke: '#fff' }}
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div className="mt-3 flex h-16 items-center justify-center rounded-xl border border-dashed border-gray-200 bg-gray-50/50">
              <p className="text-xs text-gray-400">
                {repos && repos.length === 1
                  ? 'Add more repositories to see your growth curve'
                  : 'Repository growth unavailable'}
              </p>
            </div>
          )}

          {/* Languages */}
          {languageData.length > 0 && (
            <div className="mt-4">
              <p className="mb-2 text-[11px] font-semibold uppercase tracking-wide text-gray-400">
                Top Languages
              </p>
              <ul className="space-y-2">
                {languageData.map((lang) => (
                  <li key={lang.name} className="flex items-center gap-2 text-xs">
                    <span className="w-20 truncate text-gray-600">{lang.name}</span>
                    <div className="h-1.5 flex-1 overflow-hidden rounded-full bg-gray-100">
                      <div
                        className="h-full rounded-full bg-blue-500 transition-all duration-1000"
                        style={{ width: `${lang.pct}%` }}
                      />
                    </div>
                    <span className="w-10 text-right font-semibold text-gray-700">
                      {lang.count}
                    </span>
                  </li>
                ))}
              </ul>
            </div>
          )}

          <p className="mt-3 flex items-center gap-1.5 text-[11px] text-gray-400">
            <GitFork className="h-3 w-3" />
            Growth curve built from live repository creation dates
          </p>
        </div>
      )}
    </AnalyticsCard>
  );
};
