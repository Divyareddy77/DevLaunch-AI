package com.devlaunch.service.interfaces;

import com.devlaunch.dto.request.CreateInterviewNoteRequest;
import com.devlaunch.dto.request.CreateJobApplicationRequest;
import com.devlaunch.dto.request.ScheduleInterviewRequest;
import com.devlaunch.dto.request.UpdateInterviewScheduleRequest;
import com.devlaunch.dto.request.UpdateJobApplicationRequest;
import com.devlaunch.dto.response.ApplicationAnalyticsResponse;
import com.devlaunch.dto.response.ApplicationAttachmentResponse;
import com.devlaunch.dto.response.InterviewNoteResponse;
import com.devlaunch.dto.response.InterviewScheduleResponse;
import com.devlaunch.dto.response.JobApplicationResponse;
import com.devlaunch.dto.response.TimelineEventResponse;
import com.devlaunch.entity.enums.ApplicationStatus;
import com.devlaunch.entity.enums.AttachmentCategory;
import com.devlaunch.exception.ResourceNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for job application tracking operations.
 * <p>
 * Defines the contract for creating, retrieving, updating, and deleting
 * the currently authenticated user's job applications, plus the placement
 * management features layered on top: status moves (Kanban), the automatic
 * application timeline, interview scheduling, private interview notes,
 * document attachments, and an analytics overview. Every operation is
 * scoped to the authenticated user, and update/delete operations verify
 * ownership before touching any record.
 * </p>
 *
 * @author DevLaunch
 */
public interface JobApplicationService {

    /**
     * Creates a new job application for the currently authenticated user.
     * <p>
     * If a resume ID is provided in the request, the resume is fetched
     * and verified to belong to the authenticated user before being
     * associated with the application. The initial timeline entries are
     * recorded automatically.
     * </p>
     *
     * @param request the create-job-application request containing application details
     * @return the newly created job application data
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   or if the specified resume is not found
     *                                   or does not belong to the user
     */
    JobApplicationResponse createJobApplication(CreateJobApplicationRequest request);

    /**
     * Retrieves all job applications belonging to the currently authenticated user.
     *
     * @return a list of job application data for the authenticated user,
     *         or an empty list if none exist
     */
    List<JobApplicationResponse> getAllJobApplications();

    /**
     * Retrieves a specific job application by its ID, ensuring it belongs to the
     * currently authenticated user.
     *
     * @param id the job application ID
     * @return the job application data
     * @throws ResourceNotFoundException if the job application is not found
     *                                    or does not belong to the user
     */
    JobApplicationResponse getJobApplicationById(Long id);

    /**
     * Updates a specific job application by its ID, ensuring it belongs to the
     * currently authenticated user.
     * <p>
     * If the resume ID in the request differs from the current value, the
     * resume association is updated accordingly. When the status changes,
     * a timeline event is recorded and the user is notified.
     * </p>
     *
     * @param id      the job application ID to update
     * @param request the update request containing the new application details
     * @return the updated job application data
     * @throws ResourceNotFoundException if the job application is not found
     *                                    or does not belong to the user, or if
     *                                    the specified resume is not found or
     *                                    does not belong to the user
     */
    JobApplicationResponse updateJobApplication(Long id, UpdateJobApplicationRequest request);

    /**
     * Updates only the status of a job application (used by the Kanban board
     * drag-and-drop), recording a timeline event and notification.
     *
     * @param id     the job application ID
     * @param status the new status
     * @return the updated job application data
     * @throws ResourceNotFoundException if the job application is not found
     *                                    or does not belong to the user
     */
    JobApplicationResponse updateApplicationStatus(Long id, ApplicationStatus status);

    /**
     * Deletes a specific job application by its ID, ensuring it belongs to the
     * currently authenticated user.
     *
     * @param id the job application ID to delete
     * @throws ResourceNotFoundException if the job application is not found
     *                                    or does not belong to the user
     */
    void deleteJobApplication(Long id);

    /**
     * Retrieves the milestone timeline of a job application, oldest first.
     *
     * @param id the job application ID
     * @return the ordered timeline events
     */
    List<TimelineEventResponse> getTimeline(Long id);

    /**
     * Retrieves the scheduled interviews of a job application, ordered by date.
     *
     * @param id the job application ID
     * @return the interviews, or an empty list if none exist
     */
    List<InterviewScheduleResponse> getInterviews(Long id);

    /**
     * Schedules a new interview on a job application, recording a timeline
     * event and notifying the user.
     *
     * @param id      the job application ID
     * @param request the interview details
     * @return the created interview
     */
    InterviewScheduleResponse scheduleInterview(Long id, ScheduleInterviewRequest request);

    /**
     * Updates an existing interview schedule, verifying the interview belongs
     * to the authenticated user's application.
     *
     * @param interviewId the interview ID
     * @param request     the updated interview details
     * @return the updated interview
     */
    InterviewScheduleResponse updateInterview(Long interviewId, UpdateInterviewScheduleRequest request);

    /**
     * Cancels an existing interview schedule, recording a timeline event and
     * notifying the user.
     *
     * @param interviewId the interview ID
     * @return the cancelled interview
     */
    InterviewScheduleResponse cancelInterview(Long interviewId);

    /**
     * Retrieves the private interview notes of a job application, newest first.
     *
     * @param id the job application ID
     * @return the notes, or an empty list if none exist
     */
    List<InterviewNoteResponse> getNotes(Long id);

    /**
     * Adds a private interview note entry to a job application.
     *
     * @param id      the job application ID
     * @param request the note content
     * @return the created note
     */
    InterviewNoteResponse addNote(Long id, CreateInterviewNoteRequest request);

    /**
     * Deletes a private interview note from a job application.
     *
     * @param applicationId the job application ID
     * @param noteId        the note ID to delete
     */
    void deleteNote(Long applicationId, Long noteId);

    /**
     * Retrieves the attachments of a job application, newest first.
     *
     * @param id the job application ID
     * @return the attachments, or an empty list if none exist
     */
    List<ApplicationAttachmentResponse> getAttachments(Long id);

    /**
     * Uploads and attaches a document to a job application, recording a
     * timeline event.
     *
     * @param id       the job application ID
     * @param file     the uploaded file
     * @param category the attachment category
     * @return the created attachment metadata
     */
    ApplicationAttachmentResponse uploadAttachment(Long id, MultipartFile file,
                                                   AttachmentCategory category);

    /**
     * Deletes an attachment, removing both the database record and the
     * underlying file from storage.
     *
     * @param applicationId the job application ID
     * @param attachmentId  the attachment ID to delete
     */
    void deleteAttachment(Long applicationId, Long attachmentId);

    /**
     * Prepares an attachment for download.
     *
     * @param applicationId the job application ID
     * @param attachmentId  the attachment ID to download
     * @return the downloadable file descriptor (resource, original name, content type)
     */
    AttachmentDownload downloadAttachment(Long applicationId, Long attachmentId);

    /**
     * Computes the analytics overview for the authenticated user's applications.
     *
     * @return the aggregated analytics
     */
    ApplicationAnalyticsResponse getAnalytics();

    /**
     * Immutable descriptor of a downloadable attachment file.
     *
     * @param resource    the file content as a Spring resource
     * @param fileName    the original file name for the download header
     * @param contentType the MIME content type
     */
    record AttachmentDownload(Resource resource, String fileName, String contentType) {
    }

}
