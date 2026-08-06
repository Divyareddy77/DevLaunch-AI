/**
 * InterviewSuggestionsCard — the personalised practice suggestions
 * generated for a completed interview.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Lightbulb } from 'lucide-react';
import { Card } from '../ui/Card';

interface InterviewSuggestionsCardProps {
  /** The personalised practice suggestions. */
  suggestions: string[];
}

export const InterviewSuggestionsCard: React.FC<InterviewSuggestionsCardProps> = ({
  suggestions,
}) => {
  if (suggestions.length === 0) {
    return null;
  }

  return (
    <Card
      header={
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-amber-100">
            <Lightbulb className="h-5 w-5 text-amber-600" />
          </div>
          <div>
            <h3 className="text-sm font-semibold text-gray-900">Your Practice Plan</h3>
            <p className="text-xs text-gray-500">
              Personalised next steps based on this interview
            </p>
          </div>
        </div>
      }
    >
      <ul className="space-y-2.5">
        {suggestions.map((suggestion, index) => (
          <li
            key={index}
            className="flex items-start gap-2.5 rounded-lg bg-amber-50/60 px-3 py-2.5"
          >
            <span className="mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-amber-200 text-[11px] font-semibold text-amber-800">
              {index + 1}
            </span>
            <span className="text-sm leading-relaxed text-gray-700">{suggestion}</span>
          </li>
        ))}
      </ul>
    </Card>
  );
};
