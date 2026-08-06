/**
 * Input — a reusable form input component with label, error
 * display, and optional icon.
 *
 * Supports React Hook Form's register props via forwarded ref.
 *
 * @author DevLaunch
 */

import { type InputHTMLAttributes, forwardRef, type ReactNode } from 'react';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  /** Label displayed above the input field. */
  label?: string;
  /** Error message displayed below the input field. */
  error?: string;
  /** Optional icon displayed inside the input on the left side. */
  leftIcon?: ReactNode;
  /**
   * Optional element rendered inside the input on the right side
   * (e.g. a password visibility toggle button). Interactive elements
   * receive pointer events; pass a button with {@code type="button"}.
   */
  rightIcon?: ReactNode;
  /** Hint text displayed below the input field. */
  hint?: string;
  /** Makes the input span full width. */
  fullWidth?: boolean;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, leftIcon, rightIcon, hint, fullWidth = true, className = '', id, ...props }, ref) => {
    const inputId = id ?? label?.toLowerCase().replace(/\s+/g, '-');

    return (
      <div className={fullWidth ? 'w-full' : ''}>
        {label && (
          <label
            htmlFor={inputId}
            className="mb-1.5 block text-sm font-medium text-gray-700"
          >
            {label}
          </label>
        )}

        <div className="relative">
          {leftIcon && (
            <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-gray-400">
              {leftIcon}
            </div>
          )}

          <input
            ref={ref}
            id={inputId}
            className={`
              block w-full rounded-lg border bg-white px-3 py-2 text-sm text-gray-900
              placeholder-gray-400 transition-colors
              focus:outline-none focus:ring-2 focus:ring-offset-0
              ${
                error
                  ? 'border-red-300 focus:border-red-500 focus:ring-red-500'
                  : 'border-gray-300 focus:border-primary-500 focus:ring-primary-500'
              }
              ${leftIcon ? 'pl-10' : ''}
              ${rightIcon ? 'pr-10' : ''}
              ${className}
            `}
            aria-invalid={error ? 'true' : 'false'}
            aria-describedby={error ? `${inputId}-error` : undefined}
            {...props}
          />

          {rightIcon && (
            <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-3 text-gray-400">
              <span className="pointer-events-auto">{rightIcon}</span>
            </div>
          )}
        </div>

        {error && (
          <p id={`${inputId}-error`} className="mt-1.5 text-xs text-red-500" role="alert">
            {error}
          </p>
        )}

        {hint && !error && (
          <p className="mt-1.5 text-xs text-gray-400">{hint}</p>
        )}
      </div>
    );
  },
);

Input.displayName = 'Input';
