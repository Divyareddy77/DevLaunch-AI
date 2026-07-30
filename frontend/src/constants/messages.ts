/**
 * User-facing message constants.
 *
 * All UI strings including success messages, error messages,
 * confirmations, and labels are centralised here. This makes
 * it easy to review copy, maintain consistency, and prepare
 * for internationalisation in the future.
 *
 * @author DevLaunch
 */

export const MESSAGES = {
  // ---- Authentication ----
  LOGIN_SUCCESS: 'Welcome back! You have been logged in successfully.',
  LOGIN_ERROR: 'Invalid email or password. Please try again.',
  REGISTER_SUCCESS: 'Your account has been created successfully.',
  REGISTER_ERROR: 'Registration failed. Please check your details and try again.',
  LOGOUT_CONFIRM: 'Are you sure you want to log out?',
  SESSION_EXPIRED: 'Your session has expired. Please log in again.',

  // ---- Profile ----
  PROFILE_UPDATED: 'Your profile has been updated successfully.',
  PROFILE_UPDATE_ERROR: 'Failed to update profile. Please try again.',
  PASSWORD_CHANGED: 'Your password has been changed successfully.',
  PASSWORD_CHANGE_ERROR: 'Failed to change password. Please check your current password.',

  // ---- CRUD operations ----
  CREATE_SUCCESS: (resource: string) => `${resource} has been created successfully.`,
  UPDATE_SUCCESS: (resource: string) => `${resource} has been updated successfully.`,
  DELETE_SUCCESS: (resource: string) => `${resource} has been deleted successfully.`,
  DELETE_CONFIRM: (resource: string) =>
    `Are you sure you want to delete this ${resource}? This action cannot be undone.`,
  LOAD_ERROR: (resource: string) => `Failed to load ${resource}. Please try again.`,
  SAVE_ERROR: (resource: string) => `Failed to save ${resource}. Please try again.`,

  // ---- Empty states ----
  NO_RESUMES: 'You haven\'t created any resumes yet. Create your first resume to get started.',
  NO_JOB_APPLICATIONS: 'No job applications tracked yet. Start by adding your first application.',
  NO_STUDY_PLANS: 'No study plans yet. Plan your first study session.',
  NO_GITHUB_CONNECTED: 'Connect your GitHub account to see your analytics.',
  NO_LEETCODE_CONNECTED: 'Enter your LeetCode username to track your progress.',
  NO_INTERVIEW_HISTORY: 'No mock interviews completed yet. Practice with your first interview.',
  NO_NOTIFICATIONS: 'You\'re all caught up! No new notifications.',
  NO_RESULTS: 'No results found. Try adjusting your search or filters.',

  // ---- Validation ----
  REQUIRED_FIELD: 'This field is required.',
  INVALID_EMAIL: 'Please enter a valid email address.',
  PASSWORD_MIN_LENGTH: 'Password must be at least 8 characters long.',
  PASSWORDS_MUST_MATCH: 'Passwords do not match.',
  INVALID_URL: 'Please enter a valid URL.',
} as const;
