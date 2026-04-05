# Phase 1: Session Management & API Foundation - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-04-05
**Phase:** 1-Session Management & API Foundation
**Areas discussed:** Hexagonal Packages, Validation Depth, Session TTL, API Routing

---

## Hexagonal Packages

| Option | Description | Selected |
|--------|-------------|----------|
| Standard Hexagonal (Recommended) | domain/, application/ports/, adapters/ | ✓ |
| Package by Feature | session/domain, session/adapters | |

**User's choice:** Standard Hexagonal (Recommended)

| Option | Description | Selected |
|--------|-------------|----------|
| Ports & UseCases (Recommended) | Input: *UseCase, Output: *Port | ✓ |
| Traditional (Service/Repo) | Input: *Service, Output: *Repository | |

**User's choice:** Ports & UseCases (Recommended)

| Option | Description | Selected |
|--------|-------------|----------|
| ArchUnit Tests (Recommended) | Add ArchUnit tests to fail build if adapters leak into domain | ✓ |
| Conventions Only | Rely on code reviews and developer discipline | |

**User's choice:** ArchUnit Tests (Recommended)

---

## Validation Depth

| Option | Description | Selected |
|--------|-------------|----------|
| Full Parse (Recommended) | Parse the entire file graph into the domain model immediately | ✓ |
| Header Only | Check file extension and basic binary headers only | |
| Lazy Load | Load incrementally when user edits | |

**User's choice:** Full Parse (Recommended)

| Option | Description | Selected |
|--------|-------------|----------|
| Detailed RFC-7807 (Recommended) | Return specific reason (e.g., 'Invalid team structure at offset X') | ✓ |
| Generic 400 | Return simple 400 Bad Request 'Corrupted file' | |

**User's choice:** Detailed RFC-7807 (Recommended)

---

## Session TTL

| Option | Description | Selected |
|--------|-------------|----------|
| 1 Hour (Recommended) | 1 hour of inactivity is standard for editing sessions | ✓ |
| 24 Hours | Allows coming back the next day, uses more RAM | |
| 15 Minutes | Aggressive, frees RAM quickly | |

**User's choice:** 1 Hour (Recommended)

| Option | Description | Selected |
|--------|-------------|----------|
| Absolute TTL (Recommended) | Hard deadline from upload regardless of activity | ✓ |
| Time Since Last Edit (Idle) | Reset timer on every API edit | |

**User's choice:** Absolute TTL (Recommended)

---

## API Routing

| Option | Description | Selected |
|--------|-------------|----------|
| /api/v1/sessions (Recommended) | POST /api/v1/sessions -> {id}, GET /api/v1/sessions/{id}/download | ✓ |
| /api/v1/saves | POST /api/v1/saves -> {id}, GET /api/v1/saves/{id}/file | |

**User's choice:** /api/v1/sessions (Recommended)

| Option | Description | Selected |
|--------|-------------|----------|
| Raw Binary (Recommended) | Return the raw .sav byte stream (Content-Type: application/octet-stream) | ✓ |
| Base64 in JSON | Return Base64 string embedded in a JSON response | |

**User's choice:** Raw Binary (Recommended)
