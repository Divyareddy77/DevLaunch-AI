/**
 * AuthLayout — a centered layout for authentication pages.
 *
 * Provides a full-screen gradient background with a centered card
 * containing the page content rendered via Outlet.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Outlet } from 'react-router-dom';
import { APP } from '../constants/app';

export const AuthLayout: React.FC = () => {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-gradient-to-br from-primary-50 via-white to-indigo-100 px-4">
      <div className="mb-8 flex flex-col items-center">
        <div className="mb-3 flex h-14 w-14 items-center justify-center rounded-2xl bg-primary-600 shadow-lg shadow-primary-200">
          <span className="text-2xl font-bold text-white">D</span>
        </div>
        <h1 className="text-xl font-bold text-gray-900">{APP.NAME}</h1>
        <p className="text-sm text-gray-500">{APP.TAGLINE}</p>
      </div>

      <div className="w-full max-w-md rounded-2xl border border-gray-200 bg-white p-8 shadow-xl">
        <Outlet />
      </div>
    </div>
  );
};
