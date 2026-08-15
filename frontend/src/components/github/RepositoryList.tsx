/**
 * RepositoryList — a responsive grid of GitHub repository cards.
 *
 * Renders a section heading with a repository count badge followed
 * by a grid of RepositoryCard components.
 *
 * @author DevLaunch
 */

import React from 'react';
import { BookMarked } from 'lucide-react';
import { Badge } from '../ui/Badge';
import { RepositoryCard } from './RepositoryCard';
import type { RepositoryResponse } from '../../types/github';

interface RepositoryListProps {
  /** The repositories to display in the grid. */
  repositories: RepositoryResponse[];
}

export const RepositoryList: React.FC<RepositoryListProps> = ({ repositories }) => {
  return (
    <section>
      <div className="mb-4 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <BookMarked className="h-5 w-5 text-indigo-500" />
          <h2 className="text-lg font-semibold text-gray-900">Repositories</h2>
        </div>
        <Badge variant="default">
          {repositories.length} {repositories.length === 1 ? 'repository' : 'repositories'}
        </Badge>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        {repositories.map((repository) => (
          <RepositoryCard key={repository.name} repository={repository} />
        ))}
      </div>
    </section>
  );
};
