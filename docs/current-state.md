# ShrinkMedia — Current State

> Orientation doc: **what is real vs ASPIRATIONAL**, as of 2026-08-31.
> Read before any task (AGENTS.md §7). If a task assumes a capability marked
> **ASPIRATIONAL**, stop and say so before building on it.

Legend: ✅ implemented & verified • ⚠️ implemented, device run pending •
🟡 designed/partial • ⚠️ **ASPIRATIONAL** not wired.

## Capability Matrix

| # | Capability | Status | Evidence / Notes |
|---|-----------|--------|------------------|
| C1 | Engineering governance scaffold | ✅ | Constitution, ADRs, decisions, sprints, evidence, CI |
| C2 | Web simulator (Vite+React+TS) lint/build/tests | ✅ | `npm run lint` / `npm test` (18) / `npm run build` green (Sprint 7 evidence) |
| C3 | Image compression (bitmap sampling + JPEG) | ⚠️ | Helper `compressImageFile`; JVM-verified math mirror in `src/lib`; **device run is final Sprint 8 gate** |
| C4 | Video compression (FFmpegKit x264, preset CRF/bitrate) | ⚠️ | `compressVideoFile` awaits session state; **device run Sprint 8** |
| C5 | Foreground batch service + progress notification | ⚠️ | `BatchCompressionService` wired into Media tab (`startBatch`); **device run Sprint 8** |
| C6 | Battery-aware pause/resume (opt-in) | ⚠️ | Controller + receiver; instrumented `BatchPauseContractTest` proves the pause gate never drops an item (compiles); **device run Sprint 8** |
| C7 | Autosave to MediaStore (opt-in, no permission) | ⚠️ | `saveToPublicMediaStore` returns Boolean; instrumented test covers insert (compiles); **device run Sprint 8** |
| C8 | DataStore settings persistence | ✅ | `SettingsRepository` flow; additive keys; fail-closed defaults |
| C9 | PDF build / merge / split / metrics | ✅ | iText 7.2.5 (on-device, no INTERNET); `createPdfFromImages` (vector pages via `AreaBreak`), `mergePdfDocuments` (temp-file + `copyPagesTo`), `splitPdfIntoPages` (kept on `android.graphics.pdf` for bitmap fallback), `readPdfMetrics` (`PdfRenderer`); **device run PASS** |
| C10 | PDF embedded-text extraction | ✅ | iText `PdfTextExtractor` + `SimpleTextExtractionStrategy` (temp-file + `PdfReader(File)`); honest "scan / OCR needed" when no embedded text; **device run PASS** |
| C11 | AICore device-model handoff | **ASPIRATIONAL** | `AiTab` placeholder only — no AICore dependency, no availability check (D001); staged as v2 (ADR-010) |
| C12 | Real OCR (scanned PDFs/images) | ✅ | Implemented (ADR-009): ML Kit `text-recognition` via `OcrHelper` (typed-null, no INTERNET), `AiTab` "Scan reader"; **device walkthrough PASS** (reads "SHRINKMEDI" on API 36); R8-verified |
| C13 | Signed, R8-minified release build (`com.shrinkmedia.compressor` v0.3.0) | ✅ | `app/build.gradle.kts` (minify + `signingConfigs.create("release")`); `apksigner verify` PASS on `app-release.apk` **signed with the production keystore** (`~/.android/keystores/shrinkmedia-release.jks`, gitignored `keystore.properties`); distributed via GitHub sideload |
| C14 | Android instrumentation tests **written** (JVM + instrumented) | ⚠️ | JVM unit tests run green; instrumented APK compiles; **execution on device = Sprint 8** |
| C15 | CI hardened to Node-24 actions + signed release-AAB job | ✅ | `.github/workflows/ci.yml` (checkout/setup-node/setup-java/cache v5, upload-artifact v7, fail-closed release job) |
| C16 | Batch no-silent-drops: per-file failure surfacing + on-device audit log | ⚠️ | `BatchFailureAudit` + completion notification reason summary (Constitution I.6); instrumented `failure_audit_record...` written; **device run Sprint 8** |
| C17 | Google Tools Bridge (Drive/Docs/cloud-Gemini) | **ASPIRATIONAL (v2)** | ADR-010: deliberately staged OUT of v1, opt-in connected mode, fail-closed on-device default; Photos needs no bridge (system picker); AICore device-gated |
| C13 | Signed, R8-minified release build (`com.shrinkmedia.compressor` v0.3.0) | ✅ | `app/build.gradle.kts` (minify + `signingConfigs.create("release")`); `apksigner verify` PASS on `app-release.apk` **signed with the production keystore** (`~/.android/keystores/shrinkmedia-release.jks`, gitignored `keystore.properties`); distributed via GitHub sideload |
| C14 | Android instrumentation tests **written** (JVM + instrumented) | ⚠️ | JVM unit tests run green; instrumented APK compiles; **execution on device = Sprint 8** |
| C15 | CI hardened to Node-24 actions + signed release-AAB job | ✅ | `.github/workflows/ci.yml` (checkout/setup-node/setup-java/cache v5, upload-artifact v7, fail-closed release job) |

## What This Means For New Work

- **The on-device invariant is structural:** the manifest has no INTERNET
  permission. Adding network = ADR (Article II.2).
- **Compression/PDF helpers are shippable but device-untested.** The final
  verification pass (**Sprint 8**) must exercise the real paths on hardware and
  record it in `docs/evidence/`.
- **Nothing under C11 may be claimed as delivered.** It remains ASPIRATIONAL
  until implemented **and** verified (staged v2, ADR-010). C12 OCR is
  implemented **and** device-verified (reads "SHRINKMEDI" on API 36;
  `docs/evidence/2026-08-31_device_verification.md`).
- **Release signing is complete.** The production keystore
  (`~/.android/keystores/shrinkmedia-release.jks`) is generated; `keystore.properties`
  references it and is gitignored (fail-closed if absent). `apksigner verify` PASS on
  the v0.3.0 `app-release.apk`. **Backup the keystore + passwords off-machine — the
  keystore is the app's permanent identity and cannot be recovered if lost.**

## Sprint Cross-Reference

Every claim above is audited sprint-by-sprint in
`docs/sprint-cross-reference.md`. The release gate table lives in
`docs/release-readiness.md`; the step-by-step path lives in
`docs/release-roadmap.md`.