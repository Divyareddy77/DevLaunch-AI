/**
 * AttachmentsModal — quick-access dialog for an application's documents.
 *
 * Loads the attachments when opened and renders the shared
 * AttachmentsPanel, so the same component backs both the quick action and
 * the detail tab.
 *
 * @author DevLaunch
 */

import React, { useEffect, useState } from 'react';
import { Modal } from '../ui/Modal';
import { AttachmentsPanel } from './AttachmentsPanel';
import { jobApplicationService } from '../../services/job-application.service';
import type {
  ApplicationAttachment,
  AttachmentCategory,
} from '../../types/job-application';

interface AttachmentsModalProps {
  /** Whether the modal is visible. */
  isOpen: boolean;
  /** Closes the modal. */
  onClose: () => void;
  /** The application these attachments belong to. */
  applicationId: number;
  /** Fired after a successful upload/delete so parent state can refresh. */
  onChange?: () => void;
}

export const AttachmentsModal: React.FC<AttachmentsModalProps> = ({
  isOpen,
  onClose,
  applicationId,
  onChange,
}) => {
  const [attachments, setAttachments] = useState<ApplicationAttachment[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!isOpen) return;
    setIsLoading(true);
    jobApplicationService
      .getAttachments(applicationId)
      .then(setAttachments)
      .catch(() => setAttachments([]))
      .finally(() => setIsLoading(false));
  }, [isOpen, applicationId]);

  const handleUpload = async (file: File, category: AttachmentCategory) => {
    const attachment = await jobApplicationService.uploadAttachment(
      applicationId,
      file,
      category,
    );
    setAttachments((prev) => [attachment, ...prev]);
    onChange?.();
  };

  const handleDelete = async (attachment: ApplicationAttachment) => {
    await jobApplicationService.deleteAttachment(applicationId, attachment.id);
    setAttachments((prev) => prev.filter((a) => a.id !== attachment.id));
    onChange?.();
  };

  const handleDownload = async (attachment: ApplicationAttachment) => {
    await jobApplicationService.downloadAttachment(
      applicationId,
      attachment.id,
      attachment.fileName,
    );
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Attachments"
      className="max-w-lg"
    >
      <AttachmentsPanel
        attachments={attachments}
        loading={isLoading}
        onUpload={handleUpload}
        onDownload={handleDownload}
        onDelete={handleDelete}
      />
    </Modal>
  );
};
