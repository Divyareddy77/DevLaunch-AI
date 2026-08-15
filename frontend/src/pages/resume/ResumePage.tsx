/**
 * ResumePage — the main resume list page.
 *
 * Displays all resumes for the authenticated user as cards with
 * edit and delete actions. Provides a "Create Resume" button and
 * handles loading, empty, and error states.
 *
 * @author DevLaunch
 */

import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, FileText, RefreshCw } from 'lucide-react';
import toast from 'react-hot-toast';
import { resumeService } from '../../services/resume.service';
import { useResumeDownload } from '../../hooks/useResumeDownload';
import { ResumeCard } from '../../components/resume/ResumeCard';
import { ResumeTemplatePickerModal } from '../../components/resume/ResumeTemplatePickerModal';
import { Button } from '../../components/ui/Button';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { PageHeader } from '../../components/shared/PageHeader';
import { Modal } from '../../components/ui/Modal';
import { ROUTES } from '../../constants/routes';
import { MESSAGES } from '../../constants/messages';
import type { ResumeResponse } from '../../types/resume';

export const ResumePage: React.FC = () => {
  const navigate = useNavigate();
  const [resumes, setResumes] = useState<ResumeResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<number | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [downloadTarget, setDownloadTarget] = useState<number | null>(null);
  const { download: handleDownload, isDownloading } = useResumeDownload();

  const fetchResumes = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await resumeService.getAllResumes();
      setResumes(data);
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : MESSAGES.LOAD_ERROR('resumes');
      setError(message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchResumes();
  }, [fetchResumes]);

  const handleDelete = async () => {
    if (deleteTarget === null) return;
    setIsDeleting(true);
    try {
      await resumeService.deleteResume(deleteTarget);
      toast.success(MESSAGES.DELETE_SUCCESS('Resume'));
      setResumes((prev) => prev.filter((r) => r.id !== deleteTarget));
      setDeleteTarget(null);
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : MESSAGES.SAVE_ERROR('resume');
      toast.error(message);
    } finally {
      setIsDeleting(false);
    }
  };

  // ─── Loading state ───
  if (isLoading) {
    return <LoadingScreen />;
  }

  // ─── Error state ───
  if (error) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh]">
        <ErrorMessage message={error} onRetry={fetchResumes} />
      </div>
    );
  }

  // ─── Empty state ───
  if (resumes.length === 0) {
    return (
      <div className="animate-page-enter mx-auto max-w-3xl">
        <PageHeader
          title="My Resumes"
          description="Build professional resumes that stand out to recruiters and ATS systems."
          actions={
            <Button onClick={() => navigate(ROUTES.RESUME_CREATE)}>
              <Plus className="h-4 w-4" />
              Create Resume
            </Button>
          }
        />
        <div className="flex flex-col items-center justify-center rounded-2xl border-2 border-dashed border-gray-200 bg-white px-6 py-16 text-center transition-colors hover:border-primary-200">
          <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-primary-100">
            <FileText className="h-8 w-8 text-primary-600" />
          </div>
          <h2 className="mb-2 text-xl font-semibold text-gray-900">
            No Resumes Yet
          </h2>
          <p className="mb-6 max-w-sm text-sm text-gray-500">
            {MESSAGES.NO_RESUMES}
          </p>
          <Button onClick={() => navigate(ROUTES.RESUME_CREATE)}>
            <Plus className="h-4 w-4" />
            Create Your First Resume
          </Button>
        </div>
      </div>
    );
  }

  // ─── Data state ───
  return (
    <div className="animate-page-enter mx-auto max-w-6xl">
      {/* Page header */}
      <PageHeader
        title="My Resumes"
        description={`Manage your professional resumes. You have ${resumes.length} ${
          resumes.length === 1 ? 'resume' : 'resumes'
        } ready to refine and download.`}
        actions={
          <>
            <Button variant="outline" size="sm" onClick={fetchResumes}>
              <RefreshCw className="h-4 w-4" />
              Refresh
            </Button>
            <Button onClick={() => navigate(ROUTES.RESUME_CREATE)}>
              <Plus className="h-4 w-4" />
              Create Resume
            </Button>
          </>
        }
      />

      {/* Resume grid */}
      <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
        {resumes.map((resume) => (
          <ResumeCard
            key={resume.id}
            resume={resume}
            onEdit={(id) => navigate(ROUTES.RESUME_EDIT(id))}
            onDelete={(id) => setDeleteTarget(id)}
            onView={(id) => navigate(ROUTES.RESUME_EDIT(id))}
            onDownload={(id) => setDownloadTarget(id)}
          />
        ))}
      </div>

      {/* PDF template picker modal */}
      <ResumeTemplatePickerModal
        isOpen={downloadTarget !== null}
        onClose={() => setDownloadTarget(null)}
        resumeName={
          resumes.find((r) => r.id === downloadTarget)?.headline ?? ''
        }
        isDownloading={isDownloading}
        onDownload={async (template) => {
          if (downloadTarget === null) return;
          const success = await handleDownload(downloadTarget, template);
          if (success) setDownloadTarget(null);
        }}
      />

      {/* Delete confirmation modal */}
      <Modal
        isOpen={deleteTarget !== null}
        onClose={() => setDeleteTarget(null)}
        title="Delete Resume"
        closeOnBackdrop={false}
      >
        <p className="text-sm text-gray-600">
          {MESSAGES.DELETE_CONFIRM('resume')}
        </p>
        <div className="mt-6 flex justify-end gap-2">
          <Button
            variant="ghost"
            onClick={() => setDeleteTarget(null)}
            disabled={isDeleting}
          >
            Cancel
          </Button>
          <Button
            variant="danger"
            onClick={handleDelete}
            loading={isDeleting}
          >
            Delete
          </Button>
        </div>
      </Modal>
    </div>
  );
};
