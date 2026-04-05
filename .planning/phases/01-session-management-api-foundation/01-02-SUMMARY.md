---
phase: 01-session-management-api-foundation
plan: 02
subsystem: api
tags: [caffeine, cache, domain, ports]

requires:
  - phase: 01-01
    provides: ["Hexagonal Architecture package structure"]
provides:
  - "Session domain model"
  - "UploadSaveUseCase and DownloadSaveUseCase ports"
  - "SessionStatePort, LoadSavePort, and WriteSavePort output ports"
  - "CaffeineSessionAdapter for caching sessions with TTL"
affects: [01-03]

tech-stack:
  added: [caffeine]
  patterns: [Hexagonal Ports, In-Memory Caching]

key-files:
  created: [src/main/java/br/com/saveeditor/brasfoot/domain/Session.java, src/main/java/br/com/saveeditor/brasfoot/application/ports/in/UploadSaveUseCase.java, src/main/java/br/com/saveeditor/brasfoot/adapters/out/cache/CaffeineSessionAdapter.java]
  modified: [pom.xml]

key-decisions:
  - "Used Caffeine for Session caching to provide strict, automatic TTL expiration."
  - "Included EditorShellContext inside Session model to reuse existing state tracking logic per plan instructions."

patterns-established:
  - "Output Ports for Cache and File I/O: isolating framework dependencies from business logic."

requirements-completed: [SESS-01, SESS-02, D-06, D-07]

duration: 5 min
completed: 2026-04-05
---

# Phase 01 Plan 02: Define the Domain Models, Ports, and Cache Adapter Summary

**Defined Session domain entity, UseCases, Output Ports, and an in-memory Caffeine cache adapter.**

## Performance

- **Duration:** 5 min
- **Started:** 2026-04-05T18:13:00Z
- **Completed:** 2026-04-05T18:18:00Z
- **Tasks:** 3
- **Files modified:** 8

## Accomplishments
- Created the `Session` domain model enclosing an ID and the legacy `EditorShellContext`.
- Established `UploadSaveUseCase` and `DownloadSaveUseCase` input ports.
- Defined `SessionStatePort`, `LoadSavePort`, and `WriteSavePort` output ports.
- Implemented `CaffeineSessionAdapter` caching sessions with an absolute TTL of 1 hour to prevent memory leaks.

## Task Commits

Each task was committed atomically:

1. **Task 1: Define Domain Models & Use Cases** - `ab00b5d` (feat)
2. **Task 2: Define Output Ports** - `049a459` (feat)
3. **Task 3: Implement Caffeine Cache Adapter** - `fcca7e7` (feat)

## Files Created/Modified
- `src/main/java/br/com/saveeditor/brasfoot/domain/Session.java` - Domain representation of an editing session.
- `src/main/java/br/com/saveeditor/brasfoot/application/ports/in/UploadSaveUseCase.java` - Interface for uploading a save file.
- `src/main/java/br/com/saveeditor/brasfoot/application/ports/in/DownloadSaveUseCase.java` - Interface for downloading a save file.
- `src/main/java/br/com/saveeditor/brasfoot/application/ports/out/SessionStatePort.java` - Interface for saving/loading sessions.
- `src/main/java/br/com/saveeditor/brasfoot/application/ports/out/LoadSavePort.java` - Interface for parsing byte arrays to state.
- `src/main/java/br/com/saveeditor/brasfoot/application/ports/out/WriteSavePort.java` - Interface for serializing state to byte arrays.
- `pom.xml` - Added Caffeine dependency.
- `src/main/java/br/com/saveeditor/brasfoot/adapters/out/cache/CaffeineSessionAdapter.java` - Implemented `SessionStatePort` using Caffeine cache.

## Decisions Made
- Embedded `EditorShellContext` directly into the `Session` record, maintaining backwards compatibility with the existing shell logic.
- Caffeine cache configures TTL automatically.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
None

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Domain and Ports are defined.
- Session persistence layer (cache) is ready.
- Ready for Plan 01-03 (Use Cases and REST endpoints).

---
*Phase: 01-session-management-api-foundation*
*Completed: 2026-04-05*