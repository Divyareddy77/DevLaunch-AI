import React from 'react';
import { useCountUp } from '../../hooks/useCountUp';

interface CountUpProps {
  /** The number to count up to. */
  value: number;
  /** Animation duration in milliseconds. */
  duration?: number;
  /** Text rendered before the number, e.g. '#' or '$'. */
  prefix?: string;
  /** Text rendered after the number, e.g. '%' or 'XP'. */
  suffix?: string;
  /** Number of decimal places to display. */
  decimals?: number;
  /** Optional additional CSS classes. */
  className?: string;
}

/**
 * Renders a number that animates from 0 to `value` on mount using
 * requestAnimationFrame with an ease-out curve.
 */
export const CountUp: React.FC<CountUpProps> = ({
  value,
  duration = 900,
  prefix = '',
  suffix = '',
  decimals = 0,
  className,
}) => {
  const display = useCountUp(value, duration);

  const formatted = display.toLocaleString(undefined, {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });

  return (
    <span className={className}>
      {prefix}
      {formatted}
      {suffix}
    </span>
  );
};
