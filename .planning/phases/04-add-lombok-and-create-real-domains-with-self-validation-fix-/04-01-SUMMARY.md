---
phase: 04-add-lombok-and-create-real-domains-with-self-validation-fix-
plan: 01
subsystem: api
tags: [lombok, maven, archunit, hexagonal]
requires: []
provides:
  - Lombok build-time annotation processing
  - GameDataPort read contract
  - Manager batch use-case contract baseline
  - New ArchUnit boundary rules for application/domain layers
affects: [04-02, 04-03, 04-04]
tech-stack:
  added: [org.projectlombok:lombok]
  patterns: [port-first dependency injection, architecture test hardening]
key-files:
  created:
    - src/main/java/br/com/saveeditor/brasfoot/application/ports/out/GameDataPort.java
    - src/main/java/br/com/saveeditor/brasfoot/application/ports/in/BatchUpdateManagerUseCase.java
    - src/main/java/br/com/saveeditor/brasfoot/application/ports/in/ManagerBatchUpdateCommand.java
  modified:
    - pom.xml
    - src/test/java/br/com/saveeditor/brasfoot/architecture/HexagonalArchitectureTest.java
key-decisions:
  - "Pinned Lombok version and annotation processor path in Maven for deterministic CI compilation."
  - "Introduced strict architecture rules with temporary migration exclusions to avoid breaking in-flight refactors."
patterns-established:
  - "Application layer consumes ports, not concrete service package classes."
requirements-completed: [PH4-01, PH4-03]
duration: 18min
completed: 2026-04-06
---

# Phase 4 Plan 01: Foundation Contracts Summary

**Lombok-aware Maven compilation and interface-first architecture constraints were established to safely drive the remaining Phase 4 refactors.**

## Performance

- **Duration:** 18 min
- **Started:** 2026-04-06T01:22:00Z
- **Completed:** 2026-04-06T01:40:00Z
- **Tasks:** 3
- **Files modified:** 5

## Accomplishments
- Added Lombok to dependencies and Maven compiler annotation processing.
- Created `GameDataPort` and manager batch input contracts.
- Extended ArchUnit tests with `application_must_not_depend_on_service_package` and `domain_must_not_depend_on_model_package`.

## Task Commits
1. **Task 1: Enable Lombok in Maven build** - `53969c5` (chore)
2. **Task 2: Add interface-first contracts for service consistency** - `3eab54e` (feat)
3. **Task 3: Tighten architecture guardrails for Phase 4** - `34555c1` (test)

## Files Created/Modified
- `pom.xml` - Adds Lombok dependency and compiler annotation processor path.
- `src/main/java/br/com/saveeditor/brasfoot/application/ports/out/GameDataPort.java` - Outbound read port contract.
- `src/main/java/br/com/saveeditor/brasfoot/application/ports/in/BatchUpdateManagerUseCase.java` - Manager batch use-case interface.
- `src/main/java/br/com/saveeditor/brasfoot/application/ports/in/ManagerBatchUpdateCommand.java` - Initial manager batch command type.
- `src/test/java/br/com/saveeditor/brasfoot/architecture/HexagonalArchitectureTest.java` - New boundary rules for application/domain dependency constraints.

## Decisions Made
- Added transitional ArchUnit exclusions for known legacy classes to enforce future-state rules without blocking wave progression.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Maven annotation processor resolution failed without explicit Lombok version**
- **Found during:** Task 1
- **Issue:** `maven-compiler-plugin` rejected annotation processor path because Lombok version was implicit.
- **Fix:** Added `lombok.version` property and referenced it in dependency + annotationProcessorPaths.
- **Files modified:** `pom.xml`
- **Verification:** `mvn -q -DskipTests compile`
- **Committed in:** `53969c5`

---

**Total deviations:** 1 auto-fixed (Rule 3)
**Impact on plan:** No scope creep; required to make planned Lombok build configuration functional.

## Issues Encountered
- None beyond the Maven processor version fix.

## Next Phase Readiness
- Port contracts and architecture guardrails are now in place for domain hardening and service rewiring.

## Self-Check: PASSED
