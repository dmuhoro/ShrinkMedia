# ShrinkMedia — Current State

> Orientation doc: **what is real vs ASPIRATIONAL**, as of 2026-09-03.
> Read before any task (AGENTS.md §7). If a task assumes a capability marked
> **ASPIRATIONAL**, stop and say so before building on it.

Legend: ✅ implemented & verified • ⚠️ implemented, device run pending •
🟡 designed/partial • ⚠️ **ASPIRATIONAL** not wired.

## Capability Matrix

| # | Capability | Status | Evidence / Notes |
|---|-----------|--------|------------------|
| C1 | Engineering governance scaffold | ✅ | Constitution, ADRs, decisions, sprints, evidence, CI |
| C2 | Web simulator (Vite+React+TS) lint/build/tests | ✅ | `npm run lint` / `npm test` (18) / `npm run build` green (Sprint 7 evidence) |
| C3 | Image compression (bitmap sampling + JPEG) | ✅ | Helper `compressImageFile`; JVM-verified math mirror in `src/lib`; **device run PASS** (Sprint 8: real pipeline → smaller valid JPEG; fail-closed `null` on unusable input) |
| C4 | Video compression (FFmpegKit openh264, preset CRF/bitrate) | ✅ | `compressVideoFile` awaits session state; **on-device PASS** (instrumented test generates a real source and re-encodes it smaller + decodable via `-c:v h264`). Moved to LGPL `ffmpeg-kit-full:8.1.7` — the previous audio-only `:6.0.LTS` build had NO x264/H.264/MP4 muxer (proven on-device rc=1 `Unrecognized option preset`), so compression silently returned null. armeabi-v7a/x86 are not shipped by the full AAR (arm64-v8a + x86_64 only) |
| C5 | Foreground batch service + progress notification | ⚠️ | `BatchCompressionService` wired into Media tab (`startBatch`); **device run blocked by device storage** (see `docs/evidence/2026-09-03_batch_real_path_contract_test.md`). A real-path instrumented contract test (drives the actual `executeBatchProcessing` loop via the `executeBatchProcessingForTest` seam, asserts a queued item is held at the pause gate then completed — never dropped) is written + compiles green, but has NOT run on hardware, so C5 is not claimed PASS |
| C6 | Battery-aware pause/resume (opt-in) | ✅ | Controller + receiver; instrumented `BatchPauseContractTest` proves the pause gate never drops an item **on-device PASS** (battery pause holds worker until resumed) |
| C7 | Autosave to MediaStore (opt-in, no permission) | ✅ | `saveToPublicMediaStore` returns Boolean; instrumented test covers insert **on-device PASS** (gallery autosave) |
| C8 | DataStore settings persistence | ✅ | `SettingsRepository` flow; additive keys; fail-closed defaults |
| C9 | PDF build / merge / split / metrics | ✅ | iText 7.2.5 (on-device, no INTERNET); `createPdfFromImages` (vector pages via `AreaBreak`), `mergePdfDocuments` (temp-file + `copyPagesTo`), `splitPdfIntoPages` (kept on `android.graphics.pdf` for bitmap fallback), `readPdfMetrics` (`PdfRenderer`); **device run PASS** |
| C10 | PDF embedded-text extraction | ✅ | iText `PdfTextExtractor` + `LocationTextExtractionStrategy` (layout-aware X/Y positions; page headers between sections); temp-file + `PdfReader(File)`; honest "scan / OCR needed" when no embedded text; **device run PASS** |
| C11 | AICore device-model handoff (Gemini Nano, on-device) | 🟡 **implemented, build-verified — NOT hardware-proven** | **ADR-011**. `OnDeviceInferenceRepository` (fail-closed `Status` gate + `AiResult`) + Elite AI panel wired into MainActivity; real ML Kit GenAI `Generation/GenerativeModel` path guarded behind API 26; merged release manifest declares **no INTERNET**. 12 unit tests (5 new), lint 0 errors, release shrinks clean (Kotlin 2.2 + AGP 8.10). **A real Nano inference is NOT yet run on hardware (needs a Nano-capable device, L5)** — do not claim PASS on device. Evidence: `docs/evidence/2026-09-03_adr011_on_device_ai_surface.md` |
| C12 | Real OCR (scanned PDFs/images) | ✅ | Implemented (ADR-009): ML Kit `text-recognition` via `OcrHelper` (typed-null, no INTERNET), `AiTab` "Scan reader"; **device walkthrough PASS** (reads "SHRINKMEDI" on API 36); R8-verified |
| C13 | Signed, R8-minified release build (`com.shrinkmedia.compressor` v0.3.0) | ✅ | `app/build.gradle.kts` (minify + `signingConfigs.create("release")`); `apksigner verify` PASS on `app-release.apk` **signed with the production keystore** (`~/.android/keystores/shrinkmedia-release.jks`, gitignored `keystore.properties`); distributed via GitHub sideload |
| C14 | Android instrumentation tests **written + executed on device** (JVM + instrumented) | ✅ | JVM unit tests green; **10/10** instrumented tests PASS on API-36 (Sprint 8 + video-compression v0.6.0 evidence) — incl. the new `compressVideoFile` real-path openh264 test |
| C15 | CI hardened to Node-24 actions + signed release-AAB job | ✅ | `.github/workflows/ci.yml` (checkout/setup-node/setup-java/cache v5, upload-artifact v7, fail-closed release job) |
| C16 | Batch no-silent-drops: per-file failure surfacing + on-device audit log | ✅ | `BatchFailureAudit` + completion notification reason summary (Constitution I.6); instrumented `failure_audit_record_is_written_to_on_device_sandbox` **on-device PASS** (Article I.6) |
| C17 | Google Tools Bridge (Drive/Docs/cloud-Gemini "Connected mode") | 🟡 **designed** | **ADR-012** (accepted, architecture): opt-in, OFF-by-default "Connected mode" adds INTERNET + cloud Gemini (+ Drive/Docs OAuth later) only after deliberate run-time consent; the default build keeps NO INTERNET. Not implemented — a dedicated program. (ADR-010 staged it v2; ADR-012 is the concrete design) |
| C18 | Media gallery + quality UX overhaul (Sprint 12) | ✅ | `MediaStore` `Images`/`Video` query → "Your media library" `MediaFileCard` thumbnails (Coil, no INTERNET); vertical quality `RadioButton` `HIGH→MEDIUM→LOW`; PDF-build preview card (Open / Save to Gallery / Discard); `LocationTextExtractionStrategy` text extraction; v0.4.0; all 6 Android gates + device launch green (`docs/evidence/2026-09-01_media_gallery_quality_ux_pdf_preview.md`) |
| C19 | App rename + media delete + first-run onboarding (Sprint 13) | ✅ | Launcher label **ShrinkMedia** (`strings.xml`, service notification, web-sim parity); **Select** multi-select delete in "Your media library" — API 30+ `MediaStore.createDeleteRequest` system consent (`PendingIntent` → `StartIntentSenderForResult`), API <30 `deleteLegacy`, confirm dialog, no silent drops; additive `ONBOARDING_DISMISSED` key (default `false`) + onboarding card ("Get to know ShrinkMedia"); v0.5.0; all 6 Android gates + device launch + uiautomator text proof green (`docs/evidence/2026-09-02_app_rename_media_delete_onboarding.md`) |
| C20 | Public web presence: Vercel deploy + GitHub metadata (Sprint 14) | ✅ | Web simulator live at **https://shrinkmedia.vercel.app** (HTTP 200, title/description/favicon, SPA fallback; pure static, no network calls, ADR-006 harness — separate from the no-INTERNET native app); repo description + homepage + **20 topics**; productized README (badges, live-site link); `vercel.json` + `.vercelignore`; fail-closed CI auto-deploy `deploy-web.yml` (code-complete; runtime pending secret provision) — `docs/evidence/2026-09-02_public_web_presence.md` |

## What This Means For New Work

- **The on-device invariant is structural:** the manifest has no INTERNET
  permission. Adding network = ADR (Article II.2).
- **Compression/PDF helpers are shippable but device-untested.** The final
  verification pass (**Sprint 8**) must exercise the real paths on hardware and
  record it in `docs/evidence/`.
- **Nothing under C11 may be claimed as *hardware-delivered*.** It is now
  **implemented and build-verified** (fail-closed gate + real GenAI path, no
  INTERNET) but the actual on-device inference has **not** run on Nano hardware —
  that's L5 and requires a Nano/AICore-capable device. C12 OCR is
  implemented **and** device-verified (reads "SHRINKMEDI" on API 36;
  `docs/evidence/2026-08-31_device_verification.md`).
- **Release signing is complete.** The production keystore
  (`~/.android/keystores/shrinkmedia-release.jks`) is generated; `keystore.properties`
  references it and is gitignored (fail-closed if absent). `apksigner verify` PASS on
  the v0.3.0 `app-release.apk`. **Backup the keystore off-machine — it is the app's
  permanent identity and cannot be recovered if lost.** A verified checklist with the
  live checksum anchor now exists (`docs/runbooks/keystore-backup.md`); performing the
  off-machine copy is a human step not yet completed.

## Sprint Cross-Reference

Every claim above is audited sprint-by-sprint in
`docs/sprint-cross-reference.md`. The release gate table lives in
`docs/release-readiness.md`; the step-by-step path lives in
`docs/release-roadmap.md`.