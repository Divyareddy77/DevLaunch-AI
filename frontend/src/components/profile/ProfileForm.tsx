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
import { Save } from 'lucide-react';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import { Card } from '../ui/Card';
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
    <Card
      header={
        <div>
          <h3 className="text-base font-semibold text-gray-900">Edit Profile</h3>
          <p className="mt-0.5 text-xs text-gray-500">
            Update your name and contact number. Email and role are managed by the system.
          </p>
        </div>
      }
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <div className="grid gap-4 sm:grid-cols-2">
          <Input
            label="First Name"
            placeholder="John"
            autoComplete="given-name"
            error={errors.firstName?.message}
            {...register('firstName')}
          />
          <Input
            label="Last Name"
            placeholder="Doe"
            autoComplete="family-name"
            error={errors.lastName?.message}
            {...register('lastName')}
          />
        </div>

        <Input
          label="Phone"
          type="tel"
          placeholder="10-digit mobile number"
          autoComplete="tel"
          error={errors.phone?.message}
          {...register('phone')}
        />

        <div className="flex justify-end">
          <Button type="submit" loading={isSubmitting}>
            <Save className="h-4 w-4" />
            Save Changes
          </Button>
        </div>
      </form>
    </Card>
  );
};
