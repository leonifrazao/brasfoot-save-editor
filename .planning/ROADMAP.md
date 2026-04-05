# Project Roadmap

## Phases

- [ ] **Phase 1: Session Management & API Foundation** - Users can initialize and retrieve save file sessions via a documented API
- [ ] **Phase 2: Team & Player Management** - Users can modify core game entities (teams and players) within an active session
- [ ] **Phase 3: Manager & Batch Operations** - Users can edit manager data and perform bulk updates efficiently

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
**Plans**: TBD

### Phase 3: Manager & Batch Operations
**Goal**: Users can edit manager data and perform bulk updates efficiently
**Depends on**: Phase 2
**Requirements**: EDIT-03, DX-02
**Success Criteria**:
  1. User can update manager profile information via API
  2. User can submit a single batch request to apply multiple edits at once
  3. All batch edits accurately update the in-memory state for download
**Plans**: TBD

## Progress

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Session Management & API Foundation | 0/3 | Planned | - |
| 2. Team & Player Management | 0/0 | Not started | - |
| 3. Manager & Batch Operations | 0/0 | Not started | - |
