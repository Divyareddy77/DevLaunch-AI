/**
 * SkillsAnalysisCard — the skills section evaluation of the ATS report.
 *
 * Shows technical skills, soft skills, an organisation note, and the
 * in-demand skills that are missing.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Wrench } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';

interface SkillsAnalysisCardProps {
  /** Technical skills detected in the resume. */
  technicalSkills: string[];
  /** Remaining (non-technical) skills. */
  softSkills: string[];
  /** A note on how the skills are organised. */
  organization: string;
  /** In-demand skills missing from the resume. */
  missingRelevantSkills: string[];
}

export const SkillsAnalysisCard: React.FC<SkillsAnalysisCardProps> = ({
  technicalSkills,
  softSkills,
  organization,
  missingRelevantSkills,
}) => {
  return (
    <Card
      header={
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-sky-100">
            <Wrench className="h-5 w-5 text-sky-600" />
          </div>
          <h3 className="text-sm font-semibold text-gray-900">Skills Analysis</h3>
        </div>
      }
    >
      <div className="space-y-4">
        <div>
          <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-gray-500">
            Technical skills
          </p>
          {technicalSkills.length === 0 ? (
            <p className="text-sm text-gray-400">No technical skills detected.</p>
          ) : (
            <div className="flex flex-wrap gap-1.5">
              {technicalSkills.map((skill) => (
                <Badge key={skill} variant="primary" size="sm">
                  {skill}
                </Badge>
              ))}
            </div>
          )}
        </div>

        {softSkills.length > 0 && (
          <div>
            <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-gray-500">
              Soft skills
            </p>
            <div className="flex flex-wrap gap-1.5">
              {softSkills.map((skill) => (
                <Badge key={skill} variant="default" size="sm">
                  {skill}
                </Badge>
              ))}
            </div>
          </div>
        )}

        {organization && (
          <p className="rounded-lg bg-gray-50 px-4 py-3 text-sm text-gray-600">{organization}</p>
        )}

        {missingRelevantSkills.length > 0 && (
          <div>
            <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-gray-500">
              Missing relevant skills
            </p>
            <div className="flex flex-wrap gap-1.5">
              {missingRelevantSkills.map((skill) => (
                <Badge key={skill} variant="warning" size="sm">
                  {skill}
                </Badge>
              ))}
            </div>
          </div>
        )}
      </div>
    </Card>
  );
};
