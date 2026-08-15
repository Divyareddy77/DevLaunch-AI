/**
 * PersonalInformationForm — form for editing the core resume fields.
 *
 * Uses React Hook Form + Zod for validation matching the backend
 * CreateResumeRequest / UpdateResumeRequest constraints.
 *
 * @author DevLaunch
 */

import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Save } from 'lucide-react';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import { Card } from '../ui/Card';
import type { ResumeResponse, UpdateResumeRequest } from '../../types/resume';

const personalInfoSchema = z.object({
  headline: z
    .string()
    .min(1, 'Headline is required')
    .max(255, 'Headline must be at most 255 characters'),
  summary: z
    .string()
    .min(1, 'Summary is required')
    .max(5000, 'Summary must be at most 5000 characters'),
  linkedinUrl: z
    .string()
    .url('Please enter a valid URL')
    .or(z.literal(''))
    .optional(),
  githubUrl: z
    .string()
    .url('Please enter a valid URL')
    .or(z.literal(''))
    .optional(),
  portfolioUrl: z
    .string()
    .url('Please enter a valid URL')
    .or(z.literal(''))
    .optional(),
});

type PersonalInfoFormValues = z.infer<typeof personalInfoSchema>;

interface PersonalInformationFormProps {
  /** Initial resume data for edit mode. */
  initialData?: ResumeResponse | null;
  /** Whether the form is in a loading/submitting state. */
  isSubmitting: boolean;
  /** Callback with the form values on save. */
  onSubmit: (data: UpdateResumeRequest) => void;
}

export const PersonalInformationForm: React.FC<PersonalInformationFormProps> = ({
  initialData,
  isSubmitting,
  onSubmit,
}) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<PersonalInfoFormValues>({
    resolver: zodResolver(personalInfoSchema),
    defaultValues: {
      headline: initialData?.headline ?? '',
      summary: initialData?.summary ?? '',
      linkedinUrl: initialData?.linkedinUrl ?? '',
      githubUrl: initialData?.githubUrl ?? '',
      portfolioUrl: initialData?.portfolioUrl ?? '',
    },
    values: initialData
      ? {
          headline: initialData.headline,
          summary: initialData.summary,
          linkedinUrl: initialData.linkedinUrl ?? '',
          githubUrl: initialData.githubUrl ?? '',
          portfolioUrl: initialData.portfolioUrl ?? '',
        }
      : undefined,
  });

  return (
    <Card
      header={
        <div>
          <h3 className="text-base font-semibold text-gray-900">
            Personal Information
          </h3>
          <p className="mt-0.5 text-xs text-gray-500">
            Your professional headline, summary, and online profiles.
          </p>
        </div>
      }
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <Input
          label="Headline"
          placeholder="e.g. Senior Full-Stack Developer"
          error={errors.headline?.message}
          {...register('headline')}
        />

        <div>
          <label className="mb-1.5 block text-sm font-medium text-gray-700">
            Professional Summary
          </label>
          <textarea
            rows={4}
            placeholder="Write a brief professional summary highlighting your experience, skills, and career goals…"
            className="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 placeholder-gray-400 transition-colors focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-0"
            {...register('summary')}
          />
          {errors.summary?.message && (
            <p className="mt-1.5 text-xs text-red-500" role="alert">
              {errors.summary.message}
            </p>
          )}
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <Input
            label="LinkedIn URL"
            placeholder="https://linkedin.com/in/username"
            error={errors.linkedinUrl?.message}
            {...register('linkedinUrl')}
          />
          <Input
            label="GitHub URL"
            placeholder="https://github.com/username"
            error={errors.githubUrl?.message}
            {...register('githubUrl')}
          />
        </div>

        <Input
          label="Portfolio URL"
          placeholder="https://yourportfolio.com"
          error={errors.portfolioUrl?.message}
          {...register('portfolioUrl')}
        />

        <div className="flex justify-end">
          <Button type="submit" loading={isSubmitting}>
            <Save className="h-4 w-4" />
            Save Personal Information
          </Button>
        </div>
      </form>
    </Card>
  );
};
