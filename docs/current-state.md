# ShrinkMedia — Current State

> Orientation doc: **what is real vs ASPIRATIONAL**, as of 2026-08-30.
> Read before any task (AGENTS.md §7). If a task assumes a capability marked
> **ASPIRATIONAL**, stop and say so before building on it.

Legend: ✅ implemented & verified • ⚠️ implemented, device run pending •
🟡 designed/partial • ⚠️ **ASPIRATIONAL** not wired.

## Capability Matrix

| # | Capability | Status | Evidence / Notes |
|---|-----------|--------|------------------|
| C1 | Engineering governance scaffold | ✅ | Constitution, ADRs, decisions, sprints, evidence, CI |
| C2 | Web simulator (Vite+React+TS) lint/build/tests | ✅ | `npm run lint` / `npm test` / `npm run build` green (evidence log) |
| C3 | Image compression (bitmap sampling + JPEG) | ⚠️ | Helper compiled; JVM-verified math mirror in `src/lib`; device run pending |
| C4 | Video compression (FFmpegKit Lite, preset CRF/bitrate) | ⚠️ | `compressVideoFile` awaits session state; device run pending |
| C5 | Foreground batch service + progress notification | ⚠️ | `BatchCompressionService`; needs device run to verify notification UX |
| C6 | Battery-aware pause/resume (opt-in) | ⚠️ | Controller + receiver; **never drops queue**; device run pending |
| C7 | Autosave to MediaStore (opt-in, no permission) | ⚠️ | `saveToPublicMediaStore` returns Boolean; device run pending |
| C8 | DataStore settings persistence | ✅ | `SettingsRepository` flow; additive keys; fail-closed defaults |
| C9 | PDF build / merge / split / metrics | ⚠️ | `android.graphics.pdf`; device run pending |
| C10 | PDF embedded-text extraction | ⚠️ | Heuristic; scanned PDFs report "needs OCR" honestly |
| C11 | AICore device-model handoff | **ASPIRATIONAL** | `AiTab` placeholder only — no AICore dependency, no availability check |
| C12 | Real OCR (scanned PDFs/images) | **ASPIRATIONAL** | No local OCR engine wired; refusing to fake it is by design (D001 FAQ) |
| C13 | Signed `assembleRelease` + closed-track publish | **ASPIRATIONAL** | No release signing key wired yet (ROADMAP Phase 4) |
| C14 | Android instrumentation tests on device | 🟡 | Web-side JVM tests exist; instrumented coverage is Phase 2 |

## What This Means For New Work

- **The on-device invariant is structural:** the manifest has no INTERNET
  permission. Adding network = ADR (Article II.2).
- **Compression/PDF helpers are shippable but device-untested.** The next
  verification pass (Phase 2) must exercise the real paths on hardware and
  record it in `docs/evidence/`.
- **Nothing under C11–C13 may be claimed as delivered.** They remain
  ASPIRATIONAL until implemented **and** verified.

## Sprint Cross-Reference

Every claim above is audited sprint-by-sprint in
`docs/sprint-cross-reference.md`. The release gate table lives in
`docs/release-readiness.md`.