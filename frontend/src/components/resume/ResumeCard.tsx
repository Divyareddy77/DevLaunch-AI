/**
 * ResumeCard — displays a single resume as a premium card in the resume list.
 *
 * Shows a stylised document preview thumbnail, the headline, a summary
 * preview, a completion percentage ring, and the assigned template badge,
 * with hover-lift animation and rich action buttons for viewing, editing,
 * downloading, and deleting the resume.
 *
 * @author DevLaunch
 */

import React, { useMemo } from 'react';
import {
  FileText,
  Edit3,
  Trash2,
  ExternalLink,
  Download,
  Eye,
  CheckCircle2,
  LayoutTemplate,
} from 'lucide-react';
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
  /** Callback when the download button is clicked (opens the template picker). */
  onDownload: (id: number) => void;
}

/** Accent colour per PDF template name, with a sensible fallback. */
const TEMPLATE_ACCENTS: Record<string, string> = {
  'Classic Professional': '#1e293b',
  'Modern Blue': '#2563eb',
  Minimal: '#0f172a',
  Executive: '#4338ca',
  Creative: '#db2777',
};

const DEFAULT_ACCENT = '#4f46e5';

/** Derives a UI-only completion percentage from the fields that are filled in. */
function computeCompletion(resume: ResumeResponse): number {
  let filled = 0;
  if (resume.headline.trim()) filled += 20;
  if (resume.summary.trim()) filled += 20;
  if (resume.linkedinUrl) filled += 15;
  if (resume.githubUrl) filled += 15;
  if (resume.portfolioUrl) filled += 15;
  if (resume.template) filled += 15;
  return Math.min(100, filled);
}

/** Stylised A4 document mockup used as the card's preview thumbnail. */
const DocumentPreview: React.FC<{ accent: string }> = ({ accent }) => (
  <div className="absolute inset-5 rounded-md bg-white p-3.5 shadow-[0_16px_40px_-16px_rgba(16,24,40,0.45)] transition-transform duration-500 group-hover:-rotate-1 group-hover:scale-[1.03]">
    {/* Template accent rail */}
    <div className="h-1.5 w-full rounded-full" style={{ backgroundColor: accent }} />
    {/* Headline bar */}
    <div className="mt-2.5 h-2 w-2/3 rounded-full bg-gray-800/80" />
    <div className="mt-1.5 h-1.5 w-1/3 rounded-full bg-gray-300" />
    {/* Body lines */}
    <div className="mt-3 space-y-1.5">
      <div className="h-1 w-full rounded-full bg-gray-100" />
      <div className="h-1 w-11/12 rounded-full bg-gray-100" />
      <div className="h-1 w-4/5 rounded-full bg-gray-100" />
      <div className="h-1 w-full rounded-full bg-gray-100" />
      <div className="h-1 w-2/3 rounded-full bg-gray-100" />
    </div>
    {/* Skill chips */}
    <div className="mt-3 flex gap-1.5">
      <span className="h-2.5 w-8 rounded-full" style={{ backgroundColor: `${accent}22` }} />
      <span className="h-2.5 w-8 rounded-full" style={{ backgroundColor: `${accent}22` }} />
      <span className="h-2.5 w-8 rounded-full" style={{ backgroundColor: `${accent}22` }} />
    </div>
  </div>
);

export const ResumeCard: React.FC<ResumeCardProps> = ({
  resume,
  onEdit,
  onDelete,
  onView,
  onDownload,
}) => {
  const completion = useMemo(() => computeCompletion(resume), [resume]);
  const accent = resume.template?.name
    ? TEMPLATE_ACCENTS[resume.template.name] ?? DEFAULT_ACCENT
    : DEFAULT_ACCENT;

  return (
    <Card
      className="card-lift group overflow-hidden hover:border-gray-200"
      padded={false}
    >
      {/* Preview thumbnail */}
      <button
        onClick={() => onView(resume.id)}
        className="relative block aspect-[16/10] w-full overflow-hidden bg-gradient-to-br from-gray-50 via-gray-100 to-gray-50 text-left focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-primary-500"
        aria-label={`View ${resume.headline}`}
      >
        <DocumentPreview accent={accent} />

        {/* Hover overlay */}
        <div className="absolute inset-0 flex items-center justify-center bg-gray-900/0 opacity-0 transition-all duration-300 group-hover:bg-gray-900/25 group-hover:opacity-100">
          <span className="inline-flex translate-y-1 items-center gap-1.5 rounded-full bg-white px-3.5 py-1.5 text-xs font-semibold text-gray-800 shadow-lg transition-transform duration-300 group-hover:translate-y-0">
            <Eye className="h-3.5 w-3.5" />
            Open Resume
          </span>
        </div>
      </button>

      {/* Body */}
      <div className="p-4">
        <div className="flex items-start justify-between gap-3">
          <div className="flex min-w-0 items-start gap-2.5">
            <div
              className="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-lg"
              style={{ backgroundColor: `${accent}1a`, color: accent }}
            >
              <FileText className="h-4 w-4" />
            </div>
            <div className="min-w-0">
              <h3 className="truncate text-sm font-semibold text-gray-900">
                {resume.headline}
              </h3>
              <p className="mt-0.5 flex items-center gap-1 text-xs text-gray-400">
                <LayoutTemplate className="h-3 w-3" />
                {resume.template?.name ?? 'No template assigned'}
              </p>
            </div>
          </div>

          {/* Completion ring */}
          <div
            className="relative flex h-11 w-11 flex-shrink-0 items-center justify-center rounded-full"
            style={{
              background: `conic-gradient(${accent} ${completion * 3.6}deg, #eef0f4 0deg)`,
            }}
            title={`${completion}% complete`}
          >
            <div className="flex h-9 w-9 items-center justify-center rounded-full bg-white">
              <span className="text-[10px] font-bold text-gray-700">{completion}%</span>
            </div>
          </div>
        </div>

        {/* Summary preview */}
        <p className="mt-3 line-clamp-2 text-sm text-gray-600">{resume.summary}</p>

        {/* URL links */}
        {(resume.linkedinUrl || resume.githubUrl || resume.portfolioUrl) && (
          <div className="mt-3 flex flex-wrap gap-1.5">
            {resume.linkedinUrl && (
              <a
                href={resume.linkedinUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-1 rounded-md bg-blue-50 px-2 py-1 text-[11px] font-medium text-blue-600 transition-colors hover:bg-blue-100"
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
                className="inline-flex items-center gap-1 rounded-md bg-gray-100 px-2 py-1 text-[11px] font-medium text-gray-600 transition-colors hover:bg-gray-200"
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
                className="inline-flex items-center gap-1 rounded-md bg-purple-50 px-2 py-1 text-[11px] font-medium text-purple-600 transition-colors hover:bg-purple-100"
              >
                <ExternalLink className="h-3 w-3" />
                Portfolio
              </a>
            )}
          </div>
        )}
      </div>

      {/* Actions footer */}
      <div className="flex items-center justify-between gap-2 border-t border-gray-100 bg-gray-50/60 px-4 py-2.5">
        <button
          onClick={() => onView(resume.id)}
          className="inline-flex items-center gap-1.5 rounded-lg bg-white px-2.5 py-1.5 text-xs font-semibold text-primary-600 shadow-sm ring-1 ring-inset ring-gray-200 transition-all duration-200 hover:-translate-y-0.5 hover:text-primary-700 hover:shadow"
        >
          <Eye className="h-3.5 w-3.5" />
          View
        </button>

        <div className="flex items-center gap-0.5">
          {completion === 100 && (
            <span
              className="mr-1 hidden items-center gap-1 text-[10px] font-semibold text-emerald-600 sm:inline-flex"
              title="All fields completed"
            >
              <CheckCircle2 className="h-3 w-3" />
              Complete
            </span>
          )}
          <button
            onClick={() => onDownload(resume.id)}
            className="rounded-lg p-2 text-gray-400 transition-colors hover:bg-primary-50 hover:text-primary-600"
            aria-label="Choose template and download resume as PDF"
            title="Choose template and download resume as PDF"
          >
            <Download className="h-4 w-4" />
          </button>
          <button
            onClick={() => onEdit(resume.id)}
            className="rounded-lg p-2 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-700"
            aria-label="Edit resume"
            title="Edit resume"
          >
            <Edit3 className="h-4 w-4" />
          </button>
          <button
            onClick={() => onDelete(resume.id)}
            className="rounded-lg p-2 text-gray-400 transition-colors hover:bg-red-50 hover:text-red-500"
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
