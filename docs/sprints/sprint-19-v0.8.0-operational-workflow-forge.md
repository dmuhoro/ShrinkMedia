# Sprint 19 — Operational Workflow: the SOP + Forge portable engine (case study #1)

**Status:** EXECUTED (2026-09-04)
**Scope:** turn "how we build" from implicit process into a *codified, executable, measurable*
operating system — the **SOP** (`docs/operations/SOP.md`) that governs the whole SDLC, ADR-014,
and the **portable Forge workflow engine** (ForgeTask / EcosystemIndex / LessonBook / ModelRouter)
implemented in ShrinkMedia as **case study #1** and designed to be lifted into the future Forge
repo unchanged (ADR-013/014). **No INTERNET permission is added; the default build stays offline
and private.** This sprint releases as **v0.8.0** — a **catch-up capability milestone** that also
publishes the previously-unreleased v0.7.0/v0.7.1 work, reconciling code ↔ GitHub release state.

## Focus

1. **SOP — codify the SDLC (operate like an organisation).** `docs/operations/SOP.md` turns the
   Constitution + ADRs into one governed process:
   - **Operating doctrine:** one Founder wearing PM / Principal Engineer / EM / Lead hats in order,
     plus ten invariants (fail-closed, real-boundary, no silent drops, evidence-over-narrative,
     honesty-over-optimism, tests never weakened, sequential layers, on-device privacy, everything
     recorded, one workspace at a time).
   - **The deterministic lifecycle (phases 0–9):** intake → PRD → architecture (+ADR when needed) →
     build → verify → review → evidence/docs → release gate → commit/push → lessons.
   - **Definition of Done checklist** for every task (9 boxes; incomplete = in progress, never
     silently closed).
   - **Reviewer-gate checklist + evals discipline** (false-merge/false-block rates gate autonomy).
   - **Productivity/scale telemetry** — calibration-0 (today) anchored to real numbers
     (≈104 commits / 18 sprints / 14 ADRs / 27 evidence files in ~33 days), model steps 60→100%.
2. **ForgeTask — the engine encodes the lifecycle.** Pure-Kotlin deterministic state machine
   (`queued → retrieving → building → reviewing → changes_requested → merged` / `blocked`),
   **no silent drops** (blocked routes to the human, never auto-retried, terminal states never
   resurrect), full history log. 12 unit tests.
3. **EcosystemIndex — "search up the ecosystem like an extension".** Local, deterministic, offline
   corpus: chunk → tokenize → inverted index → ranked keyword search with snippets. Fail-closed
   empty/null handling; duplicate-id idempotent. **Honest scope:** keyword index (the semantic
   RAG/rerank is the later Forge layer). 11 unit tests.
4. **LessonBook — never re-invent the wheel.** Phase-9 lessons-learned captured through
   `EcosystemIndex`; mandatory non-blank fields; duplicate-id idempotent; searchable. 6 unit tests.
5. **ModelRouter — the free-open-source-AI edge (seam only).** Online ⇒ best available **open-weight
   remote model**; offline ⇒ **local fallback** (on-device / home-lab Ollama). Typed decisions
   (AllowedRemote/AllowedLocal/Off/Refused/Unavailable-with-reason), preconditions mirror
   `ConnectedRepository` (OFF by default, consent + explicit invocation required). **Decision logic
   only — no network call, NO INTERNET added.** 11 unit tests.
6. **Case study #1 — the workflow runs a real task end-to-end.** Evidence file proves the
   EcosystemIndex task executed through SOP phases 0→9, producing the working corpus + a captured
   lesson (`case-001-ecosystem-index`). The workflow is no longer a poster.

## What shipped

| Change | Files | Proof |
|--------|-------|-------|
| SOP (SDLC + DoD + doctrine + telemetry) | `docs/operations/SOP.md` | cited below |
| ADR-014 (portable core + ModelRouter seam) | `docs/adr/ADR-014-*.md` | cited below |
| `ForgeTask` deterministic state machine | `forge/ForgeTask.kt` | 12 JVM tests green |
| `EcosystemIndex` searchable corpus | `forge/EcosystemIndex.kt` | 11 JVM tests green |
| `LessonBook` lessons capture | `forge/LessonBook.kt` | 6 JVM tests green |
| `ModelRouter` free-AI seam | `forge/ModelRouter.kt` | 11 JVM tests green |
| Case study #1 evidence | `docs/evidence/2026-09-04_forge_l1_operational_workflow.md` | full suite green |

## Honest status (what is NOT claimed)

- **EcosystemIndex is a keyword index, not semantic retrieval.** Semantic search + rerank
  (pgvector + embedding model) is the later Forge RAG layer (ADR-013 L3/L4) — do not claim it here.
- **ModelRouter decides, it does not infer.** Real open-weight/local inference needs the owner's
  home-lab GPU (RTX 3090+) / DataBank-Forge hosts (ADR-013 L2+). No network exists in this repo.
- **This is a catch-up capability milestone (v0.8.0),** publishing the previously-unreleased
  v0.7.0/v0.7.1 work alongside the new engine. Future `0.x.y` is reserved for genuine patches on a
  shipped block.
- The 20–30-person-team equivalence is an **output-multiplier target** (process + knowledge +
  automation), tracked honestly via SOP §6 telemetry, not a claim that one machine replaces
  specialists.

## Verification

- `./gradlew :app:testDebugUnitTest` → **60 tests, 0 failures** (20 prior + 40 new forge tests).
- `./gradlew :app:lintDebug` → **0 errors** (29 informational warnings, pre-existing).
- `./gradlew :app:assembleDebug` → **BUILD SUCCESSFUL**.
- Merged **debug** manifest: no `android.permission.INTERNET` (invariant held).

## Evidence

- `docs/evidence/2026-09-04_forge_l1_operational_workflow.md` (case study #1, SOP phases 0–9)
- `docs/adr/ADR-014-operational-workflow-forge-core-model-router.md`
- `docs/operations/SOP.md`

## Outstanding (explicit, not hidden)

- **Forge repo lift:** the engine core is portable; wiring it into the Forge program
  (Python/FastAPI orchestrator + RAG + agents + Docker sandbox) is ADR-013 L3/L4.
- **Real ModelRouter transport** (open-weight serving on owner hardware; DataBank connected
  flavor) — L2+/L3, dedicated program with the owner's hardware/credentials.
- Semantic retrieval (pgvector + embeddings) in the Forge layer.
- Real Gemini Nano inference proof (needs a Nano-capable device; C11 honest).
- Keystore off-machine copy + release tagging/Play distribution (human steps).

## Lessons learned (this sprint, captured via LessonBook)

- `case-001-ecosystem-index` — honest capability scoping (keyword ≠ semantic) prevents
  overclaiming (Constitution §12); enforcement/verification must exercise the real phase.