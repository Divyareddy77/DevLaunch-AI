/**
 * RepositoryCard — displays a single GitHub repository.
 *
 * Shows the repository name, description, primary language, star and
 * fork counts, creation date, last-updated date, and a link to the
 * repository on GitHub.
 *
 * @author DevLaunch
 */

import React from 'react';
import { BookMarked, ExternalLink, Star, GitFork, Calendar, Clock } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { formatNumber } from '../../utils/format';
import { formatDate, formatRelativeTime } from '../../utils/date';
import type { RepositoryResponse } from '../../types/github';

interface RepositoryCardProps {
  /** The repository data to display. */
  repository: RepositoryResponse;
}

export const RepositoryCard: React.FC<RepositoryCardProps> = ({ repository }) => {
  return (
    <Card
      className="group transition-all hover:border-primary-200 hover:shadow-md"
      padded={false}
    >
      <div className="flex h-full flex-col p-5">
        {/* Header: icon + name + language */}
        <div className="flex items-start justify-between gap-3">
          <div className="flex min-w-0 items-start gap-3">
            <div className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-lg bg-indigo-100 text-indigo-600">
              <BookMarked className="h-5 w-5" />
            </div>
            <div className="min-w-0">
              <a
                href={repository.repositoryUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="block truncate text-sm font-semibold text-gray-900 transition-colors hover:text-indigo-600"
              >
                {repository.name}
              </a>
              {repository.language && (
                <Badge variant="info" size="sm" className="mt-1">
                  {repository.language}
                </Badge>
              )}
            </div>
          </div>

          <a
            href={repository.repositoryUrl}
            target="_blank"
            rel="noopener noreferrer"
            aria-label={`Open ${repository.name} on GitHub`}
            title="Open on GitHub"
            className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600"
          >
            <ExternalLink className="h-4 w-4" />
          </a>
        </div>

        {/* Description */}
        {repository.description && (
          <p className="mt-3 line-clamp-2 flex-1 text-sm text-gray-500">
            {repository.description}
          </p>
        )}

        {/* Meta: stars, forks, dates */}
        <div className="mt-4 flex flex-wrap items-center gap-x-4 gap-y-2 border-t border-gray-100 pt-3 text-xs text-gray-500">
          <span className="inline-flex items-center gap-1">
            <Star className="h-3.5 w-3.5 text-amber-400" />
            {formatNumber(repository.stars)}
          </span>
          <span className="inline-flex items-center gap-1">
            <GitFork className="h-3.5 w-3.5 text-gray-400" />
            {formatNumber(repository.forks)}
          </span>
          <span className="inline-flex items-center gap-1">
            <Calendar className="h-3.5 w-3.5 text-gray-400" />
            {formatDate(repository.createdAt)}
          </span>
          <span className="inline-flex items-center gap-1">
            <Clock className="h-3.5 w-3.5 text-gray-400" />
            {formatRelativeTime(repository.updatedAt)}
          </span>
        </div>
      </div>
    </Card>
  );
};
