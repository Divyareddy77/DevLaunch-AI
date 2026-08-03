package com.devlaunch.service.ai;

import com.devlaunch.entity.enums.InterviewType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * OpenAI-compatible mock interview provider.
 * <p>
 * Uses the shared {@link OpenAiChatCompletions} client to generate
 * interview questions and to evaluate the user's answers. The provider
 * is only considered configured when an API key is present; otherwise
 * the service layer falls back to the deterministic
 * {@link SampleMockInterviewProvider}. Any network, parsing, or provider
 * error is surfaced so the caller can fall back gracefully.
 * </p>
 *
 * @author DevLaunch
 */
@Service
@Qualifier("openAiMockInterviewProvider")
public class OpenAiMockInterviewProvider implements MockInterviewProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiMockInterviewProvider.class);

    /** System prompt instructing the model to return questions as strict JSON. */
    private static final String QUESTIONS_SYSTEM_PROMPT =
            "You are an expert technical interviewer. Generate a set of interview questions "
                    + "for the requested category and return STRICT JSON only, with no markdown "
                    + "formatting and no commentary outside the JSON object. Use exactly this schema: "
                    + "{\"questions\": [{\"id\": string, \"question\": string, \"hint\": string}]}. "
                    + "Return exactly 5 questions. Each hint should be one sentence of practical "
                    + "guidance for answering the question well.";

    /** System prompt instructing the model to return feedback as strict JSON. */
    private static final String FEEDBACK_SYSTEM_PROMPT =
            "You are an expert interview coach. Evaluate the user's answers to a mock interview and "
                    + "return STRICT JSON only, with no markdown formatting and no commentary outside "
                    + "the JSON object. Use exactly this schema: {\"overallScore\": 0-100 integer, "
                    + "\"feedback\": [{\"questionId\": string, \"question\": string, \"answer\": string, "
                    + "\"score\": 0-100 integer, \"feedback\": string, \"suggestions\": [string]}], "
                    + "\"strengths\": [string], \"areasForImprovement\": [string]}. The overallScore is "
                    + "the average of the per-answer scores. Score relevance to the question first: "
                    + "an answer that does not address its question must receive a score of 0-20 "
                    + "regardless of its length or grammar. Be specific, encouraging, and constructive.";

    private final OpenAiChatCompletions chatCompletions;
    private final ObjectMapper objectMapper;

    /**
     * Constructs the provider with the shared Chat Completions client.
     *
     * @param chatCompletions the shared OpenAI-compatible chat client
     * @param objectMapper    the Jackson object mapper for response handling
     */
    public OpenAiMockInterviewProvider(final OpenAiChatCompletions chatCompletions,
                                       final ObjectMapper objectMapper) {
        this.chatCompletions = chatCompletions;
        this.objectMapper = objectMapper;
    }

    /**
     * The provider is configured only when the shared chat client has an
     * API key.
     *
     * @return {@code true} when an API key is present, {@code false} otherwise
     */
    @Override
    public boolean isConfigured() {
        return chatCompletions.isConfigured();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Asks the model to generate five questions for the requested
     * category, parsing the strict JSON response into
     * {@link InterviewQuestion} records. Throws if the response contains
     * no usable questions, so the caller can fall back to the
     * deterministic question bank.
     * </p>
     */
    @Override
    public List<InterviewQuestion> generateQuestions(final InterviewType type) {
        if (!isConfigured()) {
            throw new IllegalStateException("OpenAI mock interview provider is not configured");
        }

        final String userPrompt = "Please generate interview questions for a "
                + type.name().replace('_', ' ').toLowerCase(Locale.ROOT) + " interview round.";

        final String modelContent = chatCompletions.chat(QUESTIONS_SYSTEM_PROMPT, userPrompt, 0.7);
        final List<InterviewQuestion> questions = parseQuestions(modelContent);

        if (questions.isEmpty()) {
            throw new IllegalStateException("AI provider returned no interview questions");
        }

        log.info("AI mock interview questions generated for type={}: count={}",
                type, questions.size());
        return questions;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sends the question/answer pairs to the model, parses the strict
     * JSON feedback, and maps it to a {@link MockInterviewFeedback}.
     * Throws on any provider or parsing error so the caller can fall
     * back to deterministic evaluation.
     * </p>
     */
    @Override
    public MockInterviewFeedback evaluate(final InterviewType type,
                                          final List<InterviewAnswer> answers) {
        if (!isConfigured()) {
            throw new IllegalStateException("OpenAI mock interview provider is not configured");
        }

        final String userPrompt = buildEvaluationPrompt(answers);
        final String modelContent = chatCompletions.chat(FEEDBACK_SYSTEM_PROMPT, userPrompt, 0.3);
        final MockInterviewFeedback feedback = parseFeedback(modelContent, answers);

        log.info("AI mock interview evaluation completed for type={}: overallScore={}",
                type, feedback.overallScore());
        return feedback;
    }

    /**
     * Parses the model's question JSON content into a list of
     * {@link InterviewQuestion} records, applying defensive fallbacks.
     *
     * @param modelContent the JSON content returned by the model
     * @return the parsed questions
     * @throws IllegalStateException if the content cannot be parsed
     */
    private List<InterviewQuestion> parseQuestions(final String modelContent) {
        try {
            final JsonNode root = objectMapper.readTree(modelContent);
            final JsonNode questionsNode = root.path("questions");

            final List<InterviewQuestion> questions = new ArrayList<>();
            if (questionsNode.isArray()) {
                int index = 1;
                for (final JsonNode node : questionsNode) {
                    final String question = node.path("question").asText("");
                    if (question.isBlank()) {
                        continue;
                    }
                    final String hint = node.path("hint").asText(null);
                    questions.add(new InterviewQuestion(
                            node.path("id").asText("q" + index),
                            question.trim(),
                            hint == null || hint.isBlank() ? null : hint.trim()));
                    index++;
                }
            }
            return List.copyOf(questions);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse AI provider response", e);
        }
    }

    /**
     * Parses the model's feedback JSON content into a
     * {@link MockInterviewFeedback}, applying defensive fallbacks and
     * clamping scores into the 0–100 range.
     *
     * @param modelContent the JSON content returned by the model
     * @param answers      the submitted answers (used as a fallback source
     *                     when the model omits question text)
     * @return the parsed feedback
     * @throws IllegalStateException if the content cannot be parsed
     */
    private MockInterviewFeedback parseFeedback(final String modelContent,
                                                final List<InterviewAnswer> answers) {
        try {
            final JsonNode root = objectMapper.readTree(modelContent);

            final List<MockInterviewFeedback.Item> items = new ArrayList<>();
            final JsonNode feedbackNode = root.path("feedback");
            if (feedbackNode.isArray()) {
                for (final JsonNode node : feedbackNode) {
                    final String questionId = node.path("questionId").asText("");
                    final InterviewAnswer matchingAnswer = answers.stream()
                            .filter(answer -> answer.questionId().equals(questionId))
                            .findFirst()
                            .orElse(null);

                    items.add(new MockInterviewFeedback.Item(
                            questionId.isEmpty() && matchingAnswer != null
                                    ? matchingAnswer.questionId() : questionId,
                            node.path("question").asText(
                                    matchingAnswer != null ? matchingAnswer.question() : ""),
                            node.path("answer").asText(
                                    matchingAnswer != null ? matchingAnswer.answer() : ""),
                            clampScore(node.path("score").asInt(0)),
                            node.path("feedback").asText(""),
                            readStringArray(node.path("suggestions"))));
                }
            }

            return new MockInterviewFeedback(
                    clampScore(root.path("overallScore").asInt(computeFallbackScore(items))),
                    items.isEmpty() ? buildFallbackItems(answers) : List.copyOf(items),
                    readStringArray(root.path("strengths")),
                    readStringArray(root.path("areasForImprovement")));
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse AI provider response", e);
        }
    }

    /**
     * Reads a JSON array of strings, ignoring blank entries.
     *
     * @param node the JSON array node
     * @return the list of non-blank strings
     */
    private List<String> readStringArray(final JsonNode node) {
        final List<String> values = new ArrayList<>();
        if (node.isArray()) {
            for (final JsonNode element : node) {
                final String value = element.asText("");
                if (!value.isBlank()) {
                    values.add(value.trim());
                }
            }
        }
        return List.copyOf(values);
    }

    /**
     * Computes the average of the per-item scores as a fallback for a
     * missing overall score.
     */
    private int computeFallbackScore(final List<MockInterviewFeedback.Item> items) {
        if (items.isEmpty()) {
            return 0;
        }
        int sum = 0;
        for (final MockInterviewFeedback.Item item : items) {
            sum += item.score();
        }
        return Math.round((float) sum / items.size());
    }

    /**
     * Builds neutral fallback items when the model returns no feedback
     * array, so the response always contains per-question entries.
     */
    private List<MockInterviewFeedback.Item> buildFallbackItems(final List<InterviewAnswer> answers) {
        return answers.stream()
                .map(answer -> new MockInterviewFeedback.Item(
                        answer.questionId(),
                        answer.question(),
                        answer.answer(),
                        0,
                        "No individual feedback was generated for this answer.",
                        List.of()))
                .toList();
    }

    /**
     * Clamps a score into the valid 0–100 range.
     */
    private int clampScore(final int score) {
        return Math.max(0, Math.min(100, score));
    }

    /**
     * Builds the evaluation prompt containing the question/answer pairs.
     */
    private String buildEvaluationPrompt(final List<InterviewAnswer> answers) {
        final StringBuilder prompt = new StringBuilder();
        prompt.append("Please evaluate the following mock interview answers:\n\n");

        for (final InterviewAnswer answer : answers) {
            prompt.append("Q: ").append(answer.question()).append('\n');
            prompt.append("A: ").append(answer.answer()).append("\n\n");
        }

        prompt.append("Return the strict JSON evaluation for each question and the overall interview.");
        return prompt.toString();
    }

}
