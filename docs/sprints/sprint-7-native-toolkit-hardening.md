# Sprint 7 — Native Toolkit Hardening & Device Verification (PLAN)

**Status:** PLANNED (active plan — not yet executed)
**Version Target:** v0.2.x
**Focus:** Prove the native engine on hardware; close the remaining release
gates.

> Executed history lives in `docs/sprints/sprint-1..6.md`. This file is the
> **active plan** for the next sprint of work (ROADMAP Phase 2).

## Sprint Objectives
1. **Gradle wrapper + SDK fleet**: install `platforms;android-35` and
   `build-tools` in the build environment; run `./gradlew assembleDebug` and
   record the result.
2. **Instrumentation tests on a device**: add `androidTest` coverage that
   exercises the **real** `compressImageFile` / `compressVideoFile` /
   `saveToPublicMediaStore` paths (Constitution Article I.4 — proof must
   exercise the real path).
3. **Battery-pause device walkthrough**: runbook android-build §4 — start a
   batch, enter battery-saver/low-battery, assert the queue pauses and resumes
   with zero dropped items; record evidence.
4. **Autosave verification**: opt-in toggle → output under
   `Pictures/ShrinkMedia` / `Movies/ShrinkMedia`; failures surface, never
   silent (Article I.6).
5. **Docs parity**: update `docs/current-state.md` (drop `⚠️
   implemented, device run pending` rows to ✅ with citations) and re-run the
   cross-reference audit.

## Key Files Created / Modified
- `app/src/androidTest/...` — instrumented tests (new)
- `app/src/test/...` — JVM unit tests (new, where feasible)
- `gradle/wrapper/*`, `local.properties` (gitignored)
- `docs/evidence/2026-XX-XX_device_*.md` (new evidence logs)
- `docs/current-state.md`, `docs/release-readiness.md`,
  `docs/sprint-cross-reference.md`

## Validation & Verification Checklist
- [ ] `./gradlew assembleDebug` green with evidence.
- [ ] Instrumentation suite green on an emulator/device (API 24–35).
- [ ] Battery-pause walkthrough recorded: queue paused → resumed → 0 dropped.
- [ ] Autosave opt-in verified in gallery; failure path surfaced.
- [ ] `docs/current-state.md` F/⚠️ statuses promoted to ✅ with citations only.
- [ ] Release-readiness blockers closed or descoped in `docs/decisions.md`.
- [ ] All commits SSH-signed.

## Cross-Reference

ROADMAP Phase 2 (Test Hardening & AICore handoff);
`docs/release-readiness.md`; `docs/current-state.md` C3–C10; Constitution
Articles I & III.
