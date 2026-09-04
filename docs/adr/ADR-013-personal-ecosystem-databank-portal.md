# ADR-013: The Personal-Ecosystem & DataBank Portal — Connectable Layer over ALL Products

**Status:** Accepted (architecture, foundation)
**Date:** 2026-09-04
**Deciders:** Daniel Muhoro

## Context

The owner's vision (absorbed from handwritten notes + the Forge orchestrator build brief +
`docs/personal-intelligence.md`) is a **personal ecosystem**: a portal, accessible from his
phone, into his own digital world, with **personal intelligence** driven by his own data —
gathered, stored, and processed on infrastructure he owns/controls (notes reference his own
hardware: a home lab / Mac mini / RTX 3090; a "data server" and "storage facility for my data").

He already has several public projects, each serving one layer:

| Project | Role today | Layer in ecosystem |
|---------|-----------|--------------------|
| **ShrinkMedia** | Private on-device Android media/document toolkit + on-device AI (ADR-011/012) | **Edge client / collection portal** (the phone surface) |
| **Daftari** | Offline-first BusinessOS (React/TS/Vite/Supabase) with a mature `ai-context/` corpus | End-user product; RAG retrieval corpus + first real user base |
| **Hermes-Forge** | Fully-offline VS Code AI IDE extension (Ollama); AgentEngine, Codebase Oracle, checkpoints | **Offline Builder** — local agentic code capability |
| **Forge.ai** | Cloud AI app-builder (Next.js + Anthropic + Sandpack + Supabase) | **Cloud Builder / orchestration host** (the "Forge" of the build brief) |
| **DataBank** (to build) | Personal storage server on owned hardware | **Storage + processing brain** (the "data server") |

The Forge build brief describes the **orchestrator**: Planner / Retriever / Builder / Reviewer /
Ops + a task state machine + evals/safety + RAG, to build and maintain these products. His notes
add two crucial ideas: **(a)** a personal assistant present **"everywhere,"** and **(b)** that
everything connects **"as an MCP"** (Model Context Protocol) — i.e. the connectable layer must be
a standard, tool-agnostic protocol, not bespoke glue per product.

## Decision

1. **DataBank is a separate, self-hosted server + a connectable layer** (the owner's choice): it
   is NOT built into the ShrinkMedia APK. It is its **own separate project/repo** — it is a
   *server* (object store + index + processing + auth), not a mobile app, so it has an independent
   lifecycle, toolchain, and release cadence. ShrinkMedia is a *client* to it. The two share only
   a **contract** (the MCP `vault.*` tool set + auth) — not a codebase. This is the architecture
   chosen for clarity: it removes the coupling that would arise from building a server inside the
   mobile APK (coupled release cycles, Gradle↔server tooling, and — worst — the app's
   privacy-invariant NO-INTERNET manifest entangled with server networking). Same founder, one
   owner; independent, cleanly separated artifacts. Everything else connects to DataBank as a
   client. It lives on his own hardware and exposes storage, indexing, and processing.

2. **The connectable layer speaks MCP** (Model Context Protocol). Each product (ShrinkMedia,
   Daftari, Hermes-Forge, Forge.ai) ships a thin **MCP adapter** exposing its capabilities
   (ShrinkMedia: compress/PDF/OCR/on-device-AI; Daftari: ledger domain; Hermes-Forge: local agent;
   Forge.ai: cloud builder). This makes the ecosystem **tool-agnostic and reversible**: the
   personal assistant can reach any product, and no product becomes a hard dependency of another.

3. **ShrinkMedia = the phone portal**: it is the always-with-you surface. Its **Connected mode**
   (ADR-012, opt-in, OFF by default) is the *first* client hop into the ecosystem:
   - when OFF (default): fully offline, no INTERNET, no uploads (current invariant preserved);
   - when ON (explicit run-time consent per action): the user can push selected files to DataBank
     or pull from it, without any data leaving his control unless he invokes a connected action.

4. **Forge (orchestrator) is a separate program**, per its own build brief and stack, whose
   purpose is to build/maintain the ecosystem faster, correctly, and with less time. Its five
   pillars (RAG, agents, evals, safety, ML foundations) map directly:
   - **RAG (Retrieval):** Daftari's `ai-context/` + each product's ADRs/PRDs become the corpus;
   - **Agents:** Planner / Retriever / Builder / Reviewer / Ops (Hermes-Forge = offline Builder;
     Forge.ai = cloud Builder);
   - **Evals:** the Reviewer-gate + judge/calibration loop (his "Judges" layer);
   - **Safety:** Docker-sandboxed builders, hard file-scope enforcement, prompt-injection defense,
     rate/cost caps, fail-closed gates (mirrors ShrinkMedia's Constitution §1/§5);
   - **ML foundations:** the personal-intelligence reasoning core (on-device Gemini Nano via
     ADR-011; cloud Gemini only via ADR-012 Connected mode; self-hosted models on his hardware).

5. **Sequencing** (one layer at a time; default builds stay fail-closed):
   - **L1 (this sprint):** C17 Layer-1 foundation in ShrinkMedia — additive `connected_mode`
     setting (OFF default, fail-closed) + a `ConnectedRepository` gateway returning typed results
     (no silent drops) + consent UX. NO INTERNET is added; CI keeps asserting the default merged
     release manifest carries no INTERNET permission (real boundary).
   - **L2 (next):** Define the **DataBank transfer contract** (MCP tool set: `vault.put`,
     `vault.get`, `vault.index`, `vault.query`) + the connected flavor that would carry INTERNET
     behind explicit consent. The real OAuth/cloud/self-host networking is a dedicated program.
   - **L3 (beyond):** Hermes-Forge / Forge.ai / Daftari MCP adapters; the orchestrator program.

## Expanded vision (owner directive, 2026-09-04)

The ecosystem is the vehicle for a larger end-state (fully captured in `docs/ecosystem.md` §7 —
"the Founder's Engine"). The pillars this ADR's architecture must eventually support:

- **Autonomous product factory (Forge)**: Planner → PRD/ADR → Builder → Reviewer → Ops, with a
  **real-time preview panel** of what is being built, a **safety net** that catches failures
  across products and escalates to the owner, and **virtual-me oversight** that corrects any
  agent that diverts from the end-goal. No silent drops; failures are explicit and audited.
- **Virtual me / digital-identity guardian**: a "clone" that is the guardian and keeper of the
  owner's digital identity, giving every agent consistent values, memory, and governance.
- **Thinking / architect modes + PRD clarity**: a dedicated workspace that thinks and architects
  before building.
- **Polymath apprenticeship knowledge base**: trained on history's polymaths/engineers/scientists/
  artists/architects/mathematicians, saved as **skills** invoked when situations demand them; the
  virtual me learns emerging technology to mastery-level.
- **Life-as-a-system loop**: systematize thinker/architect/builder processes; reverse-engineering
  feedback (build → learn what worked/why → raise awareness) as in EasyTutor.

**Honest boundary (fail-closed, no false claims):** the factory loop, safety net, preview panel,
RAG retrieval, skills library, evals/guards, feedback loops, and portal connection are buildable
and milestone-gated (§"Sequencing"). A genuinely independent self-directed "clone," true
cross-agent autonomy without oversight, and "mastery-level" general learning are **research-grade
and stay ASPIRATIONAL until implemented *and* verified** (Constitution §12) — we do not claim them
before they run.

**Scale / schedule / compute (80/20, sequential):** the first ~2 months (L1–L4) delivers most of
the daily-operational value (working phone portal + self-hosted vault + trustworthy Forge loop
across the three live products) — see `docs/ecosystem.md` §8. Compute floor: one GPU box (RTX
3090+, 24 GB VRAM) + one low-power host (Mac mini) ≈ $2,500–4,500. ShrinkMedia itself stays
on-device with no server GPU.

## Consequences

**Positive**
- A single, standard **contract (MCP)** across all products = the "connect as an MCP" idea from
  the notes, with no bespoke glue.
- **Data separation of concerns**: phone = edge/portal, DataBank = storage/processing, Forge =
  building — each on the right hardware.
- ShrinkMedia keeps its privacy-first default while gaining a real, opt-in path to the ecosystem.

**Negative / honest**
- This is a **multi-week, multi-repo, multi-account program** (self-host networking, OAuth,
  per-product MCP adapters, orchestrator). Not one session.
- The full vision needs **external accounts/keys/credentials** (cloud providers, or a reachable
  self-hosted endpoint) that do not exist yet.
- Working across 3+ codebases "at once" in a single session would tank quality; execution is
  explicitly **sequential per repo**, with the shared contracts defined first (this ADR).

## Alternatives considered

- **DataBank inside ShrinkMedia APK:** rejected — mobile can't host the orchestrator/server well,
  and it couples the phone app to the server.
- **Bespoke per-product glue instead of MCP:** rejected — not reversible, not tool-agnostic, and
  ignores the owner's own "connect as an MCP" note.
- **Phone as the brain:** rejected — the Redmi has no AICore and can't run the orchestrator;
  the home server / connected lane is required for that.

## Linking

ADR-010 (Google Bridge staged), ADR-011 (on-device AICore), ADR-012 (Connected mode),
`docs/personal-intelligence.md`, `docs/current-state.md` C17, the Forge build brief
(`Assets/forge-orchestrator-build-brief.md`), and the owner's notes (see
`docs/ecosystem.md` / note-OCR synthesis).
