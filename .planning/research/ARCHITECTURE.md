# Architecture Patterns

**Domain:** REST API for in-memory game save editing (Hexagonal)
**Researched:** 2026-04-05

## Recommended Architecture

Strict Hexagonal Architecture (Ports and Adapters).

### Component Boundaries

| Component | Responsibility | Communicates With |
|-----------|---------------|-------------------|
| Web Adapters (`@RestController`) | Expose HTTP endpoints, map JSON to DTOs. | Input Ports (Interfaces) |
| Input Ports (`UseCase` Interfaces) | Define the business capabilities (e.g., `EditPlayerUseCase`). | Domain Layer (Implementation) |
| Domain Layer (`Entities/Services`) | Core game state logic (validation, modifications). | Output Ports (Interfaces) |
| Output Ports (`Repository` Interfaces) | Define data access contracts (e.g., `SaveRepository`). | Adapters (Implementation) |
| Cache Adapters (`Caffeine`) | In-memory storage of uploaded save files keyed by session ID. | Output Ports (Implementation) |
| File IO Adapters (`Parser/Serializer`) | Reading and writing binary Brasfoot save formats. | Output Ports (Implementation) |

### Data Flow

1. Client POSTs a `.sav` file to `/api/v1/saves` via Web Adapter.
2. Web Adapter maps `MultipartFile` to an input DTO and calls `UploadSaveUseCase` (Input Port).
3. Domain Service orchestrates the `SaveFileParser` (via an Output Port) to parse the binary.
4. Domain Service saves the parsed `GameState` into the `SaveRepository` (Output Port backed by a `Caffeine Cache` Adapter) generating a unique `sessionId`.
5. Web Adapter returns `sessionId`.
6. Client PATCHes `/api/v1/saves/{sessionId}/teams/1` with new data.
7. Web Adapter maps JSON to an input DTO and calls `EditTeamUseCase` (Input Port).
8. Domain Service retrieves the `GameState` from the `SaveRepository`, applies edits, and updates the cache.
9. Client GETs `/api/v1/saves/{sessionId}/download`.
10. Web Adapter calls `DownloadSaveUseCase`, which triggers the `SaveFileSerializer` (Output Port) to output bytes. Web Adapter returns a file download response.

## Patterns to Follow

### Pattern 1: Session-Based In-Memory Edits
**What:** Storing the complex game state graph in a `Caffeine Cache` linked to a UUID token.
**When:** You need an interactive "Upload -> Edit Multiple Times -> Download" flow without a persistent DB.
**Example:**
```java
public interface SaveRepository {
    GameState getBySessionId(UUID sessionId);
    void save(UUID sessionId, GameState state);
    void remove(UUID sessionId);
}
```

## Anti-Patterns to Avoid

### Anti-Pattern 1: Leaking Domain Entities to Web
**What:** Returning the Domain `Player` or `Team` entity directly from the REST Controller.
**Why bad:** Changes in the domain rules break the public API contract, tightly coupling the layers.
**Instead:** Create specific `PlayerResponse` and `TeamResponse` records in the web adapter package and map to them.

## Scalability Considerations

| Concern | At 100 concurrent edits | At 10K concurrent edits | At 1M concurrent edits |
|---------|--------------|--------------|-------------|
| Memory Usage (OOM) | `Caffeine` cache limits concurrent sizes and uses `expireAfterWrite`. | Introduce Redis or Hazelcast for distributed caching instead of local JVM. | Move to specialized stateful actors (Akka) or serverless cloud storage for the raw binaries. |

## Sources

- Clean Architecture & Hexagonal Architecture Principles (HIGH confidence)
