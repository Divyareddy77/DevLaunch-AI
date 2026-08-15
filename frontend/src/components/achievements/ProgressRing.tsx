/**
 * ProgressRing — a circular SVG progress indicator.
 *
 * Used by the level card and the completion stat to visualize a
 * 0–100 percentage as an animated ring. The center is rendered by
 * the caller via the children prop.
 *
 * @author DevLaunch
 */

import React from 'react';

interface ProgressRingProps {
  /** Progress percentage (0–100). */
  percent: number;
  /** Ring size in pixels. */
  size?: number;
  /** Stroke width in pixels. */
  strokeWidth?: number;
  /** Ring colour. */
  color?: string;
  /** Optional gradient endpoint colour for a two-tone sweep. */
  colorTo?: string;
  /** The content rendered inside the ring. */
  children?: React.ReactNode;
}

export const ProgressRing: React.FC<ProgressRingProps> = ({
  percent,
  size = 120,
  strokeWidth = 10,
  color = '#6366f1',
  colorTo,
  children,
}) => {
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const clamped = Math.min(100, Math.max(0, percent));
  const offset = circumference - (clamped / 100) * circumference;

  return (
    <div className="relative inline-flex items-center justify-center">
      <svg width={size} height={size} className="-rotate-90">
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke="#eef2ff"
          strokeWidth={strokeWidth}
        />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke={colorTo ?? color}
          strokeWidth={strokeWidth}
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          className="transition-all duration-1000 ease-out"
        />
      </svg>
      <div className="absolute inset-0 flex items-center justify-center">{children}</div>
    </div>
  );
};
