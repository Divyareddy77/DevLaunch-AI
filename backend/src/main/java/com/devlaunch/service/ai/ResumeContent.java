package com.devlaunch.service.ai;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Structured snapshot of a resume's content used as the input for
 * AI-powered resume analysis.
 * <p>
 * Encapsulates the core professional fields and all sub-sections
 * (education, experience, skills, projects, certifications, and
 * achievements) assembled from the resume's related entities.
 * Providers may consume the structured fields directly for
 * heuristic analysis or use {@link #toPlainText()} to produce a
 * readable text representation for an LLM prompt.
 * </p>
 *
 * @author DevLaunch
 */
public record ResumeContent(
        String headline,
        String summary,
        String linkedinUrl,
        String githubUrl,
        String portfolioUrl,
        List<String> experiences,
        List<String> educations,
        List<String> skills,
        List<String> projects,
        List<String> certifications,
        List<String> achievements) {

    /**
     * Renders the resume content as a plain-text document suitable
     * for inclusion in an LLM prompt.
     * <p>
     * Only non-empty sections are rendered, and each entry appears on
     * its own line prefixed with a bullet. Profile links are grouped
     * under a single LINKS section.
     * </p>
     *
     * @return the plain-text representation of the resume
     */
    public String toPlainText() {
        final StringBuilder builder = new StringBuilder();

        if (headline != null && !headline.isBlank()) {
            builder.append("HEADLINE: ").append(headline).append('\n');
        }
        if (summary != null && !summary.isBlank()) {
            builder.append("SUMMARY: ").append(summary).append('\n');
        }

        final String links = buildLinks();
        if (!links.isEmpty()) {
            builder.append("LINKS: ").append(links).append('\n');
        }

        appendSection(builder, "EXPERIENCE", experiences);
        appendSection(builder, "EDUCATION", educations);
        appendSection(builder, "SKILLS", skills);
        appendSection(builder, "PROJECTS", projects);
        appendSection(builder, "CERTIFICATIONS", certifications);
        appendSection(builder, "ACHIEVEMENTS", achievements);

        return builder.toString().trim();
    }

    /**
     * Joins the profile URLs into a single comma-separated string.
     *
     * @return the profile links, or an empty string if none are present
     */
    private String buildLinks() {
        return List.of(linkedinUrl, githubUrl, portfolioUrl).stream()
                .filter(url -> url != null && !url.isBlank())
                .collect(Collectors.joining(", "));
    }

    /**
     * Appends a named section of entries to the text builder.
     * <p>
     * The section is skipped entirely when the entry list is empty.
     * </p>
     *
     * @param builder the text builder to append to
     * @param name    the section header text
     * @param entries the section entries, each rendered on its own line
     */
    private void appendSection(final StringBuilder builder, final String name,
                               final List<String> entries) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        builder.append(name).append(':').append('\n');
        for (final String entry : entries) {
            if (entry != null && !entry.isBlank()) {
                builder.append("- ").append(entry.trim()).append('\n');
            }
        }
    }

}
