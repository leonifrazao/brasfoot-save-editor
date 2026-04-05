---
phase: 260405-pv4
plan: 01
subsystem: "Web adapters"
tags:
  - config
  - file-upload
  - validation
dependency_graph:
  requires: []
  provides:
    - 500MB max file upload configuration
    - .s22 file format validation
  affects:
    - GlobalExceptionHandler
    - SessionController
tech_stack:
  added: []
  patterns:
    - MultipartConfig
    - ControllerAdvice
key_files:
  created:
    - src/test/java/br/com/saveeditor/brasfoot/adapters/in/web/SessionControllerTest.java
  modified:
    - src/main/resources/application.properties
    - src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/GlobalExceptionHandler.java
    - src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/SessionController.java
decisions:
  - "Reject files without .s22 extension with a 400 Bad Request error"
  - "Handle MaxUploadSizeExceededException in GlobalExceptionHandler to return 413 Payload Too Large"
metrics:
  duration_minutes: 5
  completed_date: "2026-04-05"
---

# Phase 260405-pv4 Plan 01: Increase the multipart upload size limit Summary

Increased multipart upload limits to 500MB and added strict .s22 file format validation.

## Key Decisions

1. Configured Spring `application.properties` to allow 500MB per file and request.
2. Intercepted `MaxUploadSizeExceededException` globally to return a 413 error payload.
3. Implemented a strict check in `SessionController` to verify files end with `.s22` ignoring case.

## Deviations from Plan

None - plan executed exactly as written.

## Self-Check: PASSED
FOUND: src/test/java/br/com/saveeditor/brasfoot/adapters/in/web/SessionControllerTest.java
FOUND: src/main/resources/application.properties
FOUND: src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/GlobalExceptionHandler.java
FOUND: src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/SessionController.java
FOUND: 823b930
FOUND: 24a99c3