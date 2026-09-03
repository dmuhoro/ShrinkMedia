# WebP Output Option + Privacy Policy — Sprint 15 follow-through (L4)

Date: 2026-09-03 · Owner: Daniel (Lead)

## What and why

From the 2026 marketplace benchmark (`docs/marketplace-2026-benchmark.md`), two concrete,
low-risk, verifiable gaps were actionable in one patch:

1. **WebP output (competitive):** the benchmark found Squoosh/ZeroPNG lead on modern formats
   (WebP/AVIF). ShrinkMedia images were JPEG-only. Added an **offline, on-device WebP** path.
2. **Privacy policy (Play bar):** Google Play mandates a privacy policy + data-safety disclosure
   for every app. Drafted an honest, on-device-first policy.

## WebP implementation (additive, back-compatible, fail-closed)

- `compressImageFileAsWebP(context, uri, quality, mode)` in `MainActivity.kt`: same adaptive
  downsampling/resize math as the JPEG path; encodes via `Bitmap.CompressFormat.WEBP` (lossy) or
  `WEBP_LOSSLESS` (API 30+). Returns `null` on failure (never a bogus file) — mirrors
  `compressImageFile`'s contract.
- **Additive:** `compressImageFile` (JPEG) is unchanged; all existing callers keep working
  (AGENTS §3 back-compat).
- **Fail-closed default:** lossy is the default (lossless opt-in only) so the "compressed is
  smaller" promise is never silently broken by lossless growth.

### Evidence

- `./gradlew :app:compileDebugKotlin` → BUILD SUCCESSFUL.
- New JVM unit test `webpDefaultsToLossy_AndLosslessIsOptIn` (additive default + fail-closed
  lossy-over-lossless) → `testDebugUnitTest` BUILD SUCCESSFUL.
- `lintDebug`, `compileDebugAndroidTestKotlin` → BUILD SUCCESSFUL.
- The connected instrumented suite was not run for this change (device storage still ~full); the
  additive function does not alter the already-verified JPEG path.

## Privacy policy (Play bar)

- `docs/PRIVACY.md`: on-device-only disclosure, no-Internet default, third-party libs table
  (FFmpeg/iText/ML Kit all local), AICore + Connected-mode as designed-not-active, retention,
  deletion, contact. To be linked in-app and in Play Console.

## Honest status

WebP is implemented + unit-tested (format math deterministic). On-device WebP encode of a real
file is **not** run this session (storage-blocked), so it stays an implemented-but-JVM-verified
addition, not yet device-verified for a concrete byte-size win. Privacy policy is a draft doc.
Both are forward steps; neither is over-claimed as device-verified (Constitution Art. VII).