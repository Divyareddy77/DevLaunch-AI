/**
 * ChangePasswordForm — form for changing the authenticated user's
 * password.
 *
 * Uses React Hook Form + Zod with constraints mirroring the backend
 * ChangePasswordRequest DTO (new password 8–100 characters), plus a
 * confirmation field validated on the client. The form resets itself
 * after a successful submission.
 *
 * @see backend/src/main/java/com/devlaunch/dto/request/ChangePasswordRequest.java
 * @author DevLaunch
 */

import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { KeyRound } from 'lucide-react';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import { Card } from '../ui/Card';

const changePasswordSchema = z
  .object({
    currentPassword: z.string().min(1, 'Current password is required'),
    newPassword: z
      .string()
      .min(8, 'New password must be at least 8 characters')
      .max(100, 'New password must not exceed 100 characters'),
    confirmPassword: z.string().min(1, 'Please confirm your new password'),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  });

export type ChangePasswordFormValues = z.infer<typeof changePasswordSchema>;

interface ChangePasswordFormProps {
  /** Whether the form is currently submitting. */
  isSubmitting: boolean;
  /**
   * Callback with the validated form values. Should resolve with
   * `true` on success (resets the form) or `false` on failure.
   */
  onSubmit: (data: ChangePasswordFormValues) => Promise<boolean>;
}

export const ChangePasswordForm: React.FC<ChangePasswordFormProps> = ({
  isSubmitting,
  onSubmit,
}) => {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ChangePasswordFormValues>({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: {
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
    },
  });

  const handleFormSubmit = async (values: ChangePasswordFormValues) => {
    const success = await onSubmit(values);
    if (success) {
      reset();
    }
  };

  return (
    <Card
      header={
        <div>
          <h3 className="text-base font-semibold text-gray-900">Change Password</h3>
          <p className="mt-0.5 text-xs text-gray-500">
            Keep your account secure with a strong password.
          </p>
        </div>
      }
    >
      <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4" noValidate>
        <Input
          label="Current Password"
          type="password"
          placeholder="Enter your current password"
          autoComplete="current-password"
          error={errors.currentPassword?.message}
          {...register('currentPassword')}
        />

        <Input
          label="New Password"
          type="password"
          placeholder="At least 8 characters"
          autoComplete="new-password"
          error={errors.newPassword?.message}
          {...register('newPassword')}
        />

        <Input
          label="Confirm New Password"
          type="password"
          placeholder="Repeat your new password"
          autoComplete="new-password"
          error={errors.confirmPassword?.message}
          {...register('confirmPassword')}
        />

        <div className="flex justify-end">
          <Button type="submit" loading={isSubmitting}>
            <KeyRound className="h-4 w-4" />
            Update Password
          </Button>
        </div>
      </form>
    </Card>
  );
};
