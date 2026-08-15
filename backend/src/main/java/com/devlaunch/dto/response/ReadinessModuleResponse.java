package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO describing one module's contribution to the placement
 * readiness summary shown on the dashboard.
 * <p>
 * Each entry aggregates real data from an existing platform module
 * (resume builder, AI resume review, mock interview, GitHub, LeetCode,
 * study planner, or job tracker) into a display value plus a normalized
 * 0–100 score used purely for colour coding. The module score is a
 * presentational indicator and is never part of the placement readiness
 * formula itself.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadinessModuleResponse {

    /**
     * A stable identifier for the module, e.g. {@code RESUME_ATS},
     * {@code MOCK_INTERVIEW}, {@code GITHUB}.
     */
    private String key;

    /**
     * A human-readable module name, e.g. "Resume ATS" or "Mock Interview".
     */
    private String label;

    /**
     * The module's display value built from real user data, e.g.
     * "67 / 100", "100%", "0 Interviews", or "Not Connected".
     */
    private String value;

    /**
     * A normalized 0–100 score used for colour coding and for ranking
     * strengths and weaknesses. Not part of the readiness formula.
     */
    private Integer score;

}
