/**
 * EditJobApplicationPage — edits an existing job application.
 *
 * Loads the application by route param ID and displays the
 * JobApplicationForm pre-filled with existing data.
 *
 * @author DevLaunch
 */

import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { jobApplicationService } from '../../services/job-application.service';
import { JobApplicationForm } from '../../components/job-applications/JobApplicationForm';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { ROUTES } from '../../constants/routes';
import { MESSAGES } from '../../constants/messages';
import type { JobApplicationResponse, CreateJobApplicationRequest } from '../../types/job-application';

export const EditJobApplicationPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [application, setApplication] = useState<JobApplicationResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const applicationId = id ? Number(id) : NaN;

  const fetchApplication = useCallback(async () => {
    if (isNaN(applicationId)) {
      navigate(ROUTES.JOB_APPLICATION_LIST);
      return;
    }

    setIsLoading(true);
    setError(null);
    try {
      const data = await jobApplicationService.getById(applicationId);
      setApplication(data);
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : MESSAGES.LOAD_ERROR('job application');
      setError(message);
    } finally {
      setIsLoading(false);
    }
  }, [applicationId, navigate]);

  useEffect(() => {
    fetchApplication();
  }, [fetchApplication]);

  const handleSubmit = async (data: CreateJobApplicationRequest) => {
    if (!application) return;
    setIsSubmitting(true);
    try {
      await jobApplicationService.update(application.id, data);
      toast.success(MESSAGES.UPDATE_SUCCESS('Job application'));
      navigate(ROUTES.JOB_APPLICATION_LIST);
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : MESSAGES.SAVE_ERROR('job application');
      toast.error(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  // ─── Loading state ───
  if (isLoading) {
    return <LoadingScreen />;
  }

  // ─── Error state ───
  if (error || !application) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh]">
        <ErrorMessage
          message={error ?? MESSAGES.LOAD_ERROR('job application')}
          onRetry={fetchApplication}
        />
      </div>
    );
  }

  // ─── Data state ───
  return (
    <JobApplicationForm
      initialData={application}
      isSubmitting={isSubmitting}
      onSubmit={handleSubmit}
    />
  );
};
