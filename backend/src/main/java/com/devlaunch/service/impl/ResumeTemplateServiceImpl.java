package com.devlaunch.service.impl;

import com.devlaunch.dto.response.ResumeTemplateResponse;
import com.devlaunch.entity.ResumeTemplate;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.mapper.ResumeTemplateMapper;
import com.devlaunch.repository.ResumeTemplateRepository;
import com.devlaunch.service.interfaces.ResumeTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link ResumeTemplateService} providing retrieval
 * of predefined resume templates.
 * <p>
 * Templates are created and managed by the system — users do not
 * create, update, or delete them. All templates are seeded at
 * application startup via {@link com.devlaunch.config.DataInitializer}.
 * This service delegates persistence and mapping to
 * {@link ResumeTemplateRepository} and {@link ResumeTemplateMapper}
 * respectively.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class ResumeTemplateServiceImpl implements ResumeTemplateService {

    private final ResumeTemplateRepository resumeTemplateRepository;
    private final ResumeTemplateMapper resumeTemplateMapper;

    /**
     * Constructs the resume template service with the required dependencies.
     *
     * @param resumeTemplateRepository repository for resume template data access
     * @param resumeTemplateMapper     mapper for DTO-entity conversions
     */
    public ResumeTemplateServiceImpl(final ResumeTemplateRepository resumeTemplateRepository,
                                     final ResumeTemplateMapper resumeTemplateMapper) {
        this.resumeTemplateRepository = resumeTemplateRepository;
        this.resumeTemplateMapper = resumeTemplateMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResumeTemplateResponse> getAllTemplates() {
        final List<ResumeTemplate> templates = resumeTemplateRepository.findAll();
        return templates.stream()
                .map(resumeTemplateMapper::toResumeTemplateResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ResumeTemplateResponse getTemplateById(final Long templateId) {
        final ResumeTemplate template = resumeTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resume template with id " + templateId + " not found"));
        return resumeTemplateMapper.toResumeTemplateResponse(template);
    }

}
