/**
 * ExperienceSection — manages work experience entries for a resume.
 *
 * @author DevLaunch
 */

import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Plus, Pencil, Trash2, Briefcase, X, Check } from 'lucide-react';
import { Card } from '../ui/Card';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Spinner } from '../ui/Spinner';
import type {
  ExperienceResponse,
  CreateExperienceRequest,
  UpdateExperienceRequest,
} from '../../types/resume';

const experienceSchema = z.object({
  companyName: z.string().min(1, 'Company name is required'),
  jobTitle: z.string().min(1, 'Job title is required'),
  employmentType: z.string().optional(),
  location: z.string().optional(),
  startDate: z.string().min(1, 'Start date is required'),
  endDate: z.string().optional(),
  currentlyWorking: z.boolean().optional(),
  description: z.string().optional(),
});

type ExperienceFormValues = z.infer<typeof experienceSchema>;

interface ExperienceSectionProps {
  items: ExperienceResponse[];
  isLoading: boolean;
  onCreate: (data: CreateExperienceRequest) => Promise<void>;
  onUpdate: (id: number, data: UpdateExperienceRequest) => Promise<void>;
  onDelete: (id: number) => Promise<void>;
}

export const ExperienceSection: React.FC<ExperienceSectionProps> = ({
  items,
  isLoading,
  onCreate,
  onUpdate,
  onDelete,
}) => {
  const [isAdding, setIsAdding] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  return (
    <Card
      header={
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-base font-semibold text-gray-900">Experience</h3>
            <p className="mt-0.5 text-xs text-gray-500">
              Showcase your professional work history.
            </p>
          </div>
          {!isAdding && (
            <Button size="sm" variant="outline" onClick={() => setIsAdding(true)}>
              <Plus className="h-4 w-4" />
              Add Experience
            </Button>
          )}
        </div>
      }
    >
      {isLoading ? (
        <Spinner size="md" label="Loading experience…" />
      ) : (
        <div className="space-y-4">
          {isAdding && (
            <ExperienceFormCard
              onSave={async (data) => {
                setIsSubmitting(true);
                try {
                  await onCreate(data);
                  setIsAdding(false);
                } finally {
                  setIsSubmitting(false);
                }
              }}
              onCancel={() => setIsAdding(false)}
              isSubmitting={isSubmitting}
            />
          )}

          {items.length === 0 && !isAdding ? (
            <p className="text-sm text-gray-400 text-center py-4">
              No experience entries yet. Click "Add Experience" to get started.
            </p>
          ) : (
            items.map((item) =>
              editingId === item.id ? (
                <ExperienceFormCard
                  key={item.id}
                  initialData={item}
                  onSave={async (data) => {
                    setIsSubmitting(true);
                    try {
                      await onUpdate(item.id, data);
                      setEditingId(null);
                    } finally {
                      setIsSubmitting(false);
                    }
                  }}
                  onCancel={() => setEditingId(null)}
                  isSubmitting={isSubmitting}
                />
              ) : (
                <ExperienceDisplayCard
                  key={item.id}
                  item={item}
                  onEdit={() => setEditingId(item.id)}
                  onDelete={async () => onDelete(item.id)}
                />
              ),
            )
          )}
        </div>
      )}
    </Card>
  );
};

// ─── Inline form card ───

interface ExperienceFormCardProps {
  initialData?: ExperienceResponse;
  onSave: (data: ExperienceFormValues) => Promise<void>;
  onCancel: () => void;
  isSubmitting: boolean;
}

const ExperienceFormCard: React.FC<ExperienceFormCardProps> = ({
  initialData,
  onSave,
  onCancel,
  isSubmitting,
}) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ExperienceFormValues>({
    resolver: zodResolver(experienceSchema),
    defaultValues: initialData
      ? {
          companyName: initialData.companyName,
          jobTitle: initialData.jobTitle,
          employmentType: initialData.employmentType ?? '',
          location: initialData.location ?? '',
          startDate: initialData.startDate ?? '',
          endDate: initialData.endDate ?? '',
          currentlyWorking: initialData.currentlyWorking,
          description: initialData.description ?? '',
        }
      : { currentlyWorking: false },
  });

  const [currentlyWorking, setCurrentlyWorking] = React.useState(
    initialData?.currentlyWorking ?? false,
  );

  return (
    <form
      onSubmit={handleSubmit(onSave)}
      className="rounded-lg border border-primary-200 bg-primary-50/50 p-4 space-y-3"
    >
      <div className="grid gap-3 sm:grid-cols-2">
        <Input
          label="Company"
          placeholder="e.g. Google"
          error={errors.companyName?.message}
          {...register('companyName')}
        />
        <Input
          label="Job Title"
          placeholder="e.g. Software Engineer"
          error={errors.jobTitle?.message}
          {...register('jobTitle')}
        />
      </div>

      <div className="grid gap-3 sm:grid-cols-2">
        <Input
          label="Employment Type"
          placeholder="e.g. Full-time, Contract"
          {...register('employmentType')}
        />
        <Input
          label="Location"
          placeholder="e.g. San Francisco, CA"
          {...register('location')}
        />
      </div>

      <div className="grid gap-3 sm:grid-cols-2">
        <Input
          label="Start Date"
          type="date"
          error={errors.startDate?.message}
          {...register('startDate')}
        />
        <Input
          label="End Date"
          type="date"
          disabled={currentlyWorking}
          {...register('endDate')}
        />
      </div>

      <label className="flex items-center gap-2 text-sm text-gray-700">
        <input
          type="checkbox"
          className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
          {...register('currentlyWorking')}
          onChange={(e) => setCurrentlyWorking(e.target.checked)}
        />
        I currently work here
      </label>

      <div>
        <label className="mb-1.5 block text-sm font-medium text-gray-700">
          Description (optional)
        </label>
        <textarea
          rows={3}
          placeholder="Describe your responsibilities, achievements, and technologies used…"
          className="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 placeholder-gray-400 transition-colors focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
          {...register('description')}
        />
      </div>

      <div className="flex justify-end gap-2">
        <Button type="button" variant="ghost" size="sm" onClick={onCancel}>
          <X className="h-4 w-4" /> Cancel
        </Button>
        <Button type="submit" size="sm" loading={isSubmitting}>
          <Check className="h-4 w-4" />
          {initialData ? 'Update' : 'Add'}
        </Button>
      </div>
    </form>
  );
};

// ─── Display card ───

interface ExperienceDisplayCardProps {
  item: ExperienceResponse;
  onEdit: () => void;
  onDelete: () => Promise<void>;
}

const ExperienceDisplayCard: React.FC<ExperienceDisplayCardProps> = ({
  item,
  onEdit,
  onDelete,
}) => {
  const [deleting, setDeleting] = useState(false);

  return (
    <div className="group flex items-start gap-3 rounded-lg border border-gray-100 bg-white p-3 transition-colors hover:border-gray-200">
      <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-lg bg-indigo-100 text-indigo-600">
        <Briefcase className="h-4 w-4" />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-sm font-medium text-gray-900">{item.jobTitle}</p>
        <p className="text-xs text-gray-500">
          {item.companyName}
          {item.location ? ` — ${item.location}` : ''}
          {item.employmentType ? ` (${item.employmentType})` : ''}
        </p>
        <p className="mt-0.5 text-xs text-gray-400">
          {item.startDate} – {item.currentlyWorking ? 'Present' : item.endDate ?? '?'}
        </p>
        {item.description && (
          <p className="mt-1 text-xs text-gray-500 line-clamp-2">{item.description}</p>
        )}
      </div>
      <div className="flex flex-shrink-0 items-center gap-0.5 opacity-0 group-hover:opacity-100 transition-opacity">
        <button onClick={onEdit} className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100 hover:text-gray-600" aria-label="Edit">
          <Pencil className="h-3.5 w-3.5" />
        </button>
        <button onClick={async () => { setDeleting(true); try { await onDelete(); } finally { setDeleting(false); } }} disabled={deleting} className="rounded-lg p-1.5 text-gray-400 hover:bg-red-50 hover:text-red-500" aria-label="Delete">
          {deleting ? <Spinner size="sm" /> : <Trash2 className="h-3.5 w-3.5" />}
        </button>
      </div>
    </div>
  );
};
