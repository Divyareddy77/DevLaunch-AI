package com.devlaunch.controller;

import com.devlaunch.dto.response.ResumeTemplateResponse;
import com.devlaunch.service.interfaces.ResumeTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for resume template retrieval operations.
 * <p>
 * Exposes endpoints for viewing predefined resume templates. Templates
 * are system-defined and cannot be created, updated, or deleted by users.
 * All endpoints require a valid JWT access token.
 * </p>
 *
 * @author DevLaunch
 */
@RestController
@RequestMapping("/api/resume-templates")
@RequiredArgsConstructor
public class ResumeTemplateController {

    private final ResumeTemplateService resumeTemplateService;

    /**
     * Retrieves all available resume templates.
     * <p>
     * Delegates to {@link ResumeTemplateService#getAllTemplates()} to fetch
     * all predefined templates.
     * </p>
     *
     * @return a {@link ResponseEntity} containing a list of template data
     *         with HTTP status 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<ResumeTemplateResponse>> getAllTemplates() {
        List<ResumeTemplateResponse> responses = resumeTemplateService.getAllTemplates();
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves a specific resume template by its ID.
     * <p>
     * Delegates to {@link ResumeTemplateService#getTemplateById(Long)} to
     * fetch the template.
     * </p>
     *
     * @param templateId the ID of the template to retrieve
     * @return a {@link ResponseEntity} containing the template data
     *         with HTTP status 200 (OK)
     */
    @GetMapping("/{templateId}")
    public ResponseEntity<ResumeTemplateResponse> getTemplateById(
            @PathVariable final Long templateId) {
        ResumeTemplateResponse response = resumeTemplateService.getTemplateById(templateId);
        return ResponseEntity.ok(response);
    }

}
