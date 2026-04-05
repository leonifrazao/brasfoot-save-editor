# Domain Pitfalls

**Domain:** REST API for in-memory game save editing (Hexagonal)
**Researched:** 2026-04-05

## Critical Pitfalls

Mistakes that cause rewrites or major issues.

### Pitfall 1: Leaking Domain Entities into REST Controllers
**What goes wrong:** Exposing internal Domain objects (like `Team`, `Player`) directly in Spring `@RestController` responses.
**Why it happens:** Convenience. Writing explicit mapping layers feels like duplication.
**Consequences:** Any change in the internal game model breaks the public API contract. Swagger docs reflect internal structures.
**Prevention:** Enforce Hexagonal architecture using ArchUnit tests. Force REST controllers to use explicit DTOs (Data Transfer Objects) mapping to and from Input Ports.
**Detection:** ArchUnit test failures when `web` package imports `domain` entities directly.

### Pitfall 2: Stateful API Complexity (OOM Errors)
**What goes wrong:** The server runs out of memory (OOM) because users upload massive save files and never download them.
**Why it happens:** Not defining an eviction policy for the in-memory save file state.
**Consequences:** API crashes, affecting all concurrent users.
**Prevention:** Use `Caffeine Cache` with a strict `expireAfterAccess` or `expireAfterWrite` policy (e.g., 30 minutes) and a `maximumSize` limit for concurrent active sessions.
**Detection:** High heap usage or explicit `OutOfMemoryError` in logs.

## Moderate Pitfalls

### Pitfall 3: Not Versioning the API
**What goes wrong:** The game updates its save format, and the API needs to support the new format without breaking old scripts.
**Prevention:** Use Spring Web `@RequestMapping("/api/v1/saves")` from day one.

## Phase-Specific Warnings

| Phase Topic | Likely Pitfall | Mitigation |
|-------------|---------------|------------|
| Initial Setup | Coupling the CLI logic with the new REST logic. | Create the Hexagonal structure first and move the logic slowly. |
| State Management | Losing edit session context. | Generate a unique UUID for each upload and require it in the `Authorization` or `X-Session-ID` header for all subsequent edit requests. |

## Sources

- Spring Boot & Caching Best Practices (HIGH confidence)
- Hexagonal Architecture Principles (HIGH confidence)
