# Evidence — ADR-011 on-device AI surface (L4b)

**Date:** 2026-09-03
**Status:** PASS (host): real ML Kit GenAI library linked + availability gate + fail-closed + UI panel + unit tests. Hardware inference proof is Layer 5 (separate, device-bound).

## Goal (ADR-011)
A private, on-device "personal intelligence" surface: Gemini Nano via Android AICore, offline, no
INTERNET. Fail closed when the model/OS can't run it — never falls to cloud.

## What was built
- `OnDeviceInferenceRepository` (app/src/main/java/com/shrinkmedia/compressor/OnDeviceInferenceRepository.kt):
  `Status` gate (AVAILABLE/DOWNLOADABLE/DOWNLOADING/UNAVAILABLE/API_TOO_OLD), `AiResult`
  (Text/Unavailable/Error), `checkStatus()`, `summarize()`, `summarizeStream()`.
  Live path uses `Generation.getClient()` (a `GenerativeModel`), `.checkStatus()` (returns the
  `Int` feature status), `.generateContent` / `.generateContentStream`.
- UI panel `PersonalIntelligenceCard` in the Elite AI tab: probes availability on entry,
  renders an honest status for every gate, only enables Summarize when AVAILABLE, surfaces
  typed failures/refusals (no silent swallow, no cloud fallback).
- Manifest: `tools:overrideLibrary="com.google.mlkit.genai.prompt,com.google.mlkit.genai.common"`
  (libs declare minSdk 26 > app minSdk 24). Safe because the repo guards **every** GenAI class
  touch behind `Build.VERSION.SDK_INT >= MIN_API_LEVEL (26)` and returns API_TOO_OLD otherwise —
  the library classes are never loaded below 26, so no runtime failure can occur.
- JVM unit tests `OnDeviceInferenceRepositoryUnitTest` (5 tests) exercising the gate + result
  contract via the injected `statusOverride` probe (decision logic is tested on any machine; the
  real inference is ONLY exercised on a Nano device in L5).

## Toolchain requirement (resolved in L4a)
ML Kit GenAI artifacts are compiled with Kotlin 2.2.0; the project was bumped Kotlin 2.0.21 → 2.2.0
(kotlin.android + plugin.compose) as an isolated, regression-tested layer so this code links **for
real** (no false/placeholder gate).

## Verification (real commands + observed result)
| Gate | Command | Result |
|------|---------|--------|
| Compile (real GenAI symbols link) | `./gradlew :app:compileDebugKotlin` | BUILD SUCCESSFUL (FeatureStatus/Generation/GenerativeModel resolve) |
| Unit tests (incl. 5 new gate tests) | `./gradlew :app:testDebugUnitTest` | 12 tests, 0 failures, 0 errors |
| AndroidTest compile | `./gradlew :app:compileDebugAndroidTestKotlin` | BUILD SUCCESSFUL |
| Lint | `./gradlew :app:lintDebug` | BUILD SUCCESSFUL, `0 errors` |

The compiler-verified API reality differs from the earlier doc sketch: `FeatureStatus` lives in
`com.google.mlkit.genai.common` (not `.prompt`), `checkStatus()` is a suspend function returning
the `Int` feature status (not an enum), and the exercised methods live on the `GenerativeModel`
returned by `Generation.getClient()`. The implementation matches the actual bytecode, not the doc.

## Honest caveats
- Proven on this host = compile + gate logic + fail-closed contract. It does **not** prove a real
  Nano inference produced a model output; that requires a Nano-capable device (Layer 5). The unit
  tests deliberately do NOT fabricate a model output.
- The "Summarize on device" button only ever calls the real `Generation` path when `checkStatus()`
  == AVAILABLE; otherwise it refuses with a typed reason. No path uploads user text or adds
  INTERNET.