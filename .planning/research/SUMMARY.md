# Research Summary: Brasfoot Save Editor API

**Domain:** REST API for in-memory game save editing
**Researched:** 2026-04-05
**Overall confidence:** HIGH

## Executive Summary

The transition from a CLI (Spring Shell) to a Hexagonal REST API requires decoupling the existing parsing/serialization logic from the delivery mechanism. Because this system has no persistent database, state management between the "upload -> edit -> download" phases is the most critical technical challenge. The recommended approach is to use Java 21 LTS with Spring Boot 3.4.x, relying on `Caffeine Cache` for temporary in-memory save state persistence keyed by an edit session token. 

Strict architectural boundaries will be enforced via ArchUnit to prevent regressions where REST controllers leak into the Domain logic. The API must be documented using Springdoc OpenAPI 3 to serve as the contract for any future headless integrations.

## Key Findings

**Stack:** Java 21, Spring Boot 3.4.x, Spring Web, Caffeine (In-Memory), ArchUnit, JUnit 5.
**Architecture:** Strict Hexagonal (Ports and Adapters) with API Controllers mapped to Input Ports and an InMemory/Caffeine Cache acting as the primary State Adapter.
**Critical pitfall:** Allowing Domain entities to leak into the REST Controllers (Adapters) or bypassing the Ports to directly access the binary parser logic.

## Implications for Roadmap

Based on research, suggested phase structure:

1. **Phase 1: Architecture Foundation & Core Dependency Setup** - Upgrading to Java 21, Spring Boot 3.4.x, adding ArchUnit, and laying out the core Hexagonal package structure.
   - Addresses: Setting up Hexagonal boundaries before migrating existing logic.
   - Avoids: Mixing old CLI/Shell dependencies with new REST dependencies.

2. **Phase 2: Domain Isolation & Parser Adapter** - Moving the validated save parsing/writing logic behind an Output Port.
   - Addresses: Decoupling file IO from business logic.
   - Avoids: Tightly coupling the binary reading to web requests.

3. **Phase 3: State Management & Core REST API** - Implementing the Caffeine Cache adapter for session-based in-memory editing, and the primary Upload/Download `/saves` endpoints.
   - Addresses: The core "Upload -> Download" loop.
   - Avoids: Stateless design that would require uploading/downloading the file for every single edit request.

4. **Phase 4: Edit Endpoints Implementation** - Mapping the Team, Player, Manager, and Tournament editing logic to REST endpoints via Input Ports.
   - Addresses: Exposing all the remaining CLI features over REST.
   - Avoids: Doing everything at once. Breaking down by domain aggregate simplifies testing.

**Research flags for phases:**
- Phase 3: Likely needs deeper research on token lifecycle management (e.g., how long does a save stay in memory before eviction?).

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | Spring Boot 3.4 and Java 21 are the standard for 2025/2026. |
| Features | HIGH | Clear mapping of CLI features to REST endpoints. |
| Architecture | HIGH | Hexagonal Architecture is well documented and ArchUnit is standard for enforcement. |
| Pitfalls | HIGH | State management without a DB is a known pattern solved by standard caches. |

## Gaps to Address

- **Eviction Strategy**: What happens if a user uploads a save but never downloads it? The Cache eviction policy needs to be defined (e.g., evict after 1 hour of inactivity).
