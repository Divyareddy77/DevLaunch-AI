/**
 * SuggestionList — renders actionable resume improvement
 * suggestions with their section and priority badges.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Lightbulb } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import type { ResumeReviewSuggestion } from '../../types/ai';

interface SuggestionListProps {
  /** The suggestions to display. */
  suggestions: ResumeReviewSuggestion[];
}

/** Maps a suggestion priority to its Badge variant. */
const priorityVariant: Record<ResumeReviewSuggestion['priority'], 'danger' | 'warning' | 'success'> = {
  high: 'danger',
  medium: 'warning',
  low: 'success',
};

export const SuggestionList: React.FC<SuggestionListProps> = ({ suggestions }) => {
  return (
    <Card
      header={
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-amber-100">
            <Lightbulb className="h-5 w-5 text-amber-600" />
          </div>
          <h3 className="text-sm font-semibold text-gray-900">
            Improvement Suggestions
          </h3>
        </div>
      }
    >
      {suggestions.length === 0 ? (
        <p className="text-sm text-gray-400">
          No suggestions — your resume looks well-rounded.
        </p>
      ) : (
        <ul className="space-y-4">
          {suggestions.map((suggestion, index) => (
            <li key={index} className="flex flex-col gap-1.5 sm:flex-row sm:items-start sm:gap-3">
              <div className="flex shrink-0 items-center gap-2">
                <Badge variant="default" size="sm">
                  {suggestion.section}
                </Badge>
                <Badge variant={priorityVariant[suggestion.priority]} size="sm">
                  {suggestion.priority}
                </Badge>
              </div>
              <p className="text-sm leading-relaxed text-gray-600">
                {suggestion.suggestion}
              </p>
            </li>
          ))}
        </ul>
      )}
    </Card>
  );
};
