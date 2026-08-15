/**
 * ApplicationDetailModal — full detail view for a job application.
 *
 * Tabbed dialog covering the application overview (all stored fields), the
 * automatic milestone timeline, scheduled interviews, private interview
 * notes, and document attachments — with quick actions to edit, move
 * status, schedule, add notes, or attach documents.
 *
 * @author DevLaunch
 */

import React, { useEffect, useState } from 'react';
import {
  ExternalLink,
  MapPin,
  Briefcase,
  DollarSign,
  Calendar,
  User,
  Mail,
  Globe,
  Link2,
  StickyNote,
  Edit3,
  CalendarClock,
  Paperclip,
  Building2,
  FileText,
} from 'lucide-react';
import { Modal } from '../ui/Modal';
import { CompanyLogo } from './CompanyLogo';
import { StatusBadge } from './StatusBadge';
import { PriorityBadge } from './PriorityBadge';
import { TimelinePanel } from './TimelinePanel';
import { InterviewsPanel } from './InterviewsPanel';
import { InterviewNotesPanel } from './InterviewNotesPanel';
import { AttachmentsPanel } from './AttachmentsPanel';
import { ScheduleInterviewModal } from './ScheduleInterviewModal';
import { jobApplicationService } from '../../services/job-application.service';
import { formatDate, formatTime } from '../../utils/date';
import { interviewCountdownLabel } from '../../utils/jobApplication';
import { WORK_MODE_LABELS } from '../../types/job-application';
import type {
  ApplicationAttachment,
  AttachmentCategory,
  InterviewNote,
  InterviewSchedule,
  JobApplicationResponse,
} from '../../types/job-application';

type TabKey = 'overview' | 'timeline' | 'interviews' | 'notes' | 'attachments';

interface ApplicationDetailModalProps {
  /** Whether the modal is visible. */
  isOpen: boolean;
  /** Closes the modal. */
  onClose: () => void;
  /** The application to display. */
  application: JobApplicationResponse;
  /** Navigates to the edit page. */
  onEdit: (id: number) => void;
  /** Fired after any mutation so parent state can refresh. */
  onChanged?: () => void;
}

const TABS: { key: TabKey; label: string }[] = [
  { key: 'overview', label: 'Overview' },
  { key: 'timeline', label: 'Timeline' },
  { key: 'interviews', label: 'Interviews' },
  { key: 'notes', label: 'Notes' },
  { key: 'attachments', label: 'Attachments' },
];

export const ApplicationDetailModal: React.FC<ApplicationDetailModalProps> = ({
  isOpen,
  onClose,
  application,
  onEdit,
  onChanged,
}) => {
  const [activeTab, setActiveTab] = useState<TabKey>('overview');
  const [notes, setNotes] = useState<InterviewNote[]>([]);
  const [attachments, setAttachments] = useState<ApplicationAttachment[]>([]);
  const [isLoadingNotes, setIsLoadingNotes] = useState(false);
  const [isLoadingAttachments, setIsLoadingAttachments] = useState(false);
  const [scheduleOpen, setScheduleOpen] = useState(false);
  const [editingInterview, setEditingInterview] = useState<InterviewSchedule | null>(null);
  const [isScheduling, setIsScheduling] = useState(false);

  // Fetch lazy tab data whenever the modal opens.
  useEffect(() => {
    if (!isOpen) return;
    setActiveTab('overview');
    setEditingInterview(null);

    setIsLoadingNotes(true);
    jobApplicationService
      .getNotes(application.id)
      .then(setNotes)
      .catch(() => setNotes([]))
      .finally(() => setIsLoadingNotes(false));

    setIsLoadingAttachments(true);
    jobApplicationService
      .getAttachments(application.id)
      .then(setAttachments)
      .catch(() => setAttachments([]))
      .finally(() => setIsLoadingAttachments(false));
  }, [isOpen, application.id]);

  const handleScheduleSubmit = async (
    data: Parameters<typeof jobApplicationService.scheduleInterview>[1],
  ) => {
    setIsScheduling(true);
    try {
      if (editingInterview) {
        await jobApplicationService.updateInterview(editingInterview.id, data);
      } else {
        await jobApplicationService.scheduleInterview(application.id, data);
      }
      setScheduleOpen(false);
      setEditingInterview(null);
      onChanged?.();
      // Reload interviews through the parent by re-fetching the app.
      window.dispatchEvent(new CustomEvent('job-applications:refresh'));
    } finally {
      setIsScheduling(false);
    }
  };

  const handleAddNote = async (content: string) => {
    const note = await jobApplicationService.addNote(application.id, content);
    setNotes((prev) => [note, ...prev]);
    onChanged?.();
  };

  const handleDeleteNote = async (note: InterviewNote) => {
    await jobApplicationService.deleteNote(application.id, note.id);
    setNotes((prev) => prev.filter((n) => n.id !== note.id));
    onChanged?.();
  };

  const handleUploadAttachment = async (file: File, category: AttachmentCategory) => {
    const attachment = await jobApplicationService.uploadAttachment(
      application.id,
      file,
      category,
    );
    setAttachments((prev) => [attachment, ...prev]);
    onChanged?.();
  };

  const handleDeleteAttachment = async (attachment: ApplicationAttachment) => {
    await jobApplicationService.deleteAttachment(application.id, attachment.id);
    setAttachments((prev) => prev.filter((a) => a.id !== attachment.id));
    onChanged?.();
  };

  const handleDownloadAttachment = async (attachment: ApplicationAttachment) => {
    await jobApplicationService.downloadAttachment(
      application.id,
      attachment.id,
      attachment.fileName,
    );
  };

  const handleCancelInterview = async (interview: InterviewSchedule) => {
    await jobApplicationService.cancelInterview(interview.id);
    onChanged?.();
    window.dispatchEvent(new CustomEvent('job-applications:refresh'));
  };

  const detailRows: { icon: React.ReactNode; label: string; value: React.ReactNode }[] = [
    {
      icon: <MapPin className="h-3.5 w-3.5" />,
      label: 'Location',
      value: application.companyLocation ?? '—',
    },
    {
      icon: <Briefcase className="h-3.5 w-3.5" />,
      label: 'Employment Type',
      value: application.jobType ?? '—',
    },
    {
      icon: <DollarSign className="h-3.5 w-3.5" />,
      label: 'Expected Salary',
      value: application.salary ?? '—',
    },
    {
      icon: <Calendar className="h-3.5 w-3.5" />,
      label: 'Applied On',
      value: formatDate(application.applicationDate),
    },
    {
      icon: <Building2 className="h-3.5 w-3.5" />,
      label: 'Work Mode',
      value: application.workMode ? WORK_MODE_LABELS[application.workMode] : '—',
    },
    {
      icon: <User className="h-3.5 w-3.5" />,
      label: 'Recruiter',
      value: application.recruiterName ?? '—',
    },
    {
      icon: <Mail className="h-3.5 w-3.5" />,
      label: 'Recruiter Email',
      value: application.recruiterEmail ?? '—',
    },
    {
      icon: <Link2 className="h-3.5 w-3.5" />,
      label: 'Referral',
      value: application.referral ?? '—',
    },
    {
      icon: <FileText className="h-3.5 w-3.5" />,
      label: 'Technology',
      value: application.technology ?? '—',
    },
  ];

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      className="max-w-2xl"
    >
      {/* Header */}
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-start gap-3 min-w-0">
          <CompanyLogo
            companyName={application.companyName}
            website={application.companyWebsite ?? application.jobUrl}
            size="lg"
          />
          <div className="min-w-0">
            <h2 className="text-lg font-bold text-gray-900">{application.companyName}</h2>
            <p className="truncate text-sm text-gray-500">{application.jobRole}</p>
            <div className="mt-1.5 flex flex-wrap items-center gap-1.5">
              <StatusBadge status={application.status} size="sm" />
              <PriorityBadge priority={application.priority} size="sm" />
              {application.upcomingInterview && (
                <span className="inline-flex items-center rounded-full bg-blue-100 px-2 py-0.5 text-[10px] font-medium text-blue-700">
                  <CalendarClock className="mr-1 h-3 w-3" />
                  {interviewCountdownLabel(application.upcomingInterview)} ·{' '}
                  {formatTime(application.upcomingInterview.scheduledTime)}
                </span>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Links */}
      {(application.jobUrl || application.companyWebsite) && (
        <div className="mt-3 flex flex-wrap gap-3">
          {application.jobUrl && (
            <a
              href={application.jobUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1 text-xs font-medium text-indigo-600 hover:text-indigo-800"
            >
              <ExternalLink className="h-3 w-3" />
              View Job Posting
            </a>
          )}
          {application.companyWebsite && (
            <a
              href={application.companyWebsite}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1 text-xs font-medium text-gray-500 hover:text-gray-700"
            >
              <Globe className="h-3 w-3" />
              Company Website
            </a>
          )}
          {application.resumeId && (
            <button
              onClick={() => window.dispatchEvent(
                new CustomEvent('job-applications:open-resume', {
                  detail: application.resumeId,
                }),
              )}
              className="inline-flex items-center gap-1 text-xs font-medium text-gray-500 hover:text-gray-700"
            >
              <FileText className="h-3 w-3" />
              View Resume
            </button>
          )}
        </div>
      )}

      {/* Tabs */}
      <div className="mt-4 flex gap-1 overflow-x-auto border-b border-gray-100">
        {TABS.map((tab) => {
          const badge =
            tab.key === 'notes'
              ? notes.length
              : tab.key === 'attachments'
                ? attachments.length
                : tab.key === 'interviews'
                  ? application.interviews.length
                  : null;
          return (
            <button
              key={tab.key}
              onClick={() => setActiveTab(tab.key)}
              className={`flex items-center gap-1.5 whitespace-nowrap border-b-2 px-3 py-2 text-xs font-medium transition-colors ${
                activeTab === tab.key
                  ? 'border-indigo-500 text-indigo-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}
            >
              {tab.label}
              {badge !== null && badge > 0 && (
                <span className="rounded-full bg-gray-100 px-1.5 py-0.5 text-[10px] font-semibold text-gray-500">
                  {badge}
                </span>
              )}
            </button>
          );
        })}
      </div>

      {/* Tab content */}
      <div className="mt-4 max-h-[45vh] overflow-y-auto pr-1">
        {activeTab === 'overview' && (
          <div className="space-y-4">
            <div className="grid grid-cols-1 gap-x-6 gap-y-2.5 sm:grid-cols-2">
              {detailRows.map((row) => (
                <div key={row.label} className="flex items-start gap-2">
                  <span className="mt-0.5 text-gray-400">{row.icon}</span>
                  <div className="min-w-0">
                    <p className="text-[10px] font-medium uppercase tracking-wide text-gray-400">
                      {row.label}
                    </p>
                    <p className="truncate text-sm text-gray-800">{row.value}</p>
                  </div>
                </div>
              ))}
            </div>

            {application.notes && (
              <div className="rounded-xl border border-gray-100 bg-gray-50 p-3.5">
                <p className="text-[10px] font-medium uppercase tracking-wide text-gray-400">
                  Notes
                </p>
                <p className="mt-1 whitespace-pre-wrap text-sm text-gray-700">
                  {application.notes}
                </p>
              </div>
            )}

            {application.upcomingInterview && (
              <div className="rounded-xl border border-blue-100 bg-blue-50/50 p-3.5">
                <p className="flex items-center gap-1.5 text-[10px] font-medium uppercase tracking-wide text-blue-500">
                  <CalendarClock className="h-3 w-3" /> Next interview
                </p>
                <p className="mt-1 text-sm font-semibold text-gray-900">
                  {application.upcomingInterview.title} · {formatDate(application.upcomingInterview.scheduledDate)}
                  {application.upcomingInterview.scheduledTime
                    ? ` at ${formatTime(application.upcomingInterview.scheduledTime)}`
                    : ''}
                </p>
              </div>
            )}
          </div>
        )}

        {activeTab === 'timeline' && (
          <TimelinePanel events={application.timeline} />
        )}

        {activeTab === 'interviews' && (
          <InterviewsPanel
            interviews={application.interviews}
            onSchedule={() => {
              setEditingInterview(null);
              setScheduleOpen(true);
            }}
            onEdit={(interview) => {
              setEditingInterview(interview);
              setScheduleOpen(true);
            }}
            onCancel={handleCancelInterview}
          />
        )}

        {activeTab === 'notes' && (
          <InterviewNotesPanel
            notes={notes}
            loading={isLoadingNotes}
            onAdd={handleAddNote}
            onDelete={handleDeleteNote}
          />
        )}

        {activeTab === 'attachments' && (
          <AttachmentsPanel
            attachments={attachments}
            loading={isLoadingAttachments}
            onUpload={handleUploadAttachment}
            onDownload={handleDownloadAttachment}
            onDelete={handleDeleteAttachment}
          />
        )}
      </div>

      {/* Footer quick actions */}
      <div className="mt-4 flex flex-wrap items-center gap-2 border-t border-gray-100 pt-4">
        <button
          onClick={() => onEdit(application.id)}
          className="inline-flex items-center gap-1.5 rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-xs font-medium text-gray-700 transition-colors hover:bg-gray-50"
        >
          <Edit3 className="h-3.5 w-3.5" />
          Edit
        </button>
        <button
          onClick={() => {
            setScheduleOpen(true);
            setEditingInterview(null);
          }}
          className="inline-flex items-center gap-1.5 rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-xs font-medium text-gray-700 transition-colors hover:bg-gray-50"
        >
          <CalendarClock className="h-3.5 w-3.5" />
          Schedule Interview
        </button>
        <span className="ml-auto flex items-center gap-1 text-xs text-gray-400">
          <StickyNote className="h-3.5 w-3.5" />
          {notes.length} note{notes.length === 1 ? '' : 's'} ·{' '}
          <Paperclip className="h-3.5 w-3.5" />
          {attachments.length} file{attachments.length === 1 ? '' : 's'}
        </span>
      </div>

      {/* Nested schedule modal */}
      <ScheduleInterviewModal
        isOpen={scheduleOpen}
        onClose={() => {
          setScheduleOpen(false);
          setEditingInterview(null);
        }}
        companyName={application.companyName}
        interview={editingInterview}
        isSubmitting={isScheduling}
        onSubmit={handleScheduleSubmit}
      />
    </Modal>
  );
};
