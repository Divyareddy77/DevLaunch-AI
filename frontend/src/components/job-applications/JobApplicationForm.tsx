/**
 * JobApplicationForm — form for creating and editing job applications.
 *
 * Uses React Hook Form + Zod validation matching the backend
 * CreateJobApplicationRequest / UpdateJobApplicationRequest constraints.
 * Covers the core fields plus the placement management extras: company
 * website, recruiter details, referral, work mode, priority, and
 * technology stack.
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
  APPLICATION_PRIORITIES,
  APPLICATION_PRIORITY_LABELS,
  APPLICATION_STATUSES,
  APPLICATION_STATUS_LABELS,
  WORK_MODES,
  WORK_MODE_LABELS,
  type JobApplicationResponse,
  type CreateJobApplicationRequest,
} from '../../types/job-application';

const jobApplicationSchema = z.object({
  companyName: z.string().min(1, 'Company name is required'),
  jobRole: z.string().min(1, 'Job role is required'),
  companyLocation: z.string().optional(),
  jobType: z.string().optional(),
  salary: z.string().optional(),
  applicationDate: z.string().optional(),
  status: z.string().min(1, 'Status is required'),
  jobUrl: z.string().url('Please enter a valid URL').or(z.literal('')).optional(),
  companyWebsite: z.string().url('Please enter a valid URL').or(z.literal('')).optional(),
  recruiterName: z.string().optional(),
  recruiterEmail: z
    .string()
    .email('Please enter a valid email address')
    .or(z.literal(''))
    .optional(),
  referral: z.string().optional(),
  workMode: z.string().optional(),
  priority: z.string().optional(),
  technology: z.string().optional(),
  notes: z.string().optional(),
  resumeId: z.number().optional(),
});

export type JobApplicationFormValues = z.infer<typeof jobApplicationSchema>;

const JOB_TYPE_OPTIONS = ['', 'Full-time', 'Part-time', 'Contract', 'Freelance', 'Internship'];

interface JobApplicationFormProps {
  /** Initial data for edit mode. */
  initialData?: JobApplicationResponse | null;
  /** Whether the form is submitting. */
  isSubmitting: boolean;
  /** Callback with form values on save. */
  onSubmit: (data: CreateJobApplicationRequest) => void;
}

const selectClass =
  'block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 transition-colors focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500';

export const JobApplicationForm: React.FC<JobApplicationFormProps> = ({
  initialData,
  isSubmitting,
  onSubmit,
}) => {
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<JobApplicationFormValues>({
    resolver: zodResolver(jobApplicationSchema),
    values: initialData
      ? {
          companyName: initialData.companyName,
          jobRole: initialData.jobRole,
          companyLocation: initialData.companyLocation ?? '',
          jobType: initialData.jobType ?? '',
          salary: initialData.salary ?? '',
          applicationDate: initialData.applicationDate ?? '',
          status: initialData.status,
          jobUrl: initialData.jobUrl ?? '',
          companyWebsite: initialData.companyWebsite ?? '',
          recruiterName: initialData.recruiterName ?? '',
          recruiterEmail: initialData.recruiterEmail ?? '',
          referral: initialData.referral ?? '',
          workMode: initialData.workMode ?? '',
          priority: initialData.priority,
          technology: initialData.technology ?? '',
          notes: initialData.notes ?? '',
          resumeId: initialData.resumeId ?? undefined,
        }
      : {
          companyName: '',
          jobRole: '',
          companyLocation: '',
          jobType: '',
          salary: '',
          applicationDate: '',
          status: 'WISHLIST',
          jobUrl: '',
          companyWebsite: '',
          recruiterName: '',
          recruiterEmail: '',
          referral: '',
          workMode: '',
          priority: 'MEDIUM',
          technology: '',
          notes: '',
        },
  });

  const handleFormSubmit = (data: JobApplicationFormValues) => {
    onSubmit(data as CreateJobApplicationRequest);
  };

  return (
    <div className="mx-auto max-w-3xl">
      {/* Back button */}
      <button
        onClick={() => navigate(ROUTES.JOB_APPLICATION_LIST)}
        className="mb-6 inline-flex items-center gap-1.5 text-sm font-medium text-gray-500 hover:text-gray-700 transition-colors"
      >
        <ArrowLeft className="h-4 w-4" />
        Back to Applications
      </button>

      {/* Page header */}
      <div className="mb-6">
        <h1 className="text-xl font-bold text-gray-900">
          {initialData ? 'Edit Application' : 'New Application'}
        </h1>
        <p className="mt-1 text-sm text-gray-500">
          {initialData
            ? 'Update the details of your job application.'
            : 'Track a new job application in your pipeline.'}
        </p>
      </div>

      <Card>
        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-5">
          {/* Company & Role */}
          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label="Company Name"
              placeholder="e.g. Google"
              error={errors.companyName?.message}
              {...register('companyName')}
            />
            <Input
              label="Job Role"
              placeholder="e.g. Software Engineer"
              error={errors.jobRole?.message}
              {...register('jobRole')}
            />
          </div>

          {/* Location & Job Type */}
          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label="Location"
              placeholder="e.g. San Francisco, CA (or Remote)"
              {...register('companyLocation')}
            />
            <div>
              <label className="mb-1.5 block text-sm font-medium text-gray-700">
                Employment Type
              </label>
              <select className={selectClass} {...register('jobType')}>
                {JOB_TYPE_OPTIONS.map((opt) => (
                  <option key={opt} value={opt}>
                    {opt || 'Select…'}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Work mode & Priority */}
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label className="mb-1.5 block text-sm font-medium text-gray-700">Work Mode</label>
              <select className={selectClass} {...register('workMode')}>
                <option value="">Select…</option>
                {WORK_MODES.map((mode) => (
                  <option key={mode} value={mode}>
                    {WORK_MODE_LABELS[mode]}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="mb-1.5 block text-sm font-medium text-gray-700">Priority</label>
              <select className={selectClass} {...register('priority')}>
                {APPLICATION_PRIORITIES.map((priority) => (
                  <option key={priority} value={priority}>
                    {APPLICATION_PRIORITY_LABELS[priority]}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Salary & Application Date */}
          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label="Expected Salary"
              placeholder="e.g. $80,000 - $100,000"
              {...register('salary')}
            />
            <Input
              label="Application Date"
              type="date"
              {...register('applicationDate')}
            />
          </div>

          {/* Status */}
          <div>
            <label className="mb-1.5 block text-sm font-medium text-gray-700">
              Status <span className="text-red-500">*</span>
            </label>
            <select className={selectClass} {...register('status')}>
              {APPLICATION_STATUSES.map((status) => (
                <option key={status} value={status}>
                  {APPLICATION_STATUS_LABELS[status]}
                </option>
              ))}
            </select>
            {errors.status?.message && (
              <p className="mt-1.5 text-xs text-red-500" role="alert">
                {errors.status.message}
              </p>
            )}
          </div>

          {/* Job URL & Company website */}
          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label="Job Posting URL"
              placeholder="https://careers.company.com/job/123"
              error={errors.jobUrl?.message}
              {...register('jobUrl')}
            />
            <Input
              label="Company Website"
              placeholder="https://www.company.com"
              error={errors.companyWebsite?.message}
              {...register('companyWebsite')}
            />
          </div>

          {/* Recruiter */}
          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label="Recruiter Name"
              placeholder="e.g. Rahul Sharma"
              error={errors.recruiterName?.message}
              {...register('recruiterName')}
            />
            <Input
              label="Recruiter Email"
              type="email"
              placeholder="rahul@company.com"
              error={errors.recruiterEmail?.message}
              {...register('recruiterEmail')}
            />
          </div>

          {/* Referral & Technology */}
          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label="Referral"
              placeholder="e.g. Priya (employee referral)"
              error={errors.referral?.message}
              {...register('referral')}
            />
            <Input
              label="Technology Stack"
              placeholder="e.g. Java, Spring Boot, React"
              error={errors.technology?.message}
              {...register('technology')}
            />
          </div>

          {/* Notes */}
          <div>
            <label className="mb-1.5 block text-sm font-medium text-gray-700">
              Notes
            </label>
            <textarea
              rows={3}
              placeholder="Preparation notes, follow-up reminders, contacts, or any other details…"
              className="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 placeholder-gray-400 transition-colors focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
              {...register('notes')}
            />
          </div>

          {/* Submit */}
          <div className="flex justify-end gap-3 pt-2">
            <Button
              type="button"
              variant="ghost"
              onClick={() => navigate(ROUTES.JOB_APPLICATION_LIST)}
            >
              Cancel
            </Button>
            <Button type="submit" loading={isSubmitting}>
              <Save className="h-4 w-4" />
              {initialData ? 'Update Application' : 'Create Application'}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};
