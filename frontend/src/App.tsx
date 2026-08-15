/**
 * App — the root application component.
 *
 * Configures all global providers:
 * - Toaster (react-hot-toast for notifications)
 * - AuthProvider (authentication context)
 * - ThemeProvider (light/dark theme context)
 * - BrowserRouter with route definitions
 *
 * @author DevLaunch
 */

import React from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import { routes } from './routes';
import { APP } from './constants/app';

const router = createBrowserRouter(routes);

const App: React.FC = () => {
  return (
    <ThemeProvider>
      <AuthProvider>
        <RouterProvider router={router} />
        <Toaster
          position="top-right"
          toastOptions={{
            duration: APP.TOAST_DURATION_MS,
            style: {
              borderRadius: '12px',
              padding: '12px 16px',
              fontSize: '14px',
            },
            success: {
              iconTheme: {
                primary: '#10b981',
                secondary: '#fff',
              },
            },
            error: {
              iconTheme: {
                primary: '#ef4444',
                secondary: '#fff',
              },
            },
          }}
        />
      </AuthProvider>
    </ThemeProvider>
  );
};

export default App;
