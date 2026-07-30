/**
 * Study Planner service — CRUD operations for study task management.
 *
 * Communicates with the backend StudyPlannerController.
 *
 * @see backend/src/main/java/com/devlaunch/controller/StudyPlannerController.java
 * @author DevLaunch
 */

import apiClient from '../api/client';
import { STUDY_PLANNERS } from '../api/endpoints';
import type {
  StudyPlannerResponse,
  CreateStudyPlannerRequest,
  UpdateStudyPlannerRequest,
} from '../types/study-planner';

export const studyPlannerService = {
  /** POST /api/study-planners — Create a new study task. */
  create: (data: CreateStudyPlannerRequest) =>
    apiClient.post<StudyPlannerResponse>(STUDY_PLANNERS.BASE, data).then((r) => r.data),

  /** GET /api/study-planners — Get all study tasks for the authenticated user. */
  getAll: () =>
    apiClient.get<StudyPlannerResponse[]>(STUDY_PLANNERS.BASE).then((r) => r.data),

  /** GET /api/study-planners/{id} — Get a single study task by ID. */
  getById: (id: number) =>
    apiClient.get<StudyPlannerResponse>(STUDY_PLANNERS.BY_ID(id)).then((r) => r.data),

  /** PUT /api/study-planners/{id} — Update a study task. */
  update: (id: number, data: UpdateStudyPlannerRequest) =>
    apiClient.put<StudyPlannerResponse>(STUDY_PLANNERS.BY_ID(id), data).then((r) => r.data),

  /** DELETE /api/study-planners/{id} — Delete a study task. */
  delete: (id: number) =>
    apiClient.delete<string>(STUDY_PLANNERS.BY_ID(id)).then((r) => r.data),
};
