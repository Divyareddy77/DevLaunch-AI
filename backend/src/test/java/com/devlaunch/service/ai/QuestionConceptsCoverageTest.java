package com.devlaunch.service.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the coupling between the SQL-seeded question bank and the
 * curated question concepts in {@link SampleMockInterviewProvider}.
 * <p>
 * The two-stage question-relevance scorer relies on one curated concept
 * list per bank question. Because the bank lives in {@code data.sql} and
 * the concepts live in Java, this test parses the seed file and verifies
 * the mapping stays exactly one-to-one — every seeded question has a
 * curated list, every curated key has a seeded question, and every
 * concept is a matchable lowercase word or phrase.
 * </p>
 *
 * @author DevLaunch
 */
class QuestionConceptsCoverageTest {

    /** Matches a seed row: ('CATEGORY', 'question', 'DIFFICULTY', ...). */
    private static final Pattern SEED_ROW = Pattern.compile(
            "\\('(?:HR|JAVA|SPRING_BOOT|SQL|REACT)', '([^']+)', '(?:EASY|MEDIUM|HARD)'");

    /** Concepts must be lowercase alphanumeric words or space-separated phrases. */
    private static final Pattern MATCHABLE_CONCEPT = Pattern.compile("[a-z0-9]+( [a-z0-9]+)*");

    @Test
    @DisplayName("every seeded question has a curated concept list and vice versa")
    void curatedConceptsCoverTheWholeSeed() throws Exception {
        final Set<String> seeded = readSeededQuestions();
        final Map<String, List<String>> concepts = readCuratedConcepts();

        assertFalse(seeded.isEmpty(), "data.sql should contain seeded questions");

        final Set<String> missing = new HashSet<>();
        for (final String question : seeded) {
            if (!concepts.containsKey(question)) {
                missing.add(question);
            }
        }
        assertTrue(missing.isEmpty(), "questions missing curated concepts: " + missing);

        final Set<String> dead = new HashSet<>();
        for (final Map.Entry<String, List<String>> entry : concepts.entrySet()) {
            if (!seeded.contains(entry.getKey())) {
                dead.add(entry.getKey());
            }
            for (final String concept : entry.getValue()) {
                assertTrue(MATCHABLE_CONCEPT.matcher(concept).matches(),
                        "concept '" + concept + "' for '" + entry.getKey()
                                + "' is not a matchable lowercase word or phrase");
            }
        }
        assertTrue(dead.isEmpty(), "curated keys without a seeded question: " + dead);

        assertEquals(seeded.size(), concepts.size(),
                "the curated map and the seeded bank must stay one-to-one");
    }

    /**
     * Reads the question texts from the SQL seed file, normalised the same
     * way the scorer normalises question text.
     */
    private Set<String> readSeededQuestions() throws IOException {
        final String sql = new String(
                new ClassPathResource("data.sql").getInputStream().readAllBytes(),
                StandardCharsets.UTF_8);
        final Matcher matcher = SEED_ROW.matcher(sql);
        final Set<String> questions = new HashSet<>();
        while (matcher.find()) {
            questions.add(normalize(matcher.group(1)));
        }
        return questions;
    }

    /**
     * Reads the curated concept map from the provider via reflection.
     */
    @SuppressWarnings("unchecked")
    private Map<String, List<String>> readCuratedConcepts() throws Exception {
        final Field field = SampleMockInterviewProvider.class.getDeclaredField("QUESTION_CONCEPTS");
        field.setAccessible(true);
        return (Map<String, List<String>>) field.get(null);
    }

    /**
     * Normalises text the same way {@code questionConcepts} does: trim,
     * lowercase, and collapse whitespace.
     */
    private String normalize(final String text) {
        return text.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

}
