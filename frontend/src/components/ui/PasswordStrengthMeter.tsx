/**
 * PasswordStrengthMeter — live password strength indicator.
 *
 * Evaluates a password against the shared complexity rules in
 * utils/validation.ts and renders a segmented bar with a label plus
 * the individual rule checklist. Reusable by any password form
 * (reset password, register, change password).
 *
 * @author DevLaunch
 */

import React from 'react';
import { Check, X } from 'lucide-react';
import { evaluatePasswordStrength } from '../../utils/validation';
import { MESSAGES } from '../../constants/messages';

interface PasswordStrengthMeterProps {
  /** The password to evaluate. */
  password: string;
}

/** Tailwind classes for each strength tier (bar colour + label colour). */
const TIER_STYLES: Record<number, { bar: string; text: string }> = {
  1: { bar: 'bg-red-500', text: 'text-red-600' },
  2: { bar: 'bg-amber-500', text: 'text-amber-600' },
  3: { bar: 'bg-lime-500', text: 'text-lime-600' },
  4: { bar: 'bg-emerald-500', text: 'text-emerald-600' },
};

export const PasswordStrengthMeter: React.FC<PasswordStrengthMeterProps> = ({ password }) => {
  const { checks, score } = evaluatePasswordStrength(password);
  const hasInput = password.length > 0;

  // Labels mirror the shared MESSAGES copy.
  const label =
    score === 1
      ? MESSAGES.PASSWORD_STRENGTH_WEAK
      : score === 2
        ? MESSAGES.PASSWORD_STRENGTH_FAIR
        : score === 3
          ? MESSAGES.PASSWORD_STRENGTH_GOOD
          : MESSAGES.PASSWORD_STRENGTH_STRONG;

  const tierStyle = score >= 1 ? TIER_STYLES[score] : null;

  return (
    <div aria-live="polite">
      {hasInput && (
        <>
          {/* Segmented bar: one segment per strength point (max 4). */}
          <div className="mt-2 flex items-center gap-1">
            {[1, 2, 3, 4].map((segment) => (
              <div
                key={segment}
                className={`h-1.5 flex-1 rounded-full transition-colors duration-300 ${
                  tierStyle && segment <= score ? tierStyle.bar : 'bg-gray-200'
                }`}
              />
            ))}
            <span
              className={`ml-2 text-xs font-medium ${tierStyle ? tierStyle.text : 'text-gray-400'}`}
            >
              {label}
            </span>
          </div>

          {/* Rule checklist. */}
          <ul className="mt-2 grid grid-cols-1 gap-1 sm:grid-cols-2">
            {checks.map((check) => (
              <li
                key={check.label}
                className={`flex items-center gap-1.5 text-xs ${
                  check.met ? 'text-emerald-600' : 'text-gray-400'
                }`}
              >
                {check.met ? (
                  <Check className="h-3 w-3 shrink-0" />
                ) : (
                  <X className="h-3 w-3 shrink-0" />
                )}
                {check.label}
              </li>
            ))}
          </ul>
        </>
      )}
    </div>
  );
};
