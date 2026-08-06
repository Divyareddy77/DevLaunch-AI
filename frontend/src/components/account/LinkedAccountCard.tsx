/**
 * LinkedAccountCard — the "Connected X Account" section shown above the
 * GitHub Analytics and LeetCode Tracker search boxes.
 *
 * Lets the authenticated user connect, refresh, and disconnect their
 * linked GitHub/LeetCode username. Connection only saves the username —
 * no OAuth is performed. Search behaviour is completely independent:
 * searching any username never affects the linked account.
 *
 * @author DevLaunch
 */

import React, { useState, type ReactNode } from 'react';
import { Check, Link2, Link2Off, RefreshCw, X } from 'lucide-react';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';

interface LinkedAccountCardProps {
  /** The platform label, e.g. "GitHub" or "LeetCode". */
  platform: 'GitHub' | 'LeetCode';
  /** Platform icon shown in the section header. */
  icon: ReactNode;
  /** The currently linked username, or null if none is connected. */
  username: string | null;
  /** Whether a connect request is in flight. */
  isConnecting?: boolean;
  /** Whether a refresh request is in flight. */
  isRefreshing?: boolean;
  /** Whether a disconnect request is in flight. */
  isDisconnecting?: boolean;
  /** Saves the entered username as the linked account. */
  onConnect: (username: string) => void | Promise<void>;
  /** Re-fetches the linked account's latest data. */
  onRefresh: () => void | Promise<void>;
  /** Removes the linked username and clears cached data. */
  onDisconnect: () => void | Promise<void>;
}

export const LinkedAccountCard: React.FC<LinkedAccountCardProps> = ({
  platform,
  icon,
  username,
  isConnecting = false,
  isRefreshing = false,
  isDisconnecting = false,
  onConnect,
  onRefresh,
  onDisconnect,
}) => {
  const [isConnectingMode, setIsConnectingMode] = useState(false);
  const [usernameInput, setUsernameInput] = useState('');

  const isBusy = isConnecting || isRefreshing || isDisconnecting;

  const handleConnectSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const trimmed = usernameInput.trim();
    if (!trimmed || isConnecting) return;
    try {
      await onConnect(trimmed);
    } finally {
      // Always reset the form — even if the caller rejects, the page
      // surfaces the error via a toast and shows the disconnected state.
      setUsernameInput('');
      setIsConnectingMode(false);
    }
  };

  const handleCancelConnect = () => {
    setUsernameInput('');
    setIsConnectingMode(false);
  };

  return (
    <div className="mb-8 rounded-xl border border-gray-200 bg-white shadow-sm">
      {/* Section header */}
      <div className="flex items-center gap-2 border-b border-gray-100 px-5 py-4">
        <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-gray-100 text-gray-600">
          {icon}
        </span>
        <h2 className="text-sm font-semibold text-gray-800">
          Connected {platform} Account
        </h2>
        {username && (
          <span className="ml-auto inline-flex items-center gap-1 rounded-full bg-emerald-100 px-2.5 py-0.5 text-xs font-medium text-emerald-700">
            <Check className="h-3 w-3" />
            Connected
          </span>
        )}
      </div>

      <div className="px-5 py-4">
        {username ? (
          /* ---- Connected state ---- */
          <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-full bg-gray-100 text-gray-500">
                {icon}
              </div>
              <div>
                <p className="text-sm font-semibold text-gray-900">
                  {username}
                  <span className="ml-1.5 inline-flex h-4 w-4 items-center justify-center rounded-full bg-emerald-500 text-white">
                    <Check className="h-2.5 w-2.5" />
                  </span>
                </p>
                <p className="text-xs text-gray-500">
                  Your {platform} dashboard data is linked to this account.
                </p>
              </div>
            </div>

            <div className="flex flex-wrap items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                loading={isRefreshing}
                disabled={isBusy && !isRefreshing}
                onClick={onRefresh}
              >
                <RefreshCw className="h-3.5 w-3.5" />
                Refresh
              </Button>
              <Button
                variant="danger"
                size="sm"
                loading={isDisconnecting}
                disabled={isBusy && !isDisconnecting}
                onClick={onDisconnect}
              >
                <Link2Off className="h-3.5 w-3.5" />
                Disconnect
              </Button>
            </div>
          </div>
        ) : (
          /* ---- Not connected state ---- */
          <div>
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <p className="text-sm font-medium text-gray-700">No account connected</p>
                <p className="mt-0.5 text-xs text-gray-500">
                  Link your {platform} username to see your statistics on the dashboard.
                </p>
              </div>

              {!isConnectingMode && (
                <Button
                  variant="primary"
                  size="sm"
                  disabled={isBusy}
                  onClick={() => setIsConnectingMode(true)}
                >
                  <Link2 className="h-3.5 w-3.5" />
                  Connect {platform}
                </Button>
              )}
            </div>

            {isConnectingMode && (
              <form
                onSubmit={handleConnectSubmit}
                className="mt-4 flex flex-col gap-3 rounded-lg border border-gray-100 bg-gray-50 p-4 sm:flex-row"
              >
                <div className="w-full sm:max-w-sm">
                  <Input
                    value={usernameInput}
                    onChange={(event) => setUsernameInput(event.target.value)}
                    placeholder={`Enter your ${platform} username`}
                    leftIcon={icon}
                    aria-label={`${platform} username`}
                    autoFocus
                  />
                </div>
                <div className="flex items-center gap-2">
                  <Button
                    type="submit"
                    size="sm"
                    loading={isConnecting}
                    disabled={!usernameInput.trim()}
                  >
                    <Link2 className="h-3.5 w-3.5" />
                    Connect
                  </Button>
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    disabled={isConnecting}
                    onClick={handleCancelConnect}
                  >
                    <X className="h-3.5 w-3.5" />
                    Cancel
                  </Button>
                </div>
              </form>
            )}
          </div>
        )}

      </div>
    </div>
  );
};
