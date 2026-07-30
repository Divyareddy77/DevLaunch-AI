/**
 * Job Application service — CRUD operations for job application tracking.
 *
 * Communicates with the backend JobApplicationController.
 *
 * @see backend/src/main/java/com/devlaunch/controller/JobApplicationController.java
 * @author DevLaunch
 */

import apiClient from '../api/client';
import { JOB_APPLICATIONS } from '../api/endpoints';
import type {
  JobApplicationResponse,
  CreateJobApplicationRequest,
  UpdateJobApplicationRequest,
} from '../types/job-application';

export const jobApplicationService = {
  /** POST /api/job-applications — Create a new job application. */
  create: (data: CreateJobApplicationRequest) =>
    apiClient.post<JobApplicationResponse>(JOB_APPLICATIONS.BASE, data).then((r) => r.data),

  /** GET /api/job-applications — Get all job applications for the authenticated user. */
  getAll: () =>
    apiClient.get<JobApplicationResponse[]>(JOB_APPLICATIONS.BASE).then((r) => r.data),

  /** GET /api/job-applications/{id} — Get a single job application by ID. */
  getById: (id: number) =>
    apiClient.get<JobApplicationResponse>(JOB_APPLICATIONS.BY_ID(id)).then((r) => r.data),

  /** PUT /api/job-applications/{id} — Update a job application. */
  update: (id: number, data: UpdateJobApplicationRequest) =>
    apiClient.put<JobApplicationResponse>(JOB_APPLICATIONS.BY_ID(id), data).then((r) => r.data),

  /** DELETE /api/job-applications/{id} — Delete a job application. */
  delete: (id: number) =>
    apiClient.delete<string>(JOB_APPLICATIONS.BY_ID(id)).then((r) => r.data),
};
