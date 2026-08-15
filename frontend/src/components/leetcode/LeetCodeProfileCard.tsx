/**
 * LeetCodeProfileCard — displays a LeetCode user's profile summary.
 *
 * Shows the username, global ranking, and total problems solved,
 * with a link to the user's LeetCode profile.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Code2, Trophy, CheckCircle2, ExternalLink, type LucideIcon } from 'lucide-react';
import { Card } from '../ui/Card';
import { formatNumber } from '../../utils/format';
import type { LeetCodeProfileResponse } from '../../types/leetcode';

interface LeetCodeProfileCardProps {
  /** The LeetCode profile data to display. */
  profile: LeetCodeProfileResponse;
}

/** A single statistic tile shown in the profile footer. */
interface ProfileStatProps {
  /** Lucide icon rendered alongside the value. */
  icon: LucideIcon;
  /** Label describing the statistic. */
  label: string;
  /** The numeric value to display (may be null, shown as an em dash). */
  value: number | null;
}

const ProfileStat: React.FC<ProfileStatProps> = ({ icon: Icon, label, value }) => (
  <div>
    <div className="flex items-center gap-1.5 text-gray-900">
      <Icon className="h-4 w-4 text-gray-400" />
      <span className="text-lg font-bold">{formatNumber(value)}</span>
    </div>
    <p className="mt-0.5 text-xs text-gray-500">{label}</p>
  </div>
);

export const LeetCodeProfileCard: React.FC<LeetCodeProfileCardProps> = ({ profile }) => {
  const profileUrl = `https://leetcode.com/${profile.username}`;

  return (
    <Card className="overflow-hidden" padded={false}>
      {/* Banner */}
      <div className="h-24 bg-gradient-to-r from-amber-400 via-orange-500 to-orange-600" />

      <div className="px-6 pb-6">
        {/* Avatar + identity */}
        <div className="-mt-12 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:gap-4">
            <div className="flex h-24 w-24 items-center justify-center rounded-full border-4 border-white bg-white shadow-md">
              <Code2 className="h-10 w-10 text-orange-500" />
            </div>

            <div className="min-w-0 pb-1">
              <h2 className="truncate text-xl font-bold text-gray-900">@{profile.username}</h2>
              <a
                href={profileUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="mt-0.5 inline-flex items-center gap-1 text-sm text-gray-500 transition-colors hover:text-orange-600"
              >
                leetcode.com/{profile.username}
                <ExternalLink className="h-3 w-3" />
              </a>
            </div>
          </div>
        </div>

        {/* Stats */}
        <div className="mt-5 grid grid-cols-2 gap-4 border-t border-gray-100 pt-5">
          <ProfileStat icon={Trophy} label="Global Ranking" value={profile.ranking} />
          <ProfileStat icon={CheckCircle2} label="Problems Solved" value={profile.totalSolved} />
        </div>
      </div>
    </Card>
  );
};
