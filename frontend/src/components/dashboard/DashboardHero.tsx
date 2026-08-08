/**
 * DashboardHero — the premium welcome banner of the dashboard.
 *
 * Replaces the previous plain welcome header with a gradient hero card
 * that surfaces the user's level, XP, latest badge, practice streak,
 * today's goal, and placement readiness at a glance, plus four quick
 * action buttons that jump straight into the core modules.
 *
 * All data comes from existing API responses (dashboard, achievements,
 * interview history); failures of the supplementary data are silent.
 *
 * @author DevLaunch
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Trophy,
  Zap,
  Award,
  Flame,
  Target,
  TrendingUp,
  FileText,
  Mic,
  Briefcase,
  CalendarCheck,
  ArrowRight,
  type LucideIcon,
} from 'lucide-react';
import { APP } from '../../constants/app';
import { ROUTES } from '../../constants/routes';
import type { DashboardResponse } from '../../types/dashboard';
import type { AchievementSummary } from '../../types/achievement';

interface DashboardHeroProps {
  /** The authenticated user's first name. */
  firstName?: string;
  /** The aggregated dashboard data (readiness score + today's goal). */
  data: DashboardResponse;
  /** The gamification summary (level, XP, badge), or null while loading. */
  summary: AchievementSummary | null;
  /** The current daily practice streak in days, or null when unavailable. */
  streak: number | null;
}

/** The four quick action shortcuts rendered at the bottom of the hero. */
const QUICK_ACTIONS: { label: string; description: string; icon: LucideIcon; to: string }[] = [
  { label: 'Resume Builder', description: 'Craft your resume', icon: FileText, to: ROUTES.RESUME_LIST },
  { label: 'AI Mock Interview', description: 'Practice with AI', icon: Mic, to: ROUTES.MOCK_INTERVIEW },
  { label: 'Job Tracker', description: 'Manage applications', icon: Briefcase, to: ROUTES.JOB_APPLICATION_LIST },
  { label: 'Study Planner', description: 'Plan your sessions', icon: CalendarCheck, to: ROUTES.STUDY_PLANNER_LIST },
];

/** Returns a friendly greeting based on the current time of day. */
function timeOfDayGreeting(): string {
  const hour = new Date().getHours();
  if (hour < 12) return 'Good morning';
  if (hour < 17) return 'Good afternoon';
  return 'Good evening';
}

/** A small frosted pill chip used for hero stat badges. */
const HeroChip: React.FC<{ icon: LucideIcon; children: React.ReactNode }> = ({
  icon: Icon,
  children,
}) => (
  <span className="inline-flex items-center gap-1.5 rounded-full bg-white/10 px-3 py-1.5 text-xs font-semibold text-white ring-1 ring-inset ring-white/15 backdrop-blur-sm">
    <Icon className="h-3.5 w-3.5 text-indigo-200" />
    {children}
  </span>
);

export const DashboardHero: React.FC<DashboardHeroProps> = ({
  firstName = 'User',
  data,
  summary,
  streak,
}) => {
  const navigate = useNavigate();
  const status = data.readinessStatus ?? 'Getting started';

  return (
    <section className="relative overflow-hidden rounded-[20px] bg-gradient-to-br from-indigo-600 via-violet-600 to-purple-700 p-6 text-white shadow-xl sm:p-8">
      {/* Decorative gradient blobs + subtle grid pattern */}
      <div className="hero-grid-pattern pointer-events-none absolute inset-0" />
      <div className="animate-float-slow pointer-events-none absolute -right-20 -top-24 h-64 w-64 rounded-full bg-fuchsia-400/25 blur-3xl" />
      <div className="pointer-events-none absolute -bottom-28 -left-16 h-72 w-72 rounded-full bg-indigo-300/20 blur-3xl" />
      <div className="pointer-events-none absolute right-1/3 top-1/2 h-40 w-40 rounded-full bg-white/10 blur-3xl" />

      <div className="relative">
        <div className="flex flex-col gap-8 lg:flex-row lg:items-center lg:justify-between">
          {/* Greeting + stats */}
          <div className="min-w-0 flex-1">
            <p className="text-xs font-semibold uppercase tracking-widest text-indigo-200">
              {APP.NAME} · Career Hub
            </p>
            <h1 className="mt-2 text-2xl font-bold tracking-tight sm:text-3xl lg:text-4xl">
              Welcome back, {firstName} 👋
            </h1>
            <p className="mt-2 max-w-xl text-sm leading-relaxed text-indigo-100">
              {timeOfDayGreeting()}! Here&apos;s your career preparation snapshot — let&apos;s keep
              building momentum.
            </p>

            {/* Level / XP / badge / streak chips */}
            <div className="mt-5 flex flex-wrap gap-2">
              {summary && (
                <HeroChip icon={Trophy}>
                  Level {summary.level} · {summary.levelTitle}
                </HeroChip>
              )}
              {summary && <HeroChip icon={Zap}>{summary.totalXp.toLocaleString()} XP</HeroChip>}
              {summary?.latestUnlock && (
                <HeroChip icon={Award}>{summary.latestUnlock.title}</HeroChip>
              )}
              {streak !== null && (
                <HeroChip icon={Flame}>
                  {streak} {streak === 1 ? 'day' : 'days'} streak
                </HeroChip>
              )}
            </div>

            {/* Today's goal */}
            {data.readinessNextGoal && (
              <div className="mt-5 flex max-w-xl items-start gap-3 rounded-xl bg-white/10 p-3.5 ring-1 ring-inset ring-white/15 backdrop-blur-sm">
                <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-white/15">
                  <Target className="h-4 w-4 text-amber-300" />
                </span>
                <div className="min-w-0">
                  <p className="text-[11px] font-semibold uppercase tracking-wide text-indigo-200">
                    Today&apos;s Goal
                  </p>
                  <p className="mt-0.5 text-sm font-medium leading-snug text-white">
                    {data.readinessNextGoal}
                  </p>
                </div>
              </div>
            )}
          </div>

          {/* Placement readiness mini panel */}
          <div className="w-full shrink-0 lg:w-64">
            <div className="rounded-2xl bg-white/10 p-5 ring-1 ring-inset ring-white/15 backdrop-blur-md">
              <div className="flex items-center justify-between">
                <p className="text-[11px] font-semibold uppercase tracking-wider text-indigo-200">
                  Placement Readiness
                </p>
                <TrendingUp className="h-4 w-4 text-emerald-300" />
              </div>
              <p className="mt-2 text-4xl font-bold tracking-tight">
                {data.placementReadiness}
                <span className="ml-1 text-lg font-medium text-indigo-200">/100</span>
              </p>
              <div className="mt-3 h-1.5 overflow-hidden rounded-full bg-white/20">
                <div
                  className="h-full rounded-full bg-gradient-to-r from-emerald-300 to-emerald-400 transition-all duration-1000 ease-out"
                  style={{ width: `${Math.min(100, Math.max(0, data.placementReadiness))}%` }}
                />
              </div>
              <p className="mt-2 text-xs font-medium text-indigo-100">{status}</p>
            </div>
          </div>
        </div>

        {/* Quick actions */}
        <div className="mt-8 grid grid-cols-2 gap-3 sm:grid-cols-4">
          {QUICK_ACTIONS.map((action) => (
            <button
              key={action.label}
              type="button"
              onClick={() => navigate(action.to)}
              className="group flex items-center gap-3 rounded-xl bg-white/10 px-4 py-3.5 text-left ring-1 ring-inset ring-white/15 backdrop-blur-sm transition-all duration-300 hover:-translate-y-0.5 hover:bg-white hover:shadow-lg"
            >
              <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-white/15 text-white transition-colors duration-300 group-hover:bg-indigo-50 group-hover:text-indigo-600">
                <action.icon className="h-5 w-5" />
              </span>
              <span className="min-w-0">
                <span className="block truncate text-sm font-semibold text-white transition-colors duration-300 group-hover:text-gray-900">
                  {action.label}
                </span>
                <span className="block truncate text-[11px] text-indigo-100 transition-colors duration-300 group-hover:text-gray-500">
                  {action.description}
                </span>
              </span>
              <ArrowRight className="ml-auto h-4 w-4 shrink-0 text-indigo-200 transition-all duration-300 group-hover:translate-x-0.5 group-hover:text-indigo-500" />
            </button>
          ))}
        </div>
      </div>
    </section>
  );
};
