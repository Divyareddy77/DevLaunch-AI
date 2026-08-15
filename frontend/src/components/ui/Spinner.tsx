/**
 * Spinner — a loading indicator with configurable size.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Loader2 } from 'lucide-react';

type SpinnerSize = 'sm' | 'md' | 'lg';

interface SpinnerProps {
  /** Size of the spinner. */
  size?: SpinnerSize;
  /** Optional label displayed below the spinner. */
  label?: string;
  /** Optional additional CSS classes. */
  className?: string;
}

const sizeMap: Record<SpinnerSize, { icon: number; text: string }> = {
  sm: { icon: 16, text: 'text-xs' },
  md: { icon: 24, text: 'text-sm' },
  lg: { icon: 32, text: 'text-base' },
};

export const Spinner: React.FC<SpinnerProps> = ({
  size = 'md',
  label,
  className = '',
}) => {
  const { icon, text } = sizeMap[size];

  return (
    <div className={`flex flex-col items-center justify-center gap-2 ${className}`}>
      <Loader2
        className="animate-spin text-primary-600"
        size={icon}
      />
      {label && <p className={`text-gray-500 ${text}`}>{label}</p>}
    </div>
  );
};
