/**
 * Button — a reusable button component with variants, sizes,
 * and loading support.
 *
 * @author DevLaunch
 */

import { type ButtonHTMLAttributes, forwardRef, useState } from 'react';
import { Loader2 } from 'lucide-react';

/** A single ripple origin, keyed by a unique id. */
interface Ripple {
  id: number;
  x: number;
  y: number;
}

type ButtonVariant = 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger';
type ButtonSize = 'sm' | 'md' | 'lg';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  /** Visual style variant. */
  variant?: ButtonVariant;
  /** Button size. */
  size?: ButtonSize;
  /** Shows a loading spinner and disables the button when true. */
  loading?: boolean;
  /** Makes the button span the full width of its container. */
  fullWidth?: boolean;
}

const variantStyles: Record<ButtonVariant, string> = {
  primary:
    'bg-primary-600 text-white hover:bg-primary-700 focus:ring-primary-500 shadow-sm',
  secondary:
    'bg-gray-100 text-gray-700 hover:bg-gray-200 focus:ring-gray-400',
  outline:
    'border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 focus:ring-primary-500',
  ghost:
    'text-gray-600 hover:bg-gray-100 hover:text-gray-900 focus:ring-gray-400',
  danger:
    'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500 shadow-sm',
};

const sizeStyles: Record<ButtonSize, string> = {
  sm: 'px-3 py-1.5 text-xs',
  md: 'px-4 py-2 text-sm',
  lg: 'px-6 py-3 text-base',
};

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      variant = 'primary',
      size = 'md',
      loading = false,
      fullWidth = false,
      disabled,
      children,
      className = '',
      ...props
    },
    ref,
  ) => {
    const isDisabled = disabled || loading;
    const [ripples, setRipples] = useState<Ripple[]>([]);

    const spawnRipple = (event: React.PointerEvent<HTMLButtonElement>) => {
      if (isDisabled) return;
      const rect = event.currentTarget.getBoundingClientRect();
      const id = Date.now() + Math.random();
      setRipples((prev) => [
        ...prev,
        { id, x: event.clientX - rect.left, y: event.clientY - rect.top },
      ]);
      // Clean up after the ripple animation completes.
      window.setTimeout(() => {
        setRipples((prev) => prev.filter((r) => r.id !== id));
      }, 650);
    };

    return (
      <button
        ref={ref}
        disabled={isDisabled}
        onPointerDown={spawnRipple}
        className={`
          relative inline-flex items-center justify-center gap-2 overflow-hidden rounded-lg font-medium
          transition-all duration-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 focus-visible:ring-offset-2
          active:scale-[0.98]
          disabled:cursor-not-allowed disabled:opacity-50
          ${variantStyles[variant]}
          ${sizeStyles[size]}
          ${fullWidth ? 'w-full' : ''}
          ${className}
        `}
        {...props}
      >
        {ripples.map((ripple) => (
          <span
            key={ripple.id}
            className="ripple"
            style={{ left: ripple.x, top: ripple.y }}
            aria-hidden="true"
          />
        ))}
        {loading && <Loader2 className="h-4 w-4 animate-spin" />}
        {children}
      </button>
    );
  },
);

Button.displayName = 'Button';
