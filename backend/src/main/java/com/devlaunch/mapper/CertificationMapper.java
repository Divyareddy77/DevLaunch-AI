package com.devlaunch.mapper;

import com.devlaunch.dto.request.CreateCertificationRequest;
import com.devlaunch.dto.response.CertificationResponse;
import com.devlaunch.entity.Certification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for certification-related object conversions.
 * <p>
 * Handles mapping between {@link CreateCertificationRequest} DTO and
 * {@link Certification} entity, as well as between {@link Certification} entity
 * and {@link CertificationResponse} DTO. The resume association and
 * system-managed fields are explicitly ignored during DTO-to-entity
 * mapping — they are set by the service layer.
 * </p>
 *
 * @author DevLaunch
 */
@Mapper(componentModel = "spring")
public interface CertificationMapper {

    /**
     * Maps a create-certification request DTO to a Certification entity.
     * <p>
     * Fields with matching names (certificationName, issuingOrganization,
     * issueDate, expiryDate, credentialId, credentialUrl) are
     * auto-mapped. The resume association is explicitly ignored — it is
     * populated by the service layer. Inherited fields (id, createdAt,
     * updatedAt) are not exposed by the entity builder and are therefore
     * excluded by default.
     * </p>
     *
     * @param request the create-certification request containing certification details
     * @return a new Certification entity with the mapped fields
     */
    @Mapping(target = "resume", ignore = true)
    Certification toCertification(CreateCertificationRequest request);

    /**
     * Maps a Certification entity to a certification response DTO.
     * <p>
     * All matching fields (id, certificationName, issuingOrganization,
     * issueDate, expiryDate, credentialId, credentialUrl) are
     * auto-mapped. Internal fields such as the resume association and
     * timestamps are excluded from the response.
     * </p>
     *
     * @param certification the certification entity to map from
     * @return a certification response DTO with the entity's data
     */
    CertificationResponse toCertificationResponse(Certification certification);

}
