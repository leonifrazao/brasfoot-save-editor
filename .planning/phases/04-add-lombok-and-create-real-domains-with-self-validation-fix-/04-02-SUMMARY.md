---
phase: 04-add-lombok-and-create-real-domains-with-self-validation-fix-
plan: 02
subsystem: domain
tags: [domain-model, validation, lombok, tdd]
requires:
  - phase: 04-01
    provides: Lombok configuration and boundary contracts
provides:
  - Self-validating Team/Player/Manager domain construction
  - TDD coverage for core domain invariant failures
  - Service validation flow routed through domain objects
affects: [04-03, 04-04]
tech-stack:
  added: []
  patterns: [constructor invariants, service-to-domain validation handoff]
key-files:
  created:
    - src/test/java/br/com/saveeditor/brasfoot/domain/DomainValidationTest.java
  modified:
    - src/main/java/br/com/saveeditor/brasfoot/domain/Team.java
    - src/main/java/br/com/saveeditor/brasfoot/domain/Player.java
    - src/main/java/br/com/saveeditor/brasfoot/domain/Manager.java
    - src/main/java/br/com/saveeditor/brasfoot/domain/SaveContext.java
    - src/main/java/br/com/saveeditor/brasfoot/application/services/TeamManagementService.java
    - src/main/java/br/com/saveeditor/brasfoot/application/services/PlayerManagementService.java
    - src/main/java/br/com/saveeditor/brasfoot/application/services/ManagerManagementService.java
    - src/test/java/br/com/saveeditor/brasfoot/application/services/ManagerManagementServiceTest.java
key-decisions:
  - "Kept IllegalArgumentException as the domain validation exception type to preserve HTTP 400 mapping behavior."
  - "Added Manager.validate() and Manager.of(...) for invariant checks while keeping mutable compatibility where controllers still need setter-style mapping."
patterns-established:
  - "Validation belongs to domain constructors/builders, not repeated service conditionals."
requirements-completed: [PH4-02]
duration: 24min
completed: 2026-04-06
---

# Phase 4 Plan 02: Domain Hardening Summary

**Team, Player, and Manager now enforce invariant validation at domain construction time, and management services delegate input validation to those domain paths.**

## Performance

- **Duration:** 24 min
- **Started:** 2026-04-06T01:24:00Z
- **Completed:** 2026-04-06T01:48:00Z
- **Tasks:** 2
- **Files modified:** 8

## Accomplishments
- Added failing-first domain tests for invalid Team/Player/Manager creation paths.
- Implemented domain-level invariants with Lombok-backed constructors/builders.
- Rewired management services to perform validation through domain object creation before reflective writes.

## Task Commits
1. **Task 1 (RED): Add failing domain invariant tests** - `55fb01e` (test)
2. **Task 1 (GREEN): Add domain invariants with Lombok-backed models** - `f384ffb` (feat)
3. **Task 2 (GREEN): Rewire management services to domain validation paths** - `d13025a` (feat)

## Files Created/Modified
- `src/test/java/br/com/saveeditor/brasfoot/domain/DomainValidationTest.java` - TDD coverage for domain constructor validation.
- `src/main/java/br/com/saveeditor/brasfoot/domain/Team.java` - Money invariant enforcement.
- `src/main/java/br/com/saveeditor/brasfoot/domain/Player.java` - Age/overall/position/energy/morale invariants.
- `src/main/java/br/com/saveeditor/brasfoot/domain/Manager.java` - Confidence invariants with validate helper.
- `src/main/java/br/com/saveeditor/brasfoot/domain/SaveContext.java` - Lombok getter/no-args modernization.
- `src/main/java/br/com/saveeditor/brasfoot/application/services/*ManagementService.java` - Validation delegation through domain paths.
- `src/test/java/br/com/saveeditor/brasfoot/application/services/ManagerManagementServiceTest.java` - Updated to port-based mocking and new domain validation behavior.

## Decisions Made
- None beyond planned architecture direction.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] RED test compile failed due to missing Lombok builder on Manager**
- **Found during:** Task 1 (RED)
- **Issue:** New domain tests referenced `Manager.builder()` before domain conversion.
- **Fix:** Converted Manager to Lombok-backed class with `@Builder` and validation hooks.
- **Files modified:** `src/main/java/br/com/saveeditor/brasfoot/domain/Manager.java`
- **Verification:** `mvn -q -Dtest=DomainValidationTest test`
- **Committed in:** `f384ffb`

**2. [Rule 3 - Blocking] Manager service tests failed after port swap because manager list port was unstubbed**
- **Found during:** Task 2
- **Issue:** `gameDataPort.getManagers(root)` returned null in tests, causing false negatives.
- **Fix:** Stubbed `gameDataPort.getManagers(rootObject)` in test setup.
- **Files modified:** `src/test/java/br/com/saveeditor/brasfoot/application/services/ManagerManagementServiceTest.java`
- **Verification:** `mvn -q -Dtest=ManagerManagementServiceTest,HexagonalArchitectureTest test`
- **Committed in:** `d13025a`

---

**Total deviations:** 2 auto-fixed (Rule 3)
**Impact on plan:** Both fixes were prerequisite to complete planned TDD and service rewiring without changing external behavior.

## Issues Encountered
- None beyond the blocking test harness adaptation.

## Next Phase Readiness
- Domain invariants are now stable foundations for full interface consistency and package-level record moves.

## Known Stubs
- `src/main/java/br/com/saveeditor/brasfoot/application/services/PlayerManagementService.java:269` — morale is still hardcoded to 100 when mapping from save objects due to missing confirmed obfuscated source field.

## Self-Check: PASSED
