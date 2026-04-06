---
phase: 04
slug: add-lombok-and-create-real-domains-with-self-validation-fix
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-04-06
---

# Phase 04 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 + Mockito + ArchUnit |
| **Config file** | `pom.xml` |
| **Quick run command** | `mvn -q -Dtest=HexagonalArchitectureTest,ManagerManagementServiceTest test` |
| **Full suite command** | `mvn -q test` |
| **Estimated runtime** | ~45 seconds |

---

## Sampling Rate

- **After every task commit:** Run `mvn -q -Dtest=HexagonalArchitectureTest,ManagerManagementServiceTest test`
- **After every plan wave:** Run `mvn -q test`
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 60 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 04-01-01 | 01 | 1 | TBD | T-04-01 | Build does not fail after Lombok processor activation | compile+arch | `mvn -q -Dtest=HexagonalArchitectureTest test` | ✅ | ⬜ pending |
| 04-01-02 | 01 | 1 | TBD | T-04-02 | Architecture rules block forbidden package coupling | arch | `mvn -q -Dtest=HexagonalArchitectureTest test` | ✅ | ⬜ pending |
| 04-02-01 | 02 | 2 | TBD | T-04-03 | Invalid domain values are rejected before persistence | unit | `mvn -q -Dtest=ManagerManagementServiceTest test` | ✅ | ⬜ pending |
| 04-02-02 | 02 | 2 | TBD | T-04-04 | Services preserve API-facing validation error behavior | unit+compile | `mvn -q -Dtest=ManagerManagementServiceTest test` | ✅ | ⬜ pending |
| 04-03-01 | 03 | 3 | TBD | T-04-05 | Service contracts prevent direct concrete coupling | arch+compile | `mvn -q -Dtest=HexagonalArchitectureTest test` | ✅ | ⬜ pending |
| 04-03-02 | 03 | 3 | TBD | T-04-06 | Spring wiring remains deterministic with interfaces | compile | `mvn -q -DskipTests package` | ✅ | ⬜ pending |
| 04-04-01 | 04 | 4 | TBD | T-04-07 | Record/package reorganization cannot bypass controller validation | compile+unit | `mvn -q test` | ✅ | ⬜ pending |
| 04-04-02 | 04 | 4 | TBD | T-04-08 | End-to-end build still compiles after import rewiring | full | `mvn -q test && mvn -q -DskipTests package` | ✅ | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

Existing infrastructure covers all phase requirements.

---

## Manual-Only Verifications

All phase behaviors have automated verification.

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 60s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
