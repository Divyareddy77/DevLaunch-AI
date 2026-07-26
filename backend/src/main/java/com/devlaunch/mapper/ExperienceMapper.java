package com.devlaunch.mapper;

import com.devlaunch.dto.request.CreateExperienceRequest;
import com.devlaunch.dto.response.ExperienceResponse;
import com.devlaunch.entity.Experience;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for experience-related object conversions.
 * <p>
 * Handles mapping between {@link CreateExperienceRequest} DTO and
 * {@link Experience} entity, as well as between {@link Experience} entity
 * and {@link ExperienceResponse} DTO. The resume association and
 * system-managed fields are explicitly ignored during DTO-to-entity
 * mapping — they are set by the service layer.
 * </p>
 *
 * @author DevLaunch
 */
@Mapper(componentModel = "spring")
public interface ExperienceMapper {

    /**
     * Maps a create-experience request DTO to an Experience entity.
     * <p>
     * Fields with matching names (companyName, jobTitle, employmentType,
     * location, startDate, endDate, currentlyWorking, description) are
     * auto-mapped. The resume association is explicitly ignored — it is
     * populated by the service layer. Inherited fields (id, createdAt,
     * updatedAt) are not exposed by the entity builder and are therefore
     * excluded by default.
     * </p>
     *
     * @param request the create-experience request containing professional details
     * @return a new Experience entity with the mapped fields
     */
    @Mapping(target = "resume", ignore = true)
    Experience toExperience(CreateExperienceRequest request);

    /**
     * Maps an Experience entity to an experience response DTO.
     * <p>
     * All matching fields (id, companyName, jobTitle, employmentType,
     * location, startDate, endDate, currentlyWorking, description) are
     * auto-mapped. Internal fields such as the resume association and
     * timestamps are excluded from the response.
     * </p>
     *
     * @param experience the experience entity to map from
     * @return an experience response DTO with the entity's data
     */
    ExperienceResponse toExperienceResponse(Experience experience);

}
