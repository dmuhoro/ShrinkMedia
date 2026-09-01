# Evidence: Sprint 11 — PDF Compile Fixes + UI Polish (2026-09-01)

> Constitution Article VII: **Grangle every house and day**. A release readiness
> row marked PASS requires a cited command/test/step and its observed result,
> never narrative alone.

## Command Log

### 1. Android Compilation Gates

```bash
$ cd /home/daniel-muhoro/workspace/projects/ShrinkMedia
$ export JAVA_HOME=/home/daniel-muhoro/.local/opt/jdk17
$ export PATH=$JAVA_HOME/bin:$PATH
$ ./gradlew :app:compileDebugKotlin
> Task :app:compileDebugKotlin UP-TO-DATE
BUILD SUCCESSFUL in 4s
```

```bash
$ ./gradlew :app:testDebugUnitTest
> Task :app:testDebugUnitTest UP-TO-DATE
BUILD SUCCESSFUL in 6s
```

```bash
$ ./gradlew :app:assembleDebug
> Task :app:assembleDebug UP-TO-DATE
BUILD SUCCESSFUL in 9s
```

```bash
$ ./gradlew :app:compileDebugAndroidTestKotlin
> Task :app:compileDebugAndroidTestKotlin UP-TO-DATE
BUILD SUCCESSFUL in 5s
```

```bash
$ ./gradlew :app:assembleRelease
> Task :app:minifyReleaseWithR8 UP-TO-DATE
> Task :app:assembleRelease
BUILD SUCCESSFUL in 9s
```

```bash
$ ./gradlew :app:lintDebug
> Task :app:lintReportDebug
Wrote HTML report to file:///home/daniel-muhoro/workspace/projects/ShrinkMedia/app/build/reports/lint-results-debug.html
> Task :app:lintDebug
BUILD SUCCESSFUL in 1m 44s
```

### 2. Device Verification

```bash
$ adb devices
List of devices attached
49IZ6DJ7SONNQOBE	device
```

```bash
$ adb uninstall com.shrinkmedia.compressor
Success
```

```bash
$ adb install -r app/build/outputs/apk/debug/app-debug.apk
Performing Streamed Install
Success
```

```bash
$ adb shell am start -n com.shrinkmedia.compressor/.MainActivity
Starting: Intent { cmp=com.shrinkmedia.compressor/.MainActivity }
```

```bash
$ adb logcat -d | grep -iE "(crash|exception|error|fatal)" | grep -i shrinkmedia
(no matches — no crashes)
```

### 3. Proguard Rule Verification

```bash
$ cat app/proguard-rules.pro
# FFmpegKit
-keep class com.arthenica.ffmpegkit.** { *; }
-keep class com.arthenica.mobileffmpeg.** { *; }

# iText 7 (on-device PDF)
-keep class com.itextpdf.** { *; }
-keep class org.bouncycastle.** { *; }
-keep class org.slf4j.** { *; }
-dontwarn org.slf4j.impl.StaticLoggerBinder

# CompressionQuality enum
-keep enum com.shrinkmedia.compressor.CompressionQuality { *; }
```

### 4. Key Code Changes (Diff Summary)

**MainActivity.kt — PDF Engine (iText 7)**
- `createPdfFromImages`: single `ITextLayoutDoc` + `AreaBreak(PageSize.A4)` per image
- `mergePdfDocuments`: temp-file → `PdfReader(File)` → `copyPagesTo` → cleanup
- `extractRawTextFromUri`: temp-file → `PdfReader(File)` → `PdfTextExtractor.getTextFromPage` → honest "scan" message
- Import fix: `Image` aliased as `ITextImage` to avoid Compose conflict

**MainActivity.kt — UI Polish**
- Recent section: expandable cards with audit detail, Share, Delete (5s undo), Clear
- SettingsSheet: Theme (System/Light/Dark), OCR Language (6), Batch toggle, Autosave, Pause-on-low-battery
- SnackbarHost + SharedFlow toasts
- Dark/Light/System theme via `ThemeWrapper`
- `addDocumentToRecent` helper for PDF outputs

**OcrHelper.kt**
- `recognizeText(context, uri, language: String = OcrLanguage.ENGLISH.key)` — 3-arg form for language selection
- Defaults to bundled Latin model (ENGLISH)

**SettingsDataStore.kt**
- New additive keys: `ocr_language` (String), `enable_batch` (Boolean)
- `updateOcrLanguage`, `updateEnableBatch` suspend functions
- Defaults: ENGLISH, false (fail closed)

**app/proguard-rules.pro**
- Added `-dontwarn org.slf4j.impl.StaticLoggerBinder`

### 5. Git Diff Summary

```
 6 files changed, 584 insertions(+), 198 deletions(-)
 - app/build.gradle.kts         (dependencies: iText 7.2.5 already present)
 - app/proguard-rules.pro       (+1 line: -dontwarn slf4j)
 - app/src/main/java/com/shrinkmedia/compressor/MainActivity.kt  (PDF + UI)
 - app/src/main/java/com/shrinkmedia/compressor/OcrHelper.kt     (language param)
 - app/src/main/java/com/shrinkmedia/compressor/SettingsDataStore.kt (new keys)
 - .gitignore                   (no functional change)
```

### 6. Sprint Cross-Reference Update

All claims in this evidence map to:
- `docs/sprints/sprint-11-pdf-compile-fixes-and-ui-polish.md`
- `docs/current-state.md` (C9 → iText vector pipeline verified)
- `docs/architecture.md` (Document Eng. module: iText 7 listed)
- `docs/decisions.md` (D005: iText temp-file pattern for reliable Android random access)
- `docs/release-readiness.md` (v0.3.1 gate section)
- `CHANGELOG.md` v0.3.1