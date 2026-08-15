package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for the number of applications submitted in a given month.
 * <p>
 * The {@code yearMonth} value uses the ISO-8601 {@code yyyy-MM} format so
 * it sorts lexicographically and is easy to format client-side.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyApplicationResponse {

    /**
     * The month in {@code yyyy-MM} format (e.g. {@code 2024-03}).
     */
    private String yearMonth;

    /**
     * The number of applications submitted that month.
     */
    private Long count;

}
