# ShrinkMedia & Ecosystem — Standard Operating Procedure (SDLC)

> **Governing document for HOW WE BUILD.** This SOP is the codified, end-to-end process every
> product and every task in the ecosystem (ShrinkMedia today; DataBank, Forge.ai, Daftari,
> Hermes-Forge, and the Forge orchestrator later — ADR-013/014) runs through. It makes the
> implicit SDLC explicit so that anyone (or any agent, or the Forge workflow engine) can follow
> the same path from intent to shipped, evidenced, learned-from work.
>
> Status: **Living** — this document is itself a system artifact and is updated by the same
> process that updates code.
> Supersedes nothing in the Constitution; it operationalizes it. In a conflict, the Constitution
> (`docs/engineering/CONSTITUTION.md`) wins.

---

## 0. Operating doctrine — "operate like an organisation, not a lone developer"

We are **one Founder operating as a full product organisation** — EM, PM, Principal Engineer,
Lead. That is not a metaphor; it means the four hats are **always worn, in order, every task**:

1. **PM** — Is this the right thing to build? Who is it for? What is success and how do we
   measure it? (intake → PRD)
2. **Principal Engineer / Architect** — What is the honest architecture? Where is the real
   boundary? What fails closed? What is the risk? (architecture + ADR when required)
3. **EM** — Can the team (me/agents) actually deliver this in the planned scope with green
   gates? Are tasks sequenced so nothing blocks? (task graph, sequencing, review)
4. **Lead** — Did we ship it, evidence it, record what we learned, and leave nothing half-done?
   (Ops, evidence, lessons learned)

**Ten invariants (bind every task):**
1. **Fail closed, never fail open.** Defaults refuse. When unsure, take the conservative cap.
2. **Enforcement at the real boundary.** Guards live where the production action runs, not in a
   helper only the backtest/test path uses.
3. **No silent drops.** Every failure/refusal is explicit: typed reason + audit record + surfaced
   progress.
4. **Evidence over narrative.** Nothing is "done" without a cited command/test/step and its
   observed result in `docs/evidence/` (Constitution Article VII).
5. **Honesty over optimism.** A capability marked ASPIRATIONAL stays ASPIRATIONAL until it is
   implemented **and** verified. Never claim a protection the code does not actually provide.
6. **Never weaken tests to pass.** A green suite means correct code, not silenced tests.
7. **Work sequentially in layers.** Finish one layer (code + tests + docs) before the next. One
   piece of work per commit.
8. **On-device privacy invariant.** No user file uploads; no INTERNET in the default build (merged
   manifest CI-guarded). Network = ADR, never a drive-by.
9. **Everything is recorded.** Knowledge, decisions, and lessons accumulate into the searchable
   corpus so we never re-invent the wheel.
10. **One workspace at a time.** Sequential per-repo execution with shared contracts defined first.

---

## 1. The task lifecycle (the SOP pipeline)

Every unit of work is a **task**. A task can be a bug, a capability, a layer, a doc, a refactor,
or a release. The pipeline is deterministic (see `ForgeTaskState` / `ForgeTask` — the workflow
engine encodes this exact lifecycle):

```
IDEA
  │
  │ PM hat
  ▼
PHASE 0 — INTAKE & PRIORITISE
  • stated as intent + outcome + user ("so that…")
  • decision: BUILD / PARK / REJECT (parked → ideas.md; rejected → noted why)
  ▼
PHASE 1 — PRD (1 para + acceptance criteria)
  • problem, users, outcome, measurable acceptance criteria
  ▼
PHASE 2 — ARCHITECTURE (+ ADR when it changes invariants/scope)
  • real boundary identified & cited (file:line)
  • fail-closed default chosen; new dependency justified (AGENTS §6)
  • violates no ADR/invariant; if it wants to → write the ADR first
  ▼
PHASE 3 — BUILD
  • code in layers, production quality, no dead commentary
  • one task = one focused change-set
  ▼
PHASE 4 — VERIFY (green gates)
  • JVM unit tests for decision logic
  • instrumented tests on the real path (on-device where applicable)
  • lint 0 errors; typecheck; new feature does not weaken existing tests
  ▼
PHASE 5 — REVIEW
  • self-review vs PRD + Architecture + Review Checklist (below)
  • Forge workflow: Reviewer gate — pass → merge; changes → rebuild;
    exceeds maxAttempts → BLOCKED (surfaces to human, never silently dropped)
  ▼
PHASE 6 — EVIDENCE & DOCS
  • evidence file in docs/evidence/ with cited commands + observed results
  • sprint doc, current-state row, CHANGELOG, (release-readiness if releasing)
  ▼
PHASE 7 — GRILLE (release gate) — vN bump, release APK/AAB, apksigner,
  merged-manifest no-INTERNET proof, CI green (all jobs)
  ▼
PHASE 8 — COMMIT & PUSH (individual, SSH-signed, explicit message per AGENTS §8)
  ▼
PHASE 9 — LESSONS LEARNED (captured into the searchable corpus, never lost)
  └─► back to top (compounding loop)
```

**Enforcement:** the workflow engine (`ForgeTask` + `EcosystemIndex` + `LessonBook` + `ModelRouter`)
exists so this lifecycle is not a poster — it is executable and auditable.

---

## 2. Artifacts produced per phase

| Phase | Artifact | Location |
|-------|----------|----------|
| 0 | Intake outcome (build/park/reject) | sprint doc + `docs/ideas.md` for parked |
| 1 | PRD (problem + acceptance criteria) | sprint doc (or `docs/prd/` for large work) |
| 2 | Architecture note / ADR | `docs/adr/ADR-NNN-*.md` when required |
| 3 | Production code | `app/src/main/...` |
| 4 | Test evidence | `app/build/reports/...` + test files |
| 5 | Review result | ForgeTask history / review notes |
| 6 | Evidence + docs | `docs/evidence/`, `docs/sprints/`, `docs/current-state.md`, `CHANGELOG.md` |
| 7 | Release artifacts | `app/build/outputs/apk/release/*` / `.aab`, `apksigner` output |
| 8 | Signed commits/log | git history (SSH-signed, `%G?` = `G`) |
| 9 | Lessons-learned entries | `data/lessons/` corpus, searchable via `LessonBook` |

---

## 3. Definition of Done (applies to EVERY task)

A task is **DONE only when all of the following hold**:

- [ ] **Spec is satisfied** — every acceptance criterion is met and verifiable by a run, not an assertion of intent.
- [ ] **Architecture honoured** — no invariant violated; insertion at the real boundary; fail-closed.
- [ ] **Tests green** — relevant JVM + instrumented tests pass; no existing test weakened/skipped.
- [ ] **Quality gates green** — lint 0 errors, typecheck clean, CI all jobs pass.
- [ ] **Evidence written** — `docs/evidence/` file cites the exact command/step + observed result.
- [ ] **Docs updated** — sprint + current-state + (if released) CHANGELOG + release-readiness.
- [ ] **Committed individually** — one piece of work per SSH-signed commit with an explicit message; pushed; CI green on main.
- [ ] **Lessons learned** — captured into the corpus (Phase 9), so the next identical situation is handled faster.
- [ ] **No half-done work remains** — nothing left "pending" from this task; parked work is explicitly parked with a reason.

If any checkbox is unmet, the task is **NOT done** — it stays open and is surfaced (no silent
swallow). This is the single most important operational rule: **incomplete = in progress.**

---

## 4. Review Checklist (used by the Reviewer gate)

Reviewer (a genuinely separate instance from the Builder — the same context that wrote the code
must not grade it):

1. Diff touches only the declared `file_scope`? — else REJECT.
2. Every acceptance criterion met? — else REJECT.
3. Existing tests weakened/skipped/deleted without justification in history? — else REJECT.
4. Fail-closed default chosen (conservative cap when unsure)?
5. No INTERNET added to the default build; network work gated behind Connected mode + consent?
6. No new dependency without a named gap (AGENTS §6)?
7. Private data stays on-device (no upload, no logging of secrets)?
8. Specific to the repo's risk profile (e.g. ShrinkMedia: FFmpegKit session is awaited, batch item
   never dropped, `SettingsDataStore` additive, booleans default `false`)?
9. If uncertain about architecture intent → fail with a question, never guess.
10. Evidence cited (not narrative) for every claim.

**Evals discipline (Constitution):** every Reviewer decision is logged; `pass`ed tasks later
needing hotfixes are tracked (false-merge rate) and `changes_requested` that were false positives
are tracked (false-block rate). That number gates autonomy widening — not a calendar date.

---

## 5. Operating as an organisation — cadence

- **Daily digest** (each working sit): what was moved BUILD→VERIFY→REVIEW→EVIDENCE→SHIPPED; what
  is BLOCKED; one line on active risk.
- **Sprint** (each release or milestone): sprint doc in `docs/sprints/`, DoD signed, evidence
  referenced, lessons captured.
- **Release gate** (`docs/release-readiness.md`): table per version; every row either PASS with
  evidence or explicitly NOT-MET with a reason.
- **Knowledge review**: lessons corpus reviewed periodically; high-value lessons promoted into
  `AGENTS.md` / the SOP / code standards so they govern future work.

---

## 6. Productivity & "building-at-scale" telemetry

We measure, not guess. Baseline observed from git history (ShrinkMedia): **104 commits, 19
sprints, 15 ADRs across first-commit 2026-08-30 → 2026-09-05, 85 JVM tests, 0 failures**
**(6 committed days, ~17 commits/day)**. (Earlier drafts cited "~33 days" — corrected: the committed
history is 6 days.) That is our current operating point; recognizing it is not a ceiling, it is the
**calibration 0**.

The scale model (how "potential %" is anchored):
- **Calibration 0 (~50%)** = today: process-disciplined solo throughput, knowledge written-but-not-searchable, single-threaded.
- **60–70%** = SOP + searchable lessons corpus (stop re-inventing) + telemetry per sprint — measured velocity replaces guessing.
- **70–80%** = ModelRouter (free open-source AI when online, local fallback offline) + workflow engine automation of evidence/test/drill loops.
- **80–90%** = Forge orchestrator loop (Planner→Buider→Reviewer gate→Ops) with human approvals — 20–30-person-equivalent output on buildable categories.
- **90–100%** = the long tail (virtual-me / metacognition) — research-grade, not promised.

The first deliverables of this SOP make the number **measurable** (telemetry) instead of assumed.

---

## 7. Adoption

This SOP governs **ShrinkMedia from this sprint onward** (case study #1 in `docs/evidence/`), and
becomes the shared operating contract that the Forge workflow engine + DataBank + later repos
inherit (ADR-013/014). Any deviation requires an ADR.