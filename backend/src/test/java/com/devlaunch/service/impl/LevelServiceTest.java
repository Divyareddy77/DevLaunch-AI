package com.devlaunch.service.impl;

import com.devlaunch.service.impl.LevelService.LevelInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link LevelService}.
 * <p>
 * Verifies the explicit level table from the product spec (levels 1–6 at
 * 0/100/250/500/900/1400 XP) and the automatic continuation of the curve
 * from level 7 onward.
 * </p>
 *
 * @author DevLaunch
 */
class LevelServiceTest {

    private final LevelService levelService = new LevelService();

    @Test
    @DisplayName("maps the explicit level thresholds from the product spec")
    void mapsExplicitThresholds() {
        assertEquals(1, levelService.levelFor(0));
        assertEquals(1, levelService.levelFor(99));
        assertEquals(2, levelService.levelFor(100));
        assertEquals(2, levelService.levelFor(249));
        assertEquals(3, levelService.levelFor(250));
        assertEquals(4, levelService.levelFor(500));
        assertEquals(5, levelService.levelFor(900));
        assertEquals(6, levelService.levelFor(1400));
    }

    @Test
    @DisplayName("continues the curve automatically beyond level 6")
    void continuesCurveBeyondLevelSix() {
        // Level 7 needs 500 more XP (1900), level 8 needs 550 more (2450).
        assertEquals(6, levelService.levelFor(1899));
        assertEquals(7, levelService.levelFor(1900));
        assertEquals(7, levelService.levelFor(2449));
        assertEquals(8, levelService.levelFor(2450));
        // A very large XP value resolves to a high level without overflow
        // (L7=1900, L8=2450, L9=3050, L10=3700, L11=4400, L12=5150, L13=5950).
        assertEquals(13, levelService.levelFor(6_000));
    }

    @Test
    @DisplayName("levelInfo exposes the XP boundaries and progress of the current level")
    void levelInfoExposesBoundaries() {
        final LevelInfo info = levelService.levelInfo(260);

        assertEquals(3, info.level());
        assertEquals("Achiever", info.levelTitle());
        assertEquals(260L, info.totalXp());
        assertEquals(250L, info.currentLevelXp());
        assertEquals(500L, info.nextLevelXp());
        assertEquals(4, info.nextLevel());
        assertEquals(10L, info.xpIntoLevel());
        assertEquals(240L, info.xpNeededForNext());
        assertEquals(4.0, info.progressPercent());
    }

    @Test
    @DisplayName("levelInfo at the start of a level reports zero progress")
    void levelInfoAtLevelStart() {
        final LevelInfo info = levelService.levelInfo(250);

        assertEquals(3, info.level());
        assertEquals(0L, info.xpIntoLevel());
        assertEquals(250L, info.xpNeededForNext());
        assertEquals(0.0, info.progressPercent());
    }

    @Test
    @DisplayName("thresholdFor returns the cumulative threshold of any level")
    void thresholdForMatchesCurve() {
        assertEquals(0L, levelService.thresholdFor(1));
        assertEquals(100L, levelService.thresholdFor(2));
        assertEquals(1400L, levelService.thresholdFor(6));
        assertEquals(1900L, levelService.thresholdFor(7));
        assertEquals(2450L, levelService.thresholdFor(8));
    }

}
