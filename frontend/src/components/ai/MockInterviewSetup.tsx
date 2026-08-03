/**
 * MockInterviewSetup — interview category selection for the AI Mock
 * Interview page.
 *
 * Lets the user choose one of the available interview categories
 * (HR, Java, Spring Boot, SQL, React) and start a new session.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Users, Coffee, Leaf, Database, Atom, Play, type LucideIcon } from 'lucide-react';
import { Card } from '../ui/Card';
import { Button } from '../ui/Button';
import { InterviewCategory } from '../../types/ai';

interface MockInterviewSetupProps {
  /** The currently selected category, if any. */
  selected: InterviewCategory | null;
  /** Whether a session is currently being started. */
  starting: boolean;
  /** Called when a category card is clicked. */
  onSelect: (category: InterviewCategory) => void;
  /** Called when the start button is clicked. */
  onStart: () => void;
}

/** Metadata describing each selectable interview category. */
interface CategoryMeta {
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
}

const CATEGORIES: CategoryMeta[] = [
  {
    type: InterviewCategory.HR,
    label: 'HR',
    description: 'Behavioural questions about teamwork, conflict, and career goals.',
    icon: Users,
    iconClassName: 'bg-sky-100 text-sky-600',
    ringClassName: 'ring-sky-500',
  },
  {
    type: InterviewCategory.JAVA,
    label: 'Java',
    description: 'Core Java concepts, collections, JVM, and object-oriented design.',
    icon: Coffee,
    iconClassName: 'bg-amber-100 text-amber-600',
    ringClassName: 'ring-amber-500',
  },
  {
    type: InterviewCategory.SPRING_BOOT,
    label: 'Spring Boot',
    description: 'Dependency injection, auto-configuration, and REST APIs.',
    icon: Leaf,
    iconClassName: 'bg-emerald-100 text-emerald-600',
    ringClassName: 'ring-emerald-500',
  },
  {
    type: InterviewCategory.SQL,
    label: 'SQL',
    description: 'Joins, indexing, transactions, and database design.',
    icon: Database,
    iconClassName: 'bg-violet-100 text-violet-600',
    ringClassName: 'ring-violet-500',
  },
  {
    type: InterviewCategory.REACT,
    label: 'React',
    description: 'Hooks, state management, rendering, and performance.',
    icon: Atom,
    iconClassName: 'bg-cyan-100 text-cyan-600',
    ringClassName: 'ring-cyan-500',
  },
];

export const MockInterviewSetup: React.FC<MockInterviewSetupProps> = ({
  selected,
  starting,
  onSelect,
  onStart,
}) => {
  return (
    <Card>
      <div className="space-y-5">
        <div>
          <h3 className="text-sm font-semibold text-gray-900">Choose an interview type</h3>
          <p className="mt-1 text-sm text-gray-500">
            Pick the track you want to practise — the AI will generate a
            tailored set of questions for it.
          </p>
        </div>

        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {CATEGORIES.map((category) => {
            const isSelected = selected === category.type;
            return (
              <button
                key={category.type}
                type="button"
                onClick={() => onSelect(category.type)}
                disabled={starting}
                aria-pressed={isSelected}
                className={`
                  flex items-start gap-3 rounded-xl border p-4 text-left
                  transition-all disabled:cursor-not-allowed disabled:opacity-60
                  ${
                    isSelected
                      ? `border-transparent bg-gray-50 ring-2 ${category.ringClassName}`
                      : 'border-gray-200 bg-white hover:border-gray-300 hover:bg-gray-50'
                  }
                `}
              >
                <div
                  className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-lg ${category.iconClassName}`}
                >
                  <category.icon className="h-5 w-5" />
                </div>
                <div>
                  <p className="text-sm font-semibold text-gray-900">{category.label}</p>
                  <p className="mt-0.5 text-xs leading-relaxed text-gray-500">
                    {category.description}
                  </p>
                </div>
              </button>
            );
          })}
        </div>

        <div className="flex justify-end">
          <Button
            onClick={onStart}
            loading={starting}
            disabled={!selected}
          >
            <Play className="h-4 w-4" />
            {starting ? 'Starting…' : 'Start Interview'}
          </Button>
        </div>
      </div>
    </Card>
  );
};
