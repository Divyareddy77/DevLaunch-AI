/**
 * ProfileCard — read-only summary of the authenticated user's identity.
 *
 * Displays the initials avatar, full name, role and account status
 * badges, email, and phone number. All fields come from the backend's
 * GET /api/users/me response.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Mail, Phone, type LucideIcon } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { UserAvatar } from './UserAvatar';
import type { UserResponse } from '../../types/user';

interface ProfileCardProps {
  /** The authenticated user's profile data. */
  user: UserResponse;
}

/** A single read-only detail row. */
interface DetailRowProps {
  /** Lucide icon rendered alongside the value. */
  icon: LucideIcon;
  /** Label describing the value. */
  label: string;
  /** The value to display. */
  value: string;
}

const DetailRow: React.FC<DetailRowProps> = ({ icon: Icon, label, value }) => (
  <div className="flex items-center gap-3">
    <div className="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-lg bg-gray-100 text-gray-500">
      <Icon className="h-4 w-4" />
    </div>
    <div className="min-w-0">
      <p className="text-xs text-gray-400">{label}</p>
      <p className="truncate text-sm font-medium text-gray-800">{value}</p>
    </div>
  </div>
);

export const ProfileCard: React.FC<ProfileCardProps> = ({ user }) => {
  const displayRole = user.role.charAt(0).toUpperCase() + user.role.slice(1).toLowerCase();

  return (
    <Card className="overflow-hidden" padded={false}>
      {/* Banner */}
      <div className="h-24 bg-gradient-to-r from-primary-600 via-primary-500 to-indigo-500" />

      <div className="px-6 pb-6">
        {/* Avatar + identity */}
        <div className="-mt-12 flex items-end gap-4">
          <UserAvatar
            firstName={user.firstName}
            lastName={user.lastName}
            size="lg"
            className="border-4 border-white"
          />
          <div className="min-w-0 pb-1">
            <h2 className="truncate text-xl font-bold text-gray-900">
              {user.firstName} {user.lastName}
            </h2>
            <div className="mt-1 flex flex-wrap items-center gap-2">
              <Badge variant={user.role === 'ADMIN' ? 'danger' : 'primary'}>
                {displayRole}
              </Badge>
              <Badge variant={user.isActive ? 'success' : 'default'}>
                {user.isActive ? 'Active' : 'Inactive'}
              </Badge>
            </div>
          </div>
        </div>

        {/* Contact details */}
        <div className="mt-5 grid gap-4 border-t border-gray-100 pt-5 sm:grid-cols-2">
          <DetailRow icon={Mail} label="Email" value={user.email} />
          <DetailRow icon={Phone} label="Phone" value={user.phone || 'Not provided'} />
        </div>
      </div>
    </Card>
  );
};
