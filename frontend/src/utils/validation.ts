/**
 * Client-side validation utility functions.
 *
 * Provides reusable validation helpers for common form fields.
 * These mirror the constraint rules defined on the backend DTOs
 * (e.g. @NotBlank, @Email, @Size). For complex forms, prefer
 * Zod schemas defined alongside the form component.
 *
 * @author DevLaunch
 */

import { z } from 'zod';
import { MESSAGES } from '../constants/messages';

/**
 * Validates that a string is a non-blank value.
 * Mirrors @NotBlank on backend DTOs.
 */
export function isNotBlank(value: string | null | undefined): boolean {
  return typeof value === 'string' && value.trim().length > 0;
}

/**
 * Validates an email address format.
 * Mirrors @Email on backend DTOs.
 */
export function isValidEmail(email: string | null | undefined): boolean {
  if (!email) return false;
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email.trim());
}

/**
 * Validates password minimum length only.
 * Mirrors the minimum-length constraint on backend RegisterRequest; the
 * full complexity policy is enforced by {@link passwordFieldSchema}.
 */
export function isValidPassword(password: string | null | undefined): boolean {
  return typeof password === 'string' && password.length >= 8;
}

/**
 * Validates that two password fields match.
 */
export function doPasswordsMatch(password: string, confirmPassword: string): boolean {
  return password === confirmPassword;
}

/**
 * Validates a URL string. Accepts both HTTP(S) and protocol-relative URLs.
 */
export function isValidUrl(url: string | null | undefined): boolean {
  if (!url) return true; // Optional fields are valid when empty
  try {
    const parsed = new URL(url);
    return parsed.protocol === 'http:' || parsed.protocol === 'https:';
  } catch {
    return false;
  }
}

/**
 * Validates a phone number (simple check for digits, spaces, +, -, (, )).
 * Backend expects a non-blank phone string.
 */
export function isValidPhone(phone: string | null | undefined): boolean {
  if (!phone) return false;
  const phoneRegex = /^[\d\s+\-()]{7,20}$/;
  return phoneRegex.test(phone.trim());
}

// ---------------------------------------------------------------------------
// Password strength
//
// Shared by the reset-password form (Zod schema) and the strength meter so
// the complexity rules are defined exactly once. These mirror the backend
// @Pattern rule on ResetPasswordRequest.
// ---------------------------------------------------------------------------

/** Individual password complexity rule. */
export interface PasswordCheck {
  /** Human-readable label for the rule. */
  label: string;
  /** Regex the password must satisfy. */
  pattern: RegExp;
  /** Whether the current password satisfies the rule. */
  met: boolean;
}

/** Minimum password length shared by the schema and the meter. */
export const PASSWORD_MIN_LENGTH = 8;

/** Maximum password length accepted by the backend DTOs. */
export const PASSWORD_MAX_LENGTH = 100;

/**
 * Named password complexity rules — the single source of truth used by
 * both the Zod schemas and the strength meter. Mirrors the backend
 * @Pattern rule on ResetPasswordRequest.
 */
export const PASSWORD_RULES = {
  MIN_LENGTH: PASSWORD_MIN_LENGTH,
  MAX_LENGTH: PASSWORD_MAX_LENGTH,
  UPPERCASE: /[A-Z]/,
  LOWERCASE: /[a-z]/,
  NUMBER: /\d/,
  SPECIAL: /[^A-Za-z0-9]/,
} as const;

/**
 * The complexity rules every new password must satisfy, with display
 * labels for the strength meter checklist.
 */
export const PASSWORD_CHECKS: PasswordCheck[] = [
  { label: 'At least 8 characters', pattern: /.{8,}/, met: false },
  { label: 'One uppercase letter', pattern: PASSWORD_RULES.UPPERCASE, met: false },
  { label: 'One lowercase letter', pattern: PASSWORD_RULES.LOWERCASE, met: false },
  { label: 'One number', pattern: PASSWORD_RULES.NUMBER, met: false },
  { label: 'One special character', pattern: PASSWORD_RULES.SPECIAL, met: false },
];

/**
 * Evaluates a password against the shared complexity rules.
 *
 * @param password the password to evaluate
 * @param getLabel optional label resolver so the meter can localise labels
 * @returns the satisfied checks and a 0–4 strength score
 */
export function evaluatePasswordStrength(
  password: string,
): { checks: PasswordCheck[]; score: number } {
  const checks = PASSWORD_CHECKS.map((rule) => ({
    ...rule,
    met: rule.pattern.test(password),
  }));
  const metCount = checks.filter((check) => check.met).length;

  // 0 rules → 0; 1–2 → 1 (weak); 3 → 2 (fair); 4 → 3 (good); 5 → 4 (strong).
  const score =
    metCount === 0 ? 0 : metCount <= 2 ? 1 : metCount === 3 ? 2 : metCount === 4 ? 3 : 4;
  return { checks, score };
}

/**
 * Zod schema for a password field enforcing the shared complexity rules.
 *
 * The single source of truth for password validation, used by both the
 * register and reset-password forms so the policy is defined exactly
 * once. Mirrors the backend @Pattern rule on RegisterRequest and
 * ResetPasswordRequest.
 */
export const passwordFieldSchema = () =>
  z
    .string()
    .min(1, MESSAGES.REQUIRED_FIELD)
    .min(PASSWORD_RULES.MIN_LENGTH, MESSAGES.PASSWORD_MIN_LENGTH)
    .max(PASSWORD_RULES.MAX_LENGTH, MESSAGES.PASSWORD_MAX_LENGTH)
    .regex(PASSWORD_RULES.UPPERCASE, MESSAGES.PASSWORD_UPPERCASE)
    .regex(PASSWORD_RULES.LOWERCASE, MESSAGES.PASSWORD_LOWERCASE)
    .regex(PASSWORD_RULES.NUMBER, MESSAGES.PASSWORD_NUMBER)
    .regex(PASSWORD_RULES.SPECIAL, MESSAGES.PASSWORD_SPECIAL);
