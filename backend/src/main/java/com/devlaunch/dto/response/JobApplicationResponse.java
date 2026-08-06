package com.devlaunch.dto.response;

import com.devlaunch.entity.enums.ApplicationPriority;
import com.devlaunch.entity.enums.ApplicationStatus;
import com.devlaunch.entity.enums.WorkMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for job application information.
 * <p>
 * Exposes job application data including the company name, job role,
 * status, and optional details. Internal fields such as the user
 * association and timestamps are excluded from the response. In addition
 * to the scalar fields, the response carries the application's timeline
 * and interview schedule so list, board, and detail views render without
 * extra round trips; notes and attachments are summarized by count.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplicationResponse {

    private Long id;

    private String companyName;

    private String jobRole;

    private String companyLocation;

    private String jobType;

    private String salary;

    private LocalDate applicationDate;

    private ApplicationStatus status;

    private String jobUrl;

    private String companyWebsite;

    private String recruiterName;

    private String recruiterEmail;

    private String referral;

    private WorkMode workMode;

    private ApplicationPriority priority;

    private String technology;

    private String notes;

    private Long resumeId;

    /**
     * The milestone history of this application, oldest first.
     */
    @Builder.Default
    private List<TimelineEventResponse> timeline = new ArrayList<>();

    /**
     * The scheduled interviews for this application, ordered by date.
     */
    @Builder.Default
    private List<InterviewScheduleResponse> interviews = new ArrayList<>();

    /**
     * The next upcoming (future, non-cancelled) interview, or {@code null}.
     */
    private InterviewScheduleResponse upcomingInterview;

    /**
     * The number of private interview note entries on this application.
     */
    private Integer notesCount;

    /**
     * The number of attached documents on this application.
     */
    private Integer attachmentCount;

}
