# Project Roadmap

## Phases

- [x] **Phase 1: Session Management & API Foundation** - Users can initialize and retrieve save file sessions via a documented API
- [x] **Phase 2: Team & Player Management** - Users can modify core game entities (teams and players) within an active session
- [x] **Phase 3: Manager & Batch Operations** - Users can edit manager data and perform bulk updates efficiently
- [x] **Phase 4: Domain Model & Architecture Consistency** - Codebase standardized around interface-first, self-validating domain models and folder separation
- [ ] **Phase 5: Manager & Batch Contract Closure** - Close v1.0 blocking gaps for manager editing and batch reliability contracts
- [ ] **Phase 6: Requirements Traceability & Evidence Realignment** - Restore requirement evidence chain and traceability consistency for v1.0 closure

## Phase Details

### Phase 1: Session Management & API Foundation
**Goal**: Users can initialize and retrieve save file sessions via a documented API
**Depends on**: None
**Requirements**: SESS-01, SESS-02, DX-01, DX-03
**Success Criteria**:
  1. Developer can browse API endpoints via auto-generated Swagger UI
  2. User can upload a valid save file and receive a unique session ID
  3. System rejects invalid or corrupted save files during upload
  4. User can download the exact same save file using their active session ID
**Plans**:
- [x] 01-01-PLAN.md — Foundation and Hexagonal structure
- [x] 01-02-PLAN.md — Domain models, ports, and Caffeine cache
- [x] 01-03-PLAN.md — Use cases and REST controllers

### Phase 2: Team & Player Management
**Goal**: Users can modify core game entities (teams and players) within an active session
**Depends on**: Phase 1
**Requirements**: EDIT-01, EDIT-02
**Success Criteria**:
  1. User can update team statistics and finances via API
  2. User can update individual player attributes via API
  3. Changes to teams and players are successfully reflected in the downloaded save file
**Plans**:
- [x] 02-01-PLAN.md — Team Management API
- [x] 02-02-PLAN.md — Player Management API

### Phase 3: Manager & Batch Operations
**Goal**: Users can edit manager data and perform bulk updates efficiently
**Depends on**: Phase 2
**Requirements**: EDIT-03, DX-02
**Success Criteria**:
  1. User can update manager profile information via API
  2. User can submit a single batch request to apply multiple edits at once
  3. All batch edits accurately update the in-memory state for download
**Plans**: 3 plans
- [x] 03-01-PLAN.md — Manager Entity Management
- [x] 03-02-PLAN.md — Batch Operations for Teams and Players
- [x] 03-03-PLAN.md — Manager Batch Operations & Documentation

## Progress

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Session Management & API Foundation | 3/3 | Complete | ✓ |
| 2. Team & Player Management | 2/2 | Complete | ✓ |
| 3. Manager & Batch Operations | 3/3 | Complete | ✓ |
| 4. Domain Model & Architecture Consistency | 4/4 | Complete | ✓ |
| 5. Manager & Batch Contract Closure | 0/0 | Not Started |  |
| 6. Requirements Traceability & Evidence Realignment | 0/0 | Not Started |  |

### Phase 4: add lombok and create real Domains, with self validation. fix project inconsistency like 2 services, all the services/usecase need a interface; Organize the “record” and “domain” folders. The ‘domain’ folder must be separate; the “record” folder goes in a different folder; keep the folders separate.

**Goal:** Standardize the codebase around Lombok-enabled, self-validating domain models with interface-consistent services/use-cases and explicit separation between domain and record transport folders
**Requirements**: TBD
**Depends on:** Phase 3
**Plans:** 4 plans

Plans:
- [x] 04-01-PLAN.md — Enable Lombok and define interface-first architecture contracts
- [x] 04-02-PLAN.md — Implement self-validating domain models and rewire validations
- [x] 04-03-PLAN.md — Enforce service/use-case interface consistency and manager batch contract
- [x] 04-04-PLAN.md — Separate record and domain folders with import rewiring

### Phase 5: Manager & Batch Contract Closure
**Goal:** Close milestone-blocking manager and batch contract gaps by aligning API behavior, typed contracts, and verification evidence with v1.0 requirements.
**Requirements:** EDIT-03, DX-02
**Depends on:** Phase 4
**Gap Closure:** Closes requirement, integration, and flow gaps identified in `v1.0-v1.0-MILESTONE-AUDIT.md` for manager completeness and batch partial-failure semantics.

### Phase 6: Requirements Traceability & Evidence Realignment
**Goal:** Reconcile requirement traceability and summary evidence so audited v1.0 requirement coverage is complete and internally consistent.
**Requirements:** EDIT-01, EDIT-02
**Depends on:** Phase 5
**Gap Closure:** Closes audit gaps where verification passed but requirements-completed linkage and artifact consistency are incomplete.
