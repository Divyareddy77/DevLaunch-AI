package com.devlaunch.service.interfaces;

import com.devlaunch.dto.request.CreateCertificationRequest;
import com.devlaunch.dto.request.UpdateCertificationRequest;
import com.devlaunch.dto.response.CertificationResponse;
import com.devlaunch.exception.ResourceNotFoundException;

import java.util.List;

/**
 * Service interface for certification record management operations.
 * <p>
 * Defines the contract for creating, retrieving, updating, and deleting
 * certification records associated with the currently authenticated user's
 * resumes. Each certification record belongs to a specific resume, and all
 * operations verify that the resume belongs to the authenticated user
 * and that the certification record belongs to that resume.
 * </p>
 *
 * @author DevLaunch
 */
public interface CertificationService {

    /**
     * Creates a new certification record for the specified resume.
     * <p>
     * The resume must belong to the currently authenticated user.
     * </p>
     *
     * @param resumeId the ID of the resume to associate the certification with
     * @param request  the create-certification request containing certification details
     * @return the newly created certification record data
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   or the resume is not found, or the resume
     *                                   does not belong to the user
     */
    CertificationResponse createCertification(Long resumeId, CreateCertificationRequest request);

    /**
     * Retrieves all certification records belonging to the specified resume.
     * <p>
     * The resume must belong to the currently authenticated user.
     * </p>
     *
     * @param resumeId the ID of the resume whose certification records to retrieve
     * @return a list of certification records for the specified resume,
     *         or an empty list if none exist
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   or the resume is not found, or the resume
     *                                   does not belong to the user
     */
    List<CertificationResponse> getAllCertifications(Long resumeId);

    /**
     * Retrieves a specific certification record by its ID, ensuring it belongs
     * to the specified resume and that the resume belongs to the currently
     * authenticated user.
     *
     * @param resumeId         the ID of the resume that owns the certification record
     * @param certificationId  the ID of the certification record to retrieve
     * @return the certification record data
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   the resume is not found, the resume does
     *                                   not belong to the user, or the certification
     *                                   record is not found or does not belong
     *                                   to the specified resume
     */
    CertificationResponse getCertificationById(Long resumeId, Long certificationId);

    /**
     * Updates a specific certification record by its ID, ensuring it belongs
     * to the specified resume and that the resume belongs to the currently
     * authenticated user.
     *
     * @param resumeId         the ID of the resume that owns the certification record
     * @param certificationId  the ID of the certification record to update
     * @param request          the update request containing the new certification details
     * @return the updated certification record data
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   the resume is not found, the resume does
     *                                   not belong to the user, or the certification
     *                                   record is not found or does not belong
     *                                   to the specified resume
     */
    CertificationResponse updateCertification(Long resumeId, Long certificationId, UpdateCertificationRequest request);

    /**
     * Deletes a specific certification record by its ID, ensuring it belongs
     * to the specified resume and that the resume belongs to the currently
     * authenticated user.
     *
     * @param resumeId         the ID of the resume that owns the certification record
     * @param certificationId  the ID of the certification record to delete
     * @throws ResourceNotFoundException if the authenticated user is not found,
     *                                   the resume is not found, the resume does
     *                                   not belong to the user, or the certification
     *                                   record is not found or does not belong
     *                                   to the specified resume
     */
    void deleteCertification(Long resumeId, Long certificationId);

}
