/**
 * QuickActionsPanel — one-tap shortcuts into every core module.
 *
 * Seven glossy action tiles (resume, AI review, mock interview, jobs,
 * study, GitHub, LeetCode) with tone-matched icons and hover lift.
 *
 * @author DevLaunch
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  FileText,
  FileSearch,
  Mic,
  Briefcase,
  CalendarCheck,
  Github,
  Code2,
  Zap,
  ArrowUpRight,
  type LucideIcon,
} from 'lucide-react';
import { AnalyticsCard } from '../analytics/AnalyticsCard';
import { CARD_TONES, type CardTone } from '../cardTones';
import { ROUTES } from '../../../constants/routes';

interface QuickAction {
  label: string;
  description: string;
  icon: LucideIcon;
  tone: CardTone;
  to: string;
}

const QUICK_ACTIONS: QuickAction[] = [
  { label: 'Resume Builder', description: 'Craft your resume', icon: FileText, tone: 'info', to: ROUTES.RESUME_LIST },
  { label: 'AI Resume Review', description: 'Score & improve', icon: FileSearch, tone: 'violet', to: ROUTES.RESUME_REVIEW },
  { label: 'Mock Interview', description: 'Practice with AI', icon: Mic, tone: 'violet', to: ROUTES.MOCK_INTERVIEW },
  { label: 'Job Tracker', description: 'Manage applications', icon: Briefcase, tone: 'orange', to: ROUTES.JOB_APPLICATION_LIST },
  { label: 'Study Planner', description: 'Plan your sessions', icon: CalendarCheck, tone: 'warning', to: ROUTES.STUDY_PLANNER_LIST },
  { label: 'GitHub Sync', description: 'Connect & analyze', icon: Github, tone: 'info', to: ROUTES.GITHUB_ANALYTICS },
  { label: 'LeetCode Sync', description: 'Track your progress', icon: Code2, tone: 'danger', to: ROUTES.LEETCODE_TRACKER },
];

export const QuickActionsPanel: React.FC = () => {
  const navigate = useNavigate();

  return (
    <AnalyticsCard
      title="Quick Actions"
      subtitle="Jump straight into a module"
      icon={<Zap className="h-5 w-5" />}
      tone="primary"
    >
      <div className="grid grid-cols-2 gap-2.5">
        {QUICK_ACTIONS.map((action) => {
          const t = CARD_TONES[action.tone];
          return (
            <button
              key={action.label}
              type="button"
              onClick={() => navigate(action.to)}
              className="group flex items-center gap-2.5 rounded-xl border border-gray-100 bg-gray-50/60 px-3 py-2.5 text-left transition-all duration-200 hover:-translate-y-0.5 hover:border-gray-200 hover:bg-white hover:shadow-[0_8px_20px_-8px_rgba(16,24,40,0.18)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 focus-visible:ring-offset-1"
            >
              <span
                className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-lg transition-transform duration-200 group-hover:scale-105 ${t.chip}`}
              >
                <action.icon className="h-4 w-4" />
              </span>
              <span className="min-w-0">
                <span className="block truncate text-xs font-semibold text-gray-800">
                  {action.label}
                </span>
                <span className="block truncate text-[10px] text-gray-400">
                  {action.description}
                </span>
              </span>
              <ArrowUpRight className="ml-auto h-3.5 w-3.5 shrink-0 text-gray-300 transition-all duration-200 group-hover:-translate-y-0.5 group-hover:translate-x-0.5 group-hover:text-gray-500" />
            </button>
          );
        })}
      </div>
    </AnalyticsCard>
  );
};
