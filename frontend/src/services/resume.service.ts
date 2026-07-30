/**
 * Resume service — all resume CRUD and sub-resource operations.
 *
 * Communicates with the backend ResumeController, EducationController,
 * ExperienceController, SkillController, CertificationController,
 * AchievementController, and ProjectController.
 *
 * @see backend/src/main/java/com/devlaunch/controller/
 * @author DevLaunch
 */

import apiClient from '../api/client';
import { RESUMES, RESUME_SUB_RESOURCES } from '../api/endpoints';
import type {
  ResumeResponse,
  CreateResumeRequest,
  UpdateResumeRequest,
  EducationResponse,
  CreateEducationRequest,
  UpdateEducationRequest,
  ExperienceResponse,
  CreateExperienceRequest,
  UpdateExperienceRequest,
  SkillResponse,
  CreateSkillRequest,
  UpdateSkillRequest,
  CertificationResponse,
  CreateCertificationRequest,
  UpdateCertificationRequest,
  AchievementResponse,
  CreateAchievementRequest,
  UpdateAchievementRequest,
  ProjectResponse,
  CreateProjectRequest,
  UpdateProjectRequest,
  ResumeTemplateResponse,
} from '../types/resume';

export const resumeService = {
  // ──────── Resume Core ────────

  /** POST /api/resumes — Create a new resume. */
  createResume: (data: CreateResumeRequest) =>
    apiClient.post<ResumeResponse>(RESUMES.BASE, data).then((r) => r.data),

  /** GET /api/resumes — Get all resumes for the authenticated user. */
  getAllResumes: () =>
    apiClient.get<ResumeResponse[]>(RESUMES.BASE).then((r) => r.data),

  /** GET /api/resumes/{id} — Get a single resume by ID. */
  getResumeById: (id: number) =>
    apiClient.get<ResumeResponse>(RESUMES.BY_ID(id)).then((r) => r.data),

  /** PUT /api/resumes/{id} — Update a resume. */
  updateResume: (id: number, data: UpdateResumeRequest) =>
    apiClient.put<ResumeResponse>(RESUMES.BY_ID(id), data).then((r) => r.data),

  /** DELETE /api/resumes/{id} — Delete a resume. */
  deleteResume: (id: number) =>
    apiClient.delete<string>(RESUMES.BY_ID(id)).then((r) => r.data),

  // ──────── Templates ────────

  /** PUT /api/resumes/{resumeId}/template/{templateId} — Assign a template. */
  assignTemplate: (resumeId: number, templateId: number) =>
    apiClient
      .put<ResumeResponse>(RESUMES.ASSIGN_TEMPLATE(resumeId, templateId))
      .then((r) => r.data),

  /** GET /api/resumes/{resumeId}/template — Get the assigned template. */
  getResumeTemplate: (resumeId: number) =>
    apiClient
      .get<ResumeTemplateResponse | null>(RESUMES.TEMPLATE(resumeId))
      .then((r) => r.data),

  // ──────── Education ────────

  /** POST /api/resumes/{resumeId}/educations */
  createEducation: (resumeId: number, data: CreateEducationRequest) =>
    apiClient
      .post<EducationResponse>(RESUME_SUB_RESOURCES.EDUCATION(resumeId), data)
      .then((r) => r.data),

  /** GET /api/resumes/{resumeId}/educations */
  getAllEducations: (resumeId: number) =>
    apiClient
      .get<EducationResponse[]>(RESUME_SUB_RESOURCES.EDUCATION(resumeId))
      .then((r) => r.data),

  /** PUT /api/resumes/{resumeId}/educations/{eduId} */
  updateEducation: (
    resumeId: number,
    eduId: number,
    data: UpdateEducationRequest,
  ) =>
    apiClient
      .put<EducationResponse>(
        RESUME_SUB_RESOURCES.EDUCATION_BY_ID(resumeId, eduId),
        data,
      )
      .then((r) => r.data),

  /** DELETE /api/resumes/{resumeId}/educations/{eduId} */
  deleteEducation: (resumeId: number, eduId: number) =>
    apiClient
      .delete<string>(RESUME_SUB_RESOURCES.EDUCATION_BY_ID(resumeId, eduId))
      .then((r) => r.data),

  // ──────── Experience ────────

  /** POST /api/resumes/{resumeId}/experiences */
  createExperience: (resumeId: number, data: CreateExperienceRequest) =>
    apiClient
      .post<ExperienceResponse>(
        RESUME_SUB_RESOURCES.EXPERIENCE(resumeId),
        data,
      )
      .then((r) => r.data),

  /** GET /api/resumes/{resumeId}/experiences */
  getAllExperiences: (resumeId: number) =>
    apiClient
      .get<ExperienceResponse[]>(RESUME_SUB_RESOURCES.EXPERIENCE(resumeId))
      .then((r) => r.data),

  /** PUT /api/resumes/{resumeId}/experiences/{expId} */
  updateExperience: (
    resumeId: number,
    expId: number,
    data: UpdateExperienceRequest,
  ) =>
    apiClient
      .put<ExperienceResponse>(
        RESUME_SUB_RESOURCES.EXPERIENCE_BY_ID(resumeId, expId),
        data,
      )
      .then((r) => r.data),

  /** DELETE /api/resumes/{resumeId}/experiences/{expId} */
  deleteExperience: (resumeId: number, expId: number) =>
    apiClient
      .delete<string>(RESUME_SUB_RESOURCES.EXPERIENCE_BY_ID(resumeId, expId))
      .then((r) => r.data),

  // ──────── Skill ────────

  /** POST /api/resumes/{resumeId}/skills */
  createSkill: (resumeId: number, data: CreateSkillRequest) =>
    apiClient
      .post<SkillResponse>(RESUME_SUB_RESOURCES.SKILL(resumeId), data)
      .then((r) => r.data),

  /** GET /api/resumes/{resumeId}/skills */
  getAllSkills: (resumeId: number) =>
    apiClient
      .get<SkillResponse[]>(RESUME_SUB_RESOURCES.SKILL(resumeId))
      .then((r) => r.data),

  /** PUT /api/resumes/{resumeId}/skills/{skillId} */
  updateSkill: (
    resumeId: number,
    skillId: number,
    data: UpdateSkillRequest,
  ) =>
    apiClient
      .put<SkillResponse>(
        RESUME_SUB_RESOURCES.SKILL_BY_ID(resumeId, skillId),
        data,
      )
      .then((r) => r.data),

  /** DELETE /api/resumes/{resumeId}/skills/{skillId} */
  deleteSkill: (resumeId: number, skillId: number) =>
    apiClient
      .delete<string>(RESUME_SUB_RESOURCES.SKILL_BY_ID(resumeId, skillId))
      .then((r) => r.data),

  // ──────── Certification ────────

  /** POST /api/resumes/{resumeId}/certifications */
  createCertification: (
    resumeId: number,
    data: CreateCertificationRequest,
  ) =>
    apiClient
      .post<CertificationResponse>(
        RESUME_SUB_RESOURCES.CERTIFICATION(resumeId),
        data,
      )
      .then((r) => r.data),

  /** GET /api/resumes/{resumeId}/certifications */
  getAllCertifications: (resumeId: number) =>
    apiClient
      .get<CertificationResponse[]>(
        RESUME_SUB_RESOURCES.CERTIFICATION(resumeId),
      )
      .then((r) => r.data),

  /** PUT /api/resumes/{resumeId}/certifications/{certId} */
  updateCertification: (
    resumeId: number,
    certId: number,
    data: UpdateCertificationRequest,
  ) =>
    apiClient
      .put<CertificationResponse>(
        RESUME_SUB_RESOURCES.CERTIFICATION_BY_ID(resumeId, certId),
        data,
      )
      .then((r) => r.data),

  /** DELETE /api/resumes/{resumeId}/certifications/{certId} */
  deleteCertification: (resumeId: number, certId: number) =>
    apiClient
      .delete<string>(
        RESUME_SUB_RESOURCES.CERTIFICATION_BY_ID(resumeId, certId),
      )
      .then((r) => r.data),

  // ──────── Achievement ────────

  /** POST /api/resumes/{resumeId}/achievements */
  createAchievement: (resumeId: number, data: CreateAchievementRequest) =>
    apiClient
      .post<AchievementResponse>(
        RESUME_SUB_RESOURCES.ACHIEVEMENT(resumeId),
        data,
      )
      .then((r) => r.data),

  /** GET /api/resumes/{resumeId}/achievements */
  getAllAchievements: (resumeId: number) =>
    apiClient
      .get<AchievementResponse[]>(RESUME_SUB_RESOURCES.ACHIEVEMENT(resumeId))
      .then((r) => r.data),

  /** PUT /api/resumes/{resumeId}/achievements/{achievementId} */
  updateAchievement: (
    resumeId: number,
    achievementId: number,
    data: UpdateAchievementRequest,
  ) =>
    apiClient
      .put<AchievementResponse>(
        RESUME_SUB_RESOURCES.ACHIEVEMENT_BY_ID(resumeId, achievementId),
        data,
      )
      .then((r) => r.data),

  /** DELETE /api/resumes/{resumeId}/achievements/{achievementId} */
  deleteAchievement: (resumeId: number, achievementId: number) =>
    apiClient
      .delete<string>(
        RESUME_SUB_RESOURCES.ACHIEVEMENT_BY_ID(resumeId, achievementId),
      )
      .then((r) => r.data),

  // ──────── Project ────────

  /** POST /api/resumes/{resumeId}/projects */
  createProject: (resumeId: number, data: CreateProjectRequest) =>
    apiClient
      .post<ProjectResponse>(RESUME_SUB_RESOURCES.PROJECT(resumeId), data)
      .then((r) => r.data),

  /** GET /api/resumes/{resumeId}/projects */
  getAllProjects: (resumeId: number) =>
    apiClient
      .get<ProjectResponse[]>(RESUME_SUB_RESOURCES.PROJECT(resumeId))
      .then((r) => r.data),

  /** PUT /api/resumes/{resumeId}/projects/{projectId} */
  updateProject: (
    resumeId: number,
    projectId: number,
    data: UpdateProjectRequest,
  ) =>
    apiClient
      .put<ProjectResponse>(
        RESUME_SUB_RESOURCES.PROJECT_BY_ID(resumeId, projectId),
        data,
      )
      .then((r) => r.data),

  /** DELETE /api/resumes/{resumeId}/projects/{projectId} */
  deleteProject: (resumeId: number, projectId: number) =>
    apiClient
      .delete<string>(RESUME_SUB_RESOURCES.PROJECT_BY_ID(resumeId, projectId))
      .then((r) => r.data),
};
