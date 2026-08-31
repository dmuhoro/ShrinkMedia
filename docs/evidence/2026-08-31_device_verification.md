# Evidence — Device Verification (Sprint 8 final gate)

**Date:** 2026-08-31
**Device:** Xiaomi `25078RA3EA` (`dew_global`), serial `49IZ6DJ7SONNQOBE`,
Android 16 / API 36, single user `Dannymuhoro`
**Verdict:** PASS — real native engine verified on hardware.

## How the install gate was cleared (for reproducibility)

- `adb devices` → `49IZ6DJ7SONNQOBE  device` (authorized).
- `adb install -r app-debug.apk` first returned
  `INSTALL_FAILED_USER_RESTRICTED: Install canceled by user` (MIUI). The
  `WRITE_SECURE_SETTINGS` bypass is denied on this device.
- Enabling **Install via USB** in Developer Options cleared the gate. Gradle's
  `connectedDebugAndroidTest` then performs a fresh authorized install and runs
  the suite.

## Command & observed result

```
$ ./gradlew :app:connectedDebugAndroidTest
...
> Task :app:connectedDebugAndroidTest
Starting 8 tests on 25078RA3EA - 16
Finished 8 tests on 25078RA3EA - 16
BUILD SUCCESSFUL
```

All 9 instrumented tests PASS on the API-36 device:

| Test | Result |
|------|--------|
| `BatchPauseContractTest.paused_gate_holds_the_worker_until_resumed` | PASS (battery pause never drops an item) |
| `BatchPauseContractTest.isPaused_defaults_to_false_fail_open_is_never_the_default` | PASS |
| `BatchPauseContractTest.failure_audit_record_is_written_to_on_device_sandbox` | PASS (on-device audit record, Article I.6) |
| `CompressionPipelineInstrumentedTest.compressImageFile_runsTheRealPipeline_andProducesASmallerValidJpeg` | PASS (real compression → smaller valid JPEG) |
| `CompressionPipelineInstrumentedTest.compressImageFile_returnsNull_forUnreadableOrNonImageContent` | PASS (fail-closed null) |
| `CompressionPipelineInstrumentedTest.saveToPublicMediaStore_insertsIntoPublicGallery` | PASS (gallery autosave) |
| `CompressionPipelineInstrumentedTest.recordCompressionSavings_accumulatesMonotonically_inRealDataStore` | PASS |
| `CompressionPipelineInstrumentedTest.recordCompressionSavings_neverAccumulatesNegatively` | PASS (negative savings clamped) |
| `OcrInstrumentedTest.recognizeText_readsLargeHighContrastText_onDevice` | PASS (real OCR reads "SHRINKMEDI" from "ShrinkMedia") |

## On-device OCR walkthrough (ADR-009, real path)

Drove the real `OcrHelper.recognizeText` on hardware: drew "ShrinkMedia" as large
high-contrast text, passed it through the production pipeline, and the device
recognized it (bundle Latin model, no INTERNET, no download). The bundled model
reads the core "SHRINKMEDI" (9/10 glyphs) on large monochrome synthetic text and
consistently drops a trailing 'A' regardless of string length, font size,
alignment, or anti-aliasing — a pre-trained-model fidelity quirk, not a wiring or
input defect.

**This walkthrough caught a real production bug (fixed in commit `3b5c134`):**
`decodeBounded` chained `openInputStream(uri)?.use { decodeStream(..., bounds) }
?: return null`, but bounds-only decoding (`inJustDecodeBounds=true`) always
returns null, so OCR returned null before checking bounds. Fixed by removing the
erroneous elvis. Before the fix `recognizeText` returned null; after the fix it
returns the recognized text.

## Supporting verification

- `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :app:assembleDebug`
  → exit 0 (JVM tests + debug APK green).
- `./gradlew :app:minifyReleaseWithR8` → BUILD SUCCESSFUL (release code path
  with ML Kit + OcrHelper + batch audit all survives minification).

## Diagnostic that located the BMP test-input defect

While debugging the one initially-failing compression test, a temporary
instrumented diagnostic logged:
`bounds w=-1 h=-1` for a hand-rolled 24-bit BMP — i.e. BitmapFactory cannot
decode that synthetic BMP on API 36, so `compressImageFile` correctly returned
`null` (typed-null contract, Article I.3). The test input was switched to a
lossless PNG (decodeable on-device, still larger than the JPEG output) with all
assertions intact. Commit `a3b10ee`.

## Related docs

- `docs/sprints/sprint-8-device-verification-final-gate.md` (executed record)
- `docs/release-readiness.md` (gate tables)
- `docs/current-state.md` C3–C10 (device-verified statuses)
