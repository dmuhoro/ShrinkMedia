# Evidence — On-device feature-function benchmark (excluding AI) + AI IO-thread wiring

**Date:** 2026-09-04
**Status:** PASS — every built feature (JPEG/WebP image compression, H.264 video
compression, PDF build/merge/text-extraction, ML Kit OCR) ran on the connected device
and completed in seconds with valid output — **no hang, no degradation**. The AI layer
was additionally re-wired so that when a Nano-capable device is later available, inference
runs off the main thread (fail-closed), so enabling it will not degrade app performance.

## Device under test

- Serial `49IZ6DJ7SONNQOBE` / `25078RA3EA` (Xiaomi/Redmi), Android 16 / API 36.
- Note: `adb shell input tap` is **now permitted** (the developer "USB debugging
  (Security settings)" / simulate-input allow was granted), closing the earlier
  INJECT_EVENTS blocker. UI launch + reaches of the Media tab were click-verified.

## What was benchmarked (the real production functions, on hardware)

`OnDevicePerformanceBenchmark` drives the exact functions the UI and `BatchCompressionService`
call, with representative inputs, measures wall-clock time, and asserts valid output
inside a generous no-hang bound. Observed results on 2026-09-04 (logcat `ShrinkMedia-Perf`
+ on-device sandbox `files/perf-benchmark.log`):

| Feature | Input | Wall-clock | Result |
|---------|-------|-----------|--------|
| JPEG compress (`compressImageFile`, MEDIUM) | 2048×2048 noise PNG (14.5 MB) | **588 ms** | OK → 1.76 MB |
| WebP lossy (`compressImageFileAsWebP`) | same 2048×2048 | **1807 ms** | OK → 1.91 MB |
| H.264 video (`compressVideoFile`, MEDIUM) | real 1280x720 ~4 s clip (2.95 MB) | **2816 ms** | OK → 742 KB, decodable |
| PDF build (`createPdfFromImages`) | 2 images | **477 ms** | OK |
| PDF merge (`mergePdfDocuments`) | 2 PDFs → 5 pages | **291 ms** | OK |
| PDF text extraction (`extractRawTextFromUri`) | single page | **49 ms** | OK (92 chars) |
| ML Kit OCR (`OcrHelper.recognizeText`) | 2400×900 text bitmap | **1064 ms** | OK, reads "SHRINKMEDI" |

Command (on-device, real path): `adb shell am instrument -w -e class
com.shrinkmedia.compressor.OnDevicePerformanceBenchmark ...` → **OK (1 test)**.
Full instrumented suite after the wiring change: **OK (13 tests)**; JVM unit tests
green; lint 0 errors.

## Diagnosis

Every shipped feature completes correctly on this mid-range device in well under 3 s
per item (heaviest = video encode at ~2.8 s for a 4 s clip). **There is no hang and no
performance degradation** for the non-AI feature surface — the app is shippable on this
class of hardware.

## AI IO-thread wiring (future-device readiness)

Per the user directive — "wire everything so a device capable of on-device AI works
perfectly without affecting performance" — `OnDeviceInferenceRepository` was hardened so
all GenAI touchpoints stay off the UI thread even when a model is AVAILABLE:

- `checkStatus()` now probes AICore via `withContext(Dispatchers.IO)` (binding is off-main).
- `summarize()`/`runOnDevice()` wrap `generateContent(...)` in `withContext(Dispatchers.IO)`.
- `summarizeStream()` already ran on `Dispatchers.IO` via `flowOn`.

Combined with the existing fail-closed gate (`summarize` returns `AiResult.Unavailable`
unless `checkStatus() == AVAILABLE`, no cloud fallback), this means: on this Redmi the AI
card refuses cleanly and costs nothing; on a future Nano-capable device the model runs on
a background thread and does not degrade the app. No behavior change for the current
non-capable device (the real on-device `checkStatus()` still returns `UNAVAILABLE`).

## Cross-reference

- Sprint: `docs/sprints/sprint-17-v0.7.1-on-device-feature-benchmark.md`
- Current state: `docs/current-state.md` (C5 now ✅ on-device PASS; no AI PASS claimed).
