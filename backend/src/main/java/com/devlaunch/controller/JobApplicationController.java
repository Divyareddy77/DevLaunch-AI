package com.devlaunch.controller;

import com.devlaunch.dto.request.CreateInterviewNoteRequest;
import com.devlaunch.dto.request.CreateJobApplicationRequest;
import com.devlaunch.dto.request.ScheduleInterviewRequest;
import com.devlaunch.dto.request.UpdateApplicationStatusRequest;
import com.devlaunch.dto.request.UpdateInterviewScheduleRequest;
import com.devlaunch.dto.request.UpdateJobApplicationRequest;
import com.devlaunch.dto.response.ApplicationAnalyticsResponse;
import com.devlaunch.dto.response.ApplicationAttachmentResponse;
import com.devlaunch.dto.response.InterviewNoteResponse;
import com.devlaunch.dto.response.InterviewScheduleResponse;
import com.devlaunch.dto.response.JobApplicationResponse;
import com.devlaunch.dto.response.TimelineEventResponse;
import com.devlaunch.entity.enums.AttachmentCategory;
import com.devlaunch.service.interfaces.JobApplicationService;
import com.devlaunch.service.interfaces.JobApplicationService.AttachmentDownload;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * REST controller for job application tracking operations.
 * <p>
 * Exposes endpoints for creating, retrieving, updating, and deleting
 * the currently authenticated user's job applications, plus the placement
 * management features: quick status moves (Kanban), the application
 * timeline, interview scheduling, private interview notes, document
 * attachments, and an analytics overview. All endpoints require a valid
 * JWT access token and operate exclusively on the authenticated user's
 * own job application data.
 * </p>
 *
 * @author DevLaunch
 */
@RestController
@RequestMapping("/api/job-applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    /**
     * Creates a new job application for the currently authenticated user.
     *
     * @param request the create-job-application request containing application details
     * @return a {@link ResponseEntity} containing the created job application data
     *         with HTTP status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<JobApplicationResponse> createJobApplication(
            @Valid @RequestBody final CreateJobApplicationRequest request) {
        JobApplicationResponse response = jobApplicationService.createJobApplication(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all job applications belonging to the currently authenticated user.
     *
     * @return a {@link ResponseEntity} containing a list of job application data
     *         with HTTP status 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<JobApplicationResponse>> getAllJobApplications() {
        List<JobApplicationResponse> responses = jobApplicationService.getAllJobApplications();
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves the job application analytics overview for the currently
     * authenticated user.
     * <p>
     * This literal path is matched before the {@code /{id}} template so it
     * is never treated as an application ID.
     * </p>
     *
     * @return a {@link ResponseEntity} containing the analytics data
     *         with HTTP status 200 (OK)
     */
    @GetMapping("/analytics")
    public ResponseEntity<ApplicationAnalyticsResponse> getAnalytics() {
        return ResponseEntity.ok(jobApplicationService.getAnalytics());
    }

    /**
     * Retrieves a specific job application by its ID.
     *
     * @param id the job application ID
     * @return a {@link ResponseEntity} containing the job application data
     *         with HTTP status 200 (OK)
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> getJobApplicationById(@PathVariable final Long id) {
        JobApplicationResponse response = jobApplicationService.getJobApplicationById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a specific job application by its ID.
     *
     * @param id      the job application ID to update
     * @param request the update request containing the new application details
     * @return a {@link ResponseEntity} containing the updated job application data
     *         with HTTP status 200 (OK)
     */
    @PutMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> updateJobApplication(
            @PathVariable final Long id,
            @Valid @RequestBody final UpdateJobApplicationRequest request) {
        JobApplicationResponse response = jobApplicationService.updateJobApplication(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates only the status of a job application — used by the Kanban
     * board drag-and-drop so moving a card to another column updates the
     * pipeline instantly.
     *
     * @param id      the job application ID
     * @param request the request containing the new status
     * @return a {@link ResponseEntity} containing the updated job application
     *         data with HTTP status 200 (OK)
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<JobApplicationResponse> updateApplicationStatus(
            @PathVariable final Long id,
            @Valid @RequestBody final UpdateApplicationStatusRequest request) {
        JobApplicationResponse response =
                jobApplicationService.updateApplicationStatus(id, request.getStatus());
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a specific job application by its ID.
     *
     * @param id the job application ID to delete
     * @return a {@link ResponseEntity} containing a success message
     *         with HTTP status 200 (OK)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteJobApplication(@PathVariable final Long id) {
        jobApplicationService.deleteJobApplication(id);
        return ResponseEntity.ok("Job application deleted successfully.");
    }

    /**
     * Retrieves the milestone timeline of a job application.
     *
     * @param id the job application ID
     * @return a {@link ResponseEntity} containing the timeline events
     *         with HTTP status 200 (OK)
     */
    @GetMapping("/{id}/timeline")
    public ResponseEntity<List<TimelineEventResponse>> getTimeline(@PathVariable final Long id) {
        return ResponseEntity.ok(jobApplicationService.getTimeline(id));
    }

    /**
     * Retrieves the scheduled interviews of a job application.
     *
     * @param id the job application ID
     * @return a {@link ResponseEntity} containing the interviews
     *         with HTTP status 200 (OK)
     */
    @GetMapping("/{id}/interviews")
    public ResponseEntity<List<InterviewScheduleResponse>> getInterviews(
            @PathVariable final Long id) {
        return ResponseEntity.ok(jobApplicationService.getInterviews(id));
    }

    /**
     * Schedules a new interview on a job application.
     *
     * @param id      the job application ID
     * @param request the interview details
     * @return a {@link ResponseEntity} containing the created interview
     *         with HTTP status 201 (Created)
     */
    @PostMapping("/{id}/interviews")
    public ResponseEntity<InterviewScheduleResponse> scheduleInterview(
            @PathVariable final Long id,
            @Valid @RequestBody final ScheduleInterviewRequest request) {
        InterviewScheduleResponse response = jobApplicationService.scheduleInterview(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing interview schedule.
     *
     * @param interviewId the interview ID
     * @param request     the updated interview details
     * @return a {@link ResponseEntity} containing the updated interview
     *         with HTTP status 200 (OK)
     */
    @PutMapping("/interviews/{interviewId}")
    public ResponseEntity<InterviewScheduleResponse> updateInterview(
            @PathVariable final Long interviewId,
            @Valid @RequestBody final UpdateInterviewScheduleRequest request) {
        return ResponseEntity.ok(jobApplicationService.updateInterview(interviewId, request));
    }

    /**
     * Cancels an existing interview schedule.
     *
     * @param interviewId the interview ID
     * @return a {@link ResponseEntity} containing the cancelled interview
     *         with HTTP status 200 (OK)
     */
    @DeleteMapping("/interviews/{interviewId}")
    public ResponseEntity<InterviewScheduleResponse> cancelInterview(
            @PathVariable final Long interviewId) {
        return ResponseEntity.ok(jobApplicationService.cancelInterview(interviewId));
    }

    /**
     * Retrieves the private interview notes of a job application.
     *
     * @param id the job application ID
     * @return a {@link ResponseEntity} containing the notes
     *         with HTTP status 200 (OK)
     */
    @GetMapping("/{id}/notes")
    public ResponseEntity<List<InterviewNoteResponse>> getNotes(@PathVariable final Long id) {
        return ResponseEntity.ok(jobApplicationService.getNotes(id));
    }

    /**
     * Adds a private interview note entry to a job application.
     *
     * @param id      the job application ID
     * @param request the note content
     * @return a {@link ResponseEntity} containing the created note
     *         with HTTP status 201 (Created)
     */
    @PostMapping("/{id}/notes")
    public ResponseEntity<InterviewNoteResponse> addNote(
            @PathVariable final Long id,
            @Valid @RequestBody final CreateInterviewNoteRequest request) {
        InterviewNoteResponse response = jobApplicationService.addNote(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Deletes a private interview note from a job application.
     *
     * @param id     the job application ID
     * @param noteId the note ID to delete
     * @return a {@link ResponseEntity} containing a success message
     *         with HTTP status 200 (OK)
     */
    @DeleteMapping("/{id}/notes/{noteId}")
    public ResponseEntity<String> deleteNote(@PathVariable final Long id,
                                             @PathVariable final Long noteId) {
        jobApplicationService.deleteNote(id, noteId);
        return ResponseEntity.ok("Interview note deleted successfully.");
    }

    /**
     * Retrieves the attachments of a job application.
     *
     * @param id the job application ID
     * @return a {@link ResponseEntity} containing the attachments
     *         with HTTP status 200 (OK)
     */
    @GetMapping("/{id}/attachments")
    public ResponseEntity<List<ApplicationAttachmentResponse>> getAttachments(
            @PathVariable final Long id) {
        return ResponseEntity.ok(jobApplicationService.getAttachments(id));
    }

    /**
     * Uploads and attaches a document to a job application.
     *
     * @param id       the job application ID
     * @param file     the uploaded file (multipart form field {@code file})
     * @param category the attachment category (multipart form field {@code category})
     * @return a {@link ResponseEntity} containing the created attachment
     *         metadata with HTTP status 201 (Created)
     */
    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApplicationAttachmentResponse> uploadAttachment(
            @PathVariable final Long id,
            @RequestParam("file") final MultipartFile file,
            @RequestParam(value = "category", defaultValue = "OTHER")
            final AttachmentCategory category) {
        ApplicationAttachmentResponse response =
                jobApplicationService.uploadAttachment(id, file, category);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Downloads an attachment document from a job application.
     *
     * @param id           the job application ID
     * @param attachmentId the attachment ID to download
     * @return a {@link ResponseEntity} streaming the file content
     *         with HTTP status 200 (OK)
     */
    @GetMapping("/{id}/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable final Long id,
                                                       @PathVariable final Long attachmentId) {
        final AttachmentDownload download = jobApplicationService.downloadAttachment(id, attachmentId);

        final String encodedFileName = URLEncoder.encode(download.fileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        download.contentType() == null ? "application/octet-stream"
                                : download.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFileName)
                .body(download.resource());
    }

    /**
     * Deletes an attachment from a job application, removing both the
     * database record and the underlying file.
     *
     * @param id           the job application ID
     * @param attachmentId the attachment ID to delete
     * @return a {@link ResponseEntity} containing a success message
     *         with HTTP status 200 (OK)
     */
    @DeleteMapping("/{id}/attachments/{attachmentId}")
    public ResponseEntity<String> deleteAttachment(@PathVariable final Long id,
                                                   @PathVariable final Long attachmentId) {
        jobApplicationService.deleteAttachment(id, attachmentId);
        return ResponseEntity.ok("Attachment deleted successfully.");
    }

}
