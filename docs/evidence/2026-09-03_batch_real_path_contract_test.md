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

## On-device run: BLOCKED at first, then EXECUTED + PASS (2026-09-03/04, device returned with free space)

The instrumented suite was re-attempted on device `49IZ6DJ7SONNQOBE` (API 36) once the
device had enough free space. The first run **failed on hardware**, which surfaced two
genuine defects in the test seam (exactly the kind of gap AGENTS §1 warns about — proof must
exercise the real path *and actually run*):

1. **NULL-CONTEXT NPE.** A bare `Service()` constructor has no attached base context, so the
   real loop's `applicationContext` (used by `acquireAuditLog`/`BatchFailureAudit.logFile`) was
   `null` → `NullPointerException` at `BatchCompressionService.acquireAuditLog`.
2. **UNINITIALIZED `notificationManager` + invalid teardown.** The loop notifies progress via
   `notificationManager`, a `lateinit` populated only in `Service.onCreate` — the seam
   bypasses `onCreate` → `UninitializedPropertyAccessException`. After that was fixed, the
   loop's end-of-run `stopForeground(STOP_FOREGROUND_REMOVE)` threw
   `NullPointerException: class name is null` because the Service was never attached to the
   framework.

### Root-cause fix (production code, real boundary)

- Added package-visible `BatchCompressionService.attachTestContext(Context)` which attaches a
  real app base context **and** runs the same `initRuntimeDependencies()` (notification
  manager + channel) that `onCreate` performs — no duplication, production `onCreate` is
  unchanged.
- Extracted `initRuntimeDependencies()` from `onCreate` so both the real lifecycle and the
  test bootstrap share one source of truth.
- Guarded the end-of-run `stopForeground`/`stopSelf` behind `startedBySystem` (true only when
  Android actually started the service via `onStartCommand`). This is **not** a weakening: in
  production the service is always started, so teardown runs exactly as before; the guard only
  prevents an unattached-service crash that is meaningless for the contract under test.

### Final on-device result (real commands)

| Step | Command | Result |
|------|---------|--------|
| Install app+test | `adb install -r -t --user 0 app-debug.apk app-debug-androidTest.apk` | Success |
| Drive the REAL batch loop on hardware | `adb shell am instrument -w -e class com.shrinkmedia.compressor.BatchPauseContractTest ...` | **OK (4 tests)** |
| Full instrumented suite | `adb shell am instrument -w com.shrinkmedia.compressor.test/...` | **OK (12 tests)** |
| JVM unit suite | `./gradlew :app:testDebugUnitTest` | 12 tests, 0 failures |
| Lint | `./gradlew :app:lintDebug` | 0 errors |

The **real** `BatchCompressionService.executeBatchProcessing` loop now holds a queued item at
the pause gate (`isPaused.first { !paused }`) and completes it exactly once on hardware —
never dropped — verified through the real DataStore counter. This closes C5's "never executed
on hardware" gap: **C5 is now marked ✅ on-device PASS** in `docs/current-state.md`.

## On-device run (historical, superseded)

The original attempt on 2026-09-03 was physically blocked:
- `INSTALL_FAILED_INSUFFICIENT_STORAGE: Failed to override installation location`
- `/data/user/0` was ~100% full (≈0.7 GB free of 106 GB); the full-FFmpegKit debug APK
  (~134 MB) + the androidTest APK could not both fit.
- Earlier errors (`INSTALL_FAILED_USER_RESTRICTED`, `INSTALL_FAILED_UPDATE_INCOMPATIBLE`)
  were secondary symptoms of the same storage upper bound.

That storage bound has since relaxed (device returned with free space), enabling the on-device
run above.

## Sprint cross-reference

- See `docs/sprints/sprint-15-branding-video-fix-release.md` (C5 written+compiled, not run)
  and `docs/sprints/sprint-17-v0.7.1-on-device-batch-verification.md` (today: ran + fixed).
- C5 is now **✅ on-device PASS** in `docs/current-state.md`.