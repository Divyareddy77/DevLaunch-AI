/**
 * MockInterviewPage — the professional AI Mock Interview platform.
 *
 * Guides the user through the full flow: the landing page with readiness
 * stats and category cards, the setup screen (difficulty, length, timed,
 * voice/text mode, camera, microphone), the live interview experience
 * with voice answers and speaking analysis, and the AI feedback report.
 * Also shows the interview analytics, history with per-session actions,
 * and empty states. Answers are auto-saved to local storage so a refresh
 * mid-interview resumes where the user left off.
 *
 * @see backend/src/main/java/com/devlaunch/controller/AiController.java
 * @author DevLaunch
 */

import React, { useCallback, useEffect, useRef, useState } from 'react';
import { RotateCcw, Sparkles } from 'lucide-react';
import toast from 'react-hot-toast';
import { aiService } from '../../services/ai.service';
import { getErrorMessage } from '../../utils/error';
import { historyItemToReport } from '../../utils/interview';
import { MESSAGES } from '../../constants/messages';
import { INTERVIEW_DRAFT_PREFIX, TIMED_QUESTION_SECONDS } from '../../constants/interview';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { PageHeader } from '../../components/shared/PageHeader';
import { MockInterviewLanding } from '../../components/ai/MockInterviewLanding';
import {
  MockInterviewSetup,
  DEFAULT_INTERVIEW_CONFIG,
  type InterviewSetupConfig,
} from '../../components/ai/MockInterviewSetup';
import { InterviewSessionCard } from '../../components/ai/InterviewSessionCard';
import { InterviewReportCard } from '../../components/ai/InterviewReportCard';
import { InterviewAnalyticsCard } from '../../components/ai/InterviewAnalyticsCard';
import { InterviewHistoryCard } from '../../components/ai/InterviewHistoryCard';
import { Button } from '../../components/ui/Button';
import type {
  InterviewCategory,
  InterviewCategoryStats,
  InterviewHistoryItem,
  InterviewHistoryResponse,
  InterviewReportData,
  StartInterviewResponse,
  SubmitInterviewResponse,
} from '../../types/ai';

/** The active phase of the interview flow. */
type InterviewStage = 'landing' | 'setup' | 'answering' | 'report';

/** The auto-save payload persisted to local storage. */
interface InterviewDraft {
  session: StartInterviewResponse;
  answers: Record<string, string>;
  config: InterviewSetupConfig | null;
  startedAt: number;
}

/** The local storage key pointing at the active draft. */
const ACTIVE_DRAFT_KEY = 'mock-interview-active';

export const MockInterviewPage: React.FC = () => {
  const [history, setHistory] = useState<InterviewHistoryResponse | null>(null);
  const [historyLoading, setHistoryLoading] = useState(true);
  const [historyError, setHistoryError] = useState<string | null>(null);
  const [categories, setCategories] = useState<InterviewCategoryStats[] | null>(null);
  const [categoriesLoading, setCategoriesLoading] = useState(true);
  const [categoriesError, setCategoriesError] = useState<string | null>(null);

  const [stage, setStage] = useState<InterviewStage>('landing');
  const [selectedCategory, setSelectedCategory] = useState<InterviewCategory | null>(null);
  const [config, setConfig] = useState<InterviewSetupConfig | null>(null);
  const [lastConfig, setLastConfig] = useState<InterviewSetupConfig>(DEFAULT_INTERVIEW_CONFIG);
  const [session, setSession] = useState<StartInterviewResponse | null>(null);
  const [feedback, setFeedback] = useState<SubmitInterviewResponse | null>(null);
  const [reportData, setReportData] = useState<InterviewReportData | null>(null);

  const [currentIndex, setCurrentIndex] = useState(0);
  const [answers, setAnswers] = useState<Record<string, string>>({});
  const [elapsedSeconds, setElapsedSeconds] = useState(0);

  const [starting, setStarting] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [deletingSessionId, setDeletingSessionId] = useState<string | null>(null);
  const [startError, setStartError] = useState<string | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);

  // Refs mirroring the state used inside stable callbacks.
  const sessionRef = useRef<StartInterviewResponse | null>(null);
  const answersRef = useRef<Record<string, string>>({});
  const configRef = useRef<InterviewSetupConfig | null>(null);
  const elapsedRef = useRef(0);
  useEffect(() => {
    sessionRef.current = session;
  }, [session]);
  useEffect(() => {
    answersRef.current = answers;
  }, [answers]);
  useEffect(() => {
    configRef.current = config;
  }, [config]);
  useEffect(() => {
    elapsedRef.current = elapsedSeconds;
  }, [elapsedSeconds]);

  /** Loads the interview history and category statistics in parallel. */
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

  const fetchCategories = useCallback(async () => {
    setCategoriesLoading(true);
    setCategoriesError(null);
    try {
      const data = await aiService.getInterviewCategories();
      setCategories(data);
    } catch (err) {
      setCategoriesError(getErrorMessage(err, MESSAGES.INTERVIEW_HISTORY_ERROR));
    } finally {
      setCategoriesLoading(false);
    }
  }, []);

  const fetchAll = useCallback(() => {
    void fetchHistory();
    void fetchCategories();
  }, [fetchHistory, fetchCategories]);

  useEffect(() => {
    fetchAll();
  }, [fetchAll]);

  /** Restores an in-progress interview draft after a refresh. */
  useEffect(() => {
    const activeId = window.localStorage.getItem(ACTIVE_DRAFT_KEY);
    if (!activeId) {
      return;
    }
    const raw = window.localStorage.getItem(INTERVIEW_DRAFT_PREFIX + activeId);
    if (!raw) {
      return;
    }
    try {
      const draft = JSON.parse(raw) as InterviewDraft;
      if (draft.session && draft.answers) {
        setSession(draft.session);
        setAnswers(draft.answers);
        setConfig(draft.config ?? null);
        setCurrentIndex(0);
        setElapsedSeconds(0);
        setStage('answering');
      }
    } catch {
      // Corrupt draft — ignore and start fresh.
    }
  }, []);

  /** Persists the current answers as a resumable draft. */
  const saveDraft = useCallback(() => {
    const current = sessionRef.current;
    if (!current) {
      return;
    }
    const payload: InterviewDraft = {
      session: current,
      answers: answersRef.current,
      config: configRef.current,
      startedAt: Date.now(),
    };
    window.localStorage.setItem(INTERVIEW_DRAFT_PREFIX + current.sessionId, JSON.stringify(payload));
    window.localStorage.setItem(ACTIVE_DRAFT_KEY, current.sessionId);
  }, []);

  /** Removes the persisted draft for the active session. */
  const clearDraft = useCallback(() => {
    const activeId = window.localStorage.getItem(ACTIVE_DRAFT_KEY);
    if (activeId) {
      window.localStorage.removeItem(INTERVIEW_DRAFT_PREFIX + activeId);
    }
    window.localStorage.removeItem(ACTIVE_DRAFT_KEY);
  }, []);

  /** Enters the setup screen for a category. */
  const handleSelectCategory = useCallback((category: InterviewCategory) => {
    setSelectedCategory(category);
    setConfig(null);
    setStartError(null);
    setStage('setup');
  }, []);

  /** Starts a new interview with the chosen configuration. */
  const handleStart = useCallback(
    async (setupConfig: InterviewSetupConfig) => {
      if (!selectedCategory) {
        return;
      }
      setStarting(true);
      setStartError(null);
      setFeedback(null);
      setReportData(null);
      try {
        const result = await aiService.startInterview({
          interviewType: selectedCategory,
          difficulty: setupConfig.difficulty,
          questionLength: setupConfig.questionLength,
          timed: setupConfig.timed,
        });
        setSession(result);
        setLastConfig(setupConfig);
        setConfig(setupConfig);
        setAnswers({});
        setCurrentIndex(0);
        setElapsedSeconds(0);
        clearDraft();
        setStage('answering');
      } catch (err) {
        setStartError(getErrorMessage(err, MESSAGES.MOCK_INTERVIEW_START_ERROR));
      } finally {
        setStarting(false);
      }
    },
    [selectedCategory, clearDraft],
  );

  /** Submits the whole interview for AI evaluation. */
  const submitInterview = useCallback(
    async (finalAnswers: Record<string, string>) => {
      const current = sessionRef.current;
      if (!current) {
        return;
      }
      setSubmitting(true);
      setSubmitError(null);
      try {
        const result = await aiService.submitInterview({
          sessionId: current.sessionId,
          interviewType: current.interviewType,
          difficulty: configRef.current?.difficulty,
          timed: configRef.current?.timed,
          durationSeconds: elapsedRef.current,
          answers: current.questions.map((question) => ({
            questionId: question.id,
            question: question.question,
            answer:
              finalAnswers[question.id]?.trim() ||
              'I did not get to answer this question in time.',
          })),
        });
        setFeedback(result);
        setReportData(null);
        setStage('report');
        clearDraft();
        toast.success(MESSAGES.INTERVIEW_SUBMITTED);
        fetchAll();
      } catch (err) {
        setSubmitError(getErrorMessage(err, MESSAGES.MOCK_INTERVIEW_SUBMIT_ERROR));
      } finally {
        setSubmitting(false);
      }
    },
    [clearDraft, fetchAll],
  );

  /** Saves the current answer and advances or submits. */
  const handleAnswerSubmit = useCallback(
    (answer: string) => {
      const current = sessionRef.current;
      if (!current) {
        return;
      }
      const question = current.questions[currentIndex];
      const nextAnswers = { ...answersRef.current, [question.id]: answer };
      answersRef.current = nextAnswers;
      setAnswers(nextAnswers);
      saveDraft();

      if (currentIndex < current.questions.length - 1) {
        setCurrentIndex((index) => index + 1);
      } else {
        void submitInterview(nextAnswers);
      }
    },
    [currentIndex, saveDraft, submitInterview],
  );

  /** Auto-saves the answer on every keystroke/transcript change. */
  const handleAnswerChange = useCallback(
    (questionId: string, answer: string) => {
      const nextAnswers = { ...answersRef.current, [questionId]: answer };
      answersRef.current = nextAnswers;
      setAnswers(nextAnswers);
      saveDraft();
    },
    [saveDraft],
  );

  /** Returns to the landing page. */
  const handleNewInterview = useCallback(() => {
    setStage('landing');
    setSession(null);
    setFeedback(null);
    setReportData(null);
    setSelectedCategory(null);
    setStartError(null);
    setSubmitError(null);
    setCurrentIndex(0);
    setAnswers({});
  }, []);

  /** Opens the setup screen for a retake, pre-filling the last config. */
  const handleRetake = useCallback(() => {
    const category = reportData?.interviewType ?? feedback?.interviewType;
    if (!category) {
      return;
    }
    setSelectedCategory(category);
    setConfig(lastConfig);
    setStage('setup');
  }, [reportData, feedback, lastConfig]);

  /** Views a past session's stored report. */
  const handleViewReport = useCallback((item: InterviewHistoryItem) => {
    setReportData(historyItemToReport(item));
    setFeedback(null);
    setStage('report');
  }, []);

  /** Deletes a session from the history. */
  const handleDelete = useCallback(
    async (sessionId: string) => {
      if (!window.confirm(MESSAGES.DELETE_CONFIRM('interview session'))) {
        return;
      }
      setDeletingSessionId(sessionId);
      try {
        await aiService.deleteInterviewSession(sessionId);
        toast.success(MESSAGES.INTERVIEW_DELETED);
        fetchAll();
      } catch (err) {
        toast.error(getErrorMessage(err, MESSAGES.INTERVIEW_DELETE_ERROR));
      } finally {
        setDeletingSessionId(null);
      }
    },
    [fetchAll],
  );

  /** Ticks the elapsed timer while answering. */
  useEffect(() => {
    if (stage !== 'answering') {
      return;
    }
    const interval = window.setInterval(() => {
      setElapsedSeconds((seconds) => seconds + 1);
    }, 1000);
    return () => window.clearInterval(interval);
  }, [stage]);

  const effectiveConfig = config ?? DEFAULT_INTERVIEW_CONFIG;

  return (
    <div className="animate-page-enter space-y-6">
      {/* Page header */}
      <PageHeader
        title="AI Mock Interview"
        description="Practise with AI-generated questions, answer by voice or text, and get a professional report with personalised coaching."
      />

      {/* Stage: landing */}
      {stage === 'landing' && (
        <MockInterviewLanding
          history={history}
          categories={categories}
          loading={historyLoading || categoriesLoading}
          error={historyError ?? categoriesError}
          onRetry={fetchAll}
          onSelectCategory={handleSelectCategory}
        />
      )}

      {/* Stage: setup */}
      {stage === 'setup' && selectedCategory && (
        <>
          {startError && <ErrorMessage message={startError} />}
          <MockInterviewSetup
            category={selectedCategory}
            starting={starting}
            defaultConfig={config ?? undefined}
            onStart={(setupConfig) => void handleStart(setupConfig)}
            onBack={handleNewInterview}
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
                if (current) {
                  void handleAnswerSubmit(answers[current.id] ?? '');
                }
              }}
            />
          )}
          <InterviewSessionCard
            key={session.questions[currentIndex]?.id}
            question={session.questions[currentIndex]}
            index={currentIndex}
            total={session.questions.length}
            sessionDifficulty={session.difficulty}
            timed={effectiveConfig.timed}
            timeLimitSeconds={TIMED_QUESTION_SECONDS}
            voiceMode={effectiveConfig.voiceMode}
            cameraEnabled={effectiveConfig.cameraEnabled}
            microphoneEnabled={effectiveConfig.microphoneEnabled}
            minWords={30}
            defaultAnswer={answers[session.questions[currentIndex]?.id ?? ''] ?? ''}
            submitting={submitting}
            elapsedSeconds={elapsedSeconds}
            onSubmit={handleAnswerSubmit}
            onBack={() => setCurrentIndex((index) => Math.max(0, index - 1))}
            onAnswerChange={(answer) => handleAnswerChange(session.questions[currentIndex].id, answer)}
          />
        </div>
      )}

      {/* Stage: report */}
      {stage === 'report' && (feedback || reportData) && (
        <div className="space-y-4">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex items-center gap-2">
              <Sparkles className="h-4 w-4 shrink-0 text-amber-500" />
              <p className="text-sm text-gray-500">
                {feedback
                  ? 'Interview complete — here is your AI feedback report.'
                  : 'Viewing a past interview report.'}
              </p>
            </div>
            {!feedback && (
              <Button variant="outline" size="sm" onClick={handleNewInterview}>
                <RotateCcw className="h-3.5 w-3.5" />
                Back to Tracks
              </Button>
            )}
          </div>
          <InterviewReportCard
            report={reportData ?? feedback!}
            onRetake={handleRetake}
            onNewInterview={handleNewInterview}
          />
        </div>
      )}

      {/* Analytics + history (not while answering) */}
      {stage !== 'answering' && (
        <>
          <InterviewAnalyticsCard
            data={history}
            loading={historyLoading}
            error={historyError}
            onRetry={fetchHistory}
          />
          <InterviewHistoryCard
            data={history}
            loading={historyLoading}
            error={historyError}
            onRetry={fetchHistory}
            onViewReport={handleViewReport}
            onRetake={(category) => handleSelectCategory(category)}
            onDelete={(sessionId) => void handleDelete(sessionId)}
            deletingSessionId={deletingSessionId}
          />
        </>
      )}
    </div>
  );
};
