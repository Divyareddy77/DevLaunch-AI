/**
 * InterviewQuestionCard — the question-and-answer step of the AI Mock
 * Interview flow.
 *
 * Displays one question at a time with an optional hint, a textarea for
 * the user's answer (validated with React Hook Form + Zod), and
 * Back / Next / Finish navigation. The form remounts per question via a
 * key so each question starts with a clean, pre-filled answer.
 *
 * @author DevLaunch
 */

import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { HelpCircle, ArrowLeft, ArrowRight, Check } from 'lucide-react';
import { Card } from '../ui/Card';
import { Button } from '../ui/Button';
import type { InterviewQuestion } from '../../types/ai';

/** Zod schema for a single interview answer. */
const answerSchema = z.object({
  answer: z.string().min(1, 'Please write an answer before continuing.'),
});

type AnswerFormValues = z.infer<typeof answerSchema>;

interface InterviewQuestionCardProps {
  /** The question currently being answered. */
  question: InterviewQuestion;
  /** Zero-based index of the current question. */
  index: number;
  /** Total number of questions in the session. */
  total: number;
  /** The answer already stored for this question, if any. */
  defaultAnswer: string;
  /** Whether the interview is currently being submitted. */
  submitting: boolean;
  /** Called with the answer when the user advances or finishes. */
  onSubmit: (answer: string) => void;
  /** Called when the user navigates to the previous question. */
  onBack: () => void;
}

export const InterviewQuestionCard: React.FC<InterviewQuestionCardProps> = ({
  question,
  index,
  total,
  defaultAnswer,
  submitting,
  onSubmit,
  onBack,
}) => {
  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors, isValid },
  } = useForm<AnswerFormValues>({
    resolver: zodResolver(answerSchema),
    defaultValues: { answer: defaultAnswer },
    mode: 'onChange',
  });

  // Pre-fill / reset the answer whenever the question changes.
  useEffect(() => {
    reset({ answer: defaultAnswer });
  }, [question.id, defaultAnswer, reset]);

  const answer = watch('answer');
  const canProceed = isValid && answer.trim().length > 0;
  const isLast = index === total - 1;
  const progress = ((index + 1) / total) * 100;

  const handleNext = (values: AnswerFormValues) => {
    onSubmit(values.answer);
  };

  return (
    <Card>
      <div className="space-y-5">
        {/* Progress header */}
        <div>
          <div className="flex items-center justify-between gap-4">
            <p className="text-sm font-medium text-gray-500">
              Question {index + 1} of {total}
            </p>
            <p className="text-sm font-medium text-primary-600">
              {isLast ? 'Final question' : `${total - index - 1} remaining`}
            </p>
          </div>
          <div className="mt-2 h-2 w-full overflow-hidden rounded-full bg-gray-100">
            <div
              className="h-2 rounded-full bg-primary-600 transition-all duration-500"
              style={{ width: `${progress}%` }}
            />
          </div>
        </div>

        {/* Question */}
        <div>
          <h3 className="text-base font-semibold text-gray-900">{question.question}</h3>
          {question.hint && (
            <div className="mt-3 flex items-start gap-2 rounded-lg bg-blue-50 p-3">
              <HelpCircle className="mt-0.5 h-4 w-4 shrink-0 text-blue-500" />
              <p className="text-sm leading-relaxed text-blue-700">{question.hint}</p>
            </div>
          )}
        </div>

        {/* Answer */}
        <form onSubmit={handleSubmit(handleNext)} className="space-y-5">
          <div>
            <label
              htmlFor={`answer-${question.id}`}
              className="mb-1.5 block text-sm font-medium text-gray-700"
            >
              Your Answer
            </label>
            <textarea
              id={`answer-${question.id}`}
              rows={7}
              placeholder="Write your answer here — be specific and give concrete examples…"
              disabled={submitting}
              className={`
                block w-full rounded-lg border bg-white px-3 py-2 text-sm text-gray-900
                placeholder-gray-400 transition-colors focus:outline-none focus:ring-2 focus:ring-offset-0
                ${
                  errors.answer
                    ? 'border-red-300 focus:border-red-500 focus:ring-red-500'
                    : 'border-gray-300 focus:border-primary-500 focus:ring-primary-500'
                }
                disabled:cursor-not-allowed disabled:opacity-50
              `}
              aria-invalid={errors.answer ? 'true' : 'false'}
              {...register('answer')}
            />
            {errors.answer && (
              <p className="mt-1.5 text-xs text-red-500" role="alert">
                {errors.answer.message}
              </p>
            )}
          </div>

          <div className="flex items-center justify-between gap-3">
            <Button
              type="button"
              variant="outline"
              onClick={onBack}
              disabled={index === 0 || submitting}
            >
              <ArrowLeft className="h-4 w-4" />
              Back
            </Button>

            <Button
              type="submit"
              loading={submitting}
              disabled={!canProceed}
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
        </form>
      </div>
    </Card>
  );
};
