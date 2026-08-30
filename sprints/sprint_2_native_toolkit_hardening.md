# Sprint 2: Native Toolkit Hardening & Device Verification

**Status:** Planned
**Version Target:** v0.2.x
**Focus:** Prove the native engine on hardware; close the release gates.

---

## Sprint Objectives
1. **Gradle wrapper + SDK fleet**: install `platforms;android-35` and
   `build-tools` in the build environment; run `./gradlew assembleDebug` and
   record the result (unblocks the release-readiness row).
2. **Instrumentation tests on a device**: add `androidTest` coverage that
   exercises the **real** `compressImageFile` / `compressVideoFile` /
   `saveToPublicMediaStore` paths (Constitution Article I.4 — proof must
   exercise the real path).
3. **Battery-pause device walkthrough**: runbook §4 — start a batch, enter
   battery-saver/low-battery, assert the queue pauses and resumes with zero
   dropped items; record evidence.
4. **Autosave verification**: opt-in toggle → output under
   `Pictures/ShrinkMedia` / `Movies/ShrinkMedia`; failures surface, never
   silent (Article I.6).
5. **Docs parity**: update `docs/current-state.md` (drop `⚠️
   implemented, device run pending` rows to ✅ with citations) and re-run the
   cross-reference audit.

---

## Key Files Created / Modified
- `app/src/androidTest/...` — instrumented tests (new)
- `app/src/test/...` — JVM unit tests (new, where feasible)
- `gradle/wrapper/*`, `local.properties` (gitignored)
- `docs/evidence/2026-08-XX_device_*.md` (new evidence logs)
- `docs/current-state.md`, `docs/release-readiness.md`,
  `docs/sprint-cross-reference.md`

---

## Validation & Verification Checklist
- [ ] `./gradlew assembleDebug` green with evidence.
- [ ] Instrumentation suite green on an emulator/device (API 24–35).
- [ ] Battery-pause walkthrough recorded: queue paused → resumed → 0 dropped.
- [ ] Autosave opt-in verified in gallery; failure path surfaced.
- [ ] `docs/current-state.md` F-status promoted to ✅ with citations only.
- [ ] Release-readiness blockers closed or descoped in `docs/decisions.md`.
- [ ] All commits SSH-signed.