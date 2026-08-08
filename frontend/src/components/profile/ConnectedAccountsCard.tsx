/**
 * ConnectedAccountsCard — read-only overview of the user's linked
 * external accounts (GitHub / LeetCode).
 *
 * Renders one compact card per platform with a brand icon chip, platform
 * name, username, and a Connected / Connect status badge. Clicking a card
 * navigates to that platform's analytics page, where accounts are managed.
 * All data comes from the backend's GET /api/users/me response.
 *
 * @author DevLaunch
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Github,
  Code2,
  CheckCircle2,
  Link2,
  ChevronRight,
  type LucideIcon,
} from 'lucide-react';
import { ROUTES } from '../../constants/routes';
import type { UserResponse } from '../../types/user';

interface ConnectedAccountsCardProps {
  /** The authenticated user's profile data. */
  user: UserResponse;
}

/** Display configuration for one platform. */
interface PlatformAccount {
  icon: LucideIcon;
  name: string;
  username: string | null;
  /** Classes for the brand icon chip. */
  chip: string;
  /** Route navigated to when the card is clicked. */
  to: string;
}

export const ConnectedAccountsCard: React.FC<ConnectedAccountsCardProps> = ({ user }) => {
  const navigate = useNavigate();

  const platforms: PlatformAccount[] = [
    {
      icon: Github,
      name: 'GitHub',
      username: user.githubUsername,
      chip: 'bg-gray-900 text-white',
      to: ROUTES.GITHUB_ANALYTICS,
    },
    {
      icon: Code2,
      name: 'LeetCode',
      username: user.leetcodeUsername,
      chip: 'bg-orange-100 text-orange-600',
      to: ROUTES.LEETCODE_TRACKER,
    },
  ];

  return (
    <section className="overflow-hidden rounded-2xl border border-gray-100 bg-white shadow-[0_1px_3px_rgba(16,24,40,0.06)]">
      {/* Header */}
      <header className="flex items-center justify-between border-b border-gray-100 px-6 py-4">
        <div>
          <h3 className="text-sm font-semibold text-gray-900">Connected Accounts</h3>
          <p className="mt-0.5 text-xs text-gray-500">
            Link GitHub &amp; LeetCode to unlock analytics.
          </p>
        </div>
        <ChevronRight className="h-4 w-4 text-gray-300" />
      </header>

      {/* Platform cards */}
      <div className="grid gap-3 p-4 sm:grid-cols-2">
        {platforms.map((platform) => {
          const isConnected = !!platform.username;
          return (
            <button
              key={platform.name}
              type="button"
              onClick={() => navigate(platform.to)}
              className="group flex items-center gap-3 rounded-xl border border-gray-100 bg-white p-3.5 text-left transition-all duration-300 hover:-translate-y-0.5 hover:border-gray-200 hover:shadow-[0_12px_24px_-12px_rgba(16,24,40,0.18)]"
            >
              <span
                className={`flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-lg transition-transform duration-300 group-hover:scale-105 ${platform.chip}`}
              >
                <platform.icon className="h-5 w-5" />
              </span>

              <span className="min-w-0 flex-1">
                <span className="block text-sm font-semibold text-gray-900">
                  {platform.name}
                </span>
                <span className="block truncate text-xs text-gray-500">
                  {isConnected ? `@${platform.username}` : 'Not connected yet'}
                </span>
              </span>

              {isConnected ? (
                <span className="inline-flex flex-shrink-0 items-center gap-1 rounded-full bg-emerald-50 px-2 py-0.5 text-[10px] font-semibold text-emerald-700 ring-1 ring-inset ring-emerald-200/70">
                  <CheckCircle2 className="h-3 w-3" />
                  Connected
                </span>
              ) : (
                <span className="inline-flex flex-shrink-0 items-center gap-1 rounded-full bg-gray-100 px-2 py-0.5 text-[10px] font-semibold text-gray-500">
                  <Link2 className="h-3 w-3" />
                  Connect
                </span>
              )}
            </button>
          );
        })}
      </div>
    </section>
  );
};
