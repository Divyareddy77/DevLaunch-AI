/**
 * WebcamOverlay — the small floating camera preview shown during a mock
 * interview.
 *
 * Displays the optional video feed with a recording indicator, the
 * interview timer, and the camera / microphone status. The camera is
 * never required: when the stream is unavailable the overlay hides or
 * shows the status reason and the interview continues normally.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Video, VideoOff, Mic, MicOff } from 'lucide-react';
import type { WebcamStatus } from '../../hooks/useWebcam';
import { formatClock } from '../../utils/format';

interface WebcamOverlayProps {
  /** The active video stream, or null. */
  stream: MediaStream | null;
  /** The camera lifecycle status. */
  status: WebcamStatus;
  /** Whether the interview is currently recording (voice active). */
  recording: boolean;
  /** Whether voice mode is enabled. */
  microphoneEnabled: boolean;
  /** The elapsed interview time in seconds. */
  elapsedSeconds: number;
}

export const WebcamOverlay: React.FC<WebcamOverlayProps> = ({
  stream,
  status,
  recording,
  microphoneEnabled,
  elapsedSeconds,
}) => {
  const videoRef = React.useRef<HTMLVideoElement | null>(null);

  React.useEffect(() => {
    if (videoRef.current && stream) {
      videoRef.current.srcObject = stream;
    }
  }, [stream]);

  if (status === 'off') {
    return null;
  }

  const cameraOn = status === 'on';

  return (
    <div className="pointer-events-none fixed bottom-5 right-5 z-40 w-44 overflow-hidden rounded-xl border border-gray-200 bg-gray-900 shadow-lg">
      {/* Video feed */}
      <div className="relative aspect-video bg-gray-950">
        {cameraOn && stream ? (
          <video
            ref={videoRef}
            autoPlay
            muted
            playsInline
            className="h-full w-full object-cover"
          />
        ) : (
          <div className="flex h-full w-full flex-col items-center justify-center gap-1 text-gray-400">
            <VideoOff className="h-5 w-5" />
            <span className="text-[10px]">Camera off</span>
          </div>
        )}

        {/* Recording indicator */}
        {recording && (
          <span className="absolute left-2 top-2 inline-flex items-center gap-1.5 rounded-full bg-red-600 px-2 py-0.5 text-[10px] font-semibold text-white">
            <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-white" />
            REC
          </span>
        )}

        {/* Timer */}
        <span className="absolute right-2 top-2 rounded-md bg-black/60 px-1.5 py-0.5 font-mono text-[11px] font-semibold text-white">
          {formatClock(elapsedSeconds)}
        </span>
      </div>

      {/* Status bar */}
      <div className="flex items-center justify-between bg-gray-800 px-2.5 py-1.5">
        <span
          className={`inline-flex items-center gap-1 text-[10px] font-medium ${
            cameraOn ? 'text-emerald-400' : 'text-gray-400'
          }`}
          title={cameraOn ? 'Camera on' : 'Camera unavailable'}
        >
          {cameraOn ? <Video className="h-3 w-3" /> : <VideoOff className="h-3 w-3" />}
          {cameraOn ? 'On' : 'Off'}
        </span>
        <span
          className={`inline-flex items-center gap-1 text-[10px] font-medium ${
            microphoneEnabled ? 'text-emerald-400' : 'text-gray-400'
          }`}
          title={microphoneEnabled ? 'Microphone enabled' : 'Microphone off'}
        >
          {microphoneEnabled ? <Mic className="h-3 w-3" /> : <MicOff className="h-3 w-3" />}
          {microphoneEnabled ? 'On' : 'Off'}
        </span>
      </div>
    </div>
  );
};
