/**
 * Confetti — a lightweight, dependency-free confetti burst.
 *
 * Renders a fixed number of coloured pieces that fall from the top of
 * the viewport with a rotation animation. Used by the unlock popup to
 * celebrate a freshly unlocked badge. Auto-removes after the animation.
 *
 * @author DevLaunch
 */

import React, { useMemo } from 'react';

interface ConfettiProps {
  /** Number of confetti pieces to render. */
  count?: number;
}

const CONFETTI_COLORS = [
  '#6366f1',
  '#f59e0b',
  '#10b981',
  '#ef4444',
  '#8b5cf6',
  '#06b6d4',
  '#f97316',
  '#ec4899',
];

interface Piece {
  id: number;
  left: number;
  delay: number;
  duration: number;
  color: string;
  width: number;
  height: number;
  rotate: number;
}

export const Confetti: React.FC<ConfettiProps> = ({ count = 90 }) => {
  const pieces = useMemo<Piece[]>(
    () =>
      Array.from({ length: count }, (_, index) => ({
        id: index,
        left: Math.random() * 100,
        delay: Math.random() * 0.4,
        duration: 2.4 + Math.random() * 1.6,
        color: CONFETTI_COLORS[index % CONFETTI_COLORS.length],
        width: 6 + Math.random() * 7,
        height: 8 + Math.random() * 8,
        rotate: Math.random() * 360,
      })),
    [count],
  );

  return (
    <>
      {pieces.map((piece) => (
        <span
          key={piece.id}
          className="confetti-piece"
          style={{
            left: `${piece.left}%`,
            width: piece.width,
            height: piece.height,
            backgroundColor: piece.color,
            transform: `rotate(${piece.rotate}deg)`,
            animationDelay: `${piece.delay}s`,
            animationDuration: `${piece.duration}s`,
          }}
        />
      ))}
    </>
  );
};
