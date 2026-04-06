# Phase 3: Manager & Batch Operations - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-04-05
**Phase:** 03-manager-batch-operations
**Areas discussed:** Batch Request Format, Batch Error Handling, Manager Editable Attributes, Relative vs Absolute Edits, Batch Logging, Swagger Batch UI

---

## Batch Request Format

| Option | Description | Selected |
|--------|-------------|----------|
| Custom Bulk Object | Easier to map to existing models and strongly typed (Recommended) | ✓ |
| JSON Patch (RFC 6902) | Standardized but complex to parse for nested binary structures | |
| Array of Commands | List of explicit edit actions | |

**User's choice:** Custom Bulk Object

| Option | Description | Selected |
|--------|-------------|----------|
| Hard limit (e.g. 1000 items) | Return 400 if payload exceeds limit (Recommended) | ✓ |
| No limit (Synchronous) | Keep it simple, rely on HTTP timeout/body limits | |
| Async Job Processing | Return 202 Accepted, check status via GET | |

**User's choice:** Hard limit (e.g. 1000 items)

| Option | Description | Selected |
|--------|-------------|----------|
| Typed endpoints | Cleaner REST, separation of concerns (Recommended) | ✓ |
| Mixed payload | Update multiple entity types in one go | |

**User's choice:** Typed endpoints

---

## Batch Error Handling

| Option | Description | Selected |
|--------|-------------|----------|
| Partial Success (207) | Apply what works, return 207 with error details (Better DX) | ✓ |
| Transactional (Fail all) | Reject entire batch if any item is invalid, return 400 | |

**User's choice:** Partial Success (207)

| Option | Description | Selected |
|--------|-------------|----------|
| List of errors with indices | Map error to original request index or ID | ✓ |
| Status per item | Return status for every item, successful or not | |

**User's choice:** List of errors with indices

---

## Manager Editable Attributes

| Option | Description | Selected |
|--------|-------------|----------|
| All editable | Allow full control over the manager's career (Recommended) | ✓ |
| Core attributes only | Restrict to basic profile details | |
| Core + Stats | Allow editing historical stats as well | |

**User's choice:** All editable

| Option | Description | Selected |
|--------|-------------|----------|
| Team assignment read-only | Keep simple, team assignment is read-only | ✓ |
| Yes, update currentTeamId | Updating teamId simply changes their club in save | |
| Require dedicated endpoint | Require a dedicated transfer endpoint to handle side-effects | |

**User's choice:** Team assignment read-only

---

## Relative vs Absolute Edits

| Option | Description | Selected |
|--------|-------------|----------|
| Absolute Only | Simpler REST payload, state is declarative (Recommended) | ✓ |
| Support Both | Support relative operations (+50) and absolute sets | |

**User's choice:** Absolute Only

---

## Batch Logging

| Option | Description | Selected |
|--------|-------------|----------|
| Summary log only | Summary of success/fail counts (Recommended) | ✓ |
| Verbose (Log every item) | Log every individual entity update in the batch | |

**User's choice:** Summary log only

---

## Swagger Batch UI

| Option | Description | Selected |
|--------|-------------|----------|
| Yes, full examples | Provide realistic bulk payloads for testing (Recommended) | ✓ |
| No, minimal examples | Keep it concise, only show the structure | |

**User's choice:** Yes, full examples

---

## the agent's Discretion

None explicitly requested.

## Deferred Ideas

None.
