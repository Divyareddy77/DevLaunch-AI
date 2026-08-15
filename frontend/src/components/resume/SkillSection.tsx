/**
 * SkillSection — manages skill entries for a resume.
 *
 * @author DevLaunch
 */

import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Plus, Pencil, Trash2, Code2 } from 'lucide-react';
import { Card } from '../ui/Card';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Badge } from '../ui/Badge';
import { Spinner } from '../ui/Spinner';
import type {
  SkillResponse,
  CreateSkillRequest,
  UpdateSkillRequest,
} from '../../types/resume';

const skillSchema = z.object({
  skillName: z.string().min(1, 'Skill name is required'),
  proficiency: z.string().optional(),
});

type SkillFormValues = z.infer<typeof skillSchema>;

const proficiencyColors: Record<string, 'primary' | 'success' | 'warning' | 'danger' | 'default' | 'info'> = {
  Beginner: 'warning',
  Intermediate: 'primary',
  Advanced: 'success',
  Expert: 'info',
};

interface SkillSectionProps {
  items: SkillResponse[];
  isLoading: boolean;
  onCreate: (data: CreateSkillRequest) => Promise<void>;
  onUpdate: (id: number, data: UpdateSkillRequest) => Promise<void>;
  onDelete: (id: number) => Promise<void>;
}

const PROFICIENCY_LEVELS = ['', 'Beginner', 'Intermediate', 'Advanced', 'Expert'] as const;

export const SkillSection: React.FC<SkillSectionProps> = ({
  items,
  isLoading,
  onCreate,
  onUpdate,
  onDelete,
}) => {
  const [isAdding, setIsAdding] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<SkillFormValues>({
    resolver: zodResolver(skillSchema),
  });

  const handleAdd = async (data: SkillFormValues) => {
    setIsSubmitting(true);
    try {
      await onCreate(data);
      setIsAdding(false);
      reset();
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleUpdate = async (id: number, data: SkillFormValues) => {
    setIsSubmitting(true);
    try {
      await onUpdate(id, data);
      setEditingId(null);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Card
      header={
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-base font-semibold text-gray-900">Skills</h3>
            <p className="mt-0.5 text-xs text-gray-500">
              List your technical and professional skills.
            </p>
          </div>
          {!isAdding && (
            <Button size="sm" variant="outline" onClick={() => setIsAdding(true)}>
              <Plus className="h-4 w-4" />
              Add Skill
            </Button>
          )}
        </div>
      }
    >
      {isLoading ? (
        <Spinner size="md" label="Loading skills…" />
      ) : (
        <div className="space-y-3">
          {/* Add form */}
          {isAdding && (
            <form
              onSubmit={handleSubmit(handleAdd)}
              className="flex flex-wrap items-end gap-3 rounded-lg border border-primary-200 bg-primary-50/50 p-4"
            >
              <div className="flex-1 min-w-[200px]">
                <Input
                  label="Skill Name"
                  placeholder="e.g. TypeScript"
                  error={errors.skillName?.message}
                  {...register('skillName')}
                />
              </div>
              <div className="w-40">
                <label className="mb-1.5 block text-sm font-medium text-gray-700">
                  Proficiency
                </label>
                <select
                  className="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 transition-colors focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
                  {...register('proficiency')}
                >
                  {PROFICIENCY_LEVELS.map((level) => (
                    <option key={level} value={level}>
                      {level || 'Select…'}
                    </option>
                  ))}
                </select>
              </div>
              <div className="flex gap-2 pb-0.5">
                <Button type="submit" size="sm" loading={isSubmitting}>
                  Add
                </Button>
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  onClick={() => {
                    setIsAdding(false);
                    reset();
                  }}
                >
                  Cancel
                </Button>
              </div>
            </form>
          )}

          {/* Skills list */}
          {items.length === 0 && !isAdding ? (
            <p className="text-sm text-gray-400 text-center py-4">
              No skills added yet. Click "Add Skill" to get started.
            </p>
          ) : (
            <div className="flex flex-wrap gap-2">
              {items.map((item) =>
                editingId === item.id ? (
                  <form
                    key={item.id}
                    onSubmit={handleSubmit((data) => handleUpdate(item.id, data))}
                    className="flex items-center gap-2 w-full rounded-lg border border-primary-200 bg-primary-50/50 p-2"
                  >
                    <div className="flex-1">
                      <Input
                        defaultValue={item.skillName}
                        placeholder="Skill name"
                        className="text-xs !py-1"
                        {...register('skillName')}
                      />
                    </div>
                    <select
                      className="rounded-lg border border-gray-300 bg-white px-2 py-1.5 text-xs"
                      defaultValue={item.proficiency ?? ''}
                      {...register('proficiency')}
                    >
                      {PROFICIENCY_LEVELS.map((l) => (
                        <option key={l} value={l}>{l || 'None'}</option>
                      ))}
                    </select>
                    <Button type="submit" size="sm" loading={isSubmitting}>
                      Save
                    </Button>
                    <Button type="button" variant="ghost" size="sm" onClick={() => setEditingId(null)}>
                      Cancel
                    </Button>
                  </form>
                ) : (
                  <div
                    key={item.id}
                    className="group flex items-center gap-2 rounded-lg border border-gray-100 bg-white px-3 py-2 transition-colors hover:border-gray-200"
                  >
                    <Code2 className="h-3.5 w-3.5 text-gray-400" />
                    <span className="text-sm text-gray-700">{item.skillName}</span>
                    {item.proficiency && (
                      <Badge
                        variant={proficiencyColors[item.proficiency] ?? 'default'}
                        size="sm"
                      >
                        {item.proficiency}
                      </Badge>
                    )}
                    <div className="ml-1 flex items-center gap-0.5 opacity-0 group-hover:opacity-100 transition-opacity">
                      <button
                        onClick={() => setEditingId(item.id)}
                        className="rounded p-0.5 text-gray-400 hover:text-gray-600"
                        aria-label="Edit"
                      >
                        <Pencil className="h-3 w-3" />
                      </button>
                      <button
                        onClick={async () => { onDelete(item.id); }}
                        className="rounded p-0.5 text-gray-400 hover:text-red-500"
                        aria-label="Delete"
                      >
                        <Trash2 className="h-3 w-3" />
                      </button>
                    </div>
                  </div>
                ),
              )}
            </div>
          )}
        </div>
      )}
    </Card>
  );
};


