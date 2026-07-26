package com.devlaunch.mapper;

import com.devlaunch.dto.request.CreateEducationRequest;
import com.devlaunch.dto.response.EducationResponse;
import com.devlaunch.entity.Education;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for education-related object conversions.
 * <p>
 * Handles mapping between {@link CreateEducationRequest} DTO and
 * {@link Education} entity, as well as between {@link Education} entity
 * and {@link EducationResponse} DTO. The resume association and
 * system-managed fields are explicitly ignored during DTO-to-entity
 * mapping — they are set by the service layer.
 * </p>
 *
 * @author DevLaunch
 */
@Mapper(componentModel = "spring")
public interface EducationMapper {

    /**
     * Maps a create-education request DTO to an Education entity.
     * <p>
     * Fields with matching names (institutionName, degree, fieldOfStudy,
     * grade, startDate, endDate, currentlyStudying, description) are
     * auto-mapped. The resume association is explicitly ignored — it is
     * populated by the service layer. Inherited fields (id, createdAt,
     * updatedAt) are not exposed by the entity builder and are therefore
     * excluded by default.
     * </p>
     *
     * @param request the create-education request containing academic details
     * @return a new Education entity with the mapped fields
     */
    @Mapping(target = "resume", ignore = true)
    Education toEducation(CreateEducationRequest request);

    /**
     * Maps an Education entity to an education response DTO.
     * <p>
     * All matching fields (id, institutionName, degree, fieldOfStudy,
     * grade, startDate, endDate, currentlyStudying, description) are
     * auto-mapped. Internal fields such as the resume association and
     * timestamps are excluded from the response.
     * </p>
     *
     * @param education the education entity to map from
     * @return an education response DTO with the entity's data
     */
    EducationResponse toEducationResponse(Education education);

}
