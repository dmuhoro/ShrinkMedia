# 2026-08-30 — Android Config Self-Check (Wrapper + DSL + Debug Build)

## Command / Step
```
# install SDK components (one-time)
sdkmanager --sdk_root=$ANDROID_HOME "platforms;android-35" "build-tools;35.0.0" "platform-tools"

# build with a full JDK 21 toolchain
export JAVA_HOME=<JDK-21-with-javac> ANDROID_HOME=$ANDROID_HOME
./gradlew assembleDebug --no-daemon
```

## Observed Result
- Wrapper present and pinned: `gradlew`, `gradlew.bat`,
  `gradle/wrapper/gradle-wrapper.jar`, `gradle-wrapper.properties`
  → `gradle-8.10.2-bin.zip` (AGP 8.7.2-compatible, per
  `app/build.gradle.kts` AGP). `local.properties` (absolute SDK path) is
  gitignored.
- **Found and fixed a non-resolving dependency.** `app/build.gradle.kts`
  declared `io.github.root0as:ffmpeg-kit-lite:6.0-2`, which returns 404 on
  Google Maven, Maven Central, GitHub releases, and JitPack (no such GitHub
  user or fork) — the committed app could never build. Replaced with
  `io.github.nikita36078:ffmpeg-kit:6.0.LTS`, a Maven-Central-published
  maintained fork shipping the same `com.arthenica.ffmpegkit` API and
  `libx264` on all four ABIs (arm64-v8a, armeabi-v7a, x86, x86_64).
- `./gradlew assembleDebug` → `BUILD SUCCESSFUL in 1m 1s`,
  `app/build/outputs/apk/debug/app-debug.apk` (25.96 MB).
- APK inspection (`aapt2 dump badging`): package `com.example.mediacompressor`,
  `compileSdkVersion='35'`, native code for all four ABIs including
  `lib/arm64-v8a/libffmpegkit.so` and `libavcodec.so`.
- Privacy invariant holds: manifest declares `FOREGROUND_SERVICE`,
  `FOREGROUND_SERVICE_DATA_SYNC`, `POST_NOTIFICATIONS` only — no
  `android.permission.INTERNET`.

## Verdict
PASS — debug build verified end-to-end; the previously non-resolving
FFmpegKit coordinate is fixed and the no-INTERNET invariant is unchanged.