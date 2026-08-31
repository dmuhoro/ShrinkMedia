# 2026-08-31 — Sprint 7: Release Config & Test Hardening Verification

## Command / Step
```bash
export JAVA_HOME=/home/daniel-muhoro/.local/opt/jdk17
export PATH="$JAVA_HOME/bin:$PATH"

# Web gates
npm run lint      # tsc --noEmit
npm test          # vitest run
npm run build     # vite build

# Android: JVM unit tests + instrumented APK compile
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebugAndroidTest

# Android: debug + minified release builds
./gradlew :app:assembleDebug :app:assembleRelease

# Android: signed release bundle (AAB)
./gradlew :app:bundleRelease

# Verify release signature (dev keystore, gitignored)
/home/daniel-muhoro/android-cmdline-tools/build-tools/35.0.0/apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
unzip -l app/build/outputs/bundle/release/app-release.aab | grep -E "META-INF/.*\.(RSA|SF|MF)"

# Package/badging
aapt dump badging app/build/outputs/apk/release/app-release.apk | grep -E "package:|application-label:|launchable"

# No old namespace remnant
rg -n "com.example.mediacompressor" --include='*.kt' --include='*.tsx' --include='*.ts' --include='*.xml' src app/src || echo "none"

# R8 seeds retain FFmpegKit + CompressionQuality
grep -c "com.arthenica.ffmpegkit" app/build/outputs/mapping/release/seeds.txt   # 566
grep "com.shrinkmedia.compressor.CompressionQuality" app/build/outputs/mapping/release/seeds.txt
```

## Observed Result
1. `npm run lint` → exit 0, no diagnostics.
2. `npm test` → `Test Files 3 passed (3)`, `Tests 18 passed (18)`, exit 0.
3. `npm run build` → `dist/` emitted, exit 0.
4. `./gradlew :app:testDebugUnitTest` → BUILD SUCCESSFUL.
5. `./gradlew :app:assembleDebugAndroidTest` → BUILD SUCCESSFUL (instrumented
   APK `app-debug-androidTest.apk` produced; device execution deferred to
   Sprint 8).
6. `./gradlew :app:assembleDebug :app:assembleRelease` → BUILD SUCCESSFUL
   (R8 `minifyReleaseWithR8` ran; `app-release.apk` 9.7 MB vs 26 MB debug).
7. `./gradlew :app:bundleRelease` → BUILD SUCCESSFUL (signReleaseBundle) →
   `app-release.aab` (6,118,545 bytes) with `META-INF/SHRINKME.RSA`.
8. `apksigner verify --print-certs app-release.apk` → `Signer #1` present
   (dev keystore DN: `CN=ShrinkMedia Dev`).
9. aapt badging → `package: name='com.shrinkmedia.compressor' versionCode='2'
   versionName='0.2.1'`; `application-label:'Media Compressor'`.
10. `rg "com.example.mediacompressor"` → no matches in `src`/`app/src`.
11. R8 seeds: 566 `com.arthenica.ffmpegkit` references retained + full
    `CompressionQuality` enum surface kept.

## Notes / Caveats
- Release signing was verified with a **throwaway, gitignored dev keystore**
  created solely to prove the signing/R8 path. The production keystore is a
  human-owned blocker (see `docs/release-readiness.md`).
- Instrumented tests compile here but are NOT yet executed on a device; that
  is the final Sprint 8 release gate.

## Verdict
PASS (code-configuration verification complete; device run blocked → Sprint 8)
