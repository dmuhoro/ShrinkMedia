# Decisions Log

Informal decision entries (D-series) that predate the formal ADR sequence, plus
superseded/FAQ items. Formal architecture decisions live in `docs/adr/`.

> Format: `D### — <Title>` (Status / Date / Decider), Context, Decision,
> Consequences. Superseded entries are marked `SUPERSEDED` and link the ADR.

---

## D001 — Rapid-prototype first, harden second (ACCEPTED)

- **Date:** 2026-08-30 • **Decider:** Daniel Muhoro

The Android app shipped as a working single-file prototype before being
refactored into the layered toolkit. Keeping the prototype green while the
architecture matured is what made the `MainActivity` consolidation possible
without a feature regression. This repo keeps the "iterate to correctness"
rule: land working code, cite it, then refactor with tests.

## D002 — Single entry `SettingsRepository`, no shotgun persistence (ACCEPTED)

- **Date:** 2026-08-30 • **Decider:** Daniel Muhoro

All settings go through `SettingsRepository`. This stops feature components
from opening DataStore directly (mirrors the repository-only pattern from the
Daftari reference). Supersedes any ad-hoc persistence handled by UI code.

## D003 — Typed `null` failure contract for all conversion paths (ACCEPTED)

- **Date:** 2026-08-30 • **Decider:** Daniel Muhoro

`compressImageFile` / `compressVideoFile` return `File?`; the service and
ViewModel both `requireNotNull`/guard and surface the message. Never swallow.
This is now enforced by Constitution Article III.

## D004 — Pause/resume is process-wide via a controller, not UI-local (ACCEPTED)

- **Date:** 2026-08-30 • **Decider:** Daniel Muhoro

`BatchCompressionPauseController.isPaused` is a process-wide
`MutableStateFlow` shared by the service and the ViewModel. Local UI flags
would desync progress from the actual queue.

---

## Superseded

- None yet. When an ADR supersedes a D-entry, the entry is marked `SUPERSEDED`
  and links the ADR.

## FAQ

- **Why no INTERNET permission?** The privacy invariant is structural, not
  aspirational (Constitution Article II).
- **Why is OCR ASPIRATIONAL?** Extracting text from scans needs a real local
  OCR engine ("tess") which is not yet wired; claiming otherwise would be false
  confidence.