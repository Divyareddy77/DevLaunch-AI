package com.devlaunch.mapper;

import com.devlaunch.dto.request.CreateAchievementRequest;
import com.devlaunch.dto.response.AchievementResponse;
import com.devlaunch.entity.Achievement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for achievement-related object conversions.
 * <p>
 * Handles mapping between {@link CreateAchievementRequest} DTO and
 * {@link Achievement} entity, as well as between {@link Achievement} entity
 * and {@link AchievementResponse} DTO. The resume association and
 * system-managed fields are explicitly ignored during DTO-to-entity
 * mapping — they are set by the service layer.
 * </p>
 *
 * @author DevLaunch
 */
@Mapper(componentModel = "spring")
public interface AchievementMapper {

    /**
     * Maps a create-achievement request DTO to an Achievement entity.
     * <p>
     * Fields with matching names (title, description, dateAchieved) are
     * auto-mapped. The resume association is explicitly ignored — it is
     * populated by the service layer. Inherited fields (id, createdAt,
     * updatedAt) are not exposed by the entity builder and are therefore
     * excluded by default.
     * </p>
     *
     * @param request the create-achievement request containing achievement details
     * @return a new Achievement entity with the mapped fields
     */
    @Mapping(target = "resume", ignore = true)
    Achievement toAchievement(CreateAchievementRequest request);

    /**
     * Maps an Achievement entity to an achievement response DTO.
     * <p>
     * All matching fields (id, title, description, dateAchieved) are
     * auto-mapped. Internal fields such as the resume association and
     * timestamps are excluded from the response.
     * </p>
     *
     * @param achievement the achievement entity to map from
     * @return an achievement response DTO with the entity's data
     */
    AchievementResponse toAchievementResponse(Achievement achievement);

}
