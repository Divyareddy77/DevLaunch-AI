/**
 * ResumeTemplatePickerModal — lets the user pick a PDF layout template
 * before downloading a resume.
 *
 * Shows all available templates as large cards with a CSS preview
 * mockup that closely mirrors the generated PDF layout, plus a
 * highlight on the selected one. The selection is persisted to
 * localStorage so it is pre-selected on the next visit. Only the
 * template grid scrolls — the header and footer (Cancel / Download)
 * stay fixed, and the resume content is never touched.
 *
 * @author DevLaunch
 */

import React, { useEffect, useState } from 'react';
import { Check, Download } from 'lucide-react';
import { Modal } from '../ui/Modal';
import { Button } from '../ui/Button';
import { RESUME_PDF_TEMPLATES } from '../../constants/resumeTemplates';
import {
  getSavedResumePdfTemplate,
  saveResumePdfTemplate,
} from '../../utils/resumeTemplate';
import type { ResumePdfTemplateValue } from '../../types/resume';

interface ResumeTemplatePickerModalProps {
  /** Whether the modal is visible. */
  isOpen: boolean;
  /** Callback fired when the modal requests to close. */
  onClose: () => void;
  /** Resume headline shown for context. */
  resumeName: string;
  /** Whether a download is currently in progress. */
  isDownloading: boolean;
  /** Called with the chosen template when the user confirms the download. */
  onDownload: (template: ResumePdfTemplateValue) => void;
}

export const ResumeTemplatePickerModal: React.FC<ResumeTemplatePickerModalProps> = ({
  isOpen,
  onClose,
  resumeName,
  isDownloading,
  onDownload,
}) => {
  const [selected, setSelected] = useState<ResumePdfTemplateValue>(
    () => getSavedResumePdfTemplate(),
  );

  // Restore the saved preference whenever the picker opens.
  useEffect(() => {
    if (isOpen) {
      setSelected(getSavedResumePdfTemplate());
    }
  }, [isOpen]);

  const handleSelect = (value: ResumePdfTemplateValue) => {
    setSelected(value);
    saveResumePdfTemplate(value);
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Choose a PDF Template"
      closeOnBackdrop={false}
      className="max-w-[1060px]"
    >
      <div className="flex max-h-[74vh] flex-col gap-4">
        {/* Fixed intro */}
        <p className="shrink-0 text-sm text-gray-500">
          {resumeName ? (
            <>
              Pick a layout for{' '}
              <span className="font-medium text-gray-700">{resumeName}</span>.
            </>
          ) : (
            <>Pick a layout for your resume.</>
          )}{' '}
          Your content stays exactly the same — only the presentation changes.
        </p>

        {/* Scrollable template grid */}
        <div className="min-h-0 flex-1 overflow-y-auto pr-1">
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            {RESUME_PDF_TEMPLATES.map((template) => {
              const isSelected = selected === template.value;
              return (
                <button
                  key={template.value}
                  type="button"
                  onClick={() => handleSelect(template.value)}
                  aria-pressed={isSelected}
                  className={`
                    group flex flex-col rounded-xl border-2 bg-white p-3 text-left transition-all
                    ${
                      isSelected
                        ? 'border-primary-500 shadow-md ring-2 ring-primary-200'
                        : 'border-gray-200 hover:border-primary-200 hover:shadow-sm'
                    }
                  `}
                >
                  <div
                    className={`
                      h-52 overflow-hidden rounded-lg border transition-colors sm:h-64 md:h-72
                      ${isSelected ? 'border-primary-200' : 'border-gray-200 group-hover:border-gray-300'}
                    `}
                  >
                    <TemplatePreview value={template.value} />
                  </div>
                  <div className="mt-3 flex items-center justify-between gap-2">
                    <span className="text-sm font-semibold text-gray-900">
                      {template.label}
                    </span>
                    {isSelected && (
                      <span className="flex h-5 w-5 items-center justify-center rounded-full bg-primary-600 text-white">
                        <Check className="h-3 w-3" />
                      </span>
                    )}
                  </div>
                  <p className="mt-1 text-xs leading-relaxed text-gray-500">
                    {template.description}
                  </p>
                </button>
              );
            })}
          </div>
        </div>

        {/* Fixed footer */}
        <div className="flex shrink-0 items-center justify-between gap-3 border-t border-gray-100 pt-4">
          <p className="hidden text-xs text-gray-400 sm:block">
            Your selection is saved for next time.
          </p>
          <div className="ml-auto flex items-center gap-2">
            <Button variant="ghost" onClick={onClose} disabled={isDownloading}>
              Cancel
            </Button>
            <Button onClick={() => onDownload(selected)} loading={isDownloading}>
              <Download className="h-4 w-4" />
              Download Resume
            </Button>
          </div>
        </div>
      </div>
    </Modal>
  );
};

/**
 * A CSS-only mockup of the template's layout, drawn at the size of its
 * container. Each variant mirrors the real PDF: header arrangement,
 * section headings, separators, contact grid, and highlights.
 */
const TemplatePreview: React.FC<{ value: ResumePdfTemplateValue }> = ({
  value,
}) => {
  switch (value) {
    case 'MODERN_BLUE':
      return (
        <div className="flex h-full flex-col bg-white p-4">
          <div className="flex flex-col items-center gap-1.5">
            <div className="h-2 w-1/2 rounded-sm bg-gray-900" />
            <div className="h-1 w-1/3 rounded-sm bg-gray-400" />
          </div>
          <div className="mt-2 grid grid-cols-2 gap-1.5">
            <div className="h-1 rounded-sm bg-blue-100" />
            <div className="h-1 rounded-sm bg-blue-100" />
            <div className="h-1 rounded-sm bg-blue-100" />
            <div className="h-1 rounded-sm bg-blue-100" />
          </div>
          <div className="mt-3 h-px w-full bg-blue-400" />
          <div className="mt-3 flex flex-col gap-1.5">
            <div className="h-1 w-6 rounded-sm bg-blue-600" />
            <div className="h-1.5 w-1/4 rounded-sm bg-blue-600" />
            <div className="mt-1 h-1 w-full rounded-sm bg-gray-200" />
            <div className="h-1 w-11/12 rounded-sm bg-gray-200" />
            <div className="h-1 w-4/5 rounded-sm bg-gray-200" />
          </div>
          <div className="mt-4 flex flex-col gap-1.5">
            <div className="h-1 w-6 rounded-sm bg-blue-600" />
            <div className="h-1.5 w-1/4 rounded-sm bg-blue-600" />
            <div className="mt-1 h-1 w-full rounded-sm bg-gray-200" />
            <div className="h-1 w-2/3 rounded-sm bg-gray-200" />
          </div>
        </div>
      );

    case 'MINIMAL':
      return (
        <div className="flex h-full flex-col bg-white p-5">
          <div className="flex flex-col gap-1">
            <div className="h-1.5 w-2/5 rounded-sm bg-gray-800" />
            <div className="h-1 w-3/5 rounded-sm bg-gray-300" />
          </div>
          <div className="mt-4 h-px w-full bg-gray-200" />
          <div className="mt-5 flex flex-col gap-2">
            <div className="h-1 w-1/5 rounded-sm bg-gray-500" />
            <div className="mt-2 h-1 w-full rounded-sm bg-gray-100" />
            <div className="h-1 w-4/5 rounded-sm bg-gray-100" />
          </div>
          <div className="mt-8 flex flex-col gap-2">
            <div className="h-1 w-1/5 rounded-sm bg-gray-500" />
            <div className="mt-2 h-1 w-full rounded-sm bg-gray-100" />
            <div className="h-1 w-2/3 rounded-sm bg-gray-100" />
          </div>
        </div>
      );

    case 'EXECUTIVE':
      return (
        <div className="flex h-full flex-col bg-white px-5 pt-5">
          <div className="flex flex-col items-center gap-2">
            <div className="h-3 w-3/4 rounded-sm bg-slate-900" />
            <div className="h-1 w-1/3 rounded-sm bg-slate-400" />
            <div className="h-1 w-1/2 rounded-sm bg-slate-300" />
          </div>
          <div className="mt-4 h-[2px] w-full bg-slate-900" />
          <div className="mt-4 flex flex-col gap-2">
            <div className="h-2 w-1/4 rounded-sm bg-slate-900" />
            <div className="mt-1 h-1 w-full rounded-sm bg-gray-200" />
            <div className="h-1 w-11/12 rounded-sm bg-gray-200" />
            <div className="h-1 w-3/4 rounded-sm bg-gray-200" />
          </div>
          <div className="mt-5 flex flex-col gap-2">
            <div className="h-2 w-1/4 rounded-sm bg-slate-900" />
            <div className="mt-1 h-1 w-full rounded-sm bg-gray-200" />
            <div className="h-1 w-2/3 rounded-sm bg-gray-200" />
          </div>
        </div>
      );

    case 'CREATIVE':
      return (
        <div className="flex h-full flex-col bg-white">
          <div className="flex flex-col items-center justify-center gap-1.5 bg-indigo-600 px-4 py-3">
            <div className="h-2 w-1/2 rounded-sm bg-white" />
            <div className="h-1 w-2/3 rounded-sm bg-indigo-200" />
          </div>
          <div className="flex flex-1 flex-col gap-4 p-4">
            <div className="flex flex-col gap-1.5">
              <div className="h-1 w-6 rounded-sm bg-indigo-600" />
              <div className="h-1.5 w-1/4 rounded-sm bg-indigo-600" />
            </div>
            <div className="flex flex-col gap-1 rounded-md border border-indigo-100 bg-indigo-50 p-2">
              <div className="h-1 w-full rounded-sm bg-indigo-200" />
              <div className="h-1 w-3/4 rounded-sm bg-indigo-200" />
            </div>
            <div className="flex flex-col gap-1.5">
              <div className="h-1 w-6 rounded-sm bg-indigo-600" />
              <div className="h-1.5 w-1/4 rounded-sm bg-indigo-600" />
              <div className="mt-1 h-1 w-full rounded-sm bg-gray-200" />
              <div className="h-1 w-11/12 rounded-sm bg-gray-200" />
            </div>
          </div>
        </div>
      );

    case 'CLASSIC_PROFESSIONAL':
    default:
      return (
        <div className="flex h-full flex-col bg-white p-4">
          <div className="flex flex-col items-center gap-1.5">
            <div className="h-2 w-1/2 rounded-sm bg-gray-900" />
            <div className="h-1 w-1/3 rounded-sm bg-gray-400" />
            <div className="h-1 w-3/5 rounded-sm bg-gray-300" />
          </div>
          <div className="mt-3 h-px w-full bg-gray-400" />
          <div className="mt-3 flex flex-col gap-1.5">
            <div className="h-1.5 w-1/4 rounded-sm bg-gray-800" />
            <div className="h-px w-full bg-gray-300" />
            <div className="mt-1 h-1 w-full rounded-sm bg-gray-200" />
            <div className="h-1 w-11/12 rounded-sm bg-gray-200" />
            <div className="h-1 w-4/5 rounded-sm bg-gray-200" />
          </div>
          <div className="mt-4 flex flex-col gap-1.5">
            <div className="h-1.5 w-1/4 rounded-sm bg-gray-800" />
            <div className="h-px w-full bg-gray-300" />
            <div className="mt-1 h-1 w-full rounded-sm bg-gray-200" />
            <div className="h-1 w-2/3 rounded-sm bg-gray-200" />
          </div>
        </div>
      );
  }
};
