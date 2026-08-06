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

  // ---- Password reset ----
  FORGOT_PASSWORD_TITLE: 'Forgot your password?',
  FORGOT_PASSWORD_SUBTITLE:
    'Enter your email and we\'ll send you a link to reset your password.',
  FORGOT_PASSWORD_SENT: 'If an account exists, a password reset link has been sent.',
  FORGOT_PASSWORD_ERROR: 'Something went wrong. Please try again.',
  SEND_RESET_LINK: 'Send Reset Link',
  RESET_PASSWORD_TITLE: 'Reset your password',
  RESET_PASSWORD_SUBTITLE: 'Choose a new password for your account.',
  RESET_PASSWORD_SUCCESS_TITLE: 'Password updated successfully.',
  RESET_PASSWORD_SUCCESS_SUBTITLE: 'Login with your new password.',
  RESET_PASSWORD_SUCCESS: 'Your password has been updated successfully.',
  RESET_PASSWORD_ERROR: 'Could not reset your password. Please try again.',
  RESET_PASSWORD_LINK_INVALID: 'Invalid password reset link.',
  RESET_PASSWORD_LINK_EXPIRED: 'Reset link has expired. Please request another password reset.',
  RESET_PASSWORD_LINK_INVALID_SUBTITLE:
    'The reset link you followed is invalid or has already been used.',
  GO_TO_LOGIN: 'Go to Login',
  REQUEST_NEW_LINK: 'Request a new reset link',
  PASSWORD_UPPERCASE: 'Password must contain at least one uppercase letter.',
  PASSWORD_LOWERCASE: 'Password must contain at least one lowercase letter.',
  PASSWORD_NUMBER: 'Password must contain at least one number.',
  PASSWORD_SPECIAL: 'Password must contain at least one special character.',

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

  // ---- Resume ----
  RESUME_DOWNLOAD_SUCCESS: 'Resume downloaded successfully.',
  RESUME_DOWNLOAD_ERROR: 'Failed to download the resume. Please try again.',

  // ---- Feedback ----
  FEEDBACK_SUBMITTED: 'Thank you! Your feedback has been sent.',
  FEEDBACK_SUBMIT_ERROR: 'Failed to send feedback. Please try again.',

  // ---- AI ----
  RESUME_REVIEW_ERROR: 'Failed to review the resume. Please try again.',
  SUMMARY_COPIED: 'Improved summary copied to clipboard.',
  SUMMARY_COPY_ERROR: 'Could not copy the summary. Please copy it manually.',
  MOCK_INTERVIEW_START_ERROR: 'Failed to start the interview. Please try again.',
  MOCK_INTERVIEW_SUBMIT_ERROR: 'Failed to submit your interview. Please try again.',
  INTERVIEW_HISTORY_ERROR: 'Failed to load your interview history. Please try again.',
  INTERVIEW_SUBMITTED: 'Interview submitted! Here is your AI feedback.',
  INTERVIEW_DELETED: 'Interview session deleted from your history.',
  INTERVIEW_DELETE_ERROR: 'Failed to delete the interview session. Please try again.',
  INTERVIEW_RECORDING_UNSUPPORTED:
    'Voice recording is not supported in this browser. Please use Chrome, Microsoft Edge, or Firefox.',
  INTERVIEW_MIC_PERMISSION_DENIED:
    'Microphone access was denied. Please allow microphone access in your browser and try again.',
  INTERVIEW_MIC_NOT_FOUND:
    'No microphone was found. Please connect a microphone and try again.',
  INTERVIEW_MIC_IN_USE:
    'The microphone is being used by another application. Please close it and try again.',
  INTERVIEW_MIC_UNAVAILABLE:
    'Could not access the microphone. Please check your device settings and try again.',
  INTERVIEW_RECORDING_EMPTY:
    'No audio was recorded. Please record your answer again.',
  INTERVIEW_TRANSCRIBING: 'Transcribing your answer…',
  INTERVIEW_TRANSCRIPTION_ERROR:
    'Could not transcribe your recording. Please check your connection and try again.',
  INTERVIEW_TRANSCRIPTION_EMPTY:
    'No speech was detected in your recording. Please try again.',
  INTERVIEW_CAMERA_DENIED:
    'Camera permission was denied — continuing without video. You can disable the camera in setup.',
  INTERVIEW_AUTOSAVED: 'Your answer was auto-saved.',

  // ---- Empty states ----
  NO_RESUMES: 'You haven\'t created any resumes yet. Create your first resume to get started.',
  NO_JOB_APPLICATIONS: 'No job applications tracked yet. Start by adding your first application.',
  NO_STUDY_PLANS: 'No study plans yet. Plan your first study session.',
  NO_GITHUB_CONNECTED: 'Connect your GitHub account to see your analytics.',
  NO_LEETCODE_CONNECTED: 'Enter your LeetCode username to track your progress.',
  NO_INTERVIEW_HISTORY: 'No mock interviews completed yet. Practice with your first interview.',
  NO_NOTIFICATIONS: 'You\'re all caught up! No new notifications.',
  NO_RESULTS: 'No results found. Try adjusting your search or filters.',

  // ---- Account linking ----
  GITHUB_CONNECTED: 'GitHub account connected successfully.',
  GITHUB_DISCONNECTED: 'GitHub account disconnected.',
  LEETCODE_CONNECTED: 'LeetCode account connected successfully.',
  LEETCODE_DISCONNECTED: 'LeetCode account disconnected.',
  ACCOUNT_CONNECT_ERROR: (platform: string) =>
    `Failed to connect your ${platform} account. Please try again.`,
  ACCOUNT_DISCONNECT_ERROR: (platform: string) =>
    `Failed to disconnect your ${platform} account. Please try again.`,
  ACCOUNT_REFRESH_ERROR: (platform: string) =>
    `Failed to refresh your ${platform} data. Please try again.`,

  // ---- Notifications ----
  NOTIFICATIONS_ALL_READ: 'All notifications marked as read.',
  NOTIFICATION_DELETE_SUCCESS: 'Notification has been deleted successfully.',
  NOTIFICATIONS_LOAD_ERROR: 'Failed to load notifications. Please try again.',

  // ---- Validation ----
  REQUIRED_FIELD: 'This field is required.',
  INVALID_EMAIL: 'Please enter a valid email address.',
  PASSWORD_MIN_LENGTH: 'Password must be at least 8 characters long.',
  PASSWORD_MAX_LENGTH: 'Password must be at most 100 characters long.',
  PASSWORDS_MUST_MATCH: 'Passwords do not match.',
  INVALID_URL: 'Please enter a valid URL.',

  // ---- Password strength meter ----
  PASSWORD_STRENGTH_WEAK: 'Weak',
  PASSWORD_STRENGTH_FAIR: 'Fair',
  PASSWORD_STRENGTH_GOOD: 'Good',
  PASSWORD_STRENGTH_STRONG: 'Strong',
} as const;
