package com.devlaunch.repository;

import com.devlaunch.entity.User;
import com.devlaunch.entity.XpHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for the XP ledger entries.
 *
 * @author DevLaunch
 */
@Repository
public interface XpHistoryRepository extends JpaRepository<XpHistory, Long> {

    /**
     * Finds every XP entry for a user, newest first.
     *
     * @param user the user
     * @return the user's XP ledger, or an empty list if none
     */
    List<XpHistory> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Finds the most recent XP entries for a user, newest first.
     *
     * @param user the user
     * @return up to the given number of recent entries
     */
    List<XpHistory> findTop50ByUserOrderByCreatedAtDesc(User user);

    /**
     * Sums a user's total XP. The sum is the single source of truth for
     * the user's level calculation.
     *
     * @param user the user
     * @return the total XP, or 0 when the user has no entries yet
     */
    @Query("SELECT COALESCE(SUM(x.amount), 0) FROM XpHistory x WHERE x.user = :user")
    long sumByUser(@Param("user") User user);

}
