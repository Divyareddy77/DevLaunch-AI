/**
 * ResumeReviewPage — AI-powered ATS Resume Analysis.
 *
 * Lets the user select one of their existing resumes, optionally target
 * a job role, and receive a professional ATS report: an overall ATS score
 * with a weighted category breakdown, strengths, weaknesses, missing
 * sections, keyword analysis, a formatting review, an evaluation of the
 * professional summary (with an AI-improved version), and per-project,
 * per-skill, and per-experience analyses.
 *
 * @see backend/src/main/java/com/devlaunch/controller/AiController.java
 * @author DevLaunch
 */

import React, { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  FileSearch,
  FileText,
  Sparkles,
  ThumbsUp,
  AlertTriangle,
  Puzzle,
  Ruler,
} from 'lucide-react';
import { aiService } from '../../services/ai.service';
import { resumeService } from '../../services/resume.service';
import { getErrorMessage } from '../../utils/error';
import { ROUTES } from '../../constants/routes';
import { MESSAGES } from '../../constants/messages';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { ResumeReviewForm } from '../../components/ai/ResumeReviewForm';
import { AtsScoreCard } from '../../components/ai/AtsScoreCard';
import { CategoryScoresCard } from '../../components/ai/CategoryScoresCard';
import { ReviewSection } from '../../components/ai/ReviewSection';
import { SuggestionList } from '../../components/ai/SuggestionList';
import { EmptyReviewState } from '../../components/ai/EmptyReviewState';
import { KeywordAnalysisCard } from '../../components/ai/KeywordAnalysisCard';
import { SummaryAnalysisCard } from '../../components/ai/SummaryAnalysisCard';
import { ProjectAnalysisCard } from '../../components/ai/ProjectAnalysisCard';
import { SkillsAnalysisCard } from '../../components/ai/SkillsAnalysisCard';
import { ExperienceAnalysisCard } from '../../components/ai/ExperienceAnalysisCard';
import type { ResumeResponse } from '../../types/resume';
import type { ResumeReviewRequest, ResumeReviewResponse } from '../../types/ai';

export const ResumeReviewPage: React.FC = () => {
  const navigate = useNavigate();

  const [resumes, setResumes] = useState<ResumeResponse[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  const [review, setReview] = useState<ResumeReviewResponse | null>(null);
  const [reviewing, setReviewing] = useState(false);
  const [reviewError, setReviewError] = useState<string | null>(null);
  const lastRequestRef = useRef<ResumeReviewRequest | null>(null);

  /** Loads the authenticated user's resumes. */
  const fetchResumes = useCallback(async () => {
    setLoading(true);
    setLoadError(null);
    try {
      const data = await resumeService.getAllResumes();
      setResumes(data);
    } catch (err) {
      setLoadError(getErrorMessage(err, MESSAGES.LOAD_ERROR('resumes')));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void fetchResumes();
  }, [fetchResumes]);

  /** Submits a resume for AI review and stores the result. */
  const handleReview = useCallback(async (data: ResumeReviewRequest) => {
    lastRequestRef.current = data;
    setReview(null);
    setReviewing(true);
    setReviewError(null);
    try {
      const result = await aiService.reviewResume(data);
      setReview(result);
    } catch (err) {
      setReviewError(getErrorMessage(err, MESSAGES.RESUME_REVIEW_ERROR));
    } finally {
      setReviewing(false);
    }
  }, []);

  if (loading) {
    return <LoadingScreen />;
  }

  return (
    <div className="space-y-6">
      {/* Page header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">ATS Resume Analysis</h1>
        <p className="mt-1 text-sm text-gray-500">
          Select a resume to receive a professional ATS report — overall score,
          category breakdown, keyword analysis, and actionable improvements.
        </p>
      </div>

      {loadError ? (
        <ErrorMessage message={loadError} onRetry={fetchResumes} />
      ) : resumes && resumes.length === 0 ? (
        <EmptyReviewState
          icon={FileSearch}
          iconClassName="bg-emerald-100 text-emerald-600"
          title="No resumes yet"
          description={MESSAGES.NO_RESUMES}
          actionLabel="Create Your First Resume"
          onAction={() => navigate(ROUTES.RESUME_CREATE)}
        />
      ) : (
        resumes && (
          <>
            <ResumeReviewForm
              resumes={resumes}
              submitting={reviewing}
              onSubmit={handleReview}
            />

            {reviewError && (
              <ErrorMessage
                message={reviewError}
                onRetry={() => {
                  if (lastRequestRef.current) void handleReview(lastRequestRef.current);
                }}
              />
            )}

            {!review && !reviewError && (
              <EmptyReviewState
                icon={Sparkles}
                iconClassName="bg-primary-100 text-primary-600"
                title="Ready to review"
                description="Choose a resume above and hit “Review Resume” to receive your ATS score, category breakdown, and improvement plan."
              />
            )}

            {review && (
              <div className="space-y-4">
                {/* Reviewed resume title */}
                <div className="flex items-center gap-2">
                  <FileText className="h-4 w-4 shrink-0 text-gray-400" />
                  <p className="text-sm text-gray-500">
                    Review for{' '}
                    <span className="font-medium text-gray-700">
                      {review.resumeTitle}
                    </span>
                  </p>
                </div>

                {/* Overall ATS score */}
                <AtsScoreCard atsScore={review.atsScore} resumeScore={review.resumeScore} />

                {/* Category breakdown */}
                <CategoryScoresCard categoryScores={review.categoryScores} />

                {/* Strengths / Weaknesses */}
                <div className="grid gap-4 lg:grid-cols-2">
                  <ReviewSection
                    title="Strengths"
                    icon={ThumbsUp}
                    iconClassName="bg-emerald-100 text-emerald-600"
                    items={review.strengths}
                  />
                  <ReviewSection
                    title="Weaknesses"
                    icon={AlertTriangle}
                    iconClassName="bg-red-100 text-red-600"
                    items={review.weaknesses}
                  />
                </div>

                {/* Professional summary evaluation */}
                <SummaryAnalysisCard analysis={review.summaryAnalysis} />

                {/* Keyword analysis */}
                <KeywordAnalysisCard
                  foundKeywords={review.foundKeywords}
                  missingKeywords={review.missingKeywords}
                  keywordSuggestions={review.keywordSuggestions}
                />

                {/* Project analysis */}
                <ProjectAnalysisCard projects={review.projectAnalyses} />

                {/* Experience analysis */}
                <ExperienceAnalysisCard analysis={review.experienceAnalysis} />

                {/* Skills analysis */}
                <SkillsAnalysisCard
                  technicalSkills={review.skillsAnalysis.technicalSkills}
                  softSkills={review.skillsAnalysis.softSkills}
                  organization={review.skillsAnalysis.organization}
                  missingRelevantSkills={review.skillsAnalysis.missingRelevantSkills}
                />

                {/* Missing sections + formatting review */}
                <div className="grid gap-4 lg:grid-cols-2">
                  <ReviewSection
                    title="Missing Sections"
                    icon={Puzzle}
                    iconClassName="bg-blue-100 text-blue-600"
                    items={review.missingSections}
                    emptyMessage="All standard sections are present."
                  />
                  <ReviewSection
                    title="Formatting Review"
                    icon={Ruler}
                    iconClassName="bg-cyan-100 text-cyan-600"
                    items={review.formattingAnalysis}
                    emptyMessage="No formatting concerns."
                  />
                </div>

                {/* Suggestions */}
                <SuggestionList suggestions={review.suggestions} />
              </div>
            )}
          </>
        )
      )}
    </div>
  );
};
