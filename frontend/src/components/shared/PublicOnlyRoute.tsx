/**
 * PublicOnlyRoute — a route guard that redirects already-authenticated
 * users away from public pages (login, register) to the dashboard.
 *
 * Wraps public routes that should only be accessible to unauthenticated
 * users. If the user is already logged in, they are redirected to
 * the dashboard.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { ROUTES } from '../../constants/routes';
import { LoadingScreen } from '../ui/LoadingScreen';

export const PublicOnlyRoute: React.FC = () => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return <LoadingScreen />;
  }

  if (isAuthenticated) {
    return <Navigate to={ROUTES.DASHBOARD} replace />;
  }

  return <Outlet />;
};
