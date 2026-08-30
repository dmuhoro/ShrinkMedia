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

## Gate v0.2.0

| Item | Status |
|---|---|
| Web lint / test / build | PENDING → green in sprint 5 (evidence cites) |
| Android `assembleDebug` | BLOCKED (SDK 35 install) |
| No INTERNET permission in manifest | PASS (verified) |
| Governance scaffold | PENDING → complete in sprint 5 |
| CHANGELOG + sprint records | PENDING → updated in sprint 5 |

## Blockers
1. Android SDK `platforms;android-35` + `build-tools` not installed from this
   environment; Gradle wrapper added in sprint 5.

## Definition of Release
- All PASS rows cited to `docs/evidence/`.
- No silent FAIL rows without a documented descope.
- `CHANGELOG.md` + active sprint updated.