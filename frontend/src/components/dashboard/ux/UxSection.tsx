/**
 * UxSection — the Phase 3 engagement widgets, composed into one grid.
 *
 * Brings together the Quick Actions launcher, the Recent Activity
 * timeline, the latest-notifications panel, and the AI Recommendations
 * panel so users can jump into modules and see their momentum in one
 * place.
 *
 * @author DevLaunch
 */

import React from 'react';
import { QuickActionsPanel } from './QuickActionsPanel';
import { ActivityTimeline } from './ActivityTimeline';
import { NotificationsWidget } from './NotificationsWidget';
import { AiRecommendations } from './AiRecommendations';
import type { DashboardResponse } from '../../../types/dashboard';

interface UxSectionProps {
  /** The aggregated dashboard data, used to derive recommendations. */
  data: DashboardResponse;
  /** The live unread notification count from the app context. */
  unreadCount: number;
}

export const UxSection: React.FC<UxSectionProps> = ({ data, unreadCount }) => (
  <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
    <QuickActionsPanel />
    <ActivityTimeline className="xl:col-span-2" />
    <NotificationsWidget unreadCount={unreadCount} />
    <AiRecommendations data={data} className="xl:col-span-2" />
  </div>
);
