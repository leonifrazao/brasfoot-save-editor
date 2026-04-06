---
phase: "260405-uac"
plan: "01"
subsystem: "API"
tags: ["batch", "teams", "players"]
dependency_graph:
  requires: []
  provides: ["BATCH-01"]
  affects: ["TeamController", "PlayerController", "TeamManagementService", "PlayerManagementService"]
tech_stack:
  added: []
  patterns: ["Hexagonal Architecture", "Command Object", "DTOs"]
key_files:
  created: 
    - "src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/TeamBatchUpdateRequest.java"
    - "src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/PlayerBatchUpdateRequest.java"
    - "src/main/java/br/com/saveeditor/brasfoot/application/ports/in/TeamBatchUpdateCommand.java"
    - "src/main/java/br/com/saveeditor/brasfoot/application/ports/in/PlayerBatchUpdateCommand.java"
  modified:
    - "src/main/java/br/com/saveeditor/brasfoot/application/ports/in/UpdateTeamUseCase.java"
    - "src/main/java/br/com/saveeditor/brasfoot/application/ports/in/UpdatePlayerUseCase.java"
    - "src/main/java/br/com/saveeditor/brasfoot/application/services/TeamManagementService.java"
    - "src/main/java/br/com/saveeditor/brasfoot/application/services/PlayerManagementService.java"
    - "src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/TeamController.java"
    - "src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/PlayerController.java"
key_decisions:
  - "Created Command records in the `application.ports.in` package to pass batch request data to use cases, preventing Hexagonal Architecture violation where ports depended on adapter DTOs."
metrics:
  duration: "4 mins"
  completed_date: "2026-04-05"
---

# Phase 260405-uac Plan 01: Implement the Batch Update API Endpoints Summary

Implemented REST API endpoints for batch updating teams and players, allowing multiple entities to be modified in a single transaction.

## Objectives Achieved
- Created batch update DTOs for `Team` and `Player` requests.
- Extended `UpdateTeamUseCase` and `UpdatePlayerUseCase` ports with batch update methods.
- Implemented efficient batch logic in `TeamManagementService` and `PlayerManagementService` that modifies all requested entities and performs a single save to disk.
- Added `@PatchMapping("/batch")` endpoints in `TeamController` and `PlayerController` with full Swagger annotations.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Hexagonal Architecture Violation Fix**
- **Found during:** Task 3 (mvn clean test)
- **Issue:** Using `TeamBatchUpdateRequest` and `PlayerBatchUpdateRequest` DTOs directly in the `application.ports.in` interfaces violated the architectural rule that application layers must not depend on adapters.
- **Fix:** Created `TeamBatchUpdateCommand` and `PlayerBatchUpdateCommand` records in the `application.ports.in` package and mapped the incoming DTOs to these commands in the controllers before calling the use cases.
- **Files modified:** `TeamController`, `PlayerController`, `UpdateTeamUseCase`, `UpdatePlayerUseCase`, `TeamManagementService`, `PlayerManagementService`. Added two new Command classes.

## Threat Flags

No new threat surface outside of the expected batch update endpoints (mitigations already implemented via robust input validations matching the single-update endpoints).

## Self-Check: PASSED
All required logic correctly compiles, tests pass, and Swagger annotations are present.