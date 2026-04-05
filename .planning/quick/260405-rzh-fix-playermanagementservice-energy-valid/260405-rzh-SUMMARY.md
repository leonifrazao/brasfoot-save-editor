---
phase: quick
plan: 1
subsystem: "PlayerManagementService"
tags:
  - bugfix
  - validation
  - exception-handling
dependency_graph:
  requires: []
  provides:
    - "Corrected player energy validation (allows -1)"
    - "Proper exception propagation for validation errors"
  affects:
    - src/main/java/br/com/saveeditor/brasfoot/application/services/PlayerManagementService.java
tech_stack:
  added: []
  patterns:
    - Exception handling
key_files:
  created: []
  modified:
    - src/main/java/br/com/saveeditor/brasfoot/application/services/PlayerManagementService.java
key_decisions:
  - "Changed the lower bound for player energy from 0 to -1 to reflect valid game state."
  - "Added an explicit catch block for IllegalArgumentException in `updatePlayer` to ensure validation errors reach the client instead of being wrapped in generic RuntimeExceptions."
metrics:
  duration_minutes: 2
  tasks_completed: 2
  tasks_total: 2
  date_completed: "2026-04-05"
---

# Phase quick Plan 1: Fix player energy validation and exception handling

## Objective
Fix player energy validation limits and stop swallowing validation exceptions during updates.

## Completed Tasks
- **Task 1: Fix energy validation** - Changed the lower bound check for `energy` from 0 to -1, allowing undefined energy as per the game's mechanics.
- **Task 2: Stop swallowing IllegalArgumentException** - Added a catch block to intercept and rethrow `IllegalArgumentException` so they aren't masked by generic `RuntimeException`s, allowing appropriate 400 Bad Request responses.

## Deviations from Plan
None - plan executed exactly as written.

## Known Stubs
None.

## Threat Flags
None.

## Self-Check: PASSED
- `src/main/java/br/com/saveeditor/brasfoot/application/services/PlayerManagementService.java` exists and has the necessary changes.
- Commits are verified and present in git.