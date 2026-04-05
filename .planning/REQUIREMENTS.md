# Requirements: Brasfoot Save Editor API

**Defined:** Sun Apr 05 2026
**Core Value:** Enable programmatic, reliable, and comprehensive editing of Brasfoot save files through a clean REST API without any coupled UI or dead code.

## v1 Requirements

### Session

- [ ] **SESS-01**: User can upload a save file to begin an editing session and receive a session ID
- [ ] **SESS-02**: User can download the modified save file using their session ID

### Edit

- [x] **EDIT-01**: User can edit Team stats and finances
- [x] **EDIT-02**: User can edit Player attributes
- [ ] **EDIT-03**: User can edit Manager profile data

### Developer Experience & Reliability (DX)

- [ ] **DX-01**: System validates uploaded saves for corruption before initiating a session
- [ ] **DX-02**: User can apply batch edits to multiple entities in a single request
- [ ] **DX-03**: Developer can view and test all endpoints via an auto-generated OpenAPI 3 Swagger UI

## v2 Requirements

### Tournament

- **TRN-01**: User can modify league states, fixtures, and standings

## Out of Scope

| Feature | Reason |
|---------|--------|
| Persistent Database (JPA) | Explicitly out of scope in PROJECT.md. Unnecessary overhead; use in-memory state (Caffeine Cache) tied to an edit session token. |
| CLI / Web UI | The goal is a clean REST API. UI creates coupling. Rely on the OpenAPI Swagger UI for manual testing. |
| User Authentication | Unnecessary for a simple file manipulation tool. Rely on opaque session IDs/tokens for isolation. |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| SESS-01 | Phase 1 | Pending |
| SESS-02 | Phase 1 | Pending |
| EDIT-01 | Phase 2 | Complete |
| EDIT-02 | Phase 2 | Complete |
| EDIT-03 | Phase 3 | Pending |
| DX-01 | Phase 1 | Pending |
| DX-02 | Phase 3 | Pending |
| DX-03 | Phase 1 | Pending |

**Coverage:**
- v1 requirements: 8 total
- Mapped to phases: 8
- Unmapped: 0 ✓

---
*Requirements defined: Sun Apr 05 2026*
*Last updated: Sun Apr 05 2026 after initial definition*
