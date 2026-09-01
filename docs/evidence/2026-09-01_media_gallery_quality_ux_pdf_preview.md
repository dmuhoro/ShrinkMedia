# Evidence: Sprint 12 — Media Gallery + Quality UX + PDF Preview + Text Fidelity (2026-09-01)

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
BUILD SUCCESSFUL in 9s
```

```bash
$ ./gradlew :app:testDebugUnitTest
> Task :app:testDebugUnitTest UP-TO-DATE
BUILD SUCCESSFUL in 9s
```

```bash
$ ./gradlew :app:assembleDebug
> Task :app:assembleDebug UP-TO-DATE
BUILD SUCCESSFUL in 11s
```

```bash
$ ./gradlew :app:compileDebugAndroidTestKotlin
> Task :app:compileDebugAndroidTestKotlin UP-TO-DATE
BUILD SUCCESSFUL in 10s
```

```bash
$ ./gradlew :app:lintDebug
> Task :app:lintReportDebug
> Task :app:lintDebug
BUILD SUCCESSFUL in 58s
```

```bash
$ ./gradlew :app:assembleRelease
> Task :app:minifyReleaseWithR8
> Task :app:packageRelease
> Task :app:assembleRelease
BUILD SUCCESSFUL in 9m 47s
```

Note: `assembleRelease` ran R8 minification (`minifyReleaseWithR8` executed) —
this exercised the real shrink + keep-rules path against the new code.

### 2. Device Verification

Device present and authorised:

```bash
$ /home/daniel-muhoro/android-cmdline-tools/platform-tools/adb devices
List of devices attached
49IZ6DJ7SONNQOBE	device
```

Install the v0.4.0 debug APK:

```bash
$ /home/daniel-muhoro/android-cmdline-tools/platform-tools/adb install -r --user 0 \
    app/build/outputs/apk/debug/app-debug.apk
Performing Streamed Install
Success
```

Launch the app and confirm the resumed activity + no crash:

```bash
$ export ADB=/home/daniel-muhoro/android-cmdline-tools/platform-tools/adb
$ $ADB shell am force-stop com.shrinkmedia.compressor
$ $ADB shell am start -n com.shrinkmedia.compressor/.MainActivity
Starting: Intent { cmp=com.shrinkmedia.compressor/.MainActivity }

$ $ADB logcat -d | grep -iE "FATAL|AndroidRuntime" | tail
(no matches — no crashes)

$ $ADB shell pidof com.shrinkmedia.compressor
8337
PROCESS RUNNING

$ $ADB shell "dumpsys activity activities | grep -E 'topResumedActivity'"
topResumedActivity=ActivityRecord{... u0 com.shrinkmedia.compressor/.MainActivity ...}
```

Result: app process alive (PID 8337), `MainActivity` is the top resumed
activity, no FATAL / AndroidRuntime crashes in logcat. The only log lines are
harmless MIUI framework notices (`MiuiPreloadClassImpl`, `ActivityThread`).

### 3. Release Artifact Verification

```bash
$ aapt2 dump badging app/build/outputs/apk/release/app-release.apk
package: name='com.shrinkmedia.compressor' versionCode='4' versionName='0.4.0' ...

$ apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
Signer #1 certificate DN: CN=ShrinkMedia, OU=ShrinkMedia, O=ShrinkMedia, ...
Signer #1 certificate SHA-256 digest: 21569322706156fe02111cc5a8ce0f62...

$ git ls-remote --tags origin | grep v0.4.0
c09fb4b0f36cfb71fb5bf00d0a8c35540cf3fa3e	refs/tags/v0.4.0
c43a2c977520aaea50d5a776bd3a4cd5d2ed7f57	refs/tags/v0.4.0^{}   # tags commit c43a2c9

$ gh release view v0.4.0 --json isDraft,tagName,publishedAt,assets
{"assets":["app-release.apk"],"isDraft":false,"publishedAt":"2026-09-01T20:15:57Z","tagName":"v0.4.0"}
```

All 4 Sprint 12 commits carry Good ED25519 SSH signatures
(`git log --show-signature`). Release: https://github.com/dmuhoro/ShrinkMedia/releases/tag/v0.4.0

### 4. Key Code Changes (Diff Summary)

**app/build.gradle.kts — v0.4.0**
- `versionCode 4`, `versionName "0.4.0"` (was 3 / 0.3.0)

**MainActivity.kt — MediaTab Overhaul**
- Quality selector: `FilterChip` row → vertical `Column` of `RadioButton`,
  rendered `HIGH → MEDIUM → LOW` (descending) via
  `CompressionQuality.entries.sortedByDescending { it.ordinal }`, each with a
  per-preset JPEG q / max-dimension caption line.
- `CompressionQuality` reordered: `HIGH(90,2560)`, `MEDIUM(75,1920)`,
  `LOW(55,1280)`.
- **Your media library** section: `getUserMediaFiles` queries
  `MediaStore.Images.Media` + `MediaStore.Video.Media` on `Dispatchers.IO`,
  returns `List<MediaFile>` (id, name, uri, size, mime, date, isVideo) sorted
  date-desc. Loaded on ViewModel init via `loadUserMedia()`; top 20 rendered as
  `MediaFileCard` thumbnails (Coil `rememberAsyncImagePainter` for images, video
  icon for videos) with a per-item **Compress** button. All local content
  provider — no INTERNET permission.
- Before→after size visibility: `ResultCard` ("Latest media") keeps
  `before → after`; every `MediaFileCard` shows the file size; PDF preview card
  shows built-file size.

**MainActivity.kt — DocumentsTab PDF Preview**
- `buildPdf()` now computes the page count (`readPdfPageCount` via
  `PdfRenderer`) and exposes a `PdfPreviewState` card with:
  - **Open** — `FileProvider` URI + `ACTION_VIEW` intent (with
    `FLAG_GRANT_READ_URI_PERMISSION`); falls back to a `showToast`
    ("No PDF viewer found") on failure (no silent drop).
  - **Save to Gallery** — `savePdfToGallery` → `saveToPublicMediaStore` +
    `addDocumentToRecent`; explicit success/failure toast.
  - **Discard** — deletes the temp file and clears preview.
- Added `ToolkitViewModel.showToast` (SharedFlow emit).

**MainActivity.kt — PDF Text Fidelity**
- `extractRawTextFromUri`: `SimpleTextExtractionStrategy` →
  `LocationTextExtractionStrategy` (layout-aware X/Y preservation) + a
  `--- Page N ---` header between non-blank pages.

**Honest fidelity note:** `LocationTextExtractionStrategy` preserves
approximate page coordinates for better paragraph/column reconstruction, but it
is **not** pixel-pixel identical to the source document. True fidelity requires
page rendering (`PdfRenderer`), which is out of scope for text extraction. This
is documented rather than over-claimed.

### 5. Git Diff Summary

```
 3 files changed, 239 insertions(+), 16 deletions(-)
 - app/build.gradle.kts  (versionCode 4 / versionName 0.4.0)
 - app/src/main/java/com/shrinkmedia/compressor/MainActivity.kt  (media gallery,
     quality radio UX, PDF preview, LocationTextExtractionStrategy, MediaFile/
     PdfPreviewState types)
 - docs/sprints/sprint-12-media-gallery-quality-ux-pdf-preview.md (plan)
```

### 6. Cross-Reference

All claims in this evidence map to:
- `docs/sprints/sprint-12-media-gallery-quality-ux-pdf-preview.md`
- `docs/current-state.md` (C3 media gallery & quality UX)
- `docs/architecture.md` (Media Eng. module: MediaStore gallery query)
- `docs/release-readiness.md` (v0.4.0 gate section)
- `CHANGELOG.md` v0.4.0
- `docs/sprint-cross-reference.md` (Sprint 12 row)