package com.devlaunch.mapper;

import com.devlaunch.dto.request.CreateStudyPlannerRequest;
import com.devlaunch.dto.response.StudyPlannerResponse;
import com.devlaunch.entity.StudyPlanner;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for study planner-related object conversions.
 * <p>
 * Handles mapping between {@link CreateStudyPlannerRequest} DTO and
 * {@link StudyPlanner} entity, as well as between {@link StudyPlanner}
 * entity and {@link StudyPlannerResponse} DTO. The user association is
 * explicitly ignored during DTO-to-entity mapping — it is set by the
 * service layer.
 * </p>
 *
 * @author DevLaunch
 */
@Mapper(componentModel = "spring")
public interface StudyPlannerMapper {

    /**
     * Maps a create-study-planner request DTO to a StudyPlanner entity.
     * <p>
     * Fields with matching names (title, description, studyDate, startTime,
     * endTime, priority, status) are auto-mapped. The user association is
     * explicitly ignored — it is populated by the service layer. Inherited
     * fields (id, createdAt, updatedAt) are not exposed by the entity builder
     * and are therefore excluded by default.
     * </p>
     *
     * @param request the create-study-planner request containing study session details
     * @return a new StudyPlanner entity with the mapped fields
     */
    @Mapping(target = "user", ignore = true)
    StudyPlanner toStudyPlanner(CreateStudyPlannerRequest request);

    /**
     * Maps a StudyPlanner entity to a study planner response DTO.
     * <p>
     * All matching fields (id, title, description, studyDate, startTime,
     * endTime, priority, status) are auto-mapped. Internal fields such as
     * the user association and timestamps are excluded from the response.
     * </p>
     *
     * @param studyPlanner the study planner entity to map from
     * @return a study planner response DTO with the entity's data
     */
    StudyPlannerResponse toStudyPlannerResponse(StudyPlanner studyPlanner);

}
