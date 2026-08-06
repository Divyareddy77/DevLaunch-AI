package com.devlaunch.service.impl;

import com.devlaunch.service.ai.ResumeContent;
import com.devlaunch.service.ai.ResumeReviewAnalysis;
import com.devlaunch.service.ai.SampleResumeReviewProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the deterministic ATS resume report produced by
 * {@link SampleResumeReviewProvider}.
 * <p>
 * Verifies the professional ATS report invariants: category scores always
 * sum to the overall ATS score (which is bounded by 100), missing sections
 * and keywords are reported accurately, and every section-level analysis
 * is populated from the resume content.
 * </p>
 *
 * @author DevLaunch
 */
class SampleResumeReviewProviderTest {

    private final SampleResumeReviewProvider provider = new SampleResumeReviewProvider();

    private ResumeContent fullResume() {
        return new ResumeContent(
                "Senior Java Backend Developer",
                "Backend developer with 5 years of experience building REST APIs and "
                        + "microservices with Java and Spring Boot. Improved API response times "
                        + "by 40% and led a team of 4 engineers.",
                "https://linkedin.com/in/devlaunch",
                "https://github.com/devlaunch",
                "https://devlaunch.dev",
                List.of(
                        "Software Engineer at Acme (Jan 2022 - Present): Built REST APIs "
                                + "serving 50k users, cut deployment time by 30%",
                        "Junior Developer at Beta (Jun 2020 - Dec 2021): Implemented CI/CD "
                                + "pipelines and reduced downtime"),
                List.of(
                        "Bachelor of Technology in Computer Science at Example University "
                                + "(2016 - 2020)"),
                List.of(
                        "Java (Advanced)", "Spring Boot (Advanced)", "SQL", "Docker",
                        "AWS", "React", "Git", "JUnit", "Microservices", "REST APIs"),
                List.of(
                        "Payment Gateway [Spring Boot, Stripe, JWT]: Built a payment gateway "
                                + "processing 10k transactions daily, reducing checkout failures by 25%",
                        "Inventory Tracker [React, Node.js, SQL]: Developed a real-time "
                                + "inventory dashboard used by 200 warehouse staff"),
                List.of("AWS Certified Solutions Architect — Amazon Web Services"),
                List.of("Winner of Company Hackathon: Led a team to build a monitoring "
                        + "dashboard in 48 hours"));
    }

    @Test
    @DisplayName("category scores always sum to the overall ATS score within 0-100")
    void categoryScoresSumToAtsScore() {
        final ResumeReviewAnalysis analysis = provider.analyze(fullResume(), "Java Developer");

        final int categoryTotal = analysis.categoryScores().stream()
                .mapToInt(ResumeReviewAnalysis.CategoryScore::score)
                .sum();

        assertEquals(analysis.atsScore(), categoryTotal);
        assertTrue(analysis.atsScore() >= 0 && analysis.atsScore() <= 100,
                "ATS score must be within 0-100 but was " + analysis.atsScore());
        assertEquals(100, analysis.categoryScores().stream()
                .mapToInt(ResumeReviewAnalysis.CategoryScore::maxScore)
                .sum(), "Category max scores must total 100");
    }

    @Test
    @DisplayName("missing sections only report genuinely absent content")
    void missingSectionsAreAccurate() {
        final ResumeReviewAnalysis analysis = provider.analyze(fullResume(), "Java Developer");

        // The full resume has certifications, achievements, links, and experience.
        assertTrue(analysis.missingSections().isEmpty(),
                "No sections should be missing: " + analysis.missingSections());
        assertFalse(analysis.missingSections().contains("LinkedIn"));
    }

    @Test
    @DisplayName("missing sections are reported for an empty resume")
    void emptyResumeReportsMissingSections() {
        final ResumeContent empty = new ResumeContent(
                null, "", null, null, null,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of());

        final ResumeReviewAnalysis analysis = provider.analyze(empty, null);

        assertTrue(analysis.missingSections().contains("Certifications"));
        assertTrue(analysis.missingSections().contains("Achievements"));
        assertTrue(analysis.missingSections().contains("GitHub"));
        assertTrue(analysis.missingSections().contains("LinkedIn"));
        assertTrue(analysis.missingSections().contains("Portfolio"));
        assertEquals(0, analysis.atsScore(), "An empty resume should score 0");
    }

    @Test
    @DisplayName("keyword analysis finds present keywords and role-specific gaps")
    void keywordAnalysisDetectsFoundAndMissing() {
        final ResumeReviewAnalysis analysis = provider.analyze(fullResume(), "Java Developer");

        assertTrue(analysis.foundKeywords().contains("Java"));
        assertTrue(analysis.foundKeywords().contains("Spring Boot"));
        assertTrue(analysis.foundKeywords().contains("Docker"));
        // False-positive guard: "Java" must not match "JavaScript".
        assertFalse(analysis.foundKeywords().contains("JavaScript"));
    }

    @Test
    @DisplayName("summary analysis produces a score and an improved version")
    void summaryAnalysisIsPopulated() {
        final ResumeReviewAnalysis analysis = provider.analyze(fullResume(), "Java Developer");

        final ResumeReviewAnalysis.SummaryAnalysis summary = analysis.summaryAnalysis();
        assertTrue(summary.score() > 0, "Summary score should be positive");
        assertNotNull(summary.improvedSummary());
        assertFalse(summary.improvedSummary().isBlank());
        assertTrue(summary.improvedSummary().contains("Java"),
                "Improved summary should weave in detected skills");
    }

    @Test
    @DisplayName("project and experience analyses are populated per entry")
    void sectionAnalysesArePopulated() {
        final ResumeReviewAnalysis analysis = provider.analyze(fullResume(), "Java Developer");

        assertEquals(2, analysis.projectAnalyses().size());
        final ResumeReviewAnalysis.ProjectAnalysis project = analysis.projectAnalyses().get(0);
        assertEquals("Payment Gateway", project.projectName());
        assertTrue(project.technologiesMentioned().contains("Spring Boot"));
        assertTrue(project.measurableOutcomes());
        assertFalse(project.suggestions().isEmpty());

        final ResumeReviewAnalysis.ExperienceAnalysis experience = analysis.experienceAnalysis();
        assertTrue(experience.quantifiedImpact());
        assertFalse(experience.actionVerbs().isEmpty());

        final ResumeReviewAnalysis.SkillsAnalysis skills = analysis.skillsAnalysis();
        assertTrue(skills.technicalSkills().contains("Java"));
        assertFalse(skills.organization().isBlank());
    }

}
