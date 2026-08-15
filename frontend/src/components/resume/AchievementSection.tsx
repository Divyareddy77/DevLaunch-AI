/**
 * AchievementSection — manages achievement entries for a resume.
 *
 * @author DevLaunch
 */

import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Plus, Pencil, Trash2, Trophy, X, Check } from 'lucide-react';
import { Card } from '../ui/Card';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Spinner } from '../ui/Spinner';
import type {
  AchievementResponse,
  CreateAchievementRequest,
  UpdateAchievementRequest,
} from '../../types/resume';

const achievementSchema = z.object({
  title: z.string().min(1, 'Title is required'),
  description: z.string().optional(),
  dateAchieved: z.string().optional(),
});

type AchievementFormValues = z.infer<typeof achievementSchema>;

interface AchievementSectionProps {
  items: AchievementResponse[];
  isLoading: boolean;
  onCreate: (data: CreateAchievementRequest) => Promise<void>;
  onUpdate: (id: number, data: UpdateAchievementRequest) => Promise<void>;
  onDelete: (id: number) => Promise<void>;
}

export const AchievementSection: React.FC<AchievementSectionProps> = ({
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
            <h3 className="text-base font-semibold text-gray-900">Achievements</h3>
            <p className="mt-0.5 text-xs text-gray-500">
              Highlight your notable accomplishments.
            </p>
          </div>
          {!isAdding && (
            <Button size="sm" variant="outline" onClick={() => setIsAdding(true)}>
              <Plus className="h-4 w-4" />
              Add Achievement
            </Button>
          )}
        </div>
      }
    >
      {isLoading ? (
        <Spinner size="md" label="Loading achievements…" />
      ) : (
        <div className="space-y-4">
          {isAdding && (
            <AchievementFormCard
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
              No achievements yet. Click "Add Achievement" to get started.
            </p>
          ) : (
            items.map((item) =>
              editingId === item.id ? (
                <AchievementFormCard
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
                <AchievementDisplayCard
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

// ─── Inline form ───

interface AchievementFormCardProps {
  initialData?: AchievementResponse;
  onSave: (data: AchievementFormValues) => Promise<void>;
  onCancel: () => void;
  isSubmitting: boolean;
}

const AchievementFormCard: React.FC<AchievementFormCardProps> = ({
  initialData,
  onSave,
  onCancel,
  isSubmitting,
}) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<AchievementFormValues>({
    resolver: zodResolver(achievementSchema),
    defaultValues: initialData
      ? {
          title: initialData.title,
          description: initialData.description ?? '',
          dateAchieved: initialData.dateAchieved ?? '',
        }
      : {},
  });

  return (
    <form
      onSubmit={handleSubmit(onSave)}
      className="rounded-lg border border-primary-200 bg-primary-50/50 p-4 space-y-3"
    >
      <Input
        label="Achievement Title"
        placeholder="e.g. Employee of the Month"
        error={errors.title?.message}
        {...register('title')}
      />

      <div>
        <label className="mb-1.5 block text-sm font-medium text-gray-700">
          Description (optional)
        </label>
        <textarea
          rows={2}
          placeholder="Describe the achievement, its impact, and recognition received…"
          className="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 placeholder-gray-400 transition-colors focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
          {...register('description')}
        />
      </div>

      <Input label="Date Achieved" type="date" {...register('dateAchieved')} />

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

interface AchievementDisplayCardProps {
  item: AchievementResponse;
  onEdit: () => void;
  onDelete: () => Promise<void>;
}

const AchievementDisplayCard: React.FC<AchievementDisplayCardProps> = ({
  item,
  onEdit,
  onDelete,
}) => {
  return (
    <div className="group flex items-start gap-3 rounded-lg border border-gray-100 bg-white p-3 transition-colors hover:border-gray-200">
      <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-lg bg-amber-100 text-amber-600">
        <Trophy className="h-4 w-4" />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-sm font-medium text-gray-900">{item.title}</p>
        {item.description && (
          <p className="mt-0.5 text-xs text-gray-500 line-clamp-2">{item.description}</p>
        )}
        {item.dateAchieved && (
          <p className="mt-0.5 text-xs text-gray-400">{item.dateAchieved}</p>
        )}
      </div>
      <div className="flex flex-shrink-0 items-center gap-0.5 opacity-0 group-hover:opacity-100 transition-opacity">
        <button onClick={onEdit} className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100 hover:text-gray-600" aria-label="Edit">
          <Pencil className="h-3.5 w-3.5" />
        </button>
        <button onClick={async () => { onDelete(); }} className="rounded-lg p-1.5 text-gray-400 hover:bg-red-50 hover:text-red-500" aria-label="Delete">
          <Trash2 className="h-3.5 w-3.5" />
        </button>
      </div>
    </div>
  );
};
