/**
 * EditResumePage — edit page for a single resume with all sections.
 *
 * Loads the resume by route param ID and displays the full ResumeForm
 * with personal information and all sub-resource sections.
 *
 * @author DevLaunch
 */

import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Download } from 'lucide-react';

import { resumeService } from '../../services/resume.service';
import { useResumeDownload } from '../../hooks/useResumeDownload';
import { ResumeForm } from '../../components/resume/ResumeForm';
import { ResumeTemplatePickerModal } from '../../components/resume/ResumeTemplatePickerModal';
import { Button } from '../../components/ui/Button';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { ROUTES } from '../../constants/routes';
import { MESSAGES } from '../../constants/messages';
import type { ResumeResponse } from '../../types/resume';

export const EditResumePage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [resume, setResume] = useState<ResumeResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isPickerOpen, setIsPickerOpen] = useState(false);
  const { download: handleDownload, isDownloading } = useResumeDownload();

  const resumeId = id ? Number(id) : NaN;

  const fetchResume = useCallback(async () => {
    if (isNaN(resumeId)) {
      navigate(ROUTES.RESUME_LIST);
      return;
    }

    setIsLoading(true);
    setError(null);
    try {
      const data = await resumeService.getResumeById(resumeId);
      setResume(data);
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : MESSAGES.LOAD_ERROR('resume');
      setError(message);
    } finally {
      setIsLoading(false);
    }
  }, [resumeId, navigate]);

  useEffect(() => {
    fetchResume();
  }, [fetchResume]);

  // ─── Loading state ───
  if (isLoading) {
    return <LoadingScreen />;
  }

  // ─── Error state ───
  if (error || !resume) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh]">
        <ErrorMessage
          message={error ?? MESSAGES.LOAD_ERROR('resume')}
          onRetry={fetchResume}
        />
      </div>
    );
  }

  // ─── Data state ───
  return (
    <div className="mx-auto max-w-4xl">
      {/* Page header with download action */}
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Edit Resume</h1>
          <p className="mt-1 text-sm text-gray-500">
            {resume.headline}
          </p>
        </div>
        <Button onClick={() => setIsPickerOpen(true)}>
          <Download className="h-4 w-4" />
          Download Resume
        </Button>
      </div>

      {/* PDF template picker modal */}
      <ResumeTemplatePickerModal
        isOpen={isPickerOpen}
        onClose={() => setIsPickerOpen(false)}
        resumeName={resume.headline}
        isDownloading={isDownloading}
        onDownload={async (template) => {
          const success = await handleDownload(resumeId, template);
          if (success) setIsPickerOpen(false);
        }}
      />

      {/* Refresh the resume header when data changes */}
      <ResumeForm
        resume={resume}
        onDataChanged={fetchResume}
      />
    </div>
  );
};
