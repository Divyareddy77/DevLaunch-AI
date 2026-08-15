package com.devlaunch.mapper;

import com.devlaunch.dto.request.CreateSkillRequest;
import com.devlaunch.dto.response.SkillResponse;
import com.devlaunch.entity.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for skill-related object conversions.
 * <p>
 * Handles mapping between {@link CreateSkillRequest} DTO and
 * {@link Skill} entity, as well as between {@link Skill} entity
 * and {@link SkillResponse} DTO. The resume association and
 * system-managed fields are explicitly ignored during DTO-to-entity
 * mapping — they are set by the service layer.
 * </p>
 *
 * @author DevLaunch
 */
@Mapper(componentModel = "spring")
public interface SkillMapper {

    /**
     * Maps a create-skill request DTO to a Skill entity.
     * <p>
     * Fields with matching names (skillName, proficiency) are
     * auto-mapped. The resume association is explicitly ignored — it is
     * populated by the service layer. Inherited fields (id, createdAt,
     * updatedAt) are not exposed by the entity builder and are therefore
     * excluded by default.
     * </p>
     *
     * @param request the create-skill request containing skill details
     * @return a new Skill entity with the mapped fields
     */
    @Mapping(target = "resume", ignore = true)
    Skill toSkill(CreateSkillRequest request);

    /**
     * Maps a Skill entity to a skill response DTO.
     * <p>
     * All matching fields (id, skillName, proficiency) are
     * auto-mapped. Internal fields such as the resume association and
     * timestamps are excluded from the response.
     * </p>
     *
     * @param skill the skill entity to map from
     * @return a skill response DTO with the entity's data
     */
    SkillResponse toSkillResponse(Skill skill);

}
