package com.devlaunch.service.impl;

import com.devlaunch.entity.Achievement;
import com.devlaunch.entity.Certification;
import com.devlaunch.entity.Education;
import com.devlaunch.entity.Experience;
import com.devlaunch.entity.Project;
import com.devlaunch.entity.Resume;
import com.devlaunch.entity.Skill;
import com.devlaunch.entity.User;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.repository.AchievementRepository;
import com.devlaunch.repository.CertificationRepository;
import com.devlaunch.repository.EducationRepository;
import com.devlaunch.repository.ExperienceRepository;
import com.devlaunch.repository.ProjectRepository;
import com.devlaunch.repository.ResumeRepository;
import com.devlaunch.repository.SkillRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.ResumePdfService;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementation of {@link ResumePdfService} that generates a PDF
 * document for a resume owned by the currently authenticated user.
 * <p>
 * Uses OpenPDF ({@code com.lowagie.text}) to create the PDF document.
 * Ownership verification follows the same pattern established in
 * {@link ResumeServiceImpl} to ensure that the requesting user can
 * only access their own resume data.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class ResumePdfServiceImpl implements ResumePdfService {

    private static final Logger log = LoggerFactory.getLogger(ResumePdfServiceImpl.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
    private static final Font LABEL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font VALUE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 11);

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final EducationRepository educationRepository;
    private final ExperienceRepository experienceRepository;
    private final SkillRepository skillRepository;
    private final ProjectRepository projectRepository;
    private final CertificationRepository certificationRepository;
    private final AchievementRepository achievementRepository;

    /**
     * Constructs the PDF generation service with the required dependencies.
     *
     * @param resumeRepository        repository for resume data access
     * @param userRepository          repository for user data access
     * @param educationRepository     repository for education data access
     * @param experienceRepository    repository for experience data access
     * @param skillRepository         repository for skill data access
     * @param projectRepository       repository for project data access
     * @param certificationRepository repository for certification data access
     * @param achievementRepository   repository for achievement data access
     */
    public ResumePdfServiceImpl(final ResumeRepository resumeRepository,
                                final UserRepository userRepository,
                                final EducationRepository educationRepository,
                                final ExperienceRepository experienceRepository,
                                final SkillRepository skillRepository,
                                final ProjectRepository projectRepository,
                                final CertificationRepository certificationRepository,
                                final AchievementRepository achievementRepository) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.skillRepository = skillRepository;
        this.projectRepository = projectRepository;
        this.certificationRepository = certificationRepository;
        this.achievementRepository = achievementRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(final Long resumeId) {
        // Verify the resume exists and belongs to the authenticated user
        final Resume resume = getResumeOwnedByAuthenticatedUser(resumeId);
        log.info("Generating PDF for resume id={} owned by user email={}",
                resume.getId(), resume.getUser().getEmail());

        return createPdfContent(resume);
    }

    /**
     * Creates a PDF document containing the full resume content.
     * <p>
     * Generates a professional PDF with the following sections:
     * Personal Information, Professional Information, Education,
     * Experience, Skills, Projects, Certifications, and Achievements.
     * Empty sections are skipped gracefully. Null values are omitted.
     * </p>
     *
     * @param resume the resume entity containing the data to render
     * @return a byte array containing the PDF document
     */
    private byte[] createPdfContent(final Resume resume) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final Document document = new Document();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Fetch related data
            final List<Education> educations = educationRepository.findByResume(resume);
            final List<Experience> experiences = experienceRepository.findByResume(resume);
            final List<Skill> skills = skillRepository.findByResume(resume);
            final List<Project> projects = projectRepository.findByResume(resume);
            final List<Certification> certifications = certificationRepository.findByResume(resume);
            final List<Achievement> achievements = achievementRepository.findByResume(resume);

            // Title
            final Paragraph title = new Paragraph("DEVLAUNCH RESUME", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 1. Personal Information
            final User user = resume.getUser();
            addSectionHeader(document, "Personal Information");
            addField(document, "Full Name", user.getFirstName() + " " + user.getLastName());
            addField(document, "Email", user.getEmail());
            if (user.getPhone() != null && !user.getPhone().isBlank()) {
                addField(document, "Phone", user.getPhone());
            }
            document.add(createEmptyLine());

            // 2. Professional Information
            addSectionHeader(document, "Professional Information");
            addField(document, "Headline", resume.getHeadline());
            addField(document, "Professional Summary", resume.getSummary());
            document.add(createEmptyLine());

            // 3. Education
            if (!educations.isEmpty()) {
                addSectionHeader(document, "Education");
                for (final Education education : educations) {
                    addField(document, "Degree", education.getDegree());
                    addField(document, "Institution", education.getInstitutionName());
                    addOptionalField(document, "Start Date", formatDate(education.getStartDate()));
                    addOptionalField(document, "End Date", formatDate(education.getEndDate()));
                    addOptionalField(document, "Grade", education.getGrade());
                    document.add(createEmptyLine());
                }
            }

            // 4. Experience
            if (!experiences.isEmpty()) {
                addSectionHeader(document, "Experience");
                for (final Experience experience : experiences) {
                    addField(document, "Job Title", experience.getJobTitle());
                    addField(document, "Company", experience.getCompanyName());
                    addField(document, "Start Date", formatDate(experience.getStartDate()));
                    addOptionalField(document, "End Date", formatDate(experience.getEndDate()));
                    addOptionalField(document, "Description", experience.getDescription());
                    document.add(createEmptyLine());
                }
            }

            // 5. Skills
            if (!skills.isEmpty()) {
                addSectionHeader(document, "Skills");
                for (final Skill skill : skills) {
                    addField(document, "Skill Name", skill.getSkillName());
                    document.add(createEmptyLine());
                }
            }

            // 6. Projects
            if (!projects.isEmpty()) {
                addSectionHeader(document, "Projects");
                for (final Project project : projects) {
                    addField(document, "Project Name", project.getProjectName());
                    addField(document, "Technologies", project.getTechnologies());
                    addField(document, "Description", project.getDescription());
                    addOptionalField(document, "GitHub URL", project.getGithubUrl());
                    addOptionalField(document, "Live URL", project.getLiveUrl());
                    document.add(createEmptyLine());
                }
            }

            // 7. Certifications
            if (!certifications.isEmpty()) {
                addSectionHeader(document, "Certifications");
                for (final Certification certification : certifications) {
                    addField(document, "Certification Name", certification.getCertificationName());
                    addField(document, "Issuing Organization", certification.getIssuingOrganization());
                    addOptionalField(document, "Issue Date", formatDate(certification.getIssueDate()));
                    document.add(createEmptyLine());
                }
            }

            // 8. Achievements
            if (!achievements.isEmpty()) {
                addSectionHeader(document, "Achievements");
                for (final Achievement achievement : achievements) {
                    addField(document, "Title", achievement.getTitle());
                    addOptionalField(document, "Description", achievement.getDescription());
                    addOptionalField(document, "Date Achieved", formatDate(achievement.getDateAchieved()));
                    document.add(createEmptyLine());
                }
            }

            document.close();
        } catch (final DocumentException e) {
            log.error("Failed to generate PDF document", e);
            throw new RuntimeException("Failed to generate PDF document", e);
        }

        return baos.toByteArray();
    }

    /**
     * Adds a section header to the document with bold formatting and
     * spacing above.
     *
     * @param document    the PDF document to add the header to
     * @param sectionName the text of the section header
     * @throws DocumentException if an error occurs while adding to the document
     */
    private void addSectionHeader(final Document document, final String sectionName) throws DocumentException {
        final Paragraph header = new Paragraph(sectionName, SECTION_FONT);
        header.setSpacingBefore(10);
        header.setSpacingAfter(5);
        document.add(header);
    }

    /**
     * Adds a field label and its non-blank value to the document.
     * <p>
     * The label is rendered in bold followed by the value in regular font.
     * If the value is {@code null} or blank, the field is silently skipped.
     * </p>
     *
     * @param document the PDF document to add the field to
     * @param label    the field label text
     * @param value    the field value text (may be {@code null} or blank)
     * @throws DocumentException if an error occurs while adding to the document
     */
    private void addOptionalField(final Document document, final String label, final String value)
            throws DocumentException {
        if (value == null || value.isBlank()) {
            return;
        }
        addField(document, label, value);
    }

    /**
     * Adds a field label and its value to the document.
     * <p>
     * The label is rendered in bold followed by the value in regular font.
     * The value is expected to be non-null and non-blank.
     * </p>
     *
     * @param document the PDF document to add the field to
     * @param label    the field label text
     * @param value    the field value text (must not be null or blank)
     * @throws DocumentException if an error occurs while adding to the document
     */
    private void addField(final Document document, final String label, final String value)
            throws DocumentException {
        final Paragraph paragraph = new Paragraph();
        paragraph.add(new Chunk(label + ": ", LABEL_FONT));
        paragraph.add(new Chunk(value, VALUE_FONT));
        paragraph.setSpacingAfter(2);
        document.add(paragraph);
    }

    /**
     * Formats a {@link LocalDate} to a readable month-year string.
     * <p>
     * Returns {@code null} if the provided date is {@code null}.
     * </p>
     *
     * @param date the date to format, may be {@code null}
     * @return the formatted date string (e.g. "Jan 2024"), or {@code null}
     *         if the input was {@code null}
     */
    private String formatDate(final LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * Creates an empty paragraph for spacing between sections or entries.
     *
     * @return an empty {@link Paragraph} suitable as spacing
     */
    private Paragraph createEmptyLine() {
        final Paragraph empty = new Paragraph(" ");
        empty.setSpacingAfter(5);
        return empty;
    }

    /**
     * Retrieves the currently authenticated user from the database.
     * <p>
     * Extracts the username (email) from the {@link SecurityContextHolder},
     * fetches the corresponding {@link User} entity from the repository,
     * and throws a {@link ResourceNotFoundException} if no matching user
     * is found.
     * </p>
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
     * <p>
     * Fetches the authenticated user first, then looks up the resume by ID.
     * Throws a {@link ResourceNotFoundException} if the resume does not exist
     * or if it belongs to a different user.
     * </p>
     *
     * @param id the resume ID to retrieve
     * @return the {@link Resume} entity owned by the authenticated user
     * @throws ResourceNotFoundException if the resume is not found or does not
     *                                   belong to the authenticated user
     */
    private Resume getResumeOwnedByAuthenticatedUser(final Long id) {
        final User user = getAuthenticatedUser();
        final Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resume with id " + id + " not found"));

        // Verify ownership: the resume must belong to the authenticated user
        if (!resume.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException(
                    "Resume with id " + id + " not found for the authenticated user");
        }

        return resume;
    }

}
