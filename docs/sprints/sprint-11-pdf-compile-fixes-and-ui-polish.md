# Sprint 11 — PDF Compile Fixes + UI Polish (EXECUTED)

**Status:** EXECUTED (2026-09-01)
**Version Target:** v0.3.1 (versionCode 4) — post-release maintenance sprint
that hardens the PDF pipeline (iText 7) and polishes the on-device UI.

## Focus

The v0.3.0 release shipped with the OCR and no-silent-drops features
device-verified. However, the Documents tab (image-to-PDF, PDF merge, text
extraction) was using `android.graphics.pdf` + a fragile heuristic for text
extraction. This sprint:
1. Rewrites all PDF engine helpers to use **iText 7.2.5** (on-device, no
   INTERNET) for true vector PDF build/merge and real embedded-text extraction.
2. Fixes every compile error that arose from the iText migration
   (`PdfReader` constructor mismatches, import conflicts, `AreaBreak` API).
3. Adds R8 proguard rule for the optional `slf4j` binding that iText depends on.
4. Polishes the UI: recent files with delete/undo/clear/share, audit detail
   panel, settings sheet (theme, OCR language, batch toggle), snackbar toasts,
   and dark/light/system theme support.
5. Adds OCR language selection (ENGLISH, SPANISH, FRENCH, GERMAN, ITALIAN,
   PORTUGUESE) persisted via DataStore (additive keys, fail-closed defaults).
6. Validates the full pipeline: `compileDebugKotlin`, `testDebugUnitTest`,
   `assembleDebug`, `compileDebugAndroidTestKotlin`, `assembleRelease` (R8),
   `lintDebug` — all **PASS**. App installs and launches on API-36 device
   without crashes.

## Deliverables & Evidence

### Layer A — PDF engine rewrite (iText 7)

- **`createPdfFromImages`** (MainActivity.kt): single `ITextLayoutDoc` +
  `AreaBreak(PageSize.A4)` per image → true vector PDF pages (commit in this
  sprint). Old `android.graphics.pdf` path rendered bitmaps into pages; new
  path uses iText `PdfImageXObject` + `AreaBreak` for crisp vector output.
- **`mergePdfDocuments`** (MainActivity.kt): writes each input URI to a temp
  file in cache dir → `PdfReader(File)` → `copyPagesTo` → cleanup. Reliable
  random access on Android (avoids `RandomAccessSourceFactory`/`byte[]`
  constructor mismatches). Temp files deleted after merge.
- **`extractRawTextFromUri`** (MainActivity.kt): same temp-file + `PdfReader(File)`
  pattern → `PdfTextExtractor.getTextFromPage` with `SimpleTextExtractionStrategy`
  → honest "scan / OCR needed" message when no embedded text found.
- **Imports fixed**: `Image` alias `ITextImage` resolves conflict with
  `androidx.compose.material.icons.filled.Image`. Removed unused
  `RandomAccessFile` / `RandomAccessSourceFactory` imports.
- **R8 proguard fix**: added `-dontwarn org.slf4j.impl.StaticLoggerBinder`
  to `app/proguard-rules.pro` (iText pulls in slf4j-api; the binding is
  optional at runtime — no crash, but R8 warns without this).

### Layer B — UI polish & settings

- **Recent files section** (MainActivity.kt): expandable cards showing
  before/after size, quality preset, duration, bitrate, media type. Each
  entry has Share, Delete (with 5s undo), and audit detail panel.
- **Delete/Undo/Clear** workflow: move to app-sandbox trash dir → 5s delay
  → permanent delete; Undo restores from trash; Clear removes all recent
  entries + cached files.
- **Settings sheet** (ModalBottomSheet): theme (System/Light/Dark),
  OCR language (6 languages, additive DataStore key), batch mode toggle,
  autosave, pause-on-low-battery, privacy reminder.
- **Snackbar toasts** via `MutableSharedFlow<String>` + `SnackbarHost`
  — replaces inline status text with transient user-facing messages.
- **Dark/Light/System theme** via `AppThemeMode` enum + `ThemeWrapper`
  composable; persists to DataStore.
- **OCR language** (`OcrLanguage` enum): 6 options, default ENGLISH, keyed
  by ML Kit language code string (additive, never removed).

### Layer C — Settings persistence (DataStore)

- New additive keys in `SettingsDataStore.kt`:
  - `ocr_language` (String, default `OcrLanguage.ENGLISH.key`)
  - `enable_batch` (Boolean, default `false` — fail closed)
- `updateOcrLanguage`, `updateEnableBatch` suspend functions in
  `SettingsRepository`.
- All booleans default `false` (fail closed); quality defaults `MEDIUM`.

### Layer D — Validation (all green)

| Task | Result |
|------|--------|
| `compileDebugKotlin` | PASS |
| `testDebugUnitTest` | PASS |
| `assembleDebug` | PASS |
| `compileDebugAndroidTestKotlin` | PASS |
| `assembleRelease` (R8 minify) | PASS |
| `lintDebug` | PASS |
| Device install + launch (API 36) | PASS (no crashes) |

## Validation & Verification Checklist

- [x] All Android compilation gates green (unit, debug, release R8, lint).
- [x] PDF merge / build / extract use iText 7 correctly (temp-file pattern).
- [x] R8 minification passes with `-dontwarn org.slf4j.impl.StaticLoggerBinder`.
- [x] Settings additive keys + fail-closed defaults honored.
- [x] Recent files UI: delete/undo/clear/share + audit detail all wired.
- [x] OCR language persisted + used in `OcrHelper.recognizeText` (3-arg form).
- [x] App installs + launches on API-36 device without crashes.
- [x] No INTERNET permission in manifest (unchanged).
- [x] All commits SSH-signed; pushed to `origin/main`.

## Cross-Reference

- Evidence: `docs/evidence/2026-09-01_pdf_compile_fixes_ui_polish.md`
- `docs/current-state.md` (C9 updated: iText vector pipeline)
- `docs/architecture.md` (Document Eng. module updated)
- `docs/decisions.md` (D005 added: iText temp-file pattern)
- `docs/release-readiness.md` (new v0.3.1 gate section)
- `CHANGELOG.md` v0.3.1
- `docs/sprint-cross-reference.md`