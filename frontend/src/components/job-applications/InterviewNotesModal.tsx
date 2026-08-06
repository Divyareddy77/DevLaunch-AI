/**
 * InterviewNotesModal — quick-access dialog for an application's private
 * interview notes.
 *
 * Loads the notes when opened and renders the shared InterviewNotesPanel,
 * so the same component backs both the quick action and the detail tab.
 *
 * @author DevLaunch
 */

import React, { useEffect, useState } from 'react';
import { Modal } from '../ui/Modal';
import { InterviewNotesPanel } from './InterviewNotesPanel';
import { jobApplicationService } from '../../services/job-application.service';
import type { InterviewNote } from '../../types/job-application';

interface InterviewNotesModalProps {
  /** Whether the modal is visible. */
  isOpen: boolean;
  /** Closes the modal. */
  onClose: () => void;
  /** The application these notes belong to. */
  applicationId: number;
  /** Fired after a successful add/delete so parent state can refresh. */
  onChange?: () => void;
}

export const InterviewNotesModal: React.FC<InterviewNotesModalProps> = ({
  isOpen,
  onClose,
  applicationId,
  onChange,
}) => {
  const [notes, setNotes] = useState<InterviewNote[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!isOpen) return;
    setIsLoading(true);
    jobApplicationService
      .getNotes(applicationId)
      .then(setNotes)
      .catch(() => setNotes([]))
      .finally(() => setIsLoading(false));
  }, [isOpen, applicationId]);

  const handleAdd = async (content: string) => {
    const note = await jobApplicationService.addNote(applicationId, content);
    setNotes((prev) => [note, ...prev]);
    onChange?.();
  };

  const handleDelete = async (note: InterviewNote) => {
    await jobApplicationService.deleteNote(applicationId, note.id);
    setNotes((prev) => prev.filter((n) => n.id !== note.id));
    onChange?.();
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Interview Notes"
      className="max-w-lg"
    >
      <InterviewNotesPanel
        notes={notes}
        loading={isLoading}
        onAdd={handleAdd}
        onDelete={handleDelete}
      />
    </Modal>
  );
};
