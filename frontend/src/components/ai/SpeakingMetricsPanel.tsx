/**
 * SpeakingMetricsPanel — the live speaking analysis shown while the user
 * answers by voice.
 *
 * Displays speaking pace (WPM), filler-word usage, long pauses, speaking
 * duration, and a confidence estimate derived from the live transcript.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Gauge, AlertTriangle, Timer, Activity, Mic } from 'lucide-react';
import type { SpeakingMetrics } from '../../utils/speaking';
import { formatDuration } from '../../utils/format';

interface SpeakingMetricsPanelProps {
  /** The live speaking metrics. */
  metrics: SpeakingMetrics;
}

export const SpeakingMetricsPanel: React.FC<SpeakingMetricsPanelProps> = ({ metrics }) => {
  const paceTone =
    metrics.wpm === 0
      ? 'text-gray-500'
      : metrics.wpm >= 85 && metrics.wpm <= 195
        ? 'text-emerald-600'
        : 'text-amber-600';

  const fillerTone =
    metrics.fillerWordCount === 0
      ? 'text-gray-500'
      : metrics.fillerWordCount <= 3
        ? 'text-amber-600'
        : 'text-red-500';

  return (
    <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
      {/* Pace */}
      <div className="rounded-lg border border-gray-100 bg-gray-50/60 p-2.5">
        <div className="flex items-center gap-1.5 text-xs font-medium text-gray-500">
          <Gauge className="h-3.5 w-3.5" />
          Pace
        </div>
        <p className={`mt-1 text-sm font-semibold ${paceTone}`}>
          {metrics.wpm > 0 ? `${metrics.wpm} wpm` : '—'}
        </p>
        <p className="text-[11px] text-gray-400">{metrics.paceLabel}</p>
      </div>

      {/* Filler words */}
      <div className="rounded-lg border border-gray-100 bg-gray-50/60 p-2.5">
        <div className="flex items-center gap-1.5 text-xs font-medium text-gray-500">
          <AlertTriangle className="h-3.5 w-3.5" />
          Fillers
        </div>
        <p className={`mt-1 text-sm font-semibold ${fillerTone}`}>
          {metrics.fillerWordCount > 0 ? `${metrics.fillerWordCount} found` : 'None'}
        </p>
        <p className="truncate text-[11px] text-gray-400">
          {metrics.fillerWords.length > 0
            ? metrics.fillerWords.slice(0, 4).join(', ')
            : 'um, uh, like…'}
        </p>
      </div>

      {/* Long pauses */}
      <div className="rounded-lg border border-gray-100 bg-gray-50/60 p-2.5">
        <div className="flex items-center gap-1.5 text-xs font-medium text-gray-500">
          <Timer className="h-3.5 w-3.5" />
          Long pauses
        </div>
        <p className="mt-1 text-sm font-semibold text-gray-700">
          {metrics.longPauses > 0 ? metrics.longPauses : 'None'}
        </p>
        <p className="text-[11px] text-gray-400">over 2.5s</p>
      </div>

      {/* Confidence */}
      <div className="rounded-lg border border-gray-100 bg-gray-50/60 p-2.5">
        <div className="flex items-center gap-1.5 text-xs font-medium text-gray-500">
          <Activity className="h-3.5 w-3.5" />
          Confidence
        </div>
        <p className="mt-1 text-sm font-semibold text-gray-700">{metrics.confidence}/100</p>
        <div className="mt-1 h-1.5 w-full overflow-hidden rounded-full bg-gray-200">
          <div
            className={`h-full rounded-full transition-all duration-500 ${
              metrics.confidence >= 70
                ? 'bg-emerald-500'
                : metrics.confidence >= 45
                  ? 'bg-amber-500'
                  : 'bg-red-500'
            }`}
            style={{ width: `${metrics.confidence}%` }}
          />
        </div>
      </div>

      {/* Speaking duration (full width hint) */}
      <div className="col-span-2 flex items-center gap-1.5 text-[11px] text-gray-400 sm:col-span-4">
        <Mic className="h-3 w-3" />
        Speaking time {formatDuration(metrics.speakingSeconds)} · {metrics.wordCount} words ·{' '}
        {metrics.sentenceCount} sentence{metrics.sentenceCount === 1 ? '' : 's'}
      </div>
    </div>
  );
};
