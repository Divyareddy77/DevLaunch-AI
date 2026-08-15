/**
 * ProfileCard — read-only summary of the authenticated user's identity.
 *
 * Renders a compact gradient cover with the initials avatar overlapping it
 * (LinkedIn/GitHub style), the full name, role chip and live account status,
 * plus email and phone contact rows. All fields come from the backend's
 * GET /api/users/me response. Connected external accounts (GitHub / LeetCode)
 * live in their own card — see ConnectedAccountsCard.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Mail, Phone, ShieldCheck, ShieldAlert } from 'lucide-react';
import { UserAvatar } from './UserAvatar';
import { capitalize } from '../../utils/format';
import type { UserResponse } from '../../types/user';

interface ProfileCardProps {
  /** The authenticated user's profile data. */
  user: UserResponse;
}

/** A single read-only detail row. */
interface DetailRowProps {
  /** Lucide icon rendered alongside the value. */
  icon: typeof Mail;
  /** Label describing the value. */
  label: string;
  /** The value to display. */
  value: string;
}

const DetailRow: React.FC<DetailRowProps> = ({ icon: Icon, label, value }) => (
  <div className="flex items-center gap-3">
    <div className="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-lg bg-gray-50 text-gray-500 ring-1 ring-inset ring-gray-100">
      <Icon className="h-4 w-4" />
    </div>
    <div className="min-w-0">
      <p className="text-[11px] font-medium uppercase tracking-wide text-gray-400">{label}</p>
      <p className="mt-0.5 truncate text-sm font-semibold text-gray-800">{value}</p>
    </div>
  </div>
);

/** Live account-status pill: pulsing green dot for active, red for inactive. */
const StatusPill: React.FC<{ isActive: boolean }> = ({ isActive }) => (
  <span
    className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ring-inset ${
      isActive
        ? 'bg-emerald-50 text-emerald-700 ring-emerald-200/70'
        : 'bg-red-50 text-red-600 ring-red-200/70'
    }`}
  >
    <span className="relative flex h-2 w-2">
      {isActive && (
        <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-400 opacity-75" />
      )}
      <span
        className={`relative inline-flex h-2 w-2 rounded-full ${
          isActive ? 'bg-emerald-500' : 'bg-red-500'
        }`}
      />
    </span>
    {isActive ? 'Active' : 'Inactive'}
  </span>
);

export const ProfileCard: React.FC<ProfileCardProps> = ({ user }) => {
  const displayRole = capitalize(user.role);
  const isAdmin = user.role === 'ADMIN';

  return (
    <section className="overflow-hidden rounded-2xl border border-gray-100 bg-white shadow-[0_1px_3px_rgba(16,24,40,0.06)]">
      {/* Compact decorative cover */}
      <div className="relative h-[4.5rem] overflow-hidden bg-gradient-to-br from-primary-600 via-primary-500 to-indigo-500">
        <div className="bg-dot-pattern absolute inset-0" aria-hidden="true" />
        <div
          className="animate-float-slow absolute -right-8 -top-10 h-28 w-28 rounded-full bg-white/15 blur-2xl"
          aria-hidden="true"
        />
        <div
          className="absolute -bottom-12 -left-6 h-24 w-24 rounded-full bg-indigo-300/25 blur-2xl"
          aria-hidden="true"
        />
      </div>

      <div className="px-6 pb-6">
        {/* Avatar overlapping the cover + identity */}
        <div className="-mt-11 flex items-end gap-4">
          <UserAvatar
            firstName={user.firstName}
            lastName={user.lastName}
            size="xl"
            className="border-4 border-white shadow-lg"
          />
          <div className="min-w-0 pb-0.5">
            <h2 className="truncate text-2xl font-bold tracking-tight text-gray-900">
              {user.firstName} {user.lastName}
            </h2>
            <div className="mt-2 flex flex-wrap items-center gap-2">
              <span
                className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ring-inset ${
                  isAdmin
                    ? 'bg-red-50 text-red-600 ring-red-200/70'
                    : 'bg-primary-50 text-primary-700 ring-primary-100'
                }`}
              >
                {isAdmin ? (
                  <ShieldAlert className="h-3 w-3" />
                ) : (
                  <ShieldCheck className="h-3 w-3" />
                )}
                {displayRole}
              </span>
              <StatusPill isActive={user.isActive} />
            </div>
          </div>
        </div>

        {/* Contact details */}
        <div className="mt-6 grid gap-4 border-t border-gray-100 pt-5 sm:grid-cols-2">
          <DetailRow icon={Mail} label="Email" value={user.email} />
          <DetailRow icon={Phone} label="Phone" value={user.phone || 'Not provided'} />
        </div>
      </div>
    </section>
  );
};
