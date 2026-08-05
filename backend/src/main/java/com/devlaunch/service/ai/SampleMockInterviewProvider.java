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
        QUESTION_CONCEPTS.put("how do you pass data from a parent component to a child component?",
                List.of("parent", "child", "props", "pass", "data"));
        QUESTION_CONCEPTS.put("what is conditional rendering in react and how is it implemented?",
                List.of("conditional", "render", "if", "ternary", "operator"));
        QUESTION_CONCEPTS.put("how do you render a list of items in react?",
                List.of("list", "render", "map", "array", "item"));
        QUESTION_CONCEPTS.put("how do you attach an event handler to an element in react?",
                List.of("event", "handler", "click", "attach", "synthetic"));
        QUESTION_CONCEPTS.put("what is the children prop in react and when is it useful?",
                List.of("children", "prop", "nested", "content", "pass"));
        QUESTION_CONCEPTS.put("what is a fragment in react and why would you use it?",
                List.of("fragment", "wrapper", "group", "key", "dom"));
        QUESTION_CONCEPTS.put("what is the difference between a react element and a react component?",
                List.of("element", "component", "object", "function", "instance"));
        QUESTION_CONCEPTS.put("what is the purpose of the classname attribute in react?",
                List.of("classname", "class", "css", "attribute", "style"));
        QUESTION_CONCEPTS.put("what is the role of the app component in a react application?",
                List.of("app", "component", "root", "entry", "render"));
        QUESTION_CONCEPTS.put("what is npm and what is it used for in react development?",
                List.of("npm", "package", "install", "dependency", "node"));
        QUESTION_CONCEPTS.put("what is a production build in react and why is it important?",
                List.of("production", "build", "bundle", "minify", "deploy"));
        QUESTION_CONCEPTS.put("what is vite and what is it used for in react projects?",
                List.of("vite", "build", "dev", "server", "bundler"));
        QUESTION_CONCEPTS.put("what are environment variables in react and how do you access them?",
                List.of("environment", "variable", "env", "secret", "config"));
        QUESTION_CONCEPTS.put("what is the difference between a default export and a named export in react?",
                List.of("export", "default", "named", "import", "module"));
        QUESTION_CONCEPTS.put("what is inline styling in react and when would you use it?",
                List.of("inline", "style", "object", "css", "property"));
        QUESTION_CONCEPTS.put("what is the spread operator and how is it used when passing props?",
                List.of("spread", "operator", "props", "copy", "object"));
        QUESTION_CONCEPTS.put("how do you update state in a functional component?",
                List.of("state", "update", "setstate", "function", "usestate"));
        QUESTION_CONCEPTS.put("what is the initial value in usestate and how do you set a default?",
                List.of("initial", "usestate", "default", "value", "state"));
        QUESTION_CONCEPTS.put("how do you pass a function as a prop to a child component?",
                List.of("function", "prop", "callback", "child", "pass"));
        QUESTION_CONCEPTS.put("what is conditional styling in react and how do you apply css classes conditionally?",
                List.of("conditional", "class", "style", "template", "string"));
        QUESTION_CONCEPTS.put("what is the difference between text content and a javascript expression inside jsx?",
                List.of("jsx", "expression", "curly", "brace", "text"));
        QUESTION_CONCEPTS.put("what is reactdom and what does reactdom.createroot() do?",
                List.of("reactdom", "createroot", "render", "mount", "root"));
        QUESTION_CONCEPTS.put("what is the difference between a functional component and a plain function in javascript?",
                List.of("functional", "component", "function", "jsx", "return"));
        QUESTION_CONCEPTS.put("what is the usecontext hook and how does it work?",
                List.of("usecontext", "context", "provider", "consume", "value"));
        QUESTION_CONCEPTS.put("what is the usereducer hook and when would you use it instead of usestate?",
                List.of("usereducer", "reducer", "action", "dispatch", "state"));
        QUESTION_CONCEPTS.put("what is the useref hook and what are its common use cases?",
                List.of("useref", "ref", "mutable", "dom", "value"));
        QUESTION_CONCEPTS.put("what is a custom hook in react and how do you create one?",
                List.of("custom", "hook", "reuse", "function", "logic"));
        QUESTION_CONCEPTS.put("what is lifting state up in react and why is it useful?",
                List.of("lifting", "state", "parent", "sibling", "share"));
        QUESTION_CONCEPTS.put("what is component composition in react and what are its benefits?",
                List.of("composition", "children", "reuse", "combine", "structure"));
        QUESTION_CONCEPTS.put("what is prop drilling and what are the ways to avoid it?",
                List.of("prop", "drilling", "context", "nested", "avoid"));
        QUESTION_CONCEPTS.put("what is react.memo and when should you use it?",
                List.of("memo", "component", "props", "cache", "rerender"));
        QUESTION_CONCEPTS.put("what is the difference between react.memo and usememo?",
                List.of("memo", "usememo", "component", "value", "cache"));
        QUESTION_CONCEPTS.put("what is lazy loading in react and how do you implement code splitting?",
                List.of("lazy", "loading", "code", "splitting", "import"));
        QUESTION_CONCEPTS.put("what is react suspense and what does it do?",
                List.of("suspense", "fallback", "loading", "lazy", "async"));
        QUESTION_CONCEPTS.put("what causes unnecessary re-renders in react and how do you prevent them?",
                List.of("rerender", "render", "state", "props", "optimize"));
        QUESTION_CONCEPTS.put("what is zustand and how does it differ from redux for state management?",
                List.of("zustand", "redux", "store", "state", "hook"));
        QUESTION_CONCEPTS.put("what is react router and what are its main components?",
                List.of("router", "route", "link", "navigation", "url"));
        QUESTION_CONCEPTS.put("what is the difference between browserrouter and hashrouter?",
                List.of("browserrouter", "hashrouter", "url", "history", "hash"));
        QUESTION_CONCEPTS.put("what is the purpose of the routes component in react router?",
                List.of("routes", "route", "match", "element", "path"));
        QUESTION_CONCEPTS.put("what is the difference between link and navlink in react router?",
                List.of("link", "navlink", "active", "class", "navigation"));
        QUESTION_CONCEPTS.put("what is the usenavigate hook and when would you use it?",
                List.of("usenavigate", "navigate", "redirect", "programmatic", "history"));
        QUESTION_CONCEPTS.put("what is the uselocation hook and what information does it provide?",
                List.of("uselocation", "location", "pathname", "query", "state"));
        QUESTION_CONCEPTS.put("how do you define and read route parameters in react router?",
                List.of("route", "parameter", "params", "url", "dynamic"));
        QUESTION_CONCEPTS.put("what is a controlled form in react and how do you manage its state?",
                List.of("controlled", "form", "input", "value", "state"));
        QUESTION_CONCEPTS.put("how do you validate form inputs in react?",
                List.of("validate", "validation", "input", "error", "form"));
        QUESTION_CONCEPTS.put("what is react hook form and what are its benefits?",
                List.of("hook form", "form", "validation", "library", "performance"));
        QUESTION_CONCEPTS.put("how do you handle form submission in react?",
                List.of("submit", "form", "prevent", "default", "handler"));
        QUESTION_CONCEPTS.put("what is redux and what problem does it solve?",
                List.of("redux", "store", "state", "global", "predictable"));
        QUESTION_CONCEPTS.put("what are the core concepts of redux: store, actions, and reducers?",
                List.of("store", "action", "reducer", "dispatch", "state"));
        QUESTION_CONCEPTS.put("what is redux toolkit and how does it simplify redux?",
                List.of("toolkit", "redux", "slice", "boilerplate", "configure"));
        QUESTION_CONCEPTS.put("what is the difference between redux and the context api?",
                List.of("redux", "context", "state", "global", "library"));
        QUESTION_CONCEPTS.put("what is the difference between global state and local state in react?",
                List.of("global", "local", "state", "component", "scope"));
        QUESTION_CONCEPTS.put("how do you fetch data from an api in react?",
                List.of("fetch", "api", "data", "request", "response"));
        QUESTION_CONCEPTS.put("what is the difference between fetch and axios in react?",
                List.of("fetch", "axios", "promise", "intercept", "http"));
        QUESTION_CONCEPTS.put("how do you handle loading states while fetching data in react?",
                List.of("loading", "state", "spinner", "fetch", "render"));
        QUESTION_CONCEPTS.put("how do you handle errors when fetching data in react?",
                List.of("error", "handle", "fetch", "catch", "message"));
        QUESTION_CONCEPTS.put("how do you implement pagination when fetching data in react?",
                List.of("pagination", "page", "limit", "fetch", "offset"));
        QUESTION_CONCEPTS.put("what is the difference between async/await and .then() for api calls?",
                List.of("async", "await", "promise", "then", "syntax"));
        QUESTION_CONCEPTS.put("how do you type props with an interface in react with typescript?",
                List.of("interface", "props", "typescript", "type", "component"));
        QUESTION_CONCEPTS.put("what is the difference between a type alias and an interface in typescript?",
                List.of("type", "alias", "interface", "typescript", "extend"));
        QUESTION_CONCEPTS.put("what is a generic component in react with typescript?",
                List.of("generic", "component", "typescript", "type", "reusable"));
        QUESTION_CONCEPTS.put("what is react.fc and is it still recommended for typing components?",
                List.of("fc", "react", "typescript", "function", "component"));
        QUESTION_CONCEPTS.put("how do you type event handlers in react with typescript?",
                List.of("event", "handler", "type", "typescript", "change"));
        QUESTION_CONCEPTS.put("what is the usestate functional update and when is it useful?",
                List.of("usestate", "functional", "update", "callback", "state"));
        QUESTION_CONCEPTS.put("what is the useeffect dependency array and how does it control execution?",
                List.of("useeffect", "dependency", "array", "effect", "run"));
        QUESTION_CONCEPTS.put("what is the cleanup function in useeffect and when is it needed?",
                List.of("cleanup", "useeffect", "unsubscribe", "listener", "effect"));
        QUESTION_CONCEPTS.put("what is react fiber and how does it enable concurrent rendering?",
                List.of("fiber", "concurrent", "render", "scheduler", "work"));
        QUESTION_CONCEPTS.put("what is concurrent rendering in react 18 and what are its benefits?",
                List.of("concurrent", "render", "interrupt", "priority", "react"));
        QUESTION_CONCEPTS.put("what is the usetransition hook and when would you use it?",
                List.of("usetransition", "transition", "pending", "urgent", "render"));
        QUESTION_CONCEPTS.put("what is the usedeferredvalue hook and how does it differ from usetransition?",
                List.of("usedeferredvalue", "defer", "transition", "value", "render"));
        QUESTION_CONCEPTS.put("what is an error boundary in react and how do you create one?",
                List.of("error", "boundary", "component", "fallback", "catch"));
        QUESTION_CONCEPTS.put("what is a react portal and when would you use it?",
                List.of("portal", "render", "dom", "overlay", "modal"));
        QUESTION_CONCEPTS.put("what is the render props pattern in react?",
                List.of("render", "props", "pattern", "function", "share"));
        QUESTION_CONCEPTS.put("what are compound components and what problem do they solve?",
                List.of("compound", "component", "context", "api", "flexible"));
        QUESTION_CONCEPTS.put("what is the useimperativehandle hook and how does it work with forwardref?",
                List.of("useimperativehandle", "forwardref", "ref", "imperative", "expose"));
        QUESTION_CONCEPTS.put("how do you avoid stale closures in react hooks?",
                List.of("stale", "closure", "hook", "dependency", "capture"));
        QUESTION_CONCEPTS.put("what is server-side rendering in react and what are its benefits?",
                List.of("server", "render", "ssr", "seo", "hydration"));
        QUESTION_CONCEPTS.put("what is the difference between server-side rendering and static site generation?",
                List.of("ssr", "ssg", "static", "server", "render"));
        QUESTION_CONCEPTS.put("how do you optimize the initial load performance of a react application?",
                List.of("optimize", "load", "bundle", "lazy", "performance"));
        QUESTION_CONCEPTS.put("what is the difference between a higher-order component and a custom hook?",
                List.of("hoc", "higher", "order", "hook", "reuse"));

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

        // Java expanded bank (30 EASY / 50 MEDIUM / 20 HARD; curated
        // alongside the second Java block in data.sql).
        QUESTION_CONCEPTS.put("what is java and why is it called a platform independent language?",
                List.of("platform", "independent", "jvm", "bytecode", "write once run anywhere"));
        QUESTION_CONCEPTS.put("what is bytecode in java and how is it generated?",
                List.of("bytecode", "compiler", "jvm", "class file", "source code"));
        QUESTION_CONCEPTS.put("what are the different types of operators available in java?",
                List.of("operator", "arithmetic", "relational", "logical", "assignment"));
        QUESTION_CONCEPTS.put("explain type casting in java. what is the difference between implicit and explicit casting?",
                List.of("casting", "implicit", "explicit", "widening", "narrowing"));
        QUESTION_CONCEPTS.put("what are wrapper classes in java and why are they needed?",
                List.of("wrapper", "primitive", "boxing", "integer", "autoboxing"));
        QUESTION_CONCEPTS.put("what is autoboxing and unboxing in java?",
                List.of("autoboxing", "unboxing", "wrapper", "conversion", "primitive"));
        QUESTION_CONCEPTS.put("what is the difference between string, stringbuilder, and stringbuffer?",
                List.of("string", "stringbuilder", "stringbuffer", "immutable", "mutable"));
        QUESTION_CONCEPTS.put("how do you reverse a string in java without using the reverse method?",
                List.of("reverse", "string", "character", "loop", "char array"));
        QUESTION_CONCEPTS.put("what is an array in java and how do you declare and initialize one?",
                List.of("array", "declare", "initialize", "index", "element"));
        QUESTION_CONCEPTS.put("what are the components of a method signature in java?",
                List.of("method", "signature", "parameter", "return type", "name"));
        QUESTION_CONCEPTS.put("what is the difference between instance variables and static variables?",
                List.of("instance", "static", "variable", "object", "class"));
        QUESTION_CONCEPTS.put("what is a package in java and why do we use packages?",
                List.of("package", "namespace", "import", "directory", "group"));
        QUESTION_CONCEPTS.put("what is encapsulation in java and how is it achieved?",
                List.of("encapsulation", "private", "getter", "setter", "hide"));
        QUESTION_CONCEPTS.put("what is abstraction in java?",
                List.of("abstraction", "abstract", "hide", "implementation", "detail"));
        QUESTION_CONCEPTS.put("what is inheritance in java and what are its types?",
                List.of("inheritance", "extends", "single", "multilevel", "hierarchical"));
        QUESTION_CONCEPTS.put("what is polymorphism in java and what are its two types?",
                List.of("polymorphism", "compile time", "runtime", "overload", "override"));
        QUESTION_CONCEPTS.put("what is the purpose of the tostring method in java?",
                List.of("tostring", "string", "representation", "object", "print"));
        QUESTION_CONCEPTS.put("what is an interface in java?",
                List.of("interface", "implement", "abstract", "contract", "method"));
        QUESTION_CONCEPTS.put("what is the difference between break and continue statements in java?",
                List.of("break", "continue", "loop", "switch", "iteration"));
        QUESTION_CONCEPTS.put("what is the ternary operator in java and when would you use it?",
                List.of("ternary", "operator", "condition", "shorthand", "if else"));
        QUESTION_CONCEPTS.put("what is the difference between public, private, and protected access modifiers?",
                List.of("public", "private", "protected", "access", "modifier"));
        QUESTION_CONCEPTS.put("what is the this keyword in java and how is it used?",
                List.of("this", "reference", "current object", "instance", "constructor"));
        QUESTION_CONCEPTS.put("what is the difference between throw and throws in java?",
                List.of("throw", "throws", "exception", "method", "declaration"));

        QUESTION_CONCEPTS.put("how does hashmap work internally in java?",
                List.of("hashmap", "bucket", "hashcode", "equals", "collision", "node"));
        QUESTION_CONCEPTS.put("what is the difference between hashmap and hashtable in java?",
                List.of("hashmap", "hashtable", "synchronized", "null", "thread"));
        QUESTION_CONCEPTS.put("what is the difference between hashmap and treemap in java?",
                List.of("hashmap", "treemap", "sorted", "order", "key"));
        QUESTION_CONCEPTS.put("what is the difference between linkedhashmap and hashmap in java?",
                List.of("linkedhashmap", "hashmap", "insertion order", "linked", "order"));
        QUESTION_CONCEPTS.put("what is a hashset in java and how does it store unique elements?",
                List.of("hashset", "hashmap", "duplicate", "unique", "hashcode"));
        QUESTION_CONCEPTS.put("what is a priorityqueue in java and when would you use it?",
                List.of("priorityqueue", "priority", "heap", "order", "queue"));
        QUESTION_CONCEPTS.put("what is the difference between comparable and comparator in java?",
                List.of("comparable", "comparator", "compareto", "compare", "sort"));
        QUESTION_CONCEPTS.put("how do you sort a list of custom objects in java?",
                List.of("sort", "list", "comparator", "comparable", "object"));
        QUESTION_CONCEPTS.put("what is an iterator in java and what are its key methods?",
                List.of("iterator", "hasnext", "next", "remove", "collection"));
        QUESTION_CONCEPTS.put("what is a concurrentmodificationexception and when does it occur?",
                List.of("concurrentmodification", "exception", "iterator", "modify", "collection"));
        QUESTION_CONCEPTS.put("what is the difference between fail-fast and fail-safe iterators in java?",
                List.of("fail fast", "fail safe", "iterator", "concurrent", "copy"));
        QUESTION_CONCEPTS.put("what is the difference between collection and collections in java?",
                List.of("collection", "collections", "interface", "utility", "static"));
        QUESTION_CONCEPTS.put("what is the difference between an error and an exception in java?",
                List.of("error", "exception", "throwable", "recover", "runtime"));
        QUESTION_CONCEPTS.put("how do you create a custom exception in java?",
                List.of("custom", "exception", "extends", "exception class", "constructor"));
        QUESTION_CONCEPTS.put("what is try-with-resources in java and how does it work?",
                List.of("try with resources", "autocloseable", "close", "resource", "finally"));
        QUESTION_CONCEPTS.put("what is a lambda expression in java 8?",
                List.of("lambda", "functional interface", "arrow", "anonymous", "expression"));
        QUESTION_CONCEPTS.put("what is a functional interface in java 8 and what are some built-in examples?",
                List.of("functional", "interface", "single abstract method", "lambda", "predicate"));
        QUESTION_CONCEPTS.put("what is a method reference in java 8 and what are its types?",
                List.of("method reference", "lambda", "double colon", "static", "constructor"));
        QUESTION_CONCEPTS.put("what is the optional class in java 8 and what are its benefits?",
                List.of("optional", "null", "empty", "value", "avoid"));
        QUESTION_CONCEPTS.put("what is the difference between intermediate and terminal operations in java streams?",
                List.of("intermediate", "terminal", "lazy", "stream", "operation"));
        QUESTION_CONCEPTS.put("what is the difference between map and flatmap in java streams?",
                List.of("map", "flatmap", "stream", "flatten", "function"));
        QUESTION_CONCEPTS.put("what is a thread in java and what are the ways to create one?",
                List.of("thread", "runnable", "start", "run", "new thread"));
        QUESTION_CONCEPTS.put("what is the difference between a thread and a runnable in java?",
                List.of("thread", "runnable", "interface", "extend", "implement"));
        QUESTION_CONCEPTS.put("what is synchronization in java and why is it important?",
                List.of("synchronized", "lock", "monitor", "thread safety", "mutual exclusion"));
        QUESTION_CONCEPTS.put("what is a deadlock in java and what are the ways to avoid it?",
                List.of("deadlock", "lock", "thread", "wait", "avoid"));
        QUESTION_CONCEPTS.put("what is a race condition in multithreading and how do you prevent it?",
                List.of("race", "condition", "thread", "simultaneous", "synchronized"));
        QUESTION_CONCEPTS.put("what is the difference between sleep() and wait() in java?",
                List.of("sleep", "wait", "monitor", "lock", "interrupted"));
        QUESTION_CONCEPTS.put("what is an executorservice in java and what are its benefits?",
                List.of("executorservice", "executor", "thread pool", "submit", "task"));
        QUESTION_CONCEPTS.put("what is the difference between callable and runnable in java?",
                List.of("callable", "runnable", "return", "future", "exception"));
        QUESTION_CONCEPTS.put("what is a future in java and how is it used with callable?",
                List.of("future", "callable", "get", "result", "async"));
        QUESTION_CONCEPTS.put("what is the object class in java and what methods does it provide?",
                List.of("object", "class", "equals", "hashcode", "tostring"));
        QUESTION_CONCEPTS.put("what is the difference between final, finally, and finalize in java?",
                List.of("final", "finally", "finalize", "constant", "cleanup"));
        QUESTION_CONCEPTS.put("what is the super keyword in java and how is it used?",
                List.of("super", "parent", "constructor", "method", "inheritance"));
        QUESTION_CONCEPTS.put("what is serialization in java and how is it implemented?",
                List.of("serialization", "serializable", "object", "stream", "deserialize"));
        QUESTION_CONCEPTS.put("what is the transient keyword in java and when is it used?",
                List.of("transient", "serialization", "skip", "field", "not serialized"));
        QUESTION_CONCEPTS.put("what is reflection in java and what are its use cases?",
                List.of("reflection", "class", "method", "field", "runtime"));
        QUESTION_CONCEPTS.put("what are generics in java and what are their benefits?",
                List.of("generics", "type safety", "compile time", "parameterized", "class cast"));
        QUESTION_CONCEPTS.put("what are annotations in java and how are they used?",
                List.of("annotation", "metadata", "override", "compile", "runtime"));
        QUESTION_CONCEPTS.put("you are designing a banking application. which collection would you use to store daily transactions and why?",
                List.of("collection", "transaction", "list", "map", "scenario"));
        QUESTION_CONCEPTS.put("how do you count the frequency of each character in a string in java?",
                List.of("frequency", "character", "string", "map", "count"));
        QUESTION_CONCEPTS.put("what is the output of the following code? int a = 5; system.out.println(a++ + ++a);",
                List.of("output", "increment", "postfix", "prefix", "operator"));
        QUESTION_CONCEPTS.put("what is the diamond operator in java and why was it introduced?",
                List.of("diamond", "operator", "generics", "inference", "type"));

        QUESTION_CONCEPTS.put("what is a memory leak in java and how can you prevent it?",
                List.of("memory leak", "prevent", "reference", "gc", "static"));
        QUESTION_CONCEPTS.put("what is the difference between minor gc and major gc in the jvm?",
                List.of("minor", "major", "garbage", "collection", "generation"));
        QUESTION_CONCEPTS.put("explain the difference between weak, soft, and phantom references in java.",
                List.of("weak", "soft", "phantom", "reference", "garbage"));
        QUESTION_CONCEPTS.put("what is a threadlocal variable and when would you use it in java?",
                List.of("threadlocal", "thread", "local", "isolated", "variable"));
        QUESTION_CONCEPTS.put("what is the difference between reentrantlock and the synchronized keyword in java?",
                List.of("reentrantlock", "synchronized", "lock", "unlock", "fairness"));
        QUESTION_CONCEPTS.put("what is a countdownlatch and how is it different from a cyclicbarrier?",
                List.of("countdownlatch", "cyclicbarrier", "await", "barrier", "thread"));
        QUESTION_CONCEPTS.put("what is the producer-consumer problem and how do you solve it using java concurrency?",
                List.of("producer", "consumer", "blockingqueue", "wait", "notify"));
        QUESTION_CONCEPTS.put("how does threadpoolexecutor work internally in java?",
                List.of("threadpoolexecutor", "work queue", "core pool", "maximum", "rejection"));
        QUESTION_CONCEPTS.put("what is the difference between a future and a completablefuture in java?",
                List.of("future", "completablefuture", "callback", "async", "join"));
        QUESTION_CONCEPTS.put("what is type erasure in generics and what problems does it cause?",
                List.of("type erasure", "generics", "compile", "cast", "runtime"));
        QUESTION_CONCEPTS.put("what is the diamond problem in java and how is it resolved?",
                List.of("diamond", "problem", "interface", "default", "ambiguity"));
        QUESTION_CONCEPTS.put("how do you create an immutable class in java and why would you do so?",
                List.of("immutable", "final", "private", "constructor", "getter"));
        QUESTION_CONCEPTS.put("what is the difference between synchronized collections and concurrent collections in java?",
                List.of("synchronized", "concurrent", "collection", "lock", "thread safety"));
        QUESTION_CONCEPTS.put("what is a virtual thread in java and how does it differ from a platform thread?",
                List.of("virtual", "thread", "platform", "lightweight", "jvm"));
        QUESTION_CONCEPTS.put("what is the difference between the extends and super wildcards in java generics?",
                List.of("wildcard", "extends", "super", "bounded", "generic"));

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
        QUESTION_CONCEPTS.put("what is the spring framework and how does spring boot build on top of it?",
                List.of("spring", "framework", "boot", "module", "ecosystem"));
        QUESTION_CONCEPTS.put("what is the difference between @component and @bean in spring?",
                List.of("component", "bean", "annotation", "class", "method"));
        QUESTION_CONCEPTS.put("what is component scanning in spring boot and how does it work?",
                List.of("component", "scan", "package", "annotation", "classpath"));
        QUESTION_CONCEPTS.put("what are spring boot starter dependencies and why are they useful?",
                List.of("starter", "dependency", "auto", "library", "build"));
        QUESTION_CONCEPTS.put("what is the difference between application.properties and application.yml?",
                List.of("properties", "yml", "configuration", "format", "file"));
        QUESTION_CONCEPTS.put("what are spring profiles and when would you use them?",
                List.of("profile", "environment", "configuration", "property", "active"));
        QUESTION_CONCEPTS.put("what is spring boot devtools and what does it do?",
                List.of("devtools", "restart", "reload", "develop", "tool"));
        QUESTION_CONCEPTS.put("what is the default port of a spring boot application and how can you change it?",
                List.of("port", "server", "property", "8080", "tomcat"));
        QUESTION_CONCEPTS.put("what is the purpose of @pathvariable in a rest controller?",
                List.of("pathvariable", "url", "mapping", "value", "parameter"));
        QUESTION_CONCEPTS.put("what is the purpose of @requestparam in a rest controller?",
                List.of("requestparam", "query", "parameter", "value", "url"));
        QUESTION_CONCEPTS.put("what is the difference between get and post requests in rest?",
                List.of("get", "post", "http", "method", "request"));
        QUESTION_CONCEPTS.put("what is the difference between put and patch in a rest api?",
                List.of("put", "patch", "update", "resource", "http"));
        QUESTION_CONCEPTS.put("what is a jpa entity and how is it defined?",
                List.of("entity", "jpa", "table", "annotation", "persistence"));
        QUESTION_CONCEPTS.put("what is the difference between @entity and @table annotations?",
                List.of("entity", "table", "name", "jpa", "mapping"));
        QUESTION_CONCEPTS.put("what is crudrepository in spring data jpa and what methods does it provide?",
                List.of("crud", "repository", "save", "find", "delete"));
        QUESTION_CONCEPTS.put("what is the @value annotation used for in spring?",
                List.of("value", "property", "inject", "placeholder", "configuration"));
        QUESTION_CONCEPTS.put("what is the purpose of @configuration in spring?",
                List.of("configuration", "bean", "annotation", "class", "context"));
        QUESTION_CONCEPTS.put("what is the difference between @getmapping and @postmapping?",
                List.of("getmapping", "postmapping", "http", "method", "annotation"));
        QUESTION_CONCEPTS.put("what is cors and why is it needed in a spring boot application?",
                List.of("cors", "origin", "browser", "security", "cross"));
        QUESTION_CONCEPTS.put("what is responseentity and how is it used?",
                List.of("responseentity", "status", "header", "body", "http"));
        QUESTION_CONCEPTS.put("what does the spring-boot-starter-web dependency include?",
                List.of("starter", "web", "dependency", "mvc", "embedded"));
        QUESTION_CONCEPTS.put("what does springapplication.run() do in a spring boot application?",
                List.of("springapplication", "run", "context", "bootstrap", "main"));
        QUESTION_CONCEPTS.put("what is the purpose of the @transactional annotation?",
                List.of("transactional", "transaction", "commit", "rollback", "database"));
        QUESTION_CONCEPTS.put("how does spring resolve dependencies when multiple beans of the same type exist?",
                List.of("bean", "type", "inject", "qualifier", "primary"));
        QUESTION_CONCEPTS.put("what is the difference between @primary and @qualifier?",
                List.of("primary", "qualifier", "bean", "preference", "inject"));
        QUESTION_CONCEPTS.put("what is the difference between constructor injection and setter injection?",
                List.of("constructor", "setter", "injection", "dependency", "field"));
        QUESTION_CONCEPTS.put("what is the purpose of @bean methods in a @configuration class?",
                List.of("bean", "configuration", "method", "instance", "dependency"));
        QUESTION_CONCEPTS.put("what is the difference between eager and lazy bean initialization in spring?",
                List.of("eager", "lazy", "initialization", "bean", "context"));
        QUESTION_CONCEPTS.put("how do you override a spring boot auto-configuration?",
                List.of("override", "autoconfiguration", "exclude", "property", "configuration"));
        QUESTION_CONCEPTS.put("what is the difference between @enableautoconfiguration and @springbootapplication?",
                List.of("enableautoconfiguration", "springbootapplication", "scan", "configuration", "annotation"));
        QUESTION_CONCEPTS.put("what is the difference between @configurationproperties and @value?",
                List.of("configurationproperties", "value", "property", "bind", "prefix"));
        QUESTION_CONCEPTS.put("how do you use environment variables in a spring boot application?",
                List.of("environment", "variable", "property", "config", "external"));
        QUESTION_CONCEPTS.put("what is the difference between @valid and @validated?",
                List.of("valid", "validated", "validation", "group", "annotation"));
        QUESTION_CONCEPTS.put("how do you handle validation errors in a spring boot rest api?",
                List.of("validation", "error", "binding", "field", "message"));
        QUESTION_CONCEPTS.put("how do you return a custom http status code from a rest controller?",
                List.of("status", "http", "code", "controller", "response"));
        QUESTION_CONCEPTS.put("what is the difference between @responsestatus and responseentity?",
                List.of("responsestatus", "responseentity", "status", "body", "annotation"));
        QUESTION_CONCEPTS.put("how do you handle file uploads in a spring boot application?",
                List.of("upload", "file", "multipart", "request", "storage"));
        QUESTION_CONCEPTS.put("what is the difference between an interceptor and a filter in spring?",
                List.of("interceptor", "filter", "request", "handler", "pre"));
        QUESTION_CONCEPTS.put("how do you configure cors globally in a spring boot application?",
                List.of("cors", "configuration", "origin", "allowed", "global"));
        QUESTION_CONCEPTS.put("how does spring data jpa generate queries from method names?",
                List.of("query", "method", "name", "repository", "derive"));
        QUESTION_CONCEPTS.put("what is the difference between a derived query and a @query annotation?",
                List.of("derived", "query", "annotation", "repository", "method"));
        QUESTION_CONCEPTS.put("what is jpql and how is it different from native sql?",
                List.of("jpql", "native", "sql", "query", "entity"));
        QUESTION_CONCEPTS.put("how do you implement pagination in spring data jpa?",
                List.of("pagination", "page", "pageable", "repository", "query"));
        QUESTION_CONCEPTS.put("how do you sort query results in spring data jpa?",
                List.of("sort", "order", "pageable", "repository", "direction"));
        QUESTION_CONCEPTS.put("what is the difference between lazy and eager fetching in jpa?",
                List.of("lazy", "eager", "fetch", "relationship", "performance"));
        QUESTION_CONCEPTS.put("what are cascade types in jpa and when should you use them?",
                List.of("cascade", "persist", "remove", "entity", "relationship"));
        QUESTION_CONCEPTS.put("how do you define a onetomany relationship in jpa?",
                List.of("onetomany", "manytoone", "relationship", "join", "column"));
        QUESTION_CONCEPTS.put("what is the difference between a unidirectional and bidirectional relationship in jpa?",
                List.of("unidirectional", "bidirectional", "relationship", "owner", "mappedby"));
        QUESTION_CONCEPTS.put("what is a native query in spring data jpa and when would you use it?",
                List.of("native", "query", "sql", "repository", "complex"));
        QUESTION_CONCEPTS.put("how do you hash passwords in spring security?",
                List.of("bcrypt", "password", "hash", "encoder", "security"));
        QUESTION_CONCEPTS.put("what is the difference between authentication and authorization?",
                List.of("authentication", "authorization", "identity", "permission", "security"));
        QUESTION_CONCEPTS.put("what is a userdetailsservice and what is its role in spring security?",
                List.of("userdetails", "load", "user", "database", "security"));
        QUESTION_CONCEPTS.put("what is the purpose of a securityfilterchain bean in spring security?",
                List.of("security", "filter", "chain", "bean", "configure"));
        QUESTION_CONCEPTS.put("what is the difference between jwt and session-based authentication?",
                List.of("jwt", "session", "token", "stateless", "authentication"));
        QUESTION_CONCEPTS.put("what is csrf and when should you disable it in a rest api?",
                List.of("csrf", "token", "security", "cookie", "disable"));
        QUESTION_CONCEPTS.put("what is the difference between @preauthorize and @secured?",
                List.of("preauthorize", "secured", "role", "expression", "annotation"));
        QUESTION_CONCEPTS.put("how do you configure method-level security in spring security?",
                List.of("method", "security", "annotation", "role", "enable"));
        QUESTION_CONCEPTS.put("what are the transaction isolation levels in spring?",
                List.of("isolation", "level", "transaction", "read", "commit"));
        QUESTION_CONCEPTS.put("what happens when an exception is thrown inside a @transactional method?",
                List.of("transactional", "exception", "rollback", "runtime", "commit"));
        QUESTION_CONCEPTS.put("what is the difference between @controlleradvice and @exceptionhandler?",
                List.of("controlleradvice", "exceptionhandler", "exception", "global", "handler"));
        QUESTION_CONCEPTS.put("how do you create a custom exception in spring boot and handle it globally?",
                List.of("custom", "exception", "handler", "global", "error"));
        QUESTION_CONCEPTS.put("how do you test a spring boot controller with mockmvc?",
                List.of("mockmvc", "test", "controller", "perform", "request"));
        QUESTION_CONCEPTS.put("what is the difference between @webmvctest and @springboottest?",
                List.of("webmvctest", "springboottest", "slice", "context", "test"));
        QUESTION_CONCEPTS.put("what is the difference between @mockbean and @mock in tests?",
                List.of("mockbean", "mock", "test", "context", "bean"));
        QUESTION_CONCEPTS.put("what is the purpose of @datajpatest?",
                List.of("datajpatest", "jpa", "repository", "test", "slice"));
        QUESTION_CONCEPTS.put("what is the difference between a monolith and a microservices architecture?",
                List.of("monolith", "microservice", "architecture", "scale", "deploy"));
        QUESTION_CONCEPTS.put("how do you implement caching in spring boot with @cacheable?",
                List.of("cacheable", "cache", "redis", "annotation", "performance"));
        QUESTION_CONCEPTS.put("what is the difference between pessimistic and optimistic locking in jpa?",
                List.of("pessimistic", "optimistic", "lock", "concurrent", "version"));
        QUESTION_CONCEPTS.put("how does spring aop work under the hood for @transactional?",
                List.of("aop", "proxy", "aspect", "advice", "transactional"));
        QUESTION_CONCEPTS.put("what is the difference between jdk dynamic proxy and cglib proxy in spring?",
                List.of("proxy", "dynamic", "cglib", "interface", "class"));
        QUESTION_CONCEPTS.put("how do you implement refresh tokens with jwt in spring security?",
                List.of("refresh", "token", "jwt", "expiry", "access"));
        QUESTION_CONCEPTS.put("what is the difference between oauth2 and jwt-based security?",
                List.of("oauth2", "jwt", "token", "authorization", "flow"));
        QUESTION_CONCEPTS.put("how do you containerise a spring boot application with docker?",
                List.of("docker", "container", "image", "dockerfile", "deploy"));
        QUESTION_CONCEPTS.put("what is the saga pattern and how does it work?",
                List.of("saga", "compensation", "transaction", "microservice", "event"));
        QUESTION_CONCEPTS.put("what is the role of a service registry such as eureka in microservices?",
                List.of("registry", "eureka", "discovery", "service", "microservice"));
        QUESTION_CONCEPTS.put("how does spring cloud config server centralize configuration across microservices?",
                List.of("config", "server", "central", "property", "microservice"));
        QUESTION_CONCEPTS.put("what is the role of an api gateway in a microservices architecture?",
                List.of("gateway", "routing", "load", "microservice", "request"));
        QUESTION_CONCEPTS.put("how do you secure microservice-to-microservice communication?",
                List.of("secure", "communication", "token", "mutual", "tls"));
        QUESTION_CONCEPTS.put("what is the difference between a fat jar and a war in spring boot?",
                List.of("jar", "war", "embedded", "deploy", "packaging"));
        QUESTION_CONCEPTS.put("how do you handle database migrations in a spring boot application?",
                List.of("migration", "flyway", "liquibase", "schema", "version"));

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
        QUESTION_CONCEPTS.put("what is a database and what is a table in sql?",
                List.of("database", "table", "store", "data", "record"));
        QUESTION_CONCEPTS.put("what is the difference between a row and a column in a database table?",
                List.of("row", "column", "record", "field", "table"));
        QUESTION_CONCEPTS.put("what is a candidate key and how does it differ from a primary key?",
                List.of("candidate", "primary", "key", "unique", "minimal"));
        QUESTION_CONCEPTS.put("what is a composite key and when would you use one?",
                List.of("composite", "key", "column", "multiple", "primary"));
        QUESTION_CONCEPTS.put("what is a unique key constraint and why is it used?",
                List.of("unique", "constraint", "key", "duplicate", "column"));
        QUESTION_CONCEPTS.put("what are the different types of constraints in sql?",
                List.of("constraint", "primary", "foreign", "unique", "check"));
        QUESTION_CONCEPTS.put("what is null in sql and how do you check for null values?",
                List.of("null", "value", "missing", "is null", "unknown"));
        QUESTION_CONCEPTS.put("what is the basic syntax of a select statement?",
                List.of("select", "from", "column", "table", "query"));
        QUESTION_CONCEPTS.put("what does the insert statement do and what is its syntax?",
                List.of("insert", "into", "values", "row", "add"));
        QUESTION_CONCEPTS.put("what does the update statement do and what happens without a where clause?",
                List.of("update", "set", "where", "modify", "row"));
        QUESTION_CONCEPTS.put("what does the delete statement do and what happens without a where clause?",
                List.of("delete", "from", "where", "remove", "row"));
        QUESTION_CONCEPTS.put("what is the purpose of the where clause in a query?",
                List.of("where", "filter", "condition", "row", "query"));
        QUESTION_CONCEPTS.put("how do you sort query results with order by in ascending and descending order?",
                List.of("order by", "sort", "asc", "desc", "column"));
        QUESTION_CONCEPTS.put("what does the limit clause do and when would you use it?",
                List.of("limit", "row", "number", "top", "query"));
        QUESTION_CONCEPTS.put("what is the difference between count(*) and count(column_name)?",
                List.of("count", "null", "row", "column", "aggregate"));
        QUESTION_CONCEPTS.put("what is the sum function used for?",
                List.of("sum", "total", "add", "numeric", "aggregate"));
        QUESTION_CONCEPTS.put("what is the avg function and how is it used?",
                List.of("avg", "average", "mean", "numeric", "aggregate"));
        QUESTION_CONCEPTS.put("what are the min and max functions used for?",
                List.of("min", "max", "smallest", "largest", "aggregate"));
        QUESTION_CONCEPTS.put("what is the difference between an aggregate function and a scalar function?",
                List.of("aggregate", "scalar", "function", "row", "single"));
        QUESTION_CONCEPTS.put("what is the case expression in sql and how is it used?",
                List.of("case", "when", "then", "condition", "else"));
        QUESTION_CONCEPTS.put("what does the coalesce function do in sql?",
                List.of("coalesce", "null", "first", "non null", "value"));
        QUESTION_CONCEPTS.put("what are common sql data types for numbers and text?",
                List.of("data type", "integer", "varchar", "text", "number"));
        QUESTION_CONCEPTS.put("what is the purpose of the as alias keyword in sql?",
                List.of("as", "alias", "column", "rename", "query"));
        QUESTION_CONCEPTS.put("what is a self join and when would you use it?",
                List.of("self join", "same", "table", "alias", "join"));
        QUESTION_CONCEPTS.put("what is a cross join and what does it return?",
                List.of("cross join", "cartesian", "product", "row", "combine"));
        QUESTION_CONCEPTS.put("what is the difference between an implicit join and an explicit join?",
                List.of("implicit", "explicit", "join", "syntax", "condition"));
        QUESTION_CONCEPTS.put("how do you write a query to find duplicate rows in a table?",
                List.of("duplicate", "row", "group by", "having", "count"));
        QUESTION_CONCEPTS.put("what is the difference between in and exists in sql?",
                List.of("in", "exists", "subquery", "set", "null"));
        QUESTION_CONCEPTS.put("what is the difference between any and all in sql?",
                List.of("any", "all", "comparison", "subquery", "operator"));
        QUESTION_CONCEPTS.put("how do you use aggregate functions with group by?",
                List.of("group by", "aggregate", "column", "grouping", "count"));
        QUESTION_CONCEPTS.put("how do you filter results after aggregation with having?",
                List.of("having", "aggregate", "filter", "group by", "condition"));
        QUESTION_CONCEPTS.put("what are common string functions in sql and what do they do?",
                List.of("string", "function", "upper", "lower", "concat"));
        QUESTION_CONCEPTS.put("what is the difference between length and char_length?",
                List.of("length", "char length", "character", "byte", "string"));
        QUESTION_CONCEPTS.put("how do you concatenate strings in sql?",
                List.of("concat", "concatenate", "string", "combine", "operator"));
        QUESTION_CONCEPTS.put("what are common date functions in sql?",
                List.of("date", "function", "current", "extract", "format"));
        QUESTION_CONCEPTS.put("what is the difference between a subquery and a join?",
                List.of("subquery", "join", "table", "combine", "query"));
        QUESTION_CONCEPTS.put("how do you find the second highest salary in a table?",
                List.of("second", "highest", "salary", "subquery", "order by"));
        QUESTION_CONCEPTS.put("what is the difference between in and between?",
                List.of("in", "between", "range", "list", "value"));
        QUESTION_CONCEPTS.put("what is the difference between not in and not exists?",
                List.of("not in", "not exists", "null", "subquery", "difference"));
        QUESTION_CONCEPTS.put("what is the like operator and what are its wildcards?",
                List.of("like", "wildcard", "percent", "underscore", "pattern"));
        QUESTION_CONCEPTS.put("what are window functions and how do they differ from aggregate functions?",
                List.of("window", "function", "aggregate", "row", "group"));
        QUESTION_CONCEPTS.put("what is the row_number() function used for?",
                List.of("row number", "rank", "sequence", "window", "partition"));
        QUESTION_CONCEPTS.put("what is the difference between rank() and dense_rank()?",
                List.of("rank", "dense rank", "window", "tie", "number"));
        QUESTION_CONCEPTS.put("what do the lead() and lag() functions do?",
                List.of("lead", "lag", "window", "previous", "next"));
        QUESTION_CONCEPTS.put("what is partition by in window functions and how does it work?",
                List.of("partition by", "window", "group", "function", "row"));
        QUESTION_CONCEPTS.put("how do you write a query to return the top n rows per group?",
                List.of("top", "group", "row number", "rank", "window"));
        QUESTION_CONCEPTS.put("what is a composite index and when is it useful?",
                List.of("composite", "index", "column", "multiple", "query"));
        QUESTION_CONCEPTS.put("how do you create an index in sql?",
                List.of("create index", "index", "column", "table", "performance"));
        QUESTION_CONCEPTS.put("what is the difference between an index scan and an index seek?",
                List.of("index scan", "index seek", "search", "lookup", "performance"));
        QUESTION_CONCEPTS.put("how does an index improve query performance?",
                List.of("index", "performance", "lookup", "speed", "search"));
        QUESTION_CONCEPTS.put("what are the downsides of creating too many indexes?",
                List.of("index", "downside", "write", "storage", "overhead"));
        QUESTION_CONCEPTS.put("what is the difference between commit and rollback?",
                List.of("commit", "rollback", "transaction", "save", "undo"));
        QUESTION_CONCEPTS.put("what is a savepoint and how is it used?",
                List.of("savepoint", "transaction", "rollback", "point", "partial"));
        QUESTION_CONCEPTS.put("what are the different transaction isolation levels?",
                List.of("isolation", "level", "transaction", "read", "consistency"));
        QUESTION_CONCEPTS.put("what is the difference between read committed and repeatable read?",
                List.of("read committed", "repeatable read", "isolation", "lock", "transaction"));
        QUESTION_CONCEPTS.put("what is the first normal form (1nf)?",
                List.of("first normal form", "atomic", "column", "repeat", "value"));
        QUESTION_CONCEPTS.put("what is the second normal form (2nf)?",
                List.of("second normal form", "partial", "dependency", "primary", "key"));
        QUESTION_CONCEPTS.put("what is the third normal form (3nf)?",
                List.of("third normal form", "transitive", "dependency", "non key", "normal"));
        QUESTION_CONCEPTS.put("what is the difference between 3nf and bcnf?",
                List.of("bcnf", "third normal form", "dependency", "normal", "key"));
        QUESTION_CONCEPTS.put("what is denormalisation and when would you use it?",
                List.of("denormalis", "normaliz", "redundancy", "performance", "join"));
        QUESTION_CONCEPTS.put("what is a stored procedure and what are its benefits?",
                List.of("stored procedure", "reusable", "execute", "logic", "database"));
        QUESTION_CONCEPTS.put("what is the difference between a stored procedure and a function?",
                List.of("stored procedure", "function", "return", "value", "call"));
        QUESTION_CONCEPTS.put("what is a trigger in sql and when is it used?",
                List.of("trigger", "event", "insert", "update", "delete"));
        QUESTION_CONCEPTS.put("what is a cte and how is it different from a subquery?",
                List.of("cte", "common table expression", "with", "subquery", "temporary"));
        QUESTION_CONCEPTS.put("what is a recursive cte and how does it work?",
                List.of("recursive", "cte", "anchor", "member", "repeat"));
        QUESTION_CONCEPTS.put("how do you avoid a full table scan in a query?",
                List.of("full table scan", "index", "where", "filter", "performance"));
        QUESTION_CONCEPTS.put("what is the difference between a covering index and a regular index?",
                List.of("covering", "index", "include", "column", "query"));
        QUESTION_CONCEPTS.put("what is index fragmentation and how do you fix it?",
                List.of("fragmentation", "index", "rebuild", "reorganize", "performance"));
        QUESTION_CONCEPTS.put("what is the difference between a bitmap index and a b-tree index?",
                List.of("bitmap", "b tree", "index", "distinct", "range"));
        QUESTION_CONCEPTS.put("what is the difference between pessimistic and optimistic locking in databases?",
                List.of("pessimistic", "optimistic", "lock", "concurrency", "transaction"));
        QUESTION_CONCEPTS.put("what are dirty reads, non-repeatable reads, and phantom reads?",
                List.of("dirty read", "non repeatable read", "phantom read", "isolation", "anomaly"));
        QUESTION_CONCEPTS.put("what is the difference between serializable and snapshot isolation?",
                List.of("serializable", "snapshot", "isolation", "concurrency", "lock"));
        QUESTION_CONCEPTS.put("how do you design a many-to-many relationship in a relational database?",
                List.of("many to many", "junction", "join", "table", "relationship"));
        QUESTION_CONCEPTS.put("what is an entity-relationship diagram and how is it used in database design?",
                List.of("entity relationship", "diagram", "table", "design", "relation"));
        QUESTION_CONCEPTS.put("how do you migrate data between databases safely?",
                List.of("migrate", "data", "backup", "validate", "transform"));
        QUESTION_CONCEPTS.put("what is the difference between vertical and horizontal scaling of a database?",
                List.of("vertical", "horizontal", "scaling", "server", "partition"));
        QUESTION_CONCEPTS.put("what is database sharding and what are its challenges?",
                List.of("sharding", "partition", "horizontal", "distribute", "challenge"));
        QUESTION_CONCEPTS.put("what is the difference between etl and elt?",
                List.of("etl", "elt", "extract", "transform", "load"));
        QUESTION_CONCEPTS.put("what is the difference between oltp and olap systems?",
                List.of("oltp", "olap", "transactional", "analytical", "workload"));
        QUESTION_CONCEPTS.put("how do you implement pagination in sql efficiently?",
                List.of("pagination", "limit", "offset", "page", "performance"));

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
        QUESTION_CONCEPTS.put("what are your short-term career goals?",
                List.of("career", "goal", "short", "plan", "year"));
        QUESTION_CONCEPTS.put("what is your educational background?",
                List.of("education", "degree", "background", "study", "college"));
        QUESTION_CONCEPTS.put("what made you choose your field of study?",
                List.of("field", "study", "choose", "interest", "career"));
        QUESTION_CONCEPTS.put("what is your favourite subject and why?",
                List.of("subject", "favourite", "interest", "academic", "enjoy"));
        QUESTION_CONCEPTS.put("describe your final year project in brief.",
                List.of("project", "final", "college", "team", "build"));
        QUESTION_CONCEPTS.put("what did you learn from your internship?",
                List.of("intern", "learn", "experience", "industry", "skill"));
        QUESTION_CONCEPTS.put("how would you describe yourself in three words?",
                List.of("describe", "word", "personality", "trait", "self"));
        QUESTION_CONCEPTS.put("what are you passionate about?",
                List.of("passion", "interest", "hobby", "drive", "enjoy"));
        QUESTION_CONCEPTS.put("what do you think makes a good team player?",
                List.of("team", "player", "collaborat", "support", "trust"));
        QUESTION_CONCEPTS.put("do you prefer working in a team or working alone?",
                List.of("team", "alone", "prefer", "collaborat", "independent"));
        QUESTION_CONCEPTS.put("how do you stay organised on a daily basis?",
                List.of("organise", "plan", "routine", "task", "list"));
        QUESTION_CONCEPTS.put("what is your biggest strength as a fresher?",
                List.of("strength", "fresher", "learn", "skill", "confident"));
        QUESTION_CONCEPTS.put("what excites you about this role?",
                List.of("role", "excite", "interest", "challenge", "grow"));
        QUESTION_CONCEPTS.put("how do you usually start your day?",
                List.of("morning", "routine", "start", "day", "plan"));
        QUESTION_CONCEPTS.put("how do you keep your skills updated?",
                List.of("skill", "update", "learn", "course", "practice"));
        QUESTION_CONCEPTS.put("why did you choose software development as a career?",
                List.of("software", "career", "code", "technology", "choose"));
        QUESTION_CONCEPTS.put("tell me about a college achievement you are proud of.",
                List.of("achievement", "college", "proud", "award", "effort"));
        QUESTION_CONCEPTS.put("how do you prefer to receive feedback?",
                List.of("feedback", "prefer", "receive", "improve", "open"));
        QUESTION_CONCEPTS.put("what kind of work environment suits you best?",
                List.of("environment", "suit", "culture", "prefer", "team"));
        QUESTION_CONCEPTS.put("what is your dream job?",
                List.of("dream", "job", "career", "aspire", "goal"));
        QUESTION_CONCEPTS.put("why should we hire you?",
                List.of("hire", "skill", "value", "contribute", "confident"));
        QUESTION_CONCEPTS.put("what makes you a good fit for this team?",
                List.of("fit", "team", "skill", "culture", "value"));
        QUESTION_CONCEPTS.put("do you have any questions for us?",
                List.of("question", "ask", "role", "company", "clarify"));
        QUESTION_CONCEPTS.put("describe a time you showed teamwork to complete a task.",
                List.of("team", "task", "collaborat", "complete", "support"));
        QUESTION_CONCEPTS.put("tell me about a time you helped a struggling teammate.",
                List.of("help", "teammate", "struggle", "support", "mentor"));
        QUESTION_CONCEPTS.put("describe a time you had to explain a technical concept to a non-technical person.",
                List.of("explain", "technical", "simple", "communicat", "concept"));
        QUESTION_CONCEPTS.put("how do you handle a missed deadline?",
                List.of("deadline", "miss", "plan", "communicat", "recover"));
        QUESTION_CONCEPTS.put("what would you do if you found a bug in your code after delivery?",
                List.of("bug", "code", "fix", "responsibility", "test"));
        QUESTION_CONCEPTS.put("describe a time you took responsibility for a mistake.",
                List.of("responsibility", "mistake", "own", "learn", "honest"));
        QUESTION_CONCEPTS.put("tell me about a time you had to learn a new skill quickly.",
                List.of("learn", "skill", "quick", "practice", "challenge"));
        QUESTION_CONCEPTS.put("how do you decide what to work on first in the morning?",
                List.of("plan", "task", "morning", "prioriti", "organise"));
        QUESTION_CONCEPTS.put("describe a time you had to work with a difficult colleague.",
                List.of("colleague", "difficult", "work", "patient", "communicat"));
        QUESTION_CONCEPTS.put("how do you handle a situation where you disagree with your team decision?",
                List.of("disagree", "decision", "team", "respect", "explain"));
        QUESTION_CONCEPTS.put("tell me about a time you had to present your work to an audience.",
                List.of("present", "audience", "speak", "confident", "communicat"));
        QUESTION_CONCEPTS.put("describe a time you went out of your way to help a customer or user.",
                List.of("customer", "help", "support", "user", "resolve"));
        QUESTION_CONCEPTS.put("how do you handle feedback that you do not agree with?",
                List.of("feedback", "disagree", "discuss", "respect", "understand"));
        QUESTION_CONCEPTS.put("tell me about a time you had to make a decision with incomplete information.",
                List.of("decision", "information", "incomplete", "judgement", "ask"));
        QUESTION_CONCEPTS.put("how do you manage your time when multiple deadlines fall on the same day?",
                List.of("deadline", "time", "plan", "prioriti", "manage"));
        QUESTION_CONCEPTS.put("describe a time you had to ask for help.",
                List.of("help", "ask", "humble", "support", "learn"));
        QUESTION_CONCEPTS.put("what would you do if your manager assigns you work outside your role?",
                List.of("manager", "role", "task", "flexible", "discuss"));
        QUESTION_CONCEPTS.put("tell me about a time you improved a process at work or college.",
                List.of("improve", "process", "suggest", "efficient", "initiative"));
        QUESTION_CONCEPTS.put("how do you stay calm when things do not go as planned?",
                List.of("calm", "plan", "adapt", "pressure", "handle"));
        QUESTION_CONCEPTS.put("describe a time you had to convince someone to accept your idea.",
                List.of("convince", "persuade", "idea", "present", "reason"));
        QUESTION_CONCEPTS.put("what is your approach to preparing for a job interview?",
                List.of("interview", "prepare", "research", "practice", "confident"));
        QUESTION_CONCEPTS.put("what do you think you can contribute to this company?",
                List.of("contribute", "company", "skill", "value", "role"));
        QUESTION_CONCEPTS.put("what makes our company different from its competitors?",
                List.of("company", "competitor", "different", "research", "product"));
        QUESTION_CONCEPTS.put("what are your salary expectations?",
                List.of("salary", "expectation", "research", "market", "role"));
        QUESTION_CONCEPTS.put("are you willing to relocate?",
                List.of("relocate", "location", "willing", "travel", "move"));
        QUESTION_CONCEPTS.put("what are your plans for higher studies?",
                List.of("higher", "study", "education", "future", "plan"));
        QUESTION_CONCEPTS.put("how do you handle a task that has no clear instructions?",
                List.of("instruction", "unclear", "ask", "figure", "task"));
        QUESTION_CONCEPTS.put("describe a time you received praise and what it meant to you.",
                List.of("praise", "appreciate", "motivate", "effort", "feedback"));
        QUESTION_CONCEPTS.put("describe a time you had to reschedule or reprioritise your plan.",
                List.of("reschedule", "plan", "change", "adapt", "prioriti"));
        QUESTION_CONCEPTS.put("how do you maintain a healthy work-life balance?",
                List.of("balance", "work", "life", "health", "boundary"));
        QUESTION_CONCEPTS.put("tell me about a time you had to manage your emotions in a professional situation.",
                List.of("emotion", "manage", "professional", "situation", "calm"));
        QUESTION_CONCEPTS.put("how do you handle repetitive tasks without losing quality?",
                List.of("repetitive", "task", "quality", "focus", "consistent"));
        QUESTION_CONCEPTS.put("tell me about a time you had to adapt your communication style.",
                List.of("communicat", "style", "adapt", "audience", "message"));
        QUESTION_CONCEPTS.put("how do you ensure every member of your team is heard?",
                List.of("team", "heard", "listen", "include", "respect"));
        QUESTION_CONCEPTS.put("describe a time you made a mistake in front of others.",
                List.of("mistake", "public", "own", "honest", "learn"));
        QUESTION_CONCEPTS.put("describe a time you had to keep your team motivated during a challenging phase.",
                List.of("team", "motivat", "challenge", "phase", "encourage"));
        QUESTION_CONCEPTS.put("tell me about a time you had to negotiate.",
                List.of("negotiate", "discuss", "agree", "settle", "communicat"));
        QUESTION_CONCEPTS.put("what does professionalism mean to you?",
                List.of("professional", "conduct", "respect", "ethical", "responsibility"));
        QUESTION_CONCEPTS.put("how do you handle a colleague who takes credit for your work?",
                List.of("credit", "colleague", "fair", "discuss", "respect"));
        QUESTION_CONCEPTS.put("describe a time you had to work outside your comfort zone.",
                List.of("comfort", "zone", "challenge", "grow", "learn"));
        QUESTION_CONCEPTS.put("how do you react when your idea is rejected?",
                List.of("reject", "idea", "learn", "respond", "improve"));
        QUESTION_CONCEPTS.put("tell me about a time you had to deliver bad news.",
                List.of("bad news", "deliver", "honest", "communicate", "empathis"));
        QUESTION_CONCEPTS.put("what do you think is the most important quality in a colleague?",
                List.of("quality", "colleague", "trust", "honest", "reliable"));
        QUESTION_CONCEPTS.put("describe a time you had to make an unpopular decision.",
                List.of("unpopular", "decision", "stand", "explain", "consequence"));
        QUESTION_CONCEPTS.put("tell me about a time you had to manage a conflict between two teammates.",
                List.of("conflict", "teammate", "mediate", "resolve", "fair"));
        QUESTION_CONCEPTS.put("how would you handle a situation where your manager asks you to do something unethical?",
                List.of("unethical", "manager", "ethics", "refuse", "principle"));
        QUESTION_CONCEPTS.put("describe a time you had to deal with a very difficult customer.",
                List.of("customer", "difficult", "calm", "resolve", "listen"));
        QUESTION_CONCEPTS.put("tell me about a time you had to deliver a project that was falling behind.",
                List.of("project", "behind", "schedule", "plan", "communicat"));
        QUESTION_CONCEPTS.put("how do you handle being criticised publicly?",
                List.of("criticise", "public", "calm", "respond", "professional"));
        QUESTION_CONCEPTS.put("describe a time you had to choose between two equally important tasks.",
                List.of("task", "important", "choose", "prioriti", "balance"));
        QUESTION_CONCEPTS.put("tell me about a time you had to admit you were wrong.",
                List.of("admit", "wrong", "honest", "learn", "apology"));
        QUESTION_CONCEPTS.put("how do you handle a teammate who is being blamed unfairly?",
                List.of("blame", "unfair", "teammate", "defend", "support"));
        QUESTION_CONCEPTS.put("describe a time you had to take a risk and it did not work out.",
                List.of("risk", "fail", "decision", "learn", "outcome"));
        QUESTION_CONCEPTS.put("how do you handle a situation where you disagree with company policy?",
                List.of("policy", "disagree", "raise", "respect", "discuss"));
        QUESTION_CONCEPTS.put("tell me about a time you had to manage up.",
                List.of("manager", "manage", "expect", "communicat", "align"));
        QUESTION_CONCEPTS.put("tell me about a time you had to deliver tough feedback to a teammate.",
                List.of("feedback", "tough", "teammate", "deliver", "honest"));
        QUESTION_CONCEPTS.put("how do you stay true to your values when under pressure?",
                List.of("value", "pressure", "principle", "honest", "integrity"));
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
