# ShrinkMedia Engineering Constitution

> The non-negotiable operating rules for every task in the ShrinkMedia
> repository. This document is the **highest-authority engineering governance**.
> It overrides convenience, speed, and scope-creep in favour of correctness.
> Conflicts with any other doc resolve here.

---

## Article I — Execution-Safety Guarantees

1. **Never let code claim a protection it does not actually provide.** False confidence is worse
   than no protection. If an "evidence" gate, grep, or test can pass while the real production path
   stays unguarded, the work is not done.
2. **Enforcement goes at the real boundary.** Before wiring any guard into the media/PDF path, read
   the actual code and find the true entry point (e.g. where `compressImageFile`, `compressVideoFile`
   or `BatchCompressionService.executeBatchProcessing` runs in the live flow). Insert protection
   there, not in a shared helper only a demo path uses.
3. **Fail closed, never fail open.** Defaults must refuse, not silently allow. When unsure, choose the
   conservative quality or bitrate cap.
4. **Proof must exercise the real path.** A unit test of a standalone helper does not prove wiring.
   Tests must assert that the actual production function is invoked (or refused) on the real path.
5. **Say no early, loudly.** If a plan has a flaw (wrong insertion point, false gate, scope that
   contradicts its own constraints), say so explicitly and propose the corrected version before
   executing.
6. **No silent drops.** A file that fails to compress, or fails to autosave, is surfaced explicitly:
   the caller gets a clear reason, progress is reported, and an audit record (reduction, duration,
   status) is produced or updated.

---

## Article II — On-Device Privacy Invariant

1. **All media, PDF, and text processing runs on-device.** No user file is uploaded to any remote
   service by any code in this repository.
2. The Android manifest declares **no INTERNET permission**. Adding network access changes the
   fundamental privacy contract and therefore requires an **ADR** — it is never a drive-by change.
3. User media is read via scoped-storage URIs with user-granted access only; the app holds no
   global storage permission.
4. Derived artifacts are written to the app-private cache by default. Writes to public MediaStore
   folders happen only through the explicit autosave opt-in (default `false`).
5. Extracted PDF text and audit logs are private by default. No PII or filenames may be written to
   logs outside the user-visible audit surface.

---

## Article III — Compression & File Integrity

1. Every conversion path (`compressImageFile`, `compressVideoFile`, PDF builders) returns a typed
   result or `null` for failure. Callers handle and surface `null` explicitly — never swallow it
   silently.
2. A compressed output is only accepted when it exists on disk and has non-zero length.
3. FFmpegKit video sessions are **awaited** via the returned session's state
   (`CREATE`/`RUNNING` → terminal). Never fire-and-forget an encode.
4. `BatchCompressionPauseController.isPaused` is the single source of truth for pause/resume. A
   queued batch item is **never dropped** — it waits until `isPaused` resolves to `false`.
5. The pause-on-low-battery receiver is registered only when the user setting is enabled (default
   `false`), and unregistered on service destroy.

---

## Article IV — Settings Persistence Integrity

1. All persisted settings live in `SettingsDataStore.kt` behind `SettingsRepository`. No other
   persistence store for app settings.
2. New settings are **additive**: a new `PreferencesKey` plus a safe default, never a migration of
   existing keys while a released build may still read them.
3. Booleans default to `false` (fail closed). Quality presets default to `MEDIUM`.
4. Corrupt/absent preference files degrade to `emptyPreferences` on `IOException` — the app boot
   never hard-fails on storage corruption.

---

## Article V — Testing

1. The test suite must pass (all green) before any push. `npm test` for web; Gradle `test` for
   Android.
2. Production code ships with tests for: the compression math helpers, size formatting, pause/resume
   controller behaviour, and (as instrumentation becomes available) the real on-device paths.
3. A test that asserts current behaviour as "proof a bug exists" is a diagnostic artifact. Either
   fix the bug and update the test, or mark `xfail`/`skip` with a linked issue.
4. Never weaken an existing assertion to make a build pass.
5. A capability is not "done" until its code, its tests, and its docs all land together.

---

## Article VI — Process

1. Work sequentially in layers. Finish one layer (code + tests + docs) before starting the next.
2. Every task is committed individually with an explicit message stating what changed and which doc
   (if any) needs a corresponding update.
3. All commits are SSH-signed (`commit.gpgsign true`, `gpg.format ssh`). A missing signature is an
   invalid commit.
4. New dependencies require justification against existing ones in `package.json` or
   `app/build.gradle.kts`. Name the specific gap.
5. Read `docs/current-state.md` and `docs/architecture.md` before starting a task. If a task assumes
   a capability marked **ASPIRATIONAL**, stop and say so (AGENTS.md §7).
6. Feature freeze: no new subsystem may be started while any P0 from
   `docs/release-readiness.md` is open.
7. Honesty over optimism: report gaps accurately. Closing one gap never means "risk is complete".

---

## Article VII — Evidence

1. Every verification produces an evidence file under `docs/evidence/` (or a referenced log) citing
   date, exact command/step, and observed result.
2. A "PASS" in release-readiness requires a cited test or a cited manual verification step — never
   narrative alone.
3. Live/device verification (real compression on hardware, autosave to MediaStore) is recorded as an
   evidence log with the date, the exact step, and the observed result.
4. Evidence is committed with the work that produced it, not bolted on at release time.

---

## Article VIII — Pareto Execution

1. When closing a gap, identify the 20% of work that produces 80% of the value and execute that
   first.
2. Value ordering for toolkit-readiness: on-device invariant → compression/file integrity →
   pause/resume correctness → core workflows → polish.
3. Descoped items are descoped deliberately and documented (see `docs/decisions.md`). Do not
   silently re-include them; re-include only by an explicit decision.

---

*Constitution effective as of the engineering-foundation sprint. Any amendment is an edit to this
document accompanied by a decision entry in `docs/decisions.md`.*