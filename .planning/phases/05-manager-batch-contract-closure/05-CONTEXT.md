---
phase: 05-manager-batch-contract-closure
created: 2026-04-06T02:45:00Z
status: planning
goals:
  - "Close EDIT-03 gap: Manager editing field completeness and batch reliability"
  - "Close DX-02 gap: Batch operations 207 partial-result semantics with error mapping"
requirements:
  - "EDIT-03"
  - "DX-02"
depends_on: 04
gap_closure: true
---

# Phase 5: Manager & Batch Contract Closure

## Purpose

Close milestone-blocking gaps in manager editing and batch operations identified in `v1.0-v1.0-MILESTONE-AUDIT.md`:
- **EDIT-03 (unsatisfied):** Manager field editing incomplete; batch endpoint missing typed error handling
- **DX-02 (blocked):** Batch endpoints return 200 instead of 207 multi-status; error mapping by array index missing

## Gaps to Close

### Gap 1: Manager Field Updates Incomplete (EDIT-03)

**Status:** `ManagerManagementService.updateManager()` and `batchUpdateManagers()` skip:
- `age`
- `nationality`
- `reputation`
- `trophies`

**Root Cause:** Lines 89-98 in updateManager() only set name, confidenceBoard, confidenceFans. Lines 143-151 in batchUpdateManagers() same issue.

**What Must Happen:** All four fields must be updated from request to game object via ReflectionUtils, validated in domain model.

**Constants Status:** `BrasfootConstants.java` has placeholder field names (lines 54-58) marked "assumed" — need verification these map correctly to game data.

### Gap 2: Batch Response Semantics Missing (DX-02)

**Current State:**
- `ManagerController.batchUpdateManagers()` returns `ResponseEntity<List<ManagerDto>>` (line 86-92)
- Returns HTTP 200 on success, no partial-failure handling
- No typed batch response DTO with per-item success/error envelopes
- No error mapping by array index (per D-06 decision)

**Required State:**
- Typed `BatchResponse<T>` DTO with structure:
  ```
  {
    results: [
      { index: 0, success: true, data: ManagerDto },
      { index: 1, success: false, error: "validation error" }
    ]
  }
  ```
- Return HTTP 207 Multi-Status for partial failures
- All batch endpoints (Team, Player, Manager) use consistent response contract
- Errors must track back to request array index, not entity ID

**Verification Evidence:** 03-VERIFICATION.md marks this BLOCKED (line 23, 29, 55-65).

### Gap 3: Type Contract Consistency (DX-02 + EDIT-03)

**Current Wiring Issues:**
- Session IDs are `String` path vars in TeamController but `UUID` in GameDataPort
- Batch DTOs exist for Team/Player but ManagerBatchUpdateCommand is a record (not aligned with Team/Player batch DTOs)
- No unified batch exception handling across controllers

**Impact:** Inconsistent error handling, unpredictable client behavior across entity types.

## Scope

### Must Implement

1. **Manager field updates:** Add age, nationality, reputation, trophies to updateManager() and batchUpdateManagers()
2. **Batch response 207 semantics:** Create typed BatchResponse<T>, update all batch endpoints to return 207 on partial failure
3. **Error mapping by index:** Batch failures include array index, not entity ID
4. **Consistent batch DTOs:** Align ManagerBatchUpdateCommand with TeamBatchUpdateRequest/PlayerBatchUpdateRequest patterns

### Out of Scope (Deferred)

- Morale field mapping (known placeholder, Phase 4 tech debt)
- Session ID type normalization across all layers (broader refactor, post-v1.0)

## Known Constraints

- Architecture: Hexagonal (ports and adapters)
- No UI/Shell changes — API only
- Domain models must validate before reflection
- In-memory session state only (no database)
- Must maintain backward compatibility at domain level
