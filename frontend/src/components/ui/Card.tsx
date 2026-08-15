/**
 * Card — a versatile content container with rounded corners,
 * shadow, and optional header/footer slots.
 *
 * @author DevLaunch
 */

import React, { type ReactNode } from 'react';

interface CardProps {
  /** Optional header rendered inside the card above the body. */
  header?: ReactNode;
  /** Main card content. */
  children: ReactNode;
  /** Optional footer rendered below the body. */
  footer?: ReactNode;
  /** Whether the card should show a padding around its content. */
  padded?: boolean;
  /** Optional additional CSS classes. */
  className?: string;
}

export const Card: React.FC<CardProps> = ({
  header,
  children,
  footer,
  padded = true,
  className = '',
}) => {
  return (
    <div className={`rounded-xl border border-gray-200 bg-white shadow-sm ${className}`}>
      {header && (
        <div className="border-b border-gray-100 px-5 py-4">
          {header}
        </div>
      )}

      <div className={padded ? 'px-5 py-4' : ''}>{children}</div>

      {footer && (
        <div className="border-t border-gray-100 px-5 py-3">
          {footer}
        </div>
      )}
    </div>
  );
};
