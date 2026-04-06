---
phase: 04-add-lombok-and-create-real-domains-with-self-validation-fix-
plan: 03
subsystem: api
tags: [ports, interfaces, batch, manager]
requires:
  - phase: 04-01
    provides: GameDataPort and manager batch use-case baseline
  - phase: 04-02
    provides: domain validation behavior
provides:
  - Port-driven management service dependencies via GameDataPort
  - Concrete manager batch use-case implementation in application service
  - Manager batch endpoint delegated through BatchUpdateManagerUseCase
affects: [04-04]
tech-stack:
  added: []
  patterns: [adapter implements out-port, controller uses use-case interfaces]
key-files:
  created: []
  modified:
    - src/main/java/br/com/saveeditor/brasfoot/service/GameDataService.java
    - src/main/java/br/com/saveeditor/brasfoot/application/services/TeamManagementService.java
    - src/main/java/br/com/saveeditor/brasfoot/application/services/PlayerManagementService.java
    - src/main/java/br/com/saveeditor/brasfoot/application/services/ManagerManagementService.java
    - src/main/java/br/com/saveeditor/brasfoot/application/ports/in/BatchUpdateManagerUseCase.java
    - src/main/java/br/com/saveeditor/brasfoot/application/ports/in/record/ManagerBatchUpdateCommand.java
    - src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/ManagerController.java
key-decisions:
  - "Implemented manager batch as strict all-or-error operation using IllegalArgumentException for invalid IDs to preserve 400 mapping through global exception handling."
patterns-established:
  - "Controllers depend on explicit use-case interfaces for batch operations instead of service concrete types."
requirements-completed: [PH4-03, PH4-04]
duration: 16min
completed: 2026-04-06
---

# Phase 4 Plan 03: Interface Consistency Summary

**Management services now consume `GameDataPort` and manager batch operations are exposed end-to-end through an explicit use-case contract and controller route.**

## Performance

- **Duration:** 16 min
- **Started:** 2026-04-06T01:33:00Z
- **Completed:** 2026-04-06T01:49:00Z
- **Tasks:** 2
- **Files modified:** 7

## Accomplishments
- Converted `GameDataService` into a `GameDataPort` implementation.
- Rewired Team/Player/Manager services to consume `GameDataPort` rather than concrete service classes.
- Added manager batch service method and `/managers/batch` controller endpoint using `BatchUpdateManagerUseCase`.

## Task Commits
1. **Task 1: Move management services to GameDataPort dependency** - `e416af7` (refactor)
2. **Task 2: Implement manager batch use-case contract end-to-end** - `c65d088` (feat)

## Files Created/Modified
- `src/main/java/br/com/saveeditor/brasfoot/service/GameDataService.java` - Implements `GameDataPort`.
- `src/main/java/br/com/saveeditor/brasfoot/application/services/TeamManagementService.java` - Port-based game data access.
- `src/main/java/br/com/saveeditor/brasfoot/application/services/PlayerManagementService.java` - Port-based game data access.
- `src/main/java/br/com/saveeditor/brasfoot/application/services/ManagerManagementService.java` - Implements `BatchUpdateManagerUseCase` with manager command loop.
- `src/main/java/br/com/saveeditor/brasfoot/application/ports/in/BatchUpdateManagerUseCase.java` - Updated to `record` command package.
- `src/main/java/br/com/saveeditor/brasfoot/application/ports/in/record/ManagerBatchUpdateCommand.java` - Manager batch command record in dedicated package.
- `src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/ManagerController.java` - Batch endpoint delegating to use-case interface.

## Decisions Made
- Kept manager batch payload as `List<ManagerBatchUpdateCommand>` for explicit command typing and to avoid hidden DTO-to-command transformations.

## Deviations from Plan
None - plan executed exactly as written.

## Issues Encountered
- None.

## Next Phase Readiness
- Interface and command-level consistency is in place for final package-separation cleanup.

## Self-Check: PASSED
