package com.devlaunch.repository;

import com.devlaunch.entity.Announcement;
import com.devlaunch.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link Announcement} entity operations.
 * <p>
 * Provides standard CRUD operations and custom query methods for
 * announcement-related database access, ordered newest first.
 * </p>
 *
 * @author DevLaunch
 */
@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    /**
     * Finds all announcements ordered from most recently created to oldest.
     *
     * @return a list of announcements, or an empty list if none exist
     */
    List<Announcement> findAllByOrderByCreatedAtDesc();

    /**
     * Finds only active announcements, ordered from most recently created
     * to oldest.
     * <p>
     * Powers the user-facing {@code GET /api/announcements/active} endpoint
     * so only live announcements are exposed to regular users.
     * </p>
     *
     * @return a list of active announcements, or an empty list if none exist
     */
    List<Announcement> findAllByIsActiveTrueOrderByCreatedAtDesc();

    /**
     * Finds all announcements authored by the specified user.
     * <p>
     * Used when deleting a user so their authored announcements are removed
     * before the user row, keeping the {@code created_by} foreign key
     * constraint satisfied.
     * </p>
     *
     * @param user the user whose authored announcements to retrieve
     * @return a list of announcements authored by the user,
     *         or an empty list if none exist
     */
    List<Announcement> findByCreatedBy(User user);

}
