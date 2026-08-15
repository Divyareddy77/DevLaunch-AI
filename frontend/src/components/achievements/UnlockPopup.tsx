/**
 * UnlockPopup — a celebration modal shown when a badge unlocks.
 *
 * Renders a confetti burst, a large badge tile, the badge title, the XP
 * reward, and a "Keep going" note. Dismissible via the close button, a
 * click on the backdrop, or automatically after a few seconds.
 *
 * @author DevLaunch
 */

import React, { useEffect } from 'react';
import { Sparkles } from 'lucide-react';
import { ACHIEVEMENT_CATEGORY_LABELS, type UnlockedAchievement } from '../../types/achievement';
import { Confetti } from './Confetti';

interface UnlockPopupProps {
  achievement: UnlockedAchievement;
  onClose: () => void;
}

export const UnlockPopup: React.FC<UnlockPopupProps> = ({ achievement, onClose }) => {
  // Auto-dismiss after 5 seconds.
  useEffect(() => {
    const timer = window.setTimeout(onClose, 5000);
    return () => window.clearTimeout(timer);
  }, [onClose]);

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-gray-900/60 p-4 backdrop-blur-sm"
      onClick={onClose}
    >
      <Confetti count={110} />

      <div
        className="relative w-full max-w-sm overflow-hidden rounded-3xl bg-white p-8 text-center shadow-2xl animate-pop-in"
        onClick={(event) => event.stopPropagation()}
      >
        {/* Glow behind the badge */}
        <div
          className="pointer-events-none absolute left-1/2 top-10 h-40 w-40 -translate-x-1/2 rounded-full opacity-25 blur-3xl"
          style={{ backgroundColor: achievement.color }}
        />

        <div className="relative">
          <span className="mb-2 inline-flex items-center gap-1.5 rounded-full bg-amber-100 px-3 py-1 text-[11px] font-bold uppercase tracking-widest text-amber-700">
            <Sparkles className="h-3.5 w-3.5" />
            Achievement Unlocked
          </span>

          <div
            className="mx-auto mt-5 flex h-24 w-24 items-center justify-center rounded-3xl text-5xl animate-badge-glow"
            style={{ backgroundColor: `${achievement.color}1a` }}
          >
            <span aria-hidden="true">{achievement.icon}</span>
          </div>

          <h2 className="mt-5 text-2xl font-bold text-gray-900">{achievement.title}</h2>
          <p className="mt-1 text-sm text-gray-500">
            {ACHIEVEMENT_CATEGORY_LABELS[achievement.category]} · {achievement.description}
          </p>

          <div
            className="mt-5 inline-flex items-center gap-1.5 rounded-2xl px-5 py-2.5 text-lg font-bold"
            style={{ backgroundColor: `${achievement.color}1a`, color: achievement.color }}
          >
            +{achievement.xpReward} XP
          </div>

          <p className="mt-4 text-xs text-gray-400">Keep going — more badges await!</p>

          <button
            onClick={onClose}
            className="mt-6 w-full rounded-xl bg-gray-900 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-gray-800"
          >
            Awesome, thanks!
          </button>
        </div>
      </div>
    </div>
  );
};
