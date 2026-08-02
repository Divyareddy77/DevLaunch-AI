/**
 * ResumeReviewPage — AI-powered resume review and analysis.
 *
 * Lets the user select one of their existing resumes, optionally target
 * a job role, and receive an AI-generated analysis covering the resume
 * score, ATS compatibility score, strengths, weaknesses, missing skills,
 * and actionable improvement suggestions.
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
  TrendingUp,
  ShieldCheck,
  ThumbsUp,
  AlertTriangle,
  Puzzle,
} from 'lucide-react';
import { aiService } from '../../services/ai.service';
import { resumeService } from '../../services/resume.service';
import { getErrorMessage } from '../../utils/error';
import { ROUTES } from '../../constants/routes';
import { MESSAGES } from '../../constants/messages';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { ResumeReviewForm } from '../../components/ai/ResumeReviewForm';
import { ScoreCard } from '../../components/ai/ScoreCard';
import { ReviewSection } from '../../components/ai/ReviewSection';
import { SuggestionList } from '../../components/ai/SuggestionList';
import { EmptyReviewState } from '../../components/ai/EmptyReviewState';
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
        <h1 className="text-2xl font-bold text-gray-900">AI Resume Review</h1>
        <p className="mt-1 text-sm text-gray-500">
          Select a resume to receive AI-powered insights on its quality, ATS
          compatibility, and how to improve it.
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
                description="Choose a resume above and hit “Review Resume” to see your scores, strengths, weaknesses, and improvement suggestions."
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

                {/* Scores */}
                <div className="grid gap-4 sm:grid-cols-2">
                  <ScoreCard
                    label="Resume Score"
                    value={review.resumeScore}
                    icon={TrendingUp}
                    iconClassName="bg-primary-100 text-primary-600"
                    barClassName="bg-primary-600"
                    hint="Overall quality of your resume."
                  />
                  <ScoreCard
                    label="ATS Score"
                    value={review.atsScore}
                    icon={ShieldCheck}
                    iconClassName="bg-violet-100 text-violet-600"
                    barClassName="bg-violet-500"
                    hint="How well it passes automated screening."
                  />
                </div>

                {/* Findings */}
                <div className="grid gap-4 lg:grid-cols-3">
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
                  <ReviewSection
                    title="Missing Skills"
                    icon={Puzzle}
                    iconClassName="bg-blue-100 text-blue-600"
                    items={review.missingSkills}
                    emptyMessage="No missing skills detected."
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
