/**
 * ScheduleInterviewModal — schedules (or edits) an interview on an
 * application.
 *
 * Captures the interview title, round, date, time, meeting link,
 * interviewer, and preparation notes using React Hook Form + Zod, matching
 * the backend ScheduleInterviewRequest / UpdateInterviewScheduleRequest
 * constraints.
 *
 * @author DevLaunch
 */

import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { CalendarClock } from 'lucide-react';
import { Modal } from '../ui/Modal';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import type { InterviewSchedule } from '../../types/job-application';

const scheduleSchema = z.object({
  title: z.string().min(1, 'Interview title is required'),
  round: z.string().optional(),
  scheduledDate: z.string().min(1, 'Interview date is required'),
  scheduledTime: z.string().optional(),
  meetingLink: z.string().url('Please enter a valid URL').or(z.literal('')).optional(),
  interviewer: z.string().optional(),
  notes: z.string().optional(),
});

type ScheduleFormValues = z.infer<typeof scheduleSchema>;

interface ScheduleInterviewModalProps {
  /** Whether the modal is visible. */
  isOpen: boolean;
  /** Closes the modal. */
  onClose: () => void;
  /** The application being scheduled on (used for the title). */
  companyName: string;
  /** The interview being edited, or null when creating a new one. */
  interview?: InterviewSchedule | null;
  /** Whether the form is submitting. */
  isSubmitting: boolean;
  /** Submits the form values. */
  onSubmit: (data: ScheduleFormValues) => void;
}

export const ScheduleInterviewModal: React.FC<ScheduleInterviewModalProps> = ({
  isOpen,
  onClose,
  companyName,
  interview,
  isSubmitting,
  onSubmit,
}) => {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ScheduleFormValues>({
    resolver: zodResolver(scheduleSchema),
    values: interview
      ? {
          title: interview.title,
          round: interview.round ?? '',
          scheduledDate: interview.scheduledDate,
          scheduledTime: interview.scheduledTime ? interview.scheduledTime.slice(0, 5) : '',
          meetingLink: interview.meetingLink ?? '',
          interviewer: interview.interviewer ?? '',
          notes: interview.notes ?? '',
        }
      : undefined,
  });

  // Reset to the empty form whenever the modal opens fresh.
  React.useEffect(() => {
    if (isOpen && !interview) {
      reset({
        title: '',
        round: '',
        scheduledDate: '',
        scheduledTime: '',
        meetingLink: '',
        interviewer: '',
        notes: '',
      });
    }
  }, [isOpen, interview, reset]);

  const handleFormSubmit = (data: ScheduleFormValues) => {
    onSubmit(data);
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={interview ? 'Edit Interview' : 'Schedule Interview'}
      className="max-w-lg"
    >
      <p className="mb-4 text-xs text-gray-500">
        {companyName}
        {interview
          ? ` — update this scheduled round.`
          : ' — add a phone, technical, or HR round.'}
      </p>

      <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
        <div className="grid gap-4 sm:grid-cols-2">
          <Input
            label="Interview Title"
            placeholder="e.g. Technical Interview"
            error={errors.title?.message}
            {...register('title')}
          />
          <Input
            label="Round"
            placeholder="e.g. Round 1 / HR"
            error={errors.round?.message}
            {...register('round')}
          />
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <Input
            label="Date"
            type="date"
            error={errors.scheduledDate?.message}
            {...register('scheduledDate')}
          />
          <Input
            label="Time"
            type="time"
            error={errors.scheduledTime?.message}
            {...register('scheduledTime')}
          />
        </div>

        <Input
          label="Meeting Link"
          placeholder="https://meet.google.com/…"
          error={errors.meetingLink?.message}
          {...register('meetingLink')}
        />

        <div className="grid gap-4 sm:grid-cols-2">
          <Input
            label="Interviewer"
            placeholder="e.g. Rahul Sharma"
            error={errors.interviewer?.message}
            {...register('interviewer')}
          />
        </div>

        <div>
          <label className="mb-1.5 block text-sm font-medium text-gray-700">Notes</label>
          <textarea
            rows={3}
            placeholder="Revise Java Streams, review your projects…"
            className="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 placeholder-gray-400 transition-colors focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
            {...register('notes')}
          />
        </div>

        <div className="flex justify-end gap-3 pt-2">
          <Button type="button" variant="ghost" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" loading={isSubmitting}>
            <CalendarClock className="h-4 w-4" />
            {interview ? 'Save Changes' : 'Schedule Interview'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};
