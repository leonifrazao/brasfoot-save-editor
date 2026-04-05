# External Integrations

**Analysis Date:** 2026-04-05

## APIs & External Services

**Cloud APIs:**
- None detected. The application functions entirely offline.

## Data Storage

**Databases:**
- None detected.

**File Storage:**
- Local filesystem only
  - Reads and writes Brasfoot save game files via `com.brasfoot:brasfoot-game` and Kryo.
  - Configuration/Presets handled locally with `gson`.

**Caching:**
- None detected.

## Authentication & Identity

**Auth Provider:**
- None detected. This is a local desktop CLI application.

## Monitoring & Observability

**Error Tracking:**
- None.

**Logs:**
- Spring Boot default logging
  - Configuration set via `src/main/resources/application.properties` (e.g., `logging.level.root=ERROR`).
  - Output to console and potentially `spring-shell.log`.

## CI/CD & Deployment

**Hosting:**
- Desktop Application (Local execution)

**CI Pipeline:**
- None explicitly defined (No GitHub Actions or Jenkins files detected).

## Environment Configuration

**Required env vars:**
- None detected. Relying primarily on local configuration files (`config.properties`, `src/main/resources/application.properties`).

**Secrets location:**
- Not applicable.

## Webhooks & Callbacks

**Incoming:**
- None

**Outgoing:**
- None

---

*Integration audit: 2026-04-05*