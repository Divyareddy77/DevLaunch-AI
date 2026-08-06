/**
 * SummaryAnalysisCard — the professional summary evaluation of the ATS report.
 *
 * Shows the summary score, its strengths and improvement suggestions, and an
 * AI-generated improved version of the summary that the user can copy with
 * one click.
 *
 * @author DevLaunch
 */

import React, { useState } from 'react';
import { FileText, Check, Copy } from 'lucide-react';
import toast from 'react-hot-toast';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { Button } from '../ui/Button';
import { getScoreBadgeVariant } from '../../utils/format';
import { MESSAGES } from '../../constants/messages';
import type { SummaryAnalysis } from '../../types/ai';

interface SummaryAnalysisCardProps {
  /** The professional summary evaluation. */
  analysis: SummaryAnalysis;
}

/** Copies text to the clipboard with a fallback for insecure contexts. */
async function copyText(text: string): Promise<boolean> {
  try {
    await navigator.clipboard.writeText(text);
    return true;
  } catch {
    try {
      const textarea = document.createElement('textarea');
      textarea.value = text;
      textarea.style.position = 'fixed';
      textarea.style.opacity = '0';
      document.body.appendChild(textarea);
      textarea.select();
      const ok = document.execCommand('copy');
      document.body.removeChild(textarea);
      return ok;
    } catch {
      return false;
    }
  }
}

export const SummaryAnalysisCard: React.FC<SummaryAnalysisCardProps> = ({ analysis }) => {
  const [copied, setCopied] = useState(false);

  const handleCopy = async () => {
    if (!analysis.improvedSummary) return;
    const ok = await copyText(analysis.improvedSummary);
    if (ok) {
      setCopied(true);
      toast.success(MESSAGES.SUMMARY_COPIED);
      window.setTimeout(() => setCopied(false), 2000);
    } else {
      toast.error(MESSAGES.SUMMARY_COPY_ERROR);
    }
  };

  return (
    <Card
      header={
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-100">
            <FileText className="h-5 w-5 text-blue-600" />
          </div>
          <div className="flex flex-1 items-center justify-between gap-3">
            <h3 className="text-sm font-semibold text-gray-900">Professional Summary</h3>
            <Badge variant={getScoreBadgeVariant(analysis.score)}>{analysis.score}/100</Badge>
          </div>
        </div>
      }
    >
      <div className="space-y-4">
        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <p className="mb-1.5 text-xs font-semibold uppercase tracking-wide text-gray-500">
              Strengths
            </p>
            <ul className="space-y-1.5">
              {analysis.strengths.map((item, index) => (
                <li key={index} className="flex items-start gap-2 text-sm text-gray-600">
                  <span className="mt-1.5 h-1 w-1 shrink-0 rounded-full bg-emerald-400" />
                  {item}
                </li>
              ))}
            </ul>
          </div>
          <div>
            <p className="mb-1.5 text-xs font-semibold uppercase tracking-wide text-gray-500">
              How to improve
            </p>
            <ul className="space-y-1.5">
              {analysis.suggestions.map((item, index) => (
                <li key={index} className="flex items-start gap-2 text-sm text-gray-600">
                  <span className="mt-1.5 h-1 w-1 shrink-0 rounded-full bg-amber-400" />
                  {item}
                </li>
              ))}
            </ul>
          </div>
        </div>

        {analysis.improvedSummary && (
          <div className="rounded-lg border border-blue-100 bg-blue-50/60 px-4 py-3">
            <div className="mb-2 flex items-center justify-between gap-3">
              <p className="text-xs font-semibold uppercase tracking-wide text-blue-700">
                AI-Improved Version
              </p>
              <Button type="button" variant="outline" size="sm" onClick={handleCopy}>
                {copied ? (
                  <>
                    <Check className="h-3.5 w-3.5 text-emerald-600" />
                    Copied
                  </>
                ) : (
                  <>
                    <Copy className="h-3.5 w-3.5" />
                    Copy
                  </>
                )}
              </Button>
            </div>
            <p className="text-sm leading-relaxed text-gray-700">{analysis.improvedSummary}</p>
          </div>
        )}
      </div>
    </Card>
  );
};
