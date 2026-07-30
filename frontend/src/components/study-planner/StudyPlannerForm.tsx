/**
 * StudyPlannerForm — form for creating and editing study tasks.
 *
 * Uses React Hook Form + Zod validation matching the backend
 * CreateStudyPlannerRequest / UpdateStudyPlannerRequest constraints.
 *
 * @author DevLaunch
 */

import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Save, ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import { Card } from '../ui/Card';
import { ROUTES } from '../../constants/routes';
import {
  STUDY_PRIORITIES,
  STUDY_PRIORITY_LABELS,
  STUDY_STATUSES,
  STUDY_STATUS_LABELS,
  type StudyPlannerResponse,
  type CreateStudyPlannerRequest,
} from '../../types/study-planner';

const studyPlannerSchema = z.object({
  title: z.string().min(1, 'Title is required'),
  description: z.string().optional(),
  studyDate: z.string().min(1, 'Study date is required'),
  startTime: z.string().optional(),
  endTime: z.string().optional(),
  priority: z.string().min(1, 'Priority is required'),
  status: z.string().min(1, 'Status is required'),
});

export type StudyPlannerFormValues = z.infer<typeof studyPlannerSchema>;

interface StudyPlannerFormProps {
  /** Initial data for edit mode. */
  initialData?: StudyPlannerResponse | null;
  /** Whether the form is submitting. */
  isSubmitting: boolean;
  /** Callback with form values on save. */
  onSubmit: (data: CreateStudyPlannerRequest) => void;
}

export const StudyPlannerForm: React.FC<StudyPlannerFormProps> = ({
  initialData,
  isSubmitting,
  onSubmit,
}) => {
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<StudyPlannerFormValues>({
    resolver: zodResolver(studyPlannerSchema),
    values: initialData
      ? {
          title: initialData.title,
          description: initialData.description ?? '',
          studyDate: initialData.studyDate,
          startTime: initialData.startTime?.slice(0, 5) ?? '',
          endTime: initialData.endTime?.slice(0, 5) ?? '',
          priority: initialData.priority,
          status: initialData.status,
        }
      : {
          title: '',
          description: '',
          studyDate: '',
          startTime: '',
          endTime: '',
          priority: 'MEDIUM',
          status: 'PENDING',
        },
  });

  const handleFormSubmit = (data: StudyPlannerFormValues) => {
    onSubmit(data as CreateStudyPlannerRequest);
  };

  return (
    <div className="mx-auto max-w-3xl">
      {/* Back button */}
      <button
        onClick={() => navigate(ROUTES.STUDY_PLANNER_LIST)}
        className="mb-6 inline-flex items-center gap-1.5 text-sm font-medium text-gray-500 hover:text-gray-700 transition-colors"
      >
        <ArrowLeft className="h-4 w-4" />
        Back to Study Planner
      </button>

      {/* Page header */}
      <div className="mb-6">
        <h1 className="text-xl font-bold text-gray-900">
          {initialData ? 'Edit Study Task' : 'New Study Task'}
        </h1>
        <p className="mt-1 text-sm text-gray-500">
          {initialData
            ? 'Update the details of your study session.'
            : 'Schedule a new study session or task.'}
        </p>
      </div>

      <Card>
        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-5">
          {/* Title */}
          <Input
            label="Title"
            placeholder="e.g. Review React Hooks"
            error={errors.title?.message}
            {...register('title')}
          />

          {/* Description */}
          <div>
            <label className="mb-1.5 block text-sm font-medium text-gray-700">
              Description
            </label>
            <textarea
              rows={3}
              placeholder="Topics, resources, or objectives for this study session…"
              className="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 placeholder-gray-400 transition-colors focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
              {...register('description')}
            />
          </div>

          {/* Date */}
          <Input
            label="Study Date"
            type="date"
            error={errors.studyDate?.message}
            {...register('studyDate')}
          />

          {/* Start & End time */}
          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label="Start Time"
              type="time"
              {...register('startTime')}
            />
            <Input
              label="End Time"
              type="time"
              {...register('endTime')}
            />
          </div>

          {/* Priority & Status */}
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label className="mb-1.5 block text-sm font-medium text-gray-700">
                Priority <span className="text-red-500">*</span>
              </label>
              <select
                className="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 transition-colors focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
                {...register('priority')}
              >
                {STUDY_PRIORITIES.map((p) => (
                  <option key={p} value={p}>
                    {STUDY_PRIORITY_LABELS[p]}
                  </option>
                ))}
              </select>
              {errors.priority?.message && (
                <p className="mt-1.5 text-xs text-red-500">{errors.priority.message}</p>
              )}
            </div>

            <div>
              <label className="mb-1.5 block text-sm font-medium text-gray-700">
                Status <span className="text-red-500">*</span>
              </label>
              <select
                className="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 transition-colors focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
                {...register('status')}
              >
                {STUDY_STATUSES.map((s) => (
                  <option key={s} value={s}>
                    {STUDY_STATUS_LABELS[s]}
                  </option>
                ))}
              </select>
              {errors.status?.message && (
                <p className="mt-1.5 text-xs text-red-500">{errors.status.message}</p>
              )}
            </div>
          </div>

          {/* Submit */}
          <div className="flex justify-end gap-3 pt-2">
            <Button
              type="button"
              variant="ghost"
              onClick={() => navigate(ROUTES.STUDY_PLANNER_LIST)}
            >
              Cancel
            </Button>
            <Button type="submit" loading={isSubmitting}>
              <Save className="h-4 w-4" />
              {initialData ? 'Update Task' : 'Create Task'}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};
