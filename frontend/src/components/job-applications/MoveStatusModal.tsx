/**
 * MoveStatusModal — quickly changes an application's pipeline status.
 *
 * Used by the quick actions on list and board cards. Mirrors what a
 * drag-and-drop on the Kanban board does: the status change is persisted,
 * a timeline event is recorded, and a notification is generated.
 *
 * @author DevLaunch
 */

import React, { useState } from 'react';
import { ArrowLeftRight } from 'lucide-react';
import { Modal } from '../ui/Modal';
import { Button } from '../ui/Button';
import { StatusBadge } from './StatusBadge';
import {
  APPLICATION_STATUSES,
  APPLICATION_STATUS_LABELS,
  type ApplicationStatusEnum,
} from '../../types/job-application';

interface MoveStatusModalProps {
  /** Whether the modal is visible. */
  isOpen: boolean;
  /** Closes the modal. */
  onClose: () => void;
  /** The application's current status. */
  currentStatus: ApplicationStatusEnum;
  /** Whether the update is in flight. */
  isSubmitting: boolean;
  /** Submits the new status. */
  onSubmit: (status: ApplicationStatusEnum) => void;
}

export const MoveStatusModal: React.FC<MoveStatusModalProps> = ({
  isOpen,
  onClose,
  currentStatus,
  isSubmitting,
  onSubmit,
}) => {
  const [selected, setSelected] = useState<ApplicationStatusEnum>(currentStatus);

  // Keep the selection in sync whenever the modal opens for another card.
  React.useEffect(() => {
    if (isOpen) {
      setSelected(currentStatus);
    }
  }, [isOpen, currentStatus]);

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Move Status"
      className="max-w-sm"
      closeOnBackdrop={false}
    >
      <p className="mb-4 text-xs text-gray-500">
        Update the pipeline stage. This records a timeline event and sends
        you a notification.
      </p>

      <div className="grid grid-cols-2 gap-2">
        {APPLICATION_STATUSES.map((status) => (
          <button
            key={status}
            type="button"
            onClick={() => setSelected(status)}
            className={`flex items-center justify-between rounded-xl border px-3 py-2.5 text-left text-sm transition-colors ${
              selected === status
                ? 'border-indigo-300 bg-indigo-50 ring-1 ring-indigo-200'
                : 'border-gray-200 bg-white hover:border-gray-300 hover:bg-gray-50'
            }`}
          >
            <StatusBadge status={status} size="sm" />
            <span className="text-xs text-gray-400">{APPLICATION_STATUS_LABELS[status]}</span>
          </button>
        ))}
      </div>

      <div className="mt-6 flex justify-end gap-2">
        <Button variant="ghost" onClick={onClose} disabled={isSubmitting}>
          Cancel
        </Button>
        <Button
          onClick={() => onSubmit(selected)}
          loading={isSubmitting}
          disabled={selected === currentStatus}
        >
          <ArrowLeftRight className="h-4 w-4" />
          Move
        </Button>
      </div>
    </Modal>
  );
};
