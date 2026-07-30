/**
 * PriorityBadge — displays a study task priority as a coloured badge.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Badge } from '../ui/Badge';
import { STUDY_PRIORITY_LABELS, type StudyPriorityEnum } from '../../types/study-planner';

interface PriorityBadgeProps {
  priority: StudyPriorityEnum;
  size?: 'sm' | 'md';
  className?: string;
}

const priorityColorMap: Record<StudyPriorityEnum, 'default' | 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
  LOW: 'default',
  MEDIUM: 'warning',
  HIGH: 'danger',
};

export const PriorityBadge: React.FC<PriorityBadgeProps> = ({
  priority,
  size = 'md',
  className = '',
}) => {
  return (
    <Badge variant={priorityColorMap[priority]} size={size} className={className}>
      {STUDY_PRIORITY_LABELS[priority]}
    </Badge>
  );
};
