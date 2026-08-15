package com.devlaunch.dto.response;

import com.devlaunch.entity.enums.XpReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * One entry of the user's XP ledger.
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class XpHistoryResponse {

    private Long id;

    /** How much XP was earned. */
    private int amount;

    /** Why the XP was awarded. */
    private XpReason reason;

    /** A human-readable description of the award. */
    private String description;

    /** When the XP was awarded. */
    private LocalDateTime createdAt;

}
