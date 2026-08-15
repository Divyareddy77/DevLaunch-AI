/**
 * InterviewFeedbackCard — the AI-generated result of a completed mock
 * interview.
 *
 * Shows the overall score, the strengths and areas for improvement
 * identified by the AI, and a per-question breakdown with individual
 * scores and suggestions.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Star, ThumbsUp, TrendingUp, MessageSquare } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { ScoreCard } from './ScoreCard';
import { ReviewSection } from './ReviewSection';
import { getScoreBadgeVariant } from '../../utils/format';
import type { SubmitInterviewResponse } from '../../types/ai';

interface InterviewFeedbackCardProps {
  /** The feedback result returned after submitting the interview. */
  result: SubmitInterviewResponse;
}

export const InterviewFeedbackCard: React.FC<InterviewFeedbackCardProps> = ({ result }) => {
  return (
    <div className="space-y-4">
      {/* Overall score */}
      <div className="grid gap-4 sm:grid-cols-2">
        <ScoreCard
          label="Interview Score"
          value={result.overallScore}
          icon={Star}
          iconClassName="bg-amber-100 text-amber-600"
          barClassName="bg-amber-500"
          hint="Overall performance across all answers."
        />
        <Card>
          <div className="flex h-full flex-col justify-center">
            <p className="text-sm font-medium text-gray-500">Performance Summary</p>
            <div className="mt-3 space-y-2">
              <div className="flex items-center justify-between gap-4">
                <span className="text-sm text-gray-500">Questions answered</span>
                <Badge variant="default">{result.feedback.length}</Badge>
              </div>
              <div className="flex items-center justify-between gap-4">
                <span className="text-sm text-gray-500">Strengths found</span>
                <Badge variant="success">{result.strengths.length}</Badge>
              </div>
              <div className="flex items-center justify-between gap-4">
                <span className="text-sm text-gray-500">Areas to improve</span>
                <Badge variant="warning">{result.areasForImprovement.length}</Badge>
              </div>
            </div>
          </div>
        </Card>
      </div>

      {/* Strengths / improvements */}
      <div className="grid gap-4 lg:grid-cols-2">
        <ReviewSection
          title="Strengths"
          icon={ThumbsUp}
          iconClassName="bg-emerald-100 text-emerald-600"
          items={result.strengths}
          emptyMessage="No strengths flagged yet."
        />
        <ReviewSection
          title="Areas for Improvement"
          icon={TrendingUp}
          iconClassName="bg-amber-100 text-amber-600"
          items={result.areasForImprovement}
          emptyMessage="Nothing to improve — great job!"
        />
      </div>

      {/* Per-question feedback */}
      <Card
        header={
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary-100">
              <MessageSquare className="h-5 w-5 text-primary-600" />
            </div>
            <h3 className="text-sm font-semibold text-gray-900">Question Feedback</h3>
          </div>
        }
      >
        <ul className="space-y-5">
          {result.feedback.map((item, index) => (
            <li key={item.questionId} className="space-y-2.5">
              <div className="flex items-start justify-between gap-4">
                <div className="flex items-start gap-2.5">
                  <span className="mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-gray-100 text-xs font-semibold text-gray-600">
                    {index + 1}
                  </span>
                  <p className="text-sm font-medium leading-relaxed text-gray-800">
                    {item.question}
                  </p>
                </div>
                <Badge variant={getScoreBadgeVariant(item.score)} size="sm" className="shrink-0">
                  {item.score}/100
                </Badge>
              </div>

              <p className="text-sm leading-relaxed text-gray-600">{item.feedback}</p>

              {item.suggestions.length > 0 && (
                <ul className="space-y-1.5">
                  {item.suggestions.map((suggestion, suggestionIndex) => (
                    <li
                      key={suggestionIndex}
                      className="flex items-start gap-2 text-sm text-gray-500"
                    >
                      <span className="mt-1.5 h-1 w-1 shrink-0 rounded-full bg-gray-400" />
                      {suggestion}
                    </li>
                  ))}
                </ul>
              )}
            </li>
          ))}
        </ul>
      </Card>
    </div>
  );
};
