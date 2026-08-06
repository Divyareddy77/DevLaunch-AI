/**
 * InterviewReportCard — the professional AI feedback report shown after a
 * mock interview (or when viewing a past session's report).
 *
 * Shows the overall score, the seven dimension scores, the missed
 * concepts, strengths, areas for improvement, personalised suggestions,
 * and a per-question breakdown with improved sample answers.
 *
 * @author DevLaunch
 */

import React from 'react';
import {
  Star,
  ThumbsUp,
  TrendingUp,
  MessageSquare,
  Target,
  Award,
  Mic,
  Clock,
  FileText,
  RotateCcw,
  Plus,
  ChevronDown,
  ChevronUp,
} from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { Button } from '../ui/Button';
import { ScoreCard } from './ScoreCard';
import { ReviewSection } from './ReviewSection';
import { InterviewSuggestionsCard } from './InterviewSuggestionsCard';
import { getScoreBadgeVariant, enumToLabel } from '../../utils/format';
import { formatDuration } from '../../utils/format';
import type { InterviewReportData } from '../../types/ai';

interface InterviewReportCardProps {
  /** The report data to render. */
  report: InterviewReportData;
  /** Called to retake the same category with the same configuration. */
  onRetake: () => void;
  /** Called to return to the landing page. */
  onNewInterview: () => void;
}

/** A single dimension score tile. */
interface Dimension {
  /** The field on the report holding the score. */
  key: keyof Pick<
    InterviewReportData,
    | 'technicalScore'
    | 'communicationScore'
    | 'confidenceScore'
    | 'problemSolvingScore'
    | 'clarityScore'
    | 'vocabularyScore'
    | 'professionalismScore'
  >;
  /** The display label. */
  label: string;
}

const DIMENSIONS: Dimension[] = [
  { key: 'technicalScore', label: 'Technical' },
  { key: 'communicationScore', label: 'Communication' },
  { key: 'confidenceScore', label: 'Confidence' },
  { key: 'problemSolvingScore', label: 'Problem Solving' },
  { key: 'clarityScore', label: 'Clarity' },
  { key: 'vocabularyScore', label: 'Vocabulary' },
  { key: 'professionalismScore', label: 'Professionalism' },
];

const DimensionCard: React.FC<{ label: string; value: number }> = ({ label, value }) => (
  <div className="rounded-lg border border-gray-100 p-3">
    <div className="flex items-center justify-between gap-2">
      <p className="text-xs font-medium text-gray-500">{label}</p>
      <span className="text-sm font-bold text-gray-900">{value}</span>
    </div>
    <div className="mt-2 h-1.5 w-full overflow-hidden rounded-full bg-gray-100">
      <div
        className={`h-full rounded-full transition-all duration-700 ${
          value >= 70 ? 'bg-emerald-500' : value >= 50 ? 'bg-amber-500' : 'bg-red-500'
        }`}
        style={{ width: `${value}%` }}
      />
    </div>
  </div>
);

export const InterviewReportCard: React.FC<InterviewReportCardProps> = ({
  report,
  onRetake,
  onNewInterview,
}) => {
  const [expandedQuestion, setExpandedQuestion] = React.useState<string | null>(null);
  const questionsAnswered = report.feedback.length;

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-2">
          <Badge variant="primary">{enumToLabel(report.interviewType)}</Badge>
          {report.difficulty && <Badge variant="default">{enumToLabel(report.difficulty)}</Badge>}
          <Badge variant={getScoreBadgeVariant(report.overallScore)} size="sm">
            {report.overallScore}/100
          </Badge>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" size="sm" onClick={onRetake}>
            <RotateCcw className="h-3.5 w-3.5" />
            Retake
          </Button>
          <Button variant="ghost" size="sm" onClick={onNewInterview}>
            <Plus className="h-3.5 w-3.5" />
            New Interview
          </Button>
        </div>
      </div>

      {/* Overall + meta */}
      <div className="grid gap-4 lg:grid-cols-3">
        <ScoreCard
          label="Interview Score"
          value={report.overallScore}
          icon={Star}
          iconClassName="bg-amber-100 text-amber-600"
          barClassName="bg-amber-500"
          hint="Overall performance across all answers."
        />
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:col-span-2">
          <div className="rounded-lg border border-gray-100 p-3">
            <div className="flex items-center gap-1.5 text-xs font-medium text-gray-500">
              <MessageSquare className="h-3.5 w-3.5" /> Questions
            </div>
            <p className="mt-1 text-lg font-bold text-gray-900">{questionsAnswered}</p>
          </div>
          <div className="rounded-lg border border-gray-100 p-3">
            <div className="flex items-center gap-1.5 text-xs font-medium text-gray-500">
              <Clock className="h-3.5 w-3.5" /> Duration
            </div>
            <p className="mt-1 text-lg font-bold text-gray-900">
              {formatDuration(report.durationSeconds)}
            </p>
          </div>
          <div className="rounded-lg border border-gray-100 p-3">
            <div className="flex items-center gap-1.5 text-xs font-medium text-gray-500">
              <FileText className="h-3.5 w-3.5" /> Words
            </div>
            <p className="mt-1 text-lg font-bold text-gray-900">{report.wordCount ?? '—'}</p>
          </div>
          <div className="col-span-2 rounded-lg border border-gray-100 bg-gray-50/60 p-3 sm:col-span-3">
            <div className="flex items-center gap-1.5 text-xs font-medium text-gray-500">
              <Mic className="h-3.5 w-3.5" /> Answer mode
            </div>
            <p className="mt-1 text-sm font-semibold text-gray-800">
              {report.timed ? 'Timed' : 'Untimed'} ·{' '}
              {report.wordCount && report.durationSeconds && report.durationSeconds > 0
                ? `~${Math.round((report.wordCount / report.durationSeconds) * 60)} wpm pace`
                : 'Typed answer'}
            </p>
          </div>
        </div>
      </div>

      {/* Dimension scores */}
      <Card header={
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-indigo-100">
            <Award className="h-5 w-5 text-indigo-600" />
          </div>
          <h3 className="text-sm font-semibold text-gray-900">Skill Breakdown</h3>
        </div>
      }>
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4">
          {DIMENSIONS.map((dimension) => {
            const value = report[dimension.key];
            return value != null ? (
              <DimensionCard key={dimension.key} label={dimension.label} value={value} />
            ) : null;
          })}
        </div>
      </Card>

      {/* Missed concepts */}
      {report.missedConcepts && report.missedConcepts.length > 0 && (
        <Card
          header={
            <div className="flex items-center gap-3">
              <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-red-100">
                <Target className="h-5 w-5 text-red-600" />
              </div>
              <h3 className="text-sm font-semibold text-gray-900">Missed Concepts</h3>
            </div>
          }
        >
          <ul className="space-y-2">
            {report.missedConcepts.map((concept, index) => (
              <li key={index} className="flex items-start gap-2 text-sm text-gray-600">
                <span className="mt-1.5 h-1.5 w-1.5 shrink-0 rounded-full bg-red-400" />
                {concept}
              </li>
            ))}
          </ul>
        </Card>
      )}

      {/* Strengths / improvements */}
      <div className="grid gap-4 lg:grid-cols-2">
        <ReviewSection
          title="Strengths"
          icon={ThumbsUp}
          iconClassName="bg-emerald-100 text-emerald-600"
          items={report.strengths}
          emptyMessage="No strengths flagged yet."
        />
        <ReviewSection
          title="Weaknesses"
          icon={TrendingUp}
          iconClassName="bg-amber-100 text-amber-600"
          items={report.areasForImprovement}
          emptyMessage="Nothing to improve — great job!"
        />
      </div>

      {/* Suggestions */}
      <InterviewSuggestionsCard suggestions={report.suggestions ?? []} />

      {/* Per-question feedback */}
      <Card
        header={
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary-100">
              <MessageSquare className="h-5 w-5 text-primary-600" />
            </div>
            <h3 className="text-sm font-semibold text-gray-900">Question-by-Question Feedback</h3>
          </div>
        }
      >
        <ul className="space-y-4">
          {report.feedback.map((item, index) => {
            const expanded = expandedQuestion === item.questionId;
            return (
              <li key={item.questionId} className="overflow-hidden rounded-xl border border-gray-100">
                <button
                  type="button"
                  onClick={() => setExpandedQuestion(expanded ? null : item.questionId)}
                  className="flex w-full items-center justify-between gap-3 px-4 py-3 text-left transition-colors hover:bg-gray-50"
                >
                  <div className="flex items-center gap-2.5">
                    <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-gray-100 text-xs font-semibold text-gray-600">
                      {index + 1}
                    </span>
                    <p className="text-sm font-medium text-gray-800">{item.question}</p>
                  </div>
                  <div className="flex shrink-0 items-center gap-2">
                    <Badge variant={getScoreBadgeVariant(item.score)} size="sm">
                      {item.score}/100
                    </Badge>
                    {expanded ? (
                      <ChevronUp className="h-4 w-4 text-gray-400" />
                    ) : (
                      <ChevronDown className="h-4 w-4 text-gray-400" />
                    )}
                  </div>
                </button>

                {expanded && (
                  <div className="space-y-3 border-t border-gray-100 px-4 py-3">
                    <div>
                      <p className="text-xs font-semibold uppercase tracking-wide text-gray-400">
                        Your answer
                      </p>
                      <p className="mt-1 rounded-lg bg-gray-50 p-3 text-sm leading-relaxed text-gray-600">
                        {item.answer || 'No answer provided.'}
                      </p>
                    </div>
                    <div>
                      <p className="text-xs font-semibold uppercase tracking-wide text-gray-400">
                        AI feedback
                      </p>
                      <p className="mt-1 text-sm leading-relaxed text-gray-700">{item.feedback}</p>
                    </div>
                    {item.suggestions.length > 0 && (
                      <ul className="space-y-1">
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
                    {item.improvedAnswer && (
                      <div className="rounded-lg border border-emerald-100 bg-emerald-50/60 p-3">
                        <p className="text-xs font-semibold uppercase tracking-wide text-emerald-600">
                          Improved sample answer
                        </p>
                        <p className="mt-1 text-sm leading-relaxed text-emerald-900">
                          {item.improvedAnswer}
                        </p>
                      </div>
                    )}
                  </div>
                )}
              </li>
            );
          })}
        </ul>
      </Card>
    </div>
  );
};
