package com.devlaunch.service.impl;

import com.devlaunch.dto.request.MockInterviewAnswerRequest;
import com.devlaunch.dto.request.MockInterviewSubmitRequest;
import com.devlaunch.dto.request.ResumeReviewRequest;
import com.devlaunch.dto.response.MockInterviewHistoryItemResponse;
import com.devlaunch.dto.response.MockInterviewHistoryResponse;
import com.devlaunch.dto.response.MockInterviewCategoryResponse;
import com.devlaunch.dto.response.ResumeReviewResponse;
import com.devlaunch.dto.response.TranscribeResponse;
import com.devlaunch.entity.InterviewSession;
import com.devlaunch.entity.InterviewSessionQuestion;
import com.devlaunch.entity.Resume;
import com.devlaunch.entity.ResumeReview;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.InterviewType;
import com.devlaunch.entity.enums.NotificationType;
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
import com.devlaunch.service.ai.MockInterviewFeedback;
import com.devlaunch.service.ai.MockInterviewProvider;
import com.devlaunch.service.ai.OpenAiWhisperTranscriber;
import com.devlaunch.service.ai.ResumeReviewAnalysis;
import com.devlaunch.service.ai.ResumeReviewProvider;
import com.devlaunch.service.ai.WhisperTranscription;
import com.devlaunch.service.interfaces.InterviewQuestionBankService;
import com.devlaunch.service.interfaces.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the mock interview session handling in
 * {@link AiServiceImpl}.
 * <p>
 * Verifies that the exact questions answered in a session are snapshotted
 * onto the persisted session and that the interview history returns those
 * original questions, so future question-bank changes never alter past
 * history.
 * </p>
 *
 * @author DevLaunch
 */
@ExtendWith(MockitoExtension.class)
class AiServiceImplTest {

    private static final String USER_EMAIL = "dev@example.com";

    @Mock
    private UserRepository userRepository;

    @Mock
    private InterviewSessionRepository interviewSessionRepository;

    @Mock
    private ResumeReviewRepository resumeReviewRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private EducationRepository educationRepository;

    @Mock
    private ExperienceRepository experienceRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private CertificationRepository certificationRepository;

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private ResumeReviewProvider openAiResumeReviewProvider;

    @Mock
    private ResumeReviewProvider sampleResumeReviewProvider;

    @Mock
    private MockInterviewProvider sampleMockInterviewProvider;

    @Mock
    private MockInterviewProvider openAiMockInterviewProvider;

    @Mock
    private OpenAiWhisperTranscriber whisperTranscriber;

    @Mock
    private InterviewQuestionBankService questionBankService;

    @Mock
    private NotificationService notificationService;

    private AiServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AiServiceImpl(
                resumeRepository, userRepository, educationRepository, experienceRepository,
                skillRepository, projectRepository, certificationRepository, achievementRepository,
                interviewSessionRepository, resumeReviewRepository, questionBankService,
                openAiResumeReviewProvider, sampleResumeReviewProvider,
                openAiMockInterviewProvider, sampleMockInterviewProvider,
                whisperTranscriber, notificationService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(USER_EMAIL, null, List.of()));
    }

    private User user() {
        final User user = User.builder().email(USER_EMAIL).build();
        user.setId(1L);
        return user;
    }

    private MockInterviewSubmitRequest submitRequest() {
        return MockInterviewSubmitRequest.builder()
                .sessionId("session-1")
                .interviewType(InterviewType.REACT)
                .answers(List.of(
                        MockInterviewAnswerRequest.builder()
                                .questionId("101")
                                .question("What are React hooks?")
                                .answer("Hooks let function components use state.")
                                .build(),
                        MockInterviewAnswerRequest.builder()
                                .questionId("102")
                                .question("Explain the virtual DOM.")
                                .answer("React diffs a virtual tree before rendering.")
                                .build()))
                .build();
    }

    private MockInterviewFeedback feedback() {
        return new MockInterviewFeedback(
                70,
                70, 75, 68, 70, 72, 66, 74,
                List.of(
                        new MockInterviewFeedback.Item("101", "What are React hooks?",
                                "Hooks let function components use state.", 80, "Good", List.of(), null),
                        new MockInterviewFeedback.Item("102", "Explain the virtual DOM.",
                                "React diffs a virtual tree before rendering.", 60, "OK", List.of(), null)),
                List.of("Solid hooks answer"),
                List.of("Deepen the virtual DOM explanation"),
                List.of("Practice React Hooks."),
                List.of());
    }

    @Test
    @DisplayName("transcribing a voice answer returns the transcript and duration")
    void transcribeReturnsTranscriptAndDuration() {
        final MockMultipartFile audio = new MockMultipartFile(
                "file", "answer.webm", "audio/webm", "audio data".getBytes());
        when(whisperTranscriber.transcribe(eq(audio), eq(65)))
                .thenReturn(new WhisperTranscription(
                        "I led a team of five engineers.", 65.0));

        final TranscribeResponse response = service.transcribe(audio, 65);

        assertEquals("I led a team of five engineers.", response.getTranscript());
        assertEquals(65.0, response.getDuration());
    }

    @Test
    @DisplayName("submitting an interview snapshots the exact answered questions")
    void submitPersistsTheExactAnsweredQuestions() {
        authenticate();
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(sampleMockInterviewProvider.evaluate(eq(InterviewType.REACT), anyList()))
                .thenReturn(feedback());

        service.submitMockInterview(submitRequest());

        // The completed interview always produces a notification
        verify(notificationService).createNotification(
                eq(user), eq(NotificationType.MOCK_INTERVIEW),
                eq("Interview completed"), anyString());

        final ArgumentCaptor<InterviewSession> captor =
                ArgumentCaptor.forClass(InterviewSession.class);
        verify(interviewSessionRepository).save(captor.capture());

        final InterviewSession saved = captor.getValue();
        assertEquals(2, saved.getQuestionCount());

        final List<InterviewSessionQuestion> questions = saved.getQuestions();
        assertEquals(List.of("101", "102"),
                questions.stream().map(InterviewSessionQuestion::getQuestionId).toList());
        assertEquals(List.of("What are React hooks?", "Explain the virtual DOM."),
                questions.stream().map(InterviewSessionQuestion::getQuestion).toList());
    }

    @Test
    @DisplayName("interview history returns the original snapshotted questions")
    void historyRetainsTheOriginalQuestions() {
        authenticate();
        // The same User instance must be returned by the lookup and expected
        // by the history query: User equality is based on the identity of the
        // (unequal) BaseEntity superclass, so distinct instances never match.
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

        final InterviewSession session = InterviewSession.builder()
                .sessionId("session-1")
                .interviewType(InterviewType.REACT)
                .overallScore(70)
                .questionCount(2)
                .completedAt(LocalDateTime.of(2026, 8, 5, 10, 0))
                .questions(List.of(
                        new InterviewSessionQuestion("101", "What are React hooks?"),
                        new InterviewSessionQuestion("102", "Explain the virtual DOM.")))
                .build();
        when(interviewSessionRepository.findByUserOrderByCompletedAtDesc(user))
                .thenReturn(List.of(session));

        final MockInterviewHistoryResponse response = service.getMockInterviewHistory();

        assertEquals(1, response.getHistory().size());
        final MockInterviewHistoryItemResponse item = response.getHistory().get(0);
        assertEquals(2, item.getQuestions().size());
        assertEquals("101", item.getQuestions().get(0).getId());
        assertEquals("What are React hooks?", item.getQuestions().get(0).getQuestion());
        assertEquals("Explain the virtual DOM.", item.getQuestions().get(1).getQuestion());
    }

    @Test
    @DisplayName("deleting a mock interview removes the session from history")
    void deleteRemovesSessionFromHistory() {
        authenticate();
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

        final InterviewSession session = InterviewSession.builder()
                .sessionId("session-1")
                .interviewType(InterviewType.REACT)
                .overallScore(70)
                .questionCount(2)
                .build();
        when(interviewSessionRepository.findBySessionIdAndUser("session-1", user))
                .thenReturn(Optional.of(session));

        service.deleteMockInterview("session-1");

        verify(interviewSessionRepository).delete(session);
    }

    @Test
    @DisplayName("history aggregates best score, streak, and readiness level")
    void historyAggregatesStatistics() {
        authenticate();
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

        final InterviewSession latest = InterviewSession.builder()
                .sessionId("s2")
                .interviewType(InterviewType.JAVA)
                .overallScore(75)
                .questionCount(5)
                .completedAt(LocalDateTime.now())
                .build();
        final InterviewSession previous = InterviewSession.builder()
                .sessionId("s1")
                .interviewType(InterviewType.JAVA)
                .overallScore(88)
                .questionCount(5)
                .completedAt(LocalDateTime.now().minusDays(1))
                .build();
        when(interviewSessionRepository.findByUserOrderByCompletedAtDesc(user))
                .thenReturn(List.of(latest, previous));

        final MockInterviewHistoryResponse response = service.getMockInterviewHistory();

        assertEquals(2, response.getTotalInterviews());
        assertEquals(88, response.getBestScore());
        assertEquals(81.5, response.getAverageScore());
        assertEquals(InterviewType.JAVA, response.getMostPracticedCategory());
        assertEquals(2, response.getCurrentStreak());
        assertEquals(100.0, response.getSuccessRate());
        assertEquals("Interview Ready", response.getReadinessLevel());
        assertEquals(2, response.getScoreTrend().size());
    }

    @Test
    @DisplayName("category statistics combine bank size and personal history")
    void categoryStatisticsCombineBankAndHistory() {
        authenticate();
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(questionBankService.countActive(any(InterviewType.class))).thenReturn(0L);
        when(questionBankService.countActive(InterviewType.JAVA)).thenReturn(42L);

        final InterviewSession javaSession = InterviewSession.builder()
                .sessionId("s1")
                .interviewType(InterviewType.JAVA)
                .overallScore(80)
                .questionCount(5)
                .completedAt(LocalDateTime.now().minusDays(1))
                .build();
        when(interviewSessionRepository.findByUserOrderByCompletedAtDesc(user))
                .thenReturn(List.of(javaSession));

        final List<MockInterviewCategoryResponse> responses =
                service.getMockInterviewCategories();

        final MockInterviewCategoryResponse java = responses.stream()
                .filter(response -> response.getInterviewType() == InterviewType.JAVA)
                .findFirst()
                .orElseThrow();
        assertEquals(42, java.getQuestionBankSize());
        assertEquals(1, java.getAttemptCount());
        assertEquals(80, java.getPreviousBestScore());
        assertEquals(javaSession.getCompletedAt(), java.getLastAttemptAt());
    }

    @Test
    @DisplayName("reviewing a resume persists a summary record for the admin module")
    void reviewPersistsResumeReviewHistory() {
        authenticate();
        final User user = user();
        final Resume resume = Resume.builder()
                .headline("Senior Developer")
                .summary("Experienced full-stack developer.")
                .user(user)
                .build();
        resume.setId(10L);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(resumeRepository.findById(10L)).thenReturn(Optional.of(resume));
        when(educationRepository.findByResume(resume)).thenReturn(List.of());
        when(experienceRepository.findByResume(resume)).thenReturn(List.of());
        when(skillRepository.findByResume(resume)).thenReturn(List.of());
        when(projectRepository.findByResume(resume)).thenReturn(List.of());
        when(certificationRepository.findByResume(resume)).thenReturn(List.of());
        when(achievementRepository.findByResume(resume)).thenReturn(List.of());
        when(sampleResumeReviewProvider.analyze(any(), eq("Java Developer")))
                .thenReturn(new ResumeReviewAnalysis(
                        78, 65,
                        List.of("Good structure"),
                        List.of("Weak summary"),
                        List.of("AWS"),
                        List.of(),
                        List.of(new ResumeReviewAnalysis.CategoryScore("Skills", 14, 20)),
                        List.of("Portfolio"),
                        List.of("Java", "Spring Boot"),
                        List.of("Docker"),
                        List.of("Add Docker to improve coverage"),
                        List.of("Section order is recruiter-friendly."),
                        new ResumeReviewAnalysis.SummaryAnalysis(
                                70, List.of("Solid length"), List.of("Quantify impact"),
                                "Improved summary text"),
                        List.of(new ResumeReviewAnalysis.ProjectAnalysis(
                                "Payments API", "Good", List.of("Spring Boot"), true,
                                "Moderate", List.of("built"), true, List.of("Add metrics"))),
                        new ResumeReviewAnalysis.SkillsAnalysis(
                                List.of("Java"), List.of("Leadership"),
                                "Skills are listed flat", List.of("Docker")),
                        new ResumeReviewAnalysis.ExperienceAnalysis(
                                List.of("built"), "Responsibilities described",
                                "Achievements highlighted", true, List.of("Keep roles detailed"))));

        service.reviewResume(ResumeReviewRequest.builder()
                .resumeId(10L)
                .targetRole("Java Developer")
                .build());

        final ArgumentCaptor<ResumeReview> captor =
                ArgumentCaptor.forClass(ResumeReview.class);
        verify(resumeReviewRepository).save(captor.capture());

        final ResumeReview saved = captor.getValue();
        assertEquals(78, saved.getResumeScore());
        assertEquals(65, saved.getAtsScore());
        assertEquals("Java Developer", saved.getTargetRole());
        assertEquals(user, saved.getUser());
        assertEquals(resume, saved.getResume());
    }

    @Test
    @DisplayName("reviewing a resume returns the full professional ATS report")
    void reviewReturnsTheFullAtsReport() {
        authenticate();
        final User user = user();
        final Resume resume = Resume.builder()
                .headline("Senior Developer")
                .summary("Experienced full-stack developer with 5 years of experience.")
                .user(user)
                .build();
        resume.setId(10L);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(resumeRepository.findById(10L)).thenReturn(Optional.of(resume));
        when(educationRepository.findByResume(resume)).thenReturn(List.of());
        when(experienceRepository.findByResume(resume)).thenReturn(List.of());
        when(skillRepository.findByResume(resume)).thenReturn(List.of());
        when(projectRepository.findByResume(resume)).thenReturn(List.of());
        when(certificationRepository.findByResume(resume)).thenReturn(List.of());
        when(achievementRepository.findByResume(resume)).thenReturn(List.of());
        when(sampleResumeReviewProvider.analyze(any(), any()))
                .thenReturn(new ResumeReviewAnalysis(
                        70, 60,
                        List.of("Good structure"),
                        List.of("Weak summary"),
                        List.of("AWS"),
                        List.of(),
                        List.of(new ResumeReviewAnalysis.CategoryScore("Skills", 12, 20)),
                        List.of("Portfolio"),
                        List.of("Java"),
                        List.of("AWS"),
                        List.of("Add AWS to improve coverage"),
                        List.of("Resume length is appropriate."),
                        new ResumeReviewAnalysis.SummaryAnalysis(
                                70, List.of(), List.of(), "Improved summary text"),
                        List.of(),
                        new ResumeReviewAnalysis.SkillsAnalysis(
                                List.of(), List.of(), "No skills section", List.of()),
                        new ResumeReviewAnalysis.ExperienceAnalysis(
                                List.of(), "", "", false, List.of())));

        final ResumeReviewResponse response = service.reviewResume(ResumeReviewRequest.builder()
                .resumeId(10L)
                .targetRole("Java Developer")
                .build());

        assertEquals(60, response.getAtsScore());
        assertEquals(1, response.getCategoryScores().size());
        assertEquals("Skills", response.getCategoryScores().get(0).getCategory());
        assertEquals(12, response.getCategoryScores().get(0).getScore());
        assertEquals(List.of("Portfolio"), response.getMissingSections());
        assertEquals(List.of("Java"), response.getFoundKeywords());
        assertEquals(List.of("AWS"), response.getMissingKeywords());
        assertEquals(List.of("Add AWS to improve coverage"), response.getKeywordSuggestions());
        assertEquals(List.of("Resume length is appropriate."), response.getFormattingAnalysis());
        assertEquals("Improved summary text", response.getSummaryAnalysis().getImprovedSummary());
    }

}
