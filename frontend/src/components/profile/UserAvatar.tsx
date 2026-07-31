/**
 * UserAvatar — an initials-based avatar derived from the user's name.
 *
 * The backend does not expose an avatar image, so the avatar is built
 * from the user's first and last name initials on a brand gradient.
 *
 * @author DevLaunch
 */

import React from 'react';

type UserAvatarSize = 'sm' | 'md' | 'lg';

interface UserAvatarProps {
  /** The user's first name. */
  firstName: string;
  /** The user's last name. */
  lastName: string;
  /** Avatar size. */
  size?: UserAvatarSize;
  /** Optional additional CSS classes (e.g. a border for overlapping layouts). */
  className?: string;
}

const sizeStyles: Record<UserAvatarSize, string> = {
  sm: 'h-9 w-9 text-xs',
  md: 'h-14 w-14 text-lg',
  lg: 'h-24 w-24 text-2xl',
};

export const UserAvatar: React.FC<UserAvatarProps> = ({
  firstName,
  lastName,
  size = 'md',
  className = '',
}) => {
  const initials =
    `${firstName.charAt(0)}${lastName.charAt(0)}`.trim().toUpperCase() || '?';

  return (
    <div
      aria-hidden="true"
      className={`flex flex-shrink-0 select-none items-center justify-center rounded-full bg-gradient-to-br from-primary-500 to-primary-700 font-bold text-white shadow-sm ${sizeStyles[size]} ${className}`}
    >
      {initials}
    </div>
  );
};
