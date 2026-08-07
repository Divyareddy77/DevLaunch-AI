package com.devlaunch.service.impl;

import com.devlaunch.dto.request.MockInterviewAnswerRequest;
import com.devlaunch.dto.request.MockInterviewStartRequest;
import com.devlaunch.dto.request.MockInterviewSubmitRequest;
import com.devlaunch.dto.request.ResumeReviewRequest;
import com.devlaunch.dto.response.CategoryScoreResponse;
import com.devlaunch.dto.response.ExperienceAnalysisResponse;
import com.devlaunch.dto.response.MockInterviewCategoryResponse;
import com.devlaunch.dto.response.MockInterviewFeedbackItemResponse;
import com.devlaunch.dto.response.MockInterviewFeedbackResponse;
import com.devlaunch.dto.response.MockInterviewHistoryItemResponse;
import com.devlaunch.dto.response.MockInterviewHistoryResponse;
import com.devlaunch.dto.response.MockInterviewQuestionResponse;
import com.devlaunch.dto.response.MockInterviewStartResponse;
import com.devlaunch.dto.response.ProjectAnalysisResponse;
import com.devlaunch.dto.response.ResumeReviewResponse;
import com.devlaunch.dto.response.ResumeReviewSuggestion;
import com.devlaunch.dto.response.ScoreTrendPoint;
import com.devlaunch.cache.CacheNames;
import com.devlaunch.dto.response.TranscribeResponse;
import com.devlaunch.dto.response.SkillsAnalysisResponse;
import com.devlaunch.dto.response.SummaryAnalysisResponse;
import com.devlaunch.entity.Achievement;
import com.devlaunch.entity.Certification;
import com.devlaunch.entity.Education;
import com.devlaunch.entity.Experience;
import com.devlaunch.entity.InterviewSession;
import com.devlaunch.entity.InterviewSessionQuestion;
import com.devlaunch.entity.Project;
import com.devlaunch.entity.Resume;
import com.devlaunch.entity.ResumeReview;
import com.devlaunch.entity.Skill;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.ActivityType;
import com.devlaunch.entity.enums.InterviewDifficulty;
import com.devlaunch.entity.enums.InterviewType;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.messaging.EventPublisher;
import com.devlaunch.messaging.EventTopics;
import com.devlaunch.messaging.event.ActivityEvent;
import com.devlaunch.messaging.event.MockInterviewCompletedEvent;
import com.devlaunch.messaging.event.ResumeReviewedEvent;
import com.devlaunch.repository.AchievementRepository;
import com.devlaunch.repository.CertificationRepository;
import com.devlaunch.repository.EducationRepository;
import com.devlaunch.repository.ExperienceRepository;
import com.devlaunch.repository.InterviewSessionRepository;
import com.devlaunch.repository.ProjectRepository;
import com.devlaunch.repository.ResumeRepository;
import com.devlaunch.repository.ResumeReviewRepository;
import com.devlaunch.repository.SkillRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.ai.InterviewAnswer;
import com.devlaunch.service.ai.InterviewQuestion;
import com.devlaunch.service.ai.MockInterviewFeedback;
import com.devlaunch.service.ai.MockInterviewProvider;
import com.devlaunch.service.ai.OpenAiWhisperTranscriber;
import com.devlaunch.service.ai.ResumeContent;
import com.devlaunch.service.ai.ResumeReviewAnalysis;
import com.devlaunch.service.ai.ResumeReviewProvider;
import com.devlaunch.service.ai.WhisperTranscription;
import com.devlaunch.service.interfaces.AiService;
import com.devlaunch.service.interfaces.InterviewQuestionBankService;
import org.slf4j.Logger;
import org.springframework.cache.annotation.CacheEvict;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of {@link AiService} providing AI-powered resume review
 * and mock interviews.
 * <p>
 * For resume review, loads the resume owned by the authenticated user
 * together with all of its sections, assembles a {@link ResumeContent}
 * snapshot, and delegates the analysis to a {@link ResumeReviewProvider}.
 * For mock interviews, delegates question generation and answer
 * evaluation to a {@link MockInterviewProvider} and persists completed
 * sessions — including the full report — to the user's interview history,
 * firing milestone notifications through the existing notification module.
 * In both cases the primary LLM provider is preferred when configured; on
 * any failure or when no provider is configured, the deterministic sample
 * provider is used so the features remain fully functional.
 * </p>
 * <p>
 * Messaging boundary: both flows publish domain events on the RabbitMQ
 * backbone — the resume review flow emits a resume-reviewed event (the
 * consumer stores the review history and the notifications) and the mock
 * interview flow emits an interview-completed event carrying the metrics
 * (the consumer reproduces the milestone notifications). No notification
 * or dashboard side effect happens inside the service.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class AiServiceImpl implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiServiceImpl.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");

    /** The question length used when the client does not specify one. */
    private static final int DEFAULT_QUESTION_LENGTH = 10;

    /** Scores at or above this count as a successful interview. */
    private static final int SUCCESS_SCORE = 70;

    /** The number of recent sessions included in the score trend. */
    private static final int TREND_LIMIT = 10;

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final EducationRepository educationRepository;
    private final ExperienceRepository experienceRepository;
    private final SkillRepository skillRepository;
    private final ProjectRepository projectRepository;
    private final CertificationRepository certificationRepository;
    private final AchievementRepository achievementRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final ResumeReviewRepository resumeReviewRepository;
    private final InterviewQuestionBankService questionBankService;
    private final ResumeReviewProvider openAiResumeReviewProvider;
    private final ResumeReviewProvider sampleResumeReviewProvider;
    private final MockInterviewProvider openAiMockInterviewProvider;
    private final MockInterviewProvider sampleMockInterviewProvider;
    private final OpenAiWhisperTranscriber whisperTranscriber;
    private final EventPublisher eventPublisher;

    /**
     * Constructs the AI service with the required dependencies.
     *
     * @param resumeRepository             repository for resume data access
     * @param userRepository               repository for user data access
     * @param educationRepository          repository for education data access
     * @param experienceRepository         repository for experience data access
     * @param skillRepository              repository for skill data access
     * @param projectRepository            repository for project data access
     * @param certificationRepository      repository for certification data access
     * @param achievementRepository        repository for achievement data access
     * @param interviewSessionRepository   repository for interview history data access
     * @param resumeReviewRepository       repository for AI resume review history
     * @param questionBankService          service for the database question bank
     * @param openAiResumeReviewProvider   the primary LLM resume review provider
     * @param sampleResumeReviewProvider   the deterministic resume review fallback
     * @param openAiMockInterviewProvider  the primary LLM mock interview provider
     * @param sampleMockInterviewProvider  the deterministic mock interview fallback
     * @param whisperTranscriber           the shared Whisper speech-to-text client
     * @param eventPublisher               publisher for the messaging backbone
     */
    public AiServiceImpl(final ResumeRepository resumeRepository,
                         final UserRepository userRepository,
                         final EducationRepository educationRepository,
                         final ExperienceRepository experienceRepository,
                         final SkillRepository skillRepository,
                         final ProjectRepository projectRepository,
                         final CertificationRepository certificationRepository,
                         final AchievementRepository achievementRepository,
                         final InterviewSessionRepository interviewSessionRepository,
                         final ResumeReviewRepository resumeReviewRepository,
                         final InterviewQuestionBankService questionBankService,
                         @Qualifier("openAiResumeReviewProvider")
                         final ResumeReviewProvider openAiResumeReviewProvider,
                         @Qualifier("sampleResumeReviewProvider")
                         final ResumeReviewProvider sampleResumeReviewProvider,
                         @Qualifier("openAiMockInterviewProvider")
                         final MockInterviewProvider openAiMockInterviewProvider,
                         @Qualifier("sampleMockInterviewProvider")
                         final MockInterviewProvider sampleMockInterviewProvider,
                         final OpenAiWhisperTranscriber whisperTranscriber,
                         final EventPublisher eventPublisher) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.skillRepository = skillRepository;
        this.projectRepository = projectRepository;
        this.certificationRepository = certificationRepository;
        this.achievementRepository = achievementRepository;
        this.interviewSessionRepository = interviewSessionRepository;
        this.resumeReviewRepository = resumeReviewRepository;
        this.questionBankService = questionBankService;
        this.openAiResumeReviewProvider = openAiResumeReviewProvider;
        this.sampleResumeReviewProvider = sampleResumeReviewProvider;
        this.openAiMockInterviewProvider = openAiMockInterviewProvider;
        this.sampleMockInterviewProvider = sampleMockInterviewProvider;
        this.whisperTranscriber = whisperTranscriber;
        this.eventPublisher = eventPublisher;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ResumeReviewResponse reviewResume(final ResumeReviewRequest request) {
        final Resume resume = getResumeOwnedByAuthenticatedUser(request.getResumeId());

        final ResumeContent content = buildResumeContent(resume);
        final ResumeReviewAnalysis analysis = analyze(content, request.getTargetRole());

        // Snapshot the previous best score so the consumer can celebrate an
        // improvement once it stores the new review history row. Ordered by
        // creation time (not id) so the most recent review is always the
        // baseline, even if rows are ever removed or re-inserted.
        final ResumeReview previousReview = resumeReviewRepository.findByResume(resume).stream()
                .max(Comparator.comparing(ResumeReview::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);

        // Publish the review-completed event; the consumer stores the review
        // history row (used by the admin module) and persists the completion
        // and score-improvement notifications asynchronously.
        eventPublisher.publish(EventTopics.RESUME_REVIEW_COMPLETED_KEY,
                new ResumeReviewedEvent(resume.getUser().getId(), resume.getId(),
                        request.getTargetRole(), analysis.resumeScore(), analysis.atsScore(),
                        previousReview == null ? null : previousReview.getResumeScore()));

        // Publish the gamification activity; the consumer awards XP and
        // evaluates the ATS achievements asynchronously.
        eventPublisher.publish(EventTopics.ACHIEVEMENT_ACTIVITY_KEY,
                new ActivityEvent(resume.getUser().getId(), ActivityType.RESUME_REVIEWED,
                        analysis.atsScore(), LocalDateTime.now()));

        log.info("Resume review completed for resume id={}: resumeScore={}, atsScore={}",
                resume.getId(), analysis.resumeScore(), analysis.atsScore());

        return toResponse(resume, analysis);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public MockInterviewStartResponse startMockInterview(final MockInterviewStartRequest request) {
        final InterviewDifficulty difficulty = request.getDifficulty() == null
                ? InterviewDifficulty.MIXED : request.getDifficulty();
        final int count = request.getQuestionLength() == null
                ? DEFAULT_QUESTION_LENGTH : request.getQuestionLength();
        final boolean timed = Boolean.TRUE.equals(request.getTimed());

        final List<InterviewQuestion> questions =
                generateQuestions(request.getInterviewType(), difficulty, count);

        log.info("Mock interview started for type={}, difficulty={}: {} questions generated",
                request.getInterviewType(), difficulty, questions.size());

        return MockInterviewStartResponse.builder()
                .sessionId(UUID.randomUUID().toString())
                .interviewType(request.getInterviewType())
                .difficulty(difficulty)
                .timed(timed)
                .questions(questions.stream()
                        .map(this::toQuestionResponse)
                        .toList())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.DASHBOARD)
    public MockInterviewFeedbackResponse submitMockInterview(final MockInterviewSubmitRequest request) {
        final User user = getAuthenticatedUser();

        // Snapshot the user's existing history before the new session is
        // persisted so milestone notifications compare against prior results.
        final List<InterviewSession> previousSessions =
                interviewSessionRepository.findByUserOrderByCompletedAtDesc(user);
        final int previousMaxScore = previousSessions.stream()
                .mapToInt(InterviewSession::getOverallScore)
                .max()
                .orElse(0);
        final double previousAverageScore = previousSessions.stream()
                .mapToInt(InterviewSession::getOverallScore)
                .average()
                .orElse(0.0);
        final int previousStreak = currentStreak(previousSessions);

        final List<InterviewAnswer> answers = request.getAnswers().stream()
                .map(this::toInterviewAnswer)
                .toList();

        final MockInterviewFeedback evaluation = evaluate(request.getInterviewType(), answers);

        final InterviewDifficulty difficulty = request.getDifficulty() == null
                ? InterviewDifficulty.MIXED : request.getDifficulty();
        final boolean timed = Boolean.TRUE.equals(request.getTimed());
        final int wordCount = answers.stream().mapToInt(answer -> wordCount(answer.answer())).sum();

        final InterviewSession session = InterviewSession.builder()
                .sessionId(request.getSessionId())
                .interviewType(request.getInterviewType())
                .difficulty(difficulty)
                .timed(timed)
                .durationSeconds(request.getDurationSeconds())
                .wordCount(wordCount)
                .overallScore(evaluation.overallScore())
                .questionCount(answers.size())
                .technicalScore(evaluation.technicalScore())
                .communicationScore(evaluation.communicationScore())
                .confidenceScore(evaluation.confidenceScore())
                .problemSolvingScore(evaluation.problemSolvingScore())
                .clarityScore(evaluation.clarityScore())
                .vocabularyScore(evaluation.vocabularyScore())
                .professionalismScore(evaluation.professionalismScore())
                .strengths(evaluation.strengths())
                .areasForImprovement(evaluation.areasForImprovement())
                .suggestions(evaluation.suggestions())
                .completedAt(LocalDateTime.now())
                .questions(snapshotQuestions(request, evaluation))
                .user(user)
                .build();
        interviewSessionRepository.save(session);

        // Publish the completion event carrying the metrics; the consumer
        // reproduces the milestone notifications and logs the metrics for
        // the dashboard aggregation pipeline.
        final List<InterviewSession> sessionsWithNew = new ArrayList<>(previousSessions);
        sessionsWithNew.add(session);
        final int newStreak = currentStreak(sessionsWithNew);
        final long previousSum = previousSessions.stream()
                .mapToInt(InterviewSession::getOverallScore)
                .sum();

        eventPublisher.publish(EventTopics.MOCK_INTERVIEW_COMPLETED_KEY,
                new MockInterviewCompletedEvent(
                        user.getId(), request.getSessionId(), request.getInterviewType(),
                        evaluation.overallScore(), evaluation.confidenceScore(),
                        evaluation.communicationScore(),
                        request.getFillerCount(), request.getSpeakingPace(),
                        request.getDurationSeconds(),
                        previousMaxScore, previousAverageScore, previousStreak,
                        previousSessions.size(), previousSum, newStreak));

        // Publish the gamification activity; the consumer awards XP and
        // evaluates the interview achievements asynchronously.
        eventPublisher.publish(EventTopics.ACHIEVEMENT_ACTIVITY_KEY,
                new ActivityEvent(user.getId(), ActivityType.INTERVIEW_COMPLETED,
                        evaluation.overallScore(), LocalDateTime.now()));

        log.info("Mock interview submitted for user id={}, type={}: overallScore={}",
                user.getId(), request.getInterviewType(), evaluation.overallScore());

        return toFeedbackResponse(request, evaluation, wordCount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public MockInterviewHistoryResponse getMockInterviewHistory() {
        final User user = getAuthenticatedUser();

        final List<InterviewSession> sessions =
                interviewSessionRepository.findByUserOrderByCompletedAtDesc(user);

        final double average = sessions.stream()
                .mapToInt(InterviewSession::getOverallScore)
                .average()
                .orElse(0.0);
        final long totalInterviews = sessions.size();
        final int bestScore = sessions.stream()
                .mapToInt(InterviewSession::getOverallScore)
                .max()
                .orElse(0);
        final LocalDateTime lastInterviewAt = sessions.isEmpty()
                ? null : sessions.getFirst().getCompletedAt();
        final long totalTimeSpentSeconds = sessions.stream()
                .mapToLong(session -> session.getDurationSeconds() == null
                        ? 0L : session.getDurationSeconds())
                .sum();
        final long totalQuestionsAnswered = sessions.stream()
                .mapToLong(InterviewSession::getQuestionCount)
                .sum();
        final double successRate = sessions.isEmpty()
                ? 0.0
                : Math.round(sessions.stream()
                        .filter(session -> session.getOverallScore() >= SUCCESS_SCORE)
                        .count() * 1000.0 / totalInterviews) / 10.0;

        return MockInterviewHistoryResponse.builder()
                .history(sessions.stream().map(this::toHistoryItem).toList())
                .totalInterviews((long) totalInterviews)
                .averageScore(Math.round(average * 10.0) / 10.0)
                .bestScore(totalInterviews == 0 ? null : bestScore)
                .lastInterviewAt(lastInterviewAt)
                .currentStreak(currentStreak(sessions))
                .mostPracticedCategory(mostPracticedCategory(sessions))
                .totalTimeSpentSeconds(totalTimeSpentSeconds)
                .totalQuestionsAnswered(totalQuestionsAnswered)
                .successRate(successRate)
                .readinessLevel(readinessLevel(totalInterviews, average))
                .scoreTrend(sessions.stream()
                        .limit(TREND_LIMIT)
                        .map(session -> ScoreTrendPoint.builder()
                                .completedAt(session.getCompletedAt())
                                .score(session.getOverallScore())
                                .build())
                        .toList())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<MockInterviewCategoryResponse> getMockInterviewCategories() {
        final User user = getAuthenticatedUser();
        final List<InterviewSession> sessions =
                interviewSessionRepository.findByUserOrderByCompletedAtDesc(user);

        final List<MockInterviewCategoryResponse> responses = new ArrayList<>();
        for (final InterviewType type : InterviewType.values()) {
            final List<InterviewSession> categorySessions = sessions.stream()
                    .filter(session -> session.getInterviewType() == type)
                    .toList();
            responses.add(MockInterviewCategoryResponse.builder()
                    .interviewType(type)
                    .questionBankSize((int) questionBankService.countActive(type))
                    .attemptCount(categorySessions.size())
                    .previousBestScore(categorySessions.isEmpty() ? null
                            : categorySessions.stream()
                                    .mapToInt(InterviewSession::getOverallScore)
                                    .max().orElse(0))
                    .lastAttemptAt(categorySessions.isEmpty() ? null
                            : categorySessions.getFirst().getCompletedAt())
                    .build());
        }
        return responses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteMockInterview(final String sessionId) {
        final User user = getAuthenticatedUser();
        final InterviewSession session = interviewSessionRepository
                .findBySessionIdAndUser(sessionId, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Interview session with id " + sessionId + " not found"));
        interviewSessionRepository.delete(session);
        log.info("Mock interview session id={} deleted for user id={}", sessionId, user.getId());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Delegates to the shared Whisper client, which validates the audio
     * payload and surfaces friendly errors for empty or unsupported files.
     * </p>
     */
    @Override
    public TranscribeResponse transcribe(final MultipartFile file,
                                         final Integer clientDurationSeconds) {
        final WhisperTranscription transcription =
                whisperTranscriber.transcribe(file, clientDurationSeconds);

        log.info("Voice answer transcribed: duration={}s",
                Math.round(transcription.duration()));

        return TranscribeResponse.builder()
                .transcript(transcription.transcript())
                .duration(transcription.duration())
                .build();
    }

    /**
     * Delegates the resume analysis to the preferred configured provider,
     * falling back to the deterministic sample provider when the primary
     * is not configured or throws.
     *
     * @param content    the structured resume content to analyse
     * @param targetRole the optional target job role
     * @return the structured analysis result
     */
    private ResumeReviewAnalysis analyze(final ResumeContent content, final String targetRole) {
        final ResumeReviewProvider primary =
                openAiResumeReviewProvider.isConfigured()
                        ? openAiResumeReviewProvider
                        : sampleResumeReviewProvider;

        try {
            return primary.analyze(content, targetRole);
        } catch (final Exception e) {
            log.warn("Resume review provider '{}' failed, falling back to deterministic analysis: {}",
                    primary.getClass().getSimpleName(), e.getMessage());
            return sampleResumeReviewProvider.analyze(content, targetRole);
        }
    }

    /**
     * Generates interview questions using the preferred configured
     * provider, falling back to the deterministic provider (which reads
     * the database-backed question bank) when the primary LLM provider is
     * configured but throws or returns no questions. When the LLM provider
     * is not configured, the deterministic provider is used directly so its
     * errors — such as an empty question bank — surface immediately instead
     * of being swallowed by a pointless second invocation.
     *
     * @param type       the interview category to generate questions for
     * @param difficulty the difficulty mode of the interview
     * @param count      the number of questions to generate
     * @return the generated questions
     */
    private List<InterviewQuestion> generateQuestions(final InterviewType type,
                                                      final InterviewDifficulty difficulty,
                                                      final int count) {
        if (openAiMockInterviewProvider.isConfigured()) {
            try {
                return generateFrom(openAiMockInterviewProvider, type, difficulty, count);
            } catch (final RuntimeException e) {
                log.warn("OpenAI mock interview provider failed to generate questions, "
                                + "falling back to the question bank: {}",
                        e.getMessage());
            }
        }
        return generateFrom(sampleMockInterviewProvider, type, difficulty, count);
    }

    /**
     * Invokes a provider's question generation and guards against an empty
     * result.
     *
     * @param provider   the provider to invoke
     * @param type       the interview category to generate questions for
     * @param difficulty the difficulty mode of the interview
     * @param count      the number of questions to generate
     * @return the generated questions
     * @throws IllegalStateException if the provider returns no questions
     */
    private List<InterviewQuestion> generateFrom(final MockInterviewProvider provider,
                                                 final InterviewType type,
                                                 final InterviewDifficulty difficulty,
                                                 final int count) {
        final List<InterviewQuestion> questions =
                provider.generateQuestions(type, difficulty, count);
        if (questions.isEmpty()) {
            throw new IllegalStateException("Provider returned no questions");
        }
        return questions;
    }

    /**
     * Evaluates interview answers using the preferred configured
     * provider, falling back to deterministic heuristics when the primary
     * is not configured or throws.
     *
     * @param type    the interview category that was practised
     * @param answers the question/answer pairs to evaluate
     * @return the structured evaluation result
     */
    private MockInterviewFeedback evaluate(final InterviewType type, final List<InterviewAnswer> answers) {
        final MockInterviewProvider primary =
                openAiMockInterviewProvider.isConfigured()
                        ? openAiMockInterviewProvider
                        : sampleMockInterviewProvider;

        try {
            return primary.evaluate(type, answers);
        } catch (final Exception e) {
            log.warn("Mock interview provider '{}' failed to evaluate answers, "
                            + "falling back to deterministic evaluation: {}",
                    primary.getClass().getSimpleName(), e.getMessage());
            return sampleMockInterviewProvider.evaluate(type, answers);
        }
    }

    /**
     * Snapshots the session questions together with their evaluation so
     * the history can always render the original report.
     *
     * @param request    the submit request carrying the raw answers
     * @param evaluation the evaluation result
     * @return the snapshotted question entries
     */
    private List<InterviewSessionQuestion> snapshotQuestions(
            final MockInterviewSubmitRequest request,
            final MockInterviewFeedback evaluation) {
        return request.getAnswers().stream()
                .map(answer -> {
                    final MockInterviewFeedback.Item item = evaluation.feedback().stream()
                            .filter(feedback -> feedback.questionId().equals(answer.getQuestionId()))
                            .findFirst()
                            .orElse(null);
                    return new InterviewSessionQuestion(
                            answer.getQuestionId(),
                            answer.getQuestion(),
                            answer.getAnswer(),
                            item == null ? null : item.score(),
                            item == null ? null : item.feedback(),
                            item == null ? null : item.improvedAnswer());
                })
                .toList();
    }

    /**
     * Counts the words in a piece of text.
     */
    private int wordCount(final String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }

    /**
     * Computes the current practice streak: the number of consecutive days
     * (ending today or yesterday) with at least one completed interview.
     *
     * @param sessions the user's completed interview sessions
     * @return the streak length in days
     */
    private int currentStreak(final List<InterviewSession> sessions) {
        final TreeSet<LocalDate> practiceDays = new TreeSet<>();
        for (final InterviewSession session : sessions) {
            if (session.getCompletedAt() != null) {
                practiceDays.add(session.getCompletedAt().toLocalDate());
            }
        }
        if (practiceDays.isEmpty()) {
            return 0;
        }

        final LocalDate today = LocalDate.now();
        LocalDate cursor = today;
        if (!practiceDays.contains(cursor)) {
            cursor = cursor.minusDays(1);
            if (!practiceDays.contains(cursor)) {
                return 0;
            }
        }

        int streak = 0;
        while (practiceDays.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    /**
     * Returns the category with the most completed interviews, or
     * {@code null} when no interview has been completed yet.
     *
     * @param sessions the user's completed interview sessions
     * @return the most practised category, or {@code null}
     */
    private InterviewType mostPracticedCategory(final List<InterviewSession> sessions) {
        final Map<InterviewType, Long> counts = sessions.stream()
                .collect(Collectors.groupingBy(
                        InterviewSession::getInterviewType, Collectors.counting()));
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Derives a human-readable interview readiness level from the total
     * interview count and the average score.
     *
     * @param totalInterviews the number of completed interviews
     * @param averageScore    the average score across all interviews
     * @return the readiness level label
     */
    private String readinessLevel(final long totalInterviews, final double averageScore) {
        if (totalInterviews == 0) {
            return "Getting Started";
        }
        if (averageScore >= 90) {
            return "Exceptional";
        }
        if (averageScore >= 75) {
            return "Interview Ready";
        }
        if (averageScore >= 60) {
            return "Improving";
        }
        return "Developing";
    }

    /**
     * Maps a request answer DTO to the internal {@link InterviewAnswer}.
     */
    private InterviewAnswer toInterviewAnswer(final MockInterviewAnswerRequest answer) {
        return new InterviewAnswer(answer.getQuestionId(), answer.getQuestion(), answer.getAnswer());
    }

    /**
     * Maps the internal evaluation result to the public feedback response
     * DTO, attaching the session, category, and configuration identifiers.
     *
     * @param request    the submit request
     * @param evaluation the internal evaluation result
     * @param wordCount  the total word count across all answers
     * @return the public feedback response DTO
     */
    private MockInterviewFeedbackResponse toFeedbackResponse(
            final MockInterviewSubmitRequest request,
            final MockInterviewFeedback evaluation,
            final int wordCount) {
        final List<MockInterviewFeedbackItemResponse> items = evaluation.feedback().stream()
                .map(item -> MockInterviewFeedbackItemResponse.builder()
                        .questionId(item.questionId())
                        .question(item.question())
                        .answer(item.answer())
                        .score(item.score())
                        .feedback(item.feedback())
                        .suggestions(item.suggestions())
                        .improvedAnswer(item.improvedAnswer())
                        .build())
                .toList();

        return MockInterviewFeedbackResponse.builder()
                .sessionId(request.getSessionId())
                .interviewType(request.getInterviewType())
                .difficulty(request.getDifficulty() == null
                        ? InterviewDifficulty.MIXED : request.getDifficulty())
                .timed(Boolean.TRUE.equals(request.getTimed()))
                .durationSeconds(request.getDurationSeconds())
                .wordCount(wordCount)
                .overallScore(evaluation.overallScore())
                .technicalScore(evaluation.technicalScore())
                .communicationScore(evaluation.communicationScore())
                .confidenceScore(evaluation.confidenceScore())
                .problemSolvingScore(evaluation.problemSolvingScore())
                .clarityScore(evaluation.clarityScore())
                .vocabularyScore(evaluation.vocabularyScore())
                .professionalismScore(evaluation.professionalismScore())
                .feedback(items)
                .strengths(evaluation.strengths())
                .areasForImprovement(evaluation.areasForImprovement())
                .suggestions(evaluation.suggestions())
                .missedConcepts(evaluation.missedConcepts())
                .build();
    }

    /**
     * Maps an {@link InterviewSession} entity to the public history item
     * DTO, including the full snapshotted report.
     *
     * @param session the completed interview session
     * @return the public history item DTO
     */
    private MockInterviewHistoryItemResponse toHistoryItem(final InterviewSession session) {
        final List<MockInterviewQuestionResponse> questions = session.getQuestions() == null
                ? List.of()
                : session.getQuestions().stream().map(this::toQuestionResponse).toList();

        final List<String> strengths = safeList(session.getStrengths());
        final List<String> improvements = safeList(session.getAreasForImprovement());
        final List<String> suggestions = safeList(session.getSuggestions());

        return MockInterviewHistoryItemResponse.builder()
                .sessionId(session.getSessionId())
                .interviewType(session.getInterviewType())
                .difficulty(session.getDifficulty())
                .timed(session.getTimed())
                .durationSeconds(session.getDurationSeconds())
                .completedAt(session.getCompletedAt())
                .overallScore(session.getOverallScore())
                .questionCount(session.getQuestionCount())
                .wordCount(session.getWordCount())
                .technicalScore(session.getTechnicalScore())
                .communicationScore(session.getCommunicationScore())
                .confidenceScore(session.getConfidenceScore())
                .problemSolvingScore(session.getProblemSolvingScore())
                .clarityScore(session.getClarityScore())
                .vocabularyScore(session.getVocabularyScore())
                .professionalismScore(session.getProfessionalismScore())
                .strengths(strengths)
                .areasForImprovement(improvements)
                .suggestions(suggestions)
                .questions(questions)
                .build();
    }

    /**
     * Returns an immutable copy of the list, or an empty list when the
     * collection is {@code null}.
     */
    private List<String> safeList(final List<String> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

    /**
     * Maps a generated question to the public response DTO.
     */
    private MockInterviewQuestionResponse toQuestionResponse(
            final InterviewQuestion question) {
        return MockInterviewQuestionResponse.builder()
                .id(question.id())
                .question(question.question())
                .hint(question.hint())
                .difficulty(question.difficulty())
                .build();
    }

    /**
     * Maps a snapshotted session question to the public response DTO,
     * including the stored answer and evaluation for the history report.
     */
    private MockInterviewQuestionResponse toQuestionResponse(
            final InterviewSessionQuestion question) {
        return MockInterviewQuestionResponse.builder()
                .id(question.getQuestionId())
                .question(question.getQuestion())
                .hint(null)
                .answer(question.getAnswer())
                .score(question.getScore())
                .feedback(question.getFeedback())
                .improvedAnswer(question.getImprovedAnswer())
                .build();
    }

    /**
     * Assembles a {@link ResumeContent} snapshot from the resume and all
     * of its related sections, following the same assembly pattern used
     * by {@link ResumePdfServiceImpl}.
     *
     * @param resume the resume entity to assemble
     * @return the structured resume content snapshot
     */
    private ResumeContent buildResumeContent(final Resume resume) {
        final List<Experience> experiences = experienceRepository.findByResume(resume);
        final List<Education> educations = educationRepository.findByResume(resume);
        final List<Skill> skills = skillRepository.findByResume(resume);
        final List<Project> projects = projectRepository.findByResume(resume);
        final List<Certification> certifications = certificationRepository.findByResume(resume);
        final List<Achievement> achievements = achievementRepository.findByResume(resume);

        return new ResumeContent(
                resume.getHeadline(),
                resume.getSummary(),
                resume.getLinkedinUrl(),
                resume.getGithubUrl(),
                resume.getPortfolioUrl(),
                experiences.stream().map(this::formatExperience).toList(),
                educations.stream().map(this::formatEducation).toList(),
                skills.stream().map(this::formatSkill).toList(),
                projects.stream().map(this::formatProject).toList(),
                certifications.stream().map(this::formatCertification).toList(),
                achievements.stream().map(this::formatAchievement).toList());
    }

    /**
     * Formats an experience entry into a readable single-line summary.
     */
    private String formatExperience(final Experience experience) {
        return experience.getJobTitle() + " at " + experience.getCompanyName()
                + " (" + formatDate(experience.getStartDate()) + " - "
                + (experience.getCurrentlyWorking() != null && experience.getCurrentlyWorking()
                ? "Present" : formatDate(experience.getEndDate())) + ")"
                + appendDescription(experience.getDescription());
    }

    /**
     * Formats an education entry into a readable single-line summary.
     */
    private String formatEducation(final Education education) {
        return education.getDegree() + " in " + education.getFieldOfStudy()
                + " at " + education.getInstitutionName()
                + appendDateRange(education.getStartDate(), education.getEndDate());
    }

    /**
     * Formats a project entry into a readable single-line summary.
     */
    private String formatProject(final Project project) {
        return project.getProjectName() + " [" + project.getTechnologies() + "]"
                + appendDescription(project.getDescription());
    }

    /**
     * Formats a certification entry into a readable single-line summary.
     */
    private String formatCertification(final Certification certification) {
        return certification.getCertificationName() + " — " + certification.getIssuingOrganization();
    }

    /**
     * Formats an achievement entry into a readable single-line summary.
     */
    private String formatAchievement(final Achievement achievement) {
        return achievement.getTitle() + appendDescription(achievement.getDescription());
    }

    /**
     * Formats a skill entry, appending its proficiency level in parentheses
     * when present (matching the Resume PDF formatting).
     */
    private String formatSkill(final Skill skill) {
        final String name = skill.getSkillName();
        if (skill.getProficiency() != null && !skill.getProficiency().isBlank()) {
            return name + " (" + skill.getProficiency().trim() + ")";
        }
        return name;
    }

    /**
     * Appends a non-blank description to the formatted entry.
     */
    private String appendDescription(final String description) {
        return description != null && !description.isBlank() ? ": " + description.trim() : "";
    }

    /**
     * Appends a date range (e.g. " (Jan 2022 - Jun 2024)") to the entry.
     */
    private String appendDateRange(final LocalDate startDate, final LocalDate endDate) {
        final String start = formatDate(startDate);
        final String end = formatDate(endDate);
        if (start == null && end == null) {
            return "";
        }
        return " (" + (start == null ? "?" : start) + " - " + (end == null ? "Present" : end) + ")";
    }

    /**
     * Formats a {@link LocalDate} to a readable month-year string.
     */
    private String formatDate(final LocalDate date) {
        return date == null ? null : date.format(DATE_FORMATTER);
    }

    /**
     * Maps the internal analysis result to the public response DTO,
     * attaching the resume identifiers.
     *
     * @param resume   the reviewed resume
     * @param analysis the internal analysis result
     * @return the public response DTO
     */
    private ResumeReviewResponse toResponse(final Resume resume, final ResumeReviewAnalysis analysis) {
        final List<ResumeReviewSuggestion> suggestions = analysis.suggestions().stream()
                .map(suggestion -> ResumeReviewSuggestion.builder()
                        .section(suggestion.section())
                        .suggestion(suggestion.suggestion())
                        .priority(suggestion.priority())
                        .build())
                .toList();

        final List<CategoryScoreResponse> categoryScores = analysis.categoryScores().stream()
                .map(score -> CategoryScoreResponse.builder()
                        .category(score.category())
                        .score(score.score())
                        .maxScore(score.maxScore())
                        .build())
                .toList();

        final SummaryAnalysisResponse summaryAnalysis =
                SummaryAnalysisResponse.builder()
                        .score(analysis.summaryAnalysis().score())
                        .strengths(analysis.summaryAnalysis().strengths())
                        .suggestions(analysis.summaryAnalysis().suggestions())
                        .improvedSummary(analysis.summaryAnalysis().improvedSummary())
                        .build();

        final List<ProjectAnalysisResponse> projectAnalyses =
                analysis.projectAnalyses().stream()
                        .map(project -> ProjectAnalysisResponse.builder()
                                .projectName(project.projectName())
                                .descriptionQuality(project.descriptionQuality())
                                .technologiesMentioned(project.technologiesMentioned())
                                .businessImpact(project.businessImpact())
                                .technicalDepth(project.technicalDepth())
                                .actionVerbs(project.actionVerbs())
                                .measurableOutcomes(project.measurableOutcomes())
                                .suggestions(project.suggestions())
                                .build())
                        .toList();

        final SkillsAnalysisResponse skillsAnalysis =
                SkillsAnalysisResponse.builder()
                        .technicalSkills(analysis.skillsAnalysis().technicalSkills())
                        .softSkills(analysis.skillsAnalysis().softSkills())
                        .organization(analysis.skillsAnalysis().organization())
                        .missingRelevantSkills(analysis.skillsAnalysis().missingRelevantSkills())
                        .build();

        final ExperienceAnalysisResponse experienceAnalysis =
                ExperienceAnalysisResponse.builder()
                        .actionVerbs(analysis.experienceAnalysis().actionVerbs())
                        .responsibilities(analysis.experienceAnalysis().responsibilities())
                        .achievements(analysis.experienceAnalysis().achievements())
                        .quantifiedImpact(analysis.experienceAnalysis().quantifiedImpact())
                        .suggestions(analysis.experienceAnalysis().suggestions())
                        .build();

        return ResumeReviewResponse.builder()
                .resumeId(resume.getId())
                .resumeTitle(resume.getHeadline())
                .resumeScore(analysis.resumeScore())
                .atsScore(analysis.atsScore())
                .strengths(analysis.strengths())
                .weaknesses(analysis.weaknesses())
                .missingSkills(analysis.missingSkills())
                .suggestions(suggestions)
                .categoryScores(categoryScores)
                .missingSections(analysis.missingSections())
                .foundKeywords(analysis.foundKeywords())
                .missingKeywords(analysis.missingKeywords())
                .keywordSuggestions(analysis.keywordSuggestions())
                .formattingAnalysis(analysis.formattingAnalysis())
                .summaryAnalysis(summaryAnalysis)
                .projectAnalyses(projectAnalyses)
                .skillsAnalysis(skillsAnalysis)
                .experienceAnalysis(experienceAnalysis)
                .build();
    }

    /**
     * Retrieves the currently authenticated user from the database.
     *
     * @return the authenticated {@link User} entity
     * @throws ResourceNotFoundException if the user is not found in the database
     */
    private User getAuthenticatedUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with email " + email + " not found"));
    }

    /**
     * Retrieves a resume by ID and verifies it belongs to the currently
     * authenticated user.
     *
     * @param id the resume ID to retrieve
     * @return the {@link Resume} entity owned by the authenticated user
     * @throws ResourceNotFoundException if the resume is not found or does
     *                                   not belong to the authenticated user
     */
    private Resume getResumeOwnedByAuthenticatedUser(final Long id) {
        final User user = getAuthenticatedUser();
        final Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resume with id " + id + " not found"));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException(
                    "Resume with id " + id + " not found for the authenticated user");
        }

        return resume;
    }

}
