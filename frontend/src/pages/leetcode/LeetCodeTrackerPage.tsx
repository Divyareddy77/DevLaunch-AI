/**
 * LeetCodeTrackerPage — track public LeetCode progress.
 *
 * Lets the user enter any LeetCode username and view their solved
 * problems, difficulty breakdown, and global ranking — all fetched
 * live from the LeetCode GraphQL API via the backend LeetCodeController.
 *
 * @see backend/src/main/java/com/devlaunch/controller/LeetCodeController.java
 * @author DevLaunch
 */

import React, { useCallback, useState } from 'react';
import { Search, Code2 } from 'lucide-react';
import { leetCodeService } from '../../services/leetcode.service';
import { LeetCodeProfileCard } from '../../components/leetcode/LeetCodeProfileCard';
import { LeetCodeStatsCard } from '../../components/leetcode/LeetCodeStatsCard';
import { DifficultyProgress } from '../../components/leetcode/DifficultyProgress';
import { EmptyLeetCodeState } from '../../components/leetcode/EmptyLeetCodeState';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { getErrorMessage } from '../../utils/error';
import { MESSAGES } from '../../constants/messages';
import type { LeetCodeProfileResponse } from '../../types/leetcode';

/** Fallback message when an error carries no usable backend message. */
const DEFAULT_ERROR_MESSAGE = MESSAGES.LOAD_ERROR('LeetCode data');

export const LeetCodeTrackerPage: React.FC = () => {
  const [searchInput, setSearchInput] = useState('');
  const [searchedUsername, setSearchedUsername] = useState<string | null>(null);
  const [profile, setProfile] = useState<LeetCodeProfileResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /** Fetches the LeetCode profile for the given username. */
  const fetchLeetCode = useCallback(async (username: string) => {
    const trimmed = username.trim();
    if (!trimmed) return;

    setIsLoading(true);
    setError(null);

    try {
      const data = await leetCodeService.getProfile(trimmed);
      setProfile(data);
      setSearchedUsername(trimmed);
    } catch (err: unknown) {
      setProfile(null);
      setError(getErrorMessage(err, DEFAULT_ERROR_MESSAGE));
    } finally {
      setIsLoading(false);
    }
  }, []);

  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    fetchLeetCode(searchInput);
  };

  const handleRetry = () => {
    // Re-run with the current input value — a failed re-search may have
    // left `searchedUsername` pointing at the previously loaded username.
    fetchLeetCode(searchInput);
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
      <EmptyLeetCodeState
        action={
          <Button variant="outline" size="sm" onClick={() => fetchLeetCode('leetcode')}>
            Try <span className="font-medium">leetcode</span>
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
        <LeetCodeProfileCard profile={profile} />

        {/* Statistics cards */}
        <div className="mt-6">
          <LeetCodeStatsCard
            totalSolved={profile.totalSolved}
            easySolved={profile.easySolved}
            mediumSolved={profile.mediumSolved}
            hardSolved={profile.hardSolved}
          />
        </div>

        {/* Difficulty analysis */}
        <div className="mt-8">
          <DifficultyProgress
            totalSolved={profile.totalSolved}
            easySolved={profile.easySolved}
            mediumSolved={profile.mediumSolved}
            hardSolved={profile.hardSolved}
          />
        </div>
      </>
    );
  }

  return (
    <div className="mx-auto max-w-6xl">
      {/* Page header */}
      <div className="mb-6">
        <h1 className="text-xl font-bold text-gray-900 sm:text-2xl">LeetCode Tracker</h1>
        <p className="mt-1 text-sm text-gray-500">
          Enter any LeetCode username to view their solved problems, difficulty breakdown, and
          global ranking.
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
            placeholder="Enter a LeetCode username (e.g. leetcode)"
            leftIcon={<Code2 className="h-4 w-4" />}
            aria-label="LeetCode username"
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
