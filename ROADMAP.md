# ShrinkMedia Roadmap

> Living roadmap. Every phase states its exit gate (a measured, evidencible
> condition). A phase is not "done" on narrative alone (Constitution Article
> VII). Capability status is tracked in `docs/current-state.md`.

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

Decide and record: local instrumentation tests for the real compression paths,
a JVM-safe unit seam for `SettingsDataStore`, and the Android PDF text pipeline
(mark parts ASPIRATIONAL that have no verified implementation yet).

- **Gate:** at least one instrumentation test exercising the actual
  `compressImageFile` / `saveToPublicMediaStore` path on a device or emulator;
  a plan for the AICore availability check (none of this ships inside the
  6-hour window).

## Phase 3 — Simulator Discipline & Web Parity

Make the web simulator a faithful, honest preview of the native behaviour —
no implied features the Android app does not implement. Extract the pure
helpers (compression ratio math, size formatting) into a testable lib module
(already done), and add integration tests for the batch/undo/audit flows.

- **Gate:** every simulator control maps to a verified native capability;
  simulator tests pass; the cross-reference in `docs/sprint-cross-reference.md`
  lists no unverified claims.

## Phase 4 — Release & Distribution

First signed `assembleRelease` build; AAB published to a closed track; release
notes; `docs/release-readiness.md` fully PASS with citations.

- **Gate:** Play Console closed-track AAB with a signed release key and an
  evidence-backed readiness table.

## Out of Scope (explicitly descoped — do not re-include silently)

- Any networked/cloud compression (violates the on-device invariant).
- OCR for scanned images until a real local engine exists (ASPIRATIONAL).
- iOS/desktop ports before Phases 1–3 are verified.