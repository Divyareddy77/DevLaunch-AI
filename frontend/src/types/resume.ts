/**
 * Type definitions for the Resume Builder module.
 *
 * Mirrors the backend DTOs in
 * com.devlaunch.dto.request.* and com.devlaunch.dto.response.*
 *
 * @author DevLaunch
 */

// ────────────────────────────────────────────────────
// Resume Core
// ────────────────────────────────────────────────────

/** Resume response returned from GET/POST/PUT /api/resumes. */
export interface ResumeResponse {
  id: number;
  headline: string;
  summary: string;
  linkedinUrl: string | null;
  githubUrl: string | null;
  portfolioUrl: string | null;
  template: ResumeTemplateResponse | null;
}

/** Payload for creating a new resume (POST /api/resumes). */
export interface CreateResumeRequest {
  headline: string;
  summary: string;
  linkedinUrl?: string;
  githubUrl?: string;
  portfolioUrl?: string;
}

/** Payload for updating an existing resume (PUT /api/resumes/{id}). */
export interface UpdateResumeRequest {
  headline: string;
  summary: string;
  linkedinUrl?: string;
  githubUrl?: string;
  portfolioUrl?: string;
}

// ────────────────────────────────────────────────────
// Resume Template
// ────────────────────────────────────────────────────

/** Response DTO for resume template information. */
export interface ResumeTemplateResponse {
  id: number;
  name: string;
  description: string | null;
  previewImageUrl: string | null;
}

// ────────────────────────────────────────────────────
// PDF Layout Template
// ────────────────────────────────────────────────────

/** PDF layout templates available when downloading a resume as a PDF. */
export type ResumePdfTemplateValue =
  | 'CLASSIC_PROFESSIONAL'
  | 'MODERN_BLUE'
  | 'MINIMAL'
  | 'EXECUTIVE'
  | 'CREATIVE';

// ────────────────────────────────────────────────────
// Education
// ────────────────────────────────────────────────────

/** Education record response. */
export interface EducationResponse {
  id: number;
  institutionName: string;
  degree: string;
  fieldOfStudy: string;
  grade: string | null;
  startDate: string | null;
  endDate: string | null;
  currentlyStudying: boolean;
  description: string | null;
}

/** Payload for creating an education record. */
export interface CreateEducationRequest {
  institutionName: string;
  degree: string;
  fieldOfStudy: string;
  grade?: string;
  startDate?: string;
  endDate?: string;
  currentlyStudying?: boolean;
  description?: string;
}

/** Payload for updating an education record. */
export interface UpdateEducationRequest {
  institutionName: string;
  degree: string;
  fieldOfStudy: string;
  grade?: string;
  startDate?: string;
  endDate?: string;
  currentlyStudying?: boolean;
  description?: string;
}

// ────────────────────────────────────────────────────
// Experience
// ────────────────────────────────────────────────────

/** Work experience record response. */
export interface ExperienceResponse {
  id: number;
  companyName: string;
  jobTitle: string;
  employmentType: string | null;
  location: string | null;
  startDate: string;
  endDate: string | null;
  currentlyWorking: boolean;
  description: string | null;
}

/** Payload for creating an experience record. */
export interface CreateExperienceRequest {
  companyName: string;
  jobTitle: string;
  employmentType?: string;
  location?: string;
  startDate: string;
  endDate?: string;
  currentlyWorking?: boolean;
  description?: string;
}

/** Payload for updating an experience record. */
export interface UpdateExperienceRequest {
  companyName: string;
  jobTitle: string;
  employmentType?: string;
  location?: string;
  startDate: string;
  endDate?: string;
  currentlyWorking?: boolean;
  description?: string;
}

// ────────────────────────────────────────────────────
// Project
// ────────────────────────────────────────────────────

/** Project record response. */
export interface ProjectResponse {
  id: number;
  projectName: string;
  description: string;
  technologies: string;
  githubUrl: string | null;
  liveUrl: string | null;
  startDate: string | null;
  endDate: string | null;
  currentlyWorking: boolean;
}

/** Payload for creating a project record. */
export interface CreateProjectRequest {
  projectName: string;
  description: string;
  technologies: string;
  githubUrl?: string;
  liveUrl?: string;
  startDate?: string;
  endDate?: string;
  currentlyWorking?: boolean;
}

/** Payload for updating a project record. */
export interface UpdateProjectRequest {
  projectName: string;
  description: string;
  technologies: string;
  githubUrl?: string;
  liveUrl?: string;
  startDate?: string;
  endDate?: string;
  currentlyWorking?: boolean;
}

// ────────────────────────────────────────────────────
// Skill
// ────────────────────────────────────────────────────

/** Skill record response. */
export interface SkillResponse {
  id: number;
  skillName: string;
  proficiency: string | null;
}

/** Payload for creating a skill record. */
export interface CreateSkillRequest {
  skillName: string;
  proficiency?: string;
}

/** Payload for updating a skill record. */
export interface UpdateSkillRequest {
  skillName: string;
  proficiency?: string;
}

// ────────────────────────────────────────────────────
// Certification
// ────────────────────────────────────────────────────

/** Certification record response. */
export interface CertificationResponse {
  id: number;
  certificationName: string;
  issuingOrganization: string;
  issueDate: string | null;
  expiryDate: string | null;
  credentialId: string | null;
  credentialUrl: string | null;
}

/** Payload for creating a certification record. */
export interface CreateCertificationRequest {
  certificationName: string;
  issuingOrganization: string;
  issueDate?: string;
  expiryDate?: string;
  credentialId?: string;
  credentialUrl?: string;
}

/** Payload for updating a certification record. */
export interface UpdateCertificationRequest {
  certificationName: string;
  issuingOrganization: string;
  issueDate?: string;
  expiryDate?: string;
  credentialId?: string;
  credentialUrl?: string;
}

// ────────────────────────────────────────────────────
// Achievement
// ────────────────────────────────────────────────────

/** Achievement record response. */
export interface AchievementResponse {
  id: number;
  title: string;
  description: string | null;
  dateAchieved: string | null;
}

/** Payload for creating an achievement record. */
export interface CreateAchievementRequest {
  title: string;
  description?: string;
  dateAchieved?: string;
}

/** Payload for updating an achievement record. */
export interface UpdateAchievementRequest {
  title: string;
  description?: string;
  dateAchieved?: string;
}
