/**
 * useMediaRecorder — MediaRecorder-based voice capture for mock interviews.
 *
 * Replaces the browser Web Speech API with reliable local audio recording:
 * the microphone stream is captured with MediaRecorder, audio chunks are
 * collected into a single Blob on stop, and the blob is handed to the
 * caller so it can be transcribed by the backend (OpenAI Whisper) for
 * accurate, long-answer transcripts (1–5+ minutes).
 *
 * Reliability model
 * -----------------
 * - One fresh {@code MediaRecorder} per capture session; chunks accumulate
 *   and are combined into a single audio Blob when recording stops.
 * - The recorder supports Start / Pause / Resume / Stop, with an elapsed
 *   timer that only counts active (unpaused) recording time.
 * - Long pauses (silence) are detected from the live audio level via the
 *   Web Audio API (AnalyserNode) — no webcam or speech events involved —
 *   and feed the speaking analytics and confidence estimate. Paused time
 *   never counts as a pause.
 * - Unsupported browsers, denied microphone permission, missing devices,
 *   and empty recordings all surface friendly error messages.
 *
 * @author DevLaunch
 */

import { useCallback, useEffect, useRef, useState } from 'react';
import { MESSAGES } from '../constants/messages';

/** The result of a completed recording session. */
export interface RecordingResult {
  /** The combined audio blob (a single file). */
  blob: Blob;
  /** The active recording duration in seconds (excludes pauses). */
  durationSeconds: number;
}

/** A pause longer than this (ms) counts as a long pause. */
const LONG_PAUSE_THRESHOLD_MS = 2500;

/** How often the silence detector samples the audio level (ms). */
const SILENCE_SAMPLE_INTERVAL_MS = 250;

/** RMS level below which the input is treated as silence. */
const SILENCE_RMS_THRESHOLD = 0.02;

/** How often audio chunks are collected while recording (ms). */
const CHUNK_TIMESLICE_MS = 250;

/** Delay before the recorder is force-finished if the stop event stalls. */
const STOP_TIMEOUT_MS = 3000;

/** Audio MIME types supported by Whisper, in preference order. */
const RECORDING_MIME_CANDIDATES = [
  'audio/webm;codecs=opus',
  'audio/webm',
  'audio/mp4',
  'audio/ogg;codecs=opus',
];

type AudioContextConstructor = typeof AudioContext;

function getAudioContextConstructor(): AudioContextConstructor | undefined {
  if (typeof window === 'undefined') {
    return undefined;
  }
  return (
    window.AudioContext ??
    (window as unknown as { webkitAudioContext?: AudioContextConstructor })
      .webkitAudioContext
  );
}

/** Picks the first supported recording MIME type, or '' when unsupported. */
function pickRecordingMimeType(): string {
  if (typeof MediaRecorder === 'undefined') {
    return '';
  }
  for (const candidate of RECORDING_MIME_CANDIDATES) {
    if (MediaRecorder.isTypeSupported(candidate)) {
      return candidate;
    }
  }
  return '';
}

/**
 * Whether the current browser can record audio (MediaRecorder + mic).
 */
export function mediaRecorderSupported(): boolean {
  return (
    typeof MediaRecorder !== 'undefined' &&
    typeof navigator !== 'undefined' &&
    typeof navigator.mediaDevices?.getUserMedia === 'function'
  );
}

/** Computes the root-mean-square of an analyser time-domain sample. */
function computeRms(data: Uint8Array): number {
  let sum = 0;
  for (let i = 0; i < data.length; i++) {
    const normalized = (data[i] - 128) / 128;
    sum += normalized * normalized;
  }
  return Math.sqrt(sum / data.length);
}

/** Maps a getUserMedia failure to a friendly, user-facing message. */
function mapMicrophoneError(cause: unknown): string {
  if (cause instanceof DOMException) {
    if (cause.name === 'NotAllowedError' || cause.name === 'PermissionDeniedError') {
      return MESSAGES.INTERVIEW_MIC_PERMISSION_DENIED;
    }
    if (cause.name === 'NotFoundError' || cause.name === 'DevicesNotFoundError') {
      return MESSAGES.INTERVIEW_MIC_NOT_FOUND;
    }
    if (cause.name === 'NotReadableError' || cause.name === 'TrackStartError') {
      return MESSAGES.INTERVIEW_MIC_IN_USE;
    }
  }
  return MESSAGES.INTERVIEW_MIC_UNAVAILABLE;
}

export interface MediaRecorderControls {
  /** Whether the browser supports MediaRecorder recording. */
  supported: boolean;
  /** Whether a capture session is active (may be paused). */
  recording: boolean;
  /** Whether capture is paused. */
  paused: boolean;
  /** Whether the microphone stream is acquired (may be paused). */
  micActive: boolean;
  /** The seconds of active (unpaused) recording. */
  elapsedSeconds: number;
  /** The number of long pauses (2.5s+ of silence) detected. */
  longPauses: number;
  /** A permission/recording error message, if any. */
  error: string | null;
  /** Starts (or restarts) recording. */
  start: () => void;
  /** Pauses recording, keeping the session alive. */
  pause: () => void;
  /** Resumes a paused recording. */
  resume: () => void;
  /** Stops recording, combines the chunks, and resolves with the audio. */
  stop: () => Promise<RecordingResult | null>;
  /** Stops recording and clears the recording state and metrics. */
  reset: () => void;
}

export function useMediaRecorder(): MediaRecorderControls {
  const supported = mediaRecorderSupported();

  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const chunksRef = useRef<Blob[]>([]);
  const audioContextRef = useRef<AudioContext | null>(null);
  const analyserRef = useRef<AnalyserNode | null>(null);
  const silenceSinceRef = useRef<number | null>(null);
  const elapsedRef = useRef(0);
  const longPausesRef = useRef(0);
  const activeRef = useRef(false);
  const pausedRef = useRef(false);
  const stopPromiseRef = useRef<Promise<RecordingResult | null> | null>(null);
  const tickIntervalRef = useRef<number | null>(null);
  const silenceIntervalRef = useRef<number | null>(null);

  const [recording, setRecording] = useState(false);
  const [paused, setPaused] = useState(false);
  const [micActive, setMicActive] = useState(false);
  const [elapsedSeconds, setElapsedSeconds] = useState(0);
  const [longPauses, setLongPauses] = useState(0);
  const [error, setError] = useState<string | null>(null);

  /** Releases the recorder, stream, silence detector, and timers. */
  const teardown = useCallback(() => {
    activeRef.current = false;
    pausedRef.current = false;
    if (tickIntervalRef.current !== null) {
      window.clearInterval(tickIntervalRef.current);
      tickIntervalRef.current = null;
    }
    if (silenceIntervalRef.current !== null) {
      window.clearInterval(silenceIntervalRef.current);
      silenceIntervalRef.current = null;
    }
    mediaRecorderRef.current = null;
    const stream = streamRef.current;
    if (stream) {
      stream.getTracks().forEach((track) => track.stop());
    }
    streamRef.current = null;
    const audioContext = audioContextRef.current;
    if (audioContext && audioContext.state !== 'closed') {
      void audioContext.close();
    }
    audioContextRef.current = null;
    analyserRef.current = null;
    silenceSinceRef.current = null;
    setRecording(false);
    setPaused(false);
    setMicActive(false);
  }, []);

  /** Samples the live audio level and counts long pauses. */
  const sampleSilence = useCallback(() => {
    // Paused time and teardown time never count towards long pauses.
    if (!activeRef.current || pausedRef.current) {
      silenceSinceRef.current = null;
      return;
    }
    const analyser = analyserRef.current;
    if (!analyser) {
      return;
    }
    const buffer = new Uint8Array(analyser.fftSize);
    analyser.getByteTimeDomainData(buffer);
    if (computeRms(buffer) < SILENCE_RMS_THRESHOLD) {
      const now = Date.now();
      if (silenceSinceRef.current === null) {
        silenceSinceRef.current = now;
      } else if (now - silenceSinceRef.current >= LONG_PAUSE_THRESHOLD_MS) {
        longPausesRef.current += 1;
        setLongPauses(longPausesRef.current);
        silenceSinceRef.current = now;
      }
    } else {
      silenceSinceRef.current = null;
    }
  }, []);

  /** Starts the per-second elapsed-time ticker. */
  const startTicker = useCallback(() => {
    if (tickIntervalRef.current !== null) {
      window.clearInterval(tickIntervalRef.current);
    }
    tickIntervalRef.current = window.setInterval(() => {
      if (activeRef.current && !pausedRef.current) {
        elapsedRef.current += 1;
        setElapsedSeconds(elapsedRef.current);
      }
    }, 1000);
  }, []);

  /** Wires the silence detector to the live microphone stream. */
  const setupSilenceDetection = useCallback((stream: MediaStream) => {
    const AudioContextCtor = getAudioContextConstructor();
    if (!AudioContextCtor) {
      return;
    }
    const audioContext = new AudioContextCtor();
    audioContextRef.current = audioContext;
    const source = audioContext.createMediaStreamSource(stream);
    const analyser = audioContext.createAnalyser();
    analyser.fftSize = 2048;
    analyser.smoothingTimeConstant = 0.2;
    source.connect(analyser);
    analyserRef.current = analyser;
    if (audioContext.state === 'suspended') {
      void audioContext.resume();
    }
    silenceIntervalRef.current = window.setInterval(
      sampleSilence,
      SILENCE_SAMPLE_INTERVAL_MS,
    );
  }, [sampleSilence]);

  /** Starts recording from the microphone. */
  const start = useCallback(async () => {
    if (!supported) {
      setError(MESSAGES.INTERVIEW_RECORDING_UNSUPPORTED);
      return;
    }
    if (activeRef.current) {
      return;
    }
    setError(null);
    chunksRef.current = [];
    elapsedRef.current = 0;
    longPausesRef.current = 0;
    silenceSinceRef.current = null;
    stopPromiseRef.current = null;
    setElapsedSeconds(0);
    setLongPauses(0);

    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        audio: { echoCancellation: true, noiseSuppression: true },
      });
      // Guard against a second Start click while permission was pending:
      // only the first call may capture, the rest release their stream.
      if (activeRef.current) {
        stream.getTracks().forEach((track) => track.stop());
        return;
      }
      streamRef.current = stream;

      const mimeType = pickRecordingMimeType();
      const recorder = mimeType
        ? new MediaRecorder(stream, { mimeType })
        : new MediaRecorder(stream);
      mediaRecorderRef.current = recorder;

      recorder.ondataavailable = (event: BlobEvent) => {
        if (event.data && event.data.size > 0) {
          chunksRef.current.push(event.data);
        }
      };

      activeRef.current = true;
      pausedRef.current = false;
      setMicActive(true);
      setRecording(true);
      setPaused(false);

      recorder.start(CHUNK_TIMESLICE_MS);
      setupSilenceDetection(stream);
      startTicker();
    } catch (cause) {
      setRecording(false);
      setMicActive(false);
      setError(mapMicrophoneError(cause));
    }
  }, [supported, setupSilenceDetection, startTicker]);

  /** Pauses recording, keeping the session and captured chunks alive. */
  const pause = useCallback(() => {
    const recorder = mediaRecorderRef.current;
    if (!activeRef.current || !recorder || recorder.state !== 'recording') {
      return;
    }
    pausedRef.current = true;
    setPaused(true);
    recorder.pause();
  }, []);

  /** Resumes a paused recording. */
  const resume = useCallback(() => {
    const recorder = mediaRecorderRef.current;
    if (!activeRef.current || !recorder || recorder.state !== 'paused') {
      return;
    }
    pausedRef.current = false;
    silenceSinceRef.current = null;
    setPaused(false);
    recorder.resume();
  }, []);

  /**
   * Stops recording, combines the chunks into a single audio Blob, and
   * resolves with it. Resolves {@code null} when nothing was captured.
   * Re-entrant: a second call while a stop is in flight returns the same
   * promise, so a fast double-click cannot orphan the recorder.
   */
  const stop = useCallback((): Promise<RecordingResult | null> => {
    if (stopPromiseRef.current !== null) {
      return stopPromiseRef.current;
    }

    const recorder = mediaRecorderRef.current;
    if (!recorder || recorder.state === 'inactive') {
      return Promise.resolve(null);
    }

    const promise = new Promise<RecordingResult | null>((resolve) => {
      let settled = false;
      let fallbackTimer: number | null = null;

      const finish = () => {
        if (settled) {
          return;
        }
        settled = true;
        if (fallbackTimer !== null) {
          window.clearTimeout(fallbackTimer);
        }
        stopPromiseRef.current = null;
        const chunks = chunksRef.current;
        const duration = elapsedRef.current;
        teardown();
        if (chunks.length === 0) {
          setError(MESSAGES.INTERVIEW_RECORDING_EMPTY);
          resolve(null);
          return;
        }
        const mimeType = recorder.mimeType || 'audio/webm';
        const blob = new Blob(chunks, { type: mimeType });
        chunksRef.current = [];
        resolve({ blob, durationSeconds: duration });
      };

      recorder.onstop = finish;
      fallbackTimer = window.setTimeout(finish, STOP_TIMEOUT_MS);
      activeRef.current = false;
      pausedRef.current = false;
      try {
        recorder.stop();
      } catch {
        finish();
      }
    });

    stopPromiseRef.current = promise;
    return promise;
  }, [teardown]);

  /** Stops recording and clears the recording state and metrics. */
  const reset = useCallback(() => {
    const recorder = mediaRecorderRef.current;
    if (recorder && recorder.state !== 'inactive') {
      recorder.onstop = null;
      try {
        recorder.stop();
      } catch {
        // Already stopped — nothing to do.
      }
    }
    // Resolve any in-flight stop (e.g. a timed auto-advance mid-stop) so
    // nothing hangs; the caller discards stale results on question change.
    if (stopPromiseRef.current !== null) {
      stopPromiseRef.current = null;
    }
    teardown();
    chunksRef.current = [];
    elapsedRef.current = 0;
    longPausesRef.current = 0;
    setElapsedSeconds(0);
    setLongPauses(0);
    setError(null);
  }, [teardown]);

  /** Releases the microphone on unmount. */
  useEffect(() => {
    return () => {
      const recorder = mediaRecorderRef.current;
      if (recorder && recorder.state !== 'inactive') {
        recorder.onstop = null;
        try {
          recorder.stop();
        } catch {
          // Already stopped — nothing to do.
        }
      }
      teardown();
    };
  }, [teardown]);

  return {
    supported,
    recording,
    paused,
    micActive,
    elapsedSeconds,
    longPauses,
    error,
    start: () => void start(),
    pause,
    resume,
    stop,
    reset,
  };
}
