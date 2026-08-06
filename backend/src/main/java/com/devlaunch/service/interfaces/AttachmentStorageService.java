package com.devlaunch.service.interfaces;

import com.devlaunch.exception.ResourceNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for persisting job application attachment files.
 * <p>
 * Decouples the job tracker from the concrete storage location so the
 * storage strategy can evolve (local disk today, object storage later)
 * without touching the application service. Only the opaque stored file
 * name is handled here — all display metadata lives on the
 * {@code ApplicationAttachment} entity.
 * </p>
 *
 * @author DevLaunch
 */
public interface AttachmentStorageService {

    /**
     * Stores an uploaded file and returns the opaque stored file name.
     *
     * @param file the uploaded file, must not be empty
     * @return the opaque stored file name
     * @throws IllegalArgumentException if the file is empty
     */
    String store(MultipartFile file);

    /**
     * Resolves a stored file name to a readable {@link Resource}.
     *
     * @param storedFileName the opaque stored file name
     * @return the file as a resource
     * @throws ResourceNotFoundException if the file does not exist on disk
     */
    Resource loadAsResource(String storedFileName);

    /**
     * Deletes the stored file for the given opaque name, if it exists.
     *
     * @param storedFileName the opaque stored file name
     */
    void delete(String storedFileName);

}
