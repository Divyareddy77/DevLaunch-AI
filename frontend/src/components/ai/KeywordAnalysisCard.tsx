/**
 * KeywordAnalysisCard — the keyword analysis section of the ATS report.
 *
 * Shows the technical keywords detected in the resume, the common
 * in-demand keywords that are missing, and suggestions for improving
 * keyword coverage.
 *
 * @author DevLaunch
 */

import React from 'react';
import { KeyRound } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';

interface KeywordAnalysisCardProps {
  /** Technical keywords detected in the resume text. */
  foundKeywords: string[];
  /** Common in-demand keywords that are absent. */
  missingKeywords: string[];
  /** Suggestions for improving keyword coverage. */
  keywordSuggestions: string[];
}

export const KeywordAnalysisCard: React.FC<KeywordAnalysisCardProps> = ({
  foundKeywords,
  missingKeywords,
  keywordSuggestions,
}) => {
  return (
    <Card
      header={
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-amber-100">
            <KeyRound className="h-5 w-5 text-amber-600" />
          </div>
          <h3 className="text-sm font-semibold text-gray-900">Keyword Analysis</h3>
        </div>
      }
    >
      <div className="space-y-5">
        {/* Found keywords */}
        <div>
          <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-gray-500">
            Found in your resume
          </p>
          {foundKeywords.length === 0 ? (
            <p className="text-sm text-gray-400">No technical keywords detected yet.</p>
          ) : (
            <div className="flex flex-wrap gap-1.5">
              {foundKeywords.map((keyword) => (
                <Badge key={keyword} variant="success" size="sm">
                  {keyword}
                </Badge>
              ))}
            </div>
          )}
        </div>

        {/* Missing keywords */}
        <div>
          <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-gray-500">
            Missing common keywords
          </p>
          {missingKeywords.length === 0 ? (
            <p className="text-sm text-gray-400">
              Strong coverage — no common in-demand keywords are missing.
            </p>
          ) : (
            <div className="flex flex-wrap gap-1.5">
              {missingKeywords.map((keyword) => (
                <Badge key={keyword} variant="warning" size="sm">
                  {keyword}
                </Badge>
              ))}
            </div>
          )}
        </div>

        {/* Keyword suggestions */}
        {keywordSuggestions.length > 0 && (
          <div className="rounded-lg bg-amber-50 px-4 py-3">
            <p className="mb-1.5 text-xs font-semibold uppercase tracking-wide text-amber-700">
              How to improve coverage
            </p>
            <ul className="space-y-1.5">
              {keywordSuggestions.map((suggestion, index) => (
                <li key={index} className="flex items-start gap-2 text-sm text-amber-800">
                  <span className="mt-1.5 h-1 w-1 shrink-0 rounded-full bg-amber-400" />
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
