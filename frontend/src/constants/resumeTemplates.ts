/**
 * Available PDF layout templates for resume downloads.
 *
 * The template value is sent to the backend as the `?template=` query
 * parameter on the resume PDF endpoint. The UI-facing metadata here
 * powers the template picker cards and their preview thumbnails.
 *
 * @see backend/src/main/java/com/devlaunch/service/ResumePdfTemplate.java
 * @author DevLaunch
 */

import type { ResumePdfTemplateValue } from '../types/resume';

export interface ResumePdfTemplateOption {
  /** Backend enum value (sent as ?template=...). */
  value: ResumePdfTemplateValue;
  /** Display name shown on the template card. */
  label: string;
  /** Short description of the template's look. */
  description: string;
}

export const RESUME_PDF_TEMPLATES: readonly ResumePdfTemplateOption[] = [
  {
    value: 'CLASSIC_PROFESSIONAL',
    label: 'Classic Professional',
    description: 'A traditional black & white resume — the safest ATS choice.',
  },
  {
    value: 'MODERN_BLUE',
    label: 'Modern Blue',
    description: 'Modern blue accents with a two-column contact header.',
  },
  {
    value: 'MINIMAL',
    label: 'Minimal',
    description: 'Airy, understated layout with thin separators.',
  },
  {
    value: 'EXECUTIVE',
    label: 'Executive',
    description: 'A bold, premium look for senior professionals.',
  },
  {
    value: 'CREATIVE',
    label: 'Creative',
    description: 'A colourful header band with highlighted skills.',
  },
] as const;
