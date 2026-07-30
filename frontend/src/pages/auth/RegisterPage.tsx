/**
 * RegisterPage — user registration form.
 *
 * Accepts registration details, creates the account via AuthContext,
 * and redirects to the login page on success.
 *
 * @see backend/src/main/java/com/devlaunch/dto/request/RegisterRequest.java
 * @author DevLaunch
 */

import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { UserPlus, Mail, Lock, User, Phone } from 'lucide-react';
import toast from 'react-hot-toast';
import { useAuth } from '../../hooks/useAuth';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { ROUTES } from '../../constants/routes';
import { MESSAGES } from '../../constants/messages';

const registerSchema = z
  .object({
    firstName: z
      .string()
      .min(1, MESSAGES.REQUIRED_FIELD),
    lastName: z
      .string()
      .min(1, MESSAGES.REQUIRED_FIELD),
    email: z
      .string()
      .min(1, MESSAGES.REQUIRED_FIELD)
      .email(MESSAGES.INVALID_EMAIL),
    password: z
      .string()
      .min(8, MESSAGES.PASSWORD_MIN_LENGTH),
    confirmPassword: z
      .string()
      .min(1, MESSAGES.REQUIRED_FIELD),
    phone: z
      .string()
      .min(1, MESSAGES.REQUIRED_FIELD),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: MESSAGES.PASSWORDS_MUST_MATCH,
    path: ['confirmPassword'],
  });

type RegisterFormData = z.infer<typeof registerSchema>;

export const RegisterPage: React.FC = () => {
  const { register: registerUser, isSubmitting, clearError } = useAuth();
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      firstName: '',
      lastName: '',
      email: '',
      password: '',
      confirmPassword: '',
      phone: '',
    },
  });

  const onSubmit = async (data: RegisterFormData) => {
    clearError();

    const { confirmPassword: _, ...registerData } = data;
    const result = await registerUser(registerData);

    if (result.success) {
      toast.success(MESSAGES.REGISTER_SUCCESS);
      navigate(ROUTES.LOGIN);
    } else {
      toast.error(result.error ?? MESSAGES.REGISTER_ERROR);
    }
  };

  return (
    <div>
      <div className="mb-6 text-center">
        <h2 className="text-2xl font-bold text-gray-900">Create an account</h2>
        <p className="mt-1 text-sm text-gray-500">
          Join DevLaunch and supercharge your career journey
        </p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <div className="grid grid-cols-2 gap-4">
          <Input
            label="First Name"
            type="text"
            placeholder="John"
            leftIcon={<User className="h-4 w-4" />}
            error={errors.firstName?.message}
            autoComplete="given-name"
            {...register('firstName')}
          />
          <Input
            label="Last Name"
            type="text"
            placeholder="Doe"
            error={errors.lastName?.message}
            autoComplete="family-name"
            {...register('lastName')}
          />
        </div>

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
          label="Phone"
          type="tel"
          placeholder="+1 (555) 123-4567"
          leftIcon={<Phone className="h-4 w-4" />}
          error={errors.phone?.message}
          autoComplete="tel"
          {...register('phone')}
        />

        <Input
          label="Password"
          type="password"
          placeholder="At least 8 characters"
          leftIcon={<Lock className="h-4 w-4" />}
          error={errors.password?.message}
          autoComplete="new-password"
          {...register('password')}
        />

        <Input
          label="Confirm Password"
          type="password"
          placeholder="Repeat your password"
          leftIcon={<Lock className="h-4 w-4" />}
          error={errors.confirmPassword?.message}
          autoComplete="new-password"
          {...register('confirmPassword')}
        />

        <Button
          type="submit"
          fullWidth
          size="lg"
          loading={isSubmitting}
        >
          <UserPlus className="h-4 w-4" />
          Create Account
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-gray-500">
        Already have an account?{' '}
        <Link
          to={ROUTES.LOGIN}
          className="font-medium text-primary-600 hover:text-primary-700 transition-colors"
        >
          Sign in
        </Link>
      </p>
    </div>
  );
};
