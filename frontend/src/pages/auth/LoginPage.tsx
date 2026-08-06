/**
 * LoginPage — user login form.
 *
 * Accepts email and password, authenticates via AuthContext,
 * and redirects to the dashboard on success.
 *
 * @see backend/src/main/java/com/devlaunch/dto/request/LoginRequest.java
 * @author DevLaunch
 */

import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { LogIn, Mail, Lock } from 'lucide-react';
import toast from 'react-hot-toast';
import { useAuth } from '../../hooks/useAuth';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { ROUTES } from '../../constants/routes';
import { MESSAGES } from '../../constants/messages';

const loginSchema = z.object({
  email: z
    .string()
    .min(1, MESSAGES.REQUIRED_FIELD)
    .email(MESSAGES.INVALID_EMAIL),
  password: z
    .string()
    .min(1, MESSAGES.REQUIRED_FIELD),
});

type LoginFormData = z.infer<typeof loginSchema>;

interface LocationState {
  from?: { pathname: string };
}

export const LoginPage: React.FC = () => {
  const { login, isSubmitting, clearError } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const locationState = location.state as LocationState | null;

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  });

  const onSubmit = async (data: LoginFormData) => {
    clearError();
    const result = await login(data);

    if (result.success) {
      toast.success(MESSAGES.LOGIN_SUCCESS);
      // Redirect to the page the user was trying to access, or dashboard
      const from = locationState?.from?.pathname ?? ROUTES.DASHBOARD;
      navigate(from, { replace: true });
    } else {
      toast.error(result.error ?? MESSAGES.LOGIN_ERROR);
    }
  };

  return (
    <div>
      <div className="mb-6 text-center">
        <h2 className="text-2xl font-bold text-gray-900">Welcome back</h2>
        <p className="mt-1 text-sm text-gray-500">
          Sign in to your account to continue
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

        <Input
          label="Password"
          type="password"
          placeholder="Enter your password"
          leftIcon={<Lock className="h-4 w-4" />}
          error={errors.password?.message}
          autoComplete="current-password"
          {...register('password')}
        />

        <div className="flex justify-end">
          <Link
            to={ROUTES.FORGOT_PASSWORD}
            className="text-xs font-medium text-primary-600 hover:text-primary-700 transition-colors"
          >
            Forgot password?
          </Link>
        </div>

        <Button
          type="submit"
          fullWidth
          size="lg"
          loading={isSubmitting}
        >
          <LogIn className="h-4 w-4" />
          Sign In
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-gray-500">
        Don&apos;t have an account?{' '}
        <Link
          to={ROUTES.REGISTER}
          className="font-medium text-primary-600 hover:text-primary-700 transition-colors"
        >
          Create one
        </Link>
      </p>
    </div>
  );
};
