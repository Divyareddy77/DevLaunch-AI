package com.devlaunch.repository;

import com.devlaunch.entity.ReadinessSnapshot;
import com.devlaunch.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for {@link ReadinessSnapshot} entity operations.
 * <p>
 * Provides access to the most recent placement readiness snapshot per
 * user, used by the dashboard to compare the current readiness score
 * against the previously recorded one.
 * </p>
 *
 * @author DevLaunch
 */
@Repository
public interface ReadinessSnapshotRepository extends JpaRepository<ReadinessSnapshot, Long> {

    /**
     * Finds the most recently created readiness snapshot for the
     * specified user, or an empty {@link Optional} if the user has no
     * recorded snapshot yet.
     *
     * @param user the user whose latest snapshot to retrieve
     * @return the latest snapshot for the user, or empty if none exist
     */
    Optional<ReadinessSnapshot> findTopByUserOrderByCreatedAtDesc(User user);

    /**
     * Counts the readiness snapshots belonging to the specified user.
     * <p>
     * Used by the gamification engine to evaluate the Power User
     * achievement (modules used).
     * </p>
     *
     * @param user the user whose snapshots to count
     * @return the number of snapshots belonging to the user
     */
    long countByUser(User user);

}
