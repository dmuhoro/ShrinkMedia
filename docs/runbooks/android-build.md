# Runbook: Build & Test the Android App

## Goal

Produce a debug APK and run the test suite for the native toolkit.

## Prerequisites

- JDK 17+ (`java -version`)
- Android SDK with `platforms;android-35` and `build-tools` installed
  (`ANDROID_HOME` set, `local.properties` optional but `sdk.dir` must resolve)
- Gradle wrapper (`./gradlew`)
- An emulator or device for instrumentation (later phase) — `adb devices`

## Steps

### 1. Sync / assemble debug

```bash
./gradlew assembleDebug
```

`app/build/outputs/apk/debug/app-debug.apk` is produced on success.

### 2. Run JVM + unit tests (when tests exist)

```bash
./gradlew test
./gradlew testDebugUnitTest
```

### 3. Install on device

```bash
./gradlew installDebug
```

### 4. Smoke check (device)

1. Launch **ShrinkMedia** — three tabs: **Media**, **Documents**, **Elite AI**.
2. Media → **Image** → pick a large photo → assert the result card shows a
   smaller size and the audit count increments.
3. Toggle **Save output to gallery** → re-compress → assert the file appears
   under `Pictures/ShrinkMedia` in the gallery app.
4. Toggle **Pause compression on low battery** → start a batch → simulate low
   battery → assert progress pauses and resumes (no dropped items).
5. Documents → build/merge/split a small PDF (e.g. this doc) → assert pages.

## Known Failure Modes

- `SDK location not found` → set `ANDROID_HOME`/`local.properties`.
- `Execution failed for task :app:checkDebugAapt2` → SDK build-tools missing;
  install via `sdkmanager "build-tools;35.0.0" "platforms;android-35"`.
- FFmpegKit encode fails on hardware → the call returns `null` and the UI
  surfaces it (never a silent drop).