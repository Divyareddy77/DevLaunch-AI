/**
 * PriorityBadge — displays an application priority as a coloured badge.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Badge } from '../ui/Badge';
import {
  APPLICATION_PRIORITY_LABELS,
  type ApplicationPriorityEnum,
} from '../../types/job-application';

interface PriorityBadgeProps {
  /** The priority level to display. */
  priority: ApplicationPriorityEnum;
  /** Optional size variant. */
  size?: 'sm' | 'md';
  /** Optional additional CSS classes. */
  className?: string;
}

const priorityColorMap: Record<
  ApplicationPriorityEnum,
  'default' | 'primary' | 'warning' | 'danger'
> = {
  LOW: 'default',
  MEDIUM: 'primary',
  HIGH: 'warning',
  URGENT: 'danger',
};

export const PriorityBadge: React.FC<PriorityBadgeProps> = ({
  priority,
  size = 'sm',
  className = '',
}) => {
  return (
    <Badge variant={priorityColorMap[priority] ?? 'default'} size={size} className={className}>
      {APPLICATION_PRIORITY_LABELS[priority] ?? 'Medium'}
    </Badge>
  );
};
