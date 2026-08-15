package com.devlaunch.service.ai;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deterministic, rule-based resume review provider.
 * <p>
 * Analyses a {@link ResumeContent} snapshot using a transparent set of
 * heuristics. It produces a professional ATS report: weighted category
 * scores that always sum to 100 (the overall ATS score), the overall
 * resume quality score, strengths, weaknesses, missing sections, keyword
 * analysis, a formatting review, an evaluation of the professional summary
 * (with a synthesized improved version), and per-project, per-skill, and
 * per-experience analyses. All keyword matching uses word boundaries to
 * avoid false positives. This provider requires no external configuration
 * and keeps the application fully functional when no LLM API key is
 * configured.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class SampleResumeReviewProvider implements ResumeReviewProvider {

    /** Pattern used to detect quantified achievements (numbers, percentages). */
    private static final Pattern QUANTIFIED_PATTERN =
            Pattern.compile("\\d+\\s*(%|percent|users|clients|customers|projects|tasks|requests|downtime|hours|revenue)");

    /** Action verbs that indicate measurable impact in bullet points. */
    private static final List<String> ACTION_VERBS = List.of(
            "led", "built", "developed", "implemented", "improved", "designed",
            "managed", "delivered", "created", "optimized", "optimised",
            "increased", "reduced", "launched", "architected", "migrated");

    /** Curated technical keywords used for found/missing keyword analysis. */
    private static final List<String> TECH_KEYWORDS = List.of(
            "Java", "Spring Boot", "Hibernate", "JPA", "SQL", "MySQL",
            "PostgreSQL", "MongoDB", "React", "Redux", "TypeScript", "JavaScript",
            "Node.js", "REST API", "REST APIs", "GraphQL", "Git", "GitHub",
            "Docker", "Kubernetes", "AWS", "Azure", "GCP", "CI/CD", "Jenkins",
            "Microservices", "Kafka", "Redis", "JUnit", "Mockito", "Maven",
            "Gradle", "JWT", "OAuth", "Linux", "Python", "Django", "FastAPI",
            "Pandas", "NumPy", "TensorFlow", "Machine Learning", "Agile", "Scrum",
            "Tailwind CSS", "Next.js", "Jest", "Terraform", "Helm", "Prometheus",
            "Serverless", "ETL", "Power BI", "Tableau", "HTML", "CSS", "Angular",
            "Vue");

    /** Precompiled word-boundary patterns for the technical keywords. */
    private static final Map<String, Pattern> KEYWORD_PATTERNS = new LinkedHashMap<>();

    /** Precompiled word-boundary patterns for the action verbs. */
    private static final Map<String, Pattern> ACTION_VERB_PATTERNS = new LinkedHashMap<>();

    /** Role-specific in-demand skill lists, matched by role keyword. */
    private static final Map<String, List<String>> ROLE_SKILLS = new LinkedHashMap<>();

    /** Generic in-demand skills used when no role keyword matches. */
    private static final List<String> DEFAULT_SKILLS = List.of(
            "Java", "Spring Boot", "SQL", "REST APIs", "Git",
            "Docker", "AWS", "CI/CD", "Microservices", "Agile");

    /** Parses a formatted project entry into name / technologies / description. */
    private static final Pattern PROJECT_PATTERN =
            Pattern.compile("^(.*?)\\s*\\[(.*?)\\]\\s*:?\\s*(.*)$", Pattern.DOTALL);

    static {
        for (final String keyword : TECH_KEYWORDS) {
            KEYWORD_PATTERNS.put(keyword, Pattern.compile(
                    "\\b" + Pattern.quote(keyword) + "\\b", Pattern.CASE_INSENSITIVE));
        }
        for (final String verb : ACTION_VERBS) {
            ACTION_VERB_PATTERNS.put(verb, Pattern.compile(
                    "\\b" + Pattern.quote(verb) + "\\b", Pattern.CASE_INSENSITIVE));
        }

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
     * Computes the resume and ATS scores from structural heuristics. The
     * ATS score is the exact sum of the nine weighted category scores and
     * therefore always lands in the 0–100 range. Strengths, weaknesses,
     * missing sections, keyword analysis, formatting review, and the
     * section-level analyses are all derived from the detected content.
     * </p>
     */
    @Override
    public ResumeReviewAnalysis analyze(final ResumeContent content, final String targetRole) {
        final String combinedText = combinedText(content);
        final List<String> roleSkills = resolveRoleSkills(targetRole);

        final List<ResumeReviewAnalysis.CategoryScore> categoryScores =
                computeCategoryScores(content, combinedText, roleSkills);
        final int atsScore = categoryScores.stream().mapToInt(ResumeReviewAnalysis.CategoryScore::score).sum();
        final int resumeScore = computeResumeScore(content, combinedText);

        final List<String> strengths = buildStrengths(content, combinedText);
        final List<String> weaknesses = buildWeaknesses(content, combinedText, roleSkills);
        final List<String> missingSkills = buildMissingSkills(content, roleSkills);
        final List<String> missingSections = buildMissingSections(content);
        final List<String> foundKeywords = buildFoundKeywords(combinedText);
        final List<String> missingKeywords = missingSkills;
        final List<String> keywordSuggestions = buildKeywordSuggestions(content, missingKeywords);
        final List<String> formattingAnalysis = buildFormattingAnalysis(content, combinedText);
        final ResumeReviewAnalysis.SummaryAnalysis summaryAnalysis =
                buildSummaryAnalysis(content);
        final List<ResumeReviewAnalysis.ProjectAnalysis> projectAnalyses =
                buildProjectAnalyses(content, combinedText);
        final ResumeReviewAnalysis.SkillsAnalysis skillsAnalysis =
                buildSkillsAnalysis(content, combinedText);
        final ResumeReviewAnalysis.ExperienceAnalysis experienceAnalysis =
                buildExperienceAnalysis(content, combinedText);
        final List<ResumeReviewAnalysis.Suggestion> suggestions =
                buildSuggestions(content, combinedText, missingSkills);

        return new ResumeReviewAnalysis(
                resumeScore, atsScore, strengths, weaknesses, missingSkills, suggestions,
                categoryScores, missingSections, foundKeywords, missingKeywords,
                keywordSuggestions, formattingAnalysis, summaryAnalysis,
                projectAnalyses, skillsAnalysis, experienceAnalysis);
    }

    /**
     * Computes the nine weighted category scores that make up the ATS score.
     * <p>
     * The weights always sum to 100: Contact Information (10), Professional
     * Summary (10), Skills (20), Projects (15), Experience (20), Education
     * (5), Certifications (5), Formatting (5), and Keyword Relevance (10).
     * </p>
     */
    private List<ResumeReviewAnalysis.CategoryScore> computeCategoryScores(
            final ResumeContent content, final String combinedText,
            final List<String> roleSkills) {
        final List<ResumeReviewAnalysis.CategoryScore> scores = new ArrayList<>();

        // Contact Information (max 10): headline + profile links
        int contact = 0;
        if (hasText(content.headline())) {
            contact += 2;
        }
        if (hasLink(content.linkedinUrl())) {
            contact += 3;
        }
        if (hasLink(content.githubUrl())) {
            contact += 3;
        }
        if (hasLink(content.portfolioUrl())) {
            contact += 2;
        }
        scores.add(category("Contact Information", contact, 10));

        // Professional Summary (max 10): substance, quantification, keywords
        // — scored from the summary text itself, not the whole resume.
        final String summaryText = trimToEmpty(content.summary());
        int summary = 0;
        final int summaryLength = summaryText.length();
        if (summaryLength >= 200) {
            summary += 6;
        } else if (summaryLength >= 100) {
            summary += 4;
        } else if (summaryLength >= 40) {
            summary += 2;
        }
        if (containsQuantified(summaryText)) {
            summary += 2;
        }
        if (foundKeywords(summaryText).size() >= 3) {
            summary += 2;
        }
        scores.add(category("Professional Summary", Math.min(summary, 10), 10));

        // Skills (max 20): breadth, proficiency levels, keyword coverage
        int skills = 0;
        final int skillCount = content.skills().size();
        if (skillCount >= 10) {
            skills += 14;
        } else if (skillCount >= 7) {
            skills += 12;
        } else if (skillCount >= 5) {
            skills += 10;
        } else if (skillCount >= 3) {
            skills += 7;
        } else if (skillCount > 0) {
            skills += 4;
        }
        if (content.skills().stream().anyMatch(skill -> skill != null
                && skill.contains(" ("))) {
            skills += 2;
        }
        if (content.skills().stream().filter(SampleResumeReviewProvider::isTechnical).count() >= 5) {
            skills += 4;
        }
        scores.add(category("Skills", Math.min(skills, 20), 20));

        // Projects (max 15): count, depth, quantification
        int projects = 0;
        final int projectCount = content.projects().size();
        if (projectCount >= 4) {
            projects += 13;
        } else if (projectCount == 3) {
            projects += 12;
        } else if (projectCount == 2) {
            projects += 10;
        } else if (projectCount == 1) {
            projects += 7;
        }
        if (content.projects().stream()
                .anyMatch(project -> parsedDescription(project).length() >= 200)) {
            projects += 1;
        }
        if (content.projects().stream().anyMatch(SampleResumeReviewProvider::containsQuantified)) {
            projects += 1;
        }
        scores.add(category("Projects", Math.min(projects, 15), 15));

        // Experience (max 20): roles, descriptions, impact
        int experience = 0;
        final int experienceCount = content.experiences().size();
        if (experienceCount >= 4) {
            experience += 15;
        } else if (experienceCount == 3) {
            experience += 14;
        } else if (experienceCount == 2) {
            experience += 12;
        } else if (experienceCount == 1) {
            experience += 8;
        }
        if (content.experiences().stream()
                .anyMatch(entry -> entry.contains(": "))) {
            experience += 2;
        }
        if (content.experiences().stream().anyMatch(SampleResumeReviewProvider::containsQuantified)) {
            experience += 2;
        }
        if (containsActionVerb(combinedText)) {
            experience += 1;
        }
        scores.add(category("Experience", Math.min(experience, 20), 20));

        // Education (max 5) and Certifications (max 5)
        scores.add(category("Education", content.educations().isEmpty() ? 0 : 5, 5));
        scores.add(category("Certifications", content.certifications().isEmpty() ? 0 : 5, 5));

        // Formatting (max 5): presence of the structural signals ATS relies on
        int formatting = 0;
        if (hasText(content.summary())) {
            formatting += 1;
        }
        if (!content.experiences().isEmpty()) {
            formatting += 1;
        }
        if (!content.educations().isEmpty()) {
            formatting += 1;
        }
        if (!content.skills().isEmpty()) {
            formatting += 1;
        }
        if (containsQuantified(combinedText) || containsActionVerb(combinedText)) {
            formatting += 1;
        }
        scores.add(category("Formatting", Math.min(formatting, 5), 5));

        // Keyword Relevance (max 10): role-skill alignment
        if (!roleSkills.isEmpty()) {
            final long matched = roleSkills.stream()
                    .filter(skill -> containsKeyword(combinedText, skill))
                    .count();
            final int keywordScore = (int) Math.round(10.0 * matched / roleSkills.size());
            scores.add(category("Keyword Relevance", Math.min(keywordScore, 10), 10));
        } else {
            scores.add(category("Keyword Relevance", 0, 10));
        }

        return List.copyOf(scores);
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
    private List<String> buildWeaknesses(final ResumeContent content, final String combinedText,
                                         final List<String> roleSkills) {
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
        if (!missingFrom(combinedText, roleSkills).isEmpty()) {
            weaknesses.add("Keyword coverage is incomplete for the target role");
        }

        return List.copyOf(weaknesses);
    }

    /**
     * Computes the in-demand skills missing from the resume, matched
     * against a role-specific keyword list when a target role is provided.
     */
    private List<String> buildMissingSkills(final ResumeContent content, final List<String> roleSkills) {
        return missingFrom(combinedText(content), roleSkills);
    }

    /**
     * Detects the standard resume sections that are genuinely absent.
     * <p>
     * Only sections that can actually be verified from the resume content
     * are reported — no speculative findings.
     * </p>
     */
    private List<String> buildMissingSections(final ResumeContent content) {
        final List<String> missing = new ArrayList<>();

        if (content.certifications().isEmpty()) {
            missing.add("Certifications");
        }
        if (content.achievements().isEmpty()) {
            missing.add("Achievements");
        }
        if (content.experiences().isEmpty()) {
            missing.add("Internships / Work Experience");
        }
        if (!hasLink(content.githubUrl())) {
            missing.add("GitHub");
        }
        if (!hasLink(content.linkedinUrl())) {
            missing.add("LinkedIn");
        }
        if (!hasLink(content.portfolioUrl())) {
            missing.add("Portfolio");
        }

        return List.copyOf(missing);
    }

    /**
     * Extracts the technical keywords present in the resume text using
     * word-boundary matching to avoid false positives.
     */
    private List<String> buildFoundKeywords(final String combinedText) {
        return foundKeywords(combinedText);
    }

    /**
     * Builds suggestions for improving keyword coverage based on the
     * keywords that are missing.
     */
    private List<String> buildKeywordSuggestions(final ResumeContent content,
                                                 final List<String> missingKeywords) {
        final List<String> suggestions = new ArrayList<>();

        if (!missingKeywords.isEmpty()) {
            suggestions.add("Add in-demand keywords such as "
                    + String.join(", ", missingKeywords.subList(0, Math.min(5, missingKeywords.size())))
                    + " to your Skills and experience sections so ATS screens match them.");
        }
        if (!content.skills().isEmpty()) {
            suggestions.add("Mirror the exact terminology used in the job description — ATS "
                    + "matching is literal, so prefer the employer's phrasing.");
        }
        if (content.experiences().isEmpty()) {
            suggestions.add("Weave target keywords into project and experience descriptions, "
                    + "not just the Skills list.");
        }
        suggestions.add("Include certifications and tools that map to the target role to "
                + "broaden keyword coverage.");

        return List.copyOf(suggestions);
    }

    /**
     * Builds the formatting review findings focusing on resume quality
     * (section order, heading consistency, contact completeness, length,
     * readability, and bullet consistency).
     */
    private List<String> buildFormattingAnalysis(final ResumeContent content,
                                                 final String combinedText) {
        final List<String> findings = new ArrayList<>();

        // Section order
        final String order = sectionOrder(content);
        if (order.contains("Summary → Experience")) {
            findings.add("Section order is recruiter-friendly ("
                    + order + ").");
        } else if (order.isEmpty()) {
            findings.add("Resume is largely empty — add the standard sections (Summary, "
                    + "Experience, Education, Skills).");
        } else {
            findings.add("Section order: " + order + ".");
        }

        // Heading consistency
        if (!content.skills().isEmpty() && !content.projects().isEmpty()) {
            findings.add("Section headings are consistently used for Skills and Projects.");
        }

        // Contact completeness
        final int contactPoints = (hasLink(content.linkedinUrl()) ? 1 : 0)
                + (hasLink(content.githubUrl()) ? 1 : 0)
                + (hasLink(content.portfolioUrl()) ? 1 : 0);
        if (contactPoints == 3) {
            findings.add("Contact details are complete — LinkedIn, GitHub, and portfolio links present.");
        } else if (contactPoints == 0) {
            findings.add("Contact section is incomplete — no LinkedIn, GitHub, or portfolio links.");
        } else {
            findings.add("Contact section is partial — " + contactPoints + " of 3 profile links present.");
        }

        // Length
        final long wordCount = combinedText.split("\\s+").length;
        if (wordCount < 150) {
            findings.add("Resume is short (~" + wordCount + " words) — expand it to a full page.");
        } else if (wordCount <= 900) {
            findings.add("Resume length is appropriate (~" + wordCount + " words).");
        } else {
            findings.add("Resume is long (~" + wordCount + " words) — consider trimming to 1–2 pages.");
        }

        // Readability
        if (containsQuantified(combinedText)) {
            findings.add("Content is readable and grounded with concrete numbers.");
        } else if (containsActionVerb(combinedText)) {
            findings.add("Content uses action verbs; adding numbers would make it more readable.");
        } else {
            findings.add("Descriptions read as generic statements — lead with action verbs and outcomes.");
        }

        // Bullet consistency
        if (containsActionVerb(combinedText)) {
            findings.add("Bullet points start with varied action verbs.");
        } else {
            findings.add("Most bullet points lack action verbs — start each with an active verb "
                    + "(built, led, improved).");
        }

        return List.copyOf(findings);
    }

    /**
     * Builds the professional summary evaluation, including a synthesized
     * improved version.
     */
    private ResumeReviewAnalysis.SummaryAnalysis buildSummaryAnalysis(
            final ResumeContent content) {
        final String summary = trimToEmpty(content.summary());
        final int length = summary.length();

        int score = 0;
        if (length >= 200) {
            score += 50;
        } else if (length >= 100) {
            score += 35;
        } else if (length >= 40) {
            score += 20;
        } else if (length > 0) {
            score += 10;
        }
        if (containsQuantified(summary)) {
            score += 25;
        }
        if (foundKeywords(summary).size() >= 3) {
            score += 25;
        }

        final List<String> strengths = new ArrayList<>();
        if (length >= 100) {
            strengths.add("Summary is a solid length for a professional profile.");
        } else if (length > 0) {
            strengths.add("A summary is present, which ATS screens reward.");
        }
        if (containsQuantified(summary)) {
            strengths.add("Includes quantified impact.");
        }
        if (strengths.isEmpty()) {
            strengths.add("No professional summary detected.");
        }

        final List<String> suggestions = new ArrayList<>();
        if (length < 100) {
            suggestions.add("Expand to 2–3 sentences covering your role, years of experience, "
                    + "and core strengths.");
        }
        if (!containsQuantified(summary)) {
            suggestions.add("Add one quantified achievement (e.g. \"improved load times by 40%\").");
        }
        if (foundKeywords(summary).size() < 3) {
            suggestions.add("Mention your key technologies so the summary reinforces ATS keywords.");
        }
        if (suggestions.isEmpty()) {
            suggestions.add("Keep the summary fresh as your skills and achievements grow.");
        }

        return new ResumeReviewAnalysis.SummaryAnalysis(
                clamp(score),
                List.copyOf(strengths),
                List.copyOf(suggestions),
                synthesizeImprovedSummary(content));
    }

    /**
     * Synthesises an improved professional summary from the resume's
     * headline, summary, and detected skills.
     */
    private String synthesizeImprovedSummary(final ResumeContent content) {
        final String headline = trimToEmpty(content.headline());
        final String summary = trimToEmpty(content.summary());
        final List<String> skills = foundKeywords(combinedText(content)).stream().limit(6).toList();

        final StringBuilder improved = new StringBuilder();
        improved.append("Results-oriented ");
        improved.append(headline.isEmpty() ? "professional" : headline);
        if (!skills.isEmpty()) {
            improved.append(" with hands-on expertise in ")
                    .append(String.join(", ", skills));
        }
        improved.append(", recognised for delivering measurable impact through clean, "
                + "maintainable work and strong collaboration.");

        if (!summary.isEmpty()) {
            improved.append(' ').append(summary.trim());
        }
        if (!hasText(headline) && !hasText(summary)) {
            return "Results-oriented professional seeking to apply technical expertise and a "
                    + "track record of delivery in a challenging role.";
        }
        return improved.toString().trim();
    }

    /**
     * Builds per-project quality evaluations.
     */
    private List<ResumeReviewAnalysis.ProjectAnalysis> buildProjectAnalyses(
            final ResumeContent content, final String combinedText) {
        if (content.projects().isEmpty()) {
            return List.of();
        }

        final List<ResumeReviewAnalysis.ProjectAnalysis> analyses = new ArrayList<>();
        for (final String project : content.projects()) {
            final String name = parsedName(project);
            final String description = parsedDescription(project);
            final List<String> technologies = parsedTechnologies(project);
            final boolean quantified = containsQuantified(description);
            final List<String> actionVerbs = actionVerbsIn(description);
            final int length = description.length();

            final String descriptionQuality = length >= 200 ? "Detailed"
                    : length >= 100 ? "Good"
                    : length > 0 ? "Brief"
                    : "No description";
            final String technicalDepth = technologies.size() >= 3 && length >= 150 ? "High"
                    : technologies.size() >= 1 && length >= 60 ? "Moderate"
                    : "Low";

            final List<String> suggestions = new ArrayList<>();
            if (!quantified) {
                suggestions.add("Add a measurable outcome (e.g. \"served 10k users\", \"cut latency by 30%\").");
            }
            if (technologies.isEmpty()) {
                suggestions.add("List the technologies and frameworks used.");
            }
            if (length < 150) {
                suggestions.add("Describe your role, the problem solved, and how you built it.");
            }
            if (suggestions.isEmpty()) {
                suggestions.add("Consider linking the live demo or repository for verification.");
            }

            analyses.add(new ResumeReviewAnalysis.ProjectAnalysis(
                    name, descriptionQuality, technologies, quantified, technicalDepth,
                    actionVerbs, quantified, List.copyOf(suggestions)));
        }
        return List.copyOf(analyses);
    }

    /**
     * Builds the skills section evaluation.
     */
    private ResumeReviewAnalysis.SkillsAnalysis buildSkillsAnalysis(
            final ResumeContent content, final String combinedText) {
        final List<String> technical = new ArrayList<>();
        final List<String> soft = new ArrayList<>();
        for (final String skill : content.skills()) {
            final String name = skillName(skill);
            if (isTechnical(name)) {
                technical.add(name);
            } else if (!name.isBlank()) {
                soft.add(name);
            }
        }

        final boolean hasProficiency = content.skills().stream()
                .anyMatch(skill -> skill.contains(" ("));
        final String organization = !content.skills().isEmpty()
                ? (hasProficiency
                        ? "Skills are grouped with proficiency levels — great for readability."
                        : "Skills are listed flat — consider grouping by category (Languages, "
                                + "Frameworks, Tools) or adding proficiency levels.")
                : "No skills section present.";

        return new ResumeReviewAnalysis.SkillsAnalysis(
                List.copyOf(technical),
                List.copyOf(soft),
                organization,
                missingFrom(combinedText, resolveRoleSkills(null)));
    }

    /**
     * Builds the experience section evaluation.
     */
    private ResumeReviewAnalysis.ExperienceAnalysis buildExperienceAnalysis(
            final ResumeContent content, final String combinedText) {
        if (content.experiences().isEmpty()) {
            return new ResumeReviewAnalysis.ExperienceAnalysis(
                    List.of(),
                    "No experience section present.",
                    "No achievements to evaluate.",
                    false,
                    List.of("Add at least one role with responsibilities and measurable outcomes."));
        }

        final List<String> actionVerbs = actionVerbsIn(String.join(" ", content.experiences()));
        final boolean hasDescriptions = content.experiences().stream()
                .anyMatch(entry -> entry.contains(": "));
        final boolean quantified = content.experiences().stream()
                .anyMatch(SampleResumeReviewProvider::containsQuantified);

        final List<String> suggestions = new ArrayList<>();
        if (!quantified) {
            suggestions.add("Quantify at least one achievement per role (percentages, users, revenue).");
        }
        if (!hasDescriptions) {
            suggestions.add("Add responsibility and impact descriptions to each role.");
        }
        if (actionVerbs.isEmpty()) {
            suggestions.add("Start each bullet with a strong action verb.");
        }
        if (suggestions.isEmpty()) {
            suggestions.add("Keep the most recent roles detailed and trim older, less relevant ones.");
        }

        return new ResumeReviewAnalysis.ExperienceAnalysis(
                List.copyOf(actionVerbs),
                hasDescriptions
                        ? "Responsibilities are described for your roles."
                        : "Roles lack descriptions — add responsibility statements.",
                quantified
                        ? "Achievements are highlighted with metrics."
                        : "Focuses on duties — add quantified achievements.",
                quantified,
                List.copyOf(suggestions));
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
    private List<String> resolveRoleSkills(final String targetRole) {
        if (targetRole == null || targetRole.isBlank()) {
            return DEFAULT_SKILLS;
        }
        final String roleLower = targetRole.toLowerCase();
        for (final Map.Entry<String, List<String>> entry : ROLE_SKILLS.entrySet()) {
            if (roleLower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return DEFAULT_SKILLS;
    }

    /**
     * Returns the role-skill list entries that are not present in the text.
     */
    private List<String> missingFrom(final String combinedText, final List<String> roleSkills) {
        return roleSkills.stream()
                .filter(skill -> !containsKeyword(combinedText, skill))
                .limit(8)
                .toList();
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
     * Checks whether the text contains quantified impact indicators such
     * as numbers, percentages, or counts.
     */
    private static boolean containsQuantified(final String text) {
        return QUANTIFIED_PATTERN.matcher(text).find();
    }

    /**
     * Checks whether the text contains any action verbs.
     */
    private boolean containsActionVerb(final String text) {
        return !actionVerbsIn(text).isEmpty();
    }

    /**
     * Returns the action verbs found in the text (word-boundary matched to
     * avoid false positives such as "led" in "handled").
     */
    private List<String> actionVerbsIn(final String text) {
        final String source = text == null ? "" : text;
        return ACTION_VERBS.stream()
                .filter(verb -> ACTION_VERB_PATTERNS.get(verb).matcher(source).find())
                .toList();
    }

    /**
     * Returns the technical keywords present in the text.
     */
    private List<String> foundKeywords(final String text) {
        return TECH_KEYWORDS.stream()
                .filter(keyword -> containsKeyword(text, keyword))
                .toList();
    }

    /**
     * Word-boundary, case-insensitive keyword match that avoids false
     * positives such as "Java" matching "JavaScript" or "led" matching
     * "handled". Uses the precompiled patterns.
     */
    private boolean containsKeyword(final String text, final String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return false;
        }
        // Role-skill lists contain phrases not in TECH_KEYWORDS (e.g.
        // "OAuth 2.0", "GitHub Actions") — compile those on demand.
        final Pattern pattern = KEYWORD_PATTERNS.containsKey(keyword)
                ? KEYWORD_PATTERNS.get(keyword)
                : Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b",
                        Pattern.CASE_INSENSITIVE);
        return pattern.matcher(text).find();
    }

    /**
     * Checks whether a skill name is a known technical keyword.
     */
    private static boolean isTechnical(final String skillName) {
        final String name = skillName(skillName);
        return TECH_KEYWORDS.stream()
                .anyMatch(keyword -> keyword.equalsIgnoreCase(name));
    }

    /**
     * Strips any trailing proficiency annotation (e.g. "Java (Advanced)")
     * from a skill entry to recover the plain skill name.
     */
    private static String skillName(final String skill) {
        if (skill == null) {
            return "";
        }
        final int separator = skill.indexOf(" (");
        return separator > 0 ? skill.substring(0, separator).trim() : skill.trim();
    }

    /**
     * Parses the project name from a formatted project entry.
     */
    private String parsedName(final String project) {
        final Matcher matcher = PROJECT_PATTERN.matcher(project);
        if (matcher.matches()) {
            return matcher.group(1).trim();
        }
        return project == null ? "" : project.trim();
    }

    /**
     * Parses the technologies list from a formatted project entry.
     */
    private List<String> parsedTechnologies(final String project) {
        final Matcher matcher = PROJECT_PATTERN.matcher(project == null ? "" : project);
        if (!matcher.matches()) {
            return List.of();
        }
        final String technologies = matcher.group(2).trim();
        if (technologies.isEmpty()) {
            return List.of();
        }
        return List.of(technologies.split("\\s*,\\s*"));
    }

    /**
     * Parses the description from a formatted project entry.
     */
    private String parsedDescription(final String project) {
        final Matcher matcher = PROJECT_PATTERN.matcher(project == null ? "" : project);
        if (!matcher.matches()) {
            return "";
        }
        return matcher.group(3).trim();
    }

    /**
     * Summarises the order in which the standard sections appear.
     */
    private String sectionOrder(final ResumeContent content) {
        final List<String> order = new ArrayList<>();
        if (hasText(content.summary())) {
            order.add("Summary");
        }
        if (!content.experiences().isEmpty()) {
            order.add("Experience");
        }
        if (!content.projects().isEmpty()) {
            order.add("Projects");
        }
        if (!content.educations().isEmpty()) {
            order.add("Education");
        }
        if (!content.skills().isEmpty()) {
            order.add("Skills");
        }
        if (!content.certifications().isEmpty()) {
            order.add("Certifications");
        }
        return String.join(" → ", order);
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
     * Checks whether the given text is present and non-blank.
     */
    private boolean hasText(final String value) {
        return value != null && !value.isBlank();
    }

    /**
     * Trims the value to an empty string when null.
     */
    private String trimToEmpty(final String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Builds a category score entry.
     */
    private ResumeReviewAnalysis.CategoryScore category(
            final String name, final int score, final int maxScore) {
        return new ResumeReviewAnalysis.CategoryScore(name, clamp(score), maxScore);
    }

    /**
     * Clamps a score into the valid 0–100 range.
     */
    private int clamp(final int score) {
        return Math.max(0, Math.min(100, score));
    }

}
