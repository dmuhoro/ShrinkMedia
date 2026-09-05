# Skills Workbench — the "how we build" upgrade (2026-09-05)

> The 2026-09-05 directive: *"I want to add an upgrade to how we build by giving you access to a
> knowledge resource… see the pieces we can steal like an artist, then tweak them to fit how we
> operate… reusable pieces that compound in value as the system keeps running."*
>
> **Source reviewed this sprint:** `github.com/mattpocock/skills` at `3cca18b` — "Skills for Real
> Engineers", MIT-licensed, ~60 composable SKILL.md files (engineering + productivity + misc).
> Licence note: MIT permits copying with attribution — this is "steal like an artist", not theft;
> attribution lives at the bottom of this file. **What we take is the *grammar* — small, adaptable,
> composable units — then we re-cut every piece to our Constitution/SOP values (fail-closed,
> evidence, no silent drops).**

## 1. The meta-lesson: skills are composable, reusable units (the mechanism of compounding)

Reading the repo as a whole, the durable idea is not any single skill; it is that **engineering
judgement is software**: capture Good Judgement Once as a small, plain-text, procedure unit; reuse
it everywhere; tune it per-project. `SKILL-MECHANICS.md` gives the composition rules we adopt:

1. A reusable unit has a **name + one-line trigger description** (so an agent knows when to reach
   for it) and the **unit's own sequence** (steps + completion criteria).
2. Split the unit's **sequence** from its **shared reference** — long reference lives in a plain
   doc outside the unit so it loads only on demand (low context tax).
3. When units multiply past what the maintenance brain can remember, add a **router** (one unit
   that names the others and *when* to reach for each) rather than more always-on docs.

Where this lands in the ecosystem: `docs/operations/skills-workbench.md` + the SOP are our unit
store; `AGENTS.md §13` forces future work to **reach for a unit and count the reuse**, which is how
value compounds measurably (see §3).

## 2. Pieces we steal, and how we tweak them to fit how WE operate

"Steal" = adopt the shape + the hard-won procedure; "tweak" = re-state it against our invariants
so it cannot weaken a gate. Each adopted piece keeps its `ledger-id` (S-xx) so reuse is counted.

| S-id | Source skill (file, MIT) | What it gives us | Our tweak (Constitution/SOP filter) | Adopted into |
|------|--------------------------|------------------|-------------------------------------|--------------|
| S-01 | `engineering/to-spec/SKILL.md` | A tight spec template (Problem / Solution / User Stories / Implementation Decisions / Testing Decisions / Out of Scope / Further Notes), seams-first, "no code snippets in the spec". | Spec writing feeds our **layer discipline**; "test at agreed seams, highest single seam" becomes mandatory language in Sprint docs. | Token whiteboard for every future DataBank/Forge/EasyTutor spec |
| S-02 | `engineering/code-review/SKILL.md` | Two-axis review (Standards vs Spec) in **parallel sub-agents** + a fixed Fowler smell baseline (Mysterious Name → Refused Bequest). | Our Reviewer gate (_SOP §4_) gains the **Spec axis**: a change passes Standards but misses the spec must not merge. We add our own axis: "fail-closed + evidence" (governs all three). | SOP §4-5 upgrade |
| S-03 | `engineering/tdd/SKILL.md` | Red-before-green, **vertical slices** (one test → implementation), seams agreed up front, and named anti-patterns (implementation-coupled, tautological, horizontal slicing). | "Tautological" is our Constitution's false-confidence rule made test-level; a test that recomputes the expected value its own way is rejected. | PHASE 4 (VERIFY) grammar |
| S-04 | `engineering/diagnosing-bugs/SKILL.md` | The feedback-loop-first discipline: **no red-capable, deterministic command = no hypothesis**; tighten the loop; minimise; 3–5 falsifiable hypotheses shown to the user; tagged instrumentation; regression-before-fix; cleanup checklist. | Maps exactly onto the execution-safety guardrail from `AGENTS.md` — we add our Phase-4 shape: hypothesis ranking shared with the Owner, regression test at the **real seam** or the absence is documented. | SOP §4 + defect runbooks |
| S-05 | `engineering/wayfinder/SKILL.md` | Plan huge efforts as a **decision map** (map + decision tickets, one decision per session, frontier = unblocked tickets, fog-of-war graduates, out-of-scope never graduates). | Our "one sprint = one layer" becomes "one session = one decision ticket" for the ecosystem program; the Roadmap gains a live `Decision map` view so the owner always sees the frontier. | ecosystem-roadmap.md + sprint planning |
| S-06 | `engineering/research/SKILL.md` | **Background agent** reads primary sources (not write-ups) and returns a cited evidence file, so planning never guesses at facts. | Our evidence rule: research output lands in `docs/evidence/` with cited sources; never an uncited fact in a sprint. | recon phase of every layer |
| S-07 | `productivity/wait-what/SKILL.md` | Communication discipline: when a message doesn't land, **stop and re-pitch** in simpler language rather than plowing on. | Folds into invariant 5 ("say no early, loudly") as the *re-pitch* step; also wires into the future Forge gateway (a failed precondition = re-pitch, not silence). | SOP §0 + ForgeTask thinking |
| S-08 | `productivity/handoff/SKILL.md` (shape) | End-of-session **handoff doc** (compaction) so another agent picks up lost state without losing context. | Our existing session-summary mechanism becomes a governed artifact: each sprint doc ends with an explicit Handoff block. | sprint doc template |
| S-09 | `engineering/prototype/SKILL.md` (shape) | A **throwaway prototype** to decide "how should this behave" before committing to the build. | Tuned to "decide, then throw away": prototype answers a design question, never ships. | PHASE 2 (ARCHITECTURE) when shape is foggy |
| S-10 | `engineering/implement/SKILL.md` + `to-tickets` (shape) | Implement from a spec/ticket, artificially small slices with blocking edges surfaced. | Matches our task graph; we keep our per-task DoD (SOP §3) as the completion criterion. | PHASE 3 build discipline |
| S-11 | `engineering/triage/SKILL.md` (shape) | Explicit intake state machine (roles, verify, agent-ready briefs). | Strengthens PHASE 0 (intake / prioritise): every incoming idea gets a one-line triage verdict on record. | PHASE 0 upgrade |
| S-12 | `in-progress/retro/SKILL.md` (shape) | A deliberate **retrospective** per session — what moved / what blocked / what to change. | Our Phase 9 "lessons captured" is upgraded to a documented retro format with action items that must land in the next iteration. | PHASE 9 upgrade |

**Deliberately NOT stolen:** the `triage-labels`/issue-tracker setup machinery (we are doc-based, not
tracker-based); anything that introduces a model-dependence; `setup-matt-pocock-skills` (we curate
our own). Rationale — "steal like an artist": take the durable judgement, leave the ceremony that
duplicates what we already run.

## 3. The compounding mechanism (how reuse is measured)

Every use of an adopted piece (S-xx) is counted in the **Reuse Ledger** below. A piece only lands in
the SOP when it has earned **≥2 real uses** on different work — that is the buy-in test, and it is
exactly the "compound in value as the system keeps running" loop made measurable.

### Reuse Ledger (append-only; every layer adds a row)

| Date | Work that used it | Pieces used | Time saved vs re-deriving (estimate) |
|------|-------------------|-------------|--------------------------------------|
| 2026-09-05 | Sprint 21 (this sprint): ecosystem directive turnaround | S-01 (DataBank spec shape), S-02 (this sprint's two-axis self-review method), S-03 (seams at runbook/vault boundary), S-04 (bug loop when HostTier tests failed), S-05 (roadmap decision-map), S-06 (runbook/deploy facts re: FTS5/Debian) | ~2–3 h not spent re-deriving procedures |
| 2026-09-05 | DataBank MVP build | S-01, S-03, S-04 | ~1 h |
| … | *(earned by use, not by promise)* | | |

### How a unit gets written in (process)

1. **Propose**: when a recurring procedure appears twice with pain, draft it as a unit (name,
   trigger description, sequence, completion criteria) in this file's §2 table as a candidate.
2. **Prove**: use it on ≥2 real tasks; record each use in the ledger with the time-saved estimate.
3. **Govern**: once earned, fold its binding rules into the SOP (never into narrative), citing the
   S-id; a unit that drifts from our invariants is re-cut or dropped — never kept as stale text.

## Attribution

Adapted, with substantial re-cutting against ShrinkMedia's Constitution/SOP, from **Matt Pocock's
`mattpocock/skills`** (MIT License, © 2026 Matt Pocock, https://github.com/mattpocock/skills).
Original license text is reproduced in that repository; our adaptations preserve attribution and the
MIT notice is kept in this file's canonical record at `docs/evidence/2026-09-05_ecosystem_directive_sprint.md`.