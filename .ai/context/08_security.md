# 08 — Security

## Purpose
Security posture for the on-device invariant and the web harness.

## Authority Level
Foundational — Constitution Article II.

## Consumers
All agents; auditor.

## Source Documents
`docs/` `SECURITY.md`, `docs/adr/ADR-005`.

## Update Rules
Update when the threat model or permissions change.

---

## Core stance
No server, no cloud DB, no uploads. The app declares **no INTERNET
permission** — this is the structural control.

| Threat | Control |
|---|---|
| Media exfiltration | No INTERNET permission; scoped-storage URIs only |
| PII/name leaks | Audit surface is user-visible; no filenames in logs beyond it |
| Secret commits | `.env` gitignored; no runtime secrets for the web simulator |
| Silent data loss | Typed `null` results surfaced; autosave returns Boolean |
| Batch drops | `isPaused` single source of truth; item never skipped |

## Secrets
The web simulator is a pure static front-end — it reads no runtime secrets.
The native app and simulator never require API keys.

## Guard commands (auditor)
```bash
rg -n "android\.permission\.INTERNET" app/src/main/AndroidManifest.xml   # must be empty
git check-ignore .env.local                                           # must match
```