package com.devlaunch.controller;

import com.devlaunch.dto.response.AchievementDefinitionResponse;
import com.devlaunch.dto.response.AchievementProgressResponse;
import com.devlaunch.dto.response.AchievementSummaryResponse;
import com.devlaunch.dto.response.UnlockedAchievementResponse;
import com.devlaunch.dto.response.XpHistoryResponse;
import com.devlaunch.service.interfaces.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for the gamification module.
 * <p>
 * Exposes the achievement catalog, the authenticated user's badge progress,
 * unlocks, XP ledger, and the aggregated gamification summary. All
 * user-scoped endpoints require a valid JWT access token and operate
 * exclusively on the authenticated user's own data. The read responses are
 * cached in Redis and evicted automatically whenever XP or badges change.
 * </p>
 *
 * @author DevLaunch
 */
@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;

    /**
     * Retrieves the full static achievement catalog.
     *
     * @return the achievement catalog
     */
    @GetMapping
    public ResponseEntity<List<AchievementDefinitionResponse>> getAchievementCatalog() {
        return ResponseEntity.ok(gamificationService.getAchievementCatalog());
    }

    /**
     * Retrieves the badges the authenticated user has unlocked, newest first.
     *
     * @return the unlocked badges
     */
    @GetMapping("/user")
    public ResponseEntity<List<UnlockedAchievementResponse>> getUserAchievements() {
        return ResponseEntity.ok(gamificationService.getUserAchievements());
    }

    /**
     * Retrieves the gamification summary: level, XP, next level progress,
     * badge completion, and recent unlocks.
     *
     * @return the summary
     */
    @GetMapping("/summary")
    public ResponseEntity<AchievementSummaryResponse> getSummary() {
        return ResponseEntity.ok(gamificationService.getSummary());
    }

    /**
     * Retrieves the authenticated user's recent XP ledger, newest first.
     *
     * @return the XP history entries
     */
    @GetMapping("/history")
    public ResponseEntity<List<XpHistoryResponse>> getXpHistory() {
        return ResponseEntity.ok(gamificationService.getXpHistory());
    }

    /**
     * Retrieves per-badge progress for the authenticated user (locked and
     * unlocked badges with their current progress towards the target).
     *
     * @return the per-badge progress list
     */
    @GetMapping("/progress")
    public ResponseEntity<List<AchievementProgressResponse>> getProgress() {
        return ResponseEntity.ok(gamificationService.getProgress());
    }

}
