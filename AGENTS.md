# AGENTS.md — ShrinkMedia Engineering Conventions

Rules every agent (including Opencode) must follow on every task in this repo.
This file binds to the ShrinkMedia Engineering Constitution
(`docs/engineering/CONSTITUTION.md`), which is the highest-authority governance
document. In a conflict, the Constitution wins.

---

## 1. Execution-Safety Guarantees (fail-closed, no false confidence)

1. **Never let code claim a protection it does not actually provide.** A unit test of a helper is
   not proof the real compression/autosave path works. Cite the actual production function.
2. **Enforcement goes at the real boundary.** Before wiring any guard into the media path, read the
   actual code and find the true entry point (e.g. where `compressImageFile` / `compressVideoFile`
   / `BatchCompressionService.executeBatchProcessing` runs). Insert protection there.
3. **Fail closed, never fail open.** Defaults must refuse, not silently allow. When unsure, choose
   the conservative quality/bitrate cap.
4. **No silent drops.** A file that fails to compress or fails to autosave is surfaced explicitly:
   the caller gets the reason, an audit record is produced, and progress is reported.
5. **Say no early, loudly.** If a plan has a flaw (wrong insertion point, false gate, scope that
   contradicts its own constraints), say so explicitly and propose the corrected version before
   executing.

## 2. On-Device Privacy Invariant

All media, PDF, and text processing runs **on-device**. No user file is uploaded to a remote
service by any code in this repository. The Android manifest declares **no INTERNET
permission** — any new code that would require network access changes this invariant and must be
an ADR, not a drive-by change.

## 3. Settings DataStore Rules

- All persisted app settings live in `SettingsDataStore.kt` via `SettingsRepository`.
- New settings are **additive** (new `PreferencesKey` + a safe default) and are never removed while
  a released build may still read them.
- Booleans default to `false` (fail closed). Quality defaults to `MEDIUM`.

## 4. Compression & Batch Rules

- Every conversion path (`compressImageFile`, `compressVideoFile`, PDF builders) returns a typed
  result or `null` for failure — callers must handle and surface `null` explicitly, never swallow
  it silently.
- `BatchCompressionPauseController.isPaused` is the single source of truth for pause/resume. A
  queued batch item is **never dropped** — it waits until paused resolves to false.
- FFmpegKit video sessions must be awaited via the returned session's state; never fire-and-forget.

## 5. Never Weaken Tests to Pass a Build

Do not remove or weaken an existing assertion to make a build pass. Fix the code that broke the
test, or flag the conflict and explain why it exists. A green suite means correct code, not
silenced tests.

## 6. New Dependencies Require Justification

No new dependency without stating why an existing one (in `package.json` or `app/build.gradle.kts`)
doesn't cover it. Name the specific gap. FFmpegKit Lite, Coil, and DataStore are already present —
do not add duplicates.

## 7. Read State Docs Before Building

Before starting a task, read `docs/current-state.md` and `docs/architecture.md`. If the task
assumes a capability marked **ASPIRATIONAL** in either doc, stop and say so instead of building on
top of it silently.

## 8. Commit Messages Must Be Explicit

All commit messages state what changed and which doc (if any) needs a corresponding update.
Conventional Commits, `feat`/`fix`/`refactor`/`chore`/`docs`/`test` prefixes.

## 9. Work Sequentially in Layers

Finish one layer (code + tests + docs) before starting the next. Commits land individually, one
piece of work per commit, in the order the work was done.

## 10. All Commits Must Be SSH-Signed

Every commit on `main` must carry a valid SSH signature (`commit.gpgsign true`, `gpg.format ssh`).
If a commit is missing its signature, the commit is invalid — fix the config before pushing.

## 11. Grangle — Grangle Every House and Day

No task is complete without evidence in `docs/evidence/` (Constitution Article VII). A release
readiness row marked PASS requires a cited command/test/step and its observed result, never
narrative alone.

## 12. Honesty Over Optimism

Report gaps accurately. Closing one gap never means "risk is complete". A capability marked
ASPIRATIONAL stays ASPIRATIONAL until it is implemented **and** verified.

## 13. Reuse Every Unit — Steal Like an Artist, Then Measure the Compounding

Good judgement is software. Before re-deriving any procedure, check `docs/operations/skills-workbench.md`
(the adopted piece catalog) and the SOP: if a unit already covers your task, **use it and add a row
to the Reuse Ledger** (piece-id S-xx, work, time-saved estimate). A practice is only forced into the
SOP after **≥2 real uses** on different work (the buy-in test). When a task repeats twice with pain
and still has no unit, draft it as a candidate piece and record the proposed S-id. Composition rules
(SKILL-MECHANICS adapted): a unit is a name, a one-line trigger, a sequence, and completion criteria;
long reference lives outside the unit; when units multiply past memory, add a router unit rather than
more always-on docs.

## 14. RSI Is Gated By The Owner — Never Self-Authorizing

The ecosystem's self-maintenance/self-improvement (RSI) is **OFF by default and gated by the Owner**:
the Owner is the sole authorizer of any maintenance or improvement the system performs on itself.
No agent, Forge instance, or automated loop may approve its own action — see
`docs/operations/rsia-program.md` for the full requirements (provenance, blast radius, reversibility,
evidence gate, telemetry, invariant protection). A change that would self-authorize is a violation
of the Constitution, not a convenience.