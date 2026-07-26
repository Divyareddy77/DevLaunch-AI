package com.devlaunch.mapper;

import com.devlaunch.dto.request.CreateProjectRequest;
import com.devlaunch.dto.response.ProjectResponse;
import com.devlaunch.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for project-related object conversions.
 * <p>
 * Handles mapping between {@link CreateProjectRequest} DTO and
 * {@link Project} entity, as well as between {@link Project} entity
 * and {@link ProjectResponse} DTO. The resume association and
 * system-managed fields are explicitly ignored during DTO-to-entity
 * mapping — they are set by the service layer.
 * </p>
 *
 * @author DevLaunch
 */
@Mapper(componentModel = "spring")
public interface ProjectMapper {

    /**
     * Maps a create-project request DTO to a Project entity.
     * <p>
     * Fields with matching names (projectName, description, technologies,
     * githubUrl, liveUrl, startDate, endDate, currentlyWorking) are
     * auto-mapped. The resume association is explicitly ignored — it is
     * populated by the service layer. Inherited fields (id, createdAt,
     * updatedAt) are not exposed by the entity builder and are therefore
     * excluded by default.
     * </p>
     *
     * @param request the create-project request containing project details
     * @return a new Project entity with the mapped fields
     */
    @Mapping(target = "resume", ignore = true)
    Project toProject(CreateProjectRequest request);

    /**
     * Maps a Project entity to a project response DTO.
     * <p>
     * All matching fields (id, projectName, description, technologies,
     * githubUrl, liveUrl, startDate, endDate, currentlyWorking) are
     * auto-mapped. Internal fields such as the resume association and
     * timestamps are excluded from the response.
     * </p>
     *
     * @param project the project entity to map from
     * @return a project response DTO with the entity's data
     */
    ProjectResponse toProjectResponse(Project project);

}
