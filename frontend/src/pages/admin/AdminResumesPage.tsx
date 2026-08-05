/**
 * AdminResumesPage — resume management for administrators.
 *
 * Lists every resume on the platform with owner information, a details
 * modal showing the full resume content, and permanent deletion.
 *
 * @author DevLaunch
 */

import React, { useCallback, useEffect, useState } from 'react';
import { FileText, RefreshCw, Eye, Trash2, ExternalLink } from 'lucide-react';
import toast from 'react-hot-toast';
import { adminService } from '../../services/admin.service';
import { Button } from '../../components/ui/Button';
import { Modal } from '../../components/ui/Modal';
import { LoadingScreen } from '../../components/ui/LoadingScreen';
import { ErrorMessage } from '../../components/shared/ErrorMessage';
import { ConfirmDeleteModal } from '../../components/admin/ConfirmDeleteModal';
import { AdminPagination } from '../../components/admin/AdminPagination';
import { formatDate } from '../../utils/date';
import { formatNumber, extractDomain, truncate } from '../../utils/format';
import { getErrorMessage } from '../../utils/error';
import { MESSAGES } from '../../constants/messages';
import type { AdminResumeResponse } from '../../types/admin';

const PAGE_SIZE = 10;

export const AdminResumesPage: React.FC = () => {
  const [resumes, setResumes] = useState<AdminResumeResponse[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [details, setDetails] = useState<AdminResumeResponse | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<AdminResumeResponse | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const fetchResumes = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await adminService.getResumes({ page, size: PAGE_SIZE });
      setResumes(data.content);
      setTotalElements(data.totalElements);
      setTotalPages(data.totalPages);
    } catch (err: unknown) {
      setError(getErrorMessage(err, MESSAGES.LOAD_ERROR('resumes')));
    } finally {
      setIsLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchResumes();
  }, [fetchResumes]);

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setIsDeleting(true);
    try {
      await adminService.deleteResume(deleteTarget.id);
      toast.success(MESSAGES.DELETE_SUCCESS('Resume'));
      setResumes((prev) => prev.filter((r) => r.id !== deleteTarget.id));
      setTotalElements((prev) => prev - 1);
      setDeleteTarget(null);
    } catch (err: unknown) {
      toast.error(getErrorMessage(err, MESSAGES.SAVE_ERROR('resume deletion')));
    } finally {
      setIsDeleting(false);
    }
  };

  if (isLoading && resumes.length === 0) {
    return <LoadingScreen />;
  }

  return (
    <div className="mx-auto max-w-6xl">
      {/* Header */}
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900 sm:text-2xl">Resume Management</h1>
          <p className="mt-1 text-sm text-gray-500">
            {formatNumber(totalElements)} {totalElements === 1 ? 'resume' : 'resumes'} across the
            platform.
          </p>
        </div>
        <Button variant="outline" size="sm" onClick={fetchResumes}>
          <RefreshCw className="h-4 w-4" />
          Refresh
        </Button>
      </div>

      {error && (
        <div className="mb-4">
          <ErrorMessage message={error} onRetry={fetchResumes} />
        </div>
      )}

      {/* Table */}
      <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-gray-100 bg-gray-50">
              <tr className="text-xs uppercase tracking-wide text-gray-400">
                <th className="px-4 py-3 font-medium">Headline</th>
                <th className="px-4 py-3 font-medium">Owner</th>
                <th className="px-4 py-3 font-medium">Links</th>
                <th className="px-4 py-3 font-medium">Created</th>
                <th className="px-4 py-3 text-right font-medium">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {resumes.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-4 py-12 text-center">
                    <FileText className="mx-auto mb-3 h-10 w-10 text-gray-300" />
                    <p className="text-sm font-medium text-gray-500">No resumes yet</p>
                  </td>
                </tr>
              ) : (
                resumes.map((resume) => (
                  <tr key={resume.id} className="transition-colors hover:bg-gray-50">
                    <td className="px-4 py-3 font-medium text-gray-900">
                      {truncate(resume.headline, 60)}
                    </td>
                    <td className="px-4 py-3">
                      <p className="text-gray-700">{resume.userName}</p>
                      <p className="text-xs text-gray-400">{resume.userEmail}</p>
                    </td>
                    <td className="px-4 py-3 text-xs text-gray-400">
                      {[resume.linkedinUrl, resume.githubUrl, resume.portfolioUrl]
                        .filter((url): url is string => !!url)
                        .map((url) => extractDomain(url))
                        .join(', ') || '—'}
                    </td>
                    <td className="px-4 py-3 text-xs text-gray-400">
                      {formatDate(resume.createdAt)}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center justify-end gap-1">
                        <button
                          onClick={() => setDetails(resume)}
                          title="View details"
                          className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-700"
                        >
                          <Eye className="h-4 w-4" />
                        </button>
                        <button
                          onClick={() => setDeleteTarget(resume)}
                          title="Delete resume"
                          className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-red-50 hover:text-red-600"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        <div className="border-t border-gray-100 px-4 py-3">
          <AdminPagination
            page={page}
            totalPages={totalPages}
            totalElements={totalElements}
            onPageChange={setPage}
          />
        </div>
      </div>

      {/* Details modal */}
      <Modal isOpen={details !== null} onClose={() => setDetails(null)} title="Resume Details">
        {details && (
          <div className="space-y-4 text-sm">
            <div>
              <p className="text-xs font-medium uppercase tracking-wide text-gray-400">Headline</p>
              <p className="mt-1 font-semibold text-gray-900">{details.headline}</p>
            </div>
            <div>
              <p className="text-xs font-medium uppercase tracking-wide text-gray-400">Summary</p>
              <p className="mt-1 whitespace-pre-wrap text-gray-700">{details.summary || '—'}</p>
            </div>
            <div>
              <p className="text-xs font-medium uppercase tracking-wide text-gray-400">Owner</p>
              <p className="mt-1 text-gray-700">
                {details.userName} · {details.userEmail}
              </p>
            </div>
            <div className="space-y-1">
              {[
                ['LinkedIn', details.linkedinUrl],
                ['GitHub', details.githubUrl],
                ['Portfolio', details.portfolioUrl],
              ].map(([label, url]) =>
                url ? (
                  <a
                    key={label}
                    href={url}
                    target="_blank"
                    rel="noreferrer"
                    className="flex items-center gap-1.5 text-indigo-600 hover:text-indigo-800"
                  >
                    <ExternalLink className="h-3.5 w-3.5" />
                    {label}: {extractDomain(url)}
                  </a>
                ) : null,
              )}
            </div>
          </div>
        )}
      </Modal>

      {/* Delete confirmation modal */}
      <ConfirmDeleteModal
        isOpen={deleteTarget !== null}
        resource="resume"
        targetName={deleteTarget?.headline}
        isLoading={isDeleting}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
      />
    </div>
  );
};
