# Loader Microservice — Overview

## What it does
The Loader MS is an **ETL (Extract, Transform, Load)** service. Its sole job is to watch three data directories, pick up new files dropped by external platforms (GitHub, Jira, ClickUp), normalize their different field names into one unified schema, and persist the records to MySQL — either automatically every hour or on manual request.

---

## Tech Stack

| Layer | Technology | Why |
|---|---|---|
| Framework | **Spring Boot 3.5** | Bootstraps the app, auto-configures everything |
| Language | **Java 17** | LTS release, supports records, text blocks, switch expressions |
| REST API | **Spring Web (MVC)** | Exposes `POST /loader/scan` and `GET /loader/history` |
| ORM | **Spring Data JPA + Hibernate** | Maps Java entities to MySQL tables, handles DDL automatically |
| Database | **MySQL** | Stores `platform_information` and `scanned_files` tables |
| Scheduling | **Spring Scheduler** | Runs the scan cron `0 0 * * * *` (every hour, no external tools needed) |
| JSON Parsing | **Jackson (ObjectMapper)** | Deserializes the `.json` data files from each provider into DTOs |
| API Docs | **Springdoc OpenAPI (Swagger UI)** | Auto-generates interactive docs at `/swagger-ui.html` |
| Boilerplate | **Lombok** | Generates getters/setters/builders via annotations (`@Data`, `@Builder`, etc.) |
| Messaging | **Spring Kafka** *(declared, not yet wired)* | Ready for publishing events to other microservices later |
| Testing | **JUnit 5 + Mockito** | Unit tests for the service layer using `@TempDir` for real file I/O |
| Build | **Maven + Maven Wrapper** | Reproducible builds without requiring Maven installed globally |

---

## How data flows

```
data/github/   ──┐
data/jira/     ──┤──► LoaderServiceImpl.scanAllProviders()
data/clickup/  ──┘         │
                           ├─ check ScannedFileRepository (skip if already loaded)
                           ├─ parse JSON → provider-specific DTO
                           ├─ map fields → PlatformInformation entity
                           │    (null numbers → 0)
                           ├─ save to platform_information table
                           └─ save to scanned_files table (mark as processed)
```

---

## Key design decisions

- **`ScannedFile` table** — acts as a processing ledger. The service checks this before touching any file, so restarting the app or re-triggering the scan never creates duplicates.
- **Provider-specific DTOs** — each platform has different field names (`devloper_id` in GitHub vs `employeeID` in Jira vs `worker_id` in ClickUp). DTOs isolate that messiness before the unified `PlatformInformation` entity is built.
- **`@EnableScheduling` on main class** — activates Spring's task scheduler with a single annotation; no external cron daemon or message broker needed.
- **`GlobalExceptionHandler`** — catches `FileParsingException`, `IllegalArgumentException`, and generic `Exception`, returning structured JSON error bodies instead of Spring's default HTML error page.
