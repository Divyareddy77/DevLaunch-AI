/**
 * CompanyLogo — displays a company logo for a job application card.
 *
 * Resolves the company favicon from the Google favicon service using the
 * company website (or a domain guessed from the company name). When no
 * favicon is available the existing Building2 placeholder icon is shown.
 *
 * @author DevLaunch
 */

import React, { useState } from 'react';
import { Building2 } from 'lucide-react';
import { extractDomain } from '../../utils/format';

interface CompanyLogoProps {
  /** The company name, used for the placeholder and domain fallback. */
  companyName: string;
  /** Optional company website or job URL used to resolve the favicon. */
  website?: string | null;
  /** Optional size variant. */
  size?: 'sm' | 'md' | 'lg';
  /** Optional additional CSS classes. */
  className?: string;
}

const sizeStyles = {
  sm: 'h-8 w-8 rounded-md',
  md: 'h-10 w-10 rounded-lg',
  lg: 'h-12 w-12 rounded-xl',
};

const iconStyles = {
  sm: 'h-4 w-4',
  md: 'h-5 w-5',
  lg: 'h-6 w-6',
};

/** Builds a best-effort domain from the company name (e.g. "Google" → "google.com"). */
function guessDomain(companyName: string): string {
  const slug = companyName
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '')
    .replace(/^(the|my)\s*/, '');
  return slug ? `${slug}.com` : '';
}

export const CompanyLogo: React.FC<CompanyLogoProps> = ({
  companyName,
  website,
  size = 'md',
  className = '',
}) => {
  const [failed, setFailed] = useState(false);

  const domain = extractDomain(website) || guessDomain(companyName);

  // No resolvable domain → show the placeholder immediately.
  if (failed || !domain) {
    return (
      <div
        className={`flex flex-shrink-0 items-center justify-center bg-indigo-100 text-indigo-600 ${sizeStyles[size]} ${className}`}
      >
        <Building2 className={iconStyles[size]} />
      </div>
    );
  }

  return (
    <div
      className={`relative flex flex-shrink-0 items-center justify-center overflow-hidden bg-white ring-1 ring-gray-200 ${sizeStyles[size]} ${className}`}
    >
      <img
        src={`https://www.google.com/s2/favicons?domain=${encodeURIComponent(domain)}&sz=128`}
        alt={`${companyName} logo`}
        loading="lazy"
        onError={() => setFailed(true)}
        className="h-full w-full object-contain p-1"
      />
    </div>
  );
};
