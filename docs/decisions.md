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

## D005 — iText temp-file pattern for reliable Android random access (ACCEPTED)

- **Date:** 2026-09-01 • **Decider:** Daniel Muhoro

`PdfReader` constructors in iText 7's Android AAR do not accept `byte[]`
directly (the `RandomAccessSourceFactory.createBestSource(byte[])` overload is
unavailable). The reliable pattern is: write each input `Uri` to a temp file in
the app cache dir → `PdfReader(File)` → operate (`copyPagesTo`,
`PdfTextExtractor`) → delete temp file. This applies to `mergePdfDocuments`
and `extractRawTextFromUri`. `createPdfFromImages` avoids the issue by building
from bitmaps in memory (single `Document` + `AreaBreak` per page). The pattern
is fail-closed: any I/O failure returns `null`/empty and is surfaced to the UI.

---

## D006 — PDF text fidelity: layout-aware extraction, not pixel-perfect (ACCEPTED)

- **Date:** 2026-09-01 • **Decider:** Daniel Muhoro

`extractRawTextFromUri` moved from `SimpleTextExtractionStrategy` to
`LocationTextExtractionStrategy`, which uses approximate glyph X/Y coordinates
to reconstruct paragraphs/columns more faithfully, and adds a `--- Page N ---`
header between pages. This improves layout resemblance but is **not** identical
to the source document. True pixel-perfect fidelity would require rendering
each page (`PdfRenderer`) rather than extracting the text layer. This honesty
gate was accepted: we document the limitation rather than over-claim "identical
output."

---

## D007 — Media deletion stays user-consented and fail-closed (ACCEPTED)

- **Date:** 2026-09-02 • **Decider:** Daniel Muhoro

Deleting originals from "Your media library" is destructive, so the app never deletes
without a visible consent path. API 30+ uses `MediaStore.createDeleteRequest`, which
returns a `PendingIntent` for the **system** consent dialog (launched via
`ActivityResultContracts.StartIntentSenderForResult`); any non-`RESULT_OK` outcome is
surfaced ("Delete cancelled — no files were changed") and nothing is dropped silently.
API <30 falls back to `deleteLegacy`, a typed direct `contentResolver.delete` with
explicit full / partial / failure toasts. The confirm `AlertDialog` precedes every
delete request. `buildDeleteRequest` returns `null` below API 30 and on an empty
selection (fail-closed: no request is ever built without a guarded path).

---

## Superseded

- None yet. When an ADR supersedes a D-entry, the entry is marked `SUPERSEDED`
  and links the ADR.

## FAQ

- **Why no INTERNET permission?** The privacy invariant is structural, not
  aspirational (Constitution Article II).
- **What happened to "OCR is ASPIRATIONAL"?** Originally true (no local engine
  wired, D001 FAQ). It is now **implemented** via ADR-009 — on-device ML Kit
  `text-recognition`, typed-null `OcrHelper`, no INTERNET — superseding that
  note. It still awaits the Sprint 8 device run for full verification. AICore
  handoff remains ASPIRATIONAL and is staged as v2 (ADR-010).