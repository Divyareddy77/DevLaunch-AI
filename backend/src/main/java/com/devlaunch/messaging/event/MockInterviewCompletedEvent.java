package com.devlaunch.messaging.event;

import com.devlaunch.entity.enums.InterviewType;

/**
 * Lightweight event published when a mock interview finishes.
 * <p>
 * Carries the interview metrics requested by the platform (scores, filler
 * count, speaking pace, duration) plus the milestone context the consumer
 * needs to reproduce the existing celebration notifications (new personal
 * best, improved average, practice streak) without re-querying history.
 * The interview session itself is persisted synchronously by the submitting
 * service because the history endpoint returns it immediately.
 * </p>
 *
 * @param userId               the ID of the user who completed the interview
 * @param sessionId            the interview session identifier
 * @param interviewType        the interview category
 * @param overallScore         the overall interview score (0–100)
 * @param confidenceScore      the confidence dimension score (0–100)
 * @param communicationScore   the communication dimension score (0–100)
 * @param fillerCount          the client-reported filler-word count, or {@code null}
 * @param speakingPace         the client-reported speaking pace (words per minute), or {@code null}
 * @param durationSeconds      the interview duration in seconds
 * @param previousMaxScore     the best score before this interview (0 if none)
 * @param previousAverageScore the average score before this interview (0 if none)
 * @param previousStreak       the practice streak before this interview
 * @param previousCount        the number of sessions before this interview
 * @param previousSum          the sum of scores before this interview
 * @param newStreak            the practice streak including this interview
 * @author DevLaunch
 */
public record MockInterviewCompletedEvent(Long userId,
                                          String sessionId,
                                          InterviewType interviewType,
                                          int overallScore,
                                          int confidenceScore,
                                          int communicationScore,
                                          Integer fillerCount,
                                          Double speakingPace,
                                          Integer durationSeconds,
                                          int previousMaxScore,
                                          double previousAverageScore,
                                          int previousStreak,
                                          long previousCount,
                                          long previousSum,
                                          int newStreak) {
}
