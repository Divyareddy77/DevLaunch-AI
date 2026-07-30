/**
 * ResumeReviewPage — AI-powered resume review and analysis.
 *
 * Allows users to submit their resume content and receive
 * AI-driven suggestions for improvement, missing keywords,
 * and an overall score.
 *
 * This page is a scaffold for the future AI module. It will be
 * fully implemented once the backend AI endpoints are available.
 *
 * @author DevLaunch
 */

import React from 'react';
import { FileSearch } from 'lucide-react';

export const ResumeReviewPage: React.FC = () => {
  return (
    <div className="flex flex-col items-center justify-center py-16">
      <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-emerald-100">
        <FileSearch className="h-8 w-8 text-emerald-600" />
      </div>
      <h1 className="mb-2 text-2xl font-bold text-gray-900">AI Resume Review</h1>
      <p className="max-w-md text-center text-gray-500">
        Get AI-powered insights on your resume with suggestions for
        improvement, missing keywords, and an overall readiness score.
      </p>
      <p className="mt-6 text-sm text-gray-400 italic">
        This feature is coming soon.
      </p>
    </div>
  );
};
