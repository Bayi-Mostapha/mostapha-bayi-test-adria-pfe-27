# Project: Bank microservices platform (technical exam)

## CROSS-CUTTING RULES (mandatory, apply to every task)

1. COMMITS: at the end of EVERY finished task or sub-step that compiles, run in the terminal:
   git add -A && git commit -m "<type>(<module>): <short English message>" && git push origin main
   Types: feat, fix, docs, test, chore, ci. Examples: "feat(account-service): add account CRUD", "docs(readme): add Kafka section".
   Make small, frequent commits (at least one every 10-15 minutes of work), never one big commit. ALWAYS stage everything with `git add -A`: knowledge.md and any file I edited by hand are mine and must be committed too, do not skip them. Never commit secrets (.env, API keys, tokens) or target/, node_modules/, .freebuff/. If a push fails, tell me and keep committing locally. Never rewrite history (no rebase, no force push, no amend of old commits). Ignore the LF/CRLF warnings.

2. README = MY REPORT, IN ENGLISH: README.md at the repo root is my exam report. At the end of every task, update the matching section: what was done, technical choices, run commands, curl examples with expected responses, and a screenshot placeholder like ![Description](docs/screenshots/<file-name>.png) followed by "*Screenshot to add*" while the image does not exist. Structure: 1. Overview and architecture, 2. Prerequisites and how to run, 3. Part B Design (diagrams), 4. Part C Account-Service, 5. Part C Transaction-Service, OpenFeign and Resilience4J, 6. Part D Technical services (Gateway, Eureka, Config), 7. Part E Agentic chatbot, 8. Part F Kafka, 9. Limitations and possible improvements. Never remove or change the README header (title, author, date). I add the screenshots myself in docs/screenshots/.

3. NAME: the student name appears ONLY in the README.md header (given in my first prompt). Never write my name, username or email anywhere else: not in code, comments, config files, poms (no <developers>, <scm> or custom <url>), docs or workflows. Use groupId com.bank and packages com.bank.<service> only.

4. After each task give me: the run command, a verification curl, and the list of screenshots to take with the exact file names to use.

5. Never modify the .freebuff/ folder.

6. All code, comments, API field names, messages, chatbot prompts and docs are in ENGLISH.

## PROJECT

Java 21, Spring Boot 3.5.7, Spring Cloud 2025.0.0, Spring AI 1.0.3, Maven multi-module. When unsure about a Spring AI or Spring Cloud property or class name, check the official docs instead of guessing. If something does not compile, fix it and do not invent APIs.

REPO: root = Maven parent pom (packaging pom, groupId com.bank, artifactId bank-platform).

MODULES AND PORTS:
- config-service 8888 (Spring Cloud Config Server, native profile, files in classpath:/config-repo)
- discovery-service 8761 (Eureka Server)
- gateway-service 8080 (Spring Cloud Gateway, lb:// routes + discovery locator)
- account-service 8081 (business microservice 1)
- transaction-service 8082 (business microservice 2)
- chatbot-service 8083 (Spring AI agent + MCP client)
- event-consumer-service 8084 (Kafka consumer, logs only)
- Kafka (KRaft) 9092
Start order: config, discovery, account, transaction, gateway, chatbot, consumer.

MODELS (keep exactly these fields):
Account: id UUID, customerReference String, status enum ACTIVE|BLOCKED, currency String, balance double.
Transaction: id UUID, accountId UUID, amount BigDecimal, currency String, merchantCategory String, country String, createdAt Instant.

BUSINESS RULES when creating a transaction:
1. amount > 0 strictly AND amount <= bank.transaction.max-amount (value 1000, provided by config-service).
2. The account must exist and be ACTIVE.
3. The transaction currency must equal the account currency.
4. Read the account balance (via Feign) before saving; reject if balance < amount.
5. After saving, update the account balance (via Feign).
6. After successful persistence, publish a Kafka event on bank.transactions.created (key = accountId).
7. A transfer from one account to another must exist (same currency, both ACTIVE, atomic).

MANDATORY LAYERING in each business service (root package com.bank.<service>): domain/, repository/, service/ (ALL business logic), web/rest/, web/graphql/, mcp/. REST controllers, GraphQL controllers and MCP tools all call the SAME service layer and contain no business logic.

CONVENTIONS: Lombok allowed, H2 in-memory DB per business service, Bean Validation, global @RestControllerAdvice returning JSON {timestamp,status,error,message}, SLF4J, constructor injection, registration in Eureka, spring.config.import=optional:configserver:http://localhost:8888, spring-boot-starter-actuator everywhere. Must compile with `mvn clean package`. Concise working code over perfect code.

## MAVEN
Maven is NOT installed globally. Always use the Maven Wrapper at the repo root: `.\mvnw.cmd` (Windows) instead of `mvn`. Example: `.\mvnw.cmd -q clean compile`. Never try to install Maven.

## FIXED VERSIONS (do not research, do not change)
Never browse the web or use a researcher agent to look for versions. Stay on Spring Boot 3.5.x (never Boot 4.x) and Spring AI 1.0.x. If a dependency does not resolve, try the nearest patch version of the same line yourself. Only consult the official docs if a specific class or property name fails to compile.