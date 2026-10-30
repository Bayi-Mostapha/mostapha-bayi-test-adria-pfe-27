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

## 7. Part E Agentic chatbot

## 8. Part F Kafka

## 9. Limitations and possible improvements
