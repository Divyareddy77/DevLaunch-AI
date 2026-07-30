/**
 * Theme context for light/dark mode support.
 *
 * Provides the current theme value and a toggle function to all
 * child components. The theme preference is persisted in localStorage
 * so it survives page reloads.
 *
 * Dark mode implementation is prepared here but will be fully
 * implemented in a future phase. Currently, only the light theme
 * is active.
 *
 * Usage:
 *   const { theme, toggleTheme } = useTheme();
 *
 * @author DevLaunch
 */

import React, { createContext, useContext, useState, useCallback, useEffect, useMemo } from 'react';
import { STORAGE_KEYS } from '../constants/storage';

/** Supported theme variants. */
export type Theme = 'light' | 'dark';

/** Shape of the theme context value. */
interface ThemeContextValue {
  /** The current active theme. */
  theme: Theme;
  /** Toggles between light and dark themes. */
  toggleTheme: () => void;
  /** Sets the theme to a specific value. */
  setTheme: (theme: Theme) => void;
}

const ThemeContext = createContext<ThemeContextValue | undefined>(undefined);

/** Reads the initial theme from localStorage, falling back to 'light'. */
function getInitialTheme(): Theme {
  if (typeof window === 'undefined') return 'light';
  const stored = localStorage.getItem(STORAGE_KEYS.THEME_DARK_MODE);
  if (stored === 'true') return 'dark';
  return 'light';
}

/** Props for the ThemeProvider component. */
interface ThemeProviderProps {
  children: React.ReactNode;
}

/**
 * Provides the theme context to the component tree.
 * Persists preference changes to localStorage and applies a
 * data-theme attribute to the document root for CSS targeting.
 */
export const ThemeProvider: React.FC<ThemeProviderProps> = ({ children }) => {
  const [theme, setThemeState] = useState<Theme>(getInitialTheme);

  // Persist theme changes to localStorage and update the data attribute
  useEffect(() => {
    localStorage.setItem(STORAGE_KEYS.THEME_DARK_MODE, String(theme === 'dark'));
    document.documentElement.setAttribute('data-theme', theme);
  }, [theme]);

  const toggleTheme = useCallback(() => {
    setThemeState((prev) => (prev === 'light' ? 'dark' : 'light'));
  }, []);

  const setTheme = useCallback((newTheme: Theme) => {
    setThemeState(newTheme);
  }, []);

  const value = useMemo<ThemeContextValue>(
    () => ({ theme, toggleTheme, setTheme }),
    [theme, toggleTheme, setTheme],
  );

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
};

/**
 * Hook to access the current theme and toggle function.
 * Must be used within a ThemeProvider.
 */
export function useTheme(): ThemeContextValue {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
}
