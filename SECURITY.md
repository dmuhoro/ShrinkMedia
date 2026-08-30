# ShrinkMedia Security

## Threat Model

ShrinkMedia is a private, on-device media & document toolkit. There is no
server, no database in the cloud, and no user data leaves the device.

### Assets

| Asset | Where it lives | Value |
|-------|----------------|-------|
| User media (photos, videos, PDFs) | Device storage; read-only via scoped storage URIs | High — private user content |
| Compressed output files | App cache / public MediaStore folders | Medium — derived from user media |
| App settings (quality, autosave, theme, totals) | DataStore preferences on-device | Low |
| `.env` contents (Gemini key, APP_URL for the web simulator) | Developer environment only | Medium — never committed |

### Threats

| # | Threat | Vector | Existing control |
|---|--------|--------|------------------|
| T1 | User media exfiltrated to a remote service | Malicious/accidental network call from the app | Android manifest declares **no INTERNET permission**; ALL processing is in-process on-device |
| T2 | Secrets committed to git | Developer copies `.env` into the repo | `.gitignore` excludes `.env*` (keeps `.env.example`); CI secret hygiene |
| T3 | Silent data loss (failed compression/autosave reported as success) | Defect in compression/autosave path | Typed `null` results on every conversion path; callers surface failures (AGENTS.md §1.4, §4) |
| T4 | Batch dropped on pause/resume | Pause logic skip bug | `BatchCompressionPauseController.isPaused` single source of truth; queue never drops items |
| T5 | Malicious apps reading output in public MediaStore folders | Shared storage exposure | Output written to app-private cache by default; user opt-in to public folders |

### Out of scope

- Multi-tenant/multi-user isolation (there is no server or shared database).
- OWASP network-layer controls (there is no network surface in the app).
- Web-simulator transport security — it is a local dev harness.

## Data Classification

| Class | Examples | Handling |
|-------|----------|----------|
| Private (user media) | Photos, videos, PDFs | Never uploaded. Read via scoped-storage URIs with user-granted access only. |
| Derived | Compressed files, audit logs | Written to app cache; moved to public MediaStore only when the user opts in. |
| Settings | Quality presets, theme, totals | DataStore preferences; booleans default to `false`, quality to `MEDIUM`. |
| Secrets | `GEMINI_API_KEY`, `APP_URL` | Environment-only; `.env*` is gitignored; never commit. |

## Secret Handling

- `.env` / `.env.local` / `.env.production` are gitignored (`.env.example` is
  the committed template with placeholder values).
- Keys are injected at runtime in the AI Studio / dev environment; no real key
  ever appears in the repository, CI logs, or evidence files.
- If a key is ever committed, rotate it immediately and rewrite history.

## Vulnerability Reporting

This is a private project. Report suspected vulnerabilities privately to the
maintainer (Daniel Muhoro) via a direct, non-public channel — do not open a
public issue. Include: the file and line, the exploit scenario, and whether
real user data could be at risk.

## Supported Versions

Only the latest `main` and the most recent tagged release receive security
fixes. No security support is offered for pre-release builds.