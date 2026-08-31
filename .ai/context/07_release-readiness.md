# 07 — Release Readiness

## Purpose
The release gate checklist and open blockers.

## Authority Level
Foundational — nothing ships without PASS rows.

## Consumers
Auditor agent, release work.

## Source Documents
`docs/release-readiness.md` (canonical).

## Update Rules
Mirror the table after every sprint close.

---

## Gate v0.2.1

| Item | Status |
|---|---|
| AI Studio / Gemini fingerprints removed | PASS — `rg` on tracked files returns no matches |
| Single sprint folder `docs/sprints/` (records 1–7 + active plan 8 final gate) | PASS |
| Web lint / test / build still green | PASS — `docs/evidence/2026-08-31_sprint6_cleanup.md` and `docs/evidence/2026-08-31_release-config_test-hardening.md` |
| Signed R8-minified release build + signed AAB path | PASS — `docs/evidence/2026-08-31_release-config_test-hardening.md` (dev keystore; production keystore human-owned) |

## Gate v0.2.0

| Item | Status |
|---|---|
| Web lint / test / build | PASS — `docs/evidence/2026-08-30_web_lint_test_build.md` |
| Android `assembleDebug` | PASS — `docs/evidence/2026-08-30_android_config_check.md` (SDK 35, `app-debug.apk`; FFmpegKit coordinate fixed) |
| No INTERNET permission in manifest | PASS (verified; APK badging lists only foreground-service/notification) |
| Governance scaffold | PASS — `docs/evidence/2026-08-30_governance_scaffold.md` |
| CHANGELOG + sprint records | PASS — updated in `docs/release-readiness.md`, sprint records, cross-reference |

## Blockers
- **Device/emulator runtime verification** (final Sprint 8 gate): real-path
  instrumented suite compiles but has not run on hardware; battery-pause and
  autosave walkthroughs not yet recorded.
- **Production keystore (human-owned):** release signing is configured and
  locally proven (dev keystore), but a real keystore + CI secrets must be
  provisioned to build a distributable signed AAB. Fail-closed by design.

## Definition of Release
- All PASS rows cited to `docs/evidence/`.
- No silent FAIL rows without a documented descope.
- `CHANGELOG.md` + active sprint updated.