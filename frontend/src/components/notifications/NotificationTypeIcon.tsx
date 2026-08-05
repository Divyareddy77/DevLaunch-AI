/**
 * NotificationTypeIcon — the icon and accent colour for a notification
 * category.
 *
 * Shared by the header bell dropdown and the notifications page so every
 * category renders consistently.
 *
 * @author DevLaunch
 */

import React from 'react';
import {
  Megaphone,
  FileText,
  Briefcase,
  CalendarCheck,
  Bot,
  FileSearch,
  Info,
  type LucideIcon,
} from 'lucide-react';
import type { NotificationTypeEnum } from '../../types/notification';

interface TypeMeta {
  icon: LucideIcon;
  /** Tailwind classes for the icon tile background. */
  tile: string;
  /** Tailwind classes for the icon itself. */
  iconClass: string;
}

const TYPE_META: Record<NotificationTypeEnum, TypeMeta> = {
  ANNOUNCEMENT: {
    icon: Megaphone,
    tile: 'bg-indigo-100',
    iconClass: 'text-indigo-600',
  },
  RESUME: {
    icon: FileText,
    tile: 'bg-blue-100',
    iconClass: 'text-blue-600',
  },
  JOB: {
    icon: Briefcase,
    tile: 'bg-emerald-100',
    iconClass: 'text-emerald-600',
  },
  STUDY: {
    icon: CalendarCheck,
    tile: 'bg-amber-100',
    iconClass: 'text-amber-600',
  },
  MOCK_INTERVIEW: {
    icon: Bot,
    tile: 'bg-violet-100',
    iconClass: 'text-violet-600',
  },
  RESUME_REVIEW: {
    icon: FileSearch,
    tile: 'bg-rose-100',
    iconClass: 'text-rose-600',
  },
  SYSTEM: {
    icon: Info,
    tile: 'bg-gray-100',
    iconClass: 'text-gray-600',
  },
};

interface NotificationTypeIconProps {
  /** The notification category to render. */
  type: NotificationTypeEnum;
  /** Optional size override — defaults to a 9x9 tile with a 4x4 icon. */
  size?: 'sm' | 'md';
}

export const NotificationTypeIcon: React.FC<NotificationTypeIconProps> = ({
  type,
  size = 'md',
}) => {
  const meta = TYPE_META[type] ?? TYPE_META.SYSTEM;
  const Icon = meta.icon;

  const tileClass =
    size === 'sm' ? 'h-8 w-8 rounded-lg' : 'h-9 w-9 rounded-lg';
  const iconClass = size === 'sm' ? 'h-4 w-4' : 'h-5 w-5';

  return (
    <div
      className={`flex flex-shrink-0 items-center justify-center ${tileClass} ${meta.tile}`}
    >
      <Icon className={`${iconClass} ${meta.iconClass}`} />
    </div>
  );
};
