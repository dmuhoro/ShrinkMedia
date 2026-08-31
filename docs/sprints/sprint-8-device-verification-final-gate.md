# Sprint 8 — Device Verification: Final Release Gate (PLAN)

**Status:** PLANNED (active plan — not yet executed)
**Version Target:** v0.2.x
**Focus:** Prove the native engine on real hardware as the **final** release
gate — after all code, CI, release-configuration, and doc plumbing are done.

> Executed history lives in `docs/sprints/sprint-1..7.md`. This file is the
> **active plan** for the final sprint of work (ROADMAP Phase 2 verification).
> It was re-sequenced from the original Sprint 7 plan so that hardware
> verification runs **last** — nothing is claimed shipped until the real paths
> are exercised on a device (Constitution Article I.4, VII).

## Sprint Objectives
1. **Gradle wrapper + SDK fleet**: install `platforms;android-35` and
   `build-tools` in the build environment; run `./gradlew assembleDebug` and
   record the result.
2. **Instrumentation tests on a device**: run the real-path `androidTest`
   coverage that exercises the actual `compressImageFile` / `compressVideoFile`
   / `saveToPublicMediaStore` paths (Constitution Article I.4 — proof must
   exercise the real path). The suite is already written and compiles
   (`app/src/androidTest/java/com/shrinkmedia/compressor/`); this sprint runs
   it on an emulator/device and records PASS/FAIL per test.
3. **Battery-pause device walkthrough**: runbook android-build §4 — start a
   batch, enter battery-saver/low-battery, assert the queue pauses and resumes
   with zero dropped items; record evidence.
4. **Autosave verification**: opt-in toggle → output under
   `Pictures/ShrinkMedia` / `Movies/ShrinkMedia`; failures surface, never
   silent (Article I.6).
5. **Docs parity**: promote `docs/current-state.md` `⚠️ implemented, device run
   pending` rows to ✅ **with citations** to this sprint's evidence; re-run the
   cross-reference audit and mark the release gate truly PASS for the covered
   rows.

## Key Files Created / Modified
- `app/src/androidTest/java/com/shrinkmedia/compressor/` — instrumented tests
  (already committed; executed here on hardware)
- `gradle/wrapper/*`, `local.properties` (gitignored)
- `docs/evidence/2026-XX-XX_device_*.md` (new evidence logs)
- `docs/current-state.md`, `docs/release-readiness.md`,
  `docs/sprint-cross-reference.md`

## Validation & Verification Checklist
- [ ] `./gradlew assembleDebug` green with evidence.
- [ ] Instrumentation suite green on an emulator/device (API 24–35).
- [ ] Battery-pause walkthrough recorded: queue paused → resumed → 0 dropped.
- [ ] Autosave opt-in verified in gallery; failure path surfaced.
- [ ] `docs/current-state.md` `⚠️`/`🟡` statuses promoted to ✅ with citations only.
- [ ] Release-readiness blockers closed or descoped in `docs/decisions.md`.
- [ ] All commits SSH-signed.

## Cross-Reference

ROADMAP Phase 2 (Test Hardening & AICore handoff) — verification half;
`docs/release-roadmap.md`; `docs/release-readiness.md`;
`docs/current-state.md` C3–C10; Constitution Articles I & III.
