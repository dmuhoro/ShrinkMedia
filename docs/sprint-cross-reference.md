# Sprint Cross-Reference

> Audit: every sprint's claims vs actual code/test evidence. Built to catch
> narrative that outruns reality (Constitution Article I.1, VI.7). Updated as
> sprints close.

| Sprint | Claimed capability | Real evidence | Verdict |
|--------|---------------------|---------------|---------|
| 1 — Initialisation | Gradle/AGP scaffold + manifest | `build.gradle.kts`, `settings.gradle.kts`, `app/build.gradle.kts`, `AndroidManifest.xml` at HEAD | ✅ Code exists |
| 1 — Initialisation | "Runs in Android Studio" | Informal only; no CI/device build recorded | ⚠️ Unverified on hardware |
| 2 — Web bootstrap | Vite+TS build | `npm run build` green (Sprint 5) | ✅ Verified |
| 3 — Foreground service | Service + notifications | `BatchCompressionService.kt` at HEAD | ✅ Code exists |
| 3 — Foreground service | Notification UX on device | No emulator/device run recorded | ⚠️ Unverified on hardware |
| 4 — Toolkit consolidation | 3-tab UI + ViewModel | `MainActivity.kt` at HEAD (187 lines, `UiState`) | ✅ Code exists |
| 4 — Battery-aware pause | `isPaused` controller + receiver | `BatchCompressionPauseController`, `ACTION_BATTERY_LOW` at HEAD | ✅ Code exists |
| 4 — Battery-aware pause | "No queue item dropped" | `executeBatchProcessing` awaits `isPaused.first { !it }` | ✅ Code enforces; device walkthrough pending |
| 4 — Live autosave | Per-file DataStore read | `settingsRepo.userSettingsFlow.first()` inside loop | ✅ Code exists |
| 5 — Governance | Constitution/ADRs/sprints/evidence/CI | Files at HEAD | ✅ Code exists |
| 5 — Web gates green | lint/test/build | `docs/evidence/2026-08-30_web_lint_test_build.md` | ✅ Cited |
| 5 — Android gate | `assembleDebug` | `docs/evidence/2026-08-30_android_config_check.md` — `app-debug.apk` built (SDK 35, 4 ABIs, no INTERNET permission); FFmpegKit dependency fixed to a published artifact | ✅ Cited |
| 6 — AI Studio cleanup | No Gemini/`@google/genai`/`metadata.json`/`.env.example` references | `rg` whole-repo returns no matches; `package.json`/`package-lock.json` pruned; web gates green (`docs/evidence/2026-08-31_sprint6_cleanup.md`) | ✅ Cited |
| 6 — Sprint consolidation | One `docs/sprints/` folder, records 1–6 + active plan 7 | `docs/sprints/` contents at HEAD; root `sprints/` removed | ✅ Code exists |
| 7 — Test hardening & release config | Pure web batch/savings model (no fake numbers) | `src/lib/batch.ts`, `src/lib/batch.test.ts`; `npm test` 18 pass | ✅ Verified |
| 7 — Test hardening & release config | Android JVM unit + instrumented tests written | `CompressionQualityUnitTest` runs green; `app/src/androidTest/...` compiles (`assembleDebugAndroidTest` SUCCESS) | ⚠️ Compiles; device run deferred to Sprint 8 |
| 7 — Test hardening & release config | `BatchCompressionService.startBatch` wired into Media tab + savings surfaced | `ToolkitViewModel.startBatch`, `src/App.tsx` batch handler | ✅ Code exists + web tests |
| 7 — Test hardening & release config | Production namespace `com.shrinkmedia.compressor` | `app/build.gradle.kts`, APK badging | ✅ Verified |
| 7 — Test hardening & release config | R8-minified signed release APK/AAB | `apksigner verify` PASS (`app-release.apk`); `bundleRelease` → signed AAB (dev keystore); R8 seeds retain FFmpegKit + `CompressionQuality` | ✅ Verified (dev keystore) |
| 7 — Test hardening & release config | Launcher icon + label, v0.2.1 | `res/mipmap-*`/`drawable`/`values/colors.xml`; manifest; aapt badging label | ✅ Verified |
| 7 — Test hardening & release config | CI on Node-24 actions + signed release-AAB job | `.github/workflows/ci.yml` (checkout/setup-node/setup-java/cache v5, upload-artifact v7); release job fail-closed on secrets | ✅ Verified (YAML parses, build path validated) |
| 8 — Device verification (PLAN) | Real-path instrumented suite on a device; battery/autosave walkthroughs | Not yet executed — final release gate | ⚠️ Planned |
| 9 — OCR + no-silent-drops | On-device OCR (ML Kit) + `BatchFailureAudit` | `OcrHelper.kt`, `BatchFailureAudit.kt`, `AiTab` Scan reader; `connectedDebugAndroidTest` 9/9 PASS (`docs/evidence/2026-08-31_device_verification.md`) | ✅ Verified on hardware |
| 10 — v0.3.0 signed release | Production keystore + signed APK + GitHub Release | `keystore.properties` + `app-release.apk` (`apksigner verify` PASS); Release `v0.3.0` attached | ✅ Verified |
| 11 — PDF compile fixes + UI polish | iText 7 PDF engine + recent/settings/theme + R8 proguard fix | `MainActivity.kt` (iText), `SettingsDataStore.kt`, `OcrHelper.kt`, `proguard-rules.pro`; all 6 Android gates green (`docs/evidence/2026-09-01_pdf_compile_fixes_ui_polish.md`) | ✅ Verified |
| 12 — Media gallery + quality UX + PDF preview + text fidelity | v0.4.0: MediaStore media library (`MediaFile`/`MediaFileCard`, Coil), vertical `HIGH→MEDIUM→LOW` quality radios, PDF-build preview (Open/Save/Discard), `LocationTextExtractionStrategy` extraction | `MainActivity.kt` + `app/build.gradle.kts` (v0.4.0); all 6 Android gates + device launch green (`docs/evidence/2026-09-01_media_gallery_quality_ux_pdf_preview.md`) | ✅ Verified on hardware |
| 13 — App rename + media delete + first-run onboarding | v0.5.0: launcher label **ShrinkMedia**; **Select** multi-select delete (`MediaStore.createDeleteRequest` consent on API 30+, `deleteLegacy` below, confirm dialog, no silent drops); onboarding card + additive `ONBOARDING_DISMISSED` (default `false`); liveliness radar I002 | `strings.xml` + `BatchCompressionService.kt` + `MainActivity.kt` + `SettingsDataStore.kt` + web-sim rename; all 6 Android gates + device launch + uiautomator text proof (`docs/evidence/2026-09-02_app_rename_media_delete_onboarding.md`). ⚠️ Device denies `INJECT_EVENTS`, so the delete consent **tap** is a recorded manual step, not an adb proof | ✅ Render/persist/compile verified; consent tap ≡ manual |
| 14 — *(next)* | | | |

## Rules That Keep This Honest

1. A row claims a **capability**, the right column cites **code or a test
   result**, and the verdict is ✅ only when the citation is real.
2. "Code exists" and "verified on hardware" are different verdicts — the table
   never conflates them.
3. Any sprint whose claims outgrow this table gets flagged, not silently
   rewritten.