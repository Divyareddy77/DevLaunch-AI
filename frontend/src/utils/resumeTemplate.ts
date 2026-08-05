/**
 * Persistence helpers for the user's preferred resume PDF template.
 *
 * The selection is stored in localStorage so the same template stays
 * pre-selected on the user's next visit, without needing a backend
 * schema change.
 *
 * @author DevLaunch
 */

import { STORAGE_KEYS } from '../constants/storage';
import { RESUME_PDF_TEMPLATES } from '../constants/resumeTemplates';
import type { ResumePdfTemplateValue } from '../types/resume';

/**
 * Returns the user's saved PDF template preference, falling back to the
 * first catalog entry (Classic Professional) when nothing is saved or
 * the saved value is no longer known.
 *
 * @returns the saved (or default) template value
 */
export function getSavedResumePdfTemplate(): ResumePdfTemplateValue {
  const saved = localStorage.getItem(STORAGE_KEYS.RESUME_PDF_TEMPLATE);
  const known = RESUME_PDF_TEMPLATES.some((t) => t.value === saved);
  return known ? (saved as ResumePdfTemplateValue) : RESUME_PDF_TEMPLATES[0].value;
}

/**
 * Persists the user's PDF template preference to localStorage.
 *
 * @param template the template value to save
 */
export function saveResumePdfTemplate(template: ResumePdfTemplateValue): void {
  localStorage.setItem(STORAGE_KEYS.RESUME_PDF_TEMPLATE, template);
}
