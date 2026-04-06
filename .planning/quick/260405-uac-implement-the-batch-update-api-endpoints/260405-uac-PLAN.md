---
phase: "260405-uac"
plan: "01"
type: "execute"
wave: 1
depends_on: []
files_modified:
  - "src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/TeamBatchUpdateRequest.java"
  - "src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/PlayerBatchUpdateRequest.java"
  - "src/main/java/br/com/saveeditor/brasfoot/application/ports/in/UpdateTeamUseCase.java"
  - "src/main/java/br/com/saveeditor/brasfoot/application/ports/in/UpdatePlayerUseCase.java"
  - "src/main/java/br/com/saveeditor/brasfoot/application/services/TeamManagementService.java"
  - "src/main/java/br/com/saveeditor/brasfoot/application/services/PlayerManagementService.java"
  - "src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/TeamController.java"
  - "src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/PlayerController.java"
autonomous: true
requirements: ["BATCH-01"]

must_haves:
  truths:
    - "User can perform batch updates to multiple teams in a single request"
    - "User can perform batch updates to multiple players in a single request"
    - "Swagger documentation accurately describes the batch update endpoints"
  artifacts:
    - path: "src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/TeamController.java"
      provides: "PATCH /batch endpoint for teams"
    - path: "src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/PlayerController.java"
      provides: "PATCH /batch endpoint for players"
  key_links:
    - from: "src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/TeamController.java"
      to: "UpdateTeamUseCase"
      via: "batchUpdateTeams method call"
      pattern: "batchUpdateTeams"
---

<objective>
Implement batch update REST API endpoints for teams and players, enabling multiple entities to be edited simultaneously in a single transaction/request with clear Swagger documentation.

Purpose: Greatly improve API efficiency by allowing clients to update bulk data without making hundreds of individual requests.
Output: New DTOs, updated use cases, service logic, and /batch endpoints.
</objective>

<execution_context>
@$HOME/.config/opencode/get-shit-done/workflows/execute-plan.md
</execution_context>

<context>
@.planning/STATE.md
@src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/TeamUpdateRequest.java
@src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/PlayerUpdateRequest.java
</context>

<tasks>
<task type="auto">
  <name>Task 1: Create Batch DTOs and Update Use Case Ports</name>
  <files>
    src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/TeamBatchUpdateRequest.java
    src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/dto/PlayerBatchUpdateRequest.java
    src/main/java/br/com/saveeditor/brasfoot/application/ports/in/UpdateTeamUseCase.java
    src/main/java/br/com/saveeditor/brasfoot/application/ports/in/UpdatePlayerUseCase.java
  </files>
  <action>
    - Create `TeamBatchUpdateRequest` record extending `TeamUpdateRequest` fields but adding `int teamId`.
    - Create `PlayerBatchUpdateRequest` record extending `PlayerUpdateRequest` fields but adding `int playerId`.
    - Update `UpdateTeamUseCase` to add `List<Team> batchUpdateTeams(UUID sessionId, List<TeamBatchUpdateRequest> requests)`.
    - Update `UpdatePlayerUseCase` to add `List<Player> batchUpdatePlayers(UUID sessionId, int teamId, List<PlayerBatchUpdateRequest> requests)`.
  </action>
  <verify>
    <automated>mvn clean compile</automated>
  </verify>
  <done>DTOs created and Use Cases extended, compiling successfully.</done>
</task>

<task type="auto">
  <name>Task 2: Implement Batch Update Logic in Services</name>
  <files>
    src/main/java/br/com/saveeditor/brasfoot/application/services/TeamManagementService.java
    src/main/java/br/com/saveeditor/brasfoot/application/services/PlayerManagementService.java
  </files>
  <action>
    - Implement `batchUpdateTeams` in `TeamManagementService`: iterate through requests, apply the same reflection/validation logic used in `updateTeam`, but only call `sessionStatePort.save(session)` ONCE at the end. Collect and return the updated `Team` objects. Add logging.
    - Implement `batchUpdatePlayers` in `PlayerManagementService`: iterate through requests, validate and update player properties exactly as `updatePlayer` does, call `sessionStatePort.save(session)` ONCE at the end. Collect and return the updated `Player` objects. Add logging.
  </action>
  <verify>
    <automated>mvn clean compile</automated>
  </verify>
  <done>Service implementations for batch updates added and optimized to save state only once per batch.</done>
</task>

<task type="auto">
  <name>Task 3: Add Batch Controller Endpoints with Swagger</name>
  <files>
    src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/TeamController.java
    src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/PlayerController.java
  </files>
  <action>
    - In `TeamController`, add a `@PatchMapping("/batch")` endpoint mapping to `batchUpdateTeams`.
    - In `PlayerController`, add a `@PatchMapping("/batch")` endpoint mapping to `batchUpdatePlayers`.
    - Ensure robust Swagger documentation for both: Add `@Operation(summary = "Batch update teams/players", description = "...")` and `@ApiResponse` for 200, 400, and 404 error codes.
    - Map the returned lists of domains to lists of DTOs (e.g. `List<TeamDto>`) and return as `ResponseEntity.ok`.
  </action>
  <verify>
    <automated>mvn clean test</automated>
  </verify>
  <done>REST API exposes and correctly documents the /batch endpoints.</done>
</task>
</tasks>

<threat_model>
## Trust Boundaries

| Boundary | Description |
|----------|-------------|
| REST API -> Domain | Incoming batch request data from web clients is untrusted and must be validated |

## STRIDE Threat Register

| Threat ID | Category | Component | Disposition | Mitigation Plan |
|-----------|----------|-----------|-------------|-----------------|
| T-260405-uac-01 | Tampering | TeamManagementService | mitigate | Validate inputs for each item in the batch (e.g., non-negative money) exactly as single updates do |
| T-260405-uac-02 | Denial of Service | Batch Endpoints | accept | Accept risk of large batches causing memory spikes; expected use cases are well within reasonable local boundaries |
</threat_model>
