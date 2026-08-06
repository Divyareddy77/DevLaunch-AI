/**
 * ProjectAnalysisCard — per-project quality evaluations of the ATS report.
 *
 * For each project shows its name, description-quality and technical-depth
 * ratings, the technologies and action verbs used, whether business impact
 * and measurable outcomes are present, and project-specific suggestions.
 *
 * @author DevLaunch
 */

import React from 'react';
import { FolderGit2, CheckCircle2, XCircle } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import type { ProjectAnalysis } from '../../types/ai';

interface ProjectAnalysisCardProps {
  /** The per-project evaluations. */
  projects: ProjectAnalysis[];
}

/** Maps a description-quality rating to a badge variant. */
function qualityVariant(rating: string): 'success' | 'warning' | 'danger' {
  if (rating === 'Detailed' || rating === 'Good') return 'success';
  if (rating === 'Brief') return 'warning';
  return 'danger';
}

/** Maps a technical-depth rating to a badge variant. */
function depthVariant(rating: string): 'success' | 'warning' | 'danger' {
  if (rating === 'High') return 'success';
  if (rating === 'Moderate') return 'warning';
  return 'danger';
}

const Flag: React.FC<{ present: boolean; label: string }> = ({ present, label }) => (
  <span className="inline-flex items-center gap-1 text-xs text-gray-600">
    {present ? (
      <CheckCircle2 className="h-3.5 w-3.5 text-emerald-500" />
    ) : (
      <XCircle className="h-3.5 w-3.5 text-red-400" />
    )}
    {label}
  </span>
);

export const ProjectAnalysisCard: React.FC<ProjectAnalysisCardProps> = ({ projects }) => {
  if (projects.length === 0) return null;

  return (
    <Card
      header={
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-emerald-100">
            <FolderGit2 className="h-5 w-5 text-emerald-600" />
          </div>
          <h3 className="text-sm font-semibold text-gray-900">Project Analysis</h3>
        </div>
      }
    >
      <ul className="space-y-5">
        {projects.map((project) => (
          <li key={project.projectName} className="rounded-lg border border-gray-100 p-4">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <p className="text-sm font-semibold text-gray-900">{project.projectName}</p>
              <div className="flex items-center gap-1.5">
                {project.descriptionQuality && (
                  <Badge variant={qualityVariant(project.descriptionQuality)} size="sm">
                    {project.descriptionQuality} description
                  </Badge>
                )}
                {project.technicalDepth && (
                  <Badge variant={depthVariant(project.technicalDepth)} size="sm">
                    {project.technicalDepth} depth
                  </Badge>
                )}
              </div>
            </div>

            <div className="mt-3 flex flex-wrap items-center gap-x-4 gap-y-1.5">
              <Flag present={project.businessImpact} label="Business impact" />
              <Flag present={project.measurableOutcomes} label="Measurable outcomes" />
            </div>

            {project.technologiesMentioned.length > 0 && (
              <div className="mt-3 flex flex-wrap items-center gap-1.5">
                <span className="text-xs font-medium text-gray-400">Tech:</span>
                {project.technologiesMentioned.map((tech) => (
                  <Badge key={tech} variant="primary" size="sm">
                    {tech}
                  </Badge>
                ))}
              </div>
            )}

            {project.actionVerbs.length > 0 && (
              <p className="mt-2 text-xs text-gray-500">
                Action verbs: {project.actionVerbs.join(', ')}
              </p>
            )}

            {project.suggestions.length > 0 && (
              <ul className="mt-3 space-y-1.5 border-t border-gray-100 pt-3">
                {project.suggestions.map((suggestion, index) => (
                  <li key={index} className="flex items-start gap-2 text-sm text-gray-600">
                    <span className="mt-1.5 h-1 w-1 shrink-0 rounded-full bg-gray-300" />
                    {suggestion}
                  </li>
                ))}
              </ul>
            )}
          </li>
        ))}
      </ul>
    </Card>
  );
};
