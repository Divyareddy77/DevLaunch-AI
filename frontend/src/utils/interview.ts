/**
 * Normalization helpers for the AI Mock Interview module.
 *
 * Bridges the two report shapes the client can render — the fresh
 * submission response and a snapshotted history item — into a single
 * {@link InterviewReportData} so the report component has one contract.
 *
 * @author DevLaunch
 */

import type {
  AnswerFeedback,
  InterviewHistoryItem,
  InterviewQuestion,
  InterviewReportData,
} from '../types/ai';

/**
 * Maps snapshotted history questions into the report's feedback shape.
 *
 * @param questions the snapshotted session questions
 * @return the normalized per-question feedback
 */
export function questionsToFeedback(
  questions?: InterviewQuestion[] | null,
): AnswerFeedback[] {
  if (!questions) {
    return [];
  }
  return questions.map((question) => ({
    questionId: question.id,
    question: question.question,
    answer: question.answer ?? '',
    score: question.score ?? 0,
    feedback: question.feedback ?? '',
    suggestions: [],
    improvedAnswer: question.improvedAnswer ?? null,
  }));
}

/**
 * Normalizes a snapshotted history item into the report contract used by
 * the report component.
 *
 * @param item the history item
 * @return the normalized report data
 */
export function historyItemToReport(item: InterviewHistoryItem): InterviewReportData {
  const hasSnapshots = (item.questions?.length ?? 0) > 0;
  return {
    sessionId: item.sessionId,
    interviewType: item.interviewType,
    difficulty: item.difficulty ?? null,
    timed: item.timed ?? null,
    durationSeconds: item.durationSeconds ?? null,
    wordCount: item.wordCount ?? null,
    overallScore: item.overallScore,
    technicalScore: item.technicalScore ?? null,
    communicationScore: item.communicationScore ?? null,
    confidenceScore: item.confidenceScore ?? null,
    problemSolvingScore: item.problemSolvingScore ?? null,
    clarityScore: item.clarityScore ?? null,
    vocabularyScore: item.vocabularyScore ?? null,
    professionalismScore: item.professionalismScore ?? null,
    feedback: hasSnapshots ? questionsToFeedback(item.questions) : (item.feedback ?? []),
    strengths: item.strengths ?? [],
    areasForImprovement: item.areasForImprovement ?? [],
    suggestions: item.suggestions ?? [],
    missedConcepts: item.missedConcepts ?? [],
  };
}

/**
 * Counts the words in a piece of text.
 *
 * @param text the text to count
 * @return the number of words
 */
export function wordCount(text: string): number {
  return text
    .trim()
    .split(/\s+/)
    .filter((word) => word.length > 0).length;
}
