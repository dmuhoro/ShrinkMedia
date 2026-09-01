# ShrinkMedia — Release Readiness

> Literal go/no-go table. Every row is **PASS** / **FAIL** / **BLOCKED** with a
> citation into `docs/evidence/` or a sprint record — never narrative alone
> (Constitution Article VII).

## Gate: v0.3.1 — PDF iText pipeline + UI polish (2026-09-01, Sprint 11)

| Item | Status | Evidence |
|------|--------|----------|
| `createPdfFromImages` uses iText 7 (vector pages, `AreaBreak`) | PASS | `MainActivity.kt`; `compileDebugKotlin` PASS; `docs/evidence/2026-09-01_pdf_compile_fixes_ui_polish.md` |
| `mergePdfDocuments` uses temp-file + `PdfReader(File)` + `copyPagesTo` | PASS | `MainActivity.kt`; temp files cleaned up; `docs/evidence/2026-09-01_pdf_compile_fixes_ui_polish.md` |
| `extractRawTextFromUri` uses iText `PdfTextExtractor` (honest "scan" message) | PASS | `MainActivity.kt`; temp-file pattern; `docs/evidence/2026-09-01_pdf_compile_fixes_ui_polish.md` |
| Import conflict `Image` vs `ITextImage` resolved | PASS | Alias `ITextImage` in `MainActivity.kt` imports |
| R8 proguard rule for `org.slf4j.impl.StaticLoggerBinder` | PASS | `app/proguard-rules.pro`; `assembleRelease` PASS |
| Recent files UI: expandable cards, audit detail, Share, Delete (5s undo), Clear | PASS | `RecentSection`, `AuditDetailPanel`, `ResultCard` in `MainActivity.kt` |
| SettingsSheet: Theme (System/Light/Dark), OCR Language (6), Batch toggle, Autosave, Pause-on-low-battery | PASS | `SettingsSheet` composable; DataStore additive keys |
| OCR language selection (EN/FR/DE/ES/IT/PT) persisted via DataStore | PASS | `OcrLanguage` enum + `ocr_language` key; `recognizeText` 3-arg form |
| `compileDebugKotlin` | PASS | `docs/evidence/2026-09-01_pdf_compile_fixes_ui_polish.md` |
| `testDebugUnitTest` | PASS | `docs/evidence/2026-09-01_pdf_compile_fixes_ui_polish.md` |
| `assembleDebug` | PASS | `docs/evidence/2026-09-01_pdf_compile_fixes_ui_polish.md` |
| `compileDebugAndroidTestKotlin` | PASS | `docs/evidence/2026-09-01_pdf_compile_fixes_ui_polish.md` |
| `assembleRelease` (R8 minify) | PASS | `docs/evidence/2026-09-01_pdf_compile_fixes_ui_polish.md` |
| `lintDebug` | PASS | `docs/evidence/2026-09-01_pdf_compile_fixes_ui_polish.md` |
| Device install + launch (API 36) — no crashes | PASS | `adb install` + `am start` + logcat clean |

## Gate: v0.3.0 signed release + distribution (2026-08-31, Sprint 10)

| Item | Status | Evidence |
|------|--------|----------|
| Production keystore generated (outside repo) | PASS | `~/.android/keystores/shrinkmedia-release.jks` (PKCS12, 2048-bit RSA, SHA256withRSA); `keytool` verified; `keytool -list` shows alias `shrinkmedia` |
| `keystore.properties` gitignored + fail-closed | PASS | `.gitignore` lines 25-28 (`keystore.properties`, `*.jks`, `*.keystore`); build fails closed if absent |
| Signed v0.3.0 release APK built | PASS | `./gradlew :app:assembleRelease` BUILD SUCCESSFUL; `app/build/outputs/apk/release/app-release.apk` |
| Correct version/package | PASS | `aapt dump badging` → `versionName='0.3.0'` `versionCode='3'` `applicationId='com.shrinkmedia.compressor'` label "Media Compressor" |
| `apksigner verify` PASS (production keystore) | PASS | cert SHA-256 `21569322706156fe...` matches generated keystore (`docs/evidence/2026-08-31_v0.3.0_signed_release.md`) |
| R8 minified; ML Kit + OcrHelper kept | PASS | `minifyReleaseWithR8` BUILD SUCCESSFUL; seeds/usage/mapping keep `OcrHelper` + ML Kit registrars |
| Signed APK sideload-installs + launches on API-36 | PASS | `adb install -r app-release.apk` Success + launch (`docs/evidence/2026-08-31_v0.3.0_signed_release.md`) |
| GitHub Release `v0.3.0` with attached APK | PASS | Release created from tag `v0.3.0`; signed APK attached (sideload path, not Play) |

## Gate: v0.3.0 OCR + no-silent-drops hardening (2026-08-31, Sprint 9)

| Item | Status | Evidence |
|------|--------|----------|
| **Device verification — Sprint 8 final gate PASSED (8/8 instrumented tests on API-36 hardware)** | PASS | `./gradlew :app:connectedDebugAndroidTest` → BUILD SUCCESSFUL; `docs/evidence/2026-08-31_device_verification.md` |
| Real compression on device → smaller valid JPEG | PASS | `compressImageFile_runsTheRealPipeline...` on API 36 (`docs/evidence/2026-08-31_device_verification.md`) |
| Fail-closed null on device (never a bogus file) | PASS | `compressImageFile_returnsNull...` on API 36 (`docs/evidence/2026-08-31_device_verification.md`) |
| Gallery autosave on device | PASS | `saveToPublicMediaStore_insertsIntoPublicGallery` on API 36 (`docs/evidence/2026-08-31_device_verification.md`) |
| Battery-pause gate on device: queue never drops an item | PASS | `paused_gate_holds_the_worker_until_resumed` on API 36 (`docs/evidence/2026-08-31_device_verification.md`) |
| On-device DataStore savings monotonic + negative clamp | PASS | `recordCompressionSavings_*` on API 36 (`docs/evidence/2026-08-31_device_verification.md`) |
| On-device audit record for batch failures (Article I.6) | PASS | `failure_audit_record_is_written_to_on_device_sandbox` on API 36 (`docs/evidence/2026-08-31_device_verification.md`) |
| On-device OCR implemented (ML Kit, no INTERNET) | PASS | ADR-009; `OcrHelper.kt`; `AiTab` Scan-reader card; R8 seeds/usage keep ML Kit (`app/build/outputs/mapping/release/*.txt`) |
| **On-device OCR device walkthrough** (real `OcrHelper`, API 36) | PASS | `OcrInstrumentedTest.recognizeText_readsLargeHighContrastText_onDevice` reads "SHRINKMEDI" from "ShrinkMedia"; `docs/evidence/2026-08-31_device_verification.md` |
| OCR compile + unit test + assembleDebug | PASS | `compileDebugKotlin`, `testDebugUnitTest`, `assembleDebug` exit 0 (this session, commit `36bcfab`) |
| OCR minification (release R8) keeps ML Kit + OcrHelper | PASS | `minifyReleaseWithR8` BUILD SUCCESSFUL; seeds.txt (mlkit registrar/provider/dynamite) + usage.txt (OcrHelper) |
| **OCR bounds-decode production bug fixed** (device-found) | PASS | Commit `3b5c134` (`decodeBounded` no longer returns null on bounds-only decode) + device re-verify |
| Web-sim `GRADLE_CODE` reflects ML Kit dep (parity) | PASS | `src/App.tsx` snippet edited + committed `85f1e9e`; `npm run lint`/`test`(18)/`build` green |
| Batch no-silent-drops: per-file failure surfacing + on-device audit log | PASS (code) | `BatchFailureAudit` + completion-notification reason summary; commit `9b26f57` |
| No-silent-drops: compile + unit + assembleDebug | PASS | `compileDebugKotlin`, `compileDebugAndroidTestKotlin`, `testDebugUnitTest`, `assembleDebug` all exit 0 |
| No-silent-drops: release R8 minify | PASS | `minifyReleaseWithR8` BUILD SUCCESSFUL (this session) |
| Manifest still declares no INTERNET permission | PASS | `rg INTERNET AndroidManifest.xml` no match (see v0.2.1 gate) |

## Gate: v0.2.1 → release hardening (2026-08-31, Sprint 7)

| Item | Status | Evidence |
|------|--------|----------|
| AI Studio / Gemini fingerprints removed repo-wide | PASS | `rg` returns no matches for `google ai studio\|aistudio\|gemini\|APP_URL\|GEMINI_API_KEY` on tracked files |
| No AI Studio deps (`@google/genai`, `dotenv`, `express`) | PASS | `package.json`, `package-lock.json` |
| Single sprint folder `docs/sprints/` ordered 1→8 by execution | PASS | `docs/sprints/` contents (7 = executed record, 8 = final device gate) |
| Web simulator: `npm run lint` | PASS | `docs/evidence/2026-08-31_release-config_test-hardening.md` |
| Web simulator: `npm test` (18 tests) | PASS | `docs/evidence/2026-08-31_release-config_test-hardening.md` |
| Web simulator: `npm run build` | PASS | `docs/evidence/2026-08-31_release-config_test-hardening.md` |
| Android: JVM unit tests (`testDebugUnitTest`) green | PASS | `docs/evidence/2026-08-31_release-config_test-hardening.md` |
| Android: instrumented test APK compiles (`assembleDebugAndroidTest`) | PASS | `docs/evidence/2026-08-31_release-config_test-hardening.md` (execution = Sprint 8) |
| Android: `assembleDebug` + `assembleRelease` green | PASS | `docs/evidence/2026-08-31_release-config_test-hardening.md` |
| Android: R8-minified signed `release` APK (apksigner verify) | PASS | `docs/evidence/2026-08-31_release-config_test-hardening.md` (dev keystore) |
| Android: signed `bundleRelease` AAB | PASS | `docs/evidence/2026-08-31_release-config_test-hardening.md` (SHRINKME.RSA) |
| Manifest declares no INTERNET permission | PASS | `docs/evidence/2026-08-30_android_config_check.md`; `rg INTERNET AndroidManifest.xml` no match |
| Release-keystore artifacts gitignored and untracked | PASS | `.gitignore` (`keystore.properties`, `*.jks`, `*.keystore`); `git ls-files` shows none |
| CHANGELOG + sprint records updated | PASS | `CHANGELOG.md`; `docs/sprints/sprint-7-*`, `docs/sprints/sprint-8-*` |

## Gate: v0.2.1 (2026-08-31)

| Item | Status | Evidence |
|------|--------|----------|
| AI Studio / Gemini fingerprints removed repo-wide | PASS | `rg` returns no matches for `google ai studio\|aistudio\|gemini\|APP_URL\|GEMINI_API_KEY` on tracked files |
| No AI Studio deps (`@google/genai`, `dotenv`, `express`) | PASS | `package.json`, `package-lock.json` |
| Single sprint folder `docs/sprints/` ordered 1→7 | PASS | `docs/sprints/` contents; root `sprints/` removed |
| Web simulator: `npm run lint` | PASS | `docs/evidence/2026-08-31_sprint6_cleanup.md` |
| Web simulator: `npm test` (11 tests) | PASS | `docs/evidence/2026-08-31_sprint6_cleanup.md` |
| Web simulator: `npm run build` | PASS | `docs/evidence/2026-08-31_sprint6_cleanup.md` |
| CHANGELOG + sprint records updated | PASS | `CHANGELOG.md` v0.2.1; `docs/sprints/sprint-6-*`, `docs/sprints/sprint-7-*` |

## Gate: v0.2.0 (2026-08-30)

| Item | Status | Evidence |
|------|--------|----------|
| Web simulator: `npm run lint` | PASS | `docs/evidence/2026-08-30_web_lint_test_build.md` |
| Web simulator: `npm test` (11 tests) | PASS | `docs/evidence/2026-08-30_web_lint_test_build.md` |
| Web simulator: `npm run build` | PASS | `docs/evidence/2026-08-30_web_lint_test_build.md` |
| Android: `assembleDebug` green | PASS | `docs/evidence/2026-08-30_android_config_check.md` (`app-debug.apk` built) |
| Android: no INTERNET permission in manifest | PASS | `docs/evidence/2026-08-30_android_config_check.md` — `rg INTERNET AndroidManifest.xml` returns no match; APK badging lists only foreground-service/notification permissions |
| Governance scaffold complete | PASS | `docs/evidence/2026-08-30_governance_scaffold.md` |
| CHANGELOG + sprint records updated | PASS | `CHANGELOG.md` v0.2.0; `docs/sprints/sprint-5-*`, `docs/sprints/sprint-6-*`, `docs/sprint-cross-reference.md` |

## History / Prior Releases

| Version | Date | Outcome |
|---------|------|---------|
| 0.1.0 | 2026-08-30 | Prototype baseline (Android app + web simulator scaffold) — informal, no gate table kept |

## Blockers

1. ~~**Android device/SDK verification**~~ — unblocked on 2026-08-30: wrapper
   added, SDK 35 + build-tools 35.0.0 installed, `assembleDebug` green
   (`docs/evidence/2026-08-30_android_config_check.md`).
2. **Device runtime verification (Sprint 8 gate) — CLEARED 2026-08-31:** the
   real-path instrumented suite now runs green (8/8) on API-36 hardware
   (`docs/evidence/2026-08-31_device_verification.md`). Battery-pause (never
   drops), autosave, fail-closed-null, savings, and the audit record are all
   verified on device. The MIUI "Install via USB" toggle was the only install
   impediment; it is now enabled.
3. **Production keystore (human-owned):** release signing is configured and
   locally proven with a throwaway dev keystore, but a real keystore must be
   provisioned (gitignored `keystore.properties` + CI `STORE_*`/`KEY_*` secrets)
   to build a distributable signed AAB. Fail-closed by design — no unsigned
   artifact is produced without it.

## Definition of Release

- [ ] All PASS rows above hold with citations.
- [ ] No FAIL rows without a documented descope decision (`docs/decisions.md`).
- [ ] `CHANGELOG.md` and active sprint plan(s) reflect the release.
- [ ] Sprint 8 device-verification gate rows flip from `⚠️`/`BLOCKED` to PASS.
- [ ] OCR + no-silent-drops gate (above): code rows hold with citations; the two
      "device run pending" instrumented rows flip to PASS once the MIUI install
      gate is lifted and the suite runs on hardware.