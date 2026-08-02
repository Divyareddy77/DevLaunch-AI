/**
 * ResumeReviewForm — resume selector and target-role input used to
 * submit a resume for AI-powered review.
 *
 * Lets the user pick one of their existing resumes, optionally provide
 * a target job role, and trigger the review. Mirrors the form patterns
 * used across the Resume Builder and Study Planner modules.
 *
 * @author DevLaunch
 */

import React, { useState } from 'react';
import { Sparkles, FileText, Target } from 'lucide-react';
import { Card } from '../ui/Card';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import type { ResumeResponse } from '../../types/resume';
import type { ResumeReviewRequest } from '../../types/ai';

interface ResumeReviewFormProps {
  /** The authenticated user's resumes to choose from. */
  resumes: ResumeResponse[];
  /** Whether a review request is currently in flight. */
  submitting: boolean;
  /** Called with the review payload when the form is submitted. */
  onSubmit: (data: ResumeReviewRequest) => void;
}

export const ResumeReviewForm: React.FC<ResumeReviewFormProps> = ({
  resumes,
  submitting,
  onSubmit,
}) => {
  const [selectedResumeId, setSelectedResumeId] = useState<string>(
    resumes[0] ? String(resumes[0].id) : '',
  );
  const [targetRole, setTargetRole] = useState('');

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    if (!selectedResumeId) return;

    onSubmit({
      resumeId: Number(selectedResumeId),
      targetRole: targetRole.trim() || undefined,
    });
  };

  return (
    <Card>
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <div className="grid gap-4 md:grid-cols-2">
          <div>
            <label
              htmlFor="resume-select"
              className="mb-1.5 block text-sm font-medium text-gray-700"
            >
              Select Resume
            </label>
            <div className="relative">
              <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-gray-400">
                <FileText className="h-4 w-4" />
              </div>
              <select
                id="resume-select"
                value={selectedResumeId}
                onChange={(event) => setSelectedResumeId(event.target.value)}
                disabled={submitting}
                className="
                  block w-full appearance-none rounded-lg border border-gray-300
                  bg-white py-2 pl-10 pr-8 text-sm text-gray-900 transition-colors
                  focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500
                  disabled:cursor-not-allowed disabled:opacity-50
                "
              >
                {resumes.map((resume) => (
                  <option key={resume.id} value={resume.id}>
                    {resume.headline}
                  </option>
                ))}
              </select>
              <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-3 text-gray-400">
                <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                </svg>
              </div>
            </div>
          </div>

          <Input
            id="target-role"
            label="Target Role (optional)"
            placeholder="e.g. Java Backend Developer"
            value={targetRole}
            onChange={(event) => setTargetRole(event.target.value)}
            disabled={submitting}
            leftIcon={<Target className="h-4 w-4" />}
            hint="Tailors the review towards a specific job role."
          />
        </div>

        <div>
          <Button
            type="submit"
            loading={submitting}
            disabled={!selectedResumeId}
          >
            <Sparkles className="h-4 w-4" />
            {submitting ? 'Reviewing…' : 'Review Resume'}
          </Button>
        </div>
      </form>
    </Card>
  );
};
