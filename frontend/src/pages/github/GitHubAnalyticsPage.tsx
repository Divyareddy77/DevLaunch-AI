/**
 * GitHubAnalyticsPage — explore public GitHub profiles.
 *
 * Lets the user enter any GitHub username and view the associated
 * public profile, statistics, repositories, and language usage —
 * all fetched live from the GitHub REST API via the backend
 * GitHubController.
 *
 * @see backend/src/main/java/com/devlaunch/controller/GitHubController.java
 * @author DevLaunch
 */

import React, { useCallback, useState } from 'react';
import { Search, Github, BookOpen } from 'lucide-react';
import { githubService } from '../../services/github.service';
import { GitHubProfileCard } from '../../components/github/GitHubProfileCard';
import { GitHubStatsCard } from '../../components/github/GitHubStatsCard';
import { RepositoryList } from '../../components/github/RepositoryList';
import { LanguageChart } from '../../components/github/LanguageChart';
import { EmptyGitHubState } from '../../components/github/EmptyGitHubState';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { getErrorMessage } from '../../utils/error';
import { MESSAGES } from '../../constants/messages';
import type {
  GitHubProfileResponse,
  RepositoryResponse,
  LanguageStatisticsResponse,
} from '../../types/github';

/** Fallback message when an error carries no usable backend message. */
const DEFAULT_ERROR_MESSAGE = MESSAGES.LOAD_ERROR('GitHub data');

export const GitHubAnalyticsPage: React.FC = () => {
  const [searchInput, setSearchInput] = useState('');
  const [searchedUsername, setSearchedUsername] = useState<string | null>(null);
  const [profile, setProfile] = useState<GitHubProfileResponse | null>(null);
  const [repositories, setRepositories] = useState<RepositoryResponse[]>([]);
  const [languages, setLanguages] = useState<LanguageStatisticsResponse[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /** Fetches profile, repositories, and language statistics in parallel. */
  const fetchGitHub = useCallback(async (username: string) => {
    const trimmed = username.trim();
    if (!trimmed) return;

    setIsLoading(true);
    setError(null);

    try {
      const [profileData, repositoryData, languageData] = await Promise.all([
        githubService.getProfile(trimmed),
        githubService.getRepositories(trimmed),
        githubService.getLanguageStatistics(trimmed),
      ]);

      setProfile(profileData);
      setRepositories(repositoryData);
      setLanguages(languageData);
      setSearchedUsername(trimmed);
    } catch (err: unknown) {
      setProfile(null);
      setRepositories([]);
      setLanguages([]);
      setError(getErrorMessage(err, DEFAULT_ERROR_MESSAGE));
    } finally {
      setIsLoading(false);
    }
  }, []);

  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    fetchGitHub(searchInput);
  };

  const handleRetry = () => {
    // Re-run with the current input value — a failed re-search may have
    // left `searchedUsername` pointing at the previously loaded username.
    fetchGitHub(searchInput);
  };

  // ---- Content area state machine ----
  let content: React.ReactNode;

  if (isLoading) {
    content = <LoadingScreen />;
  } else if (error) {
    content = (
      <div className="flex min-h-[40vh] items-center justify-center">
        <ErrorMessage message={error} onRetry={handleRetry} />
      </div>
    );
  } else if (!searchedUsername || !profile) {
    // Empty state — no username searched yet
    content = (
      <EmptyGitHubState
        action={
          <Button variant="outline" size="sm" onClick={() => fetchGitHub('octocat')}>
            Try <span className="font-medium">octocat</span>
          </Button>
        }
      />
    );
  } else {
    content = (
      <>
        {/* Results banner */}
        <p className="mb-4 text-xs text-gray-500">
          Showing results for{' '}
          <span className="font-medium text-gray-700">@{searchedUsername}</span>
        </p>

        {/* Profile section */}
        <GitHubProfileCard profile={profile} />

        {/* Statistics cards */}
        <div className="mt-6">
          <GitHubStatsCard
            repositories={profile.publicRepositories}
            followers={profile.followers}
            following={profile.following}
            topLanguage={languages[0]?.language ?? null}
          />
        </div>

        {/* Repository section */}
        <div className="mt-8">
          {repositories.length === 0 ? (
            <EmptyGitHubState
              icon={<BookOpen className="h-8 w-8 text-gray-400" />}
              title="No Repositories Found"
              description={`@${searchedUsername} has no public repositories to display.`}
            />
          ) : (
            <RepositoryList repositories={repositories} />
          )}
        </div>

        {/* Language analysis */}
        <div className="mt-8">
          <LanguageChart languages={languages} />
        </div>
      </>
    );
  }

  return (
    <div className="mx-auto max-w-6xl">
      {/* Page header */}
      <div className="mb-6">
        <h1 className="text-xl font-bold text-gray-900 sm:text-2xl">GitHub Analytics</h1>
        <p className="mt-1 text-sm text-gray-500">
          Enter any GitHub username to view their public profile, repositories, and language usage.
        </p>
      </div>

      {/* Username search */}
      <form
        onSubmit={handleSubmit}
        className="mb-8 flex flex-col gap-3 sm:flex-row sm:items-start"
      >
        <div className="w-full max-w-xl">
          <Input
            value={searchInput}
            onChange={(event) => setSearchInput(event.target.value)}
            placeholder="Enter a GitHub username (e.g. octocat)"
            leftIcon={<Github className="h-4 w-4" />}
            aria-label="GitHub username"
          />
        </div>
        <Button type="submit" loading={isLoading} disabled={!searchInput.trim()}>
          <Search className="h-4 w-4" />
          Search
        </Button>
      </form>

      {/* Content */}
      {content}
    </div>
  );
};
