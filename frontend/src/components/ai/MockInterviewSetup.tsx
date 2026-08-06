/**
 * MockInterviewSetup — the interview configuration screen shown before a
 * session starts.
 *
 * Lets the user choose the category (from the landing page), difficulty
 * (Easy / Medium / Hard / Mixed), interview length (5 / 10 / 15
 * questions), whether the session is timed, and the answer mode: voice
 * (with optional camera and microphone) or text. Shows coaching tips and
 * falls back to text mode when the browser has no speech recognition.
 *
 * @author DevLaunch
 */

import React, { useMemo, useState } from 'react';
import { ArrowLeft, Mic, Video, VideoOff, MicOff, Keyboard, Clock, Lightbulb, Play } from 'lucide-react';
import { Card } from '../ui/Card';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';
import { speechRecognitionSupported } from '../../hooks/useSpeechRecognition';
import { MESSAGES } from '../../constants/messages';
import {
  INTERVIEW_CATEGORY_META,
  INTERVIEW_DIFFICULTIES,
  INTERVIEW_LENGTHS,
  INTERVIEW_TIPS,
  MINUTES_PER_QUESTION,
} from '../../constants/interview';
import { InterviewCategory, InterviewDifficulty } from '../../types/ai';

/** The configuration produced by the setup screen. */
export interface InterviewSetupConfig {
  /** The difficulty mode of the interview. */
  difficulty: InterviewDifficulty;
  /** The number of questions (5, 10, or 15). */
  questionLength: number;
  /** Whether each question is timed. */
  timed: boolean;
  /** Whether the user answers by voice (speech-to-text). */
  voiceMode: boolean;
  /** Whether the optional camera preview is enabled. */
  cameraEnabled: boolean;
  /** Whether the microphone is enabled (voice mode only). */
  microphoneEnabled: boolean;
}

/** The default configuration for a new session. */
export const DEFAULT_INTERVIEW_CONFIG: InterviewSetupConfig = {
  difficulty: InterviewDifficulty.MIXED,
  questionLength: 10,
  timed: false,
  voiceMode: false,
  cameraEnabled: false,
  microphoneEnabled: false,
};

interface MockInterviewSetupProps {
  /** The selected interview category. */
  category: InterviewCategory;
  /** Whether the session is currently being started. */
  starting: boolean;
  /** The config to pre-fill (used when retaking a category). */
  defaultConfig?: InterviewSetupConfig;
  /** Called with the final configuration when the user starts. */
  onStart: (config: InterviewSetupConfig) => void;
  /** Called to return to the landing page. */
  onBack: () => void;
}

export const MockInterviewSetup: React.FC<MockInterviewSetupProps> = ({
  category,
  starting,
  defaultConfig,
  onStart,
  onBack,
}) => {
  const meta = INTERVIEW_CATEGORY_META[category];
  const speechSupported = useMemo(() => speechRecognitionSupported(), []);

  const [difficulty, setDifficulty] = useState<InterviewDifficulty>(
    defaultConfig?.difficulty ?? DEFAULT_INTERVIEW_CONFIG.difficulty,
  );
  const [questionLength, setQuestionLength] = useState<number>(
    defaultConfig?.questionLength ?? DEFAULT_INTERVIEW_CONFIG.questionLength,
  );
  const [timed, setTimed] = useState<boolean>(
    defaultConfig?.timed ?? DEFAULT_INTERVIEW_CONFIG.timed,
  );
  const [voiceMode, setVoiceMode] = useState<boolean>(
    defaultConfig?.voiceMode ?? DEFAULT_INTERVIEW_CONFIG.voiceMode,
  );
  const [cameraEnabled, setCameraEnabled] = useState<boolean>(
    defaultConfig?.cameraEnabled ?? DEFAULT_INTERVIEW_CONFIG.cameraEnabled,
  );
  const [microphoneEnabled, setMicrophoneEnabled] = useState<boolean>(
    defaultConfig?.microphoneEnabled ?? DEFAULT_INTERVIEW_CONFIG.microphoneEnabled,
  );

  const voiceUnsupported = voiceMode && !speechSupported;
  const estimatedMinutes = Math.round(questionLength * MINUTES_PER_QUESTION);

  const selectVoiceMode = (enabled: boolean) => {
    if (enabled && !speechSupported) {
      return;
    }
    setVoiceMode(enabled);
    if (!enabled) {
      setMicrophoneEnabled(false);
    }
  };

  const handleStart = () => {
    onStart({
      difficulty,
      questionLength,
      timed,
      voiceMode: voiceMode && speechSupported,
      cameraEnabled,
      microphoneEnabled: microphoneEnabled && voiceMode && speechSupported,
    });
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <button
          type="button"
          onClick={onBack}
          className="inline-flex w-fit items-center gap-1.5 text-sm font-medium text-gray-500 transition-colors hover:text-gray-900"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to tracks
        </button>
        <Badge variant="primary">{meta.label} interview</Badge>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Options */}
        <div className="space-y-5 lg:col-span-2">
          {/* Category summary */}
          <Card
            header={
              <div className="flex items-center gap-3">
                <div className={`flex h-10 w-10 items-center justify-center rounded-lg ${meta.iconClassName}`}>
                  <meta.icon className="h-5 w-5" />
                </div>
                <div>
                  <h3 className="text-sm font-semibold text-gray-900">
                    {meta.label} · {meta.description}
                  </h3>
                </div>
              </div>
            }
          >
            <div className="grid grid-cols-3 gap-3 text-center">
              <div className="rounded-lg bg-gray-50 p-3">
                <p className="text-lg font-bold text-gray-900">{questionLength}</p>
                <p className="text-xs text-gray-500">Questions</p>
              </div>
              <div className="rounded-lg bg-gray-50 p-3">
                <p className="text-lg font-bold text-gray-900">~{estimatedMinutes}m</p>
                <p className="text-xs text-gray-500">Estimated time</p>
              </div>
              <div className="rounded-lg bg-gray-50 p-3">
                <p className="text-lg font-bold text-gray-900">
                  {timed ? 'Timed' : 'Untimed'}
                </p>
                <p className="text-xs text-gray-500">Per question</p>
              </div>
            </div>
          </Card>

          {/* Difficulty */}
          <Card header={<h3 className="text-sm font-semibold text-gray-900">Difficulty</h3>}>
            <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
              {INTERVIEW_DIFFICULTIES.map((option) => {
                const selected = difficulty === option.value;
                return (
                  <button
                    key={option.value}
                    type="button"
                    onClick={() => setDifficulty(option.value)}
                    aria-pressed={selected}
                    className={`rounded-xl border p-3 text-left transition-all ${
                      selected
                        ? 'border-primary-500 bg-primary-50 ring-1 ring-primary-500'
                        : 'border-gray-200 bg-white hover:border-gray-300 hover:bg-gray-50'
                    }`}
                  >
                    <p className={`text-sm font-semibold ${selected ? 'text-primary-700' : 'text-gray-800'}`}>
                      {option.label}
                    </p>
                    <p className="mt-0.5 text-[11px] leading-snug text-gray-500">
                      {option.description}
                    </p>
                  </button>
                );
              })}
            </div>
          </Card>

          {/* Length + Timed */}
          <Card header={<h3 className="text-sm font-semibold text-gray-900">Interview length</h3>}>
            <div className="space-y-4">
              <div className="grid grid-cols-3 gap-3">
                {INTERVIEW_LENGTHS.map((length) => {
                  const selected = questionLength === length;
                  return (
                    <button
                      key={length}
                      type="button"
                      onClick={() => setQuestionLength(length)}
                      aria-pressed={selected}
                      className={`flex items-center justify-center gap-2 rounded-xl border px-3 py-3 transition-all ${
                        selected
                          ? 'border-primary-500 bg-primary-50 ring-1 ring-primary-500'
                          : 'border-gray-200 bg-white hover:border-gray-300 hover:bg-gray-50'
                      }`}
                    >
                      <span className={`text-base font-bold ${selected ? 'text-primary-700' : 'text-gray-800'}`}>
                        {length}
                      </span>
                      <span className="text-xs text-gray-500">questions</span>
                    </button>
                  );
                })}
              </div>

              <button
                type="button"
                onClick={() => setTimed(!timed)}
                aria-pressed={timed}
                className="flex w-full items-center justify-between rounded-xl border border-gray-200 px-4 py-3 text-left transition-colors hover:bg-gray-50"
              >
                <div className="flex items-center gap-3">
                  <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-indigo-100">
                    <Clock className="h-4 w-4 text-indigo-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-800">Timed interview</p>
                    <p className="text-xs text-gray-500">
                      {timed
                        ? '3 minutes per question — you advance automatically when time runs out.'
                        : 'No time limit — take as long as you need.'}
                    </p>
                  </div>
                </div>
                <span
                  className={`relative h-6 w-11 shrink-0 rounded-full transition-colors ${
                    timed ? 'bg-primary-600' : 'bg-gray-200'
                  }`}
                >
                  <span
                    className={`absolute top-0.5 h-5 w-5 rounded-full bg-white shadow transition-all ${
                      timed ? 'left-[22px]' : 'left-0.5'
                    }`}
                  />
                </span>
              </button>
            </div>
          </Card>

          {/* Mode: voice / text */}
          <Card header={<h3 className="text-sm font-semibold text-gray-900">Answer mode</h3>}>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <button
                  type="button"
                  onClick={() => selectVoiceMode(false)}
                  aria-pressed={!voiceMode}
                  className={`flex flex-col items-center gap-2 rounded-xl border px-4 py-4 transition-all ${
                    !voiceMode
                      ? 'border-primary-500 bg-primary-50 ring-1 ring-primary-500'
                      : 'border-gray-200 bg-white hover:border-gray-300 hover:bg-gray-50'
                  }`}
                >
                  <Keyboard className={`h-6 w-6 ${!voiceMode ? 'text-primary-600' : 'text-gray-400'}`} />
                  <div className="text-center">
                    <p className={`text-sm font-semibold ${!voiceMode ? 'text-primary-700' : 'text-gray-800'}`}>
                      Text mode
                    </p>
                    <p className="mt-0.5 text-[11px] text-gray-500">Type your answers</p>
                  </div>
                </button>

                <button
                  type="button"
                  onClick={() => selectVoiceMode(true)}
                  aria-pressed={voiceMode}
                  disabled={!speechSupported}
                  className={`flex flex-col items-center gap-2 rounded-xl border px-4 py-4 transition-all ${
                    voiceMode
                      ? 'border-primary-500 bg-primary-50 ring-1 ring-primary-500'
                      : speechSupported
                        ? 'border-gray-200 bg-white hover:border-gray-300 hover:bg-gray-50'
                        : 'cursor-not-allowed border-gray-200 bg-gray-50 opacity-60'
                  }`}
                >
                  <Mic className={`h-6 w-6 ${voiceMode ? 'text-primary-600' : 'text-gray-400'}`} />
                  <div className="text-center">
                    <p className={`text-sm font-semibold ${voiceMode ? 'text-primary-700' : 'text-gray-800'}`}>
                      Voice mode
                    </p>
                    <p className="mt-0.5 text-[11px] text-gray-500">
                      {speechSupported ? 'Answer with your voice' : 'Not supported in this browser'}
                    </p>
                  </div>
                </button>
              </div>

              {voiceUnsupported && (
                <p className="rounded-lg bg-amber-50 px-3 py-2 text-xs text-amber-700">
                  {MESSAGES.INTERVIEW_SPEECH_UNSUPPORTED}
                </p>
              )}

              {/* Camera + microphone */}
              <div className="grid gap-3 sm:grid-cols-2">
                <button
                  type="button"
                  onClick={() => setCameraEnabled(!cameraEnabled)}
                  aria-pressed={cameraEnabled}
                  className="flex items-center justify-between rounded-xl border border-gray-200 px-4 py-3 text-left transition-colors hover:bg-gray-50"
                >
                  <div className="flex items-center gap-3">
                    <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-gray-100">
                      {cameraEnabled ? (
                        <Video className="h-4 w-4 text-emerald-600" />
                      ) : (
                        <VideoOff className="h-4 w-4 text-gray-500" />
                      )}
                    </div>
                    <div>
                      <p className="text-sm font-medium text-gray-800">Camera</p>
                      <p className="text-xs text-gray-500">
                        {cameraEnabled ? 'Floating video preview' : 'Optional — not required'}
                      </p>
                    </div>
                  </div>
                  <span
                    className={`relative h-6 w-11 shrink-0 rounded-full transition-colors ${
                      cameraEnabled ? 'bg-primary-600' : 'bg-gray-200'
                    }`}
                  >
                    <span
                      className={`absolute top-0.5 h-5 w-5 rounded-full bg-white shadow transition-all ${
                        cameraEnabled ? 'left-[22px]' : 'left-0.5'
                      }`}
                    />
                  </span>
                </button>

                <button
                  type="button"
                  onClick={() => setMicrophoneEnabled(!microphoneEnabled)}
                  aria-pressed={microphoneEnabled}
                  disabled={!voiceMode}
                  className="flex items-center justify-between rounded-xl border border-gray-200 px-4 py-3 text-left transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  <div className="flex items-center gap-3">
                    <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-gray-100">
                      {microphoneEnabled ? (
                        <Mic className="h-4 w-4 text-emerald-600" />
                      ) : (
                        <MicOff className="h-4 w-4 text-gray-500" />
                      )}
                    </div>
                    <div>
                      <p className="text-sm font-medium text-gray-800">Microphone</p>
                      <p className="text-xs text-gray-500">
                        {voiceMode
                          ? microphoneEnabled
                            ? 'Enabled for voice answers'
                            : 'Disabled'
                          : 'Requires voice mode'}
                      </p>
                    </div>
                  </div>
                  <span
                    className={`relative h-6 w-11 shrink-0 rounded-full transition-colors ${
                      microphoneEnabled ? 'bg-primary-600' : 'bg-gray-200'
                    }`}
                  >
                    <span
                      className={`absolute top-0.5 h-5 w-5 rounded-full bg-white shadow transition-all ${
                        microphoneEnabled ? 'left-[22px]' : 'left-0.5'
                      }`}
                    />
                  </span>
                </button>
              </div>
            </div>
          </Card>

          <div className="flex justify-end">
            <Button size="lg" onClick={handleStart} loading={starting}>
              <Play className="h-4 w-4" />
              {starting ? 'Preparing your interview…' : 'Start Interview'}
            </Button>
          </div>
        </div>

        {/* Tips */}
        <Card
          header={
            <div className="flex items-center gap-2">
              <Lightbulb className="h-4 w-4 text-amber-500" />
              <h3 className="text-sm font-semibold text-gray-900">Interview tips</h3>
            </div>
          }
          className="h-fit"
        >
          <ul className="space-y-3">
            {INTERVIEW_TIPS.map((tip, index) => (
              <li key={index} className="flex items-start gap-2.5 text-sm leading-relaxed text-gray-600">
                <span className="mt-1.5 h-1.5 w-1.5 shrink-0 rounded-full bg-primary-400" />
                {tip}
              </li>
            ))}
          </ul>
        </Card>
      </div>
    </div>
  );
};
