/**
 * ResumeCard — displays a single resume as a card in the resume list.
 *
 * Shows the headline, summary preview, and action buttons for
 * editing, viewing, and deleting the resume.
 *
 * @author DevLaunch
 */

import React from 'react';
import { FileText, Edit3, Trash2, ExternalLink, Calendar } from 'lucide-react';
import { Card } from '../ui/Card';
import type { ResumeResponse } from '../../types/resume';

interface ResumeCardProps {
  /** The resume data to display. */
  resume: ResumeResponse;
  /** Callback when the edit button is clicked. */
  onEdit: (id: number) => void;
  /** Callback when the delete button is clicked. */
  onDelete: (id: number) => void;
  /** Callback when the view button is clicked. */
  onView: (id: number) => void;
}

export const ResumeCard: React.FC<ResumeCardProps> = ({
  resume,
  onEdit,
  onDelete,
  onView,
}) => {
  return (
    <Card className="group transition-all hover:shadow-md hover:border-primary-200" padded={false}>
      <div className="p-5">
        {/* Header */}
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-start gap-3 min-w-0">
            <div className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-lg bg-primary-100 text-primary-600">
              <FileText className="h-5 w-5" />
            </div>
            <div className="min-w-0">
              <h3 className="truncate text-sm font-semibold text-gray-900">
                {resume.headline}
              </h3>
              <p className="mt-0.5 text-xs text-gray-500">
                {resume.template ? (
                  <span className="flex items-center gap-1">
                    Template: {resume.template.name}
                  </span>
                ) : (
                  'No template assigned'
                )}
              </p>
            </div>
          </div>
        </div>

        {/* Summary preview */}
        <p className="mt-3 line-clamp-2 text-sm text-gray-600">
          {resume.summary}
        </p>

        {/* URL links */}
        {(resume.linkedinUrl || resume.githubUrl || resume.portfolioUrl) && (
          <div className="mt-3 flex flex-wrap gap-2">
            {resume.linkedinUrl && (
              <a
                href={resume.linkedinUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-1 rounded-md bg-blue-50 px-2 py-1 text-xs font-medium text-blue-600 hover:bg-blue-100 transition-colors"
              >
                <ExternalLink className="h-3 w-3" />
                LinkedIn
              </a>
            )}
            {resume.githubUrl && (
              <a
                href={resume.githubUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-1 rounded-md bg-gray-100 px-2 py-1 text-xs font-medium text-gray-600 hover:bg-gray-200 transition-colors"
              >
                <ExternalLink className="h-3 w-3" />
                GitHub
              </a>
            )}
            {resume.portfolioUrl && (
              <a
                href={resume.portfolioUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-1 rounded-md bg-purple-50 px-2 py-1 text-xs font-medium text-purple-600 hover:bg-purple-100 transition-colors"
              >
                <ExternalLink className="h-3 w-3" />
                Portfolio
              </a>
            )}
          </div>
        )}
      </div>

      {/* Actions footer */}
      <div className="flex items-center justify-between border-t border-gray-100 px-5 py-3">
        <button
          onClick={() => onView(resume.id)}
          className="inline-flex items-center gap-1 text-xs font-medium text-indigo-600 hover:text-indigo-800 transition-colors"
        >
          <Calendar className="h-3.5 w-3.5" />
          View Details
        </button>
        <div className="flex items-center gap-1">
          <button
            onClick={() => onEdit(resume.id)}
            className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors"
            aria-label="Edit resume"
            title="Edit resume"
          >
            <Edit3 className="h-4 w-4" />
          </button>
          <button
            onClick={() => onDelete(resume.id)}
            className="rounded-lg p-1.5 text-gray-400 hover:bg-red-50 hover:text-red-500 transition-colors"
            aria-label="Delete resume"
            title="Delete resume"
          >
            <Trash2 className="h-4 w-4" />
          </button>
        </div>
      </div>
    </Card>
  );
};
