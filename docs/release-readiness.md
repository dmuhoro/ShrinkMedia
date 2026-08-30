# ShrinkMedia — Release Readiness

> Literal go/no-go table. Every row is **PASS** / **FAIL** / **BLOCKED** with a
> citation into `docs/evidence/` or a sprint record — never narrative alone
> (Constitution Article VII).

## Gate: v0.2.0 (2026-08-30)

| Item | Status | Evidence |
|------|--------|----------|
| Web simulator: `npm run lint` | PENDING | — |
| Web simulator: `npm test` | PENDING | — |
| Web simulator: `npm run build` | PENDING | — |
| Android: `assembleDebug` green | BLOCKED | Requires local SDK 35 + Gradle wrapper (recorded in `2026-08-30_android_config_check.md`) |
| Android: no INTERNET permission in manifest | PASS | `AndroidManifest.xml` — verified via grep in governance scaffold evidence |
| Governance scaffold complete | PENDING | — |
| CHANGELOG + sprint records updated | PENDING | — |

## History / Prior Releases

| Version | Date | Outcome |
|---------|------|---------|
| 0.1.0 | 2026-08-30 | Prototype baseline (Android app + web simulator scaffold) — informal, no gate table kept |

## Blockers

1. **Android device/SDK verification** — no `platforms/android-35` +
   `build-tools` installed in the current environment, and no Gradle wrapper yet.
   Unblocking step: add the wrapper, install SDK packages, run
   `./gradlew assembleDebug test`, record the result.

## Definition of Release

- [ ] All PASS rows above hold with citations.
- [ ] No FAIL rows without a documented descope decision (`docs/decisions.md`).
- [ ] `CHANGELOG.md` and active sprint plan(s) reflect the release.