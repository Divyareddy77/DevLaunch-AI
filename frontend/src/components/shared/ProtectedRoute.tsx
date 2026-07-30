/**
 * ProtectedRoute — a route guard that redirects unauthenticated
 * users to the login page.
 *
 * Wraps child routes that require authentication. If the user
 * is not authenticated, they are redirected to /auth/login with
 * the current location saved in state so they can be returned
 * after login.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { ROUTES } from '../../constants/routes';
import { LoadingScreen } from '../ui/LoadingScreen';

export const ProtectedRoute: React.FC = () => {
  const { isAuthenticated, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return <LoadingScreen />;
  }

  if (!isAuthenticated) {
    return <Navigate to={ROUTES.LOGIN} state={{ from: location }} replace />;
  }

  return <Outlet />;
};
