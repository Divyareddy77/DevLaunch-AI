/**
 * CreateJobApplicationPage — creates a new job application.
 *
 * Uses the JobApplicationForm and redirects to the list on success.
 *
 * @author DevLaunch
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { JobApplicationForm } from '../../components/job-applications/JobApplicationForm';
import { jobApplicationService } from '../../services/job-application.service';
import { ROUTES } from '../../constants/routes';
import { MESSAGES } from '../../constants/messages';
import type { CreateJobApplicationRequest } from '../../types/job-application';

export const CreateJobApplicationPage: React.FC = () => {
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (data: CreateJobApplicationRequest) => {
    setIsSubmitting(true);
    try {
      await jobApplicationService.create(data);
      toast.success(MESSAGES.CREATE_SUCCESS('Job application'));
      navigate(ROUTES.JOB_APPLICATION_LIST);
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : MESSAGES.SAVE_ERROR('job application');
      toast.error(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <JobApplicationForm
      isSubmitting={isSubmitting}
      onSubmit={handleSubmit}
    />
  );
};
