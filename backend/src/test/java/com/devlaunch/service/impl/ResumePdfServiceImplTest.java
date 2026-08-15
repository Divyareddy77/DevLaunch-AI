package com.devlaunch.service.impl;

import com.devlaunch.entity.Achievement;
import com.devlaunch.entity.Certification;
import com.devlaunch.entity.Education;
import com.devlaunch.entity.Experience;
import com.devlaunch.entity.Project;
import com.devlaunch.entity.Resume;
import com.devlaunch.entity.Skill;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.repository.AchievementRepository;
import com.devlaunch.repository.CertificationRepository;
import com.devlaunch.repository.EducationRepository;
import com.devlaunch.repository.ExperienceRepository;
import com.devlaunch.repository.ProjectRepository;
import com.devlaunch.repository.ResumeRepository;
import com.devlaunch.repository.SkillRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.ResumePdfDocument;
import com.devlaunch.service.ResumePdfTemplate;
import com.devlaunch.service.interfaces.NotificationService;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the resume PDF generation in {@link ResumePdfServiceImpl}.
 * <p>
 * Verifies that a valid PDF document is produced with the
 * {@code <FirstName>_Resume.pdf} file name, that every populated section
 * is rendered into the document, that all five layout templates generate
 * identical content, that the download triggers a notification, and that
 * ownership verification rejects resumes that do not belong to the
 * authenticated user.
 * </p>
 *
 * @author DevLaunch
 */
@ExtendWith(MockitoExtension.class)
class ResumePdfServiceImplTest {

    private static final String USER_EMAIL = "divya@example.com";

    private static final List<String> EXPECTED_SECTIONS = List.of(
            "PROFESSIONAL SUMMARY", "SKILLS", "EXPERIENCE", "PROJECTS",
            "EDUCATION", "CERTIFICATIONS", "ACHIEVEMENTS",
            "Stanford University", "Software Engineer", "Acme Inc",
            "Java (Advanced)", "DevLaunch", "AWS Solutions Architect",
            "Hackathon Winner");

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private UserRepository userRepository;

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
    private NotificationService notificationService;

    private ResumePdfServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ResumePdfServiceImpl(
                resumeRepository, userRepository, educationRepository, experienceRepository,
                skillRepository, projectRepository, certificationRepository, achievementRepository,
                notificationService);
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
        final User user = User.builder()
                .firstName("Divya")
                .lastName("Reddy")
                .email(USER_EMAIL)
                .phone("+91 98765 43210")
                .build();
        user.setId(1L);
        return user;
    }

    private Resume resumeOwnedBy(final User owner) {
        final Resume resume = Resume.builder()
                .headline("Senior Full-Stack Developer")
                .summary("Experienced developer building scalable web applications.")
                .linkedinUrl("https://linkedin.com/in/divyareddy")
                .githubUrl("https://github.com/divyareddy")
                .user(owner)
                .build();
        resume.setId(10L);
        return resume;
    }

    private void stubEmptySections(final Resume resume) {
        when(educationRepository.findByResume(resume)).thenReturn(List.of());
        when(experienceRepository.findByResume(resume)).thenReturn(List.of());
        when(skillRepository.findByResume(resume)).thenReturn(List.of());
        when(projectRepository.findByResume(resume)).thenReturn(List.of());
        when(certificationRepository.findByResume(resume)).thenReturn(List.of());
        when(achievementRepository.findByResume(resume)).thenReturn(List.of());
    }

    private void stubPopulatedSections(final Resume resume) {
        when(educationRepository.findByResume(resume)).thenReturn(List.of(Education.builder()
                .institutionName("Stanford University")
                .degree("B.Sc.")
                .fieldOfStudy("Computer Science")
                .grade("3.8")
                .build()));
        when(experienceRepository.findByResume(resume)).thenReturn(List.of(Experience.builder()
                .jobTitle("Software Engineer")
                .companyName("Acme Inc")
                .employmentType("Full-time")
                .location("Remote")
                .description("Built the payments API.")
                .build()));
        when(skillRepository.findByResume(resume)).thenReturn(List.of(
                Skill.builder().skillName("Java").proficiency("Advanced").build()));
        when(projectRepository.findByResume(resume)).thenReturn(List.of(Project.builder()
                .projectName("DevLaunch")
                .technologies("Java, React")
                .description("Career hub for developers.")
                .githubUrl("https://github.com/devlaunch")
                .build()));
        when(certificationRepository.findByResume(resume)).thenReturn(List.of(Certification.builder()
                .certificationName("AWS Solutions Architect")
                .issuingOrganization("Amazon Web Services")
                .build()));
        when(achievementRepository.findByResume(resume)).thenReturn(List.of(Achievement.builder()
                .title("Hackathon Winner")
                .description("Won the regional hackathon.")
                .build()));
    }

    @Test
    @DisplayName("generates a valid PDF with the owner-first-name file name and header content")
    void generatesPdfWithOwnerFirstNameFileName() {
        authenticate();
        final User user = user();
        final Resume resume = resumeOwnedBy(user);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(resumeRepository.findById(10L)).thenReturn(Optional.of(resume));
        stubEmptySections(resume);

        final ResumePdfDocument pdf = service.generatePdf(10L, ResumePdfTemplate.CLASSIC_PROFESSIONAL);

        // PDF files start with the %PDF magic number
        assertTrue(pdf.content().length > 0, "PDF content must not be empty");
        assertEquals('%', pdf.content()[0]);
        assertEquals('P', pdf.content()[1]);
        assertEquals('D', pdf.content()[2]);
        assertEquals('F', pdf.content()[3]);

        assertEquals("Divya_Resume.pdf", pdf.fileName());

        // The header, contact details, and summary must actually be rendered
        final String text = extractText(pdf);
        assertTrue(text.contains("Divya Reddy"), "PDF must contain the full name");
        assertTrue(text.contains("divya@example.com"), "PDF must contain the email");
        assertTrue(text.contains("PROFESSIONAL SUMMARY"), "PDF must contain the summary section");
        assertTrue(text.contains("Experienced developer building scalable web applications."),
                "PDF must contain the summary text");

        verify(notificationService).createNotification(
                eq(user), eq(NotificationType.RESUME),
                eq("Resume downloaded"), anyString());
    }

    @Test
    @DisplayName("renders every populated resume section into the document")
    void rendersEveryPopulatedSection() {
        authenticate();
        final User user = user();
        final Resume resume = resumeOwnedBy(user);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(resumeRepository.findById(10L)).thenReturn(Optional.of(resume));
        stubPopulatedSections(resume);

        final ResumePdfDocument pdf = service.generatePdf(10L, ResumePdfTemplate.CLASSIC_PROFESSIONAL);

        final String text = extractText(pdf);
        for (final String expected : EXPECTED_SECTIONS) {
            assertTrue(text.contains(expected), "PDF must contain: " + expected);
        }
    }

    @Test
    @DisplayName("every template renders identical resume content in a valid PDF")
    void rendersEveryTemplateWithIdenticalContent() {
        authenticate();
        final User user = user();
        final Resume resume = resumeOwnedBy(user);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(resumeRepository.findById(10L)).thenReturn(Optional.of(resume));
        stubPopulatedSections(resume);

        // All templates must render the same resume content — only the
        // presentation changes. Content is verified per template below
        // (whitespace-insensitively, since PDF text extraction inside
        // table cells can vary in whitespace and ordering). The resume
        // values never change; only colours, fonts, and the header
        // treatment differ.
        for (final ResumePdfTemplate template : ResumePdfTemplate.values()) {
            final ResumePdfDocument pdf = service.generatePdf(10L, template);

            assertEquals('%', pdf.content()[0], "Invalid PDF for template " + template);
            assertEquals("Divya_Resume.pdf", pdf.fileName(),
                    "Wrong file name for template " + template);

            final String text = normalizeWhitespace(extractText(pdf));
            for (final String expected : EXPECTED_SECTIONS) {
                assertTrue(text.contains(normalizeWhitespace(expected)),
                        "Template " + template + " must contain: " + expected);
            }

            if (template == ResumePdfTemplate.EXECUTIVE) {
                assertTrue(text.contains("DIVYAREDDY"),
                        "Executive template must render the name in upper case");
            } else {
                assertTrue(text.contains("DivyaReddy"),
                        "Template " + template + " must contain the full name");
            }
        }
    }

    /**
     * Removes all whitespace so PDF text-extraction artifacts (line
     * breaks inside table cells) do not affect content assertions.
     *
     * @param text the raw extracted text
     * @return the text with all whitespace removed
     */
    private String normalizeWhitespace(final String text) {
        return text.replaceAll("\\s+", "");
    }

    @Test
    @DisplayName("throws when the resume does not exist")
    void throwsWhenResumeDoesNotExist() {
        authenticate();
        final User user = user();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(resumeRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.generatePdf(10L, ResumePdfTemplate.CLASSIC_PROFESSIONAL));
    }

    @Test
    @DisplayName("throws when the resume belongs to another user")
    void throwsWhenResumeBelongsToAnotherUser() {
        authenticate();
        final User user = user();
        final User otherUser = User.builder().email("other@example.com").build();
        otherUser.setId(99L);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(resumeRepository.findById(10L)).thenReturn(Optional.of(resumeOwnedBy(otherUser)));

        assertThrows(ResourceNotFoundException.class,
                () -> service.generatePdf(10L, ResumePdfTemplate.MODERN_BLUE));
        verify(notificationService, never()).createNotification(any(), any(), anyString(), anyString());
    }

    /**
     * Extracts the plain text of every page of the generated PDF so tests
     * can assert that the expected sections and values are rendered.
     *
     * @param pdf the generated PDF document
     * @return the concatenated page text
     */
    private String extractText(final ResumePdfDocument pdf) {
        try {
            final PdfReader reader = new PdfReader(pdf.content());
            final StringBuilder text = new StringBuilder();
            for (int page = 1; page <= reader.getNumberOfPages(); page++) {
                text.append(new PdfTextExtractor(reader).getTextFromPage(page));
            }
            reader.close();
            return text.toString();
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to read generated PDF", e);
        }
    }

}
