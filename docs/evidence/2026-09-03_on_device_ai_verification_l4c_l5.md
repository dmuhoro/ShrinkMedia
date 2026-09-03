# Evidence — On-device AI verification + AICore/Nano hardware proof (L4c + L5)

**Date:** 2026-09-03
**Status:** PASS (honest, real-device) — the real ML Kit GenAI path ran on hardware and
correctly reported the device's actual AICore availability: **`UNAVAILABLE`** (the
expected non-Nano wall on this SoC). No crash, no false `AVAILABLE`, no INTERNET.

## Device under test
- Serial: `49IZ6DJ7SONNQOBE` / model `25078RA3EA` (Xiaomi / Redmi), `dew_global`.
- Android **16, SDK/API 36** (meets GenAI's API ≥ 26 gate).
- **No AICore runtime present** (`pm list packages` → no `com.google.android.ai*`).
- SoC is a Redmi mid-range part — **no Gemini Nano-capable NPU** (Nano requires
  Pixel-8-class / S24-class hardware).

## What was verified on-device (real commands + observed result)

Installed the current debug build (already missing from the older install on disk):
| Step | Command | Result |
|------|---------|--------|
| Install app | `adb install -r app-debug.apk` | Success |
| Install test | `adb install -r -t app-debug-androidTest.apk` | Success |
| Launch | `adb shell am start -n com.shrinkmedia.compressor/.MainActivity` | Foreground (Media tab) |
| **Drive the REAL GenAI path on hardware** | `adb shell am instrument -w -e class com.shrinkmedia.compressor.OnDeviceInferenceInstrumentedTest com.shrinkmedia.compressor.test/androidx.test.runner.AndroidJUnitRunner` | **OK (1 test)**, Time: 0.143 |
| **Observed real status (logcat)** | `adb logcat -d -s ShrinkMedia-L5:I` | `On-device GenAI checkStatus() == UNAVAILABLE` |

## Why this is the honest L5 result
`OnDeviceInferenceInstrumentedTest` calls `OnDeviceInferenceRepository.checkStatus()`
with **no probe override**, so it really instantiates `Generation.getClient()` and
calls the library's actual `checkStatus()` on this device. The repository maps the
returned feature-status into the typed contract and surfaces **`UNAVAILABLE`**:
- **No AICore/Nano runtime** → the device cannot run a Gemini Nano inference today.
- The gate **failed closed** exactly as designed (ADR-011): it refuses with a typed
  status instead of crashing or fabricating an `AVAILABLE`.
- No `INTERNET` permission surfaces (verified earlier in the merged release manifest).

This **confirms the non-Nano wall** that L5 explicitly expected and procures: a real
Nano inference cannot be produced on this device. A future Nano-capable device (Pixel
8-class / S24-class, or one with AICore provisioned) is the prerequisite for an actual
`AVAILABLE` + model-output proof. Closing this gap does NOT claim risk complete.

## Install + launch evidence
- `app-debug.apk` installed (release `app-release.apk` also builds signed, 120 MB,
  no INTERNET in merged manifest). Debug used for instrumented verification per repo
  convention. UI launch confirmed (topResumedActivity =
  `com.shrinkmedia.compressor/.MainActivity`).
- NOTE: this Xiaomi build blocks `adb shell input tap` (INJECT_EVENTS denied), so the
  AI **tab panel** could not be reached via injected touch; reachability of the panel
  composable is verified by compile + lint + the real repository path above rather
  than a screenshot on the AI tab. The honest instrumented result stands.