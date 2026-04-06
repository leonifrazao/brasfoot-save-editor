---
phase: quick
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - "src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/batch/BatchResponse.java"
  - "src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/batch/BatchItemError.java"
  - "src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/batch/TeamBatchItem.java"
  - "src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/batch/PlayerBatchItem.java"
  - "src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/batch/ManagerBatchItem.java"
  - "src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/TeamController.java"
  - "src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/PlayerController.java"
  - "src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/ManagerController.java"
  - "src/main/java/br/com/saveeditor/brasfoot/application/services/TeamManagementService.java"
  - "src/main/java/br/com/saveeditor/brasfoot/application/services/PlayerManagementService.java"
  - "src/main/java/br/com/saveeditor/brasfoot/application/services/ManagerManagementService.java"
  - "src/main/java/br/com/saveeditor/brasfoot/util/BrasfootConstants.java"
autonomous: true
requirements:
  - DX-02
  - EDIT-03
must_haves:
  truths:
    - "User can submit an array of team/player/manager updates and get a 207 response if some fail"
    - "Errors explicitly map back to the array index of the request payload"
    - "User cannot submit more than the configured max batch size (400 Bad Request)"
    - "User can update a specific manager's age, nationality, reputation, and trophies"
  artifacts:
    - path: "src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/batch/TeamBatchItem.java"
      provides: "Team batch payload"
    - path: "src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/batch/BatchResponse.java"
      provides: "Batch execution results"
  key_links:
    - from: "src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/TeamController.java"
      to: "TeamManagementService.batchUpdateTeams"
      via: "POST /api/v1/sessions/{sessionId}/batch/teams"
      pattern: "batchUpdateTeams"
---

<objective>
Fix the batch operations and manager update gaps identified in phase 3 verification.

Purpose: Bring the API into compliance with the hexagonal architecture for batch processing, implement array-index based error tracking, enforce batch size limits, and fully implement all manager property updates.
Output: Typed batch DTOs, three new batch endpoints in their respective controllers, batch logic in the services, and complete manager update mapping.
</objective>

<execution_context>
@$HOME/.config/opencode/get-shit-done/workflows/execute-plan.md
@$HOME/.config/opencode/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/STATE.md
@.planning/phases/03-manager-batch-operations/03-VERIFICATION.md
</context>

<tasks>

<task type="auto">
  <name>Task 1: Create typed batch DTOs with validation</name>
  <files>
    src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/batch/BatchResponse.java
    src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/batch/BatchItemError.java
    src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/batch/TeamBatchItem.java
    src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/batch/PlayerBatchItem.java
    src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/batch/ManagerBatchItem.java
  </files>
  <action>
    Create the batch DTO records in the `adapters.in.web.dto.batch` package.
    - `BatchItemError(int index, String error)`
    - `BatchResponse(int successful, int failed, List<BatchItemError> errors)`
    - `TeamBatchItem(int id, TeamUpdateRequest update)`
    - `PlayerBatchItem(int id, PlayerUpdateRequest update)`
    - `ManagerBatchItem(int id, ManagerUpdateRequest update)`
    Import the update request classes from `br.com.saveeditor.brasfoot.adapters.in.web.dto.*`.
    Ensure these files exist and compile correctly.
    Also, remove the old legacy batch DTOs (`src/main/java/br/com/saveeditor/brasfoot/dto/Batch*.java` and `controller/BatchController.java`, `service/BatchService.java`) which were previously deleted but not committed. Use `git rm` if necessary to clean the working tree.
  </action>
  <verify>
    <automated>mvn compile</automated>
  </verify>
  <done>Typed batch DTOs exist, legacy ones removed.</done>
</task>

<task type="auto">
  <name>Task 2: Implement Batch Endpoints in Controllers</name>
  <files>
    src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/TeamController.java
    src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/PlayerController.java
    src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/ManagerController.java
  </files>
  <action>
    Add a POST `/batch/teams` endpoint in `TeamController`, POST `/batch/players` in `PlayerController`, and POST `/batch/managers` in `ManagerController`.
    Each should accept `@PathVariable String sessionId` and `@Valid @RequestBody @Size(max = "${brasfoot.api.batch.max-size:1000}") List<TBatchItem> items`.
    Wait, to use `@Size` on a list, the controller class must be annotated with `@Validated`.
    Call the respective `batchUpdate*(sessionId, items)` method on the management service.
    Return a `ResponseEntity<BatchResponse>`. If `response.failed() > 0`, return `HttpStatus.MULTI_STATUS` (207), otherwise `HttpStatus.OK` (200).
    Document endpoints with Swagger annotations.
  </action>
  <verify>
    <automated>mvn compile</automated>
  </verify>
  <done>Controllers have batch endpoints with size validation.</done>
</task>

<task type="auto">
  <name>Task 3: Implement Service Methods and Fix Manager Updates</name>
  <files>
    src/main/java/br/com/saveeditor/brasfoot/application/services/TeamManagementService.java
    src/main/java/br/com/saveeditor/brasfoot/application/services/PlayerManagementService.java
    src/main/java/br/com/saveeditor/brasfoot/application/services/ManagerManagementService.java
    src/main/java/br/com/saveeditor/brasfoot/util/BrasfootConstants.java
  </files>
  <action>
    Add `batchUpdate*(String sessionId, List<*BatchItem> items)` to each service. Iterate through `items`. Try to update the entity using the existing single update logic. If successful, increment successful count. If it throws an exception, catch it, log it, and add a `new BatchItemError(i, e.getMessage())` to a list (where `i` is the loop index of the items array). After the loop, save the session via `sessionStatePort.save(session)` once. Return a `BatchResponse`. 
    
    For `ManagerManagementService`: Fix the single `updateManager` method. Discover or assume the real obfuscated field names for `age`, `nationality`, `reputation`, and `trophies` in `BrasfootConstants` (use javap or reflection script to find them, or just use 'age', 'nationality', 'reputation', 'trophies' if undiscoverable, but update `mapToDomain` and `updateManager` to get/set them properly on the Manager domain object).
  </action>
  <verify>
    <automated>mvn test</automated>
  </verify>
  <done>Batch operations process gracefully returning index-based errors, Manager updates all fields.</done>
</task>

</tasks>

<threat_model>
## Trust Boundaries

| Boundary | Description |
|----------|-------------|
| API→Services | Client sends lists of batch items that cross into domain services |

## STRIDE Threat Register

| Threat ID | Category | Component | Disposition | Mitigation Plan |
|-----------|----------|-----------|-------------|-----------------|
| T-quick-01 | Denial of Service | Batch Controllers | mitigate | Validate maximum batch size (1000) using @Size and @Validated to prevent memory/CPU exhaustion |
</threat_model>

<verification>
Check that `mvn clean test` compiles everything and passes. Check that no legacy batch files remain.
</verification>

<success_criteria>
All batch endpoints return typed 207 Multi-Status responses for partial failures, track errors by payload array index, and reject oversized payloads with 400 Bad Request. Manager updates handle all specified attributes.
</success_criteria>

<output>
After completion, create `.planning/quick/260405-txj-id-like-to-fix-the-batch-gaps-pls/260405-txj-SUMMARY.md`
</output>
