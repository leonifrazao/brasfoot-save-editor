---
phase: 04-add-lombok-and-create-real-domains-with-self-validation-fix-
plan: 04
subsystem: api
tags: [records, package-organization, controllers, ports]
requires:
  - phase: 04-02
    provides: domain validation model updates
  - phase: 04-03
    provides: manager batch use-case and port consistency
provides:
  - Web transport records moved to adapters/in/web/record
  - Application batch command records moved to application/ports/in/record
  - Controller and service imports aligned to record/domain package boundaries
affects: [phase-verification]
tech-stack:
  added: []
  patterns: [domain-vs-record package separation]
key-files:
  created:
    - src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/record/TeamDto.java
    - src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/record/TeamUpdateRequest.java
    - src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/record/PlayerDto.java
    - src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/record/PlayerUpdateRequest.java
    - src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/record/ManagerDto.java
    - src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/record/ManagerUpdateRequest.java
    - src/main/java/br/com/saveeditor/brasfoot/application/ports/in/record/TeamBatchUpdateCommand.java
    - src/main/java/br/com/saveeditor/brasfoot/application/ports/in/record/PlayerBatchUpdateCommand.java
  modified:
    - src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/TeamController.java
    - src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/PlayerController.java
    - src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/ManagerController.java
    - src/main/java/br/com/saveeditor/brasfoot/application/ports/in/UpdateTeamUseCase.java
    - src/main/java/br/com/saveeditor/brasfoot/application/ports/in/UpdatePlayerUseCase.java
    - src/main/java/br/com/saveeditor/brasfoot/application/services/TeamManagementService.java
    - src/main/java/br/com/saveeditor/brasfoot/application/services/PlayerManagementService.java
key-decisions:
  - "Moved batch request records under the same record package boundary as other transport carriers to keep adapter contracts cohesive."
patterns-established:
  - "All transport and command records live in explicit record subpackages; domain package is reserved for behavior-rich domain objects."
requirements-completed: [PH4-04]
duration: 12min
completed: 2026-04-06
---

# Phase 4 Plan 04: Record Package Separation Summary

**Web DTOs and application batch command records were relocated into explicit `record` packages, leaving domain models focused on behavior and invariants.**

## Performance

- **Duration:** 12 min
- **Started:** 2026-04-06T01:41:00Z
- **Completed:** 2026-04-06T01:53:00Z
- **Tasks:** 2
- **Files modified:** 14

## Accomplishments
- Moved Team/Player/Manager web carriers from `adapters.in.web.dto` to `adapters.in.web.record`.
- Moved Team/Player batch command records from `application.ports.in` to `application.ports.in.record`.
- Updated controller, use-case, and service imports to compile with unchanged endpoint contracts.

## Task Commits
1. **Task 1: Move web DTO records into dedicated adapters record package** - `83f6a1a` (refactor)
2. **Task 2: Move application command records into dedicated application record package and rewire imports** - `0d43711` (refactor)

## Files Created/Modified
- `src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/record/*` - Transport request/response records and manager DTO classes.
- `src/main/java/br/com/saveeditor/brasfoot/application/ports/in/record/*BatchUpdateCommand.java` - Input command records for team/player/manager batch flows.
- `src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/*Controller.java` - Rewired imports for moved record packages.
- `src/main/java/br/com/saveeditor/brasfoot/application/ports/in/UpdateTeamUseCase.java` and `UpdatePlayerUseCase.java` - Updated command imports.
- `src/main/java/br/com/saveeditor/brasfoot/application/services/TeamManagementService.java` and `PlayerManagementService.java` - Updated command imports.

## Decisions Made
- Kept manager web transport as mutable class rather than converting to record, preserving setter-based mapping used by existing controller logic.

## Deviations from Plan
None - plan executed exactly as written.

## Issues Encountered
- None.

## Next Phase Readiness
- Domain vs. record package boundaries are now explicit and consistent for verification and future feature additions.

## Self-Check: PASSED
