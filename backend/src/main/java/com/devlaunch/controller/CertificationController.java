package com.devlaunch.controller;

import com.devlaunch.dto.request.CreateCertificationRequest;
import com.devlaunch.dto.request.UpdateCertificationRequest;
import com.devlaunch.dto.response.CertificationResponse;
import com.devlaunch.service.interfaces.CertificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for certification record management operations.
 * <p>
 * Exposes endpoints for creating, retrieving, updating, and deleting
 * certification records associated with a specific resume owned by the
 * currently authenticated user. All endpoints require a valid JWT
 * access token and verify that the target resume belongs to the
 * authenticated user and that the certification record belongs to that
 * resume.
 * </p>
 *
 * @author DevLaunch
 */
@RestController
@RequestMapping("/api/resumes/{resumeId}/certifications")
@RequiredArgsConstructor
public class CertificationController {

    private final CertificationService certificationService;

    /**
     * Creates a new certification record for the specified resume.
     * <p>
     * Accepts the certification details, validates the input, delegates
     * creation to {@link CertificationService#createCertification(Long, CreateCertificationRequest)},
     * and returns the newly created certification record data.
     * </p>
     *
     * @param resumeId the ID of the resume to associate the certification with
     * @param request  the create-certification request containing certification details
     * @return a {@link ResponseEntity} containing the created certification record
     *         with HTTP status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<CertificationResponse> createCertification(
            @PathVariable final Long resumeId,
            @Valid @RequestBody final CreateCertificationRequest request) {
        CertificationResponse response = certificationService.createCertification(resumeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all certification records belonging to the specified resume.
     * <p>
     * Delegates to {@link CertificationService#getAllCertifications(Long)} to fetch
     * all certification records for the specified resume.
     * </p>
     *
     * @param resumeId the ID of the resume whose certification records to retrieve
     * @return a {@link ResponseEntity} containing a list of certification records
     *         with HTTP status 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<CertificationResponse>> getAllCertifications(
            @PathVariable final Long resumeId) {
        List<CertificationResponse> responses = certificationService.getAllCertifications(resumeId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves a specific certification record by its ID within the specified resume.
     * <p>
     * Delegates to {@link CertificationService#getCertificationById(Long, Long)} to fetch
     * the certification record. The record must belong to the specified resume,
     * and the resume must belong to the authenticated user.
     * </p>
     *
     * @param resumeId         the ID of the resume that owns the certification record
     * @param certificationId  the ID of the certification record to retrieve
     * @return a {@link ResponseEntity} containing the certification record data
     *         with HTTP status 200 (OK)
     */
    @GetMapping("/{certificationId}")
    public ResponseEntity<CertificationResponse> getCertificationById(
            @PathVariable final Long resumeId,
            @PathVariable final Long certificationId) {
        CertificationResponse response = certificationService.getCertificationById(resumeId, certificationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a specific certification record by its ID within the specified resume.
     * <p>
     * Accepts the updated certification details, validates the input, and
     * delegates the update to {@link CertificationService#updateCertification(Long, Long, UpdateCertificationRequest)}.
     * Returns the refreshed certification record data after the update. The record
     * must belong to the specified resume, and the resume must belong to the
     * authenticated user.
     * </p>
     *
     * @param resumeId         the ID of the resume that owns the certification record
     * @param certificationId  the ID of the certification record to update
     * @param request          the update request containing the new certification details
     * @return a {@link ResponseEntity} containing the updated certification record
     *         with HTTP status 200 (OK)
     */
    @PutMapping("/{certificationId}")
    public ResponseEntity<CertificationResponse> updateCertification(
            @PathVariable final Long resumeId,
            @PathVariable final Long certificationId,
            @Valid @RequestBody final UpdateCertificationRequest request) {
        CertificationResponse response = certificationService.updateCertification(resumeId, certificationId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a specific certification record by its ID within the specified resume.
     * <p>
     * Delegates the deletion to {@link CertificationService#deleteCertification(Long, Long)}.
     * The record must belong to the specified resume, and the resume must belong
     * to the authenticated user.
     * </p>
     *
     * @param resumeId         the ID of the resume that owns the certification record
     * @param certificationId  the ID of the certification record to delete
     * @return a {@link ResponseEntity} containing a success message
     *         with HTTP status 200 (OK)
     */
    @DeleteMapping("/{certificationId}")
    public ResponseEntity<String> deleteCertification(
            @PathVariable final Long resumeId,
            @PathVariable final Long certificationId) {
        certificationService.deleteCertification(resumeId, certificationId);
        return ResponseEntity.ok("Certification deleted successfully.");
    }

}
