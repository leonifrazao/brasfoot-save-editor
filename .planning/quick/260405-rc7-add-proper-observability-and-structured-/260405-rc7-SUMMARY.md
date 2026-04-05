---
phase: quick
plan: 260405-rc7
subsystem: logging
tags:
  - observability
  - logging
  - JSON
  - exception-handling
dependency_graph:
  requires: []
  provides:
    - "JSON Logging Configuration"
    - "Global HTTP Request/Response Logging"
    - "Enhanced Exception Handling with Stack Traces"
  affects:
    - "Application Output Format"
    - "Debugging Capabilities"
tech_stack:
  added:
    - "logstash-logback-encoder"
    - "SLF4J"
    - "Logback"
  patterns:
    - "AOP Filter (OncePerRequestFilter)"
    - "Global Exception Handler (@ControllerAdvice)"
key_files:
  created:
    - src/main/resources/logback-spring.xml
    - src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/LoggingFilter.java
  modified:
    - pom.xml
    - src/main/resources/application.properties
    - src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/GlobalExceptionHandler.java
    - src/main/java/br/com/saveeditor/brasfoot/application/services/SessionService.java
    - src/main/java/br/com/saveeditor/brasfoot/application/services/PlayerManagementService.java
    - src/main/java/br/com/saveeditor/brasfoot/application/services/TeamManagementService.java
key_decisions:
  - "Output all application logs in JSON format via LogstashEncoder for structured observability."
  - "Implement LoggingFilter to track HTTP request durations."
  - "Log domain exceptions at WARN level and unexpected exceptions at ERROR level with full stack traces in GlobalExceptionHandler."
metrics:
  tasks_completed: 3/3
  files_modified: 8
  duration_minutes: 5
  completed_at: 2026-04-05T22:48:00Z
---

# Phase Quick Plan 260405-rc7: Add Proper Observability and Structured JSON Logging

**Summary:** Implemented structured JSON logging using LogstashEncoder, added a global HTTP request filter for tracking durations, enhanced exception handling with stack traces, and introduced domain-level observability.

## Completed Work

### Task 1: Add Logstash Encoder and Logback Config
- Added `logstash-logback-encoder` dependency to `pom.xml`.
- Created `logback-spring.xml` configuring `ConsoleAppender` with `LogstashEncoder` to output JSON.
- Added `logging.level.br.com.saveeditor.brasfoot=DEBUG` to `application.properties`.
- **Commit:** `6091c2b`

### Task 2: Implement Request/Response Logging & Exception Stack Traces
- Created `LoggingFilter` extending `OncePerRequestFilter` to log HTTP request metadata and durations.
- Updated `GlobalExceptionHandler` with an SLF4J logger, logging expected exceptions at WARN and unexpected errors at ERROR with full stack traces.
- **Commit:** `dbfb123`

### Task 3: Add Domain-Level Logging
- Instantiated SLF4J loggers in `SessionService`, `PlayerManagementService`, and `TeamManagementService`.
- Added INFO level logs for lifecycle events (session creation, update operations) and DEBUG level logs for state details and data updates.
- **Commit:** `da4129b`

## Deviations from Plan
None - plan executed exactly as written.

## Self-Check: PASSED
All required files were successfully created/modified and are present in the repository with their corresponding commits.
