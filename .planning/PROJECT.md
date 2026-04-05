# Brasfoot Save Editor API

## What This Is

A REST API for editing Brasfoot save game files. The application allows users to upload a save file, edit in-memory game state (Teams, Players, Managers, Tournaments, Finances), and download the modified save file. It is built using Java, Spring Boot, and Maven, and strictly follows Hexagonal Architecture (Ports and Adapters). 

## Core Value

Enable programmatic, reliable, and comprehensive editing of Brasfoot save files through a clean REST API without any coupled UI or dead code.

## Requirements

### Validated

- ✓ Read/Parse Brasfoot save files into memory (existing capability)
- ✓ Write/Serialize Brasfoot save files from memory (existing capability)

### Active

- [ ] Migrate the existing Spring Shell/CLI application to a Spring Boot REST API
- [ ] Implement Hexagonal Architecture with strict separation of Domain, Ports, and Adapters
- [ ] Expose REST endpoints to upload and download save files
- [ ] Expose REST endpoints to edit Team and Player stats/finances
- [ ] Expose REST endpoints to edit Manager data
- [ ] Expose REST endpoints to edit Tournament/League data
- [ ] Remove all dead code (especially old UI/CLI related code)

### Out of Scope

- User Interface (UI) — The project is strictly a backend REST API.
- Database Persistence — Edits are done in-memory on the uploaded save file; no database is used.

## Context

The project is a migration of an existing Brasfoot save editor (which previously used Spring Shell for a CLI interface). The goal is to modernize the architecture by moving to a strict Hexagonal Architecture (Ports and Adapters) pattern, stripping out the old CLI/UI, and exposing the functionality purely via REST APIs.

## Constraints

- **Architecture**: Strict Hexagonal Architecture (Ports and Adapters) — To ensure the domain logic is completely decoupled from web and file I/O concerns.
- **Tech Stack**: Java, Spring Boot, Maven — Standardizing on the Spring ecosystem for the API layer.
- **State Management**: In-memory editing — The API must accept a file, keep the state in memory for editing via subsequent API calls (or edit in a single transaction), and provide a way to download the result. 

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Hexagonal Architecture | Decouples the complex save-file parsing logic and domain rules from the delivery mechanism (REST). | — Pending |
| Stateless/Stateful API Design | Need to decide how to handle the "upload -> edit -> download" flow (e.g., session-based, token-based, or single-shot edits). | — Pending |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: Sun Apr 05 2026 after initialization*
