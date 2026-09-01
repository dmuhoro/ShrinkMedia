# Sprint 12 — Media Gallery + Quality UX + PDF Preview + Text Fidelity (EXECUTED)

**Status:** EXECUTED (2026-09-01)
**Version Target:** v0.4.0 (versionCode 4) — Major UX overhaul for real-world usability.

## Focus

Address the remaining gaps between "compiles green" and "actually useful on device."
The user tested v0.3.1 and found four concrete UX issues:
1. **PDF text extraction** — output doesn't resemble the original document layout.
2. **PDF build** — no preview; file created in cache but invisible to user.
3. **Media tab home** — only shows recent compressions, not the user's actual media library.
4. **Quality selector** — horizontal chips; wants vertical radio buttons in descending order (HIGH → MEDIUM → LOW).
5. **Size visibility** — before/after sizes should show on every media item.

This sprint delivers all five in a single, cohesive UX layer.

## Deliverables & Evidence

### Layer 1 — MediaTab Overhaul

| Task | Change | File |
|------|--------|------|
| Quality selector | `FilterChip` row → `Column` of `RadioButton` (HIGH, MEDIUM, LOW) | `MainActivity.kt` |
| Your Media section | Query `MediaStore.Images/Video` → `LazyColumn` of thumbnails (Coil) with name, size, date | `MainActivity.kt` + `MediaStoreHelper` |
| RecentSection | Already shows before→after; ensure visible on every card | `MainActivity.kt` |

**MediaStore query** runs on `Dispatchers.IO`, returns `List<MediaFile>` (name, uri, size, mime, date). Coil loads thumbnails via `rememberAsyncImagePainter`. No INTERNET permission needed — local content provider only.

### Layer 2 — DocumentsTab Enhancements

| Task | Change | File |
|------|--------|------|
| PDF build preview | After `createPdfFromImages`, show `AlertDialog` with PDF name, size, page count, **Open** (intent) + **Save to Gallery** + **Discard** | `MainActivity.kt` (`buildPdf` flow) |
| PDF text fidelity | Replace `SimpleTextExtractionStrategy` with `LocationTextExtractionStrategy` (preserves approximate X/Y positions → better paragraph/column reconstruction). Add page headers. | `extractRawTextFromUri` |

**Limitation note:** True pixel-perfect fidelity requires page rendering (PdfRenderer), not text extraction. `LocationTextExtractionStrategy` is the best iText offers for layout-aware text. We document this honestly.

### Layer 3 — Version & Release

- `versionCode 4`, `versionName 0.4.0` in `app/build.gradle.kts`
- Signed release APK via production keystore
- GitHub Release `v0.4.0` with attached APK

## Validation & Verification Checklist

- [x] `compileDebugKotlin` PASS
- [x] `testDebugUnitTest` PASS
- [x] `assembleDebug` PASS
- [x] `compileDebugAndroidTestKotlin` PASS
- [x] `assembleRelease` (R8) PASS
- [x] `lintDebug` PASS
- [x] Device install + launch (API 36) — no crashes
- [x] Media tab: quality radio buttons vertical descending; "Your Media" loads thumbnails
- [x] Documents tab: PDF build shows preview dialog; text extraction improved
- [x] RecentSection: before/after sizes visible on every card
- [ ] All commits SSH-signed; pushed to `origin/main`; tag `v0.4.0` created

## Cross-Reference

- Evidence: `docs/evidence/2026-09-01_media_gallery_quality_ux_pdf_preview.md`
- `docs/current-state.md` (C3, C9 updates)
- `docs/architecture.md` (Media Eng. module updated)
- `CHANGELOG.md` v0.4.0
- `docs/sprint-cross-reference.md`