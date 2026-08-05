package com.devlaunch.service.ai;

import com.devlaunch.entity.enums.InterviewType;
import com.devlaunch.service.interfaces.InterviewQuestionBankService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Deterministic, rule-based mock interview provider.
 * <p>
 * Loads a random, balanced set of questions from the database-backed
 * question bank for each interview category and evaluates answers using
 * transparent heuristics. Meaningless input
 * (empty, gibberish, repeated filler, or explicit "I don't know" replies)
 * is scored at the bottom of the scale. Every other answer is first
 * scored in two stages. First <em>category relevance</em>: the answer must
 * share concepts with the question or use category-relevant terminology,
 * otherwise it is capped in the 0–10 band regardless of length, so an
 * unrelated but grammatical paragraph cannot outscore a short relevant
 * answer. Second <em>question relevance</em>: the answer must also cover
 * the specific concepts of its own question (for example "useState" and
 * "useEffect" for a hooks question), otherwise it is capped below 20 even
 * when it reuses generic category terminology such as props or state.
 * On-topic answers are then scored from their length (base score) plus
 * bonus points for using category-relevant technical keywords, with
 * partially relevant answers capped in the middle bands. This provider
 * requires no external configuration and keeps the application fully
 * functional when no LLM API key is configured.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class SampleMockInterviewProvider implements MockInterviewProvider {

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

    /** Score ceiling for answers that ignore the question's own concepts. */
    private static final int QUESTION_OFF_TOPIC_CEILING = 18;

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

    /** Category-relevant keywords used to score answers, keyed by category. */
    private static final Map<InterviewType, List<String>> KEYWORDS = new LinkedHashMap<>();

    /**
     * Concepts a strong answer to a bank question should cover, keyed by
     * the exact (normalised) question text. Each list captures the
     * <em>distinctive</em> technical ideas of its question — for example
     * "usestate" and "useeffect" for the hooks question — so that an
     * answer reusing generic category terminology (such as props and
     * state) cannot reach a medium or high score on an unrelated
     * question. Questions not listed here fall back to dynamic keyword
     * extraction from the question text.
     */
    private static final Map<String, List<String>> QUESTION_CONCEPTS = new LinkedHashMap<>();

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

        QUESTION_CONCEPTS.put("explain the difference between props and state in react.",
                List.of("props", "state", "component"));
        QUESTION_CONCEPTS.put("what are react hooks? explain usestate and useeffect.",
                List.of("hooks", "usestate", "useeffect", "state", "effect", "dependency"));
        QUESTION_CONCEPTS.put("explain the concept of the virtual dom and reconciliation.",
                List.of("virtual dom", "virtual", "dom", "reconciliation", "render", "diffing"));
        QUESTION_CONCEPTS.put("what is the difference between controlled and uncontrolled components?",
                List.of("controlled", "uncontrolled", "form", "input", "state", "ref"));
        QUESTION_CONCEPTS.put("how does react handle performance optimisation?",
                List.of("memo", "usememo", "usecallback", "lazy", "memoization", "render"));

        QUESTION_CONCEPTS.put("explain the difference between an abstract class and an interface in java.",
                List.of("abstract", "interface", "class", "inherit", "implement", "default"));
        QUESTION_CONCEPTS.put("what is the difference between == and equals() in java?",
                List.of("equals", "hashcode", "reference", "value", "compare", "identity"));
        QUESTION_CONCEPTS.put("explain how garbage collection works in java.",
                List.of("garbage", "collection", "heap", "generation", "jvm", "memory"));
        QUESTION_CONCEPTS.put("what is the difference between checked and unchecked exceptions?",
                List.of("checked", "unchecked", "exception", "compile", "runtime", "error"));
        QUESTION_CONCEPTS.put("explain the difference between hashmap and concurrenthashmap.",
                List.of("hashmap", "concurrenthashmap", "concurrent", "thread", "lock",
                        "synchroni", "safe"));

        QUESTION_CONCEPTS.put("what is dependency injection and how does spring implement it?",
                List.of("dependency", "injection", "bean", "ioc", "container", "constructor"));
        QUESTION_CONCEPTS.put("explain the difference between @component, @service, and @repository.",
                List.of("component", "service", "repository", "stereotype", "annotation",
                        "controller"));
        QUESTION_CONCEPTS.put("what is spring boot auto-configuration and how does it work?",
                List.of("autoconfiguration", "configuration", "starter", "condition",
                        "enableautoconfiguration", "auto"));
        QUESTION_CONCEPTS.put("how would you secure a rest api with spring security?",
                List.of("security", "jwt", "filter", "authentication", "authorization", "chain"));
        QUESTION_CONCEPTS.put("what is the difference between @restcontroller and @controller?",
                List.of("restcontroller", "controller", "responsebody", "rest", "api", "json"));

        QUESTION_CONCEPTS.put("explain the difference between inner join and left join.",
                List.of("join", "inner", "left", "row", "match", "outer"));
        QUESTION_CONCEPTS.put("what is an index and when should you use one?",
                List.of("index", "btree", "tree", "query", "performance", "write"));
        QUESTION_CONCEPTS.put("explain the difference between where and having.",
                List.of("where", "having", "group", "filter", "aggregate", "rows"));
        QUESTION_CONCEPTS.put("what is a transaction and what are the acid properties?",
                List.of("transaction", "acid", "atomicity", "consistency", "isolation",
                        "durability"));
        QUESTION_CONCEPTS.put("explain the difference between a primary key, a unique key, and a foreign key.",
                List.of("primary", "unique", "foreign", "key", "null", "constraint",
                        "referential"));

        QUESTION_CONCEPTS.put("tell me about yourself and your background.",
                List.of("background", "experience", "role", "career", "team", "project"));
        QUESTION_CONCEPTS.put("why do you want to work at this company?",
                List.of("company", "mission", "product", "goal", "value", "culture"));
        QUESTION_CONCEPTS.put("describe a time you faced a conflict with a teammate. how did you resolve it?",
                List.of("conflict", "teammate", "resolve", "situation", "action", "star"));
        QUESTION_CONCEPTS.put("what are your greatest strengths and weaknesses?",
                List.of("strength", "weakness", "improve", "evidence", "example"));
        QUESTION_CONCEPTS.put("where do you see yourself in five years?",
                List.of("year", "growth", "ambition", "goal", "company", "future"));

        // Remaining bank questions (the seeded question bank is curated
        // alongside the bank text; see the bank service and data.sql).
        QUESTION_CONCEPTS.put("what is react and what are its core concepts?",
                List.of("component", "jsx", "virtual", "state", "props"));
        QUESTION_CONCEPTS.put("what is jsx and how is it different from html?",
                List.of("jsx", "html", "syntax", "javascript", "element"));
        QUESTION_CONCEPTS.put("what is a react component and what types exist?",
                List.of("component", "function", "class", "props", "ui"));
        QUESTION_CONCEPTS.put("what is the difference between a functional and a class component?",
                List.of("functional", "class", "hook", "lifecycle", "state"));
        QUESTION_CONCEPTS.put("what does the usestate hook do?",
                List.of("usestate", "state", "hook", "update", "rerender"));
        QUESTION_CONCEPTS.put("what is the purpose of the render method in react?",
                List.of("render", "dom", "element", "virtual", "component"));
        QUESTION_CONCEPTS.put("what is the virtual dom and how does react use it?",
                List.of("virtual", "dom", "diffing", "reconcil", "render"));
        QUESTION_CONCEPTS.put("what is the difference between useeffect and uselayouteffect?",
                List.of("useeffect", "uselayouteffect", "effect", "layout", "render"));
        QUESTION_CONCEPTS.put("what is the difference between usememo and usecallback?",
                List.of("usememo", "usecallback", "memoiz", "cache", "dependency"));
        QUESTION_CONCEPTS.put("what is a key in react lists and why is it important?",
                List.of("key", "list", "identity", "reorder", "render"));
        QUESTION_CONCEPTS.put("how does react handle events?",
                List.of("event", "handler", "synthetic", "bubble", "callback"));
        QUESTION_CONCEPTS.put("explain the rules of hooks and why they matter.",
                List.of("rules", "hook", "top", "call", "conditional"));
        QUESTION_CONCEPTS.put("what is react context and when should you use it?",
                List.of("context", "provider", "consumer", "prop", "drilling"));
        QUESTION_CONCEPTS.put("explain how react batches state updates.",
                List.of("batch", "update", "event", "render", "automatic"));
        QUESTION_CONCEPTS.put("what is a higher-order component and when would you use one?",
                List.of("higher", "order", "component", "wrapper", "reuse"));

        QUESTION_CONCEPTS.put("what is the difference between jdk, jre, and jvm?",
                List.of("jdk", "jre", "jvm", "compiler", "runtime"));
        QUESTION_CONCEPTS.put("what are the primitive data types in java?",
                List.of("primitive", "int", "boolean", "byte", "float"));
        QUESTION_CONCEPTS.put("explain what a constructor is in java.",
                List.of("constructor", "object", "initialize", "default", "new"));
        QUESTION_CONCEPTS.put("what is the difference between a class and an object?",
                List.of("class", "object", "instance", "template", "instantiate"));
        QUESTION_CONCEPTS.put("what is method overloading in java?",
                List.of("overload", "method", "parameter", "signature", "name"));
        QUESTION_CONCEPTS.put("what is the difference between a stack and a queue?",
                List.of("stack", "queue", "lifo", "fifo", "order"));
        QUESTION_CONCEPTS.put("explain the difference between method overloading and method overriding.",
                List.of("overload", "override", "method", "signature", "inherit"));
        QUESTION_CONCEPTS.put("what is the difference between arraylist and linkedlist?",
                List.of("arraylist", "linkedlist", "index", "node", "list"));
        QUESTION_CONCEPTS.put("explain the equals() and hashcode() contract in java.",
                List.of("equals", "hashcode", "contract", "equal", "object"));
        QUESTION_CONCEPTS.put("what is the difference between a set and a list?",
                List.of("set", "list", "duplicate", "order", "collection"));
        QUESTION_CONCEPTS.put("how does the jvm split memory between the stack and the heap, and what causes an outofmemoryerror?",
                List.of("stack", "heap", "memory", "outofmemory", "jvm"));
        QUESTION_CONCEPTS.put("explain how string immutability and the string pool work in java.",
                List.of("immutable", "string", "pool", "intern", "object"));
        QUESTION_CONCEPTS.put("what are java streams and how do they differ from collections?",
                List.of("stream", "collection", "lazy", "functional", "pipeline"));
        QUESTION_CONCEPTS.put("explain the difference between synchronized, volatile, and atomic variables.",
                List.of("synchronized", "volatile", "atomic", "thread", "memory"));
        QUESTION_CONCEPTS.put("how does the java memory model affect visibility between threads?",
                List.of("memory", "model", "visibility", "thread", "happens"));

        QUESTION_CONCEPTS.put("what is spring boot and what are its main advantages?",
                List.of("boot", "starter", "auto", "embedded", "convention"));
        QUESTION_CONCEPTS.put("what is the purpose of the application.yml file?",
                List.of("application", "yml", "configuration", "property", "profile"));
        QUESTION_CONCEPTS.put("what does @springbootapplication do?",
                List.of("springbootapplication", "configuration", "scan", "autoconfiguration",
                        "annotation"));
        QUESTION_CONCEPTS.put("what is an annotation in spring? give an example.",
                List.of("annotation", "component", "bean", "controller", "service"));
        QUESTION_CONCEPTS.put("what is the difference between @autowired and constructor injection?",
                List.of("autowired", "constructor", "injection", "field", "dependency"));
        QUESTION_CONCEPTS.put("what is the difference between @requestmapping and @getmapping?",
                List.of("requestmapping", "getmapping", "http", "method", "url"));
        QUESTION_CONCEPTS.put("what is the spring ioc container and what is a bean?",
                List.of("ioc", "container", "bean", "instance", "manage"));
        QUESTION_CONCEPTS.put("what is the difference between a controller and a service class?",
                List.of("controller", "service", "business", "request", "logic"));
        QUESTION_CONCEPTS.put("what is the role of spring data jpa repositories?",
                List.of("repository", "jpa", "crud", "persistence", "entity"));
        QUESTION_CONCEPTS.put("explain the spring bean lifecycle and the available bean scopes.",
                List.of("lifecycle", "scope", "bean", "singleton", "prototype"));
        QUESTION_CONCEPTS.put("what is @transactional and how does transaction propagation work?",
                List.of("transactional", "transaction", "propagation", "rollback", "commit"));
        QUESTION_CONCEPTS.put("explain how the spring security filter chain works with jwt authentication.",
                List.of("security", "filter", "chain", "jwt", "authentication"));
        QUESTION_CONCEPTS.put("how do you avoid n+1 queries with spring data jpa?",
                List.of("query", "fetch", "join", "batch", "lazy"));
        QUESTION_CONCEPTS.put("how does @controlleradvice centralise exception handling?",
                List.of("controlleradvice", "exception", "handler", "error", "response"));
        QUESTION_CONCEPTS.put("what are circular dependencies in spring and how do you resolve them?",
                List.of("circular", "dependency", "lazy", "constructor", "resolve"));

        QUESTION_CONCEPTS.put("what is a primary key?",
                List.of("primary", "key", "unique", "identify", "row"));
        QUESTION_CONCEPTS.put("what is the difference between delete and truncate?",
                List.of("delete", "truncate", "rollback", "transaction", "log"));
        QUESTION_CONCEPTS.put("what is a foreign key and why is it used?",
                List.of("foreign", "key", "reference", "constraint", "relate"));
        QUESTION_CONCEPTS.put("what is the difference between a table and a view?",
                List.of("table", "view", "stored", "virtual", "query"));
        QUESTION_CONCEPTS.put("what does select distinct do?",
                List.of("select", "distinct", "duplicate", "unique", "row"));
        QUESTION_CONCEPTS.put("what is the difference between group by and order by?",
                List.of("group", "order", "aggregate", "sort", "having"));
        QUESTION_CONCEPTS.put("what is a subquery and when would you use one?",
                List.of("subquery", "nested", "query", "inner", "outer"));
        QUESTION_CONCEPTS.put("what is the difference between union and union all?",
                List.of("union", "union all", "distinct", "duplicate", "combine"));
        QUESTION_CONCEPTS.put("explain the difference between inner, left, right, and full outer joins.",
                List.of("inner", "left", "right", "outer", "join"));
        QUESTION_CONCEPTS.put("what is normalisation and why is it important?",
                List.of("normalis", "normaliz", "redundancy", "anomaly", "design"));
        QUESTION_CONCEPTS.put("explain the difference between clustered and non-clustered indexes.",
                List.of("clustered", "index", "data", "heap", "separate"));
        QUESTION_CONCEPTS.put("what is an execution plan and how can it help optimise a query?",
                List.of("execution", "plan", "optimis", "cost", "explain"));
        QUESTION_CONCEPTS.put("explain the n+1 query problem and how to solve it.",
                List.of("query", "problem", "fetch", "join", "solve"));
        QUESTION_CONCEPTS.put("what is a database deadlock and how do you prevent it?",
                List.of("deadlock", "lock", "prevent", "transaction", "wait"));
        QUESTION_CONCEPTS.put("explain the difference between a correlated and a non-correlated subquery.",
                List.of("correlated", "subquery", "outer", "row", "independent"));

        QUESTION_CONCEPTS.put("tell me about a hobby or interest outside of work.",
                List.of("hobby", "interest", "outside", "passion", "activity"));
        QUESTION_CONCEPTS.put("what do you know about our company and its products?",
                List.of("company", "product", "research", "mission", "industry"));
        QUESTION_CONCEPTS.put("how do you prepare for an important meeting?",
                List.of("prepare", "meeting", "agenda", "research", "organise"));
        QUESTION_CONCEPTS.put("tell me about a project you are proud of and your role in it.",
                List.of("project", "role", "proud", "contribut", "result"));
        QUESTION_CONCEPTS.put("how do you handle working under pressure or tight deadlines?",
                List.of("pressure", "deadline", "prioriti", "calm", "organise"));
        QUESTION_CONCEPTS.put("describe a time you received constructive criticism and how you responded.",
                List.of("criticism", "feedback", "respond", "improve", "learn"));
        QUESTION_CONCEPTS.put("why are you leaving your current job?",
                List.of("leaving", "current", "career", "growth", "opportunity"));
        QUESTION_CONCEPTS.put("tell me about a time you showed leadership.",
                List.of("lead", "team", "guide", "initiative", "mentor"));
        QUESTION_CONCEPTS.put("how do you prioritise multiple competing tasks?",
                List.of("prioriti", "task", "competing", "urgent", "plan"));
        QUESTION_CONCEPTS.put("describe a time you failed at something important. what did you learn?",
                List.of("fail", "learn", "mistake", "responsibility", "improve"));
        QUESTION_CONCEPTS.put("tell me about a time you disagreed with your manager. how did you handle it?",
                List.of("disagree", "manager", "handle", "respect", "communicat"));
        QUESTION_CONCEPTS.put("how would you handle a teammate who is not contributing their fair share?",
                List.of("teammate", "contribut", "communicat", "support", "resolve"));
        QUESTION_CONCEPTS.put("describe a situation where you had to adapt to a significant change.",
                List.of("adapt", "change", "flexible", "situation", "transition"));
        QUESTION_CONCEPTS.put("what motivates you during long or repetitive work?",
                List.of("motivat", "long", "repetitive", "goal", "purpose"));
        QUESTION_CONCEPTS.put("give an example of a time you went above and beyond your job description.",
                List.of("above", "beyond", "example", "initiative", "responsibility"));
    }

    private final InterviewQuestionBankService questionBankService;

    /**
     * Constructs the provider with the database-backed question bank.
     *
     * @param questionBankService the question bank service used to select
     *                            questions for new interviews
     */
    public SampleMockInterviewProvider(
            final InterviewQuestionBankService questionBankService) {
        this.questionBankService = questionBankService;
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
     * Selects a random, balanced set of up to ten unique questions for
     * the requested category from the database-backed question bank, with
     * database identifiers used as the session-scoped question ids.
     * </p>
     */
    @Override
    public List<InterviewQuestion> generateQuestions(final InterviewType type) {
        return questionBankService.selectForInterview(type);
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
     * and short answers score the floor. Meaningful answers are scored in
     * two stages. Answers sharing no question concepts and no category
     * terminology are capped in the 0–10 band regardless of length.
     * Answers that are on-topic for the category but do not cover the
     * question's own concepts are capped below 20, so a generic props/state
     * answer cannot earn a medium score on a hooks or virtual DOM question.
     * On-topic answers are then banded by relevance — weakly relevant at
     * 20–40, moderately relevant at 50–75 — with only clearly relevant,
     * well-developed answers reaching the 80–100 band.
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
        final List<String> concepts = questionConcepts(question);
        final int questionHits = countQuestionConceptHits(concepts, lower);

        final int relevance = relevanceScore(questionHits, keywordHits);
        if (relevance == 0) {
            return new AnswerAssessment(unrelatedScore(text), true);
        }

        final int lengthScore = lengthScore(text.length());
        final int keywordBonus = keywordBonus(keywordHits);

        if (questionHits < requiredQuestionHits(concepts)) {
            return new AnswerAssessment(
                    clamp(Math.min(QUESTION_OFF_TOPIC_CEILING, lengthScore + keywordBonus)), true);
        }

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
     * Computes a 0–100 relevance score for an answer from how much of the
     * question's own concepts it covers plus how much category-relevant
     * terminology it uses. Question concepts are matched flexibly (see
     * {@link #countQuestionConceptHits(List, String)}) so plurals,
     * inflections, and paraphrases are recognised without an exact match,
     * while the bonus for category keywords rewards answers that use the
     * vocabulary of the interview category.
     *
     * @param questionHits the number of question concepts covered by the answer
     * @param keywordHits  the number of category keywords covered by the answer
     * @return the relevance score in the 0–100 range
     */
    private int relevanceScore(final int questionHits, final int keywordHits) {
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
     * Returns the concepts that a strong answer to the question should
     * cover.
     * <p>
     * Curated per-question lists capture the distinctive technical ideas
     * of each bank question (for example "usestate" and "useeffect" for
     * the hooks question), so an answer cannot earn a medium or high score
     * by reusing generic category terminology on an unrelated question.
     * Questions outside the bank fall back to dynamic keyword extraction
     * from the question text.
     * </p>
     * <p>
     * The curated keys must mirror the exact question-bank text: any
     * mismatch silently falls back to the weaker dynamic extraction, so
     * the bank and this map are always updated together.
     * </p>
     *
     * @param question the question text
     * @return the concepts to require, or an empty list when none apply
     */
    private List<String> questionConcepts(final String question) {
        if (question == null || question.isBlank()) {
            return List.of();
        }
        final String key = question.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        final List<String> curated = QUESTION_CONCEPTS.get(key);
        return curated != null ? curated : questionTerms(question);
    }

    /**
     * Counts how many of the question's concepts appear in the answer,
     * using the same flexible matcher as the category keywords so
     * inflections and paraphrases (for example "controlled inputs" for
     * "controlled") are recognised.
     *
     * @param concepts    the concepts to look for
     * @param lowerAnswer the lowercased answer text
     * @return the number of concepts covered
     */
    private int countQuestionConceptHits(final List<String> concepts, final String lowerAnswer) {
        int hits = 0;
        for (final String concept : concepts) {
            if (matchesKeyword(lowerAnswer, concept)) {
                hits++;
            }
        }
        return hits;
    }

    /**
     * Returns how many question concepts an answer must cover to count as
     * on-topic: at least one for questions with fewer than four concepts,
     * and at least two for richer questions, so that a single shared or
     * generic word (for example "state" in a hooks or controlled-components
     * question) does not count as addressing the question.
     * <p>
     * The fixed threshold of two is a deliberate trade-off: a proportional
     * rule (for example half of the concepts) would reject partial answers
     * that are clearly on-topic but brief, such as a one-concept answer to
     * a three-concept question.
     * </p>
     *
     * @param concepts the question's concepts
     * @return the minimum number of concepts the answer must cover
     */
    private int requiredQuestionHits(final List<String> concepts) {
        if (concepts.size() >= 4) {
            return 2;
        }
        return concepts.isEmpty() ? 0 : 1;
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
