# ShrinkMedia — Release Readiness

> Literal go/no-go table. Every row is **PASS** / **FAIL** / **BLOCKED** with a
> citation into `docs/evidence/` or a sprint record — never narrative alone
> (Constitution Article VII).

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
2. **Device/emulator runtime verification (final Sprint 8 gate):** the
   real-path instrumented suite is written and compiles, but has not run on
   hardware. Battery-pause and autosave walkthroughs are not yet recorded.
   This is the last step before any store release — nothing is claimed
   "shipped on device" until these rows flip to PASS with citations.
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