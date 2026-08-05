-- DevLaunch interview question bank seed data.
--
-- Populates the interview_questions table with a starter set of questions
-- across all five interview categories. The seed is idempotent: INSERT
-- IGNORE skips any row whose (category, question) pair already exists, so
-- the file can safely run on every application startup. Expanding the bank
-- later only requires adding rows here or through the future admin module —
-- no backend code changes are needed.
--
-- Difficulty mix per category: 7 EASY / 7-8 MEDIUM / 5-6 HARD so a random
-- 3-4-3 interview selection is always satisfiable.

-- HR -----------------------------------------------------------------------
INSERT IGNORE INTO interview_questions (category, question, difficulty, created_at, updated_at) VALUES
('HR', 'Tell me about yourself and your background.', 'EASY', NOW(), NOW()),
('HR', 'What are your greatest strengths and weaknesses?', 'EASY', NOW(), NOW()),
('HR', 'Why do you want to work at this company?', 'EASY', NOW(), NOW()),
('HR', 'Where do you see yourself in five years?', 'EASY', NOW(), NOW()),
('HR', 'Tell me about a hobby or interest outside of work.', 'EASY', NOW(), NOW()),
('HR', 'What do you know about our company and its products?', 'EASY', NOW(), NOW()),
('HR', 'How do you prepare for an important meeting?', 'EASY', NOW(), NOW()),
('HR', 'Describe a time you faced a conflict with a teammate. How did you resolve it?', 'MEDIUM', NOW(), NOW()),
('HR', 'Tell me about a project you are proud of and your role in it.', 'MEDIUM', NOW(), NOW()),
('HR', 'How do you handle working under pressure or tight deadlines?', 'MEDIUM', NOW(), NOW()),
('HR', 'Describe a time you received constructive criticism and how you responded.', 'MEDIUM', NOW(), NOW()),
('HR', 'Why are you leaving your current job?', 'MEDIUM', NOW(), NOW()),
('HR', 'Tell me about a time you showed leadership.', 'MEDIUM', NOW(), NOW()),
('HR', 'How do you prioritise multiple competing tasks?', 'MEDIUM', NOW(), NOW()),
('HR', 'Describe a time you failed at something important. What did you learn?', 'HARD', NOW(), NOW()),
('HR', 'Tell me about a time you disagreed with your manager. How did you handle it?', 'HARD', NOW(), NOW()),
('HR', 'How would you handle a teammate who is not contributing their fair share?', 'HARD', NOW(), NOW()),
('HR', 'Describe a situation where you had to adapt to a significant change.', 'HARD', NOW(), NOW()),
('HR', 'What motivates you during long or repetitive work?', 'HARD', NOW(), NOW()),
('HR', 'Give an example of a time you went above and beyond your job description.', 'HARD', NOW(), NOW());

-- Java ---------------------------------------------------------------------
INSERT IGNORE INTO interview_questions (category, question, difficulty, created_at, updated_at) VALUES
('JAVA', 'What is the difference between JDK, JRE, and JVM?', 'EASY', NOW(), NOW()),
('JAVA', 'What are the primitive data types in Java?', 'EASY', NOW(), NOW()),
('JAVA', 'What is the difference between == and equals() in Java?', 'EASY', NOW(), NOW()),
('JAVA', 'Explain what a constructor is in Java.', 'EASY', NOW(), NOW()),
('JAVA', 'What is the difference between a class and an object?', 'EASY', NOW(), NOW()),
('JAVA', 'What is method overloading in Java?', 'EASY', NOW(), NOW()),
('JAVA', 'What is the difference between a Stack and a Queue?', 'EASY', NOW(), NOW()),
('JAVA', 'Explain the difference between an abstract class and an interface in Java.', 'MEDIUM', NOW(), NOW()),
('JAVA', 'Explain the difference between HashMap and ConcurrentHashMap.', 'MEDIUM', NOW(), NOW()),
('JAVA', 'Explain how Garbage Collection works in Java.', 'MEDIUM', NOW(), NOW()),
('JAVA', 'What is the difference between checked and unchecked exceptions?', 'MEDIUM', NOW(), NOW()),
('JAVA', 'Explain the difference between method overloading and method overriding.', 'MEDIUM', NOW(), NOW()),
('JAVA', 'What is the difference between ArrayList and LinkedList?', 'MEDIUM', NOW(), NOW()),
('JAVA', 'Explain the equals() and hashCode() contract in Java.', 'MEDIUM', NOW(), NOW()),
('JAVA', 'What is the difference between a Set and a List?', 'MEDIUM', NOW(), NOW()),
('JAVA', 'How does the JVM split memory between the stack and the heap, and what causes an OutOfMemoryError?', 'HARD', NOW(), NOW()),
('JAVA', 'Explain how string immutability and the string pool work in Java.', 'HARD', NOW(), NOW()),
('JAVA', 'What are Java streams and how do they differ from collections?', 'HARD', NOW(), NOW()),
('JAVA', 'Explain the difference between synchronized, volatile, and atomic variables.', 'HARD', NOW(), NOW()),
('JAVA', 'How does the Java memory model affect visibility between threads?', 'HARD', NOW(), NOW());

-- Spring Boot ---------------------------------------------------------------
INSERT IGNORE INTO interview_questions (category, question, difficulty, created_at, updated_at) VALUES
('SPRING_BOOT', 'What is Spring Boot and what are its main advantages?', 'EASY', NOW(), NOW()),
('SPRING_BOOT', 'What is the difference between @RestController and @Controller?', 'EASY', NOW(), NOW()),
('SPRING_BOOT', 'What is dependency injection and how does Spring implement it?', 'EASY', NOW(), NOW()),
('SPRING_BOOT', 'What is the purpose of the application.yml file?', 'EASY', NOW(), NOW()),
('SPRING_BOOT', 'What does @SpringBootApplication do?', 'EASY', NOW(), NOW()),
('SPRING_BOOT', 'Explain the difference between @Component, @Service, and @Repository.', 'EASY', NOW(), NOW()),
('SPRING_BOOT', 'What is an annotation in Spring? Give an example.', 'EASY', NOW(), NOW()),
('SPRING_BOOT', 'What is Spring Boot auto-configuration and how does it work?', 'MEDIUM', NOW(), NOW()),
('SPRING_BOOT', 'How would you secure a REST API with Spring Security?', 'MEDIUM', NOW(), NOW()),
('SPRING_BOOT', 'What is the difference between @Autowired and constructor injection?', 'MEDIUM', NOW(), NOW()),
('SPRING_BOOT', 'What is the difference between @RequestMapping and @GetMapping?', 'MEDIUM', NOW(), NOW()),
('SPRING_BOOT', 'What is the Spring IoC container and what is a bean?', 'MEDIUM', NOW(), NOW()),
('SPRING_BOOT', 'What is the difference between a controller and a service class?', 'MEDIUM', NOW(), NOW()),
('SPRING_BOOT', 'What is the role of Spring Data JPA repositories?', 'MEDIUM', NOW(), NOW()),
('SPRING_BOOT', 'Explain the Spring bean lifecycle and the available bean scopes.', 'HARD', NOW(), NOW()),
('SPRING_BOOT', 'What is @Transactional and how does transaction propagation work?', 'HARD', NOW(), NOW()),
('SPRING_BOOT', 'Explain how the Spring Security filter chain works with JWT authentication.', 'HARD', NOW(), NOW()),
('SPRING_BOOT', 'How do you avoid N+1 queries with Spring Data JPA?', 'HARD', NOW(), NOW()),
('SPRING_BOOT', 'How does @ControllerAdvice centralise exception handling?', 'HARD', NOW(), NOW()),
('SPRING_BOOT', 'What are circular dependencies in Spring and how do you resolve them?', 'HARD', NOW(), NOW());

-- SQL ----------------------------------------------------------------------
INSERT IGNORE INTO interview_questions (category, question, difficulty, created_at, updated_at) VALUES
('SQL', 'Explain the difference between INNER JOIN and LEFT JOIN.', 'EASY', NOW(), NOW()),
('SQL', 'What is a primary key?', 'EASY', NOW(), NOW()),
('SQL', 'What is the difference between DELETE and TRUNCATE?', 'EASY', NOW(), NOW()),
('SQL', 'Explain the difference between WHERE and HAVING.', 'EASY', NOW(), NOW()),
('SQL', 'What is a foreign key and why is it used?', 'EASY', NOW(), NOW()),
('SQL', 'What is the difference between a table and a view?', 'EASY', NOW(), NOW()),
('SQL', 'What does SELECT DISTINCT do?', 'EASY', NOW(), NOW()),
('SQL', 'What is an index and when should you use one?', 'MEDIUM', NOW(), NOW()),
('SQL', 'Explain the difference between a primary key, a unique key, and a foreign key.', 'MEDIUM', NOW(), NOW()),
('SQL', 'What is the difference between GROUP BY and ORDER BY?', 'MEDIUM', NOW(), NOW()),
('SQL', 'What is a subquery and when would you use one?', 'MEDIUM', NOW(), NOW()),
('SQL', 'What is the difference between UNION and UNION ALL?', 'MEDIUM', NOW(), NOW()),
('SQL', 'Explain the difference between INNER, LEFT, RIGHT, and FULL OUTER joins.', 'MEDIUM', NOW(), NOW()),
('SQL', 'What is normalisation and why is it important?', 'MEDIUM', NOW(), NOW()),
('SQL', 'What is a transaction and what are the ACID properties?', 'HARD', NOW(), NOW()),
('SQL', 'Explain the difference between clustered and non-clustered indexes.', 'HARD', NOW(), NOW()),
('SQL', 'What is an execution plan and how can it help optimise a query?', 'HARD', NOW(), NOW()),
('SQL', 'Explain the N+1 query problem and how to solve it.', 'HARD', NOW(), NOW()),
('SQL', 'What is a database deadlock and how do you prevent it?', 'HARD', NOW(), NOW()),
('SQL', 'Explain the difference between a correlated and a non-correlated subquery.', 'HARD', NOW(), NOW());

-- React --------------------------------------------------------------------
INSERT IGNORE INTO interview_questions (category, question, difficulty, created_at, updated_at) VALUES
('REACT', 'What is React and what are its core concepts?', 'EASY', NOW(), NOW()),
('REACT', 'Explain the difference between props and state in React.', 'EASY', NOW(), NOW()),
('REACT', 'What is JSX and how is it different from HTML?', 'EASY', NOW(), NOW()),
('REACT', 'What is a React component and what types exist?', 'EASY', NOW(), NOW()),
('REACT', 'What is the difference between a functional and a class component?', 'EASY', NOW(), NOW()),
('REACT', 'What does the useState hook do?', 'EASY', NOW(), NOW()),
('REACT', 'What is the purpose of the render method in React?', 'EASY', NOW(), NOW()),
('REACT', 'What are React hooks? Explain useState and useEffect.', 'MEDIUM', NOW(), NOW()),
('REACT', 'What is the difference between controlled and uncontrolled components?', 'MEDIUM', NOW(), NOW()),
('REACT', 'What is the difference between useEffect and useLayoutEffect?', 'MEDIUM', NOW(), NOW()),
('REACT', 'What is the virtual DOM and how does React use it?', 'MEDIUM', NOW(), NOW()),
('REACT', 'What is the difference between useMemo and useCallback?', 'MEDIUM', NOW(), NOW()),
('REACT', 'What is a key in React lists and why is it important?', 'MEDIUM', NOW(), NOW()),
('REACT', 'How does React handle events?', 'MEDIUM', NOW(), NOW()),
('REACT', 'Explain the concept of the virtual DOM and reconciliation.', 'HARD', NOW(), NOW()),
('REACT', 'How does React handle performance optimisation?', 'HARD', NOW(), NOW()),
('REACT', 'Explain the rules of hooks and why they matter.', 'HARD', NOW(), NOW()),
('REACT', 'What is React context and when should you use it?', 'HARD', NOW(), NOW()),
('REACT', 'Explain how React batches state updates.', 'HARD', NOW(), NOW()),
('REACT', 'What is a higher-order component and when would you use one?', 'HARD', NOW(), NOW());
