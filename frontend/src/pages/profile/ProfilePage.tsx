/**
 * ProfilePage — the authenticated user's account and settings page.
 *
 * Displays the user's identity (avatar, name, email, phone, role) and
 * provides forms for updating the editable profile fields and changing
 * the password. All data comes from the backend UserController.
 *
 * @see backend/src/main/java/com/devlaunch/controller/UserController.java
 * @author DevLaunch
 */

import React, { useCallback, useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { userService } from '../../services/user.service';
import { feedbackService } from '../../services/feedback.service';
import { ProfileCard } from '../../components/profile/ProfileCard';
import { ConnectedAccountsCard } from '../../components/profile/ConnectedAccountsCard';
import { ProfileForm } from '../../components/profile/ProfileForm';
import { ChangePasswordForm, type ChangePasswordFormValues } from '../../components/profile/ChangePasswordForm';
import { AchievementProfileCard } from '../../components/achievements/AchievementProfileCard';
import { FeedbackForm } from '../../components/admin/FeedbackForm';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { PageHeader } from '../../components/shared/PageHeader';
import { getErrorMessage } from '../../utils/error';
import { MESSAGES } from '../../constants/messages';
import type { UserResponse, UpdateUserRequest } from '../../types/user';

export const ProfilePage: React.FC = () => {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isSavingProfile, setIsSavingProfile] = useState(false);
  const [isChangingPassword, setIsChangingPassword] = useState(false);
  const [isSendingFeedback, setIsSendingFeedback] = useState(false);

  const fetchProfile = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const data = await userService.getCurrentUser();
      setUser(data);
    } catch (err: unknown) {
      setError(getErrorMessage(err, MESSAGES.LOAD_ERROR('profile')));
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchProfile();
  }, [fetchProfile]);

  /** PUT /api/users/me — save the editable profile fields. */
  const handleUpdateProfile = async (data: UpdateUserRequest) => {
    setIsSavingProfile(true);

    try {
      const updated = await userService.updateCurrentUser(data);
      setUser(updated);
      toast.success(MESSAGES.PROFILE_UPDATED);
    } catch (err: unknown) {
      toast.error(getErrorMessage(err, MESSAGES.PROFILE_UPDATE_ERROR));
    } finally {
      setIsSavingProfile(false);
    }
  };

  /** POST /api/feedback — submit platform feedback. */
  const handleSendFeedback = async (message: string): Promise<boolean> => {
    setIsSendingFeedback(true);

    try {
      await feedbackService.submit({ message });
      toast.success(MESSAGES.FEEDBACK_SUBMITTED);
      return true;
    } catch (err: unknown) {
      toast.error(getErrorMessage(err, MESSAGES.FEEDBACK_SUBMIT_ERROR));
      return false;
    } finally {
      setIsSendingFeedback(false);
    }
  };

  /** PUT /api/users/change-password — change the user's password. */
  const handleChangePassword = async (
    data: ChangePasswordFormValues,
  ): Promise<boolean> => {
    setIsChangingPassword(true);

    try {
      await userService.changePassword({
        currentPassword: data.currentPassword,
        newPassword: data.newPassword,
      });
      toast.success(MESSAGES.PASSWORD_CHANGED);
      return true;
    } catch (err: unknown) {
      toast.error(getErrorMessage(err, MESSAGES.PASSWORD_CHANGE_ERROR));
      return false;
    } finally {
      setIsChangingPassword(false);
    }
  };

  // ---- Content area state machine ----
  let content: React.ReactNode;

  if (isLoading) {
    content = <LoadingScreen />;
  } else if (error) {
    content = (
      <div className="flex min-h-[40vh] items-center justify-center">
        <ErrorMessage message={error} onRetry={fetchProfile} />
      </div>
    );
  } else if (!user) {
    // The backend always returns the authenticated user or an error, so
    // reaching this branch is unexpected. Render nothing rather than
    // inventing an unsupported empty state.
    content = null;
  } else {
    content = (
      <div className="grid gap-6 lg:grid-cols-5">
        {/* Identity, connected accounts, gamification */}
        <div className="space-y-6 lg:col-span-2">
          <div className="animate-fade-in-up">
            <ProfileCard user={user} />
          </div>
          <div className="animate-fade-in-up" style={{ animationDelay: '70ms' }}>
            <ConnectedAccountsCard user={user} />
          </div>
          <div className="animate-fade-in-up" style={{ animationDelay: '140ms' }}>
            <AchievementProfileCard />
          </div>
        </div>

        {/* Editable forms */}
        <div className="space-y-6 lg:col-span-3">
          <div className="animate-fade-in-up" style={{ animationDelay: '90ms' }}>
            <ProfileForm
              initialData={user}
              isSubmitting={isSavingProfile}
              onSubmit={handleUpdateProfile}
            />
          </div>
          <div className="animate-fade-in-up" style={{ animationDelay: '160ms' }}>
            <ChangePasswordForm
              isSubmitting={isChangingPassword}
              onSubmit={handleChangePassword}
            />
          </div>
          <div className="animate-fade-in-up" style={{ animationDelay: '230ms' }}>
            <FeedbackForm
              isSubmitting={isSendingFeedback}
              onSubmit={handleSendFeedback}
            />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="animate-page-enter mx-auto max-w-7xl">
      {/* Page header */}
      <PageHeader
        title="Profile"
        description="Manage your account details, connected accounts, and password."
      />

      {/* Content */}
      {content}
    </div>
  );
};
