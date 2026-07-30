/**
 * ErrorMessage — displays an error alert with an optional
 * retry action.
 *
 * @author DevLaunch
 */

import React from 'react';
import { AlertCircle, RefreshCw } from 'lucide-react';
import { Button } from '../ui/Button';

interface ErrorMessageProps {
  /** The error message to display. */
  message: string;
  /** Optional callback to retry the failed operation. */
  onRetry?: () => void;
  /** Optional additional CSS classes. */
  className?: string;
}

export const ErrorMessage: React.FC<ErrorMessageProps> = ({
  message,
  onRetry,
  className = '',
}) => {
  return (
    <div
      className={`flex flex-col items-center justify-center rounded-lg border border-red-200 bg-red-50 p-6 text-center ${className}`}
    >
      <AlertCircle className="mb-2 h-8 w-8 text-red-400" />
      <p className="mb-3 text-sm text-red-600">{message}</p>
      {onRetry && (
        <Button variant="outline" size="sm" onClick={onRetry}>
          <RefreshCw className="h-3.5 w-3.5" />
          Try Again
        </Button>
      )}
    </div>
  );
};
