# Technology Stack

**Project:** Brasfoot Save Editor API
**Researched:** 2026-04-05

## Recommended Stack

### Core Framework
| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| Java | 21 LTS | Programming Language | Standard LTS version in 2025/2026. Offers Virtual Threads, Pattern Matching, and Record patterns which simplify Domain modeling in Hexagonal Architecture. |
| Spring Boot | 3.4.x | App Framework | Modern standard for Java REST APIs. Provides embeddable web server, auto-configuration, and robust dependency injection. |
| Maven | 3.9+ | Build Tool | Required by project constraints. Standard build lifecycle and dependency management. |

### API & Delivery (Primary Adapters)
| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| Spring Web | 3.4.x | REST API | Core Spring module for building RESTful endpoints (`@RestController`), handling `MultipartFile` uploads, and downloading bytes. |
| Springdoc OpenAPI | 2.8.x | API Documentation | Replaces deprecated Springfox. Auto-generates Swagger UI and OpenAPI 3.0 specs from Spring `@RestController` annotations. Crucial for UI-less backend consumption. |
| Caffeine Cache | 3.1.x | In-Memory State Management | High-performance, near-optimal caching library for Java. Ideal for temporarily storing uploaded save files in-memory (mapped to a session/edit token) between API requests without needing a persistent datastore. |

### Architecture & Validation
| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| ArchUnit | 1.3.x | Architecture Enforcement | Automatically tests and enforces Hexagonal Architecture boundaries (e.g., ensuring Domain never imports Web/Adapter classes). |
| Spring Boot Validation | 3.4.x | Input Validation | Validates incoming JSON payloads and parameters at the REST controller edge using Jakarta Bean Validation (`@Valid`, `@NotNull`). |

### Testing
| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| JUnit Jupiter | 5.11.x | Core Testing | Standard test framework for Java. |
| Mockito | 5.14.x | Mocking | Essential for mocking Ports when testing Domain logic in isolation. |
| AssertJ | 3.26.x | Fluent Assertions | Provides highly readable, fluent test assertions (`assertThat()`) compared to standard JUnit asserts. |

## Alternatives Considered

| Category | Recommended | Alternative | Why Not |
|----------|-------------|-------------|---------|
| State Management | Caffeine Cache | Spring Session (Redis) | Project requires no database and strict in-memory edits. Redis introduces unnecessary external infrastructure overhead. |
| API Docs | Springdoc OpenAPI | Springfox | Springfox is completely dead and incompatible with Spring Boot 3 / Jakarta EE. |
| Data Mapping | Manual Mapping (Constructors/Records) | MapStruct | MapStruct adds build-time complexity. For a Hexagonal architecture dealing with binary save game state, manual mappers or standard records often provide clearer port boundary translations without magic. |
| CLI / UI | None | Spring Shell | Explicitly out of scope per PROJECT.md constraints (migrating away from it). |

## Anti-Patterns & What NOT To Use

1. **JPA/Hibernate / Spring Data JPA**: The application relies on purely in-memory editing of a binary save file. Using database ORMs adds heavy, unused dependencies.
2. **Springfox**: Abandoned. Always use Springdoc OpenAPI for Spring Boot 3.
3. **Lombok**: While popular, Java 21 `Record` classes solve the boilerplate problem for DTOs and Domain models elegantly without bytecode manipulation and IDE plugin dependencies. Prefer native Java 21 features for clean domain modeling.
4. **Leaking Domain into Adapters**: REST Controllers must not directly interact with or return internal Domain classes. Always map through Input/Output Ports and DTOs.

## Installation

```xml
<!-- In pom.xml -->

<!-- Update Java Version -->
<properties>
    <java.version>21</java.version>
</properties>

<!-- Core Web & Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Cache for In-Memory Session -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>

<!-- API Docs -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.0</version>
</dependency>

<!-- Testing: ArchUnit -->
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.3.0</version>
    <scope>test</scope>
</dependency>
```

## Sources

- Spring Boot 3.4 Release Notes (HIGH confidence)
- ArchUnit documentation for Hexagonal Architecture enforcement (HIGH confidence)
- Context: .planning/PROJECT.md strict in-memory and REST API requirements (HIGH confidence)
