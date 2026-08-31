# Sprint 7 — Test Hardening & Release Configuration

**Status:** COMPLETE
**Version Target:** v0.2.1
**Theme/Result:** Make the app testable, honester, and release-configurable:
honest web + JVM + instrumented-test coverage, a production namespace with R8
minification and signing, a proper launcher icon, and a hardened, Node-24 CI
with a signed release-AAB job. Hardware verification is deliberately deferred
to the final **Sprint 8** gate.

> Executed history lives here with sprints 1–6; the next plan is
> `docs/sprints/sprint-8-device-verification-final-gate.md`.

## Summary

| Capability | Status | Evidence |
|------------|--------|----------|
| Honest web batch/savings model + tests (no fake numbers) | ✅ | `src/lib/batch.ts`, `src/lib/batch.test.ts`; `npm test` 18 pass |
| Real-path Android tests written and compiling (JVM + instrumented) | ✅ | `app/src/test/.../CompressionQualityUnitTest.kt`, `app/src/androidTest/.../` — compile green, device run = Sprint 8 |
| `BatchCompressionService.startBatch` wired into Media tab + savings surfaced | ✅ | `ToolkitViewModel.startBatch`, `src/App.tsx` batch handler |
| Production namespace `com.shrinkmedia.compressor` (namespace + applicationId) | ✅ | `app/build.gradle.kts`, APK badging |
| R8-minified signed release build | ✅ | `app/build.gradle.kts` (`isMinifyEnabled=true`, `signingConfigs.create("release")`), `app/proguard-rules.pro`; `apksigner verify` on `app-release.apk` |
| Signed release AAB build | ✅ | `:app:bundleRelease` + `:app:signReleaseBundle` → `app-release.aab` (SHRINKME.RSA) |
| Launcher icon + label (adaptive + PNG fallbacks) | ✅ | `app/src/main/res/mipmap-*`/`drawable`/`values/colors.xml`; manifest `@mipmap/ic_launcher`/`@string/app_name` |
| v0.2.1 | ✅ | `versionCode 2`, `versionName 0.2.1` |
| CI upgraded off deprecated Node-20 actions + signed release job | ✅ | `.github/workflows/ci.yml` (checkout v5, setup-node v5, setup-java v5, cache v5, upload-artifact v7) |

## Part 1 — Test hardening (ROADMAP Phases 2 & 3)

**Problem:** The web simulator used fake inline `dummyOriginal`/`dummyCompressed`
numbers for batch savings, so its "savings" narrative could claim numbers the
native path would never produce. Android had no executable tests proving the
real compression path.

**Fix:** Introduced a pure `src/lib/batch.ts` model (`buildBatchResults`,
`accumulateSavings`) mirroring the native `recordCompressionSavings`
`maxOf(0L, savedBytes)` semantics, with 7 unit tests; `src/App.tsx` now uses it
(honest numbers). Added Android JVM unit tests (`CompressionQualityUnitTest`,
4 tests — asserting real `formatFileSize` output, not a weakened expectation)
and real-path instrumented tests (`CompressionPipelineInstrumentedTest`,
`BatchPauseContractTest` proving the fail-closed pause gate never drops an item,
`BitmapCompressUtil`). The instrumented suite compiles but awaits a device in
Sprint 8.

**Files:** `src/lib/batch.ts`, `src/lib/batch.test.ts`, `src/App.tsx`,
`app/src/test/java/com/shrinkmedia/compressor/CompressionQualityUnitTest.kt`,
`app/src/androidTest/java/com/shrinkmedia/compressor/{CompressionPipelineInstrumentedTest,BatchPauseContractTest,BitmapCompressUtil}.kt`,
`app/build.gradle.kts` (test deps).

## Part 2 — Production release configuration (ROADMAP Phase 4 prep)

**Problem:** The app shipped as `com.example.mediacompressor` with a placeholder
`@android:drawable/sym_def_app_icon`, no release minification/signing, and no
way to produce a signed release artifact.

**Fix:** Renamed namespace + applicationId to `com.shrinkmedia.compressor`
across all Kotlin sources, `App.tsx` code samples, and internal string
constants. Rewrote `app/build.gradle.kts` to enable R8 minification and wire an
optional gitignored `keystore.properties` SigningConfig (`"release"`); without
the keystore the release build fails closed (no unsigned artifact). Added
`app/proguard-rules.pro` keeping the FFmpegKit reflection surface and
`CompressionQuality`. Added an adaptive launcher icon + PNG fallbacks and moved
the label to `@string/app_name`. Bumped `versionCode 1 → 2`,
`versionName 1.0 → 0.2.1`. Verified locally with a throwaway dev keystore
(`apksigner verify` PASS on `app-release.apk`, `SHRINKME.RSA` on the AAB).

**Files:** `app/build.gradle.kts`, `app/proguard-rules.pro`,
`app/src/main/AndroidManifest.xml`, `.gitignore`,
`app/src/{main,androidTest,test}/java/com/shrinkmedia/compressor/*.kt`,
`app/src/main/res/{mipmap-*,drawable,values}/*`, `src/App.tsx`.

## Part 3 — CI hardening (governance)

**Problem:** CI ran on first-party actions pinned to the deprecated Node-20
runtime (GitHub forces Node 24 on 2026-06-02) and had no signed-release path.

**Fix:** Upgraded `checkout`, `setup-node`, `setup-java`, `cache` to their
Node-24 majors and `upload-artifact` to v7 (first Node-24 artifact release).
Android job now runs JVM unit tests + compiles the instrumented test APK before
`assembleDebug`. Added a `release` job that builds a **signed** `bundleRelease`
AAB, gated fail-closed on the presence of signing secrets and branch == main;
it recreates the gitignored `keystore.properties`/`keystore.jks` from base64
secrets exclusively in CI.

**Files:** `.github/workflows/ci.yml`.

## Validation & Verification Checklist

- [x] `npm test` → 3 files / 18 tests pass.
- [x] `npm run lint` (tsc) clean; `npm run build` clean.
- [x] `./gradlew :app:testDebugUnitTest` BUILD SUCCESSFUL.
- [x] `./gradlew :app:assembleDebugAndroidTest` BUILD SUCCESSFUL (compiles;
      device run = Sprint 8).
- [x] `./gradlew :app:assembleDebug` + `:app:assembleRelease` BUILD SUCCESSFUL.
- [x] `:app:bundleRelease` + `:app:signReleaseBundle` → signed `app-release.aab`.
- [x] `apksigner verify --print-certs app-release.apk` → Signer #1 present.
- [x] R8 seeds retain 566 FFmpegKit refs + full `CompressionQuality` surface.
- [x] No `com.example.mediacompressor` remnant in tracked sources.
- [x] Release-keystore artifacts (`keystore.properties`, `*.jks`) gitignored,
      none tracked; all commits SSH-signed.
- [ ] Instrumentation tests executed on hardware (deferred → Sprint 8).

## Cross-Reference

ROADMAP Phases 2/3 (test hardening, web parity) and Phase 4 (release);
`docs/release-roadmap.md`; `docs/current-state.md`; `docs/release-readiness.md`;
Constitution Articles I, VI.7, VII.
