package com.devlaunch.service.ai;

import com.devlaunch.entity.enums.InterviewType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the deterministic sample interview scoring.
 * <p>
 * Verifies that meaningless answers no longer receive the previous fixed
 * minimum score and that each answer quality band maps to the expected
 * range: gibberish/empty 0–5, "I don't know" style 5–15, short but
 * meaningful 20–40, average 50–75, detailed and relevant 80–100. Also
 * verifies the relevance-aware scoring: answers that do not address the
 * question — regardless of length or grammar — are capped in the 0–10
 * band, while answers that cover the question's concepts score high.
 * </p>
 *
 * @author DevLaunch
 */
class SampleMockInterviewProviderTest {

    private final SampleMockInterviewProvider provider = new SampleMockInterviewProvider();

    /**
     * Scores a single Java answer through the public evaluation entry point.
     */
    private int score(final String answer) {
        return score(InterviewType.JAVA, "What is Java?", answer);
    }

    /**
     * Scores a single answer for the given category and question through
     * the public evaluation entry point.
     */
    private int score(final InterviewType type, final String question, final String answer) {
        final MockInterviewFeedback feedback = provider.evaluate(
                type,
                List.of(new InterviewAnswer("q-1", question, answer)));
        return feedback.feedback().get(0).score();
    }

    @Test
    @DisplayName("empty and gibberish answers score 0")
    void gibberishScoresZero() {
        assertEquals(0, score(""));
        assertEquals(0, score("hdg hdbv hdbbjsb"));
        assertEquals(0, score("asdf asdf asdf asdf"));
    }

    @Test
    @DisplayName("explicit I-don't-know answers score in the 5-15 band")
    void declinedAnswersScoreLow() {
        final int score = score("I don't know");
        assertTrue(score >= 5 && score <= 15, "expected 5-15 but was " + score);
        final int noIdea = score("no idea");
        assertTrue(noIdea >= 5 && noIdea <= 15, "expected 5-15 but was " + noIdea);
    }

    @Test
    @DisplayName("very short non-answers score 0-5")
    void tooShortAnswersScoreFloor() {
        final int score = score("yes");
        assertTrue(score >= 0 && score <= 5, "expected 0-5 but was " + score);
    }

    @Test
    @DisplayName("short but meaningful answers score 20-40")
    void shortMeaningfulScoresModerate() {
        final int score = score("I am a Java developer who builds REST APIs.");
        assertTrue(score >= 20 && score <= 40, "expected 20-40 but was " + score);
    }

    @Test
    @DisplayName("average answers score 50-75")
    void averageAnswersScoreMid() {
        final int score = score("In Java, an interface defines a contract of methods that a class "
                + "agrees to implement, and a class can implement several interfaces, which "
                + "supports multiple inheritance of type. An abstract class, however, can hold "
                + "state and provide partial implementations that subclasses extend.");
        assertTrue(score >= 50 && score <= 75, "expected 50-75 but was " + score);
    }

    @Test
    @DisplayName("detailed relevant answers score 80-100")
    void detailedAnswersScoreHigh() {
        final int score = score("In Java, an interface is a reference type that defines a contract of "
                + "abstract methods, and since Java 8 it may also declare default and static methods. "
                + "A class can implement multiple interfaces, which gives Java a form of multiple "
                + "inheritance for types while avoiding the diamond problem that arises with classes. "
                + "An abstract class, in contrast, is a class that cannot be instantiated directly, "
                + "can hold state in instance fields, and can provide a partial implementation that "
                + "subclasses extend and complete. In practice I choose an interface when I only need "
                + "to expose a contract that several unrelated classes can honour, for example a "
                + "repository contract in the persistence layer, and I choose an abstract class when "
                + "subclasses should share common fields or behaviour, for example a base request "
                + "handler. This distinction matters because it drives how loosely coupled and how "
                + "testable the resulting design is, and getting it right keeps the object model "
                + "clean as the codebase grows.");
        assertTrue(score >= 80 && score <= 100, "expected 80-100 but was " + score);
    }

    @Test
    @DisplayName("gibberish scores strictly below a short meaningful answer")
    void gibberishScoresBelowMeaningful() {
        assertTrue(score("hdg hdbv hdbbjsb") < score("I am a Java developer who builds REST APIs."));
    }

    @Test
    @DisplayName("unrelated English paragraphs score 0-10 regardless of length")
    void unrelatedParagraphScoresNearZero() {
        final int score = score(InterviewType.REACT,
                "Explain the difference between props and state in React.",
                "Today is a nice day. I like eating apples. The sky is blue. "
                        + "My favorite movie is Avengers. I play cricket every weekend.");
        assertTrue(score >= 0 && score <= 10, "expected 0-10 but was " + score);
    }

    @Test
    @DisplayName("a correct React answer to a React question scores 70-100")
    void relevantReactAnswerScoresHigh() {
        final int score = score(InterviewType.REACT,
                "Explain the difference between props and state in React.",
                "In React, props are passed from parent to child and are read-only inputs. "
                        + "State stores data inside the component and can be updated with setState "
                        + "or the useState hook, which causes the component to re-render.");
        assertTrue(score >= 70 && score <= 100, "expected 70-100 but was " + score);
    }

    @Test
    @DisplayName("a React answer to a Java question scores 0-10")
    void crossCategoryAnswerScoresLow() {
        final int score = score(InterviewType.JAVA,
                "Explain the difference between an abstract class and an interface in Java.",
                "React uses components, props and state to render a virtual DOM efficiently.");
        assertTrue(score >= 0 && score <= 10, "expected 0-10 but was " + score);
    }

    @Test
    @DisplayName("a cricket answer to a SQL question scores 0-10")
    void sqlQuestionWithUnrelatedAnswerScoresLow() {
        final int score = score(InterviewType.SQL,
                "Explain the difference between INNER JOIN and LEFT JOIN.",
                "I play cricket every weekend with my friends. My favorite player is Virat Kohli "
                        + "and we won the local tournament last month.");
        assertTrue(score >= 0 && score <= 10, "expected 0-10 but was " + score);
    }

    @Test
    @DisplayName("a meaningful HR answer to an HR question scores 70-100")
    void meaningfulHrAnswerScoresHigh() {
        final int score = score(InterviewType.HR,
                "Tell me about yourself and your background.",
                "I have three years of experience working in a team and delivering projects. "
                        + "I took responsibility for leading a small group and communicated well "
                        + "under deadlines. My goal is to grow into a leadership role, and I am "
                        + "learning from feedback every day.");
        assertTrue(score >= 70 && score <= 100, "expected 70-100 but was " + score);
    }

    @Test
    @DisplayName("a Java-only answer to a Spring Boot question scores moderately")
    void springQuestionWithJavaOnlyAnswerScoresModerate() {
        final int score = score(InterviewType.SPRING_BOOT,
                "What is dependency injection and how does Spring implement it?",
                "In Java, dependency injection means that an object receives its dependencies "
                        + "from outside rather than creating them itself. This decouples the classes "
                        + "and makes unit testing easier, because you can inject mocks into the "
                        + "class under test.");
        assertTrue(score >= 40 && score <= 75, "expected 40-75 but was " + score);
    }

    @Test
    @DisplayName("repeated-word filler answers still score near zero")
    void repeatedWordAnswersScoreNearZero() {
        final int score = score(InterviewType.REACT,
                "What are React hooks? Explain useState and useEffect.",
                "React is great react is great react is great react is great react is great");
        assertTrue(score >= 0 && score <= 10, "expected 0-10 but was " + score);
    }

    @Test
    @DisplayName("repeated I-don't-know filler scores near zero")
    void repeatedDeclinedFillerScoresNearZero() {
        final int score = score("I don't know I don't know I don't know I don't know I don't know");
        assertTrue(score >= 0 && score <= 10, "expected 0-10 but was " + score);
    }

    @Test
    @DisplayName("short but relevant answers score 20-40")
    void shortRelevantAnswersScoreModerate() {
        final int score = score(InterviewType.REACT,
                "Explain the difference between props and state in React.",
                "Props are inputs.");
        assertTrue(score >= 20 && score <= 40, "expected 20-40 but was " + score);
    }

    @Test
    @DisplayName("unrelated answers receive a relevance-specific feedback message")
    void unrelatedAnswerFeedbackMentionsQuestion() {
        final MockInterviewFeedback feedback = provider.evaluate(
                InterviewType.REACT,
                List.of(new InterviewAnswer("react-1",
                        "Explain the difference between props and state in React.",
                        "Today is a nice day. I like eating apples.")));

        final String text = feedback.feedback().get(0).feedback();
        assertTrue(text.contains("does not address the interview question"),
                "expected relevance feedback but was: " + text);

        assertTrue(feedback.areasForImprovement().stream()
                        .anyMatch(area -> area.contains("did not address")),
                "expected an off-topic area for improvement but was: " + feedback.areasForImprovement());
    }
}
