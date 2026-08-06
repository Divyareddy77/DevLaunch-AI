/**
 * useSpeechRecognition — a typed wrapper around the browser Web Speech API
 * tuned for long, continuous mock-interview answers.
 *
 * Reliability model
 * -----------------
 * - One fresh {@code SpeechRecognition} instance per capture session.
 *   Chrome and Edge refuse to restart a recognizer that the browser has
 *   already ended, so every restart tears the previous instance down and
 *   creates a new one. This is what keeps 1–2 minute answers flowing
 *   without the user having to touch the recorder.
 * - The microphone keeps listening until the user clicks Stop, the
 *   interview ends, or permission is revoked. When the browser ends a
 *   session (silence timeout) or fires a recoverable error
 *   ({@code no-speech}, {@code audio-capture}, {@code network},
 *   {@code aborted}), a restart is scheduled automatically after a short
 *   delay.
 * - Infinite-loop protection: auto-restarts are counted, and a session
 *   that keeps ending or erroring without producing a single final result
 *   gives up with a friendly message instead of spinning forever.
 * - Transcripts: final results accumulate across restarts; interim results
 *   belong to the current session only, so nothing is overwritten or
 *   duplicated. Every result from {@code event.resultIndex} through
 *   {@code event.results.length} is consumed.
 *
 * Falls back gracefully: when the browser has no speech recognition,
 * {@code supported} is {@code false} and the caller switches to text mode.
 *
 * @author DevLaunch
 */

import { useCallback, useEffect, useRef, useState } from 'react';
import { MESSAGES } from '../constants/messages';

/** Minimal structural types for the Web Speech API (absent from the TS DOM lib). */
interface SpeechRecognitionAlternative {
  readonly transcript: string;
}

interface SpeechRecognitionResult {
  readonly isFinal: boolean;
  readonly [index: number]: SpeechRecognitionAlternative;
}

interface SpeechRecognitionResultList {
  readonly length: number;
  readonly [index: number]: SpeechRecognitionResult;
}

interface SpeechRecognitionEvent {
  readonly resultIndex: number;
  readonly results: SpeechRecognitionResultList;
}

interface SpeechRecognitionErrorEvent {
  readonly error: string;
}

interface SpeechRecognitionInstance {
  continuous: boolean;
  interimResults: boolean;
  maxAlternatives: number;
  lang: string;
  onresult: ((event: SpeechRecognitionEvent) => void) | null;
  onend: (() => void) | null;
  onerror: ((event: SpeechRecognitionErrorEvent) => void) | null;
  start: () => void;
  stop: () => void;
  abort: () => void;
}

type SpeechRecognitionConstructor = new () => SpeechRecognitionInstance;

/** A pause longer than this (ms) counts as a long pause. */
const LONG_PAUSE_THRESHOLD_MS = 2500;

/** Delay before an automatic restart after the browser ends a session. */
const AUTO_RESTART_DELAY_MS = 350;

/** Max silent auto-restarts before giving up (prevents infinite loops). */
const MAX_AUTO_RESTARTS = 15;

/**
 * A browser session usually reports one failure as both an error event and
 * an end event. Failures within this window (ms) count as a single one.
 */
const FAILURE_DEDUPE_WINDOW_MS = 1000;

/** Errors the browser fires that are safe to recover from by restarting. */
const RECOVERABLE_ERRORS: ReadonlySet<string> = new Set([
  'no-speech',
  'audio-capture',
  'network',
  'aborted',
]);

function getRecognitionConstructor(): SpeechRecognitionConstructor | null {
  if (typeof window === 'undefined') {
    return null;
  }
  const globalScope = window as unknown as {
    SpeechRecognition?: SpeechRecognitionConstructor;
    webkitSpeechRecognition?: SpeechRecognitionConstructor;
  };
  return globalScope.SpeechRecognition ?? globalScope.webkitSpeechRecognition ?? null;
}

/**
 * Whether the current browser supports the Web Speech API.
 */
export function speechRecognitionSupported(): boolean {
  return getRecognitionConstructor() !== null;
}

export interface SpeechRecognitionControls {
  /** Whether the browser supports speech recognition. */
  supported: boolean;
  /** Whether the recorder is currently capturing audio. */
  listening: boolean;
  /** Whether the recorder is paused (capture stopped, session kept). */
  paused: boolean;
  /** The accumulated final transcript. */
  finalTranscript: string;
  /** The live interim (not-yet-final) transcript. */
  interimTranscript: string;
  /** The seconds of active (unpaused) recording. */
  speakingSeconds: number;
  /** The number of long pauses detected while recording. */
  longPauses: number;
  /** A permission/support error message, if any. */
  error: string | null;
  /** Starts (or resumes) recording. */
  start: () => void;
  /** Pauses recording, keeping the session alive. */
  pause: () => void;
  /** Resumes a paused recording. */
  resume: () => void;
  /** Stops recording and finalizes the transcript. */
  stop: () => void;
  /** Stops recording and clears the transcript and metrics. */
  reset: () => void;
}

/**
 * Temporarily logs speech lifecycle events so the flow can be verified in
 * the browser console. Remove once speech reliability is confirmed.
 */
function debugLog(...args: unknown[]): void {
  // eslint-disable-next-line no-console
  console.log('[Speech]', ...args);
}

export function useSpeechRecognition(): SpeechRecognitionControls {
  const constructorRef = useRef<SpeechRecognitionConstructor | null>(null);
  if (constructorRef.current === null) {
    constructorRef.current = getRecognitionConstructor();
  }
  const supported = constructorRef.current !== null;

  const recognitionRef = useRef<SpeechRecognitionInstance | null>(null);
  const shouldListenRef = useRef(false);
  const pausedRef = useRef(false);
  const lastResultAtRef = useRef(Date.now());
  const restartTimerRef = useRef<number | null>(null);
  const silentRestartCountRef = useRef(0);
  const lastFailureAtRef = useRef<number>(0);

  const [listening, setListening] = useState(false);
  const [paused, setPaused] = useState(false);
  const [finalTranscript, setFinalTranscript] = useState('');
  const [interimTranscript, setInterimTranscript] = useState('');
  const [speakingSeconds, setSpeakingSeconds] = useState(0);
  const [longPauses, setLongPauses] = useState(0);
  const [error, setError] = useState<string | null>(null);

  // The session start and restart functions call each other through refs,
  // which keeps both closures free of stale state without re-subscribing.
  const startSessionRef = useRef<() => void>(() => {});
  const scheduleRestartRef = useRef<(delayMs: number) => void>(() => {});

  /** Detaches every handler and aborts the active recognizer, if any. */
  const teardownSession = useCallback(() => {
    const current = recognitionRef.current;
    if (current === null) {
      return;
    }
    current.onresult = null;
    current.onend = null;
    current.onerror = null;
    try {
      current.abort();
    } catch {
      // Already stopped — nothing to do.
    }
    recognitionRef.current = null;
  }, []);

  /** Stops listening entirely and releases the microphone. */
  const stopListening = useCallback(() => {
    shouldListenRef.current = false;
    pausedRef.current = false;
    if (restartTimerRef.current !== null) {
      window.clearTimeout(restartTimerRef.current);
      restartTimerRef.current = null;
    }
    teardownSession();
    setListening(false);
    setPaused(false);
    debugLog('recorder stopped');
  }, [teardownSession]);

  /**
   * Counts a silent failure and gives up after too many consecutive ones.
   * Error + end events from the same browser failure are deduped so they
   * count as a single restart attempt.
   */
  const countSilentRestart = useCallback((): boolean => {
    const now = Date.now();
    if (now - lastFailureAtRef.current < FAILURE_DEDUPE_WINDOW_MS) {
      return true;
    }
    lastFailureAtRef.current = now;
    silentRestartCountRef.current += 1;
    if (silentRestartCountRef.current > MAX_AUTO_RESTARTS) {
      debugLog(`giving up after ${MAX_AUTO_RESTARTS} silent restarts`);
      stopListening();
      setError(MESSAGES.INTERVIEW_SPEECH_RECOVERY_FAILED);
      return false;
    }
    return true;
  }, [stopListening]);

  /** Handles a permission denial — unrecoverable, show a friendly message. */
  const handlePermissionDenied = useCallback(() => {
    debugLog('microphone permission denied');
    stopListening();
    setError(MESSAGES.INTERVIEW_SPEECH_PERMISSION_DENIED);
  }, [stopListening]);

  /** Handles an unexpected, unrecoverable recognition failure. */
  const handleFatalError = useCallback(() => {
    debugLog('unrecoverable recognition error');
    stopListening();
    setError(MESSAGES.INTERVIEW_SPEECH_RECOVERY_FAILED);
  }, [stopListening]);

  /** Handles the browser ending a session (silence timeout). */
  const handleSessionEnded = useCallback(() => {
    debugLog('recognition ended — restarting');
    if (!countSilentRestart()) {
      return;
    }
    scheduleRestartRef.current(AUTO_RESTART_DELAY_MS);
  }, [countSilentRestart]);

  /** Handles a recoverable error (no-speech, audio-capture, network, aborted). */
  const handleRecoverableError = useCallback((code: string) => {
    debugLog(`recoverable error '${code}' — restarting`);
    if (!countSilentRestart()) {
      return;
    }
    scheduleRestartRef.current(AUTO_RESTART_DELAY_MS);
  }, [countSilentRestart]);

  /**
   * Creates a fresh recognizer, wires the handlers, and starts capturing.
   * Safe to call repeatedly — the previous instance is torn down first, so
   * there is never more than one recognizer alive at a time.
   */
  const startSession = useCallback(() => {
    const Recognition = constructorRef.current;
    if (Recognition === null) {
      return;
    }

    // Reset the failure timestamp so this session's failure is always
    // counted fresh — without this, fast-failing browsers would have every
    // failure deduped against the previous one and the loop guard could
    // never trip.
    lastFailureAtRef.current = 0;
    teardownSession();

    const recognition = new Recognition();
    recognition.continuous = true;
    recognition.interimResults = true;
    recognition.maxAlternatives = 1;
    recognition.lang = 'en-US';

    recognition.onresult = (event) => {
      lastResultAtRef.current = Date.now();

      // Consume every result from resultIndex onward, not just the latest.
      let interim = '';
      let finalChunk = '';
      for (let i = event.resultIndex; i < event.results.length; i++) {
        const result = event.results[i];
        const transcript = result[0]?.transcript ?? '';
        if (result.isFinal) {
          finalChunk += transcript;
        } else {
          interim += transcript;
        }
      }

      if (finalChunk) {
        // A healthy session produced real speech — reset the restart guard.
        silentRestartCountRef.current = 0;
        debugLog('final transcript:', finalChunk.trim());
        setFinalTranscript((previous) =>
          previous ? `${previous} ${finalChunk.trim()}` : finalChunk.trim(),
        );
      }
      if (interim) {
        debugLog('interim transcript:', interim);
      }
      setInterimTranscript(interim);
    };

    recognition.onend = () => {
      if (shouldListenRef.current && !pausedRef.current) {
        handleSessionEnded();
      }
    };

    recognition.onerror = (event) => {
      debugLog('recognition error:', event.error);
      if (event.error === 'not-allowed' || event.error === 'service-not-allowed') {
        handlePermissionDenied();
        return;
      }
      if (!shouldListenRef.current || pausedRef.current) {
        return;
      }
      if (RECOVERABLE_ERRORS.has(event.error)) {
        handleRecoverableError(event.error);
        return;
      }
      handleFatalError();
    };

    recognitionRef.current = recognition;
    try {
      recognition.start();
      debugLog('recognition started');
    } catch (cause) {
      debugLog('start() threw, retrying:', cause);
      if (shouldListenRef.current && !pausedRef.current && countSilentRestart()) {
        scheduleRestartRef.current(AUTO_RESTART_DELAY_MS);
      }
    }
  }, [countSilentRestart, handlePermissionDenied, handleRecoverableError,
      handleFatalError, handleSessionEnded, teardownSession]);

  /** Schedules a single auto-restart; any pending restart is replaced. */
  const scheduleRestart = useCallback((delayMs: number) => {
    if (restartTimerRef.current !== null) {
      window.clearTimeout(restartTimerRef.current);
    }
    restartTimerRef.current = window.setTimeout(() => {
      restartTimerRef.current = null;
      if (shouldListenRef.current && !pausedRef.current) {
        debugLog('restart triggered');
        startSessionRef.current();
      }
    }, delayMs);
  }, []);

  startSessionRef.current = startSession;
  scheduleRestartRef.current = scheduleRestart;

  /** Ticks speaking time and pause detection once per second. */
  useEffect(() => {
    const interval = window.setInterval(() => {
      if (shouldListenRef.current && !pausedRef.current) {
        setSpeakingSeconds((seconds) => seconds + 1);
        if (Date.now() - lastResultAtRef.current > LONG_PAUSE_THRESHOLD_MS) {
          setLongPauses((pauses) => pauses + 1);
          lastResultAtRef.current = Date.now();
        }
      }
    }, 1000);
    return () => window.clearInterval(interval);
  }, []);

  /** Releases the microphone and pending restarts on unmount. */
  useEffect(() => {
    return () => {
      shouldListenRef.current = false;
      if (restartTimerRef.current !== null) {
        window.clearTimeout(restartTimerRef.current);
        restartTimerRef.current = null;
      }
      teardownSession();
    };
  }, [teardownSession]);

  const start = useCallback(() => {
    if (constructorRef.current === null) {
      return;
    }
    setError(null);
    shouldListenRef.current = true;
    pausedRef.current = false;
    silentRestartCountRef.current = 0;
    lastResultAtRef.current = Date.now();
    setListening(true);
    setPaused(false);
    setInterimTranscript('');
    startSessionRef.current();
  }, []);

  const pause = useCallback(() => {
    if (!shouldListenRef.current) {
      return;
    }
    pausedRef.current = true;
    setPaused(true);
    if (restartTimerRef.current !== null) {
      window.clearTimeout(restartTimerRef.current);
      restartTimerRef.current = null;
    }
    teardownSession();
    debugLog('recorder paused');
  }, [teardownSession]);

  const resume = useCallback(() => {
    if (!shouldListenRef.current) {
      return;
    }
    pausedRef.current = false;
    setPaused(false);
    lastResultAtRef.current = Date.now();
    startSessionRef.current();
    debugLog('recorder resumed');
  }, []);

  const stop = useCallback(() => {
    stopListening();
  }, [stopListening]);

  const reset = useCallback(() => {
    stopListening();
    setFinalTranscript('');
    setInterimTranscript('');
    setSpeakingSeconds(0);
    setLongPauses(0);
    setError(null);
  }, [stopListening]);

  return {
    supported,
    listening,
    paused,
    finalTranscript,
    interimTranscript,
    speakingSeconds,
    longPauses,
    error,
    start,
    pause,
    resume,
    stop,
    reset,
  };
}
