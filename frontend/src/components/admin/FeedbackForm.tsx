/**
 * FeedbackForm — user-facing platform feedback submission.
 *
 * Uses React Hook Form + Zod with a message constraint mirroring the
 * backend FeedbackSubmissionRequest DTO (required, max 2000 characters).
 * Submitted feedback is reviewed by administrators in the admin module.
 *
 * @see backend/src/main/java/com/devlaunch/dto/request/FeedbackSubmissionRequest.java
 * @author DevLaunch
 */

import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Send, MessageSquare } from 'lucide-react';
import { Button } from '../ui/Button';
import { Card } from '../ui/Card';

const feedbackSchema = z.object({
  message: z
    .string()
    .min(1, 'Please write a short message')
    .max(2000, 'Feedback must not exceed 2000 characters'),
});

type FeedbackFormValues = z.infer<typeof feedbackSchema>;

interface FeedbackFormProps {
  /** Whether the form is currently submitting. */
  isSubmitting: boolean;
  /**
   * Callback with the feedback message. Should resolve with `true` on
   * success (resets the form) or `false` on failure.
   */
  onSubmit: (message: string) => Promise<boolean>;
}

export const FeedbackForm: React.FC<FeedbackFormProps> = ({ isSubmitting, onSubmit }) => {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FeedbackFormValues>({
    resolver: zodResolver(feedbackSchema),
    defaultValues: { message: '' },
  });

  const handleFormSubmit = async (values: FeedbackFormValues) => {
    const success = await onSubmit(values.message);
    if (success) {
      reset();
    }
  };

  return (
    <Card
      header={
        <div>
          <h3 className="text-base font-semibold text-gray-900">Send Feedback</h3>
          <p className="mt-0.5 text-xs text-gray-500">
            Share your suggestions or report an issue — the DevLaunch team reads every message.
          </p>
        </div>
      }
    >
      <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4" noValidate>
        <div>
          <label
            htmlFor="feedback-message"
            className="mb-1.5 block text-sm font-medium text-gray-700"
          >
            Message
          </label>
          <div className="relative">
            <div className="pointer-events-none absolute inset-y-0 left-0 flex items-start pl-3 pt-2.5 text-gray-400">
              <MessageSquare className="h-4 w-4" />
            </div>
            <textarea
              id="feedback-message"
              rows={4}
              placeholder="Tell us what you think…"
              className={`
                block w-full rounded-lg border bg-white py-2 pl-10 pr-3 text-sm text-gray-900
                placeholder-gray-400 transition-colors focus:outline-none focus:ring-2
                ${
                  errors.message
                    ? 'border-red-300 focus:border-red-500 focus:ring-red-500'
                    : 'border-gray-300 focus:border-primary-500 focus:ring-primary-500'
                }
              `}
              aria-invalid={errors.message ? 'true' : 'false'}
              {...register('message')}
            />
          </div>
          {errors.message && (
            <p className="mt-1.5 text-xs text-red-500" role="alert">
              {errors.message.message}
            </p>
          )}
        </div>

        <div className="flex justify-end">
          <Button type="submit" loading={isSubmitting}>
            <Send className="h-4 w-4" />
            Send Feedback
          </Button>
        </div>
      </form>
    </Card>
  );
};
