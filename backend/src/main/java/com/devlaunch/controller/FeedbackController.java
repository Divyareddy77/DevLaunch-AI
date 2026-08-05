package com.devlaunch.controller;

import com.devlaunch.dto.request.FeedbackSubmissionRequest;
import com.devlaunch.service.interfaces.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user-facing feedback submission.
 * <p>
 * Exposes a single authenticated endpoint allowing any logged-in user to
 * submit platform feedback. The submitted entries are reviewed and
 * moderated by administrators through the admin module
 * ({@link AdminController}).
 * </p>
 *
 * @author DevLaunch
 */
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final AdminService adminService;

    /**
     * Submits feedback on behalf of the currently authenticated user.
     *
     * @param request the feedback payload containing the message
     * @return a 200 OK response
     */
    @PostMapping
    public ResponseEntity<Void> submitFeedback(
            @Valid @RequestBody final FeedbackSubmissionRequest request) {
        adminService.submitFeedback(request);
        return ResponseEntity.ok().build();
    }

}
