/**
 * MockInterviewPage — AI-powered mock interview practice.
 *
 * Guides the user through the full interview flow: choosing an interview
 * category, answering a generated set of questions, submitting the
 * answers for AI evaluation, and reviewing the feedback. Also shows the
 * user's interview history with summary statistics.
 *
 * @see backend/src/main/java/com/devlaunch/controller/AiController.java
 * @author DevLaunch
 */

import React, { useCallback, useEffect, useState } from 'react';
import { Mic, RotateCcw, Sparkles } from 'lucide-react';
import toast from 'react-hot-toast';
import { aiService } from '../../services/ai.service';
import { getErrorMessage } from '../../utils/error';
import { MESSAGES } from '../../constants/messages';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { MockInterviewSetup } from '../../components/ai/MockInterviewSetup';
import { InterviewQuestionCard } from '../../components/ai/InterviewQuestionCard';
import { InterviewFeedbackCard } from '../../components/ai/InterviewFeedbackCard';
import { InterviewHistoryCard } from '../../components/ai/InterviewHistoryCard';
import { EmptyReviewState } from '../../components/ai/EmptyReviewState';
import { Button } from '../../components/ui/Button';
import type {
  InterviewCategory,
  InterviewHistoryResponse,
  StartInterviewResponse,
  SubmitInterviewResponse,
} from '../../types/ai';

/** The active phase of the interview flow. */
type InterviewStage = 'setup' | 'answering' | 'result';

export const MockInterviewPage: React.FC = () => {
  const [history, setHistory] = useState<InterviewHistoryResponse | null>(null);
  const [historyLoading, setHistoryLoading] = useState(true);
  const [historyError, setHistoryError] = useState<string | null>(null);

  const [stage, setStage] = useState<InterviewStage>('setup');
  const [selectedCategory, setSelectedCategory] = useState<InterviewCategory | null>(null);
  const [session, setSession] = useState<StartInterviewResponse | null>(null);
  const [feedback, setFeedback] = useState<SubmitInterviewResponse | null>(null);

  const [starting, setStarting] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [startError, setStartError] = useState<string | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const [currentIndex, setCurrentIndex] = useState(0);
  const [answers, setAnswers] = useState<Record<string, string>>({});

  /** Loads the authenticated user's interview history. */
  const fetchHistory = useCallback(async () => {
    setHistoryLoading(true);
    setHistoryError(null);
    try {
      const data = await aiService.getInterviewHistory();
      setHistory(data);
    } catch (err) {
      setHistoryError(getErrorMessage(err, MESSAGES.INTERVIEW_HISTORY_ERROR));
    } finally {
      setHistoryLoading(false);
    }
  }, []);

  useEffect(() => {
    void fetchHistory();
  }, [fetchHistory]);

  /** Starts a new interview session for the selected category. */
  const handleStart = useCallback(async () => {
    if (!selectedCategory) return;

    setStarting(true);
    setStartError(null);
    setFeedback(null);
    try {
      const result = await aiService.startInterview({ interviewType: selectedCategory });
      setSession(result);
      setAnswers({});
      setCurrentIndex(0);
      setStage('answering');
    } catch (err) {
      setStartError(getErrorMessage(err, MESSAGES.MOCK_INTERVIEW_START_ERROR));
    } finally {
      setStarting(false);
    }
  }, [selectedCategory]);

  /** Advances to the next question or submits the whole interview. */
  const handleAnswerSubmit = useCallback(
    async (answer: string) => {
      if (!session) return;

      const question = session.questions[currentIndex];
      const nextAnswers = { ...answers, [question.id]: answer };
      setAnswers(nextAnswers);

      // More questions remain — advance to the next one.
      if (currentIndex < session.questions.length - 1) {
        setCurrentIndex((index) => index + 1);
        return;
      }

      // Final question — submit the interview for evaluation.
      setSubmitting(true);
      setSubmitError(null);
      try {
        const result = await aiService.submitInterview({
          sessionId: session.sessionId,
          interviewType: session.interviewType,
          answers: session.questions.map((q) => ({
            questionId: q.id,
            question: q.question,
            answer: nextAnswers[q.id] ?? '',
          })),
        });
        setFeedback(result);
        setStage('result');
        toast.success(MESSAGES.INTERVIEW_SUBMITTED);
        void fetchHistory();
      } catch (err) {
        setSubmitError(getErrorMessage(err, MESSAGES.MOCK_INTERVIEW_SUBMIT_ERROR));
      } finally {
        setSubmitting(false);
      }
    },
    [session, answers, currentIndex, fetchHistory],
  );

  /** Resets the flow back to the category selection. */
  const handleRestart = useCallback(() => {
    setStage('setup');
    setSession(null);
    setFeedback(null);
    setSelectedCategory(null);
    setStartError(null);
    setSubmitError(null);
    setCurrentIndex(0);
    setAnswers({});
  }, []);

  return (
    <div className="space-y-6">
      {/* Page header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">AI Mock Interview</h1>
        <p className="mt-1 text-sm text-gray-500">
          Practise with AI-generated questions, receive personalised
          feedback, and track your progress over time.
        </p>
      </div>

      {/* Stage: setup */}
      {stage === 'setup' && (
        <>
          <EmptyReviewState
            icon={Mic}
            iconClassName="bg-primary-100 text-primary-600"
            title="Ready for a practice round?"
            description="Select an interview type below to start. You'll answer a set of AI-generated questions and get a score with actionable feedback."
          />
          {startError && <ErrorMessage message={startError} onRetry={handleStart} />}
          <MockInterviewSetup
            selected={selectedCategory}
            starting={starting}
            onSelect={setSelectedCategory}
            onStart={handleStart}
          />
        </>
      )}

      {/* Stage: answering */}
      {stage === 'answering' && session && (
        <div className="space-y-4">
          {submitError && (
            <ErrorMessage
              message={submitError}
              onRetry={() => {
                const current = session.questions[currentIndex];
                if (current) void handleAnswerSubmit(answers[current.id] ?? '');
              }}
            />
          )}
          <InterviewQuestionCard
            key={session.questions[currentIndex]?.id}
            question={session.questions[currentIndex]}
            index={currentIndex}
            total={session.questions.length}
            defaultAnswer={answers[session.questions[currentIndex]?.id] ?? ''}
            submitting={submitting}
            onSubmit={handleAnswerSubmit}
            onBack={() => setCurrentIndex((index) => Math.max(0, index - 1))}
          />
        </div>
      )}

      {/* Stage: result */}
      {stage === 'result' && feedback && (
        <div className="space-y-4">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex items-center gap-2">
              <Sparkles className="h-4 w-4 shrink-0 text-amber-500" />
              <p className="text-sm text-gray-500">
                Interview complete — here is your AI feedback.
              </p>
            </div>
            <Button variant="outline" size="sm" onClick={handleRestart}>
              <RotateCcw className="h-3.5 w-3.5" />
              Start New Interview
            </Button>
          </div>
          <InterviewFeedbackCard result={feedback} />
        </div>
      )}

      {/* Interview history */}
      {stage !== 'answering' && (
        <InterviewHistoryCard
          data={history}
          loading={historyLoading}
          error={historyError}
          onRetry={fetchHistory}
        />
      )}
    </div>
  );
};
