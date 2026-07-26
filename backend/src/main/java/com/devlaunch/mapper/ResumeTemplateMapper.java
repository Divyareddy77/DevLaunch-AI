package com.devlaunch.mapper;

import com.devlaunch.dto.response.ResumeTemplateResponse;
import com.devlaunch.entity.ResumeTemplate;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for resume template-related object conversions.
 * <p>
 * Handles mapping between {@link ResumeTemplate} entity and
 * {@link ResumeTemplateResponse} DTO. No create or update request DTOs
 * are needed because templates are predefined by the system and users
 * do not create or modify them.
 * </p>
 *
 * @author DevLaunch
 */
@Mapper(componentModel = "spring")
public interface ResumeTemplateMapper {

    /**
     * Maps a ResumeTemplate entity to a resume template response DTO.
     * <p>
     * All matching fields (id, name, description, previewImageUrl) are
     * auto-mapped. System-managed fields such as timestamps are excluded
     * from the response.
     * </p>
     *
     * @param resumeTemplate the resume template entity to map from
     * @return a resume template response DTO with the entity's data
     */
    ResumeTemplateResponse toResumeTemplateResponse(ResumeTemplate resumeTemplate);

}
