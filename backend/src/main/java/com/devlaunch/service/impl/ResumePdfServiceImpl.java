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
import com.devlaunch.service.PdfTemplateStyle;
import com.devlaunch.service.ResumePdfDocument;
import com.devlaunch.service.ResumePdfTemplate;
import com.devlaunch.service.interfaces.NotificationService;
import com.devlaunch.service.interfaces.ResumePdfService;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link ResumePdfService} that generates a PDF
 * document for a resume owned by the currently authenticated user.
 * <p>
 * A single renderer produces the resume in any of the
 * {@link ResumePdfTemplate} layouts. All section content and ordering is
 * identical across templates — only colours, typography, spacing, and the
 * header treatment vary, driven entirely by the template's
 * {@link PdfTemplateStyle}. This keeps the generator free of per-template
 * branches and guarantees every template stays ATS-friendly.
 * </p>
 * <p>
 * Uses OpenPDF ({@code com.lowagie.text}) on A4 pages with a
 * single-column, text-first layout. Sections are rendered only when they
 * contain data, and entries flow across pages without breaking mid-entry.
 * Ownership verification follows the same pattern established in
 * {@link ResumeServiceImpl}.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class ResumePdfServiceImpl implements ResumePdfService {

    private static final Logger log = LoggerFactory.getLogger(ResumePdfServiceImpl.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");
    private static final String PRESENT = "Present";
    private static final String EN_DASH = " \u2013 ";
    private static final String BULLET = " \u00b7 ";

    /** A4 page with ~1.7 cm margins, a clean ATS-friendly geometry. */
    private static final Rectangle PAGE_SIZE = PageSize.A4;

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final EducationRepository educationRepository;
    private final ExperienceRepository experienceRepository;
    private final SkillRepository skillRepository;
    private final ProjectRepository projectRepository;
    private final CertificationRepository certificationRepository;
    private final AchievementRepository achievementRepository;
    private final NotificationService notificationService;

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
     * @param notificationService     service for creating user notifications
     */
    public ResumePdfServiceImpl(final ResumeRepository resumeRepository,
                                final UserRepository userRepository,
                                final EducationRepository educationRepository,
                                final ExperienceRepository experienceRepository,
                                final SkillRepository skillRepository,
                                final ProjectRepository projectRepository,
                                final CertificationRepository certificationRepository,
                                final AchievementRepository achievementRepository,
                                final NotificationService notificationService) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.skillRepository = skillRepository;
        this.projectRepository = projectRepository;
        this.certificationRepository = certificationRepository;
        this.achievementRepository = achievementRepository;
        this.notificationService = notificationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ResumePdfDocument generatePdf(final Long resumeId, final ResumePdfTemplate template) {
        // Verify the resume exists and belongs to the authenticated user
        final Resume resume = getResumeOwnedByAuthenticatedUser(resumeId);
        log.info("Generating PDF for resume id={} owned by user email={} with template={}",
                resume.getId(), resume.getUser().getEmail(), template);

        // Notify the user that their resume was downloaded. The transaction
        // is intentionally read-write so the notification persists alongside
        // the download.
        notificationService.createNotification(resume.getUser(), NotificationType.RESUME,
                "Resume downloaded",
                "Your resume was downloaded as a PDF. Good luck with your applications!");

        return new ResumePdfDocument(createPdfContent(resume, template), buildFileName(resume));
    }

    /**
     * Creates a professional, ATS-friendly PDF document from the resume
     * using the visual configuration of the given template.
     * <p>
     * The layout renders the candidate's name, headline, and contact
     * details in a header (centred, or as a coloured band for templates
     * that request it), followed by the sections Professional Summary,
     * Skills, Experience, Projects, Education, Certifications, and
     * Achievements. Empty sections are skipped gracefully and null values
     * are omitted, so the document only ever contains real data.
     * </p>
     *
     * @param resume   the resume entity containing the data to render
     * @param template the PDF layout template to apply
     * @return a byte array containing the PDF document
     */
    private byte[] createPdfContent(final Resume resume, final ResumePdfTemplate template) {
        final PdfTemplateStyle style = template.getStyle();

        // Typography — Helvetica is universally embedded and parseable by ATS
        // tools. Colours are applied per font, as OpenPDF tints text that way.
        final Font nameFont = font(FontFactory.HELVETICA_BOLD, style.nameSize(), style.textColor());
        final Font headlineFont = font(FontFactory.HELVETICA_BOLD, style.bodySize() + 2, style.mutedColor());
        final Font sectionFont = font(FontFactory.HELVETICA_BOLD, style.sectionSize(), style.headingColor());
        final Font entryTitleFont = font(FontFactory.HELVETICA_BOLD, style.bodySize() + 1,
                style.coloredEntryTitles() ? style.headingColor() : style.textColor());
        final Font bodyFont = font(FontFactory.HELVETICA, style.bodySize(), style.textColor());
        final Font dateFont = font(FontFactory.HELVETICA, style.metaSize(), style.mutedColor());
        final Font metaFont = font(FontFactory.HELVETICA, style.metaSize(), style.mutedColor());
        final Font contactFont = font(FontFactory.HELVETICA, style.metaSize(), style.mutedColor());

        final float margin = style.margin();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final Document document = new Document(PAGE_SIZE, margin, margin, margin + 8, margin + 8);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            final User user = resume.getUser();

            // ── Header ──
            if (style.headerLayout() == PdfTemplateStyle.HeaderLayout.BAND) {
                addBandHeader(document, user, resume, style);
            } else if (style.headerLayout() == PdfTemplateStyle.HeaderLayout.LEFT_ALIGNED) {
                addLeftAlignedHeader(document, user, resume, style, nameFont, headlineFont, contactFont);
            } else {
                addCentredHeader(document, user, resume, style, nameFont, headlineFont, contactFont);
            }

            // Fetch related data
            final List<Education> educations = educationRepository.findByResume(resume);
            final List<Experience> experiences = experienceRepository.findByResume(resume);
            final List<Skill> skills = skillRepository.findByResume(resume);
            final List<Project> projects = projectRepository.findByResume(resume);
            final List<Certification> certifications = certificationRepository.findByResume(resume);
            final List<Achievement> achievements = achievementRepository.findByResume(resume);

            // 1. Professional Summary
            if (notBlank(resume.getSummary())) {
                addSectionHeader(document, "Professional Summary", sectionFont, style);
                final Paragraph summary = new Paragraph(resume.getSummary().trim(), bodyFont);
                summary.setLeading(leading(bodyFont, style));
                summary.setSpacingAfter(4);
                document.add(summary);
            }

            // 2. Skills
            if (!skills.isEmpty()) {
                addSectionHeader(document, "Skills", sectionFont, style);
                final String skillsLine = skills.stream()
                        .map(this::formatSkill)
                        .filter(skillText -> !skillText.isEmpty())
                        .collect(Collectors.joining(", "));
                if (!skillsLine.isEmpty()) {
                    final Paragraph skillsParagraph = new Paragraph(skillsLine, bodyFont);
                    skillsParagraph.setLeading(leading(bodyFont, style));
                    if (style.skillsHighlight() && style.highlightFill() != null) {
                        addHighlightBox(document, skillsParagraph, style);
                    } else {
                        skillsParagraph.setSpacingAfter(4);
                        document.add(skillsParagraph);
                    }
                }
            }

            // 3. Experience
            if (!experiences.isEmpty()) {
                addSectionHeader(document, "Experience", sectionFont, style);
                for (final Experience experience : experiences) {
                    final String title = join(EN_DASH, experience.getJobTitle(), experience.getCompanyName());
                    final String meta = join(BULLET, experience.getEmploymentType(), experience.getLocation());
                    addEntry(document, title,
                            formatDateRange(experience.getStartDate(), experience.getEndDate(),
                                    Boolean.TRUE.equals(experience.getCurrentlyWorking())),
                            meta, experience.getDescription(), "",
                            entryTitleFont, dateFont, metaFont, bodyFont, style.lineSpacing());
                }
            }

            // 4. Projects
            if (!projects.isEmpty()) {
                addSectionHeader(document, "Projects", sectionFont, style);
                for (final Project project : projects) {
                    final String links = join("   ",
                            prefixed("GitHub: ", project.getGithubUrl()),
                            prefixed("Live: ", project.getLiveUrl()));
                    addEntry(document, project.getProjectName(),
                            formatDateRange(project.getStartDate(), project.getEndDate(),
                                    Boolean.TRUE.equals(project.getCurrentlyWorking())),
                            project.getTechnologies(), project.getDescription(), links,
                            entryTitleFont, dateFont, metaFont, bodyFont, style.lineSpacing());
                }
            }

            // 5. Education
            if (!educations.isEmpty()) {
                addSectionHeader(document, "Education", sectionFont, style);
                for (final Education education : educations) {
                    final String title = join(EN_DASH, education.getDegree(), education.getFieldOfStudy());
                    final String meta = join(BULLET, education.getInstitutionName(),
                            prefixed("Grade: ", education.getGrade()));
                    addEntry(document, title,
                            formatDateRange(education.getStartDate(), education.getEndDate(),
                                    Boolean.TRUE.equals(education.getCurrentlyStudying())),
                            meta, education.getDescription(), "",
                            entryTitleFont, dateFont, metaFont, bodyFont, style.lineSpacing());
                }
            }

            // 6. Certifications
            if (!certifications.isEmpty()) {
                addSectionHeader(document, "Certifications", sectionFont, style);
                for (final Certification certification : certifications) {
                    final String meta = join(BULLET, certification.getIssuingOrganization());
                    final String details = join("  ", prefixed("Credential ID: ", certification.getCredentialId()),
                            prefixed("Verify: ", certification.getCredentialUrl()));
                    addEntry(document, certification.getCertificationName(),
                            formatDate(certification.getIssueDate()),
                            meta, details, "",
                            entryTitleFont, dateFont, metaFont, bodyFont, style.lineSpacing());
                }
            }

            // 7. Achievements
            if (!achievements.isEmpty()) {
                addSectionHeader(document, "Achievements", sectionFont, style);
                for (final Achievement achievement : achievements) {
                    addEntry(document, achievement.getTitle(),
                            formatDate(achievement.getDateAchieved()),
                            "", achievement.getDescription(), "",
                            entryTitleFont, dateFont, metaFont, bodyFont, style.lineSpacing());
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
     * Renders a centred header with the name, headline, contact line,
     * and a thin rule beneath it.
     *
     * @param document     the PDF document to add the header to
     * @param user         the resume owner
     * @param resume       the resume being rendered
     * @param style        the template's visual configuration
     * @param nameFont     the name font (size/colour from the template)
     * @param headlineFont the headline font
     * @param contactFont  the contact details font
     * @throws DocumentException if an error occurs while adding to the document
     */
    private void addCentredHeader(final Document document, final User user, final Resume resume,
                                  final PdfTemplateStyle style, final Font nameFont,
                                  final Font headlineFont, final Font contactFont)
            throws DocumentException {
        final String name = style.uppercaseName()
                ? fullName(user).toUpperCase(Locale.ROOT)
                : fullName(user);
        final Paragraph nameParagraph = new Paragraph(name, nameFont);
        nameParagraph.setAlignment(Element.ALIGN_CENTER);
        nameParagraph.setSpacingAfter(6);
        document.add(nameParagraph);

        if (notBlank(resume.getHeadline())) {
            final Paragraph headline = new Paragraph(resume.getHeadline().trim(), headlineFont);
            headline.setAlignment(Element.ALIGN_CENTER);
            headline.setSpacingAfter(8);
            document.add(headline);
        }

        if (style.contactColumns()) {
            addContactColumns(document, contactItems(user, resume), contactFont);
        } else {
            final String contact = buildContactLine(user, resume);
            if (!contact.isEmpty()) {
                final Paragraph contactLine = new Paragraph(contact, contactFont);
                contactLine.setAlignment(Element.ALIGN_CENTER);
                contactLine.setSpacingAfter(10);
                document.add(contactLine);
            }
        }

        final LineSeparator separator = new LineSeparator();
        separator.setLineColor(style.ruleColor());
        separator.setLineWidth(style.ruleWidth());
        document.add(new Chunk(separator));
    }

    /**
     * Renders a left-aligned header with the name, headline, and contact
     * line, followed by a thin rule — the relaxed, airy header used by
     * the Minimal template.
     *
     * @param document     the PDF document to add the header to
     * @param user         the resume owner
     * @param resume       the resume being rendered
     * @param style        the template's visual configuration
     * @param nameFont     the name font
     * @param headlineFont the headline font
     * @param contactFont  the contact details font
     * @throws DocumentException if an error occurs while adding to the document
     */
    private void addLeftAlignedHeader(final Document document, final User user, final Resume resume,
                                      final PdfTemplateStyle style, final Font nameFont,
                                      final Font headlineFont, final Font contactFont)
            throws DocumentException {
        final Paragraph nameParagraph = new Paragraph(fullName(user), nameFont);
        nameParagraph.setSpacingAfter(4);
        document.add(nameParagraph);

        if (notBlank(resume.getHeadline())) {
            final Paragraph headline = new Paragraph(resume.getHeadline().trim(), headlineFont);
            headline.setSpacingAfter(6);
            document.add(headline);
        }

        final String contact = buildContactLine(user, resume);
        if (!contact.isEmpty()) {
            final Paragraph contactLine = new Paragraph(contact, contactFont);
            contactLine.setSpacingAfter(10);
            document.add(contactLine);
        }

        final LineSeparator separator = new LineSeparator();
        separator.setLineColor(style.ruleColor());
        separator.setLineWidth(style.ruleWidth());
        document.add(new Chunk(separator));
    }

    /**
     * Renders contact details as a centred two-column grid below the
     * name and headline (used by the Modern Blue template).
     *
     * @param document    the PDF document to add the grid to
     * @param items       the contact items to display
     * @param contactFont the contact details font
     * @throws DocumentException if an error occurs while adding to the document
     */
    private void addContactColumns(final Document document, final List<String> items,
                                   final Font contactFont) throws DocumentException {
        if (items.isEmpty()) {
            return;
        }
        final PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(70);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setSpacingAfter(10);
        for (final String item : items) {
            final PdfPCell cell = new PdfPCell(new Paragraph(item, contactFont));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setPadding(2);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        if (items.size() % 2 != 0) {
            final PdfPCell empty = new PdfPCell(new Paragraph("", contactFont));
            empty.setBorder(PdfPCell.NO_BORDER);
            table.addCell(empty);
        }
        document.add(table);
    }

    /**
     * Renders a full-width coloured band header containing the name,
     * headline, and contact details in the template's band colours
     * (used by the Creative template).
     *
     * @param document the PDF document to add the header to
     * @param user     the resume owner
     * @param resume   the resume being rendered
     * @param style    the template's visual configuration
     * @throws DocumentException if an error occurs while adding to the document
     */
    private void addBandHeader(final Document document, final User user, final Resume resume,
                               final PdfTemplateStyle style) throws DocumentException {
        final PdfPTable band = new PdfPTable(1);
        band.setWidthPercentage(100);
        band.setSpacingAfter(12);
        band.setKeepTogether(true);

        final PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(style.headingColor());
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPadding(16);
        cell.setPaddingBottom(18);

        final Font bandNameFont = font(FontFactory.HELVETICA_BOLD, style.nameSize(), style.bandTextColor());
        final Font bandMutedFont = font(FontFactory.HELVETICA_BOLD, style.bodySize() + 2, style.bandMutedColor());

        final Paragraph name = new Paragraph(fullName(user), bandNameFont);
        name.setAlignment(Element.ALIGN_CENTER);
        name.setSpacingAfter(3);
        cell.addElement(name);

        if (notBlank(resume.getHeadline())) {
            final Paragraph headline = new Paragraph(resume.getHeadline().trim(), bandMutedFont);
            headline.setAlignment(Element.ALIGN_CENTER);
            headline.setSpacingAfter(3);
            cell.addElement(headline);
        }

        final String contact = buildContactLine(user, resume);
        if (!contact.isEmpty()) {
            final Paragraph contactLine = new Paragraph(contact, bandMutedFont);
            contactLine.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(contactLine);
        }

        band.addCell(cell);
        document.add(band);
    }

    /**
     * Builds the download file name for a resume.
     * <p>
     * Follows the {@code <FirstName>_Resume.pdf} convention requested by
     * users, e.g. {@code Divya_Resume.pdf}. Falls back to
     * {@code Resume.pdf} when the user has no first name on file.
     * </p>
     *
     * @param resume the resume being downloaded
     * @return the suggested download file name
     */
    private String buildFileName(final Resume resume) {
        String firstName = resume.getUser().getFirstName();
        if (firstName != null) {
            firstName = firstName.trim().replaceAll("[^\\p{Alnum}_ -]", "");
        }
        final String base = firstName == null || firstName.isEmpty() ? "Resume" : firstName;
        return base + "_Resume.pdf";
    }

    /**
     * Adds an entry row with a bold title, a right-aligned date range,
     * and optional metadata and description paragraphs below it.
     *
     * @param document       the PDF document to add the entry to
     * @param title          the bold entry title (e.g. "Senior Developer – Acme Inc")
     * @param dateRange      the formatted date range, may be empty
     * @param meta           optional metadata line (e.g. employment type, location), may be empty
     * @param description    optional description text, may be empty
     * @param links          optional small links line (e.g. project URLs), may be empty
     * @param entryTitleFont font for the entry title
     * @param dateFont       font for the date range
     * @param metaFont       font for metadata and links
     * @param bodyFont       font for descriptions
     * @param lineSpacing    line-height multiplier for description text
     * @throws DocumentException if an error occurs while adding to the document
     */
    private void addEntry(final Document document, final String title, final String dateRange,
                          final String meta, final String description, final String links,
                          final Font entryTitleFont, final Font dateFont, final Font metaFont,
                          final Font bodyFont, final float lineSpacing) throws DocumentException {
        // The whole entry is a single table so that a page break never
        // strands an entry title away from its description. keepTogether
        // moves the complete entry to the next page when it does not fit
        // on the current one (entries larger than a full page still split).
        final PdfPTable entry = new PdfPTable(2);
        entry.setWidthPercentage(100);
        entry.setWidths(new float[]{72f, 28f});
        entry.setHorizontalAlignment(Element.ALIGN_LEFT);
        entry.setSpacingBefore(8);
        entry.setSpacingAfter(4);
        entry.setKeepTogether(true);

        final PdfPCell titleCell = new PdfPCell(new Paragraph(title, entryTitleFont));
        titleCell.setBorder(PdfPCell.NO_BORDER);
        titleCell.setPadding(0);

        final PdfPCell dateCell = new PdfPCell(new Paragraph(dateRange, dateFont));
        dateCell.setBorder(PdfPCell.NO_BORDER);
        dateCell.setPadding(0);
        dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        entry.addCell(titleCell);
        entry.addCell(dateCell);

        if (notBlank(meta)) {
            entry.addCell(fullWidthCell(paragraph(meta.trim(), metaFont, lineSpacing)));
        }
        if (notBlank(description)) {
            entry.addCell(fullWidthCell(paragraph(description.trim(), bodyFont, lineSpacing)));
        }
        if (notBlank(links)) {
            entry.addCell(fullWidthCell(paragraph(links.trim(), metaFont, lineSpacing)));
        }

        document.add(entry);
    }

    /**
     * Creates a borderless cell spanning both entry columns with a small
     * top padding to separate it from the title row.
     *
     * @param content the cell content
     * @return a full-width, borderless cell
     */
    private PdfPCell fullWidthCell(final Paragraph content) {
        final PdfPCell cell = new PdfPCell(content);
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPadding(0);
        cell.setPaddingTop(3);
        cell.setColspan(2);
        return cell;
    }

    /**
     * Creates a paragraph with the template's line spacing applied.
     *
     * @param text        the paragraph text
     * @param font        the font to use
     * @param lineSpacing the line-height multiplier
     * @return the configured paragraph
     */
    private Paragraph paragraph(final String text, final Font font, final float lineSpacing) {
        final Paragraph paragraph = new Paragraph(text, font);
        paragraph.setLeading(font.getSize() * lineSpacing);
        return paragraph;
    }

    /**
     * Computes the line height for a font under the template's spacing.
     *
     * @param font  the font whose line height to compute
     * @param style the template's visual configuration
     * @return the line height in points
     */
    private float leading(final Font font, final PdfTemplateStyle style) {
        return font.getSize() * style.lineSpacing();
    }

    /**
     * Renders content inside a softly filled box (used to highlight the
     * skills section in the Creative template). The box is a single text
     * cell, so the content still extracts cleanly for ATS parsers.
     *
     * @param document the PDF document to add the box to
     * @param content  the paragraph to place inside the box
     * @param style    the template's visual configuration
     * @throws DocumentException if an error occurs while adding to the document
     */
    private void addHighlightBox(final Document document, final Paragraph content,
                                 final PdfTemplateStyle style) throws DocumentException {
        final PdfPTable box = new PdfPTable(1);
        box.setWidthPercentage(100);
        box.setSpacingAfter(4);
        final PdfPCell cell = new PdfPCell(content);
        cell.setBackgroundColor(style.highlightFill());
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPadding(8);
        cell.setPaddingTop(7);
        box.addCell(cell);
        document.add(box);
    }

    /**
     * Adds a section heading with a divider rule beneath it, both styled
     * by the template.
     *
     * @param document    the PDF document to add the header to
     * @param sectionName the text of the section header
     * @param sectionFont the section heading font (colour from the template)
     * @param style       the template's visual configuration
     * @throws DocumentException if an error occurs while adding to the document
     */
    private void addSectionHeader(final Document document, final String sectionName,
                                  final Font sectionFont, final PdfTemplateStyle style)
            throws DocumentException {
        if (style.headingBar()) {
            // A short accent bar above the heading, used by the modern
            // and creative templates.
            final PdfPTable bar = new PdfPTable(1);
            bar.setTotalWidth(28f);
            bar.setLockedWidth(true);
            bar.setSpacingBefore(style.sectionSpacingBefore());
            bar.setSpacingAfter(4);
            final PdfPCell barCell = new PdfPCell();
            barCell.setBackgroundColor(style.headingColor());
            barCell.setBorder(PdfPCell.NO_BORDER);
            barCell.setFixedHeight(3f);
            bar.addCell(barCell);
            document.add(bar);
        }

        final Paragraph header = new Paragraph(sectionName.toUpperCase(Locale.ROOT), sectionFont);
        header.setSpacingBefore(style.headingBar() ? 4 : style.sectionSpacingBefore());
        header.setSpacingAfter(4);
        document.add(header);

        final LineSeparator separator = new LineSeparator();
        separator.setLineColor(style.ruleColor());
        separator.setLineWidth(style.ruleWidth());
        document.add(new Chunk(separator));
    }

    /**
     * Creates a Helvetica font in the given family, size, and colour.
     *
     * @param family the font family name
     * @param size   the font size
     * @param color  the font colour
     * @return the configured font
     */
    private static Font font(final String family, final float size, final Color color) {
        final Font f = FontFactory.getFont(family, size);
        f.setColor(color);
        return f;
    }

    /**
     * Joins a skill name with its optional proficiency in parentheses,
     * e.g. {@code Java (Advanced)}.
     *
     * @param skill the skill to format
     * @return the formatted skill text
     */
    private String formatSkill(final Skill skill) {
        final String name = skill.getSkillName() == null ? "" : skill.getSkillName().trim();
        if (!notBlank(skill.getProficiency())) {
            return name;
        }
        return name + " (" + skill.getProficiency().trim() + ")";
    }

    /**
     * Builds the contact line from the user's email, phone, and the
     * resume's profile URLs, separated by dots.
     *
     * @param user   the resume owner
     * @param resume the resume whose profile URLs to include
     * @return the formatted contact line, possibly empty
     */
    /**
     * Returns the user's non-blank contact details (email, phone, and
     * profile URLs) as a list.
     *
     * @param user   the resume owner
     * @param resume the resume whose profile URLs to include
     * @return the contact items, in display order
     */
    private List<String> contactItems(final User user, final Resume resume) {
        return Stream.of(
                        user.getEmail(),
                        user.getPhone(),
                        compactUrl(resume.getLinkedinUrl()),
                        compactUrl(resume.getGithubUrl()),
                        compactUrl(resume.getPortfolioUrl()))
                .filter(this::notBlank)
                .map(String::trim)
                .toList();
    }

    /**
     * Builds the contact line from the user's email, phone, and the
     * resume's profile URLs, separated by dots.
     *
     * @param user   the resume owner
     * @param resume the resume whose profile URLs to include
     * @return the formatted contact line, possibly empty
     */
    private String buildContactLine(final User user, final Resume resume) {
        return join("   |   ", contactItems(user, resume).toArray(String[]::new));
    }

    /**
     * Shortens a URL for display by stripping the protocol and
     * {@code www.} prefix, e.g. {@code https://www.github.com/me} →
     * {@code github.com/me}.
     *
     * @param url the URL to compact, may be {@code null}
     * @return the compacted URL, or an empty string when blank
     */
    private String compactUrl(final String url) {
        if (!notBlank(url)) {
            return "";
        }
        return url.trim().replaceFirst("^https?://(www\\.)?", "");
    }

    /**
     * Formats a date range as e.g. {@code Jan 2020 – Present} or
     * {@code Jan 2020 – Mar 2023}. Ongoing or open-ended ranges render
     * {@code Present} as the end.
     *
     * @param start   the start date, may be {@code null}
     * @param end     the end date, may be {@code null}
     * @param ongoing whether the range is currently ongoing
     * @return the formatted range, possibly empty
     */
    private String formatDateRange(final LocalDate start, final LocalDate end, final boolean ongoing) {
        final String startText = formatDate(start);
        if (ongoing || end == null) {
            return startText.isEmpty() ? PRESENT : startText + EN_DASH + PRESENT;
        }
        final String endText = formatDate(end);
        if (startText.isEmpty()) {
            return endText;
        }
        return startText + EN_DASH + endText;
    }

    /**
     * Formats a {@link LocalDate} to a readable month-year string.
     *
     * @param date the date to format, may be {@code null}
     * @return the formatted date string (e.g. "Jan 2024"), or an empty
     *         string when the input was {@code null}
     */
    private String formatDate(final LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * Prefixes a value when it is not blank, used to build optional
     * labelled detail lines.
     *
     * @param prefix the label prefix
     * @param value  the value, may be {@code null} or blank
     * @return {@code prefix + value}, or an empty string when blank
     */
    private String prefixed(final String prefix, final String value) {
        if (!notBlank(value)) {
            return "";
        }
        return prefix + value.trim();
    }

    /**
     * Joins the non-blank parts with the given separator.
     *
     * @param separator the separator between parts
     * @param parts     the candidate parts
     * @return the joined string
     */
    private String join(final String separator, final String... parts) {
        return Stream.of(parts)
                .filter(this::notBlank)
                .map(String::trim)
                .collect(Collectors.joining(separator));
    }

    /**
     * Returns the user's full name, tolerating missing name parts.
     *
     * @param user the user whose name to compose
     * @return the full name
     */
    private String fullName(final User user) {
        return join(" ", user.getFirstName(), user.getLastName());
    }

    /**
     * Whether the value is non-null and not blank.
     *
     * @param value the value to check, may be {@code null}
     * @return {@code true} when the value has visible content
     */
    private boolean notBlank(final String value) {
        return value != null && !value.isBlank();
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
