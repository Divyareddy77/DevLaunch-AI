/**
 * CreateStudyPlannerPage — creates a new study task.
 *
 * Uses the StudyPlannerForm and redirects to the list on success.
 *
 * @author DevLaunch
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { StudyPlannerForm } from '../../components/study-planner/StudyPlannerForm';
import { studyPlannerService } from '../../services/study-planner.service';
import { ROUTES } from '../../constants/routes';
import { MESSAGES } from '../../constants/messages';
import type { CreateStudyPlannerRequest } from '../../types/study-planner';

export const CreateStudyPlannerPage: React.FC = () => {
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (data: CreateStudyPlannerRequest) => {
    setIsSubmitting(true);
    try {
      await studyPlannerService.create(data);
      toast.success(MESSAGES.CREATE_SUCCESS('Study task'));
      navigate(ROUTES.STUDY_PLANNER_LIST);
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : MESSAGES.SAVE_ERROR('study task');
      toast.error(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <StudyPlannerForm
      isSubmitting={isSubmitting}
      onSubmit={handleSubmit}
    />
  );
};
