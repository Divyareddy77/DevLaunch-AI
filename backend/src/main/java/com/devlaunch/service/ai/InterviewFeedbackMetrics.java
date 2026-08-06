package com.devlaunch.service.ai;

import com.devlaunch.entity.enums.InterviewType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared scoring helpers for mock interview evaluations.
 * <p>
 * Both mock interview providers ({@link SampleMockInterviewProvider} and
 * {@link OpenAiMockInterviewProvider}) delegate their per-dimension scores,
 * personalised suggestions, and missed-concept detection to this single
 * utility so the business rules live in exactly one place. All scores are
 * clamped to the 0–100 range.
 * </p>
 *
 * @author DevLaunch
 */
public final class InterviewFeedbackMetrics {

    /** Ideal average answer length (in words) used to score communication. */
    private static final double IDEAL_AVERAGE_WORDS = 90.0;

    /** Ideal words-per-sentence used to score clarity. */
    private static final double IDEAL_WORDS_PER_SENTENCE = 18.0;

    /** Answers with fewer than this many words on average are too brief. */
    private static final int MIN_AVERAGE_WORDS = 55;

    /** Per-answer scores below this flag the question's concept as missed. */
    private static final int MISSED_CONCEPT_THRESHOLD = 55;

    /** A dimension score below this triggers a related practice suggestion. */
    private static final int WEAK_DIMENSION_THRESHOLD = 65;

    /** Words too common to count towards vocabulary breadth. */
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "but", "by", "for",
            "from", "has", "have", "i", "in", "is", "it", "of", "on", "or",
            "that", "the", "their", "there", "they", "this", "to", "was",
            "we", "were", "with", "you", "your");

    /** Topic hints keyed by category, used to name practice suggestions. */
    private static final Map<InterviewType, List<KeywordTopic>> TOPIC_HINTS =
            new LinkedHashMap<>();

    static {
        TOPIC_HINTS.put(InterviewType.JAVA, List.of(
                new KeywordTopic(List.of("collection", "hashmap", "arraylist", "hashset"),
                        "Java Collections"),
                new KeywordTopic(List.of("stream", "lambda", "optional"), "Java Streams and functional APIs"),
                new KeywordTopic(List.of("thread", "concurrent", "synchroni", "executor", "deadlock"),
                        "Java concurrency"),
                new KeywordTopic(List.of("jvm", "memory", "garbage", "heap", "stack"), "JVM internals"),
                new KeywordTopic(List.of("exception", "throw", "error"), "Java exception handling"),
                new KeywordTopic(List.of("generics", "wildcard", "type erasure"), "Java generics"),
                new KeywordTopic(List.of("string", "immutab"), "String handling and immutability"),
                new KeywordTopic(List.of("equals", "hashcode"), "the equals() / hashCode() contract")));
        TOPIC_HINTS.put(InterviewType.SPRING_BOOT, List.of(
                new KeywordTopic(List.of("bean", "injection", "ioc", "autowired"), "Spring dependency injection"),
                new KeywordTopic(List.of("security", "jwt", "filter chain"), "Spring Security"),
                new KeywordTopic(List.of("jpa", "hibernate", "entity", "repository", "n+1"),
                        "Spring Data JPA"),
                new KeywordTopic(List.of("transaction"), "Spring transactions"),
                new KeywordTopic(List.of("autoconfiguration", "starter", "configuration"),
                        "Spring Boot auto-configuration"),
                new KeywordTopic(List.of("controller", "rest", "endpoint", "responseentity"),
                        "REST API design with Spring MVC")));
        TOPIC_HINTS.put(InterviewType.SQL, List.of(
                new KeywordTopic(List.of("join", "inner", "left", "outer"), "SQL joins"),
                new KeywordTopic(List.of("index", "btree"), "Database indexing"),
                new KeywordTopic(List.of("transaction", "acid"), "Transactions and ACID"),
                new KeywordTopic(List.of("group by", "having", "aggregate"), "aggregation with GROUP BY / HAVING"),
                new KeywordTopic(List.of("primary key", "foreign key", "unique key", "constraint"),
                        "keys and referential integrity"),
                new KeywordTopic(List.of("normaliz"), "database normalisation")));
        TOPIC_HINTS.put(InterviewType.REACT, List.of(
                new KeywordTopic(List.of("hook", "usestate", "useeffect", "usememo", "usecallback"),
                        "React Hooks"),
                new KeywordTopic(List.of("state", "context", "redux", "zustand", "prop drilling"),
                        "state management in React"),
                new KeywordTopic(List.of("virtual", "render", "dom", "reconcil", "fiber"),
                        "React rendering and the virtual DOM"),
                new KeywordTopic(List.of("performance", "optimiz", "memo", "lazy", "rerender"),
                        "React performance optimisation"),
                new KeywordTopic(List.of("router", "navigation"), "React Router")));
        TOPIC_HINTS.put(InterviewType.HR, List.of(
                new KeywordTopic(List.of("conflict", "teammate", "disagree"), "the STAR method for conflict stories"),
                new KeywordTopic(List.of("strength", "weakness"), "framing strengths and weaknesses with evidence"),
                new KeywordTopic(List.of("goal", "career", "five year"), "articulating career goals"),
                new KeywordTopic(List.of("company", "mission", "culture", "value"), "researching the company")));
    }

    private InterviewFeedbackMetrics() {
        // Utility class — no instantiation
    }

    /**
     * Computes the dimension scores, personalised suggestions, and missed
     * concepts for a completed interview.
     *
     * @param type  the interview category that was practised
     * @param items the per-question evaluation items
     * @return the derived metrics
     */
    public static Metrics compute(final InterviewType type,
                                  final List<MockInterviewFeedback.Item> items) {
        if (items.isEmpty()) {
            return new Metrics(0, 0, 0, 0, 0, 0, 0, List.of(), List.of());
        }

        final int averageScore = average(items.stream()
                .mapToInt(MockInterviewFeedback.Item::score).toArray());
        final double averageWords = items.stream()
                .mapToDouble(item -> wordCount(item.answer()))
                .average().orElse(0.0);
        final double averageWordsPerSentence = averageWordsPerSentence(items);
        final double distinctWordRatio = distinctWordRatio(items);

        final int communicationScore = clamp(
                (int) Math.round(averageWords / IDEAL_AVERAGE_WORDS * 100));
        final int clarityScore = clamp(100 - (int) Math.round(
                Math.abs(averageWordsPerSentence - IDEAL_WORDS_PER_SENTENCE) * 2.5));
        final int vocabularyScore = clamp((int) Math.round(distinctWordRatio * 130));
        final int confidenceScore = clamp((int) Math.round(
                averageWords * 0.75 + Math.min(averageWords, 60) * 0.4));
        final int professionalismScore = clamp((int) Math.round(
                averageScore * 0.8 + Math.min(averageWords, 100) * 0.2));

        return new Metrics(
                averageScore,
                communicationScore,
                confidenceScore,
                averageScore,
                clarityScore,
                vocabularyScore,
                professionalismScore,
                generateSuggestions(type, items, communicationScore, vocabularyScore, averageWords),
                deriveMissedConcepts(items));
    }

    /**
     * Derives a short, deduplicated list of concepts the answers missed,
     * based on the questions whose answers scored below the threshold.
     *
     * @param items the per-question evaluation items
     * @return the missed concept labels, or an empty list
     */
    public static List<String> deriveMissedConcepts(final List<MockInterviewFeedback.Item> items) {
        final List<String> missed = new ArrayList<>();
        for (final MockInterviewFeedback.Item item : items) {
            if (item.score() < MISSED_CONCEPT_THRESHOLD) {
                final String label = conciseTopic(item.question());
                if (!missed.contains(label)) {
                    missed.add(label);
                }
            }
        }
        return List.copyOf(missed);
    }

    /**
     * Builds personalised practice suggestions from the evaluation data,
     * ordered by likely impact and capped at five items.
     *
     * @param type                the interview category that was practised
     * @param items               the per-question evaluation items
     * @param communicationScore  the communication dimension score
     * @param vocabularyScore     the vocabulary dimension score
     * @param averageWords        the average answer length in words
     * @return the ordered practice suggestions
     */
    private static List<String> generateSuggestions(final InterviewType type,
                                                    final List<MockInterviewFeedback.Item> items,
                                                    final int communicationScore,
                                                    final int vocabularyScore,
                                                    final double averageWords) {
        final List<String> suggestions = new ArrayList<>();
        final List<String> topics = new ArrayList<>();

        for (final MockInterviewFeedback.Item item : items) {
            if (item.score() >= MISSED_CONCEPT_THRESHOLD) {
                continue;
            }
            final String topic = topicHint(type, item.question());
            if (topic != null && !topics.contains(topic)) {
                topics.add(topic);
            }
        }
        for (final String topic : topics) {
            suggestions.add("Practice " + topic + " before your next interview.");
        }

        if (communicationScore < WEAK_DIMENSION_THRESHOLD) {
            suggestions.add("Speak at a steadier pace — aim for 100–140 words per minute.");
        }
        if (vocabularyScore < WEAK_DIMENSION_THRESHOLD) {
            suggestions.add("Reduce filler words such as \"um\", \"uh\", \"like\", and \"you know\".");
        }
        if (averageWords < MIN_AVERAGE_WORDS) {
            suggestions.add("Increase answer depth — structure answers with a concrete example and a result.");
        }
        if (suggestions.isEmpty()) {
            suggestions.add("Strong session! Try a harder difficulty next time to keep improving.");
        }

        return suggestions.stream().limit(5).toList();
    }

    /**
     * Matches a question against the category's topic hints.
     *
     * @param type     the interview category
     * @param question the question text
     * @return the topic label, or {@code null} when nothing matches
     */
    private static String topicHint(final InterviewType type, final String question) {
        final String text = question.toLowerCase(Locale.ROOT);
        final List<KeywordTopic> hints = TOPIC_HINTS.getOrDefault(type, List.of());
        for (final KeywordTopic hint : hints) {
            if (hint.keywords().stream().anyMatch(text::contains)) {
                return hint.topic();
            }
        }
        return null;
    }

    /**
     * Reduces a question to a short practice topic by keeping its first
     * few meaningful words.
     *
     * @param question the question text
     * @return a concise topic label
     */
    private static String conciseTopic(final String question) {
        final String[] words = question.split("\\s+");
        final StringBuilder topic = new StringBuilder();
        int kept = 0;
        for (final String word : words) {
            final String cleaned = word.replaceAll("[^a-zA-Z]", "").toLowerCase(Locale.ROOT);
            if (cleaned.length() >= 4 && !STOP_WORDS.contains(cleaned)) {
                topic.append(topic.isEmpty() ? "" : " ").append(word.trim().replaceAll("[.,;:]$", ""));
                kept++;
                if (kept >= 3) {
                    break;
                }
            }
        }
        if (topic.isEmpty()) {
            return "the question's core concepts";
        }
        return topic.toString().toLowerCase(Locale.ROOT);
    }

    /**
     * Counts the words in a piece of text.
     */
    private static int wordCount(final String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }

    /**
     * Computes the average words-per-sentence across all answers.
     */
    private static double averageWordsPerSentence(final List<MockInterviewFeedback.Item> items) {
        final Pattern sentenceEnd = Pattern.compile("[.!?]+");
        int sentenceCount = 0;
        int wordCountTotal = 0;
        for (final MockInterviewFeedback.Item item : items) {
            final String answer = item.answer() == null ? "" : item.answer().trim();
            if (answer.isEmpty()) {
                continue;
            }
            wordCountTotal += wordCount(answer);
            final Matcher matcher = sentenceEnd.matcher(answer);
            int matches = 0;
            while (matcher.find()) {
                matches++;
            }
            // An answer with no sentence punctuation counts as one sentence.
            sentenceCount += matches == 0 ? 1 : matches;
        }
        if (sentenceCount == 0) {
            return IDEAL_WORDS_PER_SENTENCE;
        }
        return (double) wordCountTotal / sentenceCount;
    }

    /**
     * Computes the distinct-word ratio across all answer text.
     */
    private static double distinctWordRatio(final List<MockInterviewFeedback.Item> items) {
        final Set<String> distinct = new java.util.HashSet<>();
        int total = 0;
        for (final MockInterviewFeedback.Item item : items) {
            if (item.answer() == null || item.answer().isBlank()) {
                continue;
            }
            for (final String word : item.answer().toLowerCase(Locale.ROOT).split("[^a-z]+")) {
                if (word.isEmpty() || STOP_WORDS.contains(word)) {
                    continue;
                }
                distinct.add(word);
                total++;
            }
        }
        if (total == 0) {
            return 0.0;
        }
        return (double) distinct.size() / total;
    }

    /**
     * Averages the given scores.
     */
    private static int average(final int[] values) {
        int sum = 0;
        for (final int value : values) {
            sum += value;
        }
        return (int) Math.round((double) sum / values.length);
    }

    /**
     * Clamps a score into the 0–100 range.
     */
    private static int clamp(final int score) {
        return Math.max(0, Math.min(100, score));
    }

    /**
     * A keyword-to-topic mapping used to name practice suggestions.
     *
     * @param keywords the question-text keywords that match the topic
     * @param topic    the human-readable topic label
     */
    private record KeywordTopic(List<String> keywords, String topic) {
    }

    /**
     * The derived dimension scores, suggestions, and missed concepts.
     *
     * @param technicalScore       the technical knowledge score
     * @param communicationScore   the communication score
     * @param confidenceScore      the confidence estimate
     * @param problemSolvingScore  the problem-solving score
     * @param clarityScore         the answer clarity score
     * @param vocabularyScore      the vocabulary breadth score
     * @param professionalismScore the professionalism score
     * @param suggestions          the personalised practice suggestions
     * @param missedConcepts       the concepts the answers did not cover
     */
    public record Metrics(int technicalScore, int communicationScore,
                          int confidenceScore, int problemSolvingScore,
                          int clarityScore, int vocabularyScore,
                          int professionalismScore,
                          List<String> suggestions, List<String> missedConcepts) {
    }

}
