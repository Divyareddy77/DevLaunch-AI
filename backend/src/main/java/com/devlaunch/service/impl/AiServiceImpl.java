package com.devlaunch.service.impl;

import com.devlaunch.dto.request.MockInterviewAnswerRequest;
import com.devlaunch.dto.request.MockInterviewStartRequest;
import com.devlaunch.dto.request.MockInterviewSubmitRequest;
import com.devlaunch.dto.request.ResumeReviewRequest;
import com.devlaunch.dto.response.MockInterviewFeedbackItemResponse;
import com.devlaunch.dto.response.MockInterviewFeedbackResponse;
import com.devlaunch.dto.response.MockInterviewHistoryItemResponse;
import com.devlaunch.dto.response.MockInterviewHistoryResponse;
import com.devlaunch.dto.response.MockInterviewQuestionResponse;
import com.devlaunch.dto.response.MockInterviewStartResponse;
import com.devlaunch.dto.response.ResumeReviewResponse;
import com.devlaunch.dto.response.ResumeReviewSuggestion;
import com.devlaunch.entity.Achievement;
import com.devlaunch.entity.Certification;
import com.devlaunch.entity.Education;
import com.devlaunch.entity.Experience;
import com.devlaunch.entity.InterviewSession;
import com.devlaunch.entity.Project;
import com.devlaunch.entity.Resume;
import com.devlaunch.entity.Skill;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.InterviewType;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.repository.AchievementRepository;
import com.devlaunch.repository.CertificationRepository;
import com.devlaunch.repository.EducationRepository;
import com.devlaunch.repository.ExperienceRepository;
import com.devlaunch.repository.InterviewSessionRepository;
import com.devlaunch.repository.ProjectRepository;
import com.devlaunch.repository.ResumeRepository;
import com.devlaunch.repository.SkillRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.ai.InterviewAnswer;
import com.devlaunch.service.ai.InterviewQuestion;
import com.devlaunch.service.ai.MockInterviewFeedback;
import com.devlaunch.service.ai.MockInterviewProvider;
import com.devlaunch.service.ai.ResumeContent;
import com.devlaunch.service.ai.ResumeReviewAnalysis;
import com.devlaunch.service.ai.ResumeReviewProvider;
import com.devlaunch.service.interfaces.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link AiService} providing AI-powered resume review
 * and mock interviews.
 * <p>
 * For resume review, loads the resume owned by the authenticated user
 * together with all of its sections, assembles a {@link ResumeContent}
 * snapshot, and delegates the analysis to a {@link ResumeReviewProvider}.
 * For mock interviews, delegates question generation and answer
 * evaluation to a {@link MockInterviewProvider} and persists completed
 * sessions to the user's interview history. In both cases the primary LLM
 * provider is preferred when configured; on any failure or when no
 * provider is configured, the deterministic sample provider is used so
 * the features remain fully functional.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class AiServiceImpl implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiServiceImpl.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final EducationRepository educationRepository;
    private final ExperienceRepository experienceRepository;
    private final SkillRepository skillRepository;
    private final ProjectRepository projectRepository;
    private final CertificationRepository certificationRepository;
    private final AchievementRepository achievementRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final ResumeReviewProvider openAiResumeReviewProvider;
    private final ResumeReviewProvider sampleResumeReviewProvider;
    private final MockInterviewProvider openAiMockInterviewProvider;
    private final MockInterviewProvider sampleMockInterviewProvider;

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
     * @param openAiResumeReviewProvider   the primary LLM resume review provider
     * @param sampleResumeReviewProvider   the deterministic resume review fallback
     * @param openAiMockInterviewProvider  the primary LLM mock interview provider
     * @param sampleMockInterviewProvider  the deterministic mock interview fallback
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
                         @Qualifier("openAiResumeReviewProvider")
                         final ResumeReviewProvider openAiResumeReviewProvider,
                         @Qualifier("sampleResumeReviewProvider")
                         final ResumeReviewProvider sampleResumeReviewProvider,
                         @Qualifier("openAiMockInterviewProvider")
                         final MockInterviewProvider openAiMockInterviewProvider,
                         @Qualifier("sampleMockInterviewProvider")
                         final MockInterviewProvider sampleMockInterviewProvider) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.skillRepository = skillRepository;
        this.projectRepository = projectRepository;
        this.certificationRepository = certificationRepository;
        this.achievementRepository = achievementRepository;
        this.interviewSessionRepository = interviewSessionRepository;
        this.openAiResumeReviewProvider = openAiResumeReviewProvider;
        this.sampleResumeReviewProvider = sampleResumeReviewProvider;
        this.openAiMockInterviewProvider = openAiMockInterviewProvider;
        this.sampleMockInterviewProvider = sampleMockInterviewProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ResumeReviewResponse reviewResume(final ResumeReviewRequest request) {
        final Resume resume = getResumeOwnedByAuthenticatedUser(request.getResumeId());

        final ResumeContent content = buildResumeContent(resume);
        final ResumeReviewAnalysis analysis = analyze(content, request.getTargetRole());

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
        final List<InterviewQuestion> questions = generateQuestions(request.getInterviewType());

        log.info("Mock interview started for type={}: {} questions generated",
                request.getInterviewType(), questions.size());

        return MockInterviewStartResponse.builder()
                .sessionId(UUID.randomUUID().toString())
                .interviewType(request.getInterviewType())
                .questions(questions.stream()
                        .map(question -> MockInterviewQuestionResponse.builder()
                                .id(question.id())
                                .question(question.question())
                                .hint(question.hint())
                                .build())
                        .toList())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MockInterviewFeedbackResponse submitMockInterview(final MockInterviewSubmitRequest request) {
        final User user = getAuthenticatedUser();

        final List<InterviewAnswer> answers = request.getAnswers().stream()
                .map(this::toInterviewAnswer)
                .toList();

        final MockInterviewFeedback evaluation = evaluate(request.getInterviewType(), answers);

        final InterviewSession session = InterviewSession.builder()
                .sessionId(request.getSessionId())
                .interviewType(request.getInterviewType())
                .overallScore(evaluation.overallScore())
                .questionCount(answers.size())
                .completedAt(LocalDateTime.now())
                .user(user)
                .build();
        interviewSessionRepository.save(session);

        log.info("Mock interview submitted for user id={}, type={}: overallScore={}",
                user.getId(), request.getInterviewType(), evaluation.overallScore());

        return toFeedbackResponse(request.getSessionId(), request.getInterviewType(), evaluation);
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

        return MockInterviewHistoryResponse.builder()
                .history(sessions.stream().map(this::toHistoryItem).toList())
                .totalInterviews((long) sessions.size())
                .averageScore(Math.round(average * 10.0) / 10.0)
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
     * provider, falling back to the deterministic question bank when the
     * primary is not configured, throws, or returns no questions.
     *
     * @param type the interview category to generate questions for
     * @return the generated questions
     */
    private List<InterviewQuestion> generateQuestions(final InterviewType type) {
        final MockInterviewProvider primary =
                openAiMockInterviewProvider.isConfigured()
                        ? openAiMockInterviewProvider
                        : sampleMockInterviewProvider;

        try {
            final List<InterviewQuestion> questions = primary.generateQuestions(type);
            if (questions.isEmpty()) {
                throw new IllegalStateException("Provider returned no questions");
            }
            return questions;
        } catch (final Exception e) {
            log.warn("Mock interview provider '{}' failed to generate questions, "
                            + "falling back to the question bank: {}",
                    primary.getClass().getSimpleName(), e.getMessage());
            return sampleMockInterviewProvider.generateQuestions(type);
        }
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
     * Maps a request answer DTO to the internal {@link InterviewAnswer}.
     */
    private InterviewAnswer toInterviewAnswer(final MockInterviewAnswerRequest answer) {
        return new InterviewAnswer(answer.getQuestionId(), answer.getQuestion(), answer.getAnswer());
    }

    /**
     * Maps the internal evaluation result to the public feedback response
     * DTO, attaching the session and category identifiers.
     *
     * @param sessionId  the session identifier of the interview
     * @param type       the interview category
     * @param evaluation the internal evaluation result
     * @return the public feedback response DTO
     */
    private MockInterviewFeedbackResponse toFeedbackResponse(
            final String sessionId, final InterviewType type,
            final MockInterviewFeedback evaluation) {
        final List<MockInterviewFeedbackItemResponse> items = evaluation.feedback().stream()
                .map(item -> MockInterviewFeedbackItemResponse.builder()
                        .questionId(item.questionId())
                        .question(item.question())
                        .answer(item.answer())
                        .score(item.score())
                        .feedback(item.feedback())
                        .suggestions(item.suggestions())
                        .build())
                .toList();

        return MockInterviewFeedbackResponse.builder()
                .sessionId(sessionId)
                .interviewType(type)
                .overallScore(evaluation.overallScore())
                .feedback(items)
                .strengths(evaluation.strengths())
                .areasForImprovement(evaluation.areasForImprovement())
                .build();
    }

    /**
     * Maps an {@link InterviewSession} entity to the public history item
     * DTO.
     *
     * @param session the completed interview session
     * @return the public history item DTO
     */
    private MockInterviewHistoryItemResponse toHistoryItem(final InterviewSession session) {
        return MockInterviewHistoryItemResponse.builder()
                .sessionId(session.getSessionId())
                .interviewType(session.getInterviewType())
                .completedAt(session.getCompletedAt())
                .overallScore(session.getOverallScore())
                .questionCount(session.getQuestionCount())
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
                skills.stream().map(Skill::getSkillName).toList(),
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

        return ResumeReviewResponse.builder()
                .resumeId(resume.getId())
                .resumeTitle(resume.getHeadline())
                .resumeScore(analysis.resumeScore())
                .atsScore(analysis.atsScore())
                .strengths(analysis.strengths())
                .weaknesses(analysis.weaknesses())
                .missingSkills(analysis.missingSkills())
                .suggestions(suggestions)
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
