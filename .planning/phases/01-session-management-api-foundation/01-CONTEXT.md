# Phase 1: Session Management & API Foundation - Context

**Gathered:** Sun Apr 05 2026
**Status:** Ready for planning

<domain>
## Phase Boundary

This phase establishes the core Hexagonal Architecture structure, the Spring Boot REST foundation, the in-memory Caffeine cache for session management, and the `upload -> validate -> download` cycle for `.sav` files. It strictly handles file IO and state session creation, deferring the actual editing of internal team/player models to later phases. It also establishes the OpenAPI 3 Swagger UI.
</domain>

<decisions>
## Implementation Decisions

### Hexagonal Packages
- **D-01:** Organize top-level packages into `domain/`, `application/ports/`, and `adapters/` (Standard Hexagonal).
- **D-02:** Interface naming convention: Input interfaces are `*UseCase`, Output interfaces are `*Port`.
- **D-03:** Add ArchUnit tests to the build pipeline to programmatically enforce that adapters do not leak into the domain.

### Validation Depth
- **D-04:** Perform a **Full Parse** of the `.sav` file graph into the domain model immediately during upload to guarantee validity.
- **D-05:** Return detailed RFC-7807 problem detail responses if parsing fails (e.g., "Invalid structure at offset X") rather than generic 400 errors.

### Session TTL (State Management)
- **D-06:** Inactive sessions will be purged from the Caffeine Cache after **1 Hour**.
- **D-07:** The cache eviction policy is an **Absolute TTL** (1 hour from upload, timer does NOT reset on subsequent edits).

### API Routing
- **D-08:** Base session routes will be `POST /api/v1/sessions` (returns the session ID) and `GET /api/v1/sessions/{id}/download`.
- **D-09:** The download endpoint serves the modified save file as a **Raw Binary** byte stream (`application/octet-stream`).

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project Goals
- `.planning/PROJECT.md` — Overall architecture constraints and goal (Strict Ports & Adapters, no DB).
- `.planning/REQUIREMENTS.md` — SESS-01, SESS-02, DX-01, DX-03 requirements mapping.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- The existing binary parsing logic (likely using Kryo) in `service/` or `util/` will be reused but moved behind a `LoadSavePort` / `WriteSavePort`.
- Error mapping logic can be adapted into `@ControllerAdvice` for RFC-7807 responses.

### Established Patterns
- State management was previously in `EditorShellContext.java`. This will be fully replaced by a session-scoped Caffeine cache Adapter implementing a `SessionStatePort`.

### Integration Points
- Uploaded `MultipartFile` payload in REST controller -> Application `UploadSaveUseCase` -> Domain Model Parser -> Cache Adapter.
</code_context>

<specifics>
## Specific Ideas

No specific UI/UX elements, strictly API definitions and architecture layout as decided above.
</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.
</deferred>

---

*Phase: 01-session-management-api-foundation*
*Context gathered: Sun Apr 05 2026*
