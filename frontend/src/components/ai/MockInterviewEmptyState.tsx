/**
 * MockInterviewEmptyState — the landing hero for users with no interview
 * history yet.
 *
 * Shows an illustration, the benefits of mock interviewing, the estimated
 * session duration, and a call to action to start the first interview.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Mic, Clock, Target, LineChart, MessageSquare, Play } from 'lucide-react';
import { Button } from '../ui/Button';

interface MockInterviewEmptyStateProps {
  /** Called when the user starts their first interview. */
  onStart: () => void;
}

const BENEFITS: Array<{ icon: typeof Mic; title: string; text: string }> = [
  {
    icon: Target,
    title: 'Real interview questions',
    text: 'AI-generated questions across HR, Java, Spring Boot, SQL, and React tracks.',
  },
  {
    icon: Mic,
    title: 'Practise out loud',
    text: 'Answer by voice with live speaking analysis, or type — your choice.',
  },
  {
    icon: LineChart,
    title: 'Instant AI feedback',
    text: 'Score every answer and get personalised suggestions to improve.',
  },
  {
    icon: MessageSquare,
    title: 'Track your progress',
    text: 'Build streaks, review past reports, and watch your scores trend upward.',
  },
];

export const MockInterviewEmptyState: React.FC<MockInterviewEmptyStateProps> = ({
  onStart,
}) => {
  return (
    <div className="overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm">
      <div className="grid gap-8 p-8 lg:grid-cols-2 lg:items-center lg:p-12">
        {/* Illustration */}
        <div className="flex flex-col items-center text-center lg:items-start lg:text-left">
          <div className="mb-5 flex h-16 w-16 items-center justify-center rounded-2xl bg-primary-50">
            <Mic className="h-8 w-8 text-primary-600" />
          </div>
          <h2 className="text-2xl font-bold text-gray-900">
            Ready for a practice round?
          </h2>
          <p className="mt-2 max-w-md text-sm leading-relaxed text-gray-500">
            Mock interviews are the fastest way to build interview confidence.
            Answer AI-generated questions, get scored feedback, and track
            your readiness — all in one place.
          </p>
          <div className="mt-5 flex flex-wrap items-center justify-center gap-2 lg:justify-start">
            <span className="inline-flex items-center gap-1.5 rounded-full bg-gray-100 px-3 py-1 text-xs font-medium text-gray-600">
              <Clock className="h-3.5 w-3.5" />
              ~25 minutes per interview
            </span>
            <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-50 px-3 py-1 text-xs font-medium text-emerald-700">
              <LineChart className="h-3.5 w-3.5" />
              Instant AI feedback
            </span>
          </div>
          <Button size="lg" className="mt-7" onClick={onStart}>
            <Play className="h-4 w-4" />
            Start Your First Interview
          </Button>
        </div>

        {/* Benefits */}
        <ul className="space-y-4">
          {BENEFITS.map((benefit) => (
            <li
              key={benefit.title}
              className="flex items-start gap-4 rounded-xl border border-gray-100 p-4 transition-colors hover:bg-gray-50"
            >
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-primary-50">
                <benefit.icon className="h-5 w-5 text-primary-600" />
              </div>
              <div>
                <p className="text-sm font-semibold text-gray-900">{benefit.title}</p>
                <p className="mt-0.5 text-sm leading-relaxed text-gray-500">{benefit.text}</p>
              </div>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
};
