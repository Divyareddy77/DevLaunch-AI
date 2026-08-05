/**
 * Type definitions for AI-powered features.
 *
 * These types support the AI module endpoints which provide resume review,
 * mock interview generation, and feedback analysis. The backend AI service
 * processes user-submitted content and returns structured analysis results.
 *
 * @author DevLaunch
 */

/** Categories available for mock interview sessions. */
export enum InterviewCategory {
  HR = 'HR',
  JAVA = 'JAVA',
  SPRING_BOOT = 'SPRING_BOOT',
  SQL = 'SQL',
  REACT = 'REACT',
}

/** Request payload to start a new mock interview session. */
export interface StartInterviewRequest {
  /** The category of interview to simulate. */
  interviewType: InterviewCategory;
}

/** A single question generated for a mock interview. */
export interface InterviewQuestion {
  /** Unique identifier for the question within the session. */
  id: string;
  /** The question text presented to the user. */
  question: string;
  /** Optional hints or context to help the user answer. */
  hint?: string;
}

/** Response returned when a mock interview session begins. */
export interface StartInterviewResponse {
  /** Unique session identifier for the interview. */
  sessionId: string;
  /** The category of this interview session. */
  interviewType: InterviewCategory;
  /** Array of questions generated for this session. */
  questions: InterviewQuestion[];
}

/** A single question/answer pair submitted for evaluation. */
export interface InterviewAnswer {
  /** The ID of the question within the session. */
  questionId: string;
  /** The question text the user was asked. */
  question: string;
  /** The user's answer to the question. */
  answer: string;
}

/** Request payload to submit answers for an interview session. */
export interface SubmitInterviewRequest {
  /** The session ID returned from StartInterview. */
  sessionId: string;
  /** The category of interview that was practised. */
  interviewType: InterviewCategory;
  /** The question/answer pairs to evaluate. */
  answers: InterviewAnswer[];
}

/** Feedback for a single interview answer. */
export interface AnswerFeedback {
  /** The ID of the question this feedback relates to. */
  questionId: string;
  /** The original question text. */
  question: string;
  /** The user's submitted answer. */
  answer: string;
  /** A score from 0–100 for this answer. */
  score: number;
  /** Detailed feedback text. */
  feedback: string;
  /** Specific suggestions for improvement. */
  suggestions: string[];
}

/** Response returned after answers are submitted and analysed. */
export interface SubmitInterviewResponse {
  /** The session ID this feedback belongs to. */
  sessionId: string;
  /** The category of the interview that was analysed. */
  interviewType: InterviewCategory;
  /** Overall score for the entire interview (0–100). */
  overallScore: number;
  /** Per-question feedback breakdown. */
  feedback: AnswerFeedback[];
  /** Summary of strengths identified across all answers. */
  strengths: string[];
  /** Areas for improvement identified across all answers. */
  areasForImprovement: string[];
}

/** A historical interview session record. */
export interface InterviewHistoryItem {
  /** Session identifier. */
  sessionId: string;
  /** The category of interview. */
  interviewType: InterviewCategory;
  /** Date the interview was completed. */
  completedAt: string;
  /** Overall score achieved. */
  overallScore: number;
  /** Number of questions in the session. */
  questionCount: number;
  /** The exact questions presented in the session, as originally answered. */
  questions?: InterviewQuestion[];
}

/** Response for the interview history endpoint. */
export interface InterviewHistoryResponse {
  /** Array of past interview sessions. */
  history: InterviewHistoryItem[];
  /** Total number of interviews taken. */
  totalInterviews: number;
  /** Average score across all completed interviews. */
  averageScore: number;
}

/** Request payload to submit an existing resume for AI-powered review. */
export interface ResumeReviewRequest {
  /** The ID of the resume to review. */
  resumeId: number;
  /** Optional target job role to tailor the review towards. */
  targetRole?: string;
}

/** A single suggestion returned from a resume review. */
export interface ResumeReviewSuggestion {
  /** The specific section of the resume this relates to. */
  section: string;
  /** The suggestion text. */
  suggestion: string;
  /** Priority level: 'high', 'medium', or 'low'. */
  priority: 'high' | 'medium' | 'low';
}

/** Response returned after an AI resume review. */
export interface ResumeReviewResponse {
  /** The ID of the resume that was reviewed. */
  resumeId: number;
  /** The headline/title of the reviewed resume. */
  resumeTitle: string;
  /** Overall resume quality score (0–100). */
  resumeScore: number;
  /** Applicant Tracking System compatibility score (0–100). */
  atsScore: number;
  /** Overall strengths of the resume. */
  strengths: string[];
  /** Areas where the resume is weak or underdeveloped. */
  weaknesses: string[];
  /** In-demand skills missing from the resume. */
  missingSkills: string[];
  /** Detailed suggestions for improvement. */
  suggestions: ResumeReviewSuggestion[];
}
