/**
 * EyeToggle — password visibility toggle button.
 *
 * Renders a clickable eye icon that switches a password field between
 * hidden and visible values. Shared by the auth forms (register, reset
 * password) so the toggle behaviour and styling stay consistent.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Eye, EyeOff } from 'lucide-react';

interface EyeToggleProps {
  /** Whether the field value is currently visible. */
  visible: boolean;
  /** Callback fired when the toggle is clicked. */
  onToggle: () => void;
  /** Accessible label for the button. */
  label: string;
}

export const EyeToggle: React.FC<EyeToggleProps> = ({ visible, onToggle, label }) => (
  <button
    type="button"
    onClick={onToggle}
    aria-label={label}
    className="rounded p-0.5 text-gray-400 transition-colors hover:text-gray-600"
  >
    {visible ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
  </button>
);
