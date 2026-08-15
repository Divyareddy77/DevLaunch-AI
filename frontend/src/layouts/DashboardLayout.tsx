/**
 * DashboardLayout — the main application shell with a responsive
 * sidebar, top navigation bar, and content area.
 *
 * The sidebar renders navigation items dynamically from the
 * navigation.ts configuration and the user's role.
 *
 * @author DevLaunch
 */

import React, { useState } from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import {
  Menu,
  X,
  LogOut,
  User,
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { getNavigationItems } from '../utils/navigation';
import { NotificationProvider } from '../context/NotificationContext';
import { NotificationBell } from '../components/notifications/NotificationBell';
import { APP } from '../constants/app';
import { ROUTES } from '../constants/routes';

export const DashboardLayout: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const navItems = getNavigationItems(user?.role);

  const handleLogout = () => {
    logout();
    navigate(ROUTES.LOGIN);
  };

  return (
    <NotificationProvider>
      <div className="flex min-h-screen bg-gray-50">
      {/* Mobile sidebar overlay */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/50 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside
        className={`
          fixed inset-y-0 left-0 z-50 flex w-64 flex-col border-r border-gray-200 bg-white transition-transform duration-300 lg:static lg:translate-x-0
          ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}
        `}
      >
        {/* Sidebar header */}
        <div className="flex h-16 items-center justify-between border-b border-gray-100 px-4">
          <div className="flex items-center gap-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-primary-500 to-indigo-600 shadow-sm shadow-primary-200">
              <span className="text-sm font-bold text-white">D</span>
            </div>
            <span className="text-sm font-bold text-gray-900">{APP.NAME}</span>
          </div>
          <button
            onClick={() => setSidebarOpen(false)}
            className="rounded-lg p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600 lg:hidden"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Navigation */}
        <nav className="flex-1 overflow-y-auto px-3 py-4">
          <ul className="space-y-1">
            {navItems.map((item) => (
              <li key={item.path}>
                <NavLink
                  to={item.path}
                  end={item.path === ROUTES.DASHBOARD}
                  onClick={() => setSidebarOpen(false)}
                  className={({ isActive }) =>
                    isActive ? 'sidebar-link-active' : 'sidebar-link'
                  }
                >
                  <item.icon className="h-5 w-5 flex-shrink-0" />
                  <span>{item.label}</span>
                </NavLink>
              </li>
            ))}
          </ul>
        </nav>

        {/* User section at the bottom */}
        <div className="border-t border-gray-100 p-3">
          <button
            onClick={handleLogout}
            className="sidebar-link w-full text-red-600 hover:bg-red-50 hover:text-red-700"
          >
            <LogOut className="h-5 w-5" />
            <span>Logout</span>
          </button>
        </div>
      </aside>

      {/* Main content area */}
      <div className="flex flex-1 flex-col">
        {/* Top bar */}
        <header className="sticky top-0 z-20 flex h-16 items-center justify-between border-b border-gray-200 bg-white/90 px-4 shadow-[0_1px_2px_rgba(16,24,40,0.04)] backdrop-blur lg:px-6">
          <div className="flex items-center gap-3">
            <button
              onClick={() => setSidebarOpen(true)}
              className="rounded-lg p-1.5 text-gray-500 hover:bg-gray-100 hover:text-gray-700 lg:hidden"
              aria-label="Open sidebar"
            >
              <Menu className="h-6 w-6" />
            </button>

            {/* Breadcrumb or page title area — pages can inject via a portal later */}
            <div />
          </div>

          {/* Notifications bell + user avatar dropdown */}
          <div className="flex items-center gap-3">
            <NotificationBell />
            <div className="hidden text-right sm:block">
              <p className="text-sm font-medium text-gray-700">
                {user?.firstName} {user?.lastName}
              </p>
              <p className="text-xs text-gray-400 capitalize">{user?.role?.toLowerCase()}</p>
            </div>
            <button
              onClick={() => navigate(ROUTES.PROFILE)}
              className="flex h-9 w-9 items-center justify-center rounded-full bg-primary-100 text-primary-600 transition-colors hover:bg-primary-200"
            >
              <User className="h-5 w-5" />
            </button>
          </div>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto p-4 lg:p-6 xl:p-8">
          <Outlet />
        </main>
      </div>
    </div>
    </NotificationProvider>
  );
};
