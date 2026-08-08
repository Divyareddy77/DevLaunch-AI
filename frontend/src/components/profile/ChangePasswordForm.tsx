/**
 * ChangePasswordForm — form for changing the authenticated user's
 * password.
 *
 * Uses React Hook Form + Zod with constraints mirroring the backend
 * ChangePasswordRequest DTO (new password 8–100 characters), plus a
 * confirmation field validated on the client. Includes show/hide
 * password toggles and a live strength meter with a rule checklist
 * that updates while typing. The form resets itself after a
 * successful submission.
 *
 * @see backend/src/main/java/com/devlaunch/dto/request/ChangePasswordRequest.java
 * @author DevLaunch
 */

import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { KeyRound, Lock } from 'lucide-react';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import { EyeToggle } from '../ui/EyeToggle';
import { PasswordStrengthMeter } from '../ui/PasswordStrengthMeter';

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

/** Shared label styling for prominent form labels. */
const LABEL_CLASSES = 'text-sm font-semibold text-gray-800';

export const ChangePasswordForm: React.FC<ChangePasswordFormProps> = ({
  isSubmitting,
  onSubmit,
}) => {
  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors },
  } = useForm<ChangePasswordFormValues>({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: {
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
    },
  });

  // Per-field visibility toggles for the show/hide password controls.
  const [showCurrent, setShowCurrent] = useState(false);
  const [showNew, setShowNew] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const newPassword = watch('newPassword');

  const handleFormSubmit = async (values: ChangePasswordFormValues) => {
    const success = await onSubmit(values);
    if (success) {
      reset();
      setShowCurrent(false);
      setShowNew(false);
      setShowConfirm(false);
    }
  };

  return (
    <section className="overflow-hidden rounded-2xl border border-gray-100 bg-white shadow-[0_1px_3px_rgba(16,24,40,0.06)] transition-shadow duration-300 hover:shadow-[0_16px_40px_-12px_rgba(16,24,40,0.12)]">
      {/* Header */}
      <header className="flex items-center gap-3 border-b border-gray-100 px-6 py-5">
        <span className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-xl bg-indigo-50 text-indigo-600">
          <KeyRound className="h-5 w-5" />
        </span>
        <div className="min-w-0">
          <h3 className="text-base font-semibold text-gray-900">Change Password</h3>
          <p className="mt-0.5 text-xs text-gray-500">
            Keep your account secure with a strong password.
          </p>
        </div>
      </header>

      <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-5 p-6" noValidate>
        <Input
          label="Current Password"
          labelClassName={LABEL_CLASSES}
          type={showCurrent ? 'text' : 'password'}
          placeholder="Enter your current password"
          autoComplete="current-password"
          leftIcon={<Lock className="h-4 w-4" />}
          rightIcon={
            <EyeToggle
              visible={showCurrent}
              onToggle={() => setShowCurrent((v) => !v)}
              label={showCurrent ? 'Hide current password' : 'Show current password'}
            />
          }
          error={errors.currentPassword?.message}
          {...register('currentPassword')}
        />

        <div>
          <Input
            label="New Password"
            labelClassName={LABEL_CLASSES}
            type={showNew ? 'text' : 'password'}
            placeholder="At least 8 characters"
            autoComplete="new-password"
            leftIcon={<Lock className="h-4 w-4" />}
            rightIcon={
              <EyeToggle
                visible={showNew}
                onToggle={() => setShowNew((v) => !v)}
                label={showNew ? 'Hide new password' : 'Show new password'}
              />
            }
            error={errors.newPassword?.message}
            {...register('newPassword')}
          />
          {/* Live strength bar + rule checklist while typing. */}
          <PasswordStrengthMeter password={newPassword} />
        </div>

        <Input
          label="Confirm New Password"
          labelClassName={LABEL_CLASSES}
          type={showConfirm ? 'text' : 'password'}
          placeholder="Repeat your new password"
          autoComplete="new-password"
          leftIcon={<Lock className="h-4 w-4" />}
          rightIcon={
            <EyeToggle
              visible={showConfirm}
              onToggle={() => setShowConfirm((v) => !v)}
              label={showConfirm ? 'Hide confirmation' : 'Show confirmation'}
            />
          }
          error={errors.confirmPassword?.message}
          {...register('confirmPassword')}
        />

        <div className="flex justify-end border-t border-gray-100 pt-5">
          <Button type="submit" loading={isSubmitting} className="hover:-translate-y-0.5 hover:shadow-md">
            <KeyRound className="h-4 w-4" />
            Update Password
          </Button>
        </div>
      </form>
    </section>
  );
};
