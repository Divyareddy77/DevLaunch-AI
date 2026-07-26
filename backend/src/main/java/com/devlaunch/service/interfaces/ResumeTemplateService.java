package com.devlaunch.service.interfaces;

import com.devlaunch.dto.response.ResumeTemplateResponse;
import com.devlaunch.exception.ResourceNotFoundException;

import java.util.List;

/**
 * Service interface for resume template retrieval operations.
 * <p>
 * Defines the contract for retrieving predefined resume templates.
 * Templates are created and managed by the system — users do not
 * create, update, or delete them. All templates are seeded at
 * application startup.
 * </p>
 *
 * @author DevLaunch
 */
public interface ResumeTemplateService {

    /**
     * Retrieves all available resume templates.
     *
     * @return a list of all predefined resume templates, or an empty list if none exist
     */
    List<ResumeTemplateResponse> getAllTemplates();

    /**
     * Retrieves a specific resume template by its ID.
     *
     * @param templateId the ID of the template to retrieve
     * @return the template data
     * @throws ResourceNotFoundException if the template is not found
     */
    ResumeTemplateResponse getTemplateById(Long templateId);

}
