# Feature Landscape

**Domain:** REST API for in-memory game save editing
**Researched:** 2026-04-05

## Table Stakes

Features users expect. Missing = product feels incomplete.

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| Upload Save | To initiate an editing session. | Low | Must return a session token or ID for subsequent edits. |
| Download Save | To retrieve the modified save file. | Low | Returns the binary `.sav` file matching the session ID. |
| Edit Team/Player | Core domain of a soccer manager game. | Med | Exposing CRUD-like endpoints over in-memory state. |
| Edit Manager Data | Modifying user profile in the save. | Low | Simple scalar edits. |
| Edit Tournament Data | Modifying league states. | High | Involves complex interrelated game structures. |
| OpenAPI 3 Docs | Crucial for backend-only APIs without a UI. | Low | Auto-generated via `springdoc-openapi-starter-webmvc-ui`. |

## Differentiators

Features that set product apart. Not expected, but valued.

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| Batch Edits | Allows applying multiple changes to a save in a single request. | High | Reduces HTTP overhead and complexity for automated scripts. |
| Save Validation | Checks if a file is corrupted before attempting to load it. | Med | Prevents internal server errors and bad UX. |

## Anti-Features

Features to explicitly NOT build.

| Anti-Feature | Why Avoid | What to Do Instead |
|--------------|-----------|-------------------|
| Persistent Database (JPA) | Explicitly out of scope in PROJECT.md. Unnecessary overhead. | Use in-memory state (Caffeine Cache) tied to an edit session token. |
| CLI / Web UI | The goal is a clean REST API. UI creates coupling. | Rely on the OpenAPI Swagger UI for manual testing. |
| User Authentication | Unnecessary for a simple file manipulation tool (unless it becomes a multi-tenant SaaS). | Rely on opaque session IDs/tokens for isolation. |

## Feature Dependencies

```
Upload Save → Edit Features (Team, Player, Manager, Tournament) → Download Save
OpenAPI Docs → API Consumption
```

## MVP Recommendation

Prioritize:
1. Upload Save (Initialization)
2. Download Save (Finalization)
3. Edit Team/Player (Core Editing)
4. OpenAPI 3 Documentation

Defer: Batch Edits, Edit Tournament Data (more complex and requires deeper domain mapping).

## Sources

- .planning/PROJECT.md requirements (HIGH confidence)
