# ShrinkMedia — Release Readiness

> Literal go/no-go table. Every row is **PASS** / **FAIL** / **BLOCKED** with a
> citation into `docs/evidence/` or a sprint record — never narrative alone
> (Constitution Article VII).

## Gate: v0.2.0 (2026-08-30)

| Item | Status | Evidence |
|------|--------|----------|
| Web simulator: `npm run lint` | PASS | `docs/evidence/2026-08-30_web_lint_test_build.md` |
| Web simulator: `npm test` (11 tests) | PASS | `docs/evidence/2026-08-30_web_lint_test_build.md` |
| Web simulator: `npm run build` | PASS | `docs/evidence/2026-08-30_web_lint_test_build.md` |
| Android: `assembleDebug` green | PASS | `docs/evidence/2026-08-30_android_config_check.md` (`app-debug.apk` built) |
| Android: no INTERNET permission in manifest | PASS | `docs/evidence/2026-08-30_android_config_check.md` — `rg INTERNET AndroidManifest.xml` returns no match; APK badging lists only foreground-service/notification permissions |
| Governance scaffold complete | PASS | `docs/evidence/2026-08-30_governance_scaffold.md` |
| CHANGELOG + sprint records updated | PASS | `CHANGELOG.md` v0.2.0; `docs/sprints/sprint-5-*`, `sprints/sprint_1_*`, `docs/sprint-cross-reference.md` |

## History / Prior Releases

| Version | Date | Outcome |
|---------|------|---------|
| 0.1.0 | 2026-08-30 | Prototype baseline (Android app + web simulator scaffold) — informal, no gate table kept |

## Blockers

1. ~~**Android device/SDK verification**~~ — unblocked on 2026-08-30: wrapper
   added, SDK 35 + build-tools 35.0.0 installed, `assembleDebug` green
   (`docs/evidence/2026-08-30_android_config_check.md`). Runtime on-device
   verification (presses, MediaStore round-trip) is not covered by any gate
   and remains a manual QA step before a store release.

## Definition of Release

- [ ] All PASS rows above hold with citations.
- [ ] No FAIL rows without a documented descope decision (`docs/decisions.md`).
- [ ] `CHANGELOG.md` and active sprint plan(s) reflect the release.