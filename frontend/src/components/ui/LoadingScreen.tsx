/**
 * LoadingScreen — a full-page loading overlay used while the
 * application initialises or during top-level route transitions.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Spinner } from './Spinner';
import { APP } from '../../constants/app';

export const LoadingScreen: React.FC = () => {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-gray-50">
      <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-primary-100">
        <div className="text-2xl font-bold text-primary-600">D</div>
      </div>
      <h1 className="mb-1 text-xl font-bold text-gray-900">{APP.NAME}</h1>
      <p className="mb-8 text-sm text-gray-500">{APP.TAGLINE}</p>
      <Spinner size="lg" label="Loading…" />
    </div>
  );
};
