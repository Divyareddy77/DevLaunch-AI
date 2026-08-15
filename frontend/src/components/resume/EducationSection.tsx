/**
 * EducationSection — manages education entries for a resume.
 *
 * Displays existing entries and provides inline forms for adding,
 * editing, and deleting education records.
 *
 * @author DevLaunch
 */

import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Plus, Pencil, Trash2, GraduationCap, X, Check } from 'lucide-react';
import { Card } from '../ui/Card';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Spinner } from '../ui/Spinner';
import type {
  EducationResponse,
  CreateEducationRequest,
  UpdateEducationRequest,
} from '../../types/resume';

const educationSchema = z.object({
  institutionName: z.string().min(1, 'Institution name is required'),
  degree: z.string().min(1, 'Degree is required'),
  fieldOfStudy: z.string().min(1, 'Field of study is required'),
  grade: z.string().optional(),
  startDate: z.string().optional(),
  endDate: z.string().optional(),
  currentlyStudying: z.boolean().optional(),
  description: z.string().optional(),
});

type EducationFormValues = z.infer<typeof educationSchema>;

interface EducationSectionProps {
  /** List of current education entries. */
  items: EducationResponse[];
  /** Whether data is loading. */
  isLoading: boolean;
  /** Callback to create a new education entry. */
  onCreate: (data: CreateEducationRequest) => Promise<void>;
  /** Callback to update an existing education entry. */
  onUpdate: (id: number, data: UpdateEducationRequest) => Promise<void>;
  /** Callback to delete an education entry. */
  onDelete: (id: number) => Promise<void>;
}

export const EducationSection: React.FC<EducationSectionProps> = ({
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
            <h3 className="text-base font-semibold text-gray-900">Education</h3>
            <p className="mt-0.5 text-xs text-gray-500">
              Add your academic qualifications and degrees.
            </p>
          </div>
          {!isAdding && (
            <Button
              size="sm"
              variant="outline"
              onClick={() => setIsAdding(true)}
            >
              <Plus className="h-4 w-4" />
              Add Education
            </Button>
          )}
        </div>
      }
    >
      {isLoading ? (
        <Spinner size="md" label="Loading education…" />
      ) : (
        <div className="space-y-4">
          {/* Add form */}
          {isAdding && (
            <EducationFormCard
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

          {/* Existing entries */}
          {items.length === 0 && !isAdding ? (
            <p className="text-sm text-gray-400 text-center py-4">
              No education entries yet. Click "Add Education" to get started.
            </p>
          ) : (
            items.map((item) =>
              editingId === item.id ? (
                <EducationFormCard
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
                <EducationDisplayCard
                  key={item.id}
                  item={item}
                  onEdit={() => setEditingId(item.id)}
                  onDelete={async () => {
                    await onDelete(item.id);
                  }}
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

interface EducationFormCardProps {
  initialData?: EducationResponse;
  onSave: (data: EducationFormValues) => Promise<void>;
  onCancel: () => void;
  isSubmitting: boolean;
}

const EducationFormCard: React.FC<EducationFormCardProps> = ({
  initialData,
  onSave,
  onCancel,
  isSubmitting,
}) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<EducationFormValues>({
    resolver: zodResolver(educationSchema),
    defaultValues: initialData
      ? {
          institutionName: initialData.institutionName,
          degree: initialData.degree,
          fieldOfStudy: initialData.fieldOfStudy,
          grade: initialData.grade ?? '',
          startDate: initialData.startDate ?? '',
          endDate: initialData.endDate ?? '',
          currentlyStudying: initialData.currentlyStudying,
          description: initialData.description ?? '',
        }
      : {
          currentlyStudying: false,
        },
  });

  const [currentlyStudying, setCurrentlyStudying] = React.useState(
    initialData?.currentlyStudying ?? false,
  );

  return (
    <form
      onSubmit={handleSubmit(onSave)}
      className="rounded-lg border border-primary-200 bg-primary-50/50 p-4 space-y-3"
    >
      <div className="grid gap-3 sm:grid-cols-2">
        <Input
          label="Institution"
          placeholder="e.g. Stanford University"
          error={errors.institutionName?.message}
          {...register('institutionName')}
        />
        <Input
          label="Degree"
          placeholder="e.g. Bachelor of Science"
          error={errors.degree?.message}
          {...register('degree')}
        />
      </div>

      <div className="grid gap-3 sm:grid-cols-2">
        <Input
          label="Field of Study"
          placeholder="e.g. Computer Science"
          error={errors.fieldOfStudy?.message}
          {...register('fieldOfStudy')}
        />
        <Input
          label="Grade / GPA"
          placeholder="e.g. 3.8 GPA"
          {...register('grade')}
        />
      </div>

      <div className="grid gap-3 sm:grid-cols-2">
        <Input
          label="Start Date"
          type="date"
          {...register('startDate')}
        />
        <Input
          label="End Date"
          type="date"
          disabled={currentlyStudying}
          {...register('endDate')}
        />
      </div>

      <label className="flex items-center gap-2 text-sm text-gray-700">
        <input
          type="checkbox"
          className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
          {...register('currentlyStudying')}
          onChange={(e) => setCurrentlyStudying(e.target.checked)}
        />
        I am currently studying here
      </label>

      <div>
        <label className="mb-1.5 block text-sm font-medium text-gray-700">
          Description (optional)
        </label>
        <textarea
          rows={2}
          placeholder="Relevant coursework, honours, activities…"
          className="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 placeholder-gray-400 transition-colors focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
          {...register('description')}
        />
      </div>

      <div className="flex justify-end gap-2">
        <Button type="button" variant="ghost" size="sm" onClick={onCancel}>
          <X className="h-4 w-4" />
          Cancel
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

interface EducationDisplayCardProps {
  item: EducationResponse;
  onEdit: () => void;
  onDelete: () => Promise<void>;
}

const EducationDisplayCard: React.FC<EducationDisplayCardProps> = ({
  item,
  onEdit,
  onDelete,
}) => {
  const [deleting, setDeleting] = useState(false);

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await onDelete();
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div className="group flex items-start gap-3 rounded-lg border border-gray-100 bg-white p-3 transition-colors hover:border-gray-200">
      <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-lg bg-indigo-100 text-indigo-600">
        <GraduationCap className="h-4 w-4" />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-sm font-medium text-gray-900">{item.degree}</p>
        <p className="text-xs text-gray-500">
          {item.institutionName}
          {item.fieldOfStudy ? ` — ${item.fieldOfStudy}` : ''}
        </p>
        {(item.startDate || item.endDate) && (
          <p className="mt-0.5 text-xs text-gray-400">
            {item.startDate ?? '?'} – {item.currentlyStudying ? 'Present' : item.endDate ?? '?'}
            {item.grade ? ` | ${item.grade}` : ''}
          </p>
        )}
        {item.description && (
          <p className="mt-1 text-xs text-gray-500 line-clamp-2">
            {item.description}
          </p>
        )}
      </div>
      <div className="flex flex-shrink-0 items-center gap-0.5 opacity-0 group-hover:opacity-100 transition-opacity">
        <button
          onClick={onEdit}
          className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors"
          aria-label="Edit"
        >
          <Pencil className="h-3.5 w-3.5" />
        </button>
        <button
          onClick={handleDelete}
          disabled={deleting}
          className="rounded-lg p-1.5 text-gray-400 hover:bg-red-50 hover:text-red-500 transition-colors"
          aria-label="Delete"
        >
          {deleting ? (
            <Spinner size="sm" />
          ) : (
            <Trash2 className="h-3.5 w-3.5" />
          )}
        </button>
      </div>
    </div>
  );
};
