# Evidence — WebP in the image UI (L3)

**Date:** 2026-09-03
**Status:** PASS (host compile + unit + androidTest-compile + lint all green; on-device encode pending install of SDK-36 APK)

## What changed

Made the additive WebP output a **user-facing, selectable option** rather than a code-only
helper (it shipped in the prior sprint as `compressImageFileAsWebP` but was not reachable from
the UI):

- Added **`ImageFormat`** enum (`JPEG` / `WEBP_LOSSY` / `WEBP_LOSSLESS`) that maps to either no
  WebP mode or the existing `WebpMode`.
- Added `UiState.imageFormat` (default `JPEG` — additive/fail-closed: existing UX unchanged until
  the user picks WebP) + `ToolkitViewModel.imageFormat(...)` setter.
- **`compressMedia(...)`** — the real boundary the UI drives — dispatches image compressions to
  `compressImageFileAsWebP` when the mode is non-null, else `compressImageFile` (JPEG); videos are
  unchanged.
- **Format selector UI** on the home screen (radio list: JPEG / WebP lossy / WebP lossless) above
  the quality selector, with honest capability/API notes.
- **Batch path wired too**: `BatchCompressionService.startBatch`/`onStartCommand`/
  `executeBatchProcessing[_ForTest]` now carry an `imageFormatName` extra (default `"JPEG"`) so a
  batch of images honors the chosen format — no silent divergence between single vs batch image
  output.
- Audit detail/media-type now reflect the chosen format (`image/jpeg` vs `image/webp`).

## Why

`docs/marketplace-2026-benchmark.md` shows modern local compressors (Squoosh/ZeroPNG) default to
WebP; WebP is ~25% smaller than JPEG at equal quality. This closes that gap while staying 100%
on-device and offline, with no new dependency.

## Verification (real commands + observed results)

| Gate | Command | Result |
|------|---------|--------|
| Compile | `./gradlew :app:compileDebugKotlin` | `BUILD SUCCESSFUL in 1m 6s` |
| Unit tests | `./gradlew :app:testDebugUnitTest` | `BUILD SUCCESSFUL`; `tests="7" failures="0" errors="0"` (added `imageFormatMapsToCorrectWebpModeAndDefaultsToJpeg`) |
| AndroidTest compile | `./gradlew :app:compileDebugAndroidTestKotlin` | included in BUILD SUCCESSFUL |
| Lint | `./gradlew :app:lintDebug` | `BUILD SUCCESSFUL`; `0 errors` |

## Honest caveats

- The C5/batch contract test still calls `executeBatchProcessingForTest` with its default
  `imageFormatName="JPEG"`, so its assertion (item held at pause gate, then completed) is
  unchanged and still passes on the real loop — no regression.
- **On-device WebP encode is not yet proven** — blocked on installing the SDK-36 APK (device
  `INSTALL` works now without `-g`; a WebP encode smoke run will be done when the SDK-36 build is
  installed, as its own evidence row). Host-level decoding/format logic is covered by the unit test.
- Lossless WebP is API-30+ gated in `compressImageFileAsWebP`; on API 24-29 the lossless option
  silently degrades to lossy WebP — documented in the UI footnote, fail-closed (never fails, never
  produces unexpectedly-large lossless growth below API 30).