/**
 * ResetPasswordPage — completes a password reset with the one-time token
 * from the emailed link.
 *
 * Reads the token from the URL query string, validates the new password
 * against the shared complexity rules, submits POST /api/auth/reset-password,
 * and shows a success screen with a link back to login. Handles invalid,
 * expired, and already-used tokens.
 *
 * @see backend/src/main/java/com/devlaunch/controller/AuthController.java
 * @author DevLaunch
 */

import React, { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Lock, KeyRound, CheckCircle2, ShieldAlert, ArrowLeft } from 'lucide-react';
import toast from 'react-hot-toast';
import axios from 'axios';
import { authService } from '../../services/auth.service';
import { Button } from '../../components/ui/Button';
import { EyeToggle } from '../../components/ui/EyeToggle';
import { Input } from '../../components/ui/Input';
import { PasswordStrengthMeter } from '../../components/ui/PasswordStrengthMeter';
import { ROUTES } from '../../constants/routes';
import { MESSAGES } from '../../constants/messages';
import { passwordFieldSchema } from '../../utils/validation';

const resetPasswordSchema = z
  .object({
    newPassword: passwordFieldSchema(),
    confirmPassword: z.string().min(1, MESSAGES.REQUIRED_FIELD),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: MESSAGES.PASSWORDS_MUST_MATCH,
    path: ['confirmPassword'],
  });

type ResetPasswordFormData = z.infer<typeof resetPasswordSchema>;

export const ResetPasswordPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') ?? '';

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [resetComplete, setResetComplete] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<ResetPasswordFormData>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: {
      newPassword: '',
      confirmPassword: '',
    },
  });

  const newPassword = watch('newPassword');

  const onSubmit = async (data: ResetPasswordFormData) => {
    setIsSubmitting(true);
    setSubmitError(null);
    try {
      await authService.resetPassword({ token, ...data });
      setResetComplete(true);
    } catch (err: unknown) {
      const message = axios.isAxiosError(err)
        ? (err.response?.data as { message?: string })?.message ?? MESSAGES.RESET_PASSWORD_ERROR
        : MESSAGES.RESET_PASSWORD_ERROR;
      setSubmitError(message);
      toast.error(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  // No token in the URL — the emailed link is malformed or was opened directly.
  if (!token) {
    return (
      <div className="text-center">
        <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-red-100">
          <ShieldAlert className="h-7 w-7 text-red-600" />
        </div>
        <h2 className="text-2xl font-bold text-gray-900">
          {MESSAGES.RESET_PASSWORD_LINK_INVALID}
        </h2>
        <p className="mt-2 text-sm leading-relaxed text-gray-500">
          {MESSAGES.RESET_PASSWORD_LINK_INVALID_SUBTITLE}
        </p>
        <div className="mt-6 space-y-3">
          <Button fullWidth size="lg" onClick={() => navigate(ROUTES.LOGIN)}>
            {MESSAGES.GO_TO_LOGIN}
          </Button>
          <Link
            to={ROUTES.FORGOT_PASSWORD}
            className="block text-sm font-medium text-primary-600 hover:text-primary-700 transition-colors"
          >
            {MESSAGES.REQUEST_NEW_LINK}
          </Link>
        </div>
      </div>
    );
  }

  if (resetComplete) {
    return (
      <div className="text-center">
        <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-emerald-100">
          <CheckCircle2 className="h-7 w-7 text-emerald-600" />
        </div>
        <h2 className="text-2xl font-bold text-gray-900">
          {MESSAGES.RESET_PASSWORD_SUCCESS_TITLE}
        </h2>
        <p className="mt-2 text-sm text-gray-500">
          {MESSAGES.RESET_PASSWORD_SUCCESS_SUBTITLE}
        </p>
        <Button
          className="mt-6"
          fullWidth
          size="lg"
          onClick={() => navigate(ROUTES.LOGIN)}
        >
          <KeyRound className="h-4 w-4" />
          {MESSAGES.GO_TO_LOGIN}
        </Button>
      </div>
    );
  }

  return (
    <div>
      <div className="mb-6 text-center">
        <h2 className="text-2xl font-bold text-gray-900">
          {MESSAGES.RESET_PASSWORD_TITLE}
        </h2>
        <p className="mt-1 text-sm text-gray-500">
          {MESSAGES.RESET_PASSWORD_SUBTITLE}
        </p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <div>
          <Input
            label="New Password"
            type={showPassword ? 'text' : 'password'}
            placeholder="At least 8 characters"
            leftIcon={<Lock className="h-4 w-4" />}
            rightIcon={
              <EyeToggle
                visible={showPassword}
                onToggle={() => setShowPassword((prev) => !prev)}
                label={showPassword ? 'Hide new password' : 'Show new password'}
              />
            }
            error={errors.newPassword?.message}
            autoComplete="new-password"
            {...register('newPassword')}
          />
          <PasswordStrengthMeter password={newPassword} />
        </div>

        <Input
          label="Confirm New Password"
          type={showConfirmPassword ? 'text' : 'password'}
          placeholder="Repeat your new password"
          leftIcon={<Lock className="h-4 w-4" />}
          rightIcon={
            <EyeToggle
              visible={showConfirmPassword}
              onToggle={() => setShowConfirmPassword((prev) => !prev)}
              label={showConfirmPassword ? 'Hide confirm password' : 'Show confirm password'}
            />
          }
          error={errors.confirmPassword?.message}
          autoComplete="new-password"
          {...register('confirmPassword')}
        />

        {submitError && (
          <div className="rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700" role="alert">
            <p>{submitError}</p>
            <Link
              to={ROUTES.FORGOT_PASSWORD}
              className="mt-1 inline-block font-medium text-red-700 underline hover:text-red-800 transition-colors"
            >
              {MESSAGES.REQUEST_NEW_LINK}
            </Link>
          </div>
        )}

        <Button type="submit" fullWidth size="lg" loading={isSubmitting}>
          <KeyRound className="h-4 w-4" />
          Update Password
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-gray-500">
        <Link
          to={ROUTES.LOGIN}
          className="inline-flex items-center gap-1 font-medium text-primary-600 hover:text-primary-700 transition-colors"
        >
          <ArrowLeft className="h-3.5 w-3.5" />
          Back to Sign In
        </Link>
      </p>
    </div>
  );
};
