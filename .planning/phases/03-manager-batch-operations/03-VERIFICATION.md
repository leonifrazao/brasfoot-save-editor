---
phase: 03-manager-batch-operations
verified: 2026-04-05T00:00:00Z
status: gaps_found
score: 2/6 must-haves verified
gaps:
  - truth: "User can submit an array of team updates and get a 207 response if some fail"
    status: failed
    reason: "Batch DTOs (TeamBatchItem, BatchResponse, BatchItemError) don't exist in planned location. TeamController missing POST /batch/teams endpoint."
    artifacts:
      - path: src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/batch/TeamBatchItem.java
        issue: MISSING
      - path: src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/batch/BatchResponse.java
        issue: MISSING
      - path: src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/TeamController.java
        issue: Missing POST /batch/teams endpoint
    missing:
      - "Typed batch DTOs in adapters/in/web/dto/batch/ package"
      - "POST /batch/teams endpoint in TeamController"
      - "batchUpdateTeams method in TeamManagementService"

  - truth: "User can submit an array of player updates and see the successful ones applied"
    status: failed
    reason: "Batch DTOs for players don't exist. PlayerController missing POST /batch/players endpoint."
    artifacts:
      - path: src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/batch/PlayerBatchItem.java
        issue: MISSING
      - path: src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/PlayerController.java
        issue: Missing POST /batch/players endpoint
    missing:
      - "Typed batch DTO for players"
      - "POST /batch/players endpoint in PlayerController"
      - "batchUpdatePlayers method in PlayerManagementService"

  - truth: "User cannot submit more than the configured max batch size (D-03)"
    status: failed
    reason: "No batch size validation found in code. No @Value('${brasfoot.api.batch.max-size:1000}') annotation."
    artifacts:
      - path: src/main/java/br/com/saveeditor/brasfoot/controller/BatchController.java
        issue: No size limit validation before processing
    missing:
      - "@Value property for max batch size"
      - "400 Bad Request rejection for oversized payloads"

  - truth: "Errors explicitly map back to the array index of the request payload (D-06)"
    status: failed
    reason: "BatchOperationResult uses string IDs, not integer array indices. Legacy batch uses {teamId}_{playerId} format."
    artifacts:
      - path: src/main/java/br/com/saveeditor/brasfoot/dto/BatchOperationResult.java
        issue: Uses String id instead of int index
    missing:
      - "Error tracking by array index per D-06"

  - truth: "User can submit an array of manager updates and get a 207 response if some fail"
    status: failed
    reason: "ManagerBatchItem.java missing. ManagerController missing POST /batch/managers. ManagerManagementService missing batchUpdateManagers."
    artifacts:
      - path: src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/batch/ManagerBatchItem.java
        issue: MISSING
      - path: src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/ManagerController.java
        issue: Missing POST /batch/managers endpoint
    missing:
      - "ManagerBatchItem DTO"
      - "POST /batch/managers endpoint"
      - "batchUpdateManagers in ManagerManagementService"

  - truth: "User can update a specific manager's properties (like name, age, reputation, trophies) via API"
    status: partial
    reason: "ManagerManagementService.updateManager() only updates name, confidenceBoard, confidenceFans. Missing age, nationality, reputation, trophies."
    artifacts:
      - path: src/main/java/br/com/saveeditor/brasfoot/application/services/ManagerManagementService.java
        issue: updateManager() missing field mappings for age, nationality, reputation, trophies
      - path: src/main/java/br/com/saveeditor/brasfoot/util/BrasfootConstants.java
        issue: Constants use placeholder names ('age', 'nationality', 'reputation') - not real obfuscated field names
    missing:
      - "Complete field updates in updateManager() method"
      - "Real obfuscated field name mappings for manager attributes"
---

# Phase 03: Manager & Batch Operations Verification Report

**Phase Goal:** Implement Manager entity endpoints and Batch operations for bulk entity editing
**Verified:** 2026-04-05
**Status:** gaps_found
**Score:** 2/6 must-haves verified

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | User can retrieve a list of all managers in a session | ✓ VERIFIED | `ManagerController.getAllManagers()` delegates to `getManagerUseCase.getManagers()` |
| 2 | User can retrieve a specific manager by their index/ID | ✓ VERIFIED | `ManagerController.getManager()` returns single ManagerDto |
| 3 | User can update a specific manager's properties via API | ⚠️ PARTIAL | Only name/confidence fields updated, missing age/nationality/reputation/trophies |
| 4 | Team assignment for a manager remains strictly read-only | ✓ VERIFIED | `teamId` absent from `ManagerUpdateRequest.java` |
| 5 | User can submit array of team updates with 207 response | ✗ FAILED | Batch DTOs missing, TeamController lacks batch endpoint |
| 6 | User can submit array of player updates | ✗ FAILED | Batch DTOs missing, PlayerController lacks batch endpoint |

**Score:** 2/6 truths verified

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `domain/Manager.java` | Domain model | ✓ VERIFIED | All fields present with getters/setters |
| `adapters/in/web/dto/ManagerDto.java` | DTO | ✓ VERIFIED | All manager fields present |
| `adapters/in/web/dto/ManagerUpdateRequest.java` | Update DTO (read-only teamId) | ✓ VERIFIED | Excludes teamId per D-08 |
| `adapters/in/web/ManagerController.java` | GET / PATCH endpoints | ✓ VERIFIED | GET, GET/{id}, PATCH/{id} exist |
| `application/services/ManagerManagementService.java` | Business logic | ⚠️ PARTIAL | Only partial fields updated |
| `dto/batch/BatchResponse.java` | Batch response DTO | ✗ MISSING | Not in planned location |
| `dto/batch/BatchItemError.java` | Error with index | ✗ MISSING | Not in planned location |
| `dto/batch/TeamBatchItem.java` | Team batch item | ✗ MISSING | Not created |
| `dto/batch/PlayerBatchItem.java` | Player batch item | ✗ MISSING | Not created |
| `dto/batch/ManagerBatchItem.java` | Manager batch item | ✗ MISSING | Not created |
| `adapters/in/web/TeamController.java` | POST /batch/teams | ✗ MISSING | No batch endpoint |
| `adapters/in/web/PlayerController.java` | POST /batch/players | ✗ MISSING | No batch endpoint |
| `adapters/in/web/ManagerController.java` | POST /batch/managers | ✗ MISSING | No batch endpoint |

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| ManagerController | ManagerManagementService | GetManagerUseCase, UpdateManagerUseCase injection | ✓ WIRED | Correct constructor injection |
| ManagerController.getAllManagers() | ManagerManagementService.getManagers() | Delegation | ✓ WIRED | Direct method call |
| ManagerController.updateManager() | ManagerManagementService.updateManager() | Delegation | ✓ WIRED | Direct method call |

### Data-Flow Trace (Level 4)

| Artifact | Data Variable | Source | Produces Real Data | Status |
|----------|--------------|--------|-------------------|--------|
| ManagerController | List<ManagerDto> | ManagerManagementService.getManagers() | ✓ FLOWING | Data fetched via reflection from game objects |
| ManagerController | ManagerDto | ManagerManagementService.updateManager() | ✓ FLOWING | Updates reflectively sets field values |

## Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|------------|-------------|-------------|--------|----------|
| EDIT-03 | 03-01, 03-03 | Manager editing capabilities | ⚠️ PARTIAL | Basic GET/PATCH works, but batch endpoint missing |
| DX-02 | 03-02, 03-03 | Batch operations for bulk edits | ✗ BLOCKED | Batch DTOs and endpoints missing |

## Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| `ManagerManagementService.java` | 87-98 | Partial implementation | ⚠️ Warning | Only name, confidenceBoard, confidenceFans updated - missing age, nationality, reputation, trophies |
| `BrasfootConstants.java` | 54-58 | Placeholder constants | ℹ️ Info | Manager constants use assumed field names ('age', 'nationality') - need real obfuscated names |
| `ManagerManagementService.java` | 116-141 | Partial mapping | ⚠️ Warning | mapToDomain missing age, nationality, reputation, trophies |

## Human Verification Required

None - all gaps can be verified programmatically.

## Gaps Summary

**Phase 3 has significant gaps:**

1. **Batch Operations (Plan 02) - FAILED:** The planned batch DTOs and typed batch endpoints were not implemented. A legacy batch implementation exists in `controller/BatchController.java` but:
   - Uses a different architecture (single unified endpoint vs. typed endpoints)
   - Uses string IDs instead of array indices
   - No batch size limit validation
   - Always returns 207 regardless of success/failure

2. **Manager Batch (Plan 03) - FAILED:** ManagerBatchItem.java was not created and ManagerController lacks POST /batch/managers endpoint.

3. **Manager Update Incomplete (Plan 01) - PARTIAL:** While the controller wiring is correct, the `ManagerManagementService.updateManager()` only updates a subset of fields:
   - ✓ Updates: name, confidenceBoard, confidenceFans
   - ✗ Missing: age, nationality, reputation, trophies

4. **Constants Need Verification:** Manager constants in `BrasfootConstants.java` use placeholder names ('age', 'nationality', 'reputation') marked as "assumed" - these need verification against actual game data.

---

_Verified: 2026-04-05_
_Verifier: gsd-verifier_
