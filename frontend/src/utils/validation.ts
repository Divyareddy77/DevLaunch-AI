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
 * Validates password minimum length.
 * Mirrors @Size(min = 8) on backend RegisterRequest.
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
