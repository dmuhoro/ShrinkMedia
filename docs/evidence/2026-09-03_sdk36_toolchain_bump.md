# Evidence — SDK 35 → 36 toolchain bump (L2)

**Date:** 2026-09-03
**Commit:** (Layer 2 — toolchain/SDK bump)
**Status:** PASS (host compile + unit + lint + androidTest-compile verified; on-device boot pending device install)

## What changed

- `build.gradle.kts`: AGP `8.7.2` → `8.9.1` (required for `compileSdk` 36).
- `gradle/wrapper/gradle-wrapper.properties`: Gradle `8.10.2` → `8.11.1` (required by AGP 8.9.1).
- `app/build.gradle.kts`: `compileSdk` 35 → 36, `targetSdk` 35 → 36. `minSdk` unchanged (24).
- Installed Android SDK Platform 36 (revision 2) via `sdkmanager --sdk_root=...` and via AGP's auto-install during the first build.

## Why (gov/ADR context)

Play Store's new-app policy targets API 36 for submissions in this window; `targetSdk` 36 is required to stay conformant on the billing/policy bar. No behavioral change introduced by the bump itself (default `targetSdk` semantics: edge-to-edge flag; validated by lint + unit suite).

## Verification (real commands + observed results)

| Gate | Command | Result |
|------|---------|--------|
| Platform present | `ls platforms/` | `android-35`, `android-36` |
| Compile | `./gradlew :app:compileDebugKotlin` | `BUILD SUCCESSFUL in 2m 8s` |
| Unit tests | `./gradlew :app:testDebugUnitTest` | `BUILD SUCCESSFUL`; `tests="6" failures="0" errors="0"` |
| Lint | `./gradlew :app:lintDebug` | `BUILD SUCCESSFUL`; `0 errors, 27 warnings` |
| AndroidTest compile | `./gradlew :app:compileDebugAndroidTestKotlin` | `BUILD SUCCESSFUL` |

## Honest caveats

- Toolchain downloads (Gradle 8.11.1 dist, AGP 8.9.1) required a robust retry; the `--no-daemon` path stalls on this host (test-R8 fork hang), so builds run **with** the daemon (matches CI).
- **On-device boot verification is still PENDING** — blocked on the handset's "Install via USB" authorization (human action). The bump compiles/links/test-green on host; final proof is installing + launching the SDK-36 build on `49IZ6DJ7SONNQOBE`.
- `targetSdk` 36 not yet exercised across a multi-API device matrix (physically limited to this one device).
