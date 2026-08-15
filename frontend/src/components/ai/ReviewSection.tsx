/**
 * ReviewSection — a titled card rendering a bulleted list of
 * review findings (strengths, weaknesses, or missing skills).
 *
 * @author DevLaunch
 */

import React from 'react';
import { type LucideIcon } from 'lucide-react';
import { Card } from '../ui/Card';

interface ReviewSectionProps {
  /** The section title (e.g. "Strengths"). */
  title: string;
  /** Icon rendered inside the coloured circle in the card header. */
  icon: LucideIcon;
  /** Background/text classes for the icon circle. */
  iconClassName: string;
  /** The findings to display as a bulleted list. */
  items: string[];
  /** Message shown when the list is empty. */
  emptyMessage?: string;
}

export const ReviewSection: React.FC<ReviewSectionProps> = ({
  title,
  icon: Icon,
  iconClassName,
  items,
  emptyMessage = 'Nothing flagged.',
}) => {
  return (
    <Card
      header={
        <div className="flex items-center gap-3">
          <div
            className={`flex h-9 w-9 items-center justify-center rounded-lg ${iconClassName}`}
          >
            <Icon className="h-5 w-5" />
          </div>
          <h3 className="text-sm font-semibold text-gray-900">{title}</h3>
        </div>
      }
    >
      {items.length === 0 ? (
        <p className="text-sm text-gray-400">{emptyMessage}</p>
      ) : (
        <ul className="space-y-2.5">
          {items.map((item, index) => (
            <li key={index} className="flex items-start gap-2.5">
              <span className="mt-1.5 h-1.5 w-1.5 shrink-0 rounded-full bg-gray-300" />
              <span className="text-sm leading-relaxed text-gray-600">{item}</span>
            </li>
          ))}
        </ul>
      )}
    </Card>
  );
};
