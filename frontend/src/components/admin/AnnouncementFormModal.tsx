/**
 * AnnouncementFormModal — create/edit form for announcements.
 *
 * Uses React Hook Form + Zod with constraints mirroring the backend
 * AnnouncementRequest DTO. Renders inside the shared Modal so it can be
 * reused for both creating and editing announcements.
 *
 * @see backend/src/main/java/com/devlaunch/dto/request/AnnouncementRequest.java
 * @author DevLaunch
 */

import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Megaphone } from 'lucide-react';
import { Modal } from '../ui/Modal';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import type { AdminAnnouncementResponse, AnnouncementRequest } from '../../types/admin';

const announcementSchema = z.object({
  title: z
    .string()
    .min(1, 'Title is required')
    .max(255, 'Title must not exceed 255 characters'),
  content: z.string().min(1, 'Content is required'),
  isActive: z.boolean(),
});

type AnnouncementFormValues = z.infer<typeof announcementSchema>;

interface AnnouncementFormModalProps {
  /** Whether the modal is visible. */
  isOpen: boolean;
  /** The announcement being edited, or null when creating. */
  announcement: AdminAnnouncementResponse | null;
  /** Whether the save request is in flight. */
  isSubmitting: boolean;
  /** Closes the modal. */
  onClose: () => void;
  /** Saves the announcement with the validated values. */
  onSubmit: (data: AnnouncementRequest) => void;
}

export const AnnouncementFormModal: React.FC<AnnouncementFormModalProps> = ({
  isOpen,
  announcement,
  isSubmitting,
  onClose,
  onSubmit,
}) => {
  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors },
  } = useForm<AnnouncementFormValues>({
    resolver: zodResolver(announcementSchema),
    defaultValues: {
      title: announcement?.title ?? '',
      content: announcement?.content ?? '',
      isActive: announcement?.isActive ?? true,
    },
  });

  // Re-sync the form whenever a different announcement is opened.
  React.useEffect(() => {
    if (isOpen) {
      reset({
        title: announcement?.title ?? '',
        content: announcement?.content ?? '',
        isActive: announcement?.isActive ?? true,
      });
    }
  }, [isOpen, announcement, reset]);

  const isActive = watch('isActive');

  const handleFormSubmit = (values: AnnouncementFormValues) => {
    onSubmit({
      title: values.title,
      content: values.content,
      isActive: values.isActive,
    });
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={announcement ? 'Edit Announcement' : 'New Announcement'}
      closeOnBackdrop={false}
    >
      <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4" noValidate>
        <Input
          label="Title"
          placeholder="e.g. New Mock Interview Categories are live"
          error={errors.title?.message}
          {...register('title')}
        />

        <div>
          <label
            htmlFor="announcement-content"
            className="mb-1.5 block text-sm font-medium text-gray-700"
          >
            Content
          </label>
          <textarea
            id="announcement-content"
            rows={4}
            placeholder="Write the announcement body…"
            className={`
              block w-full rounded-lg border bg-white px-3 py-2 text-sm text-gray-900
              placeholder-gray-400 transition-colors focus:outline-none focus:ring-2
              ${
                errors.content
                  ? 'border-red-300 focus:border-red-500 focus:ring-red-500'
                  : 'border-gray-300 focus:border-primary-500 focus:ring-primary-500'
              }
            `}
            aria-invalid={errors.content ? 'true' : 'false'}
            {...register('content')}
          />
          {errors.content && (
            <p className="mt-1.5 text-xs text-red-500" role="alert">
              {errors.content.message}
            </p>
          )}
        </div>

        <label className="flex cursor-pointer items-center gap-2.5">
          <input
            type="checkbox"
            className="h-4 w-4 rounded border-gray-300 text-primary-600 focus:ring-primary-500"
            {...register('isActive')}
          />
          <span className="text-sm text-gray-700">
            Active{isActive ? ' — visible to users' : ' — hidden (draft)'}
          </span>
        </label>

        <div className="flex justify-end gap-2 border-t border-gray-100 pt-4">
          <Button variant="ghost" onClick={onClose} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button type="submit" loading={isSubmitting}>
            <Megaphone className="h-4 w-4" />
            {announcement ? 'Save Changes' : 'Publish'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};
