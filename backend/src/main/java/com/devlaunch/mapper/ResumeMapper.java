package com.devlaunch.mapper;

import com.devlaunch.dto.request.CreateResumeRequest;
import com.devlaunch.dto.response.ResumeResponse;
import com.devlaunch.dto.response.ResumeTemplateResponse;
import com.devlaunch.entity.Resume;
import com.devlaunch.entity.ResumeTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for resume-related object conversions.
 * <p>
 * Handles mapping between {@link CreateResumeRequest} DTO and {@link Resume} entity,
 * as well as between {@link Resume} entity and {@link ResumeResponse} DTO.
 * The user association and system-managed fields are explicitly ignored during
 * DTO-to-entity mapping — they are set by the service layer.
 * </p>
 *
 * @author DevLaunch
 */
@Mapper(componentModel = "spring")
public interface ResumeMapper {

    /**
     * Maps a create-resume request DTO to a Resume entity.
     * <p>
     * Fields with matching names (headline, summary, linkedinUrl, githubUrl,
     * portfolioUrl) are auto-mapped. The user association is explicitly
     * ignored — it is populated by the service layer. Inherited fields
     * (id, createdAt, updatedAt) are not exposed by the entity builder and
     * are therefore excluded by default.
     * </p>
     *
     * @param request the create-resume request containing professional details
     * @return a new Resume entity with the mapped fields
     */
    @Mapping(target = "user", ignore = true)
    Resume toResume(CreateResumeRequest request);

    /**
     * Maps a Resume entity to a resume response DTO.
     * <p>
     * All matching fields (id, headline, summary, linkedinUrl, githubUrl,
     * portfolioUrl) are auto-mapped. The template association is mapped
     * via the discovered {@link #toResumeTemplateResponse(ResumeTemplate)}
     * method so that the selected template is included in the response.
     * Internal fields such as the user association and timestamps are
     * excluded from the response.
     * </p>
     *
     * @param resume the resume entity to map from
     * @return a resume response DTO with the entity's data
     */
    ResumeResponse toResumeResponse(Resume resume);

    /**
     * Maps a ResumeTemplate entity to a resume template response DTO.
     * <p>
     * Used when returning template data as part of a resume response.
     * All matching fields (id, name, description, previewImageUrl) are
     * auto-mapped.
     * </p>
     *
     * @param resumeTemplate the resume template entity to map from
     * @return a resume template response DTO with the entity's data
     */
    ResumeTemplateResponse toResumeTemplateResponse(ResumeTemplate resumeTemplate);

}
