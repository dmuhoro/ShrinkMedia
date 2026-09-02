# Evidence: Sprint 13 — App Rename + Media Delete + First-Run Onboarding (2026-09-02)

> Constitution Article VII: **Grangle every house and day**. A release readiness
> row marked PASS requires a cited command/test/step and its observed result,
> never narrative alone.

## Command Log

### 1. Web-Sim Parity Gates (rename)

```bash
$ cd /home/daniel-muhoro/workspace/projects/ShrinkMedia
$ npm run lint        # PASS — no errors
$ npm test            # PASS — 18 tests
$ npm run build       # PASS — vite build
```

### 2. Android Compilation Gates

```bash
$ export JAVA_HOME=/home/daniel-muhoro/.local/opt/jdk17
$ export PATH=$JAVA_HOME/bin:$PATH
$ ./gradlew :app:compileDebugKotlin
BUILD SUCCESSFUL

$ ./gradlew :app:testDebugUnitTest        # includes onboardingDismissedDefaultsToFalseFailClosed
BUILD SUCCESSFUL

$ ./gradlew :app:assembleDebug
BUILD SUCCESSFUL

$ ./gradlew :app:compileDebugAndroidTestKotlin
BUILD SUCCESSFUL

$ ./gradlew :app:assembleRelease          # R8 minify; repackaged after v0.5.0 bump
> Task :app:minifyReleaseWithR8 UP-TO-DATE
> Task :app:packageRelease
BUILD SUCCESSFUL in 1m 13s

$ ./gradlew :app:lintDebug
BUILD SUCCESSFUL
```

### 3. Release Artifact Verification

```bash
$ aapt2 dump badging app/build/outputs/apk/release/app-release.apk
package: name='com.shrinkmedia.compressor' versionCode='5' versionName='0.5.0' ...
application-label:'ShrinkMedia'

$ apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
Signer #1 certificate SHA-256 digest: 21569322706156fe02111cc5a8ce0f62e33c5229516710f7f078105c6cbb580a

$ ls -la app/build/outputs/apk/release/app-release.apk
-rw-rw-r-- 1 daniel-muhoro daniel-muhoro 58200664 Sep  2 11:23 app-release.apk
```

Signer matches the production keystore (same cert SHA-256 as v0.3.0/v0.4.0).

### 4. Device Verification (API 36, `49IZ6DJ7SONNQOBE`)

Install + launch reporter gate:

```bash
$ export ADB=/home/daniel-muhoro/android-cmdline-tools/platform-tools/adb
$ $ADB install -r --user 0 app/build/outputs/apk/debug/app-debug.apk
Success

$ $ADB shell am force-stop com.shrinkmedia.compressor
$ $ADB shell am start -n com.shrinkmedia.compressor/.MainActivity
Starting: Intent { cmp=com.shrinkmedia.compressor/.MainActivity }

$ $ADB shell pidof com.shrinkmedia.compressor
29044                       # process alive

$ $ADB shell "dumpsys activity activities | grep topResumedActivity"
topResumedActivity=ActivityRecord{... u0 com.shrinkmedia.compressor/.MainActivity ...}

$ $ADB logcat -d | grep -iE "FATAL|AndroidRuntime" | tail
(no matches — no crashes)
```

**On-screen text proof** (uiautomator dump, media tab; grep for non-empty text nodes):

```bash
$ $ADB shell uiautomator dump /sdcard/window_dump5.xml
$ $ADB shell cat /sdcard/window_dump5.xml | grep -oE 'text="[^"]+"'
text="Loaded 1 media files"
text="Media Engine"
text="Get to know ShrinkMedia"                          <- onboarding card
text="• Private by design — images, videos and PDFs are processed on-device. …"
text="• Compress a file, then find the smaller copy under Recent compressions. …"
text="• Free up space — tap Select next to “Your media library”, …"
text="Got it"
text="Compression quality"
text="Low / maximum savings"
text="ShrinkMedia"                                      <- app label (top bar)
text="Your media library"
text="Select"                                           <- new Select button
```

**Onboarding persistence proof** — `dismissOnboarding()` writes the new additive key;
the committed value is observable in the app-sandbox DataStore (debuggable build):

```bash
$ $ADB shell run-as com.shrinkmedia.compressor cat \
    files/datastore/user_settings.preferences_pb | od -A x -t x1z | tail -4
000060 72 65 12 02 08 00 0a 1a 0a 14 6f 6e 62 6f 61 72  e........onboar
000070 64 69 6e 67 5f 64 69 73 6d 69 73 73 65 64 12 02  ding_dismissed..
000080 08 01                                            >..<
```
`08 01` = boolean **true** committed for `onboarding_dismissed` (field 8 = bool value,
varint 1). Removing the DataStore file (`run-as … find files/datastore -type f -delete`)
and relaunching renders the card again — proving the fail-closed default.

### 5. Honest Device Gap (this device blocks simulated input)

```bash
$ $ADB shell input tap 590 1312
java.lang.SecurityException: Injecting input events requires … INJECT_EVENTS …
$ $ADB shell pm clear com.shrinkmedia.compressor
java.lang.SecurityException: … CLEAR_APP_USER_DATA …
```

This device denies `INJECT_EVENTS` and `CLEAR_APP_USER_DATA`, so adb cannot tap through
the Select → Delete → confirm UI path. The system consent dialog
(`MediaStore.createDeleteRequest` PendingIntent, API 30+) requires a **human hand** for
its final tap; that single manual step is pending on-device verification. Everything
short of it is proven above (rendering, persistence, compile, `PendingIntent` construction).
Nothing is claimed beyond that (Honesty Over Optimism, Agile war-room §XII).

### 6. Key Code Changes (Diff Summary)

**Rename to ShrinkMedia**
- `app/src/main/res/values/strings.xml` — `app_name` "Media Compressor" → "ShrinkMedia".
- `app/src/main/java/com/shrinkmedia/compressor/BatchCompressionService.kt` —
  notification title → "ShrinkMedia".
- `index.html`, `src/App.tsx` — title/meta + `h1`/mark label parity.

**Media library multi-select delete** (`MainActivity.kt`)
- `UiState` +: `mediaSelecting: Boolean`, `mediaSelection: Set<Long>`.
- `MediaTab` header right action **Select** → **Done**; selection-mode action bar
  (count / Cancel / Delete-error-colored); confirm `AlertDialog`; launcher
  `rememberLauncherForActivityResult(StartIntentSenderForResult)`.
- `buildDeleteRequest(files)`: `MediaStore.createDeleteRequest(resolver, uris)` →
  **`PendingIntent`** (verified by `javap` on `android-35/android.jar`), launched via
  `IntentSenderRequest.Builder(pending.intentSender).build()`; `null` below API 30.
- `applyMediaDeletion(files, RESULT_OK)`: removes deleted ids from the library; any other
  result → explicit "Delete cancelled — no files were changed" toast (no silent drop).
- `deleteLegacy(files)`: API <30 direct `contentResolver.delete`; per-item result counted,
  partial/full failure surfaced in the toast.

**First-run onboarding** (`MainActivity.kt`, `SettingsDataStore.kt`)
- Card "Get to know ShrinkMedia" with 3 bullet pointers + **Got it** → `dismissOnboarding()`.
- `SettingsDataStore`: additive key `ONBOARDING_DISMISSED`, default `false` (fail-closed),
  `updateOnboardingDismissed(dismissed)`.
- Unit test `onboardingDismissedDefaultsToFalseFailClosed` (green).

**Version bump** (`app/build.gradle.kts`)
- `versionCode 5`, `versionName "0.5.0"` (was 4 / 0.4.0).

### 7. Cross-Reference

- `docs/sprints/sprint-13-app-rename-media-delete-onboarding.md`
- `docs/current-state.md` (C19)
- `docs/architecture.md` (Media Eng. + Settings rows)
- `docs/decisions.md` (D007)
- `CHANGELOG.md` v0.5.0
- `docs/ideas.md` (I002 liveliness radar)
- `docs/sprint-cross-reference.md` (Sprint 13 row)