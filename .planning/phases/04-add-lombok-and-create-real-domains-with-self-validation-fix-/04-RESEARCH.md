# Phase 04 — Research

**Date:** 2026-04-06  
**Phase:** add lombok and create real Domains, with self validation. fix project inconsistency like 2 services, all the services/usecase need a interface; Organize the “record” and “domain” folders.

## Research Question

What is the safest implementation path to:
1. add Lombok,
2. move to real self-validating domain models,
3. enforce service/use-case interface consistency,
4. separate `domain` and `record` packages clearly,
without breaking current Spring Boot + Hexagonal behavior?

## Current State Findings

1. **Lombok is not configured** in `pom.xml`.
2. **Domain is mixed style**:
   - Records: `Team`, `Player`, `Session`
   - Mutable class: `Manager`, `SaveContext`
3. **Validation currently lives in services**, not in entities (e.g., `PlayerManagementService`).
4. **Service layer inconsistency exists**:
   - `application/services/*` follows port interfaces (`*UseCase`) and hexagonal style.
   - `service/*` contains concrete classes with no interfaces and direct utility coupling.
5. **Record usage is dispersed** (DTO records and command records), with no dedicated package convention for records.

## Recommended Technical Direction

### 1) Lombok introduction (low risk)

- Add Lombok dependency with `provided` scope.
- Configure `maven-compiler-plugin` annotation processor path for reproducible CI builds.
- Keep generated methods explicit only where necessary for compatibility.

### 2) Self-validating domain model (medium risk)

- Convert mutable/partial domains to constructor-based invariants.
- Centralize guard rules in domain factory/constructor methods:
  - Team money must be `>= 0`
  - Player fields (age, overall, position, energy) validated in domain, not only services
  - Manager confidence fields range-checked
- Keep business invariants in domain; keep reflective field mapping in services/adapters.

### 3) Service/use-case interface consistency (medium risk)

- Preserve existing `application.ports.in` interfaces.
- For concrete `service/*` classes, introduce interfaces under a dedicated contract package (e.g., `application/ports/domain` or `service/contracts`) and wire Spring beans via interfaces.
- Avoid introducing adapter dependencies into application/domain packages.

### 4) `record` vs `domain` folder separation (medium risk)

- Keep `domain` for behavior-rich entities/value objects.
- Move pure data-carrier Java records (especially API/command contracts) to explicit `record` package trees (e.g., `adapters/in/web/record`, `application/ports/in/record`).
- Update imports in controllers/services accordingly.

## Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Lombok annotation processing differs in IDE/CI | Build drift | Configure compiler plugin annotation processor explicitly |
| Large refactor breaks imports | Compile failures | Split into waves with compile/test at each wave |
| Validation migration changes API errors | Behavior regressions | Preserve exception semantics and add targeted unit tests |
| Interface extraction causes wiring ambiguity | Runtime startup failures | Keep one implementation per interface, verify Spring context starts |

## Test Strategy

1. Run fast architecture + targeted service tests after each plan:
   - `mvn -q -Dtest=HexagonalArchitectureTest,ManagerManagementServiceTest test`
2. Run full test suite at end:
   - `mvn -q test`
3. Run compile/package gate:
   - `mvn -q -DskipTests package`

## Validation Architecture

Validation will be Nyquist-style sampled at task boundaries:

- **After each task:** run targeted tests for touched package(s).
- **After each plan:** run phase-level command (`mvn -q -Dtest=... test`).
- **Before execute-phase handoff complete:** run full suite + package command.

Wave 0 is not required because test infrastructure already exists (`spring-boot-starter-test`, ArchUnit, Mockito).

## Planning Guidance

Use **3 execute plans**:

1. **Foundation contracts:** Lombok setup + architecture rule updates for package boundaries.
2. **Domain hardening:** self-validating domain entities and corresponding tests.
3. **Structural consistency:** extract interfaces for legacy services and move record carriers to dedicated record packages.

This keeps each plan within context budget and allows controlled verification between structural and behavioral changes.
