/**
 * CertificationSection — manages certification entries for a resume.
 *
 * @author DevLaunch
 */

import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Plus, Pencil, Trash2, Award, X, Check } from 'lucide-react';
import { Card } from '../ui/Card';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Spinner } from '../ui/Spinner';
import type {
  CertificationResponse,
  CreateCertificationRequest,
  UpdateCertificationRequest,
} from '../../types/resume';

const certificationSchema = z.object({
  certificationName: z.string().min(1, 'Certification name is required'),
  issuingOrganization: z.string().min(1, 'Issuing organization is required'),
  issueDate: z.string().optional(),
  expiryDate: z.string().optional(),
  credentialId: z.string().optional(),
  credentialUrl: z.string().url('Invalid URL').or(z.literal('')).optional(),
});

type CertificationFormValues = z.infer<typeof certificationSchema>;

interface CertificationSectionProps {
  items: CertificationResponse[];
  isLoading: boolean;
  onCreate: (data: CreateCertificationRequest) => Promise<void>;
  onUpdate: (id: number, data: UpdateCertificationRequest) => Promise<void>;
  onDelete: (id: number) => Promise<void>;
}

export const CertificationSection: React.FC<CertificationSectionProps> = ({
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
            <h3 className="text-base font-semibold text-gray-900">Certifications</h3>
            <p className="mt-0.5 text-xs text-gray-500">
              Add your professional certifications and credentials.
            </p>
          </div>
          {!isAdding && (
            <Button size="sm" variant="outline" onClick={() => setIsAdding(true)}>
              <Plus className="h-4 w-4" />
              Add Certification
            </Button>
          )}
        </div>
      }
    >
      {isLoading ? (
        <Spinner size="md" label="Loading certifications…" />
      ) : (
        <div className="space-y-4">
          {isAdding && (
            <CertFormCard
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
              No certifications yet. Click "Add Certification" to get started.
            </p>
          ) : (
            items.map((item) =>
              editingId === item.id ? (
                <CertFormCard
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
                <CertDisplayCard
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

interface CertFormCardProps {
  initialData?: CertificationResponse;
  onSave: (data: CertificationFormValues) => Promise<void>;
  onCancel: () => void;
  isSubmitting: boolean;
}

const CertFormCard: React.FC<CertFormCardProps> = ({
  initialData,
  onSave,
  onCancel,
  isSubmitting,
}) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<CertificationFormValues>({
    resolver: zodResolver(certificationSchema),
    defaultValues: initialData
      ? {
          certificationName: initialData.certificationName,
          issuingOrganization: initialData.issuingOrganization,
          issueDate: initialData.issueDate ?? '',
          expiryDate: initialData.expiryDate ?? '',
          credentialId: initialData.credentialId ?? '',
          credentialUrl: initialData.credentialUrl ?? '',
        }
      : {},
  });

  return (
    <form
      onSubmit={handleSubmit(onSave)}
      className="rounded-lg border border-primary-200 bg-primary-50/50 p-4 space-y-3"
    >
      <div className="grid gap-3 sm:grid-cols-2">
        <Input
          label="Certification Name"
          placeholder="e.g. AWS Solutions Architect"
          error={errors.certificationName?.message}
          {...register('certificationName')}
        />
        <Input
          label="Issuing Organization"
          placeholder="e.g. Amazon Web Services"
          error={errors.issuingOrganization?.message}
          {...register('issuingOrganization')}
        />
      </div>

      <div className="grid gap-3 sm:grid-cols-2">
        <Input label="Issue Date" type="date" {...register('issueDate')} />
        <Input label="Expiry Date" type="date" {...register('expiryDate')} />
      </div>

      <div className="grid gap-3 sm:grid-cols-2">
        <Input
          label="Credential ID"
          placeholder="e.g. AWS-12345"
          {...register('credentialId')}
        />
        <Input
          label="Credential URL"
          placeholder="https://credential.example.com"
          {...register('credentialUrl')}
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

interface CertDisplayCardProps {
  item: CertificationResponse;
  onEdit: () => void;
  onDelete: () => Promise<void>;
}

const CertDisplayCard: React.FC<CertDisplayCardProps> = ({
  item,
  onEdit,
  onDelete,
}) => {
  return (
    <div className="group flex items-start gap-3 rounded-lg border border-gray-100 bg-white p-3 transition-colors hover:border-gray-200">
      <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-lg bg-indigo-100 text-indigo-600">
        <Award className="h-4 w-4" />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-sm font-medium text-gray-900">{item.certificationName}</p>
        <p className="text-xs text-gray-500">{item.issuingOrganization}</p>
        {item.issueDate && (
          <p className="mt-0.5 text-xs text-gray-400">
            Issued: {item.issueDate}
            {item.expiryDate ? ` — Expires: ${item.expiryDate}` : ''}
          </p>
        )}
        {item.credentialId && (
          <p className="mt-0.5 text-xs text-gray-400">ID: {item.credentialId}</p>
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
