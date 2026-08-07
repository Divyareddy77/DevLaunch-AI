package com.devlaunch.entity;

import com.devlaunch.entity.enums.XpReason;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * One entry of a user's XP ledger.
 * <p>
 * Every XP award — from activities (resume created, interview completed,
 * …) and from achievement unlocks — is recorded here, newest first. The
 * current total XP is derived as the sum of a user's entries, which keeps
 * the ledger the single source of truth and makes the history endpoint a
 * plain read of this table.
 * </p>
 *
 * @author DevLaunch
 */
@Entity
@Table(name = "xp_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = "user")
@EqualsAndHashCode(callSuper = true, exclude = "user")
public class XpHistory extends BaseEntity {

    /**
     * The user who earned this XP.
     */
    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * How much XP was earned. Always positive — XP is never deducted.
     */
    @Positive
    @Column(name = "amount", nullable = false)
    private int amount;

    /**
     * Why the XP was awarded (activity or achievement unlock).
     */
    @NotNull(message = "Reason is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 40)
    private XpReason reason;

    /**
     * A human-readable description of the award, e.g. "Unlocked Interview Expert".
     */
    @Column(name = "description", length = 300)
    private String description;

}
