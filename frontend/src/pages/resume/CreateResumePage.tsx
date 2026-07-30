/**
 * CreateResumePage — creates a new resume and redirects to the edit page.
 *
 * A simple form collecting the core resume fields (headline, summary, URLs).
 * On successful creation, navigates to the EditResumePage so the user can
 * immediately add sections (education, experience, etc.).
 *
 * @author DevLaunch
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import toast from 'react-hot-toast';
import { PersonalInformationForm } from '../../components/resume/PersonalInformationForm';
import { resumeService } from '../../services/resume.service';
import { ROUTES } from '../../constants/routes';
import { MESSAGES } from '../../constants/messages';
import type { UpdateResumeRequest } from '../../types/resume';

export const CreateResumePage: React.FC = () => {
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (data: UpdateResumeRequest) => {
    setIsSubmitting(true);
    try {
      const created = await resumeService.createResume(data);
      toast.success(MESSAGES.CREATE_SUCCESS('Resume'));
      navigate(ROUTES.RESUME_EDIT(created.id));
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : MESSAGES.SAVE_ERROR('resume');
      toast.error(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-3xl">
      {/* Back button */}
      <button
        onClick={() => navigate(ROUTES.RESUME_LIST)}
        className="mb-6 inline-flex items-center gap-1.5 text-sm font-medium text-gray-500 hover:text-gray-700 transition-colors"
      >
        <ArrowLeft className="h-4 w-4" />
        Back to Resumes
      </button>

      {/* Page header */}
      <div className="mb-6">
        <h1 className="text-xl font-bold text-gray-900">Create New Resume</h1>
        <p className="mt-1 text-sm text-gray-500">
          Start by filling in your professional details. You can add sections
          like education, experience, and skills afterward.
        </p>
      </div>

      {/* Form */}
      <PersonalInformationForm
        isSubmitting={isSubmitting}
        onSubmit={handleSubmit}
      />
    </div>
  );
};
