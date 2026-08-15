/**
 * ForgotPasswordPage — requests a password reset link.
 *
 * Collects the user's email, calls POST /api/auth/forgot-password, and
 * always shows the same generic confirmation (the backend never reveals
 * whether the account exists).
 *
 * @see backend/src/main/java/com/devlaunch/controller/AuthController.java
 * @author DevLaunch
 */

import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Mail, Send, CheckCircle2, ArrowLeft } from 'lucide-react';
import toast from 'react-hot-toast';
import { authService } from '../../services/auth.service';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { ROUTES } from '../../constants/routes';
import { MESSAGES } from '../../constants/messages';
import axios from 'axios';

const forgotPasswordSchema = z.object({
  email: z
    .string()
    .min(1, MESSAGES.REQUIRED_FIELD)
    .email(MESSAGES.INVALID_EMAIL),
});

type ForgotPasswordFormData = z.infer<typeof forgotPasswordSchema>;

export const ForgotPasswordPage: React.FC = () => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ForgotPasswordFormData>({
    resolver: zodResolver(forgotPasswordSchema),
    defaultValues: { email: '' },
  });

  const onSubmit = async (data: ForgotPasswordFormData) => {
    setIsSubmitting(true);
    try {
      await authService.forgotPassword(data);
      // The response is identical for registered and unregistered emails;
      // always present the same confirmation screen.
      setSubmitted(true);
    } catch (err: unknown) {
      const message = axios.isAxiosError(err)
        ? (err.response?.data as { message?: string })?.message ?? MESSAGES.FORGOT_PASSWORD_ERROR
        : MESSAGES.FORGOT_PASSWORD_ERROR;
      toast.error(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (submitted) {
    return (
      <div className="text-center">
        <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-emerald-100">
          <CheckCircle2 className="h-7 w-7 text-emerald-600" />
        </div>
        <h2 className="text-2xl font-bold text-gray-900">Check your email</h2>
        <p className="mt-2 text-sm leading-relaxed text-gray-500">
          {MESSAGES.FORGOT_PASSWORD_SENT}
        </p>
        <Button
          className="mt-6"
          fullWidth
          size="lg"
          onClick={() => setSubmitted(false)}
        >
          <Send className="h-4 w-4" />
          Send Another Link
        </Button>
        <p className="mt-4 text-sm text-gray-500">
          Remembered your password?{' '}
          <Link
            to={ROUTES.LOGIN}
            className="font-medium text-primary-600 hover:text-primary-700 transition-colors"
          >
            Sign in
          </Link>
        </p>
      </div>
    );
  }

  return (
    <div>
      <div className="mb-6 text-center">
        <h2 className="text-2xl font-bold text-gray-900">
          {MESSAGES.FORGOT_PASSWORD_TITLE}
        </h2>
        <p className="mt-1 text-sm text-gray-500">
          {MESSAGES.FORGOT_PASSWORD_SUBTITLE}
        </p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <Input
          label="Email"
          type="email"
          placeholder="you@example.com"
          leftIcon={<Mail className="h-4 w-4" />}
          error={errors.email?.message}
          autoComplete="email"
          {...register('email')}
        />

        <Button type="submit" fullWidth size="lg" loading={isSubmitting}>
          <Send className="h-4 w-4" />
          {MESSAGES.SEND_RESET_LINK}
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
