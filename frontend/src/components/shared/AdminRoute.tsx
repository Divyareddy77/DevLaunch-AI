/**
 * AdminRoute — a route guard that restricts access to the admin panel.
 *
 * Only users with the ADMIN role may render the wrapped admin routes.
 * Non-admin (but authenticated) users are redirected to the dashboard,
 * matching the backend's 403 protection on /api/admin/**.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { ROUTES } from '../../constants/routes';
import { LoadingScreen } from '../ui/LoadingScreen';

export const AdminRoute: React.FC = () => {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return <LoadingScreen />;
  }

  if (user?.role !== 'ADMIN') {
    return <Navigate to={ROUTES.DASHBOARD} replace />;
  }

  return <Outlet />;
};
