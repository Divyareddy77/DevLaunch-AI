/**
 * Type definitions for the Study Planner module.
 *
 * Mirrors the backend DTOs in
 * com.devlaunch.dto.request.*,
 * com.devlaunch.dto.response.*,
 * com.devlaunch.entity.enums.StudyPriority,
 * com.devlaunch.entity.enums.StudyStatus.
 *
 * @author DevLaunch
 */

// ────────────────────────────────────────────────────
// Enums
// ────────────────────────────────────────────────────

/** Priority levels for a study session. */
export type StudyPriorityEnum = 'LOW' | 'MEDIUM' | 'HIGH';

/** Completion statuses for a study session. */
export type StudyStatusEnum = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED';

/** Human-readable labels for each priority level. */
export const STUDY_PRIORITY_LABELS: Record<StudyPriorityEnum, string> = {
  LOW: 'Low',
  MEDIUM: 'Medium',
  HIGH: 'High',
};

/** Human-readable labels for each status. */
export const STUDY_STATUS_LABELS: Record<StudyStatusEnum, string> = {
  PENDING: 'Pending',
  IN_PROGRESS: 'In Progress',
  COMPLETED: 'Completed',
};

/** All priority values as an array. */
export const STUDY_PRIORITIES: StudyPriorityEnum[] = ['LOW', 'MEDIUM', 'HIGH'];

/** All status values as an array. */
export const STUDY_STATUSES: StudyStatusEnum[] = ['PENDING', 'IN_PROGRESS', 'COMPLETED'];

// ────────────────────────────────────────────────────
// Response DTO
// ────────────────────────────────────────────────────

/** Study planner response from GET/POST/PUT /api/study-planners. */
export interface StudyPlannerResponse {
  id: number;
  title: string;
  description: string | null;
  studyDate: string;
  startTime: string | null;
  endTime: string | null;
  priority: StudyPriorityEnum;
  status: StudyStatusEnum;
}

// ────────────────────────────────────────────────────
// Request DTOs
// ────────────────────────────────────────────────────

/** Payload for creating a study task (POST /api/study-planners). */
export interface CreateStudyPlannerRequest {
  title: string;
  description?: string;
  studyDate: string;
  startTime?: string;
  endTime?: string;
  priority: StudyPriorityEnum;
  status: StudyStatusEnum;
}

/** Payload for updating a study task (PUT /api/study-planners/{id}). */
export interface UpdateStudyPlannerRequest {
  title: string;
  description?: string;
  studyDate: string;
  startTime?: string;
  endTime?: string;
  priority: StudyPriorityEnum;
  status: StudyStatusEnum;
}
