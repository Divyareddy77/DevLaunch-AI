/**
 * AttachmentsPanel — document attachments for an application.
 *
 * Lets the user upload documents (resume used, cover letter, offer letter,
 * assessment PDF, interview feedback), download them, and delete them.
 * Files are stored by the backend on the local file system; only metadata
 * is exchanged with the API.
 *
 * @author DevLaunch
 */

import React, { useRef, useState } from 'react';
import {
  Trash2,
  Download,
  Upload,
  FileText,
  Image,
  File,
  Inbox,
} from 'lucide-react';
import { formatDate } from '../../utils/date';

/** Formats a byte count into a human-readable size. */
function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}
import {
  ATTACHMENT_CATEGORIES,
  ATTACHMENT_CATEGORY_LABELS,
  type ApplicationAttachment,
  type AttachmentCategory,
} from '../../types/job-application';

interface AttachmentsPanelProps {
  /** The attachments of the application, newest first. */
  attachments: ApplicationAttachment[];
  /** Whether the attachments are still loading. */
  loading?: boolean;
  /** Uploads a file with the given category. */
  onUpload: (file: File, category: AttachmentCategory) => Promise<void>;
  /** Downloads an attachment. */
  onDownload: (attachment: ApplicationAttachment) => Promise<void>;
  /** Deletes an attachment. */
  onDelete: (attachment: ApplicationAttachment) => Promise<void>;
}

function fileIcon(contentType: string | null) {
  if (contentType?.startsWith('image/')) {
    return <Image className="h-4 w-4" />;
  }
  if (contentType === 'application/pdf') {
    return <FileText className="h-4 w-4" />;
  }
  return <File className="h-4 w-4" />;
}

export const AttachmentsPanel: React.FC<AttachmentsPanelProps> = ({
  attachments,
  loading = false,
  onUpload,
  onDownload,
  onDelete,
}) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [category, setCategory] = useState<AttachmentCategory>('RESUME');
  const [isUploading, setIsUploading] = useState(false);
  const [busyId, setBusyId] = useState<number | null>(null);

  const handleFileSelected = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file || isUploading) return;
    setIsUploading(true);
    try {
      await onUpload(file, category);
    } finally {
      setIsUploading(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  if (loading) {
    return (
      <div className="space-y-3 animate-pulse">
        {[0, 1].map((i) => (
          <div key={i} className="h-14 rounded-lg bg-gray-200" />
        ))}
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Upload row */}
      <div className="flex flex-col gap-2 rounded-xl border border-dashed border-gray-300 bg-gray-50 p-3 sm:flex-row sm:items-center">
        <select
          value={category}
          onChange={(e) => setCategory(e.target.value as AttachmentCategory)}
          className="rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 transition-colors focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500 sm:w-44"
          aria-label="Attachment category"
        >
          {ATTACHMENT_CATEGORIES.map((value) => (
            <option key={value} value={value}>
              {ATTACHMENT_CATEGORY_LABELS[value]}
            </option>
          ))}
        </select>
        <input
          ref={fileInputRef}
          type="file"
          className="hidden"
          accept=".pdf,.doc,.docx,.png,.jpg,.jpeg,.txt,.md"
          onChange={handleFileSelected}
        />
        <button
          onClick={() => fileInputRef.current?.click()}
          disabled={isUploading}
          className="inline-flex items-center justify-center gap-1.5 rounded-lg bg-indigo-600 px-3 py-2 text-sm font-medium text-white transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-50"
        >
          <Upload className="h-4 w-4" />
          {isUploading ? 'Uploading…' : 'Upload Document'}
        </button>
        <p className="text-[11px] text-gray-400">Max 10 MB · PDF, DOC, images</p>
      </div>

      {/* Attachments list */}
      {attachments.length === 0 ? (
        <div className="flex flex-col items-center py-6 text-center">
          <Inbox className="mb-2 h-8 w-8 text-gray-300" />
          <p className="text-sm font-medium text-gray-500">No attachments yet</p>
          <p className="mt-0.5 text-xs text-gray-400">
            Attach the resume you used, cover letters, or offer letters.
          </p>
        </div>
      ) : (
        <ul className="space-y-2">
          {attachments.map((attachment) => (
            <li
              key={attachment.id}
              className="group flex items-center justify-between gap-3 rounded-xl border border-gray-200 bg-white p-3 transition-colors hover:border-gray-300"
            >
              <div className="flex items-center gap-3 min-w-0">
                <span className="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-lg bg-violet-100 text-violet-600">
                  {fileIcon(attachment.contentType)}
                </span>
                <div className="min-w-0">
                  <p className="truncate text-sm font-medium text-gray-900">
                    {attachment.fileName}
                  </p>
                  <p className="text-[11px] text-gray-400">
                    {ATTACHMENT_CATEGORY_LABELS[attachment.category]} ·{' '}
                    {formatFileSize(attachment.fileSize)} · {formatDate(attachment.createdAt)}
                  </p>
                </div>
              </div>
              <div className="flex flex-shrink-0 items-center gap-1">
                <button
                  onClick={() => onDownload(attachment)}
                  className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600"
                  aria-label="Download attachment"
                  title="Download"
                >
                  <Download className="h-4 w-4" />
                </button>
                <button
                  onClick={async () => {
                    setBusyId(attachment.id);
                    try {
                      await onDelete(attachment);
                    } finally {
                      setBusyId(null);
                    }
                  }}
                  disabled={busyId === attachment.id}
                  className="rounded-lg p-1.5 text-gray-300 transition-all hover:bg-red-50 hover:text-red-500 group-hover:opacity-100 disabled:opacity-50"
                  aria-label="Delete attachment"
                  title="Delete"
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};
