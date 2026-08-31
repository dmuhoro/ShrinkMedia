# ShrinkMedia Roadmap

> Living roadmap. Every phase states its exit gate (a measured, evidencible
> condition). A phase is not "done" on narrative alone (Constitution Article
> VII). Capability status is tracked in `docs/current-state.md`. The step-by-step
release path (with per-step status and exit gates) lives in
`docs/release-roadmap.md`; the literal go/no-go table lives in
`docs/release-readiness.md`.

## North Star

**Every file a user owns can be made smaller, tidier, and more portable on
their own device — no uploads, no subscriptions, no compromises.**

Privately and permanently. Storage savings, running minutes, and files
processed are the leading indicators.

## Phase 0 — Engineering Foundation (DONE)

Elite governance scaffold, build verification, and quality gates.

- **Gate:** `docs/` governance tree present; web `lint`/`test`/`build` green;
  CI workflow merged; evidence recorded in `docs/evidence/`.

## Phase 1 — Native Media & Document Toolkit (DONE)

The Android app is a working local toolkit: single + batch compression in a
foreground service, quality presets, autosave to MediaStore, battery-aware
pause, PDF merge/split/build, and local text extraction.

- **Gate:** `assembleDebug` green on JDK 17 / SDK 35; core functions return
  typed results; pause/resume never drops a queued item.

## Phase 2 — Test Hardening & AICore Handoff (IN PROGRESS)

Local instrumentation tests for the real compression paths (written and
compiling in Sprint 7 — JVM unit tests green), a JVM-safe unit seam for
`SettingsDataStore`, and the Android PDF text pipeline (parts marked
ASPIRATIONAL that have no verified implementation yet).

- **Gate:** the real-path instrumented suite runs green on a device or emulator
  (final **Sprint 8** verification gate); a plan for the AICore availability
  check (none ships until a real implementation exists).

## Phase 3 — Simulator Discipline & Web Parity

Make the web simulator a faithful, honest preview of the native behaviour —
no implied features the Android app does not implement. Pure helpers
(compression ratio math, size formatting, batch/savings model) live in a
testable lib module and are covered by unit tests.

- **Gate:** every simulator control maps to a verified native capability;
  simulator tests pass (18 green); the cross-reference in
  `docs/sprint-cross-reference.md` lists no unverified claims.

## Phase 4 — Release & Distribution (IN PROGRESS)

Production namespace `com.shrinkmedia.compressor`, R8-minified signed release
APK/AAB build, launcher icon, and hardened CI with a signed release-AAB job
are done (Sprint 7). Remaining: provision a real keystore, publish to a closed
track, and complete the evidence-backed readiness table.

- **Gate:** Play Console closed-track AAB built from a real signed release key;
  `docs/release-readiness.md` fully PASS with citations (device verification
  completed in Sprint 8).

## Out of Scope (explicitly descoped — do not re-include silently)

- Any networked/cloud compression (violates the on-device invariant).
- OCR for scanned images until a real local engine exists (ASPIRATIONAL).
- iOS/desktop ports before Phases 1–3 are verified.