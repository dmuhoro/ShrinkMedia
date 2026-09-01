# Changelog

All notable changes to ShrinkMedia are documented here, following
[Semantic Versioning](https://semver.org/). Sections: **Added**, **Changed**,
**Fixed**, **Removed**. The full per-sprint narrative lives in
`docs/sprints/`.

## [0.4.0] — 2026-09-01

Real-world usability overhaul: the Media tab now shows the user's actual media
library with a vertical quality selector, PDF builds surface a preview dialog,
and embedded-text extraction preserves layout better. All 6 Android gates and
the on-device launch gate are green.

### Added
- **Your media library** (`MainActivity.kt` — `MediaFile`, `MediaFileCard`,
  `getUserMediaFiles`):
  - Queries `MediaStore.Images.Media` + `MediaStore.Video.Media` on
    `Dispatchers.IO` at ViewModel init (date-descending).
  - Coil thumbnails for images (`rememberAsyncImagePainter`), video icon for
    videos; shows name, type, size; per-item **Compress** button.
  - Purely local content-provider reads — no storage permission, no INTERNET.
- **Vertical quality selector**: `FilterChip` row → `Column` of `RadioButton`
  rendered **HIGH → MEDIUM → LOW** (descending) with per-preset JPEG q/max-dim
  captions; `CompressionQuality` reordered `HIGH(90,2560)`, `MEDIUM(75,1920)`,
  `LOW(55,1280)`.
- **PDF build preview** (`PdfPreviewState`): after `createPdfFromImages`, a
  card shows the file name, page count, and size with **Open** (FileProvider
  `ACTION_VIEW`, fallback toast "No PDF viewer found"), **Save to Gallery**
  (`saveToPublicMediaStore` + `addDocumentToRecent`, explicit success/failure
  toast), and **Discard** (deletes the temp file).
- **`ToolkitViewModel.showToast(message)`** — explicit SharedFlow emit for
  transient UI messages.
- **Sprint 12 docs**: `docs/sprints/sprint-12-media-gallery-quality-ux-pdf-preview.md`
  (EXECUTED), `docs/evidence/2026-09-01_media_gallery_quality_ux_pdf_preview.md`.

### Changed
- **PDF text extraction** (`extractRawTextFromUri`): `SimpleTextExtractionStrategy`
  → `LocationTextExtractionStrategy` (approximate X/Y layout preservation for
  better paragraph/column reconstruction) + `--- Page N ---` headers between
  non-blank pages. Fidelity limitation documented honestly (D006): not
  pixel-perfect; true fidelity needs `PdfRenderer` page rendering.
- Version bumped to `0.4.0` (`versionCode 4`).

### Fixed
- `MainActivity.kt` `android.graphics.Color` vs Compose `Color` conflict —
  Compose color aliased as `ComposeColor`; PDF canvas uses fully-qualified
  `android.graphics.Color.WHITE`.
- `Modifier.clip(shape)` unresolved in the Compose BOM — thumbnail tile uses
  `Modifier.background(color, RoundedCornerShape(8.dp))`, which clips rounded
  corners automatically.

### Note
- All builds pass: `compileDebugKotlin`, `testDebugUnitTest`, `assembleDebug`,
  `compileDebugAndroidTestKotlin`, `assembleRelease` (R8 executed), `lintDebug`.
- App installs + launches on API-36 device with no crashes (top resumed
  activity `com.shrinkmedia.compressor/.MainActivity`, logcat clean).
- Manifest still declares **no INTERNET permission** (privacy invariant held).
- Evidence: `docs/evidence/2026-09-01_media_gallery_quality_ux_pdf_preview.md`.

## [0.3.1] — 2026-09-01

PDF pipeline hardened with iText 7 (true vector build, page-exact merge, real
embedded-text extraction), UI polished with recent files management, settings
sheet, and dark/light/system theme support. All compilation gates green
including R8 minification.

### Added
- **iText 7.2.5 PDF engine** (on-device, no INTERNET):
  - `createPdfFromImages`: single `ITextLayoutDoc` + `AreaBreak(PageSize.A4)`
    per image → true vector PDF pages (crisp, no bitmap rasterization).
  - `mergePdfDocuments`: temp-file → `PdfReader(File)` → `copyPagesTo` →
    cleanup; reliable random access on Android, no `byte[]` constructor issues.
  - `extractRawTextFromUri`: iText `PdfTextExtractor` +
    `SimpleTextExtractionStrategy` on temp file; honest "scan / OCR needed"
    message when no embedded text found.
  - `readPdfMetrics` / `splitPdfIntoPages` retained on `android.graphics.pdf`
    (page count + bitmap fallback).
- **Recent files section** with expandable audit detail cards:
  - Share (system share sheet via `FileProvider`).
  - Delete → 5s undo (moves to app-sandbox trash dir, restores on undo).
  - Clear all → removes cached files + history.
  - Audit detail panel: quality preset, target bitrate, resolution scaling,
    duration, media type.
- **Settings sheet** (ModalBottomSheet):
  - Theme: System / Light / Dark (persisted).
  - OCR language: English, Spanish, French, German, Italian, Portuguese
    (6 options, additive DataStore key, default ENGLISH).
  - Batch mode toggle (advanced, off by default).
  - Autosave to gallery, Pause on low battery.
  - Privacy reminder: "All processing stays on-device. No files are uploaded.
    The app declares no INTERNET permission."
- **Snackbar toasts** via `MutableSharedFlow<String>` + `SnackbarHost` —
  transient user-facing messages replace inline status text.
- **Dark/Light/System theme** via `AppThemeMode` enum + `ThemeWrapper`
  composable; persists to DataStore.
- **OCR language parameter**: `OcrHelper.recognizeText(context, uri, language)`
  3-arg form; `OcrLanguage` enum with ML Kit language codes.
- **DataStore additive keys** (fail-closed defaults):
  - `ocr_language` (String, default `en`).
  - `enable_batch` (Boolean, default `false`).
- **R8 proguard fix**: `-dontwarn org.slf4j.impl.StaticLoggerBinder` in
  `app/proguard-rules.pro` (iText pulls in slf4j-api; binding is optional).

### Changed
- **PDF build/merge/extract** now use iText 7 consistently (was mixed
  `android.graphics.pdf` + heuristic text extraction).
- `mergePdfDocuments` and `extractRawTextFromUri` use temp-file pattern
  (D005) for reliable `PdfReader(File)` random access.
- `createPdfFromImages` uses single layout `Document` + `AreaBreak` per page
  (was new `PdfDocument` per image).
- `OcrHelper.recognizeText` signature extended with `language` parameter
  (default ENGLISH); `AiTab` passes selected language from state.
- `MainActivity` UI: `DocumentsTab` hero text updated to reflect iText
  embedded-text extraction; `AiTab` OCR card shows selected language label.
- Recent compression entries now include PDF outputs via `addDocumentToRecent`.

### Fixed
- **Compile errors** from iText migration resolved:
  - `PdfReader` constructor mismatches (no `byte[]` overload on Android) —
    fixed via temp-file pattern.
  - `Image` import conflict (iText vs Compose icons) — fixed with `ITextImage`
    alias.
  - `AreaBreak` API: `AreaBreak(PageSize.A4)` (was `AreaBreakType.NEXT_PAGE`).
  - Removed unused imports (`RandomAccessFile`, `RandomAccessSourceFactory`).
- **R8 release build** warning on missing `StaticLoggerBinder` — fixed with
  `-dontwarn` proguard rule.

### Note
- All builds pass: `compileDebugKotlin`, `testDebugUnitTest`, `assembleDebug`,
  `compileDebugAndroidTestKotlin`, `assembleRelease` (R8), `lintDebug`.
- App installs and launches on API-36 device without crashes.
- Manifest still declares **no INTERNET permission** (privacy invariant held).
- Evidence: `docs/evidence/2026-09-01_pdf_compile_fixes_ui_polish.md`.

## [0.3.0] — 2026-08-31

Real on-device OCR, honest batch failure surfacing, and the hardware-verified
release gate. Adds local text recognition via ML Kit (no INTERNET), surfaces
every per-file batch failure with an on-device audit record, and proves the
native engine green on an API-36 phone.

### Release & Distribution (v0.3.0, production keystore)
- **Production keystore generated** (`~/.android/keystores/shrinkmedia-release.jks`,
  PKCS12, 2048-bit RSA, SHA256withRSA, 10,000-day validity, alias `shrinkmedia`).
  Kept **outside the repo**; `keystore.properties` (repo root, **gitignored**)
  drives the release `SigningConfig` and fails closed if absent.
- **Signed release APK** `app-release.apk`: `versionName 0.3.0`, `versionCode 3`,
  `applicationId com.shrinkmedia.compressor`, R8-minified;
  `apksigner verify` PASS (cert SHA-256 `21569322706156fe...` — matches keystore).
- **Distribution:** direct APK sideload via a GitHub Release (`v0.3.0`). Not the
  Play route (per user decision). Signed APK installs + launches on API-36.
- Evidence: `docs/evidence/2026-08-31_v0.3.0_signed_release.md`.

### Added
- **On-device OCR** (ADR-009): `OcrHelper` (typed-null `recognizeText`, bounded
  decode) + ML Kit `com.google.mlkit:text-recognition:16.0.1` +
  `com.google.mlkit.vision.text.latin.TextRecognizerOptions`. New `AiTab`
  "Scan reader (OCR)" card explicitly distinguishing success / empty /
  no-text / failure (no silent drops). R8 keeps ML Kit (`seeds.txt`/`usage.txt`).
- **Google Tools Bridge planning ADR (ADR-010)** staging Drive/Docs/
  cloud-Gemini OUT of v1 as opt-in connected mode; AICore stays ASPIRATIONAL;
  `docs/ideas.md` I001.
- **Batch no-silent-drops audit** (Article I.6): `BatchFailureAudit` writes a
  timestamped `batch-audit.log` in the app sandbox; the completion notification
  now reports count + reason summary and a warning icon on any failure.
- **Device-verification evidence** (`docs/evidence/2026-08-31_device_verification.md`)
  and Sprint 8/9 records; Sprint 8 promoted to EXECUTED.
- **New instrumented test** `failure_audit_record_is_written_to_on_device_sandbox`.

### Changed
- **Sprint 8 device gate → PASS.** `connectedDebugAndroidTest` is green: 9/9
  tests on Xiaomi `25078RA3EA` (API 36) covering real compression, fail-closed
  null, gallery autosave, battery-pause (never drops), DataStore savings, the
  audit record, and **real on-device OCR** (reads "SHRINKMEDI").
- **Web-sim `GRADLE_CODE` parity** now lists the ML Kit dependency.
- State docs (`current-state.md`, `architecture.md`, `decisions.md`,
  `release-roadmap.md`, `release-readiness.md`) reflect C12 implemented, C11
  staged v2, C16/C17 added.

### Fixed
- **Three genuine instrumented-test defects** surfaced by the first device run
  (correctly fixed, not weakened): the pause-gate test now arms `isPaused=true`,
  two `@Test` methods are void (JUnit `InvalidTestClassError`), and the
  real-compression test input is a lossless PNG (decodeable on-device) instead
  of a synthetic BMP that API 36's `BitmapFactory` can't read (bounds w=-1/h=-1).
- **On-device OCR `decodeBounded` bug (device-found, commit `3b5c134`):**
  bounds-only decode (`inJustDecodeBounds=true`) always returns a null Bitmap, so
  `openInputStream(...)?.use { decodeStream(...) } ?: return null` returned null
  before checking bounds and OCR never ran. Removed the erroneous elvis; the
  downstream bounds check still covers a genuine null stream. OCR now returns
  recognized text on device.

### Note
- OCR uses ML Kit's **bundled** Latin model (`TextRecognizerOptions.DEFAULT_OPTIONS`,
  `com.google.mlkit:text-recognition`) — no model download, still fully on-device,
  no INTERNET permission in the manifest. The on-device walkthrough is captured in
  `docs/evidence/2026-08-31_device_verification.md`; the bundled model reads the
  word core "SHRINKMEDI" on large monochrome synthetic text (drops a trailing
  glyph — a model-fidelity quirk, not a wiring issue).

Housekeeping + release-hardening release: removed the inherited Google AI Studio/
Gemini artifacts, consolidated the sprint documentation into a single,
execution-ordered folder, added honest test coverage, and made the app
releasable (production namespace, R8 minification + signing, launcher icon,
hardened CI).

### Removed
- **Google AI Studio / Gemini fingerprints**: `metadata.json` (AI Studio
  applet descriptor), `assets/.aistudio/`, `.env.example` (documented
  `GEMINI_API_KEY` / `APP_URL`), and unused `@google/genai`, `dotenv`,
  `express`, `@types/express` dependencies (regenerated `package-lock.json`,
  −121 packages). No AI Studio/Gemini references remain in the product code or
  docs.
- **Root `sprints/` folder** (mis-numbered plan files). All sprint docs now
  live in a single `docs/sprints/` (records 1–7 + active plan 8).
- **`com.example.mediacompressor` namespace** replaced across Kotlin sources,
  `App.tsx` code samples, and manifest default icon.

### Added
- **Sprint 6 record** (`docs/sprints/sprint-6-ai-studio-cleanup-sprint-consolidation.md`)
  covering the cleanup/consolidation.
- **Sprint 7 record** (`docs/sprints/sprint-7-test-hardening-and-release-config.md`)
  covering honest tests + release configuration.
- **Sprint 8 plan** (`docs/sprints/sprint-8-device-verification-final-gate.md`)
  — device verification, re-sequenced as the **final** release gate.
- **`docs/release-roadmap.md`** — step-by-step release path with per-step
  status and exit gates.
- **Honest web batch model** (`src/lib/batch.ts`, `src/lib/batch.test.ts`):
  pure `buildBatchResults`/`accumulateSavings` mirroring native
  `recordCompressionSavings` semantics; `src/App.tsx` uses it (no fake savings).
- **Android JVM unit tests** (`CompressionQualityUnitTest.kt`) and **real-path
  instrumented tests** (`CompressionPipelineInstrumentedTest`,
  `BatchPauseContractTest` — fail-closed pause gate never drops an item,
  `BitmapCompressUtil`) with test deps in `app/build.gradle.kts`.
- **Production release config**: `com.shrinkmedia.compressor` namespace +
  `applicationId`, `versionCode 2` / `versionName 0.2.1`, R8
  (`isMinifyEnabled=true`) + `app/proguard-rules.pro` (FFmpegKit +
  `CompressionQuality` keep rules), optional gitignored `keystore.properties`
  signing config, adaptive launcher icon + label via `@string/app_name`.
- **Hardened CI** (`.github/workflows/ci.yml`): upgraded off deprecated
  Node-20 actions (checkout/setup-node/setup-java/cache v5,
  upload-artifact v7), Android job runs JVM unit tests + compiles instrumented
  APK, added a fail-closed signed `bundleRelease` AAB job gated on secrets.

### Changed
- `vite.config.ts` stripped of AI Studio `DISABLE_HMR`/watch logic.
- Web package renamed `react-example` → `shrinkmedia-web`.
- `ROADMAP.md`, `docs/current-state.md`, `docs/sprint-cross-reference.md`,
  `docs/release-readiness.md` updated for Sprint 7/8 re-sequencing (hardware
  verification last).
- Docs updated to reflect a single sprint folder and a pure static web
  simulator with no runtime secrets: `README.md`, `CONTRIBUTING.md`,
  `SECURITY.md`, `.ai/VERSION` (1.1.0), `.ai/context/02, 07, 08, 09, 12`,
  `.ai/agents/builder-agent.md`, `docs/runbooks/web-simulator.md`,
  `docs/adr/ADR-006-web-simulator-harness.md`,
  `docs/sprint-cross-reference.md`, `docs/release-readiness.md`,
  `docs/sprints/sprint-2`, `docs/sprints/sprint-5`,
  `docs/evidence/2026-08-30_governance_scaffold.md`.

### Fixed
- Manifest no-INTERNET guardrail (`ci.yml`, `.ai/context/08`,
  `.ai/agents/auditor-agent.md`) previously grepped the noisy
  `INTERNET|http://|https://` pattern, which the mandatory XML namespace
  (`http://schemas.android.com/apk/res/android`) would trip — a false
  positive. It now matches only the `android.permission.INTERNET` declaration.
  (No runtime code changed.)
- **Release builds now fail closed**: previously the release buildType had no
  signing/config path; now `assembleRelease`/`bundleRelease` produce a signed
  artifact only when a keystore is present, otherwise they refuse (no unsigned
  distribution).

### Note
- Reverse-dependency check: removing `com.example.mediacompressor` is safe
  because no released build has shipped under that namespace; `0.1.0` was an
  informal prototype with no published artifact.

## [0.2.0] — 2026-08-30

Engineering foundation, unified toolkit, and battery-aware batch compression.

### Added
- **Engineering governance scaffold**: Constitution (`docs/engineering/CONSTITUTION.md`),
  ADRs (`docs/adr/`), decisions log, architecture + current-state docs,
  code-standards, sprint records, evidence log + release-readiness table,
  runbooks, active sprint plans, `.ai/` context + agent definitions,
  `.github/` CI workflow + templates.
- Root governance files: `AGENTS.md`, `CONTRIBUTING.md`, `ROADMAP.md`,
  `SECURITY.md`, `LICENSE` (MIT).
- Web tooling: Vitest with `lib/` helper module + unit tests.
- Gradle wrapper entry points for reproducible Android builds.

### Changed
- `MainActivity.kt` consolidated into a three-tab toolkit (Media / Documents /
  Elite AI) driven by a ViewModel `UiState` — compact, single-responsibility
  UI with quality presets, autosave toggle, and pause-on-low-battery toggle.
- `BatchCompressionService.kt` now battery-aware: a shared
  `BatchCompressionPauseController` state, `ACTION_BATTERY_LOW` receiver
  (registered only when the setting is enabled), and per-file reads of the
  live DataStore autosave preference.
- `SettingsDataStore.kt` persists `pauseCompressionOnLowBattery` (additive
  key, boolean default `false`).
- `README.md` rebuilt as a living product-state document.

### Fixed
- Batch compression previously read the autosave flag once at start from a
  stale intent extra; it now respects the **live** DataStore preference per
  file.
- Android build: the committed `io.github.root0as:ffmpeg-kit-lite:6.0-2`
  dependency did not exist on any repository, so `assembleDebug` could never
  resolve. Replaced with the Maven-Central-published `io.github.nikita36078:ffmpeg-kit:6.0.LTS`
  fork (same `com.arthenica.ffmpegkit` API, libx264 enabled on all ABIs).
  See `docs/evidence/2026-08-30_android_config_check.md`.

## [0.1.0] — 2026-08-30

Initial project scaffold.

### Added
- Android app skeleton (Kotlin + Jetpack Compose, Material 3, Coil, FFmpegKit
  Lite, DataStore) with single-image/video compression and quality presets.
- Foreground service for background batch compression (first cut).
- Web simulator (Vite + React + TypeScript + Tailwind) with a live preview of
  the compressor UI.
- `README.md`, `.gitignore`, `.env.example`.

### Changed
- None.

### Fixed
- None.