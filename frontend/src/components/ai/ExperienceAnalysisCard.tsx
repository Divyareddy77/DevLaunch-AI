/**
 * ExperienceAnalysisCard — the experience section evaluation of the ATS report.
 *
 * Shows the action verbs used across the roles, how responsibilities and
 * achievements are described, whether quantified impact is present, and
 * experience-specific suggestions.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Briefcase, CheckCircle2, XCircle } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import type { ExperienceAnalysis } from '../../types/ai';

interface ExperienceAnalysisCardProps {
  /** The experience section evaluation. */
  analysis: ExperienceAnalysis;
}

export const ExperienceAnalysisCard: React.FC<ExperienceAnalysisCardProps> = ({ analysis }) => {
  return (
    <Card
      header={
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-violet-100">
            <Briefcase className="h-5 w-5 text-violet-600" />
          </div>
          <h3 className="text-sm font-semibold text-gray-900">Experience Analysis</h3>
        </div>
      }
    >
      <div className="space-y-4">
        <div className="flex flex-wrap items-center gap-4">
          <span className="inline-flex items-center gap-1 text-sm text-gray-600">
            {analysis.quantifiedImpact ? (
              <CheckCircle2 className="h-4 w-4 text-emerald-500" />
            ) : (
              <XCircle className="h-4 w-4 text-red-400" />
            )}
            Quantified impact
          </span>
          {analysis.actionVerbs.length > 0 && (
            <span className="inline-flex flex-wrap items-center gap-1.5 text-sm text-gray-600">
              Action verbs:
              {analysis.actionVerbs.map((verb) => (
                <Badge key={verb} variant="success" size="sm">
                  {verb}
                </Badge>
              ))}
            </span>
          )}
        </div>

        <div className="grid gap-3 sm:grid-cols-2">
          <div className="rounded-lg bg-gray-50 px-4 py-3">
            <p className="text-xs font-semibold uppercase tracking-wide text-gray-500">
              Responsibilities
            </p>
            <p className="mt-1 text-sm text-gray-700">{analysis.responsibilities}</p>
          </div>
          <div className="rounded-lg bg-gray-50 px-4 py-3">
            <p className="text-xs font-semibold uppercase tracking-wide text-gray-500">
              Achievements
            </p>
            <p className="mt-1 text-sm text-gray-700">{analysis.achievements}</p>
          </div>
        </div>

        {analysis.suggestions.length > 0 && (
          <div>
            <p className="mb-1.5 text-xs font-semibold uppercase tracking-wide text-gray-500">
              How to improve
            </p>
            <ul className="space-y-1.5">
              {analysis.suggestions.map((suggestion, index) => (
                <li key={index} className="flex items-start gap-2 text-sm text-gray-600">
                  <span className="mt-1.5 h-1 w-1 shrink-0 rounded-full bg-violet-400" />
                  {suggestion}
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </Card>
  );
};
