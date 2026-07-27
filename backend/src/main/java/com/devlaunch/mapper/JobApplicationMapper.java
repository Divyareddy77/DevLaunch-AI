package com.devlaunch.mapper;

import com.devlaunch.dto.request.CreateJobApplicationRequest;
import com.devlaunch.dto.response.JobApplicationResponse;
import com.devlaunch.entity.JobApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for job application-related object conversions.
 * <p>
 * Handles mapping between {@link CreateJobApplicationRequest} DTO and
 * {@link JobApplication} entity, as well as between {@link JobApplication}
 * entity and {@link JobApplicationResponse} DTO. The user and resume
 * associations are explicitly ignored during DTO-to-entity mapping —
 * they are set by the service layer.
 * </p>
 *
 * @author DevLaunch
 */
@Mapper(componentModel = "spring")
public interface JobApplicationMapper {

    /**
     * Maps a create-job-application request DTO to a JobApplication entity.
     * <p>
     * Fields with matching names (companyName, jobRole, companyLocation,
     * jobType, salary, applicationDate, status, jobUrl, notes) are
     * auto-mapped. The user and resume associations are explicitly
     * ignored — they are populated by the service layer. Inherited fields
     * (id, createdAt, updatedAt) are not exposed by the entity builder
     * and are therefore excluded by default.
     * </p>
     *
     * @param request the create-job-application request containing application details
     * @return a new JobApplication entity with the mapped fields
     */
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "resume", ignore = true)
    JobApplication toJobApplication(CreateJobApplicationRequest request);

    /**
     * Maps a JobApplication entity to a job application response DTO.
     * <p>
     * All matching fields (id, companyName, jobRole, companyLocation,
     * jobType, salary, applicationDate, status, jobUrl, notes) are
     * auto-mapped. The resume ID is explicitly mapped from the nested
     * resume association. Internal fields such as the user association
     * and timestamps are excluded from the response.
     * </p>
     *
     * @param jobApplication the job application entity to map from
     * @return a job application response DTO with the entity's data
     */
    @Mapping(target = "resumeId", source = "resume.id")
    JobApplicationResponse toJobApplicationResponse(JobApplication jobApplication);

}
