/**
 * ResumeForm — composes all resume section components for the edit page.
 *
 * This component orchestrates all sub-resource sections into a single
 * editing view. It manages loading all sub-resources for the resume
 * and delegates CRUD operations to the resume service via callbacks.
 *
 * @author DevLaunch
 */

import React, { useEffect, useState, useCallback } from 'react';
import { ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { PersonalInformationForm } from './PersonalInformationForm';
import { EducationSection } from './EducationSection';
import { ExperienceSection } from './ExperienceSection';
import { ProjectSection } from './ProjectSection';
import { SkillSection } from './SkillSection';
import { CertificationSection } from './CertificationSection';
import { AchievementSection } from './AchievementSection';

import { resumeService } from '../../services/resume.service';
import { ROUTES } from '../../constants/routes';
import type {
  ResumeResponse,
  UpdateResumeRequest,
  EducationResponse,
  ExperienceResponse,
  ProjectResponse,
  SkillResponse,
  CertificationResponse,
  AchievementResponse,
} from '../../types/resume';

interface ResumeFormProps {
  /** The resume being edited. */
  resume: ResumeResponse;
  /** Callback after any successful save/create/delete to refresh parent state. */
  onDataChanged: () => void;
}

export const ResumeForm: React.FC<ResumeFormProps> = ({ resume, onDataChanged }) => {
  const navigate = useNavigate();
  const [submittingCore, setSubmittingCore] = useState(false);

  // Sub-resource state
  const [educations, setEducations] = useState<EducationResponse[]>([]);
  const [experiences, setExperiences] = useState<ExperienceResponse[]>([]);
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [skills, setSkills] = useState<SkillResponse[]>([]);
  const [certifications, setCertifications] = useState<CertificationResponse[]>([]);
  const [achievements, setAchievements] = useState<AchievementResponse[]>([]);
  const [loading, setLoading] = useState(true);

  const loadSubResources = useCallback(async () => {
    setLoading(true);
    try {
      const [edu, exp, proj, sk, cert, ach] = await Promise.all([
        resumeService.getAllEducations(resume.id),
        resumeService.getAllExperiences(resume.id),
        resumeService.getAllProjects(resume.id),
        resumeService.getAllSkills(resume.id),
        resumeService.getAllCertifications(resume.id),
        resumeService.getAllAchievements(resume.id),
      ]);
      setEducations(edu);
      setExperiences(exp);
      setProjects(proj);
      setSkills(sk);
      setCertifications(cert);
      setAchievements(ach);
    } catch {
      // Errors are handled by the parent page
    } finally {
      setLoading(false);
    }
  }, [resume.id]);

  useEffect(() => {
    loadSubResources();
  }, [loadSubResources]);

  // ── Core resume update ──
  const handleCoreUpdate = async (data: UpdateResumeRequest) => {
    setSubmittingCore(true);
    try {
      await resumeService.updateResume(resume.id, data);
      onDataChanged();
    } finally {
      setSubmittingCore(false);
    }
  };

  return (
    <div className="space-y-8">
      {/* Back button */}
      <button
        onClick={() => navigate(ROUTES.RESUME_LIST)}
        className="inline-flex items-center gap-1.5 text-sm font-medium text-gray-500 hover:text-gray-700 transition-colors"
      >
        <ArrowLeft className="h-4 w-4" />
        Back to Resumes
      </button>

      {/* Personal Information */}
      <PersonalInformationForm
        initialData={resume}
        isSubmitting={submittingCore}
        onSubmit={handleCoreUpdate}
      />

      {/* Sections */}
      <EducationSection
        items={educations}
        isLoading={loading}
        onCreate={async (data) => {
          await resumeService.createEducation(resume.id, data);
          await loadSubResources();
        }}
        onUpdate={async (id, data) => {
          await resumeService.updateEducation(resume.id, id, data);
          await loadSubResources();
        }}
        onDelete={async (id) => {
          await resumeService.deleteEducation(resume.id, id);
          await loadSubResources();
        }}
      />

      <ExperienceSection
        items={experiences}
        isLoading={loading}
        onCreate={async (data) => {
          await resumeService.createExperience(resume.id, data);
          await loadSubResources();
        }}
        onUpdate={async (id, data) => {
          await resumeService.updateExperience(resume.id, id, data);
          await loadSubResources();
        }}
        onDelete={async (id) => {
          await resumeService.deleteExperience(resume.id, id);
          await loadSubResources();
        }}
      />

      <ProjectSection
        items={projects}
        isLoading={loading}
        onCreate={async (data) => {
          await resumeService.createProject(resume.id, data);
          await loadSubResources();
        }}
        onUpdate={async (id, data) => {
          await resumeService.updateProject(resume.id, id, data);
          await loadSubResources();
        }}
        onDelete={async (id) => {
          await resumeService.deleteProject(resume.id, id);
          await loadSubResources();
        }}
      />

      <SkillSection
        items={skills}
        isLoading={loading}
        onCreate={async (data) => {
          await resumeService.createSkill(resume.id, data);
          await loadSubResources();
        }}
        onUpdate={async (id, data) => {
          await resumeService.updateSkill(resume.id, id, data);
          await loadSubResources();
        }}
        onDelete={async (id) => {
          await resumeService.deleteSkill(resume.id, id);
          await loadSubResources();
        }}
      />

      <CertificationSection
        items={certifications}
        isLoading={loading}
        onCreate={async (data) => {
          await resumeService.createCertification(resume.id, data);
          await loadSubResources();
        }}
        onUpdate={async (id, data) => {
          await resumeService.updateCertification(resume.id, id, data);
          await loadSubResources();
        }}
        onDelete={async (id) => {
          await resumeService.deleteCertification(resume.id, id);
          await loadSubResources();
        }}
      />

      <AchievementSection
        items={achievements}
        isLoading={loading}
        onCreate={async (data) => {
          await resumeService.createAchievement(resume.id, data);
          await loadSubResources();
        }}
        onUpdate={async (id, data) => {
          await resumeService.updateAchievement(resume.id, id, data);
          await loadSubResources();
        }}
        onDelete={async (id) => {
          await resumeService.deleteAchievement(resume.id, id);
          await loadSubResources();
        }}
      />
    </div>
  );
};
