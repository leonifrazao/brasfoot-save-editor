# Phase 3: Manager & Batch Operations - Context

**Gathered:** 2026-04-05
**Status:** Ready for planning

<domain>
## Phase Boundary

This phase introduces editing capabilities for Manager profiles and implements batch operation endpoints for both Managers and previously completed entities (Teams, Players). It covers the request format, transaction boundaries (partial success), and necessary Swagger documentation for these bulk actions.
</domain>

<decisions>
## Implementation Decisions

### Batch Request Format
- **D-01:** Use **Custom Bulk Objects** (arrays of strongly typed items) for batch payloads rather than RFC 6902 JSON Patch.
- **D-02:** Use **Typed endpoints** (e.g., `POST /api/v1/sessions/{id}/batch/players`, `POST /api/v1/sessions/{id}/batch/teams`) rather than a single mixed-entity payload.
- **D-03:** Enforce a **Hard limit** on batch sizes (e.g., 1000 items) synchronously. Return 400 Bad Request if the payload exceeds this limit.
- **D-04:** Edits are **Absolute Only** (e.g., setting energy to 100, not +50) to maintain a declarative REST style.

### Batch Error Handling & Transactions
- **D-05:** Implement **Partial Success (207 Multi-Status)**. Valid items in the batch are applied to the save state, while invalid items are rejected.
- **D-06:** In the 207 response, represent errors as a **list of errors with indices** mapping directly back to the index of the item in the request payload.

### Manager Editable Attributes
- **D-07:** **All manager attributes** are editable (Name, Nationality, Age, Reputation, Trophies, etc.) to allow full career control.
- **D-08:** **Team assignment is read-only**. Changing a manager's `teamId` directly is not permitted via basic edit endpoints, as transfers require complex business logic side-effects.

### DX and Observability
- **D-09:** For batch operations, use **Summary logging only** (e.g., "Batch applied 50 items, 2 failed") rather than verbose per-item logs to prevent log spam.
- **D-10:** In the OpenAPI/Swagger UI, provide **full, realistic example payloads** for the batch endpoints to facilitate easier developer testing.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project Goals & Roadmap
- `.planning/PROJECT.md` — Overall architecture constraints and in-memory requirements.
- `.planning/ROADMAP.md` — Phase 3 requirements (EDIT-03, DX-02).
- `.planning/REQUIREMENTS.md` — SESS-01, SESS-02 context, and existing DX-03 Swagger requirements.

### Existing Architecture
- `.planning/codebase/STRUCTURE.md` — Hexagonal architecture folder structure and patterns.
- `.planning/phases/01-session-management-api-foundation-CONTEXT.md` — Context regarding the session UUID state management that these batch operations will run against.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- The existing error mapping logic (e.g., in `GlobalExceptionHandler` created in Quick task `260405-rzh`) can be extended to format the 207 Multi-Status error lists.
- Existing SLF4J setup (from Quick task `260405-rc7`) should be used for the summary batch logging.
- Existing `Player` and `Team` domain models will be reused for the typed batch endpoints.

### Established Patterns
- State management operates via a session-scoped Caffeine cache Adapter implementing `SessionStatePort`. Batch operations must lock or synchronize properly on this session state.

### Integration Points
- REST controllers in `application/ports/` must route the typed batch arrays into the Domain model to process each item, collecting errors to return the 207 response.
</code_context>

<specifics>
## Specific Ideas

- The 207 response payload for errors should cleanly map back to the request array index (e.g., `{"index": 5, "error": "Invalid player ID"}`).
- The batch size limit should ideally be configurable via `application.properties` (e.g., `brasfoot.api.batch.max-size=1000`), even though the default is a hard limit.
</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.
</deferred>

---

*Phase: 03-manager-batch-operations*
*Context gathered: 2026-04-05*