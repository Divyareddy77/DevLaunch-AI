/**
 * ConfirmDeleteModal — a reusable delete confirmation dialog.
 *
 * Wraps the shared Modal with a consistent confirmation layout used by
 * every admin management page: a warning message, Cancel, and Delete.
 *
 * @author DevLaunch
 */

import React from 'react';
import { Trash2 } from 'lucide-react';
import { Modal } from '../ui/Modal';
import { Button } from '../ui/Button';
import { MESSAGES } from '../../constants/messages';

interface ConfirmDeleteModalProps {
  /** Whether the modal is visible. */
  isOpen: boolean;
  /** The resource being deleted, e.g. "user" or "resume". */
  resource: string;
  /** Optional name/identifier shown in the confirmation message. */
  targetName?: string;
  /** Whether the delete request is in flight. */
  isLoading: boolean;
  /** Closes the modal without deleting. */
  onClose: () => void;
  /** Confirms and deletes the resource. */
  onConfirm: () => void;
}

export const ConfirmDeleteModal: React.FC<ConfirmDeleteModalProps> = ({
  isOpen,
  resource,
  targetName,
  isLoading,
  onClose,
  onConfirm,
}) => {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={`Delete ${resource}`}
      closeOnBackdrop={false}
    >
      <div className="flex items-start gap-3">
        <div className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full bg-red-100">
          <Trash2 className="h-5 w-5 text-red-600" />
        </div>
        <p className="text-sm text-gray-600">
          {MESSAGES.DELETE_CONFIRM(resource)}
          {targetName ? (
            <>
              {' '}
              <span className="font-medium text-gray-900">&quot;{targetName}&quot;</span>
            </>
          ) : null}
        </p>
      </div>
      <div className="mt-6 flex justify-end gap-2">
        <Button variant="ghost" onClick={onClose} disabled={isLoading}>
          Cancel
        </Button>
        <Button variant="danger" onClick={onConfirm} loading={isLoading}>
          Delete
        </Button>
      </div>
    </Modal>
  );
};
