# Sprint 17 — On-device feature benchmark + C5 real-path verification + AI IO-thread wiring

**Status:** EXECUTED (2026-09-04)
**Scope:** close the on-device evidence gaps that are now possible (the device has ample
free space and USB input injection is permitted), prove every built feature runs correctly
on hardware without degradation (excluding AI), and wire the AI layer so a future
Nano-capable device gets full function without affecting app performance.

## Focus

1. **C5 real-path batch verification (on-device).** The `BatchCompressionService`
   product-loop contract test that previously "compiled but never ran" (device storage
   bound) was executed on the connected handset. It **passed on hardware** — but only
   after surfacing and fixing **two genuine seam defects**:
   - a bare `Service()` had no base context → null-`applicationContext` NPE;
   - `notificationManager` (populated only in `onCreate`) was uninitialized, and the
     end-of-run `stopForeground`/`stopSelf` threw on an unattached service.
   Fixes are in production code (`attachTestContext`, `initRuntimeDependencies`,
   `startedBySystem` guarded teardown), not masked in the test. This is the AGENTS §1
   lesson: a green standalone test is not proof — proof must *run* and it must exercise
   the real boundary.
2. **On-device feature-function benchmark (excluding AI).** A new
   `OnDevicePerformanceBenchmark` drives the real `compressImageFile` /
   `compressImageFileAsWebP` / `compressVideoFile` / `createPdfFromImages` /
   `mergePdfDocuments` / `extractRawTextFromUri` / `OcrHelper.recognizeText` with
   representative inputs, measures wall-clock, and asserts valid output inside a no-hang
   bound. **All pass in seconds — no hang, no degradation.**
3. **AI IO-thread wiring (future-device readiness).** `OnDeviceInferenceRepository`
   now runs `checkStatus()` and `generateContent(...)` on `Dispatchers.IO` so that when a
   Nano-capable device is present the model runs off the main thread and the app's
   performance is not degraded. Non-capable devices still fail closed (this Redmi →
   `UNAVAILABLE`, no cost).

## What shipped

| Change | Files | Proof |
|--------|-------|-------|
| C5 real-path batch seam fixes | `BatchCompressionService.kt`, `BatchPauseContractTest.kt` | on-device `OK (4 tests)` |
| On-device feature benchmark | `OnDevicePerformanceBenchmark.kt` | on-device `OK`, 7 features in ms |
| AI IO-thread wiring | `OnDeviceInferenceRepository.kt` | 12 JVM tests green, on-device `checkStatus()` still UNAVAILABLE |
| Docs/evidence | evidence + this sprint + current-state + CHANGELOG | cited below |

## Honest status (what is NOT claimed)

- **On-device AI inference is still NOT Nano-proven (L5).** The real path returns
  `UNAVAILABLE` on this non-Nano Redmi. A real Gemini Nano output still requires a
  Nano-capable device. The IO-thread wiring is a readiness/correctness improvement, not a
  hardware proof.
- **Keystore off-machine copy** remains a human step (runbook ready).

## Verification

- `./gradlew :app:testDebugUnitTest` → 12 tests, 0 failures.
- `adb shell am instrument -w ...` full suite → **OK (13 tests)** (was 12; +1 benchmark).
- `adb shell am instrument -w -e class ...OnDevicePerformanceBenchmark` → **OK (1 test)**,
  logcat `ShrinkMedia-Perf` + sandbox `perf-benchmark.log` capture per-feature ms.
- `./gradlew :app:lintDebug` → 0 errors.
- Privacy invariant held: no INTERNET permission added; all GenAI work on-device/fail-closed.

## Evidence

- `docs/evidence/2026-09-04_on_device_feature_benchmark.md`
- `docs/evidence/2026-09-03_batch_real_path_contract_test.md` (updated: on-device run + seam fixes)

## Outstanding (explicit, not hidden)

- Real Gemini Nano inference proof (needs Nano-capable device).
- Keystore off-machine copy (human step).
- Release tagging/Play distribution of v0.7.0 (per `docs/release-readiness.md` gate).
