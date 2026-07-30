/**
 * ProjectSection — manages project entries for a resume.
 *
 * @author DevLaunch
 */

import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Plus, Pencil, Trash2, FolderGit2, X, Check } from 'lucide-react';
import { Card } from '../ui/Card';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Spinner } from '../ui/Spinner';
import type {
  ProjectResponse,
  CreateProjectRequest,
  UpdateProjectRequest,
} from '../../types/resume';

const projectSchema = z.object({
  projectName: z.string().min(1, 'Project name is required'),
  description: z.string().min(1, 'Description is required'),
  technologies: z.string().min(1, 'Technologies are required'),
  githubUrl: z.string().url('Invalid URL').or(z.literal('')).optional(),
  liveUrl: z.string().url('Invalid URL').or(z.literal('')).optional(),
  startDate: z.string().optional(),
  endDate: z.string().optional(),
  currentlyWorking: z.boolean().optional(),
});

type ProjectFormValues = z.infer<typeof projectSchema>;

interface ProjectSectionProps {
  items: ProjectResponse[];
  isLoading: boolean;
  onCreate: (data: CreateProjectRequest) => Promise<void>;
  onUpdate: (id: number, data: UpdateProjectRequest) => Promise<void>;
  onDelete: (id: number) => Promise<void>;
}

export const ProjectSection: React.FC<ProjectSectionProps> = ({
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
            <h3 className="text-base font-semibold text-gray-900">Projects</h3>
            <p className="mt-0.5 text-xs text-gray-500">
              Highlight key projects from your portfolio.
            </p>
          </div>
          {!isAdding && (
            <Button size="sm" variant="outline" onClick={() => setIsAdding(true)}>
              <Plus className="h-4 w-4" />
              Add Project
            </Button>
          )}
        </div>
      }
    >
      {isLoading ? (
        <Spinner size="md" label="Loading projects…" />
      ) : (
        <div className="space-y-4">
          {isAdding && (
            <ProjectFormCard
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
              No projects yet. Click "Add Project" to get started.
            </p>
          ) : (
            items.map((item) =>
              editingId === item.id ? (
                <ProjectFormCard
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
                <ProjectDisplayCard
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

interface ProjectFormCardProps {
  initialData?: ProjectResponse;
  onSave: (data: ProjectFormValues) => Promise<void>;
  onCancel: () => void;
  isSubmitting: boolean;
}

const ProjectFormCard: React.FC<ProjectFormCardProps> = ({
  initialData,
  onSave,
  onCancel,
  isSubmitting,
}) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ProjectFormValues>({
    resolver: zodResolver(projectSchema),
    defaultValues: initialData
      ? {
          projectName: initialData.projectName,
          description: initialData.description,
          technologies: initialData.technologies,
          githubUrl: initialData.githubUrl ?? '',
          liveUrl: initialData.liveUrl ?? '',
          startDate: initialData.startDate ?? '',
          endDate: initialData.endDate ?? '',
          currentlyWorking: initialData.currentlyWorking,
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
      <Input
        label="Project Name"
        placeholder="e.g. E-Commerce Platform"
        error={errors.projectName?.message}
        {...register('projectName')}
      />

      <div>
        <label className="mb-1.5 block text-sm font-medium text-gray-700">
          Description
        </label>
        <textarea
          rows={3}
          placeholder="Describe the project, your role, and key features…"
          className="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 placeholder-gray-400 transition-colors focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
          {...register('description')}
        />
        {errors.description?.message && (
          <p className="mt-1.5 text-xs text-red-500">{errors.description.message}</p>
        )}
      </div>

      <Input
        label="Technologies Used"
        placeholder="e.g. React, Node.js, PostgreSQL"
        error={errors.technologies?.message}
        {...register('technologies')}
      />

      <div className="grid gap-3 sm:grid-cols-2">
        <Input
          label="GitHub URL"
          placeholder="https://github.com/user/project"
          {...register('githubUrl')}
        />
        <Input
          label="Live URL"
          placeholder="https://myproject.com"
          {...register('liveUrl')}
        />
      </div>

      <div className="grid gap-3 sm:grid-cols-2">
        <Input label="Start Date" type="date" {...register('startDate')} />
        <Input label="End Date" type="date" disabled={currentlyWorking} {...register('endDate')} />
      </div>

      <label className="flex items-center gap-2 text-sm text-gray-700">
        <input
          type="checkbox"
          className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
          {...register('currentlyWorking')}
          onChange={(e) => setCurrentlyWorking(e.target.checked)}
        />
        This project is ongoing
      </label>

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

interface ProjectDisplayCardProps {
  item: ProjectResponse;
  onEdit: () => void;
  onDelete: () => Promise<void>;
}

const ProjectDisplayCard: React.FC<ProjectDisplayCardProps> = ({
  item,
  onEdit,
  onDelete,
}) => {
  return (
    <div className="group flex items-start gap-3 rounded-lg border border-gray-100 bg-white p-3 transition-colors hover:border-gray-200">
      <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-lg bg-indigo-100 text-indigo-600">
        <FolderGit2 className="h-4 w-4" />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-sm font-medium text-gray-900">{item.projectName}</p>
        <p className="text-xs text-gray-500">{item.technologies}</p>
        {item.description && (
          <p className="mt-1 text-xs text-gray-500 line-clamp-2">{item.description}</p>
        )}
        {(item.startDate || item.endDate) && (
          <p className="mt-0.5 text-xs text-gray-400">
            {item.startDate ?? '?'} – {item.currentlyWorking ? 'Present' : item.endDate ?? '?'}
          </p>
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
