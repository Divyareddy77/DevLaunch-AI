/**
 * useWebcam — optional camera preview for mock interviews.
 *
 * The camera is never required: when permission is denied or the stream
 * cannot be started, the hook reports an error status and the interview
 * continues without video.
 *
 * @author DevLaunch
 */

import { useEffect, useRef, useState } from 'react';

/** The lifecycle status of the camera preview. */
export type WebcamStatus = 'off' | 'requesting' | 'on' | 'error';

export interface WebcamControls {
  /** The active video stream, or null when not available. */
  stream: MediaStream | null;
  /** The camera lifecycle status. */
  status: WebcamStatus;
  /** A human-readable error when permission was denied, else null. */
  error: string | null;
}

export function useWebcam(enabled: boolean): WebcamControls {
  const streamRef = useRef<MediaStream | null>(null);
  const [stream, setStream] = useState<MediaStream | null>(null);
  const [status, setStatus] = useState<WebcamStatus>('off');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!enabled) {
      streamRef.current?.getTracks().forEach((track) => track.stop());
      streamRef.current = null;
      setStream(null);
      setStatus('off');
      setError(null);
      return;
    }

    let cancelled = false;
    setStatus('requesting');
    setError(null);

    navigator.mediaDevices
      .getUserMedia({ video: true, audio: false })
      .then((media) => {
        if (cancelled) {
          media.getTracks().forEach((track) => track.stop());
          return;
        }
        streamRef.current = media;
        setStream(media);
        setStatus('on');
      })
      .catch((err: unknown) => {
        if (cancelled) {
          return;
        }
        setStatus('error');
        setError(err instanceof Error ? err.message : 'Camera unavailable');
      });

    return () => {
      cancelled = true;
    };
  }, [enabled]);

  // Release the camera on unmount.
  useEffect(() => {
    return () => {
      streamRef.current?.getTracks().forEach((track) => track.stop());
      streamRef.current = null;
    };
  }, []);

  return { stream, status, error };
}
