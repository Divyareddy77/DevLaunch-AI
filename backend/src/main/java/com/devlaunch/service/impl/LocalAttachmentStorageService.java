package com.devlaunch.service.impl;

import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.service.interfaces.AttachmentStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Local file-system implementation of {@link AttachmentStorageService}.
 * <p>
 * Stores attachment files under {@code {upload-dir}/job-applications},
 * where {@code upload-dir} defaults to {@code ./uploads} and can be
 * overridden with the {@code devlaunch.upload.dir} property. Each file is
 * stored under a UUID-prefixed opaque name so the original file name never
 * touches the disk path, and every path is resolved against the configured
 * directory to prevent path-traversal.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class LocalAttachmentStorageService implements AttachmentStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalAttachmentStorageService.class);

    private final Path storageDirectory;

    /**
     * Constructs the local storage service with the configured upload
     * directory.
     *
     * @param uploadDir the configured upload directory (default {@code ./uploads})
     */
    public LocalAttachmentStorageService(
            @Value("${devlaunch.upload.dir:./uploads}") final String uploadDir) {
        this.storageDirectory = Paths.get(uploadDir)
                .toAbsolutePath().normalize().resolve("job-applications");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String store(final MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Attachment file must not be empty");
        }

        try {
            Files.createDirectories(storageDirectory);

            final String sanitizedOriginal = sanitizeFileName(file.getOriginalFilename());
            final String storedFileName = UUID.randomUUID() + "_" + sanitizedOriginal;

            Files.copy(file.getInputStream(), storageDirectory.resolve(storedFileName),
                    StandardCopyOption.REPLACE_EXISTING);

            log.debug("Attachment stored as {}", storedFileName);
            return storedFileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store attachment file", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource loadAsResource(final String storedFileName) {
        final Path file = resolveStoredFile(storedFileName);
        final Resource resource = new FileSystemResource(file);
        if (!resource.exists() || !resource.isReadable()) {
            throw new ResourceNotFoundException(
                    "Attachment file " + storedFileName + " not found");
        }
        return resource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final String storedFileName) {
        try {
            final Path file = resolveStoredFile(storedFileName);
            final boolean deleted = Files.deleteIfExists(file);
            if (deleted) {
                log.debug("Attachment {} deleted from disk", storedFileName);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete attachment file", e);
        }
    }

    /**
     * Resolves an opaque stored file name against the storage directory,
     * guarding against path traversal so a caller can never escape the
     * configured directory.
     *
     * @param storedFileName the opaque stored file name
     * @return the normalized absolute path of the file
     */
    private Path resolveStoredFile(final String storedFileName) {
        final Path file = storageDirectory.resolve(storedFileName).normalize();
        if (!file.startsWith(storageDirectory)) {
            throw new ResourceNotFoundException("Attachment file not found");
        }
        return file;
    }

    /**
     * Removes any path components and unsafe characters from an uploaded
     * file name so it can be embedded in the opaque stored name safely.
     *
     * @param originalName the original file name, may be {@code null}
     * @return a sanitized file name, or {@code "file"} as a fallback
     */
    private String sanitizeFileName(final String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "file";
        }
        final String cleaned = Paths.get(originalName).getFileName().toString()
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        return cleaned.isBlank() ? "file" : cleaned;
    }

}
