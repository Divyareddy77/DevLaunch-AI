/**
 * GitHubCard — dashboard widget summarising the user's GitHub presence.
 *
 * When a GitHub account is linked it displays the live repository count,
 * top language, follower count, and following count. When no account is
 * connected it shows a friendly prompt with a Connect button that
 * navigates to the GitHub Analytics page.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Github, ExternalLink, Star, GitFork, Users, Code2 } from 'lucide-react';
import { DashboardCard } from './DashboardCard';
import { EmptyState } from '../shared/EmptyState';

interface GitHubCardProps {
  /** Whether a GitHub account is linked to the user. */
  connected: boolean;
  /** The linked GitHub username, or null when not connected. */
  username: string | null;
  /** Total number of public repositories. */
  repositoryCount: number | null;
  /** The user's most-used programming language. */
  topLanguage: string | null;
  /** Number of followers on GitHub. */
  followers: number | null;
  /** Number of users the person follows on GitHub. */
  following: number | null;
  /** Whether the card data is still loading. */
  loading?: boolean;
  /** Error message to display if data could not be loaded. */
  error?: string | null;
  /** Callback to navigate to the full GitHub analytics page. */
  onViewAll?: () => void;
  /** Callback to navigate to the GitHub analytics page to connect an account. */
  onConnect?: () => void;
}

export const GitHubCard: React.FC<GitHubCardProps> = ({
  connected,
  username,
  repositoryCount,
  topLanguage,
  followers,
  following,
  loading = false,
  error = null,
  onViewAll,
  onConnect,
}) => {
  return (
    <DashboardCard
      title="GitHub Overview"
      icon={<Github className="h-5 w-5" />}
      tone="info"
      action={
        onViewAll && (
          <button
            onClick={onViewAll}
            className="flex items-center gap-1 text-xs font-medium text-indigo-600 hover:text-indigo-800 transition-colors"
          >
            View All
            <ExternalLink className="h-3 w-3" />
          </button>
        )
      }
      loading={loading}
    >
      {error ? (
        <p className="text-sm text-red-500">{error}</p>
      ) : !connected ? (
        /* ---- Not connected: prompt to link an account ---- */
        <EmptyState
          icon={Github}
          tone="info"
          title="No GitHub account connected"
          description="Connect your GitHub account to display your live repositories, languages, and followers."
          actionLabel="Connect Account"
          onAction={onConnect}
        />
      ) : (
        /* ---- Connected: live statistics ---- */
        <div>
          <p className="mb-3 truncate text-center text-xs font-medium text-gray-500">
            @{username}
          </p>
          <div className="grid grid-cols-2 gap-4">
            <div className="text-center">
              <div className="flex items-center justify-center gap-1 text-2xl font-bold text-gray-900">
                <GitFork className="h-4 w-4 text-gray-400" />
                {repositoryCount ?? '—'}
              </div>
              <p className="mt-0.5 text-xs text-gray-500">Repositories</p>
            </div>

            <div className="text-center">
              <div className="flex items-center justify-center gap-1 text-2xl font-bold text-gray-900">
                <Code2 className="h-4 w-4 text-gray-400" />
                {topLanguage ?? '—'}
              </div>
              <p className="mt-0.5 text-xs text-gray-500">Top Language</p>
            </div>

            <div className="text-center">
              <div className="flex items-center justify-center gap-1 text-2xl font-bold text-gray-900">
                <Users className="h-4 w-4 text-gray-400" />
                {followers ?? '—'}
              </div>
              <p className="mt-0.5 text-xs text-gray-500">Followers</p>
            </div>

            <div className="text-center">
              <div className="flex items-center justify-center gap-1 text-2xl font-bold text-gray-900">
                <Star className="h-4 w-4 text-gray-400" />
                {following ?? '—'}
              </div>
              <p className="mt-0.5 text-xs text-gray-500">Following</p>
            </div>
          </div>
        </div>
      )}
    </DashboardCard>
  );
};
