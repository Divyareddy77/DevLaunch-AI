/**
 * InterviewSessionCard — the professional mock interview experience.
 *
 * Shows one question at a time with the progress bar, elapsed timer, an
 * optional per-question countdown, the difficulty badge, a large answer
 * area with word count, voice answer mode with live speaking analysis, and
 * an optional floating webcam preview. Answers are pushed up to the page
 * on every change so they can be auto-saved.
 *
 * Voice answers are recorded with MediaRecorder, uploaded to the backend
 * for OpenAI Whisper transcription, and the resulting transcript is
 * automatically inserted into the answer box where the user can edit it
 * before submitting.
 *
 * @author DevLaunch
 */

import React, { useCallback, useEffect, useRef, useState } from 'react';
import {
  HelpCircle,
  ArrowLeft,
  ArrowRight,
  Check,
  Clock,
  Timer,
  Mic,
  Pause,
  Square,
  Play,
  Type,
  Loader2,
} from 'lucide-react';
import { Card } from '../ui/Card';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';
import { useMediaRecorder } from '../../hooks/useMediaRecorder';
import { useWebcam } from '../../hooks/useWebcam';
import { analyzeSpeaking } from '../../utils/speaking';
import { wordCount } from '../../utils/interview';
import { formatClock, enumToLabel } from '../../utils/format';
import { SpeakingMetricsPanel } from './SpeakingMetricsPanel';
import { WebcamOverlay } from './WebcamOverlay';
import { aiService } from '../../services/ai.service';
import { getErrorMessage } from '../../utils/error';
import { MESSAGES } from '../../constants/messages';
import type { InterviewQuestion, InterviewDifficulty } from '../../types/ai';

interface InterviewSessionCardProps {
  /** The question currently being answered. */
  question: InterviewQuestion;
  /** Zero-based index of the current question. */
  index: number;
  /** Total number of questions in the session. */
  total: number;
  /** The session difficulty mode (from the setup screen). */
  sessionDifficulty: InterviewDifficulty;
  /** Whether the session is timed. */
  timed: boolean;
  /** The per-question time limit in seconds. */
  timeLimitSeconds: number;
  /** Whether the user answers by voice. */
  voiceMode: boolean;
  /** Whether the optional camera preview is enabled. */
  cameraEnabled: boolean;
  /** Whether the microphone is enabled. */
  microphoneEnabled: boolean;
  /** The minimum recommended answer length in words. */
  minWords: number;
  /** The answer already stored for this question, if any. */
  defaultAnswer: string;
  /** Whether the interview is currently being submitted. */
  submitting: boolean;
  /** The total elapsed interview time in seconds. */
  elapsedSeconds: number;
  /** Called with the answer when the user advances or finishes. */
  onSubmit: (answer: string) => void;
  /** Called when the user navigates to the previous question. */
  onBack: () => void;
  /** Called on every answer change so the page can auto-save. */
  onAnswerChange: (answer: string) => void;
}

export const InterviewSessionCard: React.FC<InterviewSessionCardProps> = ({
  question,
  index,
  total,
  sessionDifficulty,
  timed,
  timeLimitSeconds,
  voiceMode,
  cameraEnabled,
  microphoneEnabled,
  minWords,
  defaultAnswer,
  submitting,
  elapsedSeconds,
  onSubmit,
  onBack,
  onAnswerChange,
}) => {
  const [answer, setAnswer] = useState<string>(defaultAnswer);
  const [transcribing, setTranscribing] = useState(false);
  const [transcriptionError, setTranscriptionError] = useState<string | null>(null);
  const baselineRef = useRef<string>('');
  const autoAdvancedRef = useRef(false);
  // Guards against redundant syncs: the parent rebuilds the onAnswerChange
  // callback on every render, so the sync effect would otherwise loop.
  const lastSyncedRef = useRef<string>('');

  const recorder = useMediaRecorder();
  const webcam = useWebcam(cameraEnabled);
  // Recording/transcription in flight: blocks navigation and timer advance
  // so a transcript is never lost or applied to the wrong question.
  const recorderBusy = transcribing || recorder.recording;

  // Reset the editable answer and recorder whenever the question changes.
  // The reset callback is stable, so it is safe to omit from the deps.
  const resetRecorder = recorder.reset;
  useEffect(() => {
    setAnswer(defaultAnswer);
    setTranscribing(false);
    setTranscriptionError(null);
    autoAdvancedRef.current = false;
    lastSyncedRef.current = '';
    resetRecorder();
  }, [question.id, defaultAnswer, resetRecorder]);

  // Per-question countdown for timed sessions.
  const [remaining, setRemaining] = useState<number>(timeLimitSeconds);
  useEffect(() => {
    setRemaining(timeLimitSeconds);
    if (!timed) {
      return;
    }
    const interval = window.setInterval(() => {
      setRemaining((seconds) => {
        if (seconds <= 1) {
          window.clearInterval(interval);
          return 0;
        }
        return seconds - 1;
      });
    }, 1000);
    return () => window.clearInterval(interval);
  }, [question.id, timed, timeLimitSeconds]);

  // Auto-advance when the per-question timer expires — but never while a
  // recording is running or a transcript is pending, so the spoken answer
  // is captured first.
  useEffect(() => {
    if (timed && remaining === 0 && !autoAdvancedRef.current && !recorderBusy) {
      autoAdvancedRef.current = true;
      onSubmit(answer);
    }
  }, [timed, remaining, answer, onSubmit, recorderBusy]);

  // ---- Voice transcript sync ----
  const syncTranscript = useCallback(
    (transcript: string) => {
      const combined = baselineRef.current
        ? `${baselineRef.current} ${transcript}`.trim()
        : transcript.trim();
      // Skip when nothing changed so an unstable onAnswerChange identity
      // cannot push the parent into an infinite re-render loop.
      if (combined === lastSyncedRef.current) {
        return;
      }
      lastSyncedRef.current = combined;
      setAnswer(combined);
      onAnswerChange(combined);
    },
    [onAnswerChange],
  );

  const handleStartRecording = () => {
    baselineRef.current = answer;
    lastSyncedRef.current = answer;
    setTranscriptionError(null);
    recorder.start();
  };

  const handleStopRecording = async () => {
    const result = await recorder.stop();
    if (!result) {
      // The recorder surfaced an error (e.g. no audio was captured).
      return;
    }
    setTranscribing(true);
    setTranscriptionError(null);
    try {
      const { transcript } = await aiService.transcribe(
        result.blob,
        result.durationSeconds,
      );
      const clean = transcript.trim();
      if (clean) {
        syncTranscript(clean);
      } else {
        setTranscriptionError(MESSAGES.INTERVIEW_TRANSCRIPTION_EMPTY);
      }
    } catch (err) {
      setTranscriptionError(
        getErrorMessage(err, MESSAGES.INTERVIEW_TRANSCRIPTION_ERROR),
      );
    } finally {
      setTranscribing(false);
    }
  };

  const speakingMetrics = analyzeSpeaking(
    answer,
    recorder.elapsedSeconds,
    recorder.longPauses,
  );
  const showMetrics =
    voiceMode && (recorder.recording || recorder.elapsedSeconds > 0);

  const words = wordCount(answer);
  const isLast = index === total - 1;
  const progress = ((index + 1) / total) * 100;
  const canProceed = answer.trim().length > 0;
  const timeUp = timed && remaining === 0;

  const handleAnswerEdit = (next: string) => {
    setAnswer(next);
    onAnswerChange(next);
  };

  const countdownTone =
    remaining <= 30 ? 'text-red-500' : remaining <= 60 ? 'text-amber-500' : 'text-gray-600';

  return (
    <Card>
      <div className="space-y-5">
        {/* Progress header */}
        <div>
          <div className="flex flex-wrap items-center justify-between gap-2">
            <p className="text-sm font-medium text-gray-500">
              Question {index + 1} of {total}
            </p>
            <div className="flex items-center gap-2">
              <Badge variant="primary" size="sm">
                {enumToLabel(sessionDifficulty)}
              </Badge>
              {question.difficulty && (
                <Badge variant="info" size="sm">
                  {enumToLabel(question.difficulty)}
                </Badge>
              )}
            </div>
          </div>
          <div className="mt-2 h-2 w-full overflow-hidden rounded-full bg-gray-100">
            <div
              className="h-2 rounded-full bg-primary-600 transition-all duration-500"
              style={{ width: `${progress}%` }}
            />
          </div>
        </div>

        {/* Timer row */}
        <div className="flex flex-wrap items-center justify-between gap-2 rounded-lg bg-gray-50 px-3 py-2">
          <p className="text-sm font-medium text-gray-600">
            {isLast ? 'Final question' : `${total - index - 1} remaining`}
          </p>
          <div className="flex items-center gap-4 text-sm">
            {timed && (
              <span className={`inline-flex items-center gap-1.5 font-mono font-semibold ${countdownTone}`}>
                <Timer className="h-4 w-4" />
                {timeUp ? 'Time\'s up' : formatClock(remaining)}
              </span>
            )}
            <span className="inline-flex items-center gap-1.5 font-mono font-semibold text-gray-600">
              <Clock className="h-4 w-4" />
              {formatClock(elapsedSeconds)}
            </span>
          </div>
        </div>

        {/* Question */}
        <div>
          <h3 className="text-base font-semibold leading-relaxed text-gray-900">
            {question.question}
          </h3>
          {question.hint && (
            <div className="mt-3 flex items-start gap-2 rounded-lg bg-blue-50 p-3">
              <HelpCircle className="mt-0.5 h-4 w-4 shrink-0 text-blue-500" />
              <p className="text-sm leading-relaxed text-blue-700">{question.hint}</p>
            </div>
          )}
        </div>

        {/* Voice recorder (voice mode only) */}
        {voiceMode && (
          <div className="rounded-xl border border-gray-200 p-3">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div className="flex items-center gap-2">
                {transcribing ? (
                  <span className="inline-flex items-center gap-1.5 rounded-full bg-amber-50 px-2.5 py-1 text-xs font-semibold text-amber-700">
                    <Loader2 className="h-3 w-3 animate-spin" />
                    {MESSAGES.INTERVIEW_TRANSCRIBING}
                  </span>
                ) : recorder.recording ? (
                  <span className="inline-flex items-center gap-1.5 rounded-full bg-red-50 px-2.5 py-1 text-xs font-semibold text-red-600">
                    <span className="h-2 w-2 animate-pulse rounded-full bg-red-500" />
                    {recorder.paused ? 'Paused' : 'Recording'} · {formatClock(recorder.elapsedSeconds)}
                  </span>
                ) : (
                  <span className="inline-flex items-center gap-1.5 rounded-full bg-gray-100 px-2.5 py-1 text-xs font-medium text-gray-600">
                    <Type className="h-3 w-3" />
                    {recorder.supported ? 'Ready to record' : MESSAGES.INTERVIEW_RECORDING_UNSUPPORTED}
                  </span>
                )}
                {/* Microphone status (live while the stream is held) */}
                {recorder.micActive && (
                  <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-semibold text-emerald-700">
                    <Mic className="h-3 w-3" />
                    Mic on
                  </span>
                )}
              </div>

              <div className="flex items-center gap-2">
                {transcribing ? (
                  <span className="text-xs text-gray-400">Hold on…</span>
                ) : !recorder.recording ? (
                  <Button
                    size="sm"
                    onClick={handleStartRecording}
                    disabled={!recorder.supported}
                  >
                    <Mic className="h-3.5 w-3.5" />
                    Start Recording
                  </Button>
                ) : recorder.paused ? (
                  <Button size="sm" onClick={recorder.resume}>
                    <Play className="h-3.5 w-3.5" />
                    Resume
                  </Button>
                ) : (
                  <Button size="sm" variant="outline" onClick={recorder.pause}>
                    <Pause className="h-3.5 w-3.5" />
                    Pause
                  </Button>
                )}
                {recorder.recording && !transcribing && (
                  <Button size="sm" variant="danger" onClick={() => void handleStopRecording()}>
                    <Square className="h-3.5 w-3.5" />
                    Stop
                  </Button>
                )}
              </div>
            </div>

            {(recorder.error || transcriptionError) && (
              <p
                className="mt-3 rounded-lg bg-amber-50 px-3 py-2 text-xs leading-relaxed text-amber-700"
                role="alert"
              >
                {recorder.error ?? transcriptionError}
              </p>
            )}

            {showMetrics && <div className="mt-3"><SpeakingMetricsPanel metrics={speakingMetrics} /></div>}
          </div>
        )}

        {/* Answer */}
        <div>
          <div className="mb-1.5 flex items-center justify-between gap-3">
            <label
              htmlFor={`answer-${question.id}`}
              className="block text-sm font-medium text-gray-700"
            >
              Your Answer
            </label>
            <div className="flex items-center gap-3 text-xs">
              <span
                className={`font-semibold ${
                  words === 0
                    ? 'text-gray-400'
                    : words >= minWords
                      ? 'text-emerald-600'
                      : 'text-amber-600'
                }`}
              >
                {words} words
              </span>
              <span className="text-gray-400">min {minWords}</span>
            </div>
          </div>
          <textarea
            id={`answer-${question.id}`}
            rows={8}
            placeholder="Write or dictate your answer here — be specific and give concrete examples…"
            disabled={submitting || timeUp || recorderBusy}
            value={answer}
            onChange={(event) => handleAnswerEdit(event.target.value)}
            className={`
              block w-full resize-y rounded-lg border bg-white px-3 py-2 text-sm text-gray-900
              placeholder-gray-400 transition-colors focus:outline-none focus:ring-2 focus:ring-offset-0
              ${
                timeUp
                  ? 'border-red-300 bg-red-50'
                  : 'border-gray-300 focus:border-primary-500 focus:ring-primary-500'
              }
              disabled:cursor-not-allowed disabled:opacity-60
            `}
          />
          {timeUp && (
            <p className="mt-1.5 text-xs text-red-500" role="alert">
              Time&apos;s up — your answer was saved automatically.
            </p>
          )}
          {words > 0 && words < minWords && (
            <p className="mt-1.5 text-xs text-amber-600">
              Add a few more words — {minWords - words} to reach the recommended depth.
            </p>
          )}
          {transcribing && (
            <p className="mt-1.5 text-xs text-gray-400" role="status">
              Your transcript will appear here automatically and can be edited before submitting.
            </p>
          )}
        </div>

        {/* Navigation */}
        <div className="flex items-center justify-between gap-3">
          <Button
            type="button"
            variant="outline"
            onClick={onBack}
            disabled={index === 0 || submitting || recorderBusy}
          >
            <ArrowLeft className="h-4 w-4" />
            Back
          </Button>

          <Button
            type="button"
            loading={submitting}
            disabled={!canProceed || submitting || recorderBusy}
            onClick={() => onSubmit(answer)}
          >
            {isLast ? (
              <>
                <Check className="h-4 w-4" />
                {submitting ? 'Submitting…' : 'Finish Interview'}
              </>
            ) : (
              <>
                Next Question
                <ArrowRight className="h-4 w-4" />
              </>
            )}
          </Button>
        </div>
      </div>

      {/* Floating webcam preview (optional, never required) */}
      <WebcamOverlay
        stream={webcam.stream}
        status={webcam.status}
        recording={recorder.recording && voiceMode}
        microphoneEnabled={voiceMode && microphoneEnabled}
        elapsedSeconds={elapsedSeconds}
      />
    </Card>
  );
};
