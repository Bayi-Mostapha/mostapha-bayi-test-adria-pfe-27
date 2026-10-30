# Technical Exam – Microservices Architecture, AI and Full-Stack Development (ADRIA / ENSET)
**Author:** Mostapha Bayi
**Date:** October 30, 2026

## 1. Overview and architecture

### Modules and ports

| Module | Port | Role |
| --- | --- | --- |
| config-service | 8888 | Spring Cloud Config Server, native profile, files in `classpath:/config-repo` |
| discovery-service | 8761 | Eureka server, service registry |
| gateway-service | 8080 | Spring Cloud Gateway, `lb://` routes and discovery locator |
| account-service | 8081 | Business microservice 1 |
| transaction-service | 8082 | Business microservice 2 |
| chatbot-service | 8083 | Spring AI agent and MCP client |
| event-consumer-service | 8084 | Kafka consumer, logs only |
| Kafka (KRaft) | 9092 | Event streaming |
| Angular frontend | 4200 | Web client |

Start order: config, discovery, account, transaction, gateway, chatbot, consumer.

## 2. Prerequisites and how to run

## 3. Part B Design (diagrams)

### System architecture

```mermaid
flowchart LR
    Client["REST client - curl / Swagger"] --> GW["gateway-service :8080"]
    Telegram["Telegram / Discord"] --> GW

    subgraph Business["Business services"]
        ACC["account-service :8081"]
        TXN["transaction-service :8082"]
        BOT["chatbot-service :8083"]
    end

    GW --> ACC
    GW --> TXN
    GW --> BOT

    subgraph Tech["Technical services"]
        EUREKA["Eureka :8761"]
        CONFIG["Config Server :8888"]
    end

    CONSUMER["event-consumer-service :8084"]

    GW -->|registers and discovers| EUREKA
    ACC -->|registers and discovers| EUREKA
    TXN -->|registers and discovers| EUREKA
    BOT -->|registers and discovers| EUREKA
    CONSUMER -->|registers and discovers| EUREKA

    GW -->|loads config| CONFIG
    ACC -->|loads config| CONFIG
    TXN -->|loads config| CONFIG
    BOT -->|loads config| CONFIG
    CONSUMER -->|loads config| CONFIG

    TXN -->|"OpenFeign + Resilience4J circuit breaker"| ACC
    TXN -->|"Kafka topic bank.transactions.created"| KAFKA["Kafka KRaft :9092"]
    KAFKA -->|"consume and log only"| CONSUMER

    BOT -->|LLM| LLM["LLM provider"]
    BOT -->|"MCP client"| MCP1["MCP server in account-service"]
    BOT -->|"MCP client"| MCP2["MCP server in transaction-service"]
```

### Class diagram

```mermaid
classDiagram
    direction LR

    class Account {
        +UUID id
        +String customerReference
        +AccountStatus status
        +String currency
        +double balance
    }

    class AccountStatus {
        <<enumeration>>
        ACTIVE
        BLOCKED
    }

    class Transaction {
        +UUID id
        +UUID accountId
        +BigDecimal amount
        +String currency
        +String merchantCategory
        +String country
        +Instant createdAt
    }

    class AccountRequest
    class AccountResponse
    class TransactionRequest
    class TransactionResponse

    class TransactionCreatedEvent {
        +UUID transactionId
        +UUID accountId
        +BigDecimal amount
        +String currency
        +Instant createdAt
    }

    class AccountController {
        <<REST>>
        +create(request) response
    }
    class AccountGraphQLController {
        <<GraphQL>>
        +account(id) response
    }
    class AccountMcpTools {
        <<MCP>>
        +getAccount(id) response
    }
    class AccountService
    class AccountRepository

    class TransactionController {
        <<REST>>
        +create(request) response
    }
    class TransactionGraphQLController {
        <<GraphQL>>
        +transaction(id) response
    }
    class TransactionMcpTools {
        <<MCP>>
        +getTransaction(id) response
    }
    class TransactionService
    class TransactionRepository

    class AccountClient {
        <<OpenFeign>>
        +getAccount(id) AccountResponse
        +updateBalance(id, amount) AccountResponse
    }
    class AccountClientFallback {
        <<Resilience4J>>
        +getAccount(id) AccountResponse
    }

    class KafkaTransactionPublisher {
        <<Kafka producer>>
        +publish(event)
    }
    class KafkaEventConsumer {
        <<Kafka consumer>>
        +onTransactionCreated(event)
    }

    Account --> AccountStatus : status
    AccountRequest ..> AccountResponse : mapped to
    TransactionRequest ..> TransactionResponse : mapped to
    TransactionCreatedEvent ..> Transaction : published after save

    AccountController --> AccountService
    AccountGraphQLController --> AccountService
    AccountMcpTools --> AccountService
    AccountService --> AccountRepository

    TransactionController --> TransactionService
    TransactionGraphQLController --> TransactionService
    TransactionMcpTools --> TransactionService
    TransactionService --> TransactionRepository
    TransactionService --> AccountClient : OpenFeign
    AccountClient --> AccountClientFallback : fallback on failure

    TransactionService --> KafkaTransactionPublisher : after save
    KafkaTransactionPublisher --> KafkaEventConsumer : bank.transactions.created
```

### Transaction creation sequence

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant G as gateway-service :8080
    participant T as transaction-service :8082
    participant A as account-service :8081
    participant K as Kafka topic bank.transactions.created

    C->>G: POST /api/transactions
    G->>T: POST /api/transactions
    Note over T: check amount is greater than 0 and lower than or equal to bank.transaction.max-amount (1000)
    T->>A: GET /api/accounts/{accountId} via OpenFeign
    A-->>T: 200 OK with status, currency and balance
    Note over T: validate status is ACTIVE, currency matches the account, balance is enough
    T->>T: save Transaction in H2
    T->>A: PUT /api/accounts/{accountId}/balance via OpenFeign
    A-->>T: 200 OK with the updated account
    T->>K: publish TransactionCreatedEvent with key = accountId
    K-->>T: acknowledged
    T-->>G: 201 Created with the Transaction body
    G-->>C: 201 Created with the Transaction body
```

## 4. Part C Account-Service

## 5. Part C Transaction-Service, OpenFeign and Resilience4J

## 6. Part D Technical services (Gateway, Eureka, Config)

### What was implemented

**discovery-service (8761) - Eureka server**

- Dependency `spring-cloud-starter-netflix-eureka-server` and `@EnableEurekaServer` on `DiscoveryServiceApplication`.
- `eureka.client.register-with-eureka: false` and `eureka.client.fetch-registry: false`: it is a standalone registry, it must not register with itself.
- It is also a config client: `spring.config.import=optional:configserver:http://localhost:8888`.

**config-service (8888) - Spring Cloud Config Server**

- Dependency `spring-cloud-config-server` and `@EnableConfigServer` on `ConfigServiceApplication`.
- Profile `native` with `spring.cloud.config.server.native.search-locations=classpath:/config-repo`, so configuration is read from the classpath and not from a Git repository.
- `config-service/src/main/resources/config-repo/application.yml` is served to every service as the shared `application` profile:

```yaml
bank:
  transaction:
    max-amount: 1000
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,refresh,circuitbreakers,circuitbreakerevents
```

- It registers in Eureka as `CONFIG-SERVICE`, so a running Eureka is required first.

**gateway-service (8080) - Spring Cloud Gateway (reactive, Netty)**

| Route id | Path predicate | Target |
| --- | --- | --- |
| `account-service-routes` | `/api/accounts/**`, `/api/transfers/**`, `/graphql/accounts` | `lb://account-service` |
| `transaction-service-routes` | `/api/transactions/**`, `/graphql/transactions` | `lb://transaction-service` |
| `chatbot-service-routes` | `/api/chat/**` | `lb://chatbot-service` |

- The discovery locator is enabled with `lower-case-service-id: true`, so a service is also reachable through `/{serviceId}/**`, for example `/config-service/actuator/health`.
- A `LoggingGlobalFilter` (`-1` order) logs every call with its method, path, status, duration and reactive signal (`onComplete` / `onError`). It uses `doFinally`, because a call with no available instance completes with an error signal and would be missed by `then(...)`.
- Technical choices in this Spring Cloud train (2025.0.0): the starter `spring-cloud-starter-gateway` is deprecated, so `spring-cloud-starter-gateway-server-webflux` is used instead, and the gateway properties live under `spring.cloud.gateway.server.webflux.*`. Both choices remove the deprecation and property-rename warnings that were observed at startup.
- Every service sets `spring.application.name` to its module name and imports `optional:configserver:http://localhost:8888`.

### Run commands

Start order: config, discovery, gateway (then the business services).

```bash
.\mvnw.cmd clean package -DskipTests
java -jar config-service/target/config-service-1.0.0.jar
java -jar discovery-service/target/discovery-service-1.0.0.jar
java -jar gateway-service/target/gateway-service-1.0.0.jar
```

### Verification

```bash
curl http://localhost:8761/                       # Eureka dashboard, HTTP 200, <title>Eureka</title>
curl http://localhost:8888/actuator/health        # {"status":"UP"}
curl http://localhost:8888/application/default    # serves bank.transaction.max-amount: 1000
curl http://localhost:8761/eureka/apps            # lists CONFIG-SERVICE and GATEWAY-SERVICE
curl -i http://localhost:8080/api/accounts/123    # 503: route matched, no account-service instance yet
curl -i http://localhost:8080/config-service/actuator/health   # 200 through the discovery locator
```

Expected body of `GET http://localhost:8888/application/default`:

```json
{"name":"application","profiles":["default"],"propertySources":[{"name":"classpath:/config-repo/application.yml","source":{"bank.transaction.max-amount":1000,"eureka.client.service-url.defaultZone":"http://localhost:8761/eureka","management.endpoints.web.exposure.include":"health,info,metrics,prometheus,refresh,circuitbreakers,circuitbreakerevents"}}]}
```

![Eureka dashboard with the registered services](docs/screenshots/04-eureka-dashboard.png)
*Screenshot to add*

![Config Server serving bank.transaction.max-amount](docs/screenshots/05-config-server-application-default.png)
*Screenshot to add*

![Gateway route reaching a service through the discovery locator](docs/screenshots/06-gateway-discovery-locator.png)
*Screenshot to add*

## 7. Part E Agentic chatbot

## 8. Part F Kafka

## 9. Limitations and possible improvements
