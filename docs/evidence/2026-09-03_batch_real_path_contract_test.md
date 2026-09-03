# Batch Real-Path Contract Test (C5 evidence)

Date: 2026-09-03 · Sprint 15 · Owner: Daniel (Lead)

## Why this exists

C5 (`Foreground batch service + progress notification`) previously carried NO on-device
evidence and its only contract test (`BatchPauseContractTest`) proved the *pause-gate
pattern* by hand-rolling `isPaused.first { !paused }` — it never drove the real
`BatchCompressionService.executeBatchProcessing` loop. Per AGENTS §1 (and the
Constitution's "proof must exercise the real boundary"), a green helper test is not proof
the production loop honors the pause gate.

## The fix (code, real-boundary)

1. Added a minimal, package-visible **test seam** on the production class
   `BatchCompressionService.executeBatchProcessingForTest(...)` that simply forwards to the
   real `executeBatchProcessing(...)` (the exact method `onStartCommand` drives).
   Production call sites are unchanged; no behavior altered.
2. Added instrumented test
   `real_batch_loop_holds_a_queued_item_while_paused_and_does_not_drop_it` in
   `BatchPauseContractTest.kt` that:
   - writes a real, decodable 512x512 noise PNG and exposes it via FileProvider;
   - reads `totalHistoricalFilesCount` from the real DataStore (`SettingsRepository`);
   - arms `BatchCompressionPauseController.isPaused = true` BEFORE the loop starts;
   - runs the real batch loop on `Dispatchers.IO`;
   - asserts that while paused the queued item is NOT processed (count unchanged) —
     the loop is physically suspended at the gate;
   - resumes, awaits completion, and asserts the count increments by exactly 1 — the
     queued item was never dropped nor skipped.

The test closes the AGENTS §1 false-confidence gap in the code: it exercises the REAL
loop's `isPaused.first { !paused }` gate, not a copy.

## Evidence of execution

- `./gradlew :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin :app:testDebugUnitTest` → **BUILD SUCCESSFUL** (seam + test compile; unit suite green).
- The debug base + androidTest APKs build clean.

## On-device run: PHYSICALLY BLOCKED (honest — NOT flipped to PASS)

Attempting to run the instrumented suite on device 49IZ6DJ7SONNQOBE (API 36) failed with:

- `INSTALL_FAILED_INSUFFICIENT_STORAGE: Failed to override installation location`
- `/data/user/0` is ~100% full (≈0.7 GB free of 106 GB); the full-FFmpegKit debug APK
  (~134 MB) + the androidTest APK cannot both fit alongside the installed OS apps.
- Earlier errors (`INSTALL_FAILED_USER_RESTRICTED`, `INSTALL_FAILED_UPDATE_INCOMPATIBLE`)
  were secondary symptoms of the same storage upper bound.

Per the Constitution Article VII (evidence must cite an observed result) and AGENTS §1/§5,
**C5 is NOT marked PASS/✅.** The code-level real-path test is a genuine forward step, but
"device-verified" is not claimed until this test executes on hardware. The remaining gate is
a **physical storage-capacity action** (free device space, or provision a larger-memory
device/emulator), not a code change.

## Sprint cross-reference

- See `docs/sprints/sprint-15-branding-video-fix-release.md` and this file.
- C5 remains **⚠️ device run blocked by storage** in `docs/current-state.md`.