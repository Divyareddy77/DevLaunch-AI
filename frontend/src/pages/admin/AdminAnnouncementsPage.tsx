/**
 * AdminAnnouncementsPage — announcement management for administrators.
 *
 * Lists all announcements with a create/edit modal and deletion, using
 * the shared AnnouncementFormModal for the react-hook-form + zod form.
 *
 * @author DevLaunch
 */

import React, { useCallback, useEffect, useState } from 'react';
import { Megaphone, Plus, RefreshCw, Pencil, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';
import { adminService } from '../../services/admin.service';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { ConfirmDeleteModal } from '../../components/admin/ConfirmDeleteModal';
import { AnnouncementFormModal } from '../../components/admin/AnnouncementFormModal';
import { formatDate } from '../../utils/date';
import { getErrorMessage } from '../../utils/error';
import { MESSAGES } from '../../constants/messages';
import type { AdminAnnouncementResponse, AnnouncementRequest } from '../../types/admin';

export const AdminAnnouncementsPage: React.FC = () => {
  const [announcements, setAnnouncements] = useState<AdminAnnouncementResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [formTarget, setFormTarget] = useState<AdminAnnouncementResponse | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  const [deleteTarget, setDeleteTarget] = useState<AdminAnnouncementResponse | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const fetchAnnouncements = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await adminService.getAnnouncements();
      setAnnouncements(data);
    } catch (err: unknown) {
      setError(getErrorMessage(err, MESSAGES.LOAD_ERROR('announcements')));
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchAnnouncements();
  }, [fetchAnnouncements]);

  const openCreate = () => {
    setFormTarget(null);
    setIsFormOpen(true);
  };

  const openEdit = (announcement: AdminAnnouncementResponse) => {
    setFormTarget(announcement);
    setIsFormOpen(true);
  };

  const handleSubmit = async (data: AnnouncementRequest) => {
    setIsSaving(true);
    try {
      if (formTarget) {
        const updated = await adminService.updateAnnouncement(formTarget.id, data);
        setAnnouncements((prev) =>
          prev.map((a) => (a.id === updated.id ? updated : a)),
        );
        toast.success(MESSAGES.UPDATE_SUCCESS('Announcement'));
      } else {
        const created = await adminService.createAnnouncement(data);
        setAnnouncements((prev) => [created, ...prev]);
        toast.success(MESSAGES.CREATE_SUCCESS('Announcement'));
      }
      setIsFormOpen(false);
    } catch (err: unknown) {
      toast.error(getErrorMessage(err, MESSAGES.SAVE_ERROR('announcement')));
    } finally {
      setIsSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setIsDeleting(true);
    try {
      await adminService.deleteAnnouncement(deleteTarget.id);
      toast.success(MESSAGES.DELETE_SUCCESS('Announcement'));
      setAnnouncements((prev) => prev.filter((a) => a.id !== deleteTarget.id));
      setDeleteTarget(null);
    } catch (err: unknown) {
      toast.error(getErrorMessage(err, MESSAGES.SAVE_ERROR('announcement deletion')));
    } finally {
      setIsDeleting(false);
    }
  };

  if (isLoading) {
    return <LoadingScreen />;
  }

  return (
    <div className="mx-auto max-w-4xl">
      {/* Header */}
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900 sm:text-2xl">Announcements</h1>
          <p className="mt-1 text-sm text-gray-500">
            Publish updates visible across the platform.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={fetchAnnouncements}>
            <RefreshCw className="h-4 w-4" />
            Refresh
          </Button>
          <Button size="sm" onClick={openCreate}>
            <Plus className="h-4 w-4" />
            New Announcement
          </Button>
        </div>
      </div>

      {error && (
        <div className="mb-4">
          <ErrorMessage message={error} onRetry={fetchAnnouncements} />
        </div>
      )}

      {/* Announcement list */}
      {announcements.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-2xl border-2 border-dashed border-gray-200 bg-white px-6 py-16 text-center">
          <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-indigo-100">
            <Megaphone className="h-8 w-8 text-indigo-600" />
          </div>
          <h2 className="mb-2 text-xl font-semibold text-gray-900">No Announcements</h2>
          <p className="mb-6 max-w-sm text-sm text-gray-500">
            Share platform updates with your users by creating the first announcement.
          </p>
          <Button onClick={openCreate}>
            <Plus className="h-4 w-4" />
            Create Announcement
          </Button>
        </div>
      ) : (
        <ul className="space-y-4">
          {announcements.map((announcement) => (
            <li
              key={announcement.id}
              className="rounded-xl border border-gray-200 bg-white p-5 shadow-sm transition-shadow hover:shadow-md"
            >
              <div className="flex items-start justify-between gap-4">
                <div className="min-w-0">
                  <div className="mb-1 flex flex-wrap items-center gap-2">
                    <h3 className="text-base font-semibold text-gray-900">
                      {announcement.title}
                    </h3>
                    <Badge variant={announcement.isActive ? 'success' : 'default'} size="sm">
                      {announcement.isActive ? 'Active' : 'Draft'}
                    </Badge>
                  </div>
                  <p className="whitespace-pre-wrap text-sm text-gray-600">
                    {announcement.content}
                  </p>
                  <p className="mt-3 text-xs text-gray-400">
                    By {announcement.createdByName} · {formatDate(announcement.createdAt)}
                  </p>
                </div>
                <div className="flex flex-shrink-0 items-center gap-1">
                  <button
                    onClick={() => openEdit(announcement)}
                    title="Edit announcement"
                    className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-700"
                  >
                    <Pencil className="h-4 w-4" />
                  </button>
                  <button
                    onClick={() => setDeleteTarget(announcement)}
                    title="Delete announcement"
                    className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-red-50 hover:text-red-600"
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>
              </div>
            </li>
          ))}
        </ul>
      )}

      {/* Create/edit modal */}
      <AnnouncementFormModal
        isOpen={isFormOpen}
        announcement={formTarget}
        isSubmitting={isSaving}
        onClose={() => setIsFormOpen(false)}
        onSubmit={handleSubmit}
      />

      {/* Delete confirmation modal */}
      <ConfirmDeleteModal
        isOpen={deleteTarget !== null}
        resource="announcement"
        targetName={deleteTarget?.title}
        isLoading={isDeleting}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
      />
    </div>
  );
};
