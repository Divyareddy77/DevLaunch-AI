/**
 * Centralised route configuration.
 *
 * Defines all application routes using React Router DOM v6.
 * Public routes (login/register) use AuthLayout.
 * Protected routes use ProtectedRoute + DashboardLayout.
 *
 * @author DevLaunch
 */

import { type RouteObject, Navigate } from 'react-router-dom';
import { ROUTES } from '../constants/routes';
import { AuthLayout } from '../layouts/AuthLayout';
import { DashboardLayout } from '../layouts/DashboardLayout';
import { ProtectedRoute } from '../components/shared/ProtectedRoute';
import { PublicOnlyRoute } from '../components/shared/PublicOnlyRoute';
import { LoginPage } from '../pages/auth/LoginPage';
import { RegisterPage } from '../pages/auth/RegisterPage';
import { DashboardPage } from '../pages/dashboard/DashboardPage';
import { ResumePage } from '../pages/resume/ResumePage';
import { CreateResumePage } from '../pages/resume/CreateResumePage';
import { EditResumePage } from '../pages/resume/EditResumePage';
import { JobApplicationsPage } from '../pages/job-applications/JobApplicationsPage';
import { CreateJobApplicationPage } from '../pages/job-applications/CreateJobApplicationPage';
import { EditJobApplicationPage } from '../pages/job-applications/EditJobApplicationPage';
import { StudyPlannerPage } from '../pages/study-planner/StudyPlannerPage';
import { CreateStudyPlannerPage } from '../pages/study-planner/CreateStudyPlannerPage';
import { EditStudyPlannerPage } from '../pages/study-planner/EditStudyPlannerPage';
import { GitHubAnalyticsPage } from '../pages/github/GitHubAnalyticsPage';
import { LeetCodeTrackerPage } from '../pages/leetcode/LeetCodeTrackerPage';
import { ProfilePage } from '../pages/profile/ProfilePage';
import { ResumeReviewPage } from '../pages/ai/ResumeReviewPage';
import { MockInterviewPage } from '../pages/ai/MockInterviewPage';
import { NotFoundPage } from '../pages/NotFoundPage';

export const routes: RouteObject[] = [
  // ---- Public redirect ----
  {
    path: '/',
    element: <Navigate to={ROUTES.DASHBOARD} replace />,
  },

  // ---- Auth routes (public, only for unauthenticated users) ----
  {
    element: <PublicOnlyRoute />,
    children: [
      {
        element: <AuthLayout />,
        children: [
          {
            path: ROUTES.LOGIN,
            element: <LoginPage />,
          },
          {
            path: ROUTES.REGISTER,
            element: <RegisterPage />,
          },
        ],
      },
    ],
  },

  // ---- Protected routes (require authentication) ----
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <DashboardLayout />,
        children: [
          {
            path: ROUTES.DASHBOARD,
            element: <DashboardPage />,
          },

          // ---- Resume Builder ----
          {
            path: ROUTES.RESUME_LIST,
            element: <ResumePage />,
          },
          {
            path: ROUTES.RESUME_CREATE,
            element: <CreateResumePage />,
          },
          {
            path: ROUTES.RESUME_DETAIL(':id'),
            element: <EditResumePage />,
          },
          {
            path: ROUTES.RESUME_EDIT(':id'),
            element: <EditResumePage />,
          },

          // ---- Job Application Tracker ----
          {
            path: ROUTES.JOB_APPLICATION_LIST,
            element: <JobApplicationsPage />,
          },
          {
            path: ROUTES.JOB_APPLICATION_CREATE,
            element: <CreateJobApplicationPage />,
          },
          {
            path: ROUTES.JOB_APPLICATION_EDIT(':id'),
            element: <EditJobApplicationPage />,
          },

          // ---- Study Planner ----
          {
            path: ROUTES.STUDY_PLANNER_LIST,
            element: <StudyPlannerPage />,
          },
          {
            path: ROUTES.STUDY_PLANNER_CREATE,
            element: <CreateStudyPlannerPage />,
          },
          {
            path: ROUTES.STUDY_PLANNER_EDIT(':id'),
            element: <EditStudyPlannerPage />,
          },

          // ---- GitHub Analytics ----
          {
            path: ROUTES.GITHUB_ANALYTICS,
            element: <GitHubAnalyticsPage />,
          },

          // ---- LeetCode Tracker ----
          {
            path: ROUTES.LEETCODE_TRACKER,
            element: <LeetCodeTrackerPage />,
          },

          // ---- Profile ----
          {
            path: ROUTES.PROFILE,
            element: <ProfilePage />,
          },

          // ---- AI ----
          {
            path: ROUTES.RESUME_REVIEW,
            element: <ResumeReviewPage />,
          },
          {
            path: ROUTES.MOCK_INTERVIEW,
            element: <MockInterviewPage />,
          },

          // Future module routes will be added here:
          // Admin
        ],
      },
    ],
  },

  // ---- 404 ----
  {
    path: '*',
    element: <NotFoundPage />,
  },
];
