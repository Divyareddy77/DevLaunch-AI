package com.devlaunch.service.ai;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Deterministic, rule-based resume review provider.
 * <p>
 * Analyses a {@link ResumeContent} snapshot using a transparent set of
 * heuristics: scoring rewards the presence and quality of each standard
 * resume section, strengths and weaknesses are derived from the detected
 * gaps, missing skills are matched against a curated role-specific
 * keyword list, and suggestions are generated from the identified
 * weaknesses. This provider requires no external configuration and keeps
 * the application fully functional when no LLM API key is configured.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class SampleResumeReviewProvider implements ResumeReviewProvider {

    /** Pattern used to detect quantified achievements (numbers, percentages). */
    private static final Pattern QUANTIFIED_PATTERN =
            Pattern.compile("\\d+\\s*(%|percent|users|clients|customers|projects|tasks|requests|downtime)");

    /** Action verbs that indicate measurable impact in bullet points. */
    private static final List<String> ACTION_VERBS = List.of(
            "led", "built", "developed", "implemented", "improved", "designed",
            "managed", "delivered", "created", "optimized", "optimised",
            "increased", "reduced", "launched", "architected", "migrated");

    /** Role-specific in-demand skill lists, matched by role keyword. */
    private static final Map<String, List<String>> ROLE_SKILLS = new LinkedHashMap<>();

    /** Generic in-demand skills used when no role keyword matches. */
    private static final List<String> DEFAULT_SKILLS = List.of(
            "Java", "Spring Boot", "SQL", "REST APIs", "Git",
            "Docker", "AWS", "CI/CD", "Microservices", "Agile");

    static {
        ROLE_SKILLS.put("java", List.of(
                "Spring Boot", "Spring Data JPA", "Hibernate", "REST APIs",
                "Microservices", "Docker", "Kafka", "AWS", "CI/CD", "JUnit"));
        ROLE_SKILLS.put("fullstack", List.of(
                "React", "TypeScript", "Node.js", "REST APIs", "SQL",
                "Docker", "AWS", "CI/CD", "GraphQL", "Agile"));
        ROLE_SKILLS.put("frontend", List.of(
                "React", "TypeScript", "Redux", "Tailwind CSS", "Next.js",
                "REST APIs", "GraphQL", "Jest", "Responsive Design"));
        ROLE_SKILLS.put("backend", List.of(
                "Java", "Spring Boot", "SQL", "REST APIs", "Microservices",
                "Docker", "AWS", "CI/CD", "OAuth 2.0", "JUnit"));
        ROLE_SKILLS.put("devops", List.of(
                "Docker", "Kubernetes", "AWS", "CI/CD", "Terraform",
                "Linux", "Prometheus", "GitHub Actions", "Helm"));
        ROLE_SKILLS.put("cloud", List.of(
                "AWS", "Azure", "Docker", "Kubernetes", "Terraform",
                "CI/CD", "Linux", "IAM", "Serverless"));
        ROLE_SKILLS.put("python", List.of(
                "Python", "Django", "FastAPI", "Pandas", "NumPy",
                "REST APIs", "Docker", "AWS", "Pytest"));
        ROLE_SKILLS.put("data", List.of(
                "SQL", "PostgreSQL", "MySQL", "MongoDB", "Data Modeling",
                "ETL", "Power BI", "Python", "Pandas"));
        ROLE_SKILLS.put("sql", List.of(
                "SQL", "PostgreSQL", "MySQL", "Indexing", "Stored Procedures",
                "Data Modeling", "MongoDB", "ETL"));
    }

    /**
     * The deterministic provider is always available.
     *
     * @return {@code true}
     */
    @Override
    public boolean isConfigured() {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Computes the resume and ATS scores from structural heuristics,
     * derives strengths and weaknesses from the detected content, matches
     * missing in-demand skills against the target role (or a generic
     * developer list), and builds suggestions from the identified gaps.
     * </p>
     */
    @Override
    public ResumeReviewAnalysis analyze(final ResumeContent content, final String targetRole) {
        final String combinedText = combinedText(content);

        final int resumeScore = computeResumeScore(content, combinedText);
        final int atsScore = computeAtsScore(content, combinedText, targetRole);

        final List<String> strengths = buildStrengths(content, combinedText);
        final List<String> weaknesses = buildWeaknesses(content, combinedText);
        final List<String> missingSkills = buildMissingSkills(content, targetRole);
        final List<ResumeReviewAnalysis.Suggestion> suggestions =
                buildSuggestions(content, combinedText, missingSkills);

        return new ResumeReviewAnalysis(
                resumeScore, atsScore, strengths, weaknesses, missingSkills, suggestions);
    }

    /**
     * Computes the overall resume quality score (0–100).
     * <p>
     * A base score is awarded for the core resume fields, then points are
     * added for each populated, well-developed section.
     * </p>
     */
    private int computeResumeScore(final ResumeContent content, final String combinedText) {
        int score = 20;

        final String summary = trimToEmpty(content.summary());
        if (!summary.isEmpty()) {
            if (summary.length() >= 300) {
                score += 15;
            } else if (summary.length() >= 150) {
                score += 10;
            } else {
                score += 5;
            }
        }

        final int experienceCount = content.experiences().size();
        if (experienceCount > 0) {
            score += 10;
            if (containsQuantified(combinedText)) {
                score += 5;
            }
        }

        final int skillCount = content.skills().size();
        if (skillCount >= 5) {
            score += 10;
        } else if (skillCount > 0) {
            score += 5;
        }

        if (!content.educations().isEmpty()) {
            score += 8;
        }
        if (!content.projects().isEmpty()) {
            score += 7;
        }
        if (!content.certifications().isEmpty()) {
            score += 5;
        }
        if (!content.achievements().isEmpty()) {
            score += 5;
        }
        if (hasProfileLink(content)) {
            score += 5;
        }

        return clamp(score);
    }

    /**
     * Computes the Applicant Tracking System compatibility score (0–100).
     * <p>
     * Rewards the structural signals ATS software relies on: populated
     * standard sections, profile links, action verbs, quantified
     * achievements, and keyword alignment with the target role.
     * </p>
     */
    private int computeAtsScore(final ResumeContent content, final String combinedText,
                                final String targetRole) {
        int score = 15;

        if (hasLink(content.linkedinUrl())) {
            score += 10;
        }
        if (hasLink(content.githubUrl()) || hasLink(content.portfolioUrl())) {
            score += 10;
        }
        if (!content.skills().isEmpty()) {
            score += 10;
        }
        if (!content.experiences().isEmpty()) {
            score += 10;
        }
        if (containsQuantified(combinedText)) {
            score += 10;
        }
        if (containsActionVerb(combinedText)) {
            score += 10;
        }
        if (!content.educations().isEmpty()) {
            score += 10;
        }
        if (trimToEmpty(content.summary()).length() >= 100) {
            score += 10;
        }

        // Keyword alignment bonus when a target role is provided
        if (targetRole != null && !targetRole.isBlank()) {
            final String roleLower = targetRole.toLowerCase();
            final List<String> roleSkills = resolveRoleSkills(roleLower);
            final long matched = roleSkills.stream()
                    .filter(skill -> combinedText.contains(skill.toLowerCase()))
                    .count();
            if (!roleSkills.isEmpty()) {
                score += Math.min(15, (int) (15.0 * matched / roleSkills.size()));
            }
        }

        return clamp(score);
    }

    /**
     * Derives the strengths list from the populated sections of the resume.
     */
    private List<String> buildStrengths(final ResumeContent content, final String combinedText) {
        final List<String> strengths = new ArrayList<>();

        if (trimToEmpty(content.headline()).length() >= 10 && !trimToEmpty(content.summary()).isEmpty()) {
            strengths.add("Clear professional headline supported by a written summary");
        }
        if (!content.experiences().isEmpty()) {
            strengths.add("Well-structured experience section with "
                    + content.experiences().size() + " role(s)");
        }
        if (content.skills().size() >= 5) {
            strengths.add("Comprehensive skills section with "
                    + content.skills().size() + " skills");
        } else if (!content.skills().isEmpty()) {
            strengths.add("Skills section present with "
                    + content.skills().size() + " skill(s)");
        }
        if (containsQuantified(combinedText)) {
            strengths.add("Achievements are quantified with measurable impact");
        }
        if (!content.projects().isEmpty()) {
            strengths.add("Hands-on project work showcased");
        }
        if (!content.certifications().isEmpty()) {
            strengths.add("Certifications add professional credibility");
        }
        if (hasProfileLink(content)) {
            strengths.add("Profile links (LinkedIn/GitHub) improve discoverability");
        }

        if (strengths.isEmpty()) {
            strengths.add("Resume structure is in place — populate the sections to unlock strengths");
        }
        return List.copyOf(strengths);
    }

    /**
     * Derives the weaknesses list from the missing or underdeveloped
     * sections of the resume.
     */
    private List<String> buildWeaknesses(final ResumeContent content, final String combinedText) {
        final List<String> weaknesses = new ArrayList<>();

        if (content.experiences().isEmpty()) {
            weaknesses.add("No work experience listed — recruiters expect at least one role");
        }
        if (content.skills().isEmpty()) {
            weaknesses.add("No dedicated skills section — ATS systems screen for skill keywords");
        } else if (content.skills().size() < 5) {
            weaknesses.add("Skills section is thin — aim for at least 5 relevant skills");
        }
        if (trimToEmpty(content.summary()).length() < 100) {
            weaknesses.add("Professional summary is too brief — expand it to 2–3 sentences");
        }
        if (!containsQuantified(combinedText)) {
            weaknesses.add("Lacks quantified achievements — add numbers and percentages");
        }
        if (content.projects().isEmpty()) {
            weaknesses.add("No projects showcased");
        }
        if (content.certifications().isEmpty()) {
            weaknesses.add("No certifications listed");
        }
        if (!hasProfileLink(content)) {
            weaknesses.add("Missing LinkedIn/GitHub profile links");
        }

        return List.copyOf(weaknesses);
    }

    /**
     * Computes the in-demand skills missing from the resume, matched
     * against a role-specific keyword list when a target role is provided.
     */
    private List<String> buildMissingSkills(final ResumeContent content, final String targetRole) {
        final List<String> roleSkills = targetRole != null && !targetRole.isBlank()
                ? resolveRoleSkills(targetRole.toLowerCase())
                : DEFAULT_SKILLS;

        final String combinedText = combinedText(content).toLowerCase();

        return roleSkills.stream()
                .filter(skill -> !combinedText.contains(skill.toLowerCase()))
                .limit(8)
                .toList();
    }

    /**
     * Builds actionable suggestions from the identified weaknesses and
     * missing skills, each tied to a resume section with a priority.
     */
    private List<ResumeReviewAnalysis.Suggestion> buildSuggestions(
            final ResumeContent content, final String combinedText,
            final List<String> missingSkills) {
        final List<ResumeReviewAnalysis.Suggestion> suggestions = new ArrayList<>();

        if (!missingSkills.isEmpty()) {
            suggestions.add(new ResumeReviewAnalysis.Suggestion(
                    "Keywords",
                    "Add in-demand skills such as " + String.join(", ", missingSkills)
                            + " to improve ATS keyword matching"
                            + (content.skills().isEmpty() ? " and add a dedicated Skills section" : ""),
                    "high"));
        }
        if (content.experiences().isEmpty()) {
            suggestions.add(new ResumeReviewAnalysis.Suggestion(
                    "Experience",
                    "Add a professional experience section with at least one role, "
                            + "including responsibilities and measurable outcomes",
                    "high"));
        } else if (!containsQuantified(combinedText)) {
            suggestions.add(new ResumeReviewAnalysis.Suggestion(
                    "Experience",
                    "Quantify your achievements with metrics such as percentages, "
                            + "users served, or time saved",
                    "high"));
        }
        if (content.skills().isEmpty() || content.skills().size() < 5) {
            suggestions.add(new ResumeReviewAnalysis.Suggestion(
                    "Skills",
                    "Build out the Skills section with 5–10 relevant technologies, "
                            + "frameworks, and tools",
                    "high"));
        }
        if (trimToEmpty(content.summary()).length() < 100) {
            suggestions.add(new ResumeReviewAnalysis.Suggestion(
                    "Summary",
                    "Expand your professional summary to 2–3 sentences that state "
                            + "your role, years of experience, and key strengths",
                    "medium"));
        }
        if (content.projects().isEmpty()) {
            suggestions.add(new ResumeReviewAnalysis.Suggestion(
                    "Projects",
                    "Include 2–3 projects with the technologies used and the impact achieved",
                    "medium"));
        }
        if (!hasProfileLink(content)) {
            suggestions.add(new ResumeReviewAnalysis.Suggestion(
                    "Contact",
                    "Add LinkedIn and GitHub profile links so recruiters can verify your work",
                    "medium"));
        }
        if (content.certifications().isEmpty()) {
            suggestions.add(new ResumeReviewAnalysis.Suggestion(
                    "Certifications",
                    "Consider adding industry certifications relevant to your target role",
                    "low"));
        }

        return List.copyOf(suggestions);
    }

    /**
     * Resolves the skill list matching the provided role text, falling
     * back to the generic developer list when no role keyword matches.
     */
    private List<String> resolveRoleSkills(final String roleLower) {
        for (final Map.Entry<String, List<String>> entry : ROLE_SKILLS.entrySet()) {
            if (roleLower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return DEFAULT_SKILLS;
    }

    /**
     * Concatenates all resume text into a single lower-case searchable
     * string used for keyword and heuristic checks.
     */
    private String combinedText(final ResumeContent content) {
        final StringBuilder builder = new StringBuilder();

        appendIfPresent(builder, content.headline());
        appendIfPresent(builder, content.summary());
        appendIfPresent(builder, content.linkedinUrl());
        appendIfPresent(builder, content.githubUrl());
        appendIfPresent(builder, content.portfolioUrl());
        content.experiences().forEach(entry -> appendIfPresent(builder, entry));
        content.educations().forEach(entry -> appendIfPresent(builder, entry));
        content.skills().forEach(entry -> appendIfPresent(builder, entry));
        content.projects().forEach(entry -> appendIfPresent(builder, entry));
        content.certifications().forEach(entry -> appendIfPresent(builder, entry));
        content.achievements().forEach(entry -> appendIfPresent(builder, entry));

        return builder.toString().toLowerCase();
    }

    /**
     * Appends a non-blank value to the builder followed by a space.
     */
    private void appendIfPresent(final StringBuilder builder, final String value) {
        if (value != null && !value.isBlank()) {
            builder.append(value).append(' ');
        }
    }

    /**
     * Checks whether the combined resume text contains quantified impact
     * indicators such as numbers, percentages, or counts.
     */
    private boolean containsQuantified(final String combinedText) {
        return QUANTIFIED_PATTERN.matcher(combinedText).find();
    }

    /**
     * Checks whether the combined resume text contains any action verbs.
     */
    private boolean containsActionVerb(final String combinedText) {
        return ACTION_VERBS.stream().anyMatch(combinedText::contains);
    }

    /**
     * Checks whether any profile link is present on the resume.
     */
    private boolean hasProfileLink(final ResumeContent content) {
        return hasLink(content.linkedinUrl())
                || hasLink(content.githubUrl())
                || hasLink(content.portfolioUrl());
    }

    /**
     * Checks whether the given URL is present and non-blank.
     */
    private boolean hasLink(final String url) {
        return url != null && !url.isBlank();
    }

    /**
     * Trims the value to an empty string when null.
     */
    private String trimToEmpty(final String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Clamps a score into the valid 0–100 range.
     */
    private int clamp(final int score) {
        return Math.max(0, Math.min(100, score));
    }

}
