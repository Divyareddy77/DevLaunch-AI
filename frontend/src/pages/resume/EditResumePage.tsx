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

import { resumeService } from '../../services/resume.service';
import { ResumeForm } from '../../components/resume/ResumeForm';
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
      {/* Refresh the resume header when data changes */}
      <ResumeForm
        resume={resume}
        onDataChanged={fetchResume}
      />
    </div>
  );
};
