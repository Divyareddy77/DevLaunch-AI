/**
 * GitHubProfileCard — displays a GitHub user's public profile summary.
 *
 * Shows the avatar, name, username, bio, company, location, blog,
 * account creation date, and follower/following/repository/gist counts,
 * with a link to the user's GitHub profile.
 *
 * @author DevLaunch
 */

import React from 'react';
import {
  Github,
  ExternalLink,
  Building2,
  MapPin,
  Link2,
  Calendar,
  Users,
  UserPlus,
  BookMarked,
  Code2,
  type LucideIcon,
} from 'lucide-react';
import { Card } from '../ui/Card';
import { formatNumber } from '../../utils/format';
import { formatDate } from '../../utils/date';
import type { GitHubProfileResponse } from '../../types/github';

interface GitHubProfileCardProps {
  /** The GitHub profile data to display. */
  profile: GitHubProfileResponse;
}

/** A single statistic tile shown in the profile footer. */
interface ProfileStatProps {
  /** Lucide icon rendered alongside the value. */
  icon: LucideIcon;
  /** Label describing the statistic. */
  label: string;
  /** The numeric value to display. */
  value: number;
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

export const GitHubProfileCard: React.FC<GitHubProfileCardProps> = ({ profile }) => {
  return (
    <Card className="overflow-hidden" padded={false}>
      {/* Banner */}
      <div className="h-24 bg-gradient-to-r from-indigo-500 via-purple-500 to-blue-500" />

      <div className="px-6 pb-6">
        {/* Avatar + identity */}
        <div className="-mt-12 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:gap-4">
            {profile.avatarUrl ? (
              <img
                src={profile.avatarUrl}
                alt={`${profile.username} avatar`}
                className="h-24 w-24 rounded-full border-4 border-white bg-white object-cover shadow-md"
              />
            ) : (
              <div className="flex h-24 w-24 items-center justify-center rounded-full border-4 border-white bg-gray-100 shadow-md">
                <Github className="h-10 w-10 text-gray-400" />
              </div>
            )}

            <div className="min-w-0 pb-1">
              <h2 className="truncate text-xl font-bold text-gray-900">
                {profile.name ?? profile.username}
              </h2>
              <a
                href={profile.profileUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="mt-0.5 inline-flex items-center gap-1 text-sm text-gray-500 transition-colors hover:text-indigo-600"
              >
                @{profile.username}
                <ExternalLink className="h-3 w-3" />
              </a>
            </div>
          </div>

          {profile.accountCreatedAt && (
            <div className="flex items-center gap-1.5 pb-1 text-xs text-gray-400">
              <Calendar className="h-3.5 w-3.5" />
              Joined {formatDate(profile.accountCreatedAt)}
            </div>
          )}
        </div>

        {/* Bio */}
        {profile.bio && (
          <p className="mt-4 max-w-2xl text-sm leading-relaxed text-gray-600">{profile.bio}</p>
        )}

        {/* Company / location / blog */}
        <div className="mt-4 flex flex-wrap gap-x-6 gap-y-2 text-sm text-gray-500">
          {profile.company && (
            <span className="inline-flex items-center gap-1.5">
              <Building2 className="h-4 w-4 text-gray-400" />
              {profile.company}
            </span>
          )}
          {profile.location && (
            <span className="inline-flex items-center gap-1.5">
              <MapPin className="h-4 w-4 text-gray-400" />
              {profile.location}
            </span>
          )}
          {profile.blog && (
            <a
              href={profile.blog}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex max-w-[220px] items-center gap-1.5 truncate text-gray-500 transition-colors hover:text-indigo-600"
            >
              <Link2 className="h-4 w-4 flex-shrink-0 text-gray-400" />
              {profile.blog}
            </a>
          )}
        </div>

        {/* Stats */}
        <div className="mt-5 grid grid-cols-2 gap-4 border-t border-gray-100 pt-5 sm:grid-cols-4">
          <ProfileStat icon={Users} label="Followers" value={profile.followers} />
          <ProfileStat icon={UserPlus} label="Following" value={profile.following} />
          <ProfileStat icon={BookMarked} label="Public Repositories" value={profile.publicRepositories} />
          <ProfileStat icon={Code2} label="Public Gists" value={profile.publicGists} />
        </div>
      </div>
    </Card>
  );
};
