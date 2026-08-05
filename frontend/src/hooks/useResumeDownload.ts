/**
 * useResumeDownload — shared resume PDF download logic.
 *
 * Encapsulates the download flow (call the service with the chosen PDF
 * template, show a success toast, surface backend error messages)
 * together with the loading state so the resume list and edit pages do
 * not duplicate it. Returns whether the download succeeded so callers
 * can close the template picker on success.
 *
 * @author DevLaunch
 */

import { useCallback, useState } from 'react';
import toast from 'react-hot-toast';
import { resumeService } from '../services/resume.service';
import { MESSAGES } from '../constants/messages';
import type { ResumePdfTemplateValue } from '../types/resume';

export function useResumeDownload() {
  const [isDownloading, setIsDownloading] = useState(false);

  const download = useCallback(
    async (id: number, template: ResumePdfTemplateValue): Promise<boolean> => {
      setIsDownloading(true);
      try {
        await resumeService.downloadPdf(id, template);
        toast.success(MESSAGES.RESUME_DOWNLOAD_SUCCESS);
        return true;
      } catch (err: unknown) {
        const message =
          err instanceof Error ? err.message : MESSAGES.RESUME_DOWNLOAD_ERROR;
        toast.error(message);
        return false;
      } finally {
        setIsDownloading(false);
      }
    },
    [],
  );

  return { download, isDownloading };
}
