# Case Study #2 — Personal Intelligence Foundation, run through the SOP end-to-end

**This is the SOP's own real-time test-drive** (the operational directive of 2026-09-05: *"put the
SOP and the operational workflow to a real-time test and see how fast, effectively and efficiently
we can do things"*). A single, coherent engineering layer — Personal Intelligence foundation —
executed by the same two-agent team (human owner + AI Lead) through **all nine SOP phases**, with
clock evidence.

- **Task run through the SOP:** Personal Intelligence Foundation (ADR-015) — vault categories
  (notedAt / reappearedAt / followedBy), Image Insight (scan-a-photo → clarify-or-proceed),
  PersonalIntelligenceAgent routing (vault / EasyTutor / recall / clarify / refuse), wired into the
  ShrinkMedia UI.
- **Programme:** ShrinkMedia Sprint 20 (v0.8.1-dev). Agenda: ecosystem orientation + ShrinkMedia
  stop-point + repos Forge/DataBank + estimates (docs), PI foundation (code), release of context.
- **Window:** started wall-clock **2026-09-05T07:11:43Z** (`date` proved, git base `2078a13`
  = v0.8.0). Layer complete (code+docs+repos+release build) **2026-09-05T08:03Z** —
  **≈ 52 minutes of wall-clock** for the full foundation layer (excl. CI wait).
- **Headline:** a full feature layer (3 decision engines + UI + 25 new tests + 8 doc artifacts +
  2 new repos) designed, built, verified, and release-built in **≈ 52 minutes** — and every step
  produced an artifact, not just a chat.

## Phase record (SOP §3)

| Phase | Deliverable | Evidence | Clock |
|-------|-------------|----------|-------|
| 0 Intent | Owner directive (2026-09-05) + clarifiers | `docs/operations/ecosystem-orientation.md` (answers) | 07:11Z |
| 1 PRD | Problem: "help me make the best move here that will serve me in the future… alive + accessible via the stargate" | PRD embedded in the directive + orientation doc | 07:11Z |
| 2 Architecture | **ADR-015** (processing-around-the-vault, not inside), roadmap, stop-point, orientation | `docs/adr/ADR-015-*.md`, `docs/operations/*.md` | 07:13Z |
| 3 Build | 3 decision engines + UI | `app/…/personal/NoteRecord,NoteClassifier,InstructionAider,PersonalIntelligenceAgent.kt` + `MainActivity.kt` + 25 tests | 07:20Z |
| 4 Verify | 60→**85 JVM tests, 0 failures**; `lintDebug` **0 errors**; `assembleDebug` green; merged DEBUG+RELEASE manifests **no INTERNET** | CI + lint + test reports | 07:32Z |
| 5 Review | Reviewer-gate: fail-closed checks, no silent drops, honest boundary (vision = ASPIRATIONAL), re-run gates | this file + case-study-1 precedent (`2026-09-04_forge_l1_…`) | 07:33Z |
| 6 Evid/Docs | case study, orientation, roadmap, stop-point, sprint-20, CHANGELOG, current-state C24, release-readiness, cross-ref | `docs/evidence/2026-09-05_*.md` + doc updates | 07:52Z |
| 7 Release | release build verified + repo setup (Forge + DataBank created) | `app-release.apk` badging 0.9.0/versionCode 10; `gh repo view dmuhoro/{Forge,DataBank}` = PRIVATE | 08:03Z |
| 8 Commit/Push | 6 individual SSH-signed commits, push, CI | git + CI run **33954385291** (all 4 jobs `success`, 5m40s, incl. all three no-INTERNET manifest guards); release published at 

https://github.com/dmuhoro/ShrinkMedia/releases/tag/v0.9.0 | 08:07Z (push) → 08:15Z (release live) |
| 9 Lessons | captured → this file (`case-002-*`) | this file | 07:52Z |

## Effectiveness / efficiency (SOP §6 telemetry)

- **Cost per outcome:** 25 tests, 3 engines, 1 UI flow, 8 docs, 2 repos — each sized to a single
  commit; nothing left half-done (AGENTS §9).
- **Defects caught where they should be:** the Image-Insight clarify-flow lost its transcript
  (caught by a unit test, fixed); the agent's recall gate quietly swallowed re-captures (caught by
  a unit test, fixed) — both are exactly the class of "false claim" the SOP exists to catch.
- **Honest deltas:** wall-clock is *not* pure-value time (tool runs, compilation wait); the SOP telemetry
  measures work-done-per-day, and calibration-0 is now **6 committed days / 19 sprints / 85 tests**.
- **False-confidence audit (pass):** nothing marked PASS that isn't; "vividly describe images" is
  explicitly ASPIRATIONAL (vision model), while OCR+clarify+route is real and on-device.

## Lesson captured (Phase 9)

> **case-002-personal-intelligence-layering:** a "personal intelligence" product is mostly
> *decision logic* in its first safe form — OCR the input, classify it, route it, refuse loudly.
> Real inference/vision/transport is a later seam. Building the decision layer first gives a real,
> honest, on-device feature (and its tests) without over-claiming AI — and it IS the seam the later
> brain plugs into.

**Release-readiness for this sprint's layer:** tests 85/85 green · lint 0 · no-INTERNET held ·
docs up to date · commits signed + pushed · CI green — see `docs/release-readiness.md`.