package com.devlaunch.service.ai;

import com.devlaunch.entity.enums.InterviewType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Deterministic, rule-based mock interview provider.
 * <p>
 * Generates a fixed bank of questions for each interview category and
 * evaluates answers using transparent heuristics. Meaningless input
 * (empty, gibberish, repeated filler, or explicit "I don't know" replies)
 * is scored at the bottom of the scale. Every other answer is first
 * checked for <em>relevance</em>: the answer must share concepts with the
 * question or use category-relevant terminology, otherwise it is capped
 * in the 0–10 band regardless of length, so an unrelated but grammatical
 * paragraph cannot outscore a short relevant answer. Relevant answers are
 * then scored from their length (base score) plus bonus points for using
 * category-relevant technical keywords, with partially relevant answers
 * capped in the middle bands. This provider requires no external
 * configuration and keeps the application fully functional when no LLM
 * API key is configured.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class SampleMockInterviewProvider implements MockInterviewProvider {

    /** The number of questions generated per interview category. */
    private static final int QUESTIONS_PER_CATEGORY = 5;

    /** Answers shorter than this are too brief to be considered meaningful. */
    private static final int MIN_MEANINGFUL_LENGTH = 15;

    /** Score awarded to short answers that explicitly decline to answer. */
    private static final int DECLINED_ANSWER_SCORE = 10;

    /** Score awarded to answers that merely repeat a few words as filler. */
    private static final int FILLER_ANSWER_SCORE = 5;

    /** Answers below this vowel-to-letter ratio are treated as gibberish. */
    private static final double MIN_VOWEL_RATIO = 0.15;

    /** Answers whose distinct-word ratio falls at or below this are filler. */
    private static final double MAX_DISTINCT_WORD_RATIO = 0.35;

    /** Answers need at least this many words to be considered repeated filler. */
    private static final int MIN_FILLER_WORDS = 8;

    /** Relevance at or above which an answer is clearly on-topic. */
    private static final int HIGH_RELEVANCE = 60;

    /** Relevance at or above which an answer is partially on-topic. */
    private static final int MODERATE_RELEVANCE = 30;

    /** Relevance below which an answer has almost no topical content. */
    private static final int LOW_RELEVANCE = 15;

    /** Score ceiling for partially on-topic answers. */
    private static final int MODERATE_CEILING = 75;

    /** Score ceiling for weakly on-topic answers. */
    private static final int LOW_RELEVANCE_CEILING = 40;

    /** Score ceiling for answers with almost no topical content. */
    private static final int NEAR_IRRELEVANT_CEILING = 20;

    /** Base score floor for moderately relevant answers. */
    private static final int MODERATE_FLOOR = 35;

    /** Base score floor for highly relevant answers. */
    private static final int HIGH_FLOOR = 55;

    /** Points awarded per question concept covered by the answer. */
    private static final int QUESTION_HIT_POINTS = 30;

    /** Points awarded per category keyword covered by the answer. */
    private static final int KEYWORD_HIT_POINTS = 5;

    /** Maximum number of question concepts that contribute to relevance. */
    private static final int MAX_QUESTION_HITS = 3;

    /** Question words shorter than this are ignored as too generic. */
    private static final int MIN_QUESTION_TERM_LENGTH = 4;

    /** Question banks keyed by interview category. */
    private static final Map<InterviewType, List<String[]>> QUESTION_BANK = new LinkedHashMap<>();

    /** Category-relevant keywords used to score answers, keyed by category. */
    private static final Map<InterviewType, List<String>> KEYWORDS = new LinkedHashMap<>();

    /** Phrases that signal the candidate declined to answer, matched against short answers. */
    private static final List<String> DECLINED_PATTERNS = List.of(
            "i don'?t know", "i do not know", "don'?t know", "do not know",
            "no idea", "not sure", "not certain", "dunno", "no clue",
            "haven'?t a clue", "i have no idea", "can'?t answer", "\\bidk\\b");

    /** Common or boilerplate words excluded from question-concept extraction. */
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "about", "after", "again", "all", "also", "am", "an", "and", "any",
            "are", "as", "at", "be", "because", "been", "before", "being", "between",
            "both", "but", "by", "can", "could", "describe", "did", "difference", "do",
            "does", "during", "each", "explain", "for", "from", "had", "has", "have",
            "having", "how", "however", "i", "if", "in", "into", "is", "it", "its",
            "just", "like", "may", "me", "more", "most", "much", "my", "no", "not",
            "now", "of", "on", "one", "only", "or", "other", "our", "out", "over",
            "should", "so", "some", "such", "than", "that", "the", "their", "them",
            "then", "there", "these", "they", "this", "those", "through", "time", "to",
            "under", "up", "us", "using", "versus", "vs", "want", "was", "we", "were",
            "what", "when", "where", "which", "while", "who", "whom", "why", "will",
            "with", "would", "you", "your");

    static {
        QUESTION_BANK.put(InterviewType.HR, List.of(
                new String[]{"Tell me about yourself and your background.",
                        "Structure your answer as: current role, relevant experience, and what you are looking for next."},
                new String[]{"Why do you want to work at this company?",
                        "Research the company first and connect its mission and products to your own goals."},
                new String[]{"Describe a time you faced a conflict with a teammate. How did you resolve it?",
                        "Use the STAR method: Situation, Task, Action, Result."},
                new String[]{"What are your greatest strengths and weaknesses?",
                        "Pick relevant strengths with evidence, and pair each weakness with what you are doing to improve it."},
                new String[]{"Where do you see yourself in five years?",
                        "Align your growth ambition with the company's trajectory."}));

        QUESTION_BANK.put(InterviewType.JAVA, List.of(
                new String[]{"Explain the difference between an abstract class and an interface in Java.",
                        "Cover inheritance, multiple implementation, default methods, and when to use each."},
                new String[]{"What is the difference between == and equals() in Java?",
                        "Mention reference comparison vs. value comparison and the equals/hashCode contract."},
                new String[]{"Explain how Garbage Collection works in Java.",
                        "Cover the heap generations, the JVM, and the stop-the-world concept."},
                new String[]{"What is the difference between checked and unchecked exceptions?",
                        "Mention compile-time enforcement and RuntimeException subclasses."},
                new String[]{"Explain the difference between HashMap and ConcurrentHashMap.",
                        "Cover thread safety, locking granularity, and performance trade-offs."}));

        QUESTION_BANK.put(InterviewType.SPRING_BOOT, List.of(
                new String[]{"What is dependency injection and how does Spring implement it?",
                        "Explain the Inversion of Control container, beans, and constructor injection."},
                new String[]{"Explain the difference between @Component, @Service, and @Repository.",
                        "They are all stereotype annotations — explain the semantic differences and exception translation."},
                new String[]{"What is Spring Boot auto-configuration and how does it work?",
                        "Cover @EnableAutoConfiguration, starter dependencies, and the conditions mechanism."},
                new String[]{"How would you secure a REST API with Spring Security?",
                        "Cover JWT-based stateless authentication, filters, and the security filter chain."},
                new String[]{"What is the difference between @RestController and @Controller?",
                        "Mention @ResponseBody, REST semantics, and error responses."}));

        QUESTION_BANK.put(InterviewType.SQL, List.of(
                new String[]{"Explain the difference between INNER JOIN and LEFT JOIN.",
                        "Describe the rows returned by each, ideally with a small example."},
                new String[]{"What is an index and when should you use one?",
                        "Cover B-tree indexes, query performance, and the cost on writes."},
                new String[]{"Explain the difference between WHERE and HAVING.",
                        "WHERE filters rows before grouping; HAVING filters groups after."},
                new String[]{"What is a transaction and what are the ACID properties?",
                        "Define atomicity, consistency, isolation, and durability."},
                new String[]{"Explain the difference between a primary key, a unique key, and a foreign key.",
                        "Cover constraints, nullability, and referential integrity."}));

        QUESTION_BANK.put(InterviewType.REACT, List.of(
                new String[]{"Explain the difference between props and state in React.",
                        "Props are read-only inputs; state is internal and mutable."},
                new String[]{"What are React hooks? Explain useState and useEffect.",
                        "Cover rules of hooks and common use cases for each."},
                new String[]{"Explain the concept of the virtual DOM and reconciliation.",
                        "Mention the diffing algorithm and why it improves performance."},
                new String[]{"What is the difference between controlled and uncontrolled components?",
                        "Controlled components drive inputs from React state; uncontrolled use the DOM directly."},
                new String[]{"How does React handle performance optimisation?",
                        "Cover React.memo, useMemo, useCallback, and lazy loading."}));

        KEYWORDS.put(InterviewType.HR, List.of(
                "team", "project", "experience", "challenge", "responsibility", "leadership",
                "goal", "strength", "weakness", "learning", "deadline", "feedback", "situation",
                "career", "role", "work", "lead", "manage", "communicat", "collaborat", "achiev"));
        KEYWORDS.put(InterviewType.JAVA, List.of(
                "class", "object", "inheritance", "polymorphism", "encapsulation", "abstraction",
                "interface", "exception", "collection", "thread", "jvm", "garbage", "contract",
                "inherit", "extend", "implement", "method", "override", "static", "equals",
                "hashcode", "oops", "hashmap"));
        KEYWORDS.put(InterviewType.SPRING_BOOT, List.of(
                "spring", "bean", "autowired", "dependency injection", "controller", "service",
                "repository", "jpa", "hibernate", "rest", "annotation", "ioc",
                "inversion of control", "configure", "security", "entity", "transaction",
                "actuator", "stereotype", "configuration", "endpoint"));
        KEYWORDS.put(InterviewType.SQL, List.of(
                "select", "join", "group by", "having", "index", "primary key", "foreign key",
                "normalization", "query", "database", "table", "row", "column", "where",
                "transaction", "order by", "inner", "left", "data", "aggregate", "unique key",
                "schema"));
        KEYWORDS.put(InterviewType.REACT, List.of(
                "react", "component", "props", "state", "hook", "render", "effect", "context",
                "dom", "virtual", "jsx", "memo", "usestate", "useeffect", "usecallback",
                "usememo", "rerender", "virtual dom", "re-render", "lifecycle", "update",
                "redux"));
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
     * Returns the fixed question bank for the requested category, with
     * identifiers of the form {@code <category>-1..N}.
     * </p>
     */
    @Override
    public List<InterviewQuestion> generateQuestions(final InterviewType type) {
        final List<String[]> bank = QUESTION_BANK.getOrDefault(type, List.of());
        final String prefix = type.name().toLowerCase(Locale.ROOT);

        final List<InterviewQuestion> questions = new ArrayList<>();
        for (int i = 0; i < bank.size() && i < QUESTIONS_PER_CATEGORY; i++) {
            final String[] entry = bank.get(i);
            questions.add(new InterviewQuestion(
                    prefix + "-" + (i + 1),
                    entry[0],
                    entry[1]));
        }
        return List.copyOf(questions);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Scores each answer from its length (base score) plus bonus points
     * for matching category-relevant keywords, while meaningless input is
     * pushed to the bottom of the scale and answers that do not address
     * the question are capped in the 0–10 band. Strengths and areas for
     * improvement are derived from the highest and lowest scoring
     * answers, and the overall score is the average across all answers.
     * </p>
     */
    @Override
    public MockInterviewFeedback evaluate(final InterviewType type,
                                          final List<InterviewAnswer> answers) {
        final List<MockInterviewFeedback.Item> items = new ArrayList<>();
        final List<String> strengths = new ArrayList<>();
        final List<String> areasForImprovement = new ArrayList<>();

        int scoreSum = 0;

        for (final InterviewAnswer answer : answers) {
            final AnswerAssessment assessment = assessAnswer(type, answer.question(), answer.answer());
            final int score = assessment.score();
            scoreSum += score;

            final String feedbackText = feedbackForScore(score, assessment.unrelated());
            final List<String> suggestions = suggestionsForScore(type, answer, score, assessment.unrelated());

            items.add(new MockInterviewFeedback.Item(
                    answer.questionId(),
                    answer.question(),
                    answer.answer(),
                    score,
                    feedbackText,
                    suggestions));

            if (score >= 75) {
                strengths.add("Strong response to: " + answer.question());
            } else if (score < 60) {
                areasForImprovement.add(assessment.unrelated()
                        ? "Your answer did not address the question: " + answer.question()
                        : "Expand your answer on: " + answer.question());
            }
        }

        if (strengths.isEmpty()) {
            strengths.add("Consistent effort across all questions — deepen your answers to unlock more strengths.");
        }
        if (areasForImprovement.isEmpty()) {
            areasForImprovement.add("No major gaps detected — keep practising to maintain this level.");
        }

        final int overallScore = answers.isEmpty()
                ? 0
                : Math.round((float) scoreSum / answers.size());

        return new MockInterviewFeedback(
                overallScore,
                List.copyOf(items),
                List.copyOf(strengths),
                List.copyOf(areasForImprovement));
    }

    /**
     * Computes the score (0–100) and relevance flag for a single answer.
     * <p>
     * Meaningless input — empty, gibberish, or repeated filler — scores 0–5,
     * explicit "I don't know" style replies score in the low single digits,
     * and short answers score the floor. Meaningful answers are then scored
     * from their relevance to the question: answers sharing no question
     * concepts and no category terminology are capped in the 0–10 band
     * regardless of length, weakly relevant answers are capped at 20–40,
     * and moderately relevant answers at 50–75. Only clearly relevant,
     * well-developed answers can reach the 80–100 band.
     * </p>
     */
    private AnswerAssessment assessAnswer(final InterviewType type, final String question,
                                          final String answer) {
        final String text = answer == null ? "" : answer.trim();

        if (isGibberish(text)) {
            return new AnswerAssessment(0, false);
        }
        if (isRepeatedWordFiller(text)) {
            return new AnswerAssessment(FILLER_ANSWER_SCORE, false);
        }
        if (isDeclinedAnswer(text)) {
            return new AnswerAssessment(DECLINED_ANSWER_SCORE, false);
        }
        if (text.length() < MIN_MEANINGFUL_LENGTH) {
            return new AnswerAssessment(5, false);
        }

        final String lower = text.toLowerCase(Locale.ROOT);
        final int keywordHits = countKeywordHits(type, lower);

        final int relevance = relevanceScore(type, question, lower, keywordHits);
        if (relevance == 0) {
            return new AnswerAssessment(unrelatedScore(text), true);
        }

        final int lengthScore = lengthScore(text.length());
        final int keywordBonus = keywordBonus(keywordHits);

        if (relevance < LOW_RELEVANCE) {
            return new AnswerAssessment(clamp(Math.min(NEAR_IRRELEVANT_CEILING, lengthScore + keywordBonus)), false);
        }
        if (relevance < MODERATE_RELEVANCE) {
            return new AnswerAssessment(clamp(Math.min(LOW_RELEVANCE_CEILING, lengthScore + keywordBonus)), false);
        }
        if (relevance < HIGH_RELEVANCE) {
            return new AnswerAssessment(
                    clamp(Math.min(MODERATE_CEILING, Math.max(lengthScore, MODERATE_FLOOR) + keywordBonus)), false);
        }
        return new AnswerAssessment(clamp(Math.max(lengthScore, HIGH_FLOOR) + keywordBonus), false);
    }

    /**
     * Computes a 0–100 relevance score for an answer: how much of the
     * question's own concepts it covers plus how much category-relevant
     * terminology it uses. Question concepts are matched as whole words so
     * that unrelated words sharing a prefix (for example "javascript" vs
     * "java") are not counted, while category keywords are matched
     * flexibly as substrings so plurals and inflections (for example
     * "components" or "communicated") are recognised without an exact
     * match. Both signals make the check tolerant of paraphrasing while
     * still catching off-topic answers.
     *
     * @param type        the interview category (supplies the terminology)
     * @param question    the question text (supplies the concepts)
     * @param lowerAnswer the lowercased answer text
     * @param keywordHits the precomputed category keyword hit count
     * @return the relevance score in the 0–100 range
     */
    private int relevanceScore(final InterviewType type, final String question,
                               final String lowerAnswer, final int keywordHits) {
        final Set<String> tokens = answerTokens(lowerAnswer);

        final long questionHits = questionTerms(question).stream()
                .filter(tokens::contains)
                .count();

        return Math.min(100, (int) Math.min(MAX_QUESTION_HITS, questionHits) * QUESTION_HIT_POINTS
                + keywordHits * KEYWORD_HIT_POINTS);
    }

    /**
     * Extracts the meaningful concepts from a question by lowercasing,
     * tokenising, and dropping stop words and very short terms.
     *
     * @param question the question text
     * @return the distinct, meaningful question terms
     */
    private List<String> questionTerms(final String question) {
        if (question == null || question.isBlank()) {
            return List.of();
        }
        return Arrays.stream(question.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(term -> term.length() >= MIN_QUESTION_TERM_LENGTH)
                .filter(term -> !STOP_WORDS.contains(term))
                .distinct()
                .toList();
    }

    /**
     * Tokenises lowercased text into a set of distinct whole words.
     *
     * @param lowerText the lowercased text
     * @return the distinct tokens
     */
    private Set<String> answerTokens(final String lowerText) {
        return Arrays.stream(lowerText.split("[^a-z0-9]+"))
                .filter(token -> !token.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Scores a grammatically valid but completely off-topic answer in the
     * 0–10 band; very long paragraphs gain at most a small credit so the
     * ceiling is never crossed.
     */
    private int unrelatedScore(final String text) {
        return Math.min(10, 5 + text.length() / 150);
    }

    /**
     * Maps a meaningful answer length to a base score band: short answers
     * 20–40, average answers 50–75, and detailed answers 80–100 once the
     * keyword bonus is applied. Reaching the top band requires both length
     * and category-relevant terminology; long but keyword-free answers cap
     * out at the top of the average band.
     */
    private int lengthScore(final int length) {
        if (length < 40) {
            return 20;
        }
        if (length < 100) {
            return 30;
        }
        if (length < 200) {
            return 50;
        }
        if (length < 350) {
            return 60;
        }
        if (length < 500) {
            return 75;
        }
        return 85;
    }

    /**
     * Awards up to 15 bonus points based on the precomputed number of
     * matching category keywords.
     */
    private int keywordBonus(final int keywordHits) {
        return Math.min(15, keywordHits * 5);
    }

    /**
     * Counts how many category keywords appear in the lowercased text.
     * <p>
     * Shared by the relevance computation and the keyword bonus so the
     * matching rules stay in one place.
     * </p>
     */
    private int countKeywordHits(final InterviewType type, final String lowerText) {
        int hits = 0;
        for (final String keyword : KEYWORDS.getOrDefault(type, List.of())) {
            if (matchesKeyword(lowerText, keyword)) {
                hits++;
            }
        }
        return hits;
    }

    /**
     * Flexibly matches a keyword against lowercased text.
     * <p>
     * Multi-word phrases (for example "group by" or "dependency
     * injection") are matched as substrings, while single words are
     * matched when the answer contains a word that <em>starts with</em>
     * the keyword. The prefix rule recognises plurals and inflections
     * ("components", "communicated", "responsibilities") without requiring
     * an exact match, and avoids the false positives of plain substring
     * matching ("row" does not match "arrow" or "grow", "dom" does not
     * match "random").
     * </p>
     * <p>
     * Because an inflected word may start with several related keywords
     * (for example "leadership" starts with both "lead" and "leadership",
     * and "inheritance" with both "inherit" and "inheritance"), a single
     * word can count as more than one hit. This is an accepted trade-off
     * for broad inflection coverage; the extra hits are bounded by the
     * banded score caps and only ever inflate the score of answers that
     * are already on topic.
     * </p>
     */
    private boolean matchesKeyword(final String lowerText, final String keyword) {
        if (keyword.indexOf(' ') >= 0) {
            return lowerText.contains(keyword);
        }
        for (final String word : lowerText.split("[^a-z0-9]+")) {
            if (word.startsWith(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builds the written feedback for an answer based on its score band
     * and whether it was unrelated to the question.
     */
    private String feedbackForScore(final int score, final boolean unrelated) {
        if (unrelated) {
            return "Your answer does not address the interview question. Focus on explaining the requested concept using relevant technical terminology and examples.";
        }
        if (score < 15) {
            return "This answer did not provide meaningful content. Even if you are unsure, describe what you would do, what you would check first, or how you would approach the problem.";
        }
        if (score < 45) {
            return "Your answer was quite brief. Expand it with more detail and a concrete example to show deeper understanding.";
        }
        if (score < 70) {
            return "Solid answer with the right idea. Add a real-world example or more technical detail to strengthen it.";
        }
        return "Strong answer — clear, detailed, and well-structured.";
    }

    /**
     * Builds specific improvement suggestions for an answer.
     */
    private List<String> suggestionsForScore(final InterviewType type,
                                             final InterviewAnswer answer,
                                             final int score,
                                             final boolean unrelated) {
        final List<String> suggestions = new ArrayList<>();

        if (unrelated) {
            suggestions.add("Focus on explaining the requested concept using relevant technical terminology and examples.");
            return List.copyOf(suggestions);
        }
        if (score < 45) {
            suggestions.add("Aim for at least 3–4 sentences covering the 'what', the 'how', and the 'why'.");
        }
        if (answer.answer() != null
                && answer.answer().length() < 300
                && !KEYWORDS.getOrDefault(type, List.of()).isEmpty()) {
            suggestions.add("Include specific terminology relevant to " + typeLabel(type) + " to show depth.");
        }
        if (score >= 70) {
            suggestions.add("Consider backing this with a concrete example from your own experience.");
        }

        return List.copyOf(suggestions);
    }

    /**
     * Detects answers with no meaningful content: empty input, keyboard
     * mash, or random consonant strings (for example "hdg hdbv hdbbjsb").
     * <p>
     * The vowel-ratio heuristic also flags very short acronym-only replies
     * (for example "JVM GC STW") as gibberish; such terse answers are
     * intentionally scored at the bottom of the scale.
     * </p>
     */
    private boolean isGibberish(final String text) {
        if (text.isEmpty()) {
            return true;
        }
        final String lower = text.toLowerCase(Locale.ROOT);
        final long letters = lower.chars().filter(Character::isLetter).count();
        if (letters == 0) {
            return true;
        }
        final long vowels = lower.chars().filter(c -> "aeiou".indexOf(c) >= 0).count();
        if ((double) vowels / letters < MIN_VOWEL_RATIO) {
            return true;
        }
        final long distinct = lower.chars().filter(c -> c >= 'a' && c <= 'z').distinct().count();
        return distinct <= 4 && letters >= 8;
    }

    /**
     * Detects answers that repeat a small set of words over and over as
     * filler (for example "react is great react is great react is great").
     * Such answers contain length but no content, so they are scored at
     * the bottom of the scale.
     */
    private boolean isRepeatedWordFiller(final String text) {
        final String[] words = text.toLowerCase(Locale.ROOT).split("\\s+");
        if (words.length < MIN_FILLER_WORDS) {
            return false;
        }
        final long distinct = Arrays.stream(words).filter(word -> !word.isEmpty()).distinct().count();
        return (double) distinct / words.length <= MAX_DISTINCT_WORD_RATIO;
    }

    /**
     * Detects short answers that explicitly decline to answer, such as
     * "I don't know" or "no idea". Only answers dominated by a declining
     * phrase are treated this way; longer answers that merely start with
     * one are scored normally.
     */
    private boolean isDeclinedAnswer(final String text) {
        if (text.length() > 60) {
            return false;
        }
        final String lower = text.toLowerCase(Locale.ROOT);
        return DECLINED_PATTERNS.stream().anyMatch(pattern ->
                lower.matches(".*" + pattern + ".*"));
    }

    /**
     * Returns a human-readable label for an interview category.
     */
    private String typeLabel(final InterviewType type) {
        return switch (type) {
            case HR -> "HR interviews";
            case JAVA -> "Java";
            case SPRING_BOOT -> "Spring Boot";
            case SQL -> "SQL";
            case REACT -> "React";
        };
    }

    /**
     * Clamps a score into the valid 0–100 range.
     */
    private int clamp(final int score) {
        return Math.max(0, Math.min(100, score));
    }

    /**
     * The outcome of assessing a single answer.
     *
     * @param score     the score in the 0–100 range
     * @param unrelated whether the answer was found to be off-topic
     */
    private record AnswerAssessment(int score, boolean unrelated) {
    }

}
