/**
 * StatusBadge — displays a job application status as a coloured badge.
 *
 * Each status gets a unique colour that helps users quickly scan
 * the hiring pipeline at a glance.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Badge } from '../ui/Badge';
import { APPLICATION_STATUS_LABELS, type ApplicationStatusEnum } from '../../types/job-application';

interface StatusBadgeProps {
  /** The application status to display. */
  status: ApplicationStatusEnum;
  /** Optional size variant. */
  size?: 'sm' | 'md';
  /** Optional additional CSS classes. */
  className?: string;
}

const statusColorMap: Record<ApplicationStatusEnum, 'default' | 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
  WISHLIST: 'default',
  APPLIED: 'primary',
  ASSESSMENT: 'warning',
  INTERVIEW: 'info',
  OFFER: 'success',
  REJECTED: 'danger',
};

export const StatusBadge: React.FC<StatusBadgeProps> = ({
  status,
  size = 'md',
  className = '',
}) => {
  return (
    <Badge variant={statusColorMap[status]} size={size} className={className}>
      {APPLICATION_STATUS_LABELS[status]}
    </Badge>
  );
};
