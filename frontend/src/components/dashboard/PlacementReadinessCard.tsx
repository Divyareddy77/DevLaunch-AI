/**
 * PlacementReadinessCard — the enhanced Placement Readiness section of
 * the dashboard.
 *
 * The centrepiece of the redesigned dashboard: a large animated progress
 * ring with a count-up score, colour-coded status badge, previous-vs-current
 * score tracking, strongest/weakest area, next goal, per-module breakdown,
 * and personalized recommendations. All data comes from the backend
 * DashboardServiceImpl — no calculations are duplicated here.
 *
 * @see backend/src/main/java/com/devlaunch/service/impl/DashboardServiceImpl.java
 * @author DevLaunch
 */

import React from 'react';
import {
  ShieldCheck,
  FileText,
  Mic,
  Github,
  Code2,
  CalendarCheck,
  Briefcase,
  CheckCircle2,
  AlertTriangle,
  Target,
  ArrowRight,
  TrendingUp,
  TrendingDown,
  Minus,
  type LucideIcon,
} from 'lucide-react';
import { format, isToday, isValid, parseISO } from 'date-fns';
import { Badge } from '../ui/Badge';
import { useCountUp } from '../../hooks/useCountUp';
import { CARD_TONES, type CardTone } from './cardTones';
import type {
  DashboardResponse,
  ReadinessModule,
  ReadinessModuleKey,
} from '../../types/dashboard';

interface LevelStyle {
  /** The level label. */
  label: string;
  /** Text colour for the score. */
  text: string;
  /** Badge colour classes. */
  badge: string;
  /** Ring stroke start colour. */
  ring: string;
  /** Ring stroke gradient end colour. */
  ringTo: string;
  /** Progress bar colour classes. */
  bar: string;
}

/**
 * Level styling keyed by the backend-provided readiness status label.
 *
 * The status string is the single source of truth (computed by
 * DashboardServiceImpl); the frontend only maps labels to colours, so the
 * badge, ring, and text can never drift from the backend thresholds.
 */
const LEVEL_STYLES: Record<string, LevelStyle> = {
  Excellent: {
    label: 'Excellent',
    text: 'text-emerald-600',
    badge: 'bg-emerald-100 text-emerald-700',
    ring: '#10b981',
    ringTo: '#34d399',
    bar: 'bg-emerald-500',
  },
  'Placement Ready': {
    label: 'Placement Ready',
    text: 'text-indigo-600',
    badge: 'bg-indigo-100 text-indigo-700',
    ring: '#6366f1',
    ringTo: '#a78bfa',
    bar: 'bg-indigo-500',
  },
  Improving: {
    label: 'Improving',
    text: 'text-amber-600',
    badge: 'bg-amber-100 text-amber-700',
    ring: '#f59e0b',
    ringTo: '#fbbf24',
    bar: 'bg-amber-500',
  },
  'Needs Improvement': {
    label: 'Needs Improvement',
    text: 'text-red-600',
    badge: 'bg-red-100 text-red-700',
    ring: '#ef4444',
    ringTo: '#f87171',
    bar: 'bg-red-500',
  },
};

/** Fallback style used when the backend status label is missing. */
const FALLBACK_LEVEL_STYLE = LEVEL_STYLES['Needs Improvement'];

/** Resolves the level styling for a readiness status label. */
function levelStyleFor(status: string | null | undefined): LevelStyle {
  return (status && LEVEL_STYLES[status]) || FALLBACK_LEVEL_STYLE;
}

/** Maps a level label to the reusable Badge variant. */
function badgeVariantFor(label: string): 'success' | 'primary' | 'warning' | 'danger' {
  switch (label) {
    case 'Excellent':
      return 'success';
    case 'Placement Ready':
      return 'primary';
    case 'Improving':
      return 'warning';
    default:
      return 'danger';
  }
}

/** Icon for each module in the breakdown. */
const MODULE_ICONS: Record<ReadinessModuleKey, LucideIcon> = {
  RESUME_ATS: ShieldCheck,
  RESUME_COMPLETION: FileText,
  MOCK_INTERVIEW: Mic,
  GITHUB: Github,
  LEETCODE: Code2,
  STUDY_PLANNER: CalendarCheck,
  JOB_APPLICATIONS: Briefcase,
};

/** Icon accent colour for each module. */
const MODULE_ICON_TONE: Record<ReadinessModuleKey, string> = {
  RESUME_ATS: 'text-rose-600',
  RESUME_COMPLETION: 'text-blue-600',
  MOCK_INTERVIEW: 'text-violet-600',
  GITHUB: 'text-gray-700',
  LEETCODE: 'text-orange-600',
  STUDY_PLANNER: 'text-amber-600',
  JOB_APPLICATIONS: 'text-emerald-600',
};

/** Progress bar colour for a module's normalized score. */
function moduleBarTone(score: number): string {
  if (score >= 75) return 'bg-emerald-500';
  if (score >= 50) return 'bg-indigo-500';
  if (score >= 25) return 'bg-amber-500';
  return 'bg-red-500';
}

/**
 * Formats the last-updated timestamp as "Today" when it matches the
 * current date, otherwise as a short date.
 */
function lastUpdatedLabel(iso: string | null | undefined): string {
  if (!iso) return 'Not tracked yet';
  const date = parseISO(iso);
  if (!isValid(date)) return '—';
  if (isToday(date)) return 'Today';
  return format(date, 'MMM d, yyyy');
}

/**
 * SVG circular progress ring with the score in the centre. The arc uses a
 * two-stop gradient derived from the readiness level and animates smoothly
 * because it is driven by the count-up score value.
 */
const ProgressRing: React.FC<{
  score: number;
  level: LevelStyle;
  size?: number;
  strokeWidth?: number;
}> = ({ score, level, size = 172, strokeWidth = 14 }) => {
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference - (score / 100) * circumference;
  const gradientId = `ring-grad-${level.ring.replace('#', '')}`;

  return (
    <div className="relative inline-flex items-center justify-center">
      <svg width={size} height={size} className="-rotate-90">
        <defs>
          <linearGradient id={gradientId} x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stopColor={level.ring} />
            <stop offset="100%" stopColor={level.ringTo} />
          </linearGradient>
        </defs>
        {/* Background circle */}
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke="#eef0f4"
          strokeWidth={strokeWidth}
        />
        {/* Progress arc */}
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke={`url(#${gradientId})`}
          strokeWidth={strokeWidth}
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
        />
      </svg>
      <div className="absolute flex flex-col items-center">
        <span className={`text-5xl font-bold tracking-tight ${level.text}`}>
          {Math.round(score)}
        </span>
        <span className="text-[10px] font-medium uppercase tracking-wider text-gray-400">
          / 100
        </span>
      </div>
    </div>
  );
};

/** Small chip showing the score change since the previous measurement. */
const DeltaChip: React.FC<{ change: number }> = ({ change }) => {
  if (change > 0) {
    return (
      <span className="inline-flex items-center gap-0.5 rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-bold text-emerald-700">
        <TrendingUp className="h-3 w-3" />
        +{change}
      </span>
    );
  }
  if (change < 0) {
    return (
      <span className="inline-flex items-center gap-0.5 rounded-full bg-red-100 px-2 py-0.5 text-xs font-bold text-red-700">
        <TrendingDown className="h-3 w-3" />
        {change}
      </span>
    );
  }
  return (
    <span className="inline-flex items-center gap-0.5 rounded-full bg-gray-100 px-2 py-0.5 text-xs font-bold text-gray-600">
      <Minus className="h-3 w-3" />
      No change
    </span>
  );
};

/** Small labelled tile with an icon, used in the summary row. */
const StatTile: React.FC<{
  label: string;
  value: string | null;
  icon: LucideIcon;
  tone: CardTone;
}> = ({ label, value, icon: Icon, tone }) => {
  const t = CARD_TONES[tone];
  return (
    <div className="rounded-xl border border-gray-100 bg-white/80 px-3.5 py-3 shadow-[0_1px_2px_rgba(16,24,40,0.04)]">
      <div className="flex items-center gap-2">
        <span className={`flex h-7 w-7 shrink-0 items-center justify-center rounded-lg ${t.soft}`}>
          <Icon className={`h-3.5 w-3.5 ${t.text}`} />
        </span>
        <p className="truncate text-[10px] font-semibold uppercase tracking-wide text-gray-400">
          {label}
        </p>
      </div>
      <p className="mt-1.5 line-clamp-2 text-sm font-medium leading-snug text-gray-800">
        {value ?? '—'}
      </p>
    </div>
  );
};

/** One row of the per-module breakdown. */
const ModuleRow: React.FC<{ module: ReadinessModule }> = ({ module }) => {
  const Icon = MODULE_ICONS[module.key] ?? Target;
  const iconTone = MODULE_ICON_TONE[module.key] ?? 'text-gray-500';

  return (
    <div className="flex items-center gap-3">
      <span
        className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-gray-50 ring-1 ring-inset ring-gray-100 ${iconTone}`}
      >
        <Icon className="h-4 w-4" />
      </span>
      <div className="min-w-0 flex-1">
        <div className="flex items-baseline justify-between gap-2">
          <span className="truncate text-sm font-medium text-gray-700">{module.label}</span>
          <span className="shrink-0 text-sm font-semibold text-gray-900">{module.value}</span>
        </div>
        <div className="mt-1.5 h-1.5 w-full overflow-hidden rounded-full bg-gray-100">
          <div
            className={`h-full rounded-full ${moduleBarTone(module.score)} transition-all duration-700`}
            style={{ width: `${module.score}%` }}
          />
        </div>
      </div>
    </div>
  );
};

type InsightTone = 'success' | 'warning' | 'primary';

interface InsightCardProps {
  /** The section title. */
  title: string;
  /** Header icon. */
  icon: LucideIcon;
  /** Icon used as the list bullet. */
  bullet: LucideIcon;
  /** Tone controlling the colour accents. */
  tone: InsightTone;
  /** The items to list. */
  items: string[];
  /** Message shown when the list is empty. */
  emptyText: string;
  /** Whether to number the items instead of using a bullet icon. */
  numbered?: boolean;
}

const INSIGHT_TONES: Record<InsightTone, { tile: string; icon: string; badge: string }> = {
  success: { tile: 'bg-emerald-50', icon: 'text-emerald-600', badge: 'bg-emerald-100 text-emerald-700' },
  warning: { tile: 'bg-amber-50', icon: 'text-amber-600', badge: 'bg-amber-100 text-amber-700' },
  primary: { tile: 'bg-indigo-50', icon: 'text-indigo-600', badge: 'bg-indigo-100 text-indigo-700' },
};

/** A strengths / improvement areas / recommendations column. */
const InsightCard: React.FC<InsightCardProps> = ({
  title,
  icon: Icon,
  bullet: BulletIcon,
  tone,
  items,
  emptyText,
  numbered = false,
}) => {
  const t = INSIGHT_TONES[tone];

  return (
    <div className="rounded-2xl border border-gray-100 bg-gray-50/60 p-4">
      <div className="flex items-center gap-2">
        <span className={`flex h-8 w-8 items-center justify-center rounded-lg ${t.tile} ${t.icon}`}>
          <Icon className="h-4 w-4" />
        </span>
        <h4 className="text-sm font-semibold text-gray-800">{title}</h4>
      </div>

      {items.length === 0 ? (
        <p className="mt-3 text-xs text-gray-400">{emptyText}</p>
      ) : (
        <ul className="mt-3 space-y-2">
          {items.map((item, index) => (
            <li key={item} className="flex items-start gap-2 text-sm">
              {numbered ? (
                <span
                  className={`mt-0.5 flex h-4 w-4 shrink-0 items-center justify-center rounded-full ${t.badge} text-[10px] font-bold`}
                >
                  {index + 1}
                </span>
              ) : (
                <BulletIcon className={`mt-0.5 h-3.5 w-3.5 shrink-0 ${t.icon}`} />
              )}
              <span className="leading-relaxed text-gray-600">{item}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

interface PlacementReadinessCardProps {
  /** The aggregated dashboard data containing the readiness summary. */
  data: DashboardResponse;
}

export const PlacementReadinessCard: React.FC<PlacementReadinessCardProps> = ({ data }) => {
  const level = levelStyleFor(data.readinessStatus);
  const hasPrevious = data.readinessPrevious !== null && data.readinessChange !== null;
  const animatedScore = useCountUp(data.placementReadiness, 1100);

  return (
    <div className="overflow-hidden rounded-2xl border border-gray-100 bg-white shadow-[0_1px_3px_rgba(16,24,40,0.06)]">
      {/* ---- Summary header ---- */}
      <div className="border-b border-gray-100 bg-gradient-to-br from-indigo-50/70 via-white to-violet-50/60 px-6 py-7 sm:px-8">
        <div className="flex flex-col items-center gap-8 lg:flex-row lg:items-center">
          {/* Score ring — the centrepiece */}
          <div className="flex shrink-0 flex-col items-center lg:pr-8">
            <ProgressRing score={animatedScore} level={level} />
            <div className="mt-4 flex flex-wrap items-center justify-center gap-2">
              <Badge variant={badgeVariantFor(level.label)}>
                {data.readinessStatus ?? level.label}
              </Badge>
              {hasPrevious && <DeltaChip change={data.readinessChange ?? 0} />}
            </div>
            <p className="mt-2 text-[11px] font-medium text-gray-400">
              {data.readinessUpdatedAt ? `Updated ${lastUpdatedLabel(data.readinessUpdatedAt)}` : 'Progress tracked over time'}
            </p>
          </div>

          {/* Summary facts */}
          <div className="min-w-0 flex-1 text-center lg:text-left">
            <div className="flex flex-wrap items-center justify-center gap-2.5 lg:justify-start">
              <h2 className="text-xl font-semibold tracking-tight text-gray-900">
                Placement Readiness
              </h2>
            </div>
            <p className="mx-auto mt-1.5 max-w-xl text-sm leading-relaxed text-gray-500 lg:mx-0">
              Your overall score combines resume completeness, job applications, study progress,
              GitHub presence, and LeetCode activity.
            </p>

            {/* Progress tracking vs previous score */}
            <div className="mt-4">
              {hasPrevious ? (
                <div className="inline-flex flex-wrap items-center gap-x-3 gap-y-1 rounded-xl border border-gray-100 bg-white px-3.5 py-2.5 shadow-[0_1px_2px_rgba(16,24,40,0.04)]">
                  <span className="text-xs font-medium text-gray-500">Previous</span>
                  <span className="text-sm font-bold text-gray-900">{data.readinessPrevious}</span>
                  <ArrowRight className="h-3.5 w-3.5 text-gray-300" />
                  <span className="text-xs font-medium text-gray-500">Now</span>
                  <span className={`text-sm font-bold ${level.text}`}>{data.placementReadiness}</span>
                </div>
              ) : (
                <p className="text-xs text-gray-400">
                  Complete more activities across modules to start tracking your progress over time.
                </p>
              )}
            </div>

            {/* Strongest / weakest / next goal */}
            <div className="mt-6 grid grid-cols-1 gap-3 sm:grid-cols-3">
              <StatTile
                label="Strongest Area"
                value={data.readinessStrongestArea}
                icon={TrendingUp}
                tone="success"
              />
              <StatTile
                label="Weakest Area"
                value={data.readinessWeakestArea}
                icon={TrendingDown}
                tone="warning"
              />
              <StatTile
                label="Next Goal"
                value={data.readinessNextGoal}
                icon={Target}
                tone="primary"
              />
            </div>
          </div>
        </div>
      </div>

      {/* ---- Module breakdown ---- */}
      <div className="border-b border-gray-100 px-6 py-5 sm:px-8">
        <h3 className="text-sm font-semibold text-gray-800">Module Breakdown</h3>
        <p className="mt-0.5 text-xs text-gray-500">
          Your contribution from each platform module, using live data
        </p>
        <div className="mt-4 grid gap-x-10 gap-y-4 sm:grid-cols-2">
          {data.readinessModules.map((module) => (
            <ModuleRow key={module.key} module={module} />
          ))}
        </div>
      </div>

      {/* ---- Insights ---- */}
      <div className="grid gap-5 px-6 py-5 sm:px-8 lg:grid-cols-3">
        <InsightCard
          title="Strengths"
          tone="success"
          icon={CheckCircle2}
          bullet={CheckCircle2}
          items={data.readinessStrengths}
          emptyText="Complete activities across modules to build your strengths."
        />
        <InsightCard
          title="Improvement Areas"
          tone="warning"
          icon={AlertTriangle}
          bullet={AlertTriangle}
          items={data.readinessImprovements}
          emptyText="You're on track — no obvious gaps detected."
        />
        <InsightCard
          title="Recommendations"
          tone="primary"
          icon={Target}
          bullet={ArrowRight}
          items={data.readinessRecommendations}
          numbered
          emptyText="Great job — you're fully prepared. Keep it up!"
        />
      </div>
    </div>
  );
};
