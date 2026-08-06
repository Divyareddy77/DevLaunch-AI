/**
 * Shared metadata for the AI Mock Interview module.
 *
 * Centralises the interview category cards, the setup options, and the
 * coaching tips so the landing page, setup screen, and session screen all
 * read from a single source of truth.
 *
 * @author DevLaunch
 */

import { Users, Coffee, Leaf, Database, Atom, type LucideIcon } from 'lucide-react';
import { InterviewCategory, InterviewDifficulty } from '../types/ai';

/** Display metadata for one interview category card. */
export interface InterviewCategoryMeta {
  /** The category value sent to the backend. */
  type: InterviewCategory;
  /** The display label. */
  label: string;
  /** A short description of the track. */
  description: string;
  /** Icon rendered inside the coloured circle. */
  icon: LucideIcon;
  /** Background/text classes for the icon circle. */
  iconClassName: string;
  /** Ring classes applied when the card is selected. */
  ringClassName: string;
  /** The typical difficulty mix of the track. */
  difficultyLabel: string;
  /** Minimum recommended answer length in words. */
  minWords: number;
}

/** Metadata for every selectable interview category. */
export const INTERVIEW_CATEGORY_META: Record<InterviewCategory, InterviewCategoryMeta> = {
  [InterviewCategory.HR]: {
    type: InterviewCategory.HR,
    label: 'HR',
    description: 'Behavioural questions about teamwork, conflict, and career goals.',
    icon: Users,
    iconClassName: 'bg-sky-100 text-sky-600',
    ringClassName: 'ring-sky-500',
    difficultyLabel: 'Mixed',
    minWords: 40,
  },
  [InterviewCategory.JAVA]: {
    type: InterviewCategory.JAVA,
    label: 'Java',
    description: 'Core Java concepts, collections, JVM, and object-oriented design.',
    icon: Coffee,
    iconClassName: 'bg-amber-100 text-amber-600',
    ringClassName: 'ring-amber-500',
    difficultyLabel: 'Easy to Hard',
    minWords: 30,
  },
  [InterviewCategory.SPRING_BOOT]: {
    type: InterviewCategory.SPRING_BOOT,
    label: 'Spring Boot',
    description: 'Dependency injection, auto-configuration, and REST APIs.',
    icon: Leaf,
    iconClassName: 'bg-emerald-100 text-emerald-600',
    ringClassName: 'ring-emerald-500',
    difficultyLabel: 'Easy to Hard',
    minWords: 30,
  },
  [InterviewCategory.SQL]: {
    type: InterviewCategory.SQL,
    label: 'SQL',
    description: 'Joins, indexing, transactions, and database design.',
    icon: Database,
    iconClassName: 'bg-violet-100 text-violet-600',
    ringClassName: 'ring-violet-500',
    difficultyLabel: 'Easy to Hard',
    minWords: 30,
  },
  [InterviewCategory.REACT]: {
    type: InterviewCategory.REACT,
    label: 'React',
    description: 'Hooks, state management, rendering, and performance.',
    icon: Atom,
    iconClassName: 'bg-cyan-100 text-cyan-600',
    ringClassName: 'ring-cyan-500',
    difficultyLabel: 'Easy to Hard',
    minWords: 30,
  },
};

/** The ordered list of categories for the landing page grid. */
export const INTERVIEW_CATEGORIES: InterviewCategory[] = [
  InterviewCategory.HR,
  InterviewCategory.JAVA,
  InterviewCategory.SPRING_BOOT,
  InterviewCategory.SQL,
  InterviewCategory.REACT,
];

/** The supported interview lengths (in questions). */
export const INTERVIEW_LENGTHS: number[] = [5, 10, 15];

/** The supported difficulty modes with display labels. */
export const INTERVIEW_DIFFICULTIES: Array<{
  value: InterviewDifficulty;
  label: string;
  description: string;
}> = [
  { value: InterviewDifficulty.EASY, label: 'Easy', description: 'Foundational definitions and core concepts.' },
  { value: InterviewDifficulty.MEDIUM, label: 'Medium', description: 'Explanations, comparisons, and applied concepts.' },
  { value: InterviewDifficulty.HARD, label: 'Hard', description: 'Trade-offs, design reasoning, and depth.' },
  { value: InterviewDifficulty.MIXED, label: 'Mixed', description: 'A balanced mix of all difficulty levels.' },
];

/** Approximate minutes per question used for duration estimates. */
export const MINUTES_PER_QUESTION = 2.5;

/** Coaching tips shown on the interview setup screen. */
export const INTERVIEW_TIPS: string[] = [
  'Structure answers with the STAR method: Situation, Task, Action, Result.',
  'Aim for 100–140 words per minute — steady and clear beats rushed.',
  'Use concrete examples from real projects instead of generic definitions.',
  'Read the question twice and cover every part of it before answering.',
  'A strong answer is 60–120 seconds — long enough to be specific, short enough to stay sharp.',
  'If you get stuck, say what you know first, then think out loud.',
];

/** Number of seconds allowed per question in timed mode. */
export const TIMED_QUESTION_SECONDS = 180;

/** Local storage prefix used to auto-save answers mid-interview. */
export const INTERVIEW_DRAFT_PREFIX = 'mock-interview-draft:';
