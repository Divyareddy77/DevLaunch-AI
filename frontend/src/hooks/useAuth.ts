/**
 * useAuth — convenience hook for accessing authentication state
 * and actions from any component.
 *
 * Must be used within an AuthProvider.
 *
 * @author DevLaunch
 */

import { useContext } from 'react';
import { AuthContext, type AuthContextValue } from '../context/AuthContext';

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
