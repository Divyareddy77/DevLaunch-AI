package com.devlaunch.messaging.event;

/**
 * Lightweight event published when an ATS resume review completes.
 * <p>
 * Carries the domain data of the finished review: the scores, the target
 * role, and the previously best score (nullable) so the consumer can store
 * the review history row and compose both the completion and the
 * score-improvement notifications without re-querying the database.
 * </p>
 *
 * @param userId             the ID of the user who requested the review
 * @param resumeId           the ID of the reviewed resume
 * @param targetRole         the optional job role the review was tailored towards
 * @param resumeScore        the overall resume quality score (0–100)
 * @param atsScore           the ATS compatibility score (0–100)
 * @param previousResumeScore the previous best score on this resume, or {@code null}
 * @author DevLaunch
 */
public record ResumeReviewedEvent(Long userId,
                                  Long resumeId,
                                  String targetRole,
                                  int resumeScore,
                                  int atsScore,
                                  Integer previousResumeScore) {
}
