package com.devlaunch.mapper;

import com.devlaunch.dto.response.AchievementDefinitionResponse;
import com.devlaunch.dto.response.UnlockedAchievementResponse;
import com.devlaunch.dto.response.XpHistoryResponse;
import com.devlaunch.entity.AchievementDefinition;
import com.devlaunch.entity.UserAchievement;
import com.devlaunch.entity.XpHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for the gamification module.
 * <p>
 * Converts the achievement catalog, the per-user unlock records, and the
 * XP ledger into their response DTOs. The per-user progress response is
 * built in the service layer because it needs runtime progress values.
 * </p>
 *
 * @author DevLaunch
 */
@Mapper(componentModel = "spring")
public interface GamificationMapper {

    /**
     * Maps a catalog definition to its response DTO.
     *
     * @param definition the achievement definition
     * @return the catalog response DTO
     */
    AchievementDefinitionResponse toAchievementDefinitionResponse(AchievementDefinition definition);

    /**
     * Maps an unlock record to its response DTO, flattening the badge
     * definition fields and the unlock timestamp.
     *
     * @param userAchievement the unlock record
     * @return the unlocked-badge response DTO
     */
    @Mapping(target = "code", source = "achievementDefinition.code")
    @Mapping(target = "category", source = "achievementDefinition.category")
    @Mapping(target = "title", source = "achievementDefinition.title")
    @Mapping(target = "description", source = "achievementDefinition.description")
    @Mapping(target = "icon", source = "achievementDefinition.icon")
    @Mapping(target = "color", source = "achievementDefinition.color")
    @Mapping(target = "xpReward", source = "achievementDefinition.xpReward")
    @Mapping(target = "unlockedAt", source = "unlockedAt")
    UnlockedAchievementResponse toUnlockedResponse(UserAchievement userAchievement);

    /**
     * Maps an XP ledger entry to its response DTO.
     *
     * @param xpHistory the ledger entry
     * @return the history response DTO
     */
    XpHistoryResponse toXpHistoryResponse(XpHistory xpHistory);

}
