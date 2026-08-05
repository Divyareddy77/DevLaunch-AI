/**
 * Navigation configuration for the sidebar menu.
 *
 * All sidebar navigation items are defined here as a single source
 * of truth. The Sidebar component renders dynamically from this
 * configuration, making it easy to reorder, add, or remove items
 * without touching component code.
 *
 * When the user's role is ADMIN, additional admin-only navigation
 * items can be appended at render time.
 *
 * @author DevLaunch
 */

import {
  LayoutDashboard,
  FileText,
  Briefcase,
  CalendarCheck,
  Github,
  Code2,
  Bot,
  FileSearch,
  User,
  Users,
  Activity,
  Megaphone,
  MessageSquare,
  Bell,
  type LucideIcon,
} from 'lucide-react';
import { ROUTES } from '../constants/routes';

/** A single navigation item displayed in the sidebar. */
export interface NavItem {
  /** The display label shown to the user. */
  label: string;
  /** The route path this item navigates to. */
  path: string;
  /** Lucide icon component rendered alongside the label. */
  icon: LucideIcon;
  /** Optional roles that restrict visibility. Omit for all authenticated users. */
  roles?: string[];
}

/** All navigation items for the main sidebar. */
export const NAV_ITEMS: NavItem[] = [
  { label: 'Dashboard', path: ROUTES.DASHBOARD, icon: LayoutDashboard },
  { label: 'Resume Builder', path: ROUTES.RESUME_LIST, icon: FileText },
  { label: 'Job Tracker', path: ROUTES.JOB_APPLICATION_LIST, icon: Briefcase },
  { label: 'Study Planner', path: ROUTES.STUDY_PLANNER_LIST, icon: CalendarCheck },
  { label: 'GitHub Analytics', path: ROUTES.GITHUB_ANALYTICS, icon: Github },
  { label: 'LeetCode Tracker', path: ROUTES.LEETCODE_TRACKER, icon: Code2 },
  { label: 'AI Mock Interview', path: ROUTES.MOCK_INTERVIEW, icon: Bot },
  { label: 'AI Resume Review', path: ROUTES.RESUME_REVIEW, icon: FileSearch },
  { label: 'Notifications', path: ROUTES.NOTIFICATIONS, icon: Bell },
  { label: 'Profile', path: ROUTES.PROFILE, icon: User },
];

/** Admin-only navigation items appended for users with the ADMIN role. */
export const ADMIN_NAV_ITEMS: NavItem[] = [
  { label: 'Admin Dashboard', path: ROUTES.ADMIN, icon: LayoutDashboard, roles: ['ADMIN'] },
  { label: 'User Management', path: ROUTES.ADMIN_USERS, icon: Users, roles: ['ADMIN'] },
  { label: 'Resumes', path: ROUTES.ADMIN_RESUMES, icon: FileText, roles: ['ADMIN'] },
  { label: 'Job Applications', path: ROUTES.ADMIN_JOB_APPLICATIONS, icon: Briefcase, roles: ['ADMIN'] },
  { label: 'Study Plans', path: ROUTES.ADMIN_STUDY_PLANS, icon: CalendarCheck, roles: ['ADMIN'] },
  { label: 'AI Reports', path: ROUTES.ADMIN_REPORTS, icon: Activity, roles: ['ADMIN'] },
  { label: 'Announcements', path: ROUTES.ADMIN_ANNOUNCEMENTS, icon: Megaphone, roles: ['ADMIN'] },
  { label: 'Feedback', path: ROUTES.ADMIN_FEEDBACK, icon: MessageSquare, roles: ['ADMIN'] },
];

/**
 * Returns the nav items appropriate for the given user role.
 * Non-admin users see only the main items; admin users see all items.
 */
export function getNavigationItems(role?: string): NavItem[] {
  if (role === 'ADMIN') {
    return [...NAV_ITEMS, ...ADMIN_NAV_ITEMS];
  }
  return NAV_ITEMS;
}
