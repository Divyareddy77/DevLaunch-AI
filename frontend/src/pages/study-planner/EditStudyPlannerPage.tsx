/**
 * EditStudyPlannerPage — edits an existing study task.
 *
 * Loads the task by route param ID and displays the StudyPlannerForm
 * pre-filled with existing data.
 *
 * @author DevLaunch
 */

import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { studyPlannerService } from '../../services/study-planner.service';
import { StudyPlannerForm } from '../../components/study-planner/StudyPlannerForm';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { ROUTES } from '../../constants/routes';
import { MESSAGES } from '../../constants/messages';
import type { StudyPlannerResponse, CreateStudyPlannerRequest } from '../../types/study-planner';

export const EditStudyPlannerPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [task, setTask] = useState<StudyPlannerResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const taskId = id ? Number(id) : NaN;

  const fetchTask = useCallback(async () => {
    if (isNaN(taskId)) {
      navigate(ROUTES.STUDY_PLANNER_LIST);
      return;
    }

    setIsLoading(true);
    setError(null);
    try {
      const data = await studyPlannerService.getById(taskId);
      setTask(data);
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : MESSAGES.LOAD_ERROR('study task');
      setError(message);
    } finally {
      setIsLoading(false);
    }
  }, [taskId, navigate]);

  useEffect(() => {
    fetchTask();
  }, [fetchTask]);

  const handleSubmit = async (data: CreateStudyPlannerRequest) => {
    if (!task) return;
    setIsSubmitting(true);
    try {
      await studyPlannerService.update(task.id, data);
      toast.success(MESSAGES.UPDATE_SUCCESS('Study task'));
      navigate(ROUTES.STUDY_PLANNER_LIST);
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : MESSAGES.SAVE_ERROR('study task');
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
  if (error || !task) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh]">
        <ErrorMessage
          message={error ?? MESSAGES.LOAD_ERROR('study task')}
          onRetry={fetchTask}
        />
      </div>
    );
  }

  // ─── Data state ───
  return (
    <StudyPlannerForm
      initialData={task}
      isSubmitting={isSubmitting}
      onSubmit={handleSubmit}
    />
  );
};
