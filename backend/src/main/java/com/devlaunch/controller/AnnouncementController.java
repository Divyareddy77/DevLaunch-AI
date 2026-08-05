package com.devlaunch.controller;

import com.devlaunch.dto.response.AdminAnnouncementResponse;
import com.devlaunch.service.interfaces.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for user-facing announcements.
 * <p>
 * Exposes a single authenticated endpoint that returns the live
 * (active) announcements for regular users. Announcement creation,
 * editing, and deletion remain exclusive to the admin module
 * ({@link AdminController}).
 * </p>
 *
 * @author DevLaunch
 */
@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AdminService adminService;

    /**
     * Retrieves the active announcements visible to users, newest first.
     * <p>
     * Requires a valid JWT access token (covered by the default
     * {@code anyRequest().authenticated()} rule in {@code SecurityConfig});
     * inactive announcements are never returned.
     * </p>
     *
     * @return a {@link ResponseEntity} containing the active announcements
     *         with HTTP status 200 (OK)
     */
    @GetMapping("/active")
    public ResponseEntity<List<AdminAnnouncementResponse>> getActiveAnnouncements() {
        return ResponseEntity.ok(adminService.getActiveAnnouncements());
    }

}
