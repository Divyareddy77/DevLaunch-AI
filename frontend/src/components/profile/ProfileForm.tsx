/**
 * ProfileForm — form for editing the authenticated user's profile.
 *
 * Uses React Hook Form + Zod with constraints mirroring the backend
 * UpdateUserRequest DTO. Only the fields the backend allows (first
 * name, last name, phone) are editable — email and role are managed
 * by the backend and deliberately omitted.
 *
 * @see backend/src/main/java/com/devlaunch/dto/request/UpdateUserRequest.java
 * @author DevLaunch
 */

import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Save, UserRound, Phone } from 'lucide-react';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import type { UserResponse, UpdateUserRequest } from '../../types/user';

const profileFormSchema = z.object({
  firstName: z
    .string()
    .min(1, 'First name is required')
    .max(50, 'First name must not exceed 50 characters'),
  lastName: z
    .string()
    .min(1, 'Last name is required')
    .max(50, 'Last name must not exceed 50 characters'),
  phone: z
    .string()
    .min(1, 'Phone number is required')
    .regex(/^[6-9]\d{9}$/, 'Phone number must be a valid 10-digit Indian mobile number'),
});

type ProfileFormValues = z.infer<typeof profileFormSchema>;

interface ProfileFormProps {
  /** Initial profile data used to prefill the form. */
  initialData: UserResponse;
  /** Whether the form is currently submitting. */
  isSubmitting: boolean;
  /** Callback with the validated form values on save. */
  onSubmit: (data: UpdateUserRequest) => void;
}

/** Shared label styling for prominent form labels. */
const LABEL_CLASSES = 'text-sm font-semibold text-gray-800';

export const ProfileForm: React.FC<ProfileFormProps> = ({
  initialData,
  isSubmitting,
  onSubmit,
}) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ProfileFormValues>({
    resolver: zodResolver(profileFormSchema),
    values: {
      firstName: initialData.firstName,
      lastName: initialData.lastName,
      phone: initialData.phone,
    },
  });

  return (
    <section className="overflow-hidden rounded-2xl border border-gray-100 bg-white shadow-[0_1px_3px_rgba(16,24,40,0.06)] transition-shadow duration-300 hover:shadow-[0_16px_40px_-12px_rgba(16,24,40,0.12)]">
      {/* Header */}
      <header className="flex items-center gap-3 border-b border-gray-100 px-6 py-5">
        <span className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-xl bg-primary-50 text-primary-600">
          <UserRound className="h-5 w-5" />
        </span>
        <div className="min-w-0">
          <h3 className="text-base font-semibold text-gray-900">Edit Profile</h3>
          <p className="mt-0.5 text-xs text-gray-500">
            Update your name and contact number. Email and role are managed by the system.
          </p>
        </div>
      </header>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5 p-6" noValidate>
        <div className="grid gap-5 sm:grid-cols-2">
          <Input
            label="First Name"
            labelClassName={LABEL_CLASSES}
            placeholder="John"
            autoComplete="given-name"
            leftIcon={<UserRound className="h-4 w-4" />}
            error={errors.firstName?.message}
            {...register('firstName')}
          />
          <Input
            label="Last Name"
            labelClassName={LABEL_CLASSES}
            placeholder="Doe"
            autoComplete="family-name"
            leftIcon={<UserRound className="h-4 w-4" />}
            error={errors.lastName?.message}
            {...register('lastName')}
          />
        </div>

        <Input
          label="Phone"
          labelClassName={LABEL_CLASSES}
          type="tel"
          placeholder="10-digit mobile number"
          autoComplete="tel"
          leftIcon={<Phone className="h-4 w-4" />}
          error={errors.phone?.message}
          {...register('phone')}
        />

        <div className="flex justify-end border-t border-gray-100 pt-5">
          <Button type="submit" loading={isSubmitting} className="hover:-translate-y-0.5 hover:shadow-md">
            <Save className="h-4 w-4" />
            Save Changes
          </Button>
        </div>
      </form>
    </section>
  );
};
