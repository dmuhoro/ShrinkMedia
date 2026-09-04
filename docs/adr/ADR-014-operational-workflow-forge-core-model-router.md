# ADR-014: The Operational Workflow (SOP Engine) — Portable Core, Forge-as-Independent-Feature, ModelRouter Seam

**Status:** Accepted (architecture, foundation — sprint-19)
**Date:** 2026-09-04
**Deciders:** Daniel Muhoro

## Context

The owner's directive (2026-09-04) elevates the ecosystem from "a phone portal + a data server" to
**an operating system for building** — operate like an organisation, follow one SOP from intent to
shipped/evidenced/learned-from work, capture every lesson so the wheel is never re-invented, and
aim for the output of a 20–30-person team. Three new requirements must be reconciled with the
existing architecture (ADR-011/012/013):

1. **The Operational Workflow must live in Forge as an independent feature** — but **Forge is a
   separate repo/stack** per ADR-013 (Python/FastAPI orchestrator + RAG + agents + safety). It must
   also be usable **now** to complete ShrinkMedia as case study #1.
2. **ShrinkMedia (portal) and DataBank (server) are separate**, so the workflow engine cannot be
   embedded in the Android APK wholesale — but a **portable core** can be built once, tested
   everywhere, and carried into the Forge repo.
3. **ModelRouter insight:** the ecosystem should connect to **any open-source AI model** — use free
   open-weight AI when online, and **fall back to local AI (on-device / home-lab Ollama) when
   offline** — as an edge over closed-vendor assistants (resilience + privacy + cost). This is a
   durably valuable capability but MUST NOT touch the no-INTERNET default build (Constitution + ADR-012).

## Decision

1. **The Operational Workflow is a portable, repo-neutral core implemented first in ShrinkMedia
   (case study #1), designed to lift into the Forge repo later.** Concretely, this sprint ships a
   small pure-Kotlin/`src/main` module — `com.shrinkmedia.compressor.forge` — of four pieces:
   - `ForgeTask` — the deterministic task state machine encoding the exact SOP lifecycle
     (`queued → retrieving → building → reviewing → changes_requested → merged` / `blocked`, no
     silent drops, terminal states never resurrect, full history).
   - `EcosystemIndex` — a chunked, tokenized, inverted-index, ranked-keyword corpus over the
     ecosystem's docs (ADRs, PRDs, SOP, sprint docs, lessons). This is the local, zero-infra
     "search up the ecosystem like an extension" seed; it is the predecessor of the Forge RAG layer
     (Daftari `ai-context/` + pgvector in the later Forge repo).
   - `LessonBook` — the lessons-learned capture, stored through `EcosystemIndex` so every completed
     task writes a retrievable lesson (never re-invent the wheel).
   - `ModelRouter` — the fail-closed seam for the open-source-AI edge: given the Connected-mode
     preconditions (ADR-012) + runtime connectivity + available remote/local model lists, it returns
     a typed route (`Allowed remote open-weight` / `Allowed local fallback` / `Off` / `Refused` /
     `Unavailable`) with no silent drops. It is a *decision* seam only — it performs no network call
     in this repo (no INTERNET is added); the real transport lives behind explicit Connected-mode
     consent + the later connected flavor (ADR-013 L2).

2. **The core is engine, not a chatbot.** Decision logic only, fully unit-testable, zero I/O, zero
   network. The AI agents (Planner/Buider/Reviewer) in the later Forge program drive this engine;
   the engine's state machine is the determinism that makes the eventual orchestrator debuggable.

3. **SOP governs first.** `docs/operations/SOP.md` codifies the SDLC (intake → PRD → architecture →
   build → verify → review → evidence/docs → release gate → commit/push → lessons). The engine
   encodes that lifecycle; the SOP is its spec. The SOP also anchors practical productivity
   telemetry (baseline: ~104 commits / 18 sprints / 14 ADRs / 27 evidence files in ~33 days) so
   "percent of building-at-scale potential" is measured, not guessed (calibration 0 ≈ 50%; model
   steps 60–100% in SOP §6).

4. **ModelRouter is a Connected-mode feature only.** OFF by default (fail-closed); routing requires
   the exact same preconditions as any connected action (Connected mode ON + consent acknowledged +
   explicit invocation) via `ConnectedRepository`. The default debug+release merged manifests
   continue to declare **no INTERNET** (CI-guarded).

## Consequences

**Positive**
- **Organisation-scale operating model** on one document + one testable engine: consistent process,
  reproduced evidence, compounding lessons.
- **Model-agnostic + free + resilient AI:** online → best open-weight model (own hardware), offline
  → local fallback; no vendor lock-in, no per-token vendor cost, works in a plane.
- **Case study #1 is real:** ShrinkMedia built *through* the workflow engine, documented in
  `docs/evidence/2026-09-04_forge_l1_operational_workflow.md`.
- The engine is carried forward into the Forge repo unchanged (pure core), honoring ADR-013's
  separation.

**Negative / honest**
- `EcosystemIndex` is a **local keyword index** (deterministic, offline) — it is the *seed* of the
  Forge RAG layer, NOT semantic retrieval + rerank (that requires pgvector + an embedding model in
  the later Forge/DataBank stack). We do not claim semantic search here.
- `ModelRouter` decides the route but performs no real inference in this repo; real open-weight
  serving needs the owner's home-lab GPU (RTX 3090+) and/or the DataBank/Forge hosts (ADR-013 L2+).
- The 20–30-person-team equivalence is an **output multiplier target** (process + knowledge +
  automation), not a claim that a single machine replaces specialists — tracked honestly via the
  SOP telemetry, not a poster.

## Alternatives considered

- **Build the full Forge orchestrator inside the ShrinkMedia APK:** rejected — violates ADR-013's
  repo separation (Android app cannot host Python orchestrator/Docker sandbox), couples release
  cycles, and would entangle the no-INTERNET manifest with server networking.
- **No ModelRouter, stay vendor-pinned:** rejected — the owner explicitly wants free open-source AI
  with a local-offline fallback; that is a first-class product edge, not a bolt-on.
- **Teach via prose only (no engine):** rejected — a poster SOP does not enforce; the executable
  state machine is what makes the process auditable and what the later Forge agents will drive.

## Linking

ADR-010, ADR-011, ADR-012 (Connected-mode seam), ADR-013 (Forge = separate program; DataBank =
separate repo), `docs/operations/SOP.md`, `docs/ecosystem.md` §7/§8, `Assets/forge-orchestrator-build-brief.md`.