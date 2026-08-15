import { useEffect, useState } from 'react';

/**
 * Animates a value from 0 up to `target` using requestAnimationFrame with
 * an ease-out cubic curve. Used for dashboard counter animations.
 *
 * @param target The value to count up to.
 * @param duration Animation duration in milliseconds (default 900).
 * @returns The current animated value (0 → target).
 */
export function useCountUp(target: number, duration = 900): number {
  const [value, setValue] = useState(0);

  useEffect(() => {
    let frame = 0;
    const start = performance.now();

    const tick = (now: number) => {
      const progress = Math.min(1, (now - start) / duration);
      const eased = 1 - Math.pow(1 - progress, 3);
      setValue(target * eased);
      if (progress < 1) {
        frame = requestAnimationFrame(tick);
      }
    };

    frame = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(frame);
  }, [target, duration]);

  return value;
}
