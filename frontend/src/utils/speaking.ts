/**
 * Live speaking analysis for voice-answered mock interviews.
 *
 * Computes speaking pace (words per minute), filler-word usage, a
 * confidence estimate, and the pause count from the live transcript so
 * the interview screen can show real-time coaching metrics.
 *
 * @author DevLaunch
 */

/** Words that signal hesitation or weak delivery. */
const FILLER_WORD_PATTERN = /\b(um+|uh+|uhm+|like|actually|basically|you know|sort of|kind of|hmm)\b/gi;

/** The comfortable words-per-minute band for an interview answer. */
const IDEAL_WPM_MIN = 110;
const IDEAL_WPM_MAX = 165;

/** A wider band that is still acceptable. */
const ACCEPTABLE_WPM_MIN = 85;
const ACCEPTABLE_WPM_MAX = 195;

/** The live speaking metrics for a single answer. */
export interface SpeakingMetrics {
  /** The number of spoken words so far. */
  wordCount: number;
  /** The seconds of active speaking time. */
  speakingSeconds: number;
  /** Words per minute. */
  wpm: number;
  /** The number of sentences detected in the transcript. */
  sentenceCount: number;
  /** The filler words detected, in order of appearance. */
  fillerWords: string[];
  /** The total number of filler words detected. */
  fillerWordCount: number;
  /** The number of long pauses (2.5s+) detected while recording. */
  longPauses: number;
  /** A 0–100 confidence estimate derived from pace, fillers, and pauses. */
  confidence: number;
  /** A human-readable pace label. */
  paceLabel: string;
}

/**
 * Analyzes the current transcript and produces live speaking metrics.
 *
 * @param text            the current (editable) transcript
 * @param speakingSeconds the seconds of active recording
 * @param longPauses      the long pauses detected by the recorder
 * @return the derived speaking metrics
 */
export function analyzeSpeaking(
  text: string,
  speakingSeconds: number,
  longPauses: number,
): SpeakingMetrics {
  const words = text
    .trim()
    .split(/\s+/)
    .filter((word) => word.length > 0);
  const wordCount = words.length;
  const wpm = speakingSeconds > 0 ? Math.round((wordCount / speakingSeconds) * 60) : 0;

  // A transcript with no sentence punctuation counts as a single sentence,
  // matching the backend's sentence scoring.
  let sentenceCount = 0;
  if (wordCount > 0) {
    const sentenceMatches = text.match(/[.!?]+/g);
    sentenceCount = sentenceMatches ? sentenceMatches.length : 1;
  }

  const fillerWords = Array.from(text.matchAll(FILLER_WORD_PATTERN)).map((match) =>
    match[0].toLowerCase(),
  );
  const fillerWordCount = fillerWords.length;

  let confidence = 72;
  if (wpm >= IDEAL_WPM_MIN && wpm <= IDEAL_WPM_MAX) {
    confidence += 16;
  } else if (wpm >= ACCEPTABLE_WPM_MIN && wpm <= ACCEPTABLE_WPM_MAX) {
    confidence += 5;
  } else {
    confidence -= 12;
  }
  confidence -= Math.min(fillerWordCount * 4, 24);
  confidence -= Math.min(longPauses * 6, 20);
  confidence = Math.max(0, Math.min(100, confidence));

  const paceLabel =
    wpm === 0
      ? '—'
      : wpm < ACCEPTABLE_WPM_MIN
        ? 'Slow'
        : wpm <= IDEAL_WPM_MAX
          ? 'Good pace'
          : 'Fast';

  return {
    wordCount,
    speakingSeconds,
    wpm,
    sentenceCount,
    fillerWords,
    fillerWordCount,
    longPauses,
    confidence,
    paceLabel,
  };
}
