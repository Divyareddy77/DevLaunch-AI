/**
 * StatusBadge — displays a study task status as a coloured badge.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Badge } from '../ui/Badge';
import { STUDY_STATUS_LABELS, type StudyStatusEnum } from '../../types/study-planner';

interface StatusBadgeProps {
  status: StudyStatusEnum;
  size?: 'sm' | 'md';
  className?: string;
}

const statusColorMap: Record<StudyStatusEnum, 'default' | 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
  PENDING: 'default',
  IN_PROGRESS: 'primary',
  COMPLETED: 'success',
};

export const StatusBadge: React.FC<StatusBadgeProps> = ({
  status,
  size = 'md',
  className = '',
}) => {
  return (
    <Badge variant={statusColorMap[status]} size={size} className={className}>
      {STUDY_STATUS_LABELS[status]}
    </Badge>
  );
};
