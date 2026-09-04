# ShrinkMedia — The Personal Ecosystem: DataBank → Portal → Personal Intelligence

> The "where are we going" design for turning ShrinkMedia into the **phone portal** of a
> personal ecosystem the owner controls end-to-end: his own data server (DataBank), his own
> products (Daftari, Hermes-Forge, Forge.ai, ShrinkMedia), all connected through one standard
> protocol (MCP), answering with his own personal intelligence — secure by every measure.
> This is the companion design to ADR-011 / ADR-012 / ADR-013.

---

## 1. Where this comes from (the owner's notes, OCR-synthesized)

The owner's handwritten notes, absorbed by running ShrinkMedia's own OCR pipeline on-device
(RAW + an enhanced contrast/brightness/upscale pass), revolve around six ideas:

1. **"How is a data server built / a storage facility for my data?"** → DataBank: storage he
   owns and controls, not a rented cloud vendor's bucket.
2. **"Own my hardware?"** (Mac mini + RTX 3090 home lab) → he wants the option to run the brain
   on hardware he owns; the phone alone can't host it.
3. **"Connect to every AI I use"** (Claude, ChatGPT, Brave, Chrome, Edge…) — and the key line,
   **"the ability to connect as an MCP"** (Model Context Protocol) → one standard connector, not
   bespoke glue per tool.
4. **Meta-cognition / attention / senses / circumstances** → the assistant should be present
   **everywhere**, and personal intelligence should be a *thinking* layer, not a chat box.
5. **Multi-agent systems = Planner / Builder / Reviewer**, with **"every agent turns the PRD +
   ADR into a searchable knowledge layer"** → the Forge orchestrator (RAG + agents + evals +
   safety + ML foundations), as in `Assets/forge-orchestrator-build-brief.md`.
6. **Roles / ML foundations / engineering team** → a compounding, measurable system with guards
   and evals — not a one-off feature.

The `forge-orchestrator-build-brief.md` document adds the concrete build: a deterministic state
machine (queued → retrieving → building → reviewing → merged / blocked), a Reviewer gate that
blocks bad diffs, evals that measure the Reviewer, safety guardrails (sandboxed builders, hard
file-scope enforcement, prompt-injection defense, rate/cost caps), and an explicit build order.

---

## 2. The honest answer to "can you work on three projects at once?"

**No — and forcing it would degrade quality.** I work one repo/workspace at a time. Trying to
simultaneously develop ShrinkMedia, Forge.ai, and Daftari in one session is the fastest way to
leave all three half-done (and half-done work violates your own no-unfinished-work rule).

What I **can** do, and what quality actually requires:
- Define the **shared contracts up front** (this ADR-013 + this doc + the MCP tool set), so the
  seams between products are nailed before I touch multiple codebases.
- Then **execute sequentially, one repo at a time**, landing and verifying each layer before the
  next. This is the exact "work in layers" rule (AGENTS §9) you already enforce.
- Wire the phone portal (**ShrinkMedia Connected mode**) as the *first* real hop, because it's the
  surface you asked to reach first and it's fail-closed by design.

So: ecosystem accessible via the phone? **Yes**, engineered as portal + connector + server.
Work on three at once? **Not honestly, no.** Quality is protected by sequential layering with
shared contracts, not by parallel multi-repo development in one session.

---

## 3. The architecture

```
                    ┌────────────────────────────── PHONE ──────────────────────────────┐
                    │                     ShrinkMedia (the portal)                       │
                    │  MediaCore (compress/PDF/OCR) ─ offline, always works              │
                    │  OnDeviceInferenceRepository (ADR-011) ─ on-device AI, fail-closed │
                    │  ConnectedRepository (ADR-012/013) ─ OFF by default, explicit      │
                    │         └─ MCP adapter: vault.put / vault.get / vault.query        │
                    └──────────────────────────────┬────────────────────────────────────┘
                                                   │ Connected mode (opt-in, No internet by default)
                    ┌──────────────────────────────▼────────────────────────────────────┐
                    │              DATABANK (self-hosted server, Mac mini/3090)          │
                    │   Object store + index ─ searchable knowledge layer (RAG)          │
                    │   Personal intelligence core (on-device Nano / self-host / cloud)  │
                    └──────┬──────────────────────────┬──────────────────────────┬───────┘
                           │ (MCP adapters — one standard protocol)              │
              ┌────────────▼────────┐   ┌────────────▼────────┐   ┌─────────────▼───────┐
              │ Daftari (BusinessOS) │   │ Hermes-Forge (offline│   │ Forge.ai (cloud AI  │
              │  + ai-context corpus │   │  builder, Ollama)    │   │  app-builder)       │
              └──────────────────────┘   └──────────────────────┘   └─────────────────────┘
                          ┌───────────────────────────────────────────────────┐
                          │  FORGE orchestrator (Planner/Retriever/Builder/  │
                          │  Reviewer/Ops + evals + safety) — builds & keeps │
                          │  every product above maintained                   │
                          └───────────────────────────────────────────────────┘
```

**Rules at every boundary (mirrors ShrinkMedia's Constitution):**
- **Fail closed:** Connected mode and every cloud/self-host action default to OFF/refused.
- **No silent drops:** every gateway returns a typed result; a rejected action gives the caller a
  reason + an audit record.
- **Enforcement at the real boundary:** the privacy guard is on the shipped default manifest; the
  connected variant is separately gated.
- **Ownership:** DataBank + Forge run on hardware the owner controls; cloud is invited, never
  assumed (ADR-010/011/012).

---

## 4. What each product contributes

| Product | Contributes | Connector | Status |
|---------|-------------|-----------|--------|
| **ShrinkMedia** | On-device toolkit + on-device AI; the phone portal | MCP adapter (`vault.*`) inside Connected mode | L1 this sprint (foundation) |
| **Daftari** | Real end-user product + `ai-context/` retrieval corpus | MCP adapter | Future (L3) |
| **Hermes-Forge** | Offline local Builder (agent engine, Ollama) | MCP adapter | Future (L3) |
| **Forge.ai** | Cloud AI Builder + orchestration host | MCP adapter | Future (L3) |
| **DataBank** | Self-hosted storage + index + processing | Server, anyway the MCP endpoint | To build (L2) |

---

## 5. Sequencing (do not parallelize)

1. **L1 (this sprint): C17 foundation in ShrinkMedia.** Additive `connected_mode` setting (OFF
   default, fail-closed), a `ConnectedRepository` returning typed results (no silent drops), and
   a consent UX. No INTERNET added; CI keeps asserting the default merged release manifest has no
   INTERNET.
2. **L2: DataBank (separate repo) server + transfer contract.** DataBank is a **separate
   project** — it is a server, not a mobile APK. It gets its own repo (same founder, independent
   lifecycle/toolchain/release) and shares only the **contract** (MCP `vault.put/get/query` +
   auth) with ShrinkMedia. Define the MCP tool set and the build flavor that would carry INTERNET
   behind explicit consent; stand up DataBank (self-hosted). *Requires the owner's
   hardware/network/credentials.*
3. **L3: MCP adapters** for Daftari / Hermes-Forge / Forge.ai; then the **Forge orchestrator**
   program (its own stack, per the build brief).
4. **L4: Forge product-factory core (the autonomous loop).** The always-on safe factory that
   builds/maintains the product line with oversight; networked "safety net" that catches failures
   and escalates to the owner.
5. **L5: Virtual-me / guardian / metacognition.** The "clone" as guardian of digital identity, a
   thinking-about-resolution layer, per-activity state and memory. **Research-grade; months of
   work; not a one-sprint deliverable.**

## 6. Definition of done for the ecosystem foundation

- The **default** ShrinkMedia build declares NO INTERNET and all tier-1/2 features work offline.
- Connected mode is OFF by default, with a first-run privacy disclosure; nothing silently
  connects.
- The connectable layer is **MCP** (not bespoke glue); a rejected action returns a typed reason
  + audit record.
- This ADR + design doc + roadmap are the shared contract the sequential per-repo work builds on.

## 7. The full vision — the Founder's Engine

This is the owner's stated end-state for the ecosystem (captured 04-Sep-2026 from the directive).
It is the guide for all future work. The layered plan in §5 is the honest, sequential path to it.

### 7.1 The end-state in one paragraph

A personal, self-owned intelligence stack where the owner is **one Founder** controlling every
piece: a **portal** on his phone (ShrinkMedia) into his own **DataBank** (storage + index + RAG
he hosts on his own hardware), connected by one standard protocol (**MCP**), with a **Forge
orchestrator** acting as an autonomous product factory — Planner/Buider/Reviewer + evals + safety
— that builds and maintains his product line (Forge.ai, Hermes-Forge, Daftari, ShrinkMedia,
EasyTutor, and beyond). Around that factory sits a **virtual me / guardian** and a
**metacognition layer** that turns the whole thing into a compounding, self-aware personal
intelligence that improves the owner (a "renaissance polymath" loop), not just the software.

### 7.2 The pillars (each maps to a buildable system)

1. **The autonomous product factory (Forge effective).** Owner gives intent → Planner turns it
   into a PRD/ADR → Builder builds → Reviewer gates quality → Ops merges/keeps maintained.
   Full build in `Assets/forge-orchestrator-build-brief.md`.
   - **Real-time preview panel:** a live view of *what is being built right now*, with feedback
     surfaced to the owner.
   - **The safety net:** a networked guard rails system that catches problems, issues, and
     failures across products, alerts the virtual me, and escalates to the owner for a decision
     when required. No silent drops; failures are explicit and audited.
   - **Virtual-me oversight:** the virtual me is connected to every agent, watching what they do
     and correcting them if they diverge from the end-goal.
2. **Virtual me (the digital identity guardian).** A "clone" of the owner that is the guardian
   and keeper of his digital identity: consistent values, memory, and decision-governance across
   every agent. It is the connective tissue the factory and the portal report into.
3. **Metacognition & the thinking/architect modes.** A dedicated workspace that supports
   **think** and **architect** modes, and creates a PRD that gives clarity on what is being
   built. The system should reason *about* the work and the owner's goals, not just execute
   prompts.
4. **The polymath knowledge base (apprenticeship).** A knowledge layer that can be trained on
   the work of polymaths, engineers, scientists, artists, architects, and mathematicians — from
   medieval to the current day — saved as **skills** invoked when a situation demands them. This
   connects to the brain of the virtual me so it learns emerging technology and reaches
   mastery-level on it.
5. **Life-as-a-system loop.** The daily processes of thinker / architect / builder are
   systematized to increase output, delivery of high-quality work, and work ethic ("think more,
   do less" → "increase the speed, intensity, effectiveness, and consistency of execution").
   Reverse-engineering feedback: build an idea start-to-finish, then learn *what was done, why it
   worked, what worked and what didn't*, and raise awareness/knowledge in that topic (see
   EasyTutor).

### 7.3 What can and cannot be automated (honest boundary)

- **Can automate (buildable, milestone-gated):** the factory loop (Planner/Buider/Reviewer/Ops),
  the safety net, preview panel, RAG knowledge retrieval, skills library, evals and guards,
  feedback loops, and the portal connection.
- **Cannot truly automate (research-grade, long tail):** a genuine independent "clone" with its
  own identity, judgment, and self-direction; true cross-agent autonomy without oversight; and
  "mastery-level" general learning. These are aspirational until proven; per the Constitution
  they stay ASPIRATIONAL until implemented *and* verified. We do not claim them before they run.

The engine metaphor the owner used in mechanical engineering — **oil change, routine/periodic
maintenance, modifications** — is the operating doctrine: the factory is *maintained*, not left
to rot; trial-and-error (evals + guards) is how we know what to keep and what to drop; and
modifications (new MCP tools, new skills) are how it grows.

## 8. Scale, schedule (80/20), and compute — honest answers

### 8.1 Duration applying the 80/20 principle (sequential, one layer at a time)

| Layer | Deliverable | Focused time | % of daily-operational value |
|------|-----------|--------------|------------------------------|
| L1 | ShrinkMedia Connected-mode foundation (portal seam) | this sprint | unlocks the portal |
| L2 | DataBank server (separate repo): object store + index + `vault.*` MCP + auth | **2–3 wks** | ~80% value for ~20% effort (the working vault) |
| L3 | MCP adapters (Hermes-Forge / Daftari / Forge.ai) | ~1 wk each, sequential | full wiring |
| L4 | Forge product-factory core (build brief steps 1–3: state machine + Builder + Reviewer gate) | **3–4 wks** | the "product factory" |
| L5 | Evals + guards hardened + dashboard/safety-net (steps 4–6/8) | 3–4 wks | safe, measurable autonomy |
| L6 | Virtual-me / guardian / metacognition | **months; research-grade** | the long tail |

**80/20 conclusion:** roughly the **first two months** (L1–L4 — the working phone portal, a real
self-hosted vault, a trustworthy Forge loop across the three live products) delivers most of the
daily-operational value the owner can run his operation on. Everything from L5/L6 onward is
compound leverage, built on those rails; the "virtual-me guardian + fully autonomous factory +
polymath mastery" is explicitly the long tail and not promised on a one-sprint date.

### 8.2 Compute the owner needs

- **ShrinkMedia** runs **on-device** (no server GPU). Daftari/Forge.ai are already cloud-hosted
  (Railway/Supabase/Vercel — low GPU, manageable).
- **Hermes-Forge** (offline local agent): a local model host — Mac M-series or a GPU with
  **8–16 GB VRAM** (RTX 3060/4060 class).
- **DataBank + Forge orchestrator** (owner's Mac mini + RTX 3090 home lab): an **RTX 3090
  (24 GB VRAM)** genuinely runs a strong open-weight model (30 B full, or 70 B at low precision)
  plus RAG embeddings/rerank on one box. A Mac mini (Apple Silicon, ≥32 GB) is a good low-power
  host for DataBank storage + orchestration + the DB.
- **Honest floor:** one GPU box (RTX 3090+, 24 GB VRAM) + one always-on low-power host (the Mac
  mini) ≈ **$2,500–4,500**, the 80/20 sweet spot. The full polymath-scale knowledge base (large
  finetuning, millions of docs) needs more (a second GPU or cloud) but is not a day-one need.

## 9. How this doc / ADR-013 guides the work

- The expandable top-level pillars map directly to ADR-013 + the Forge build brief; nothing here
  changes the **fail-closed, no-INTERNET-by-default, on-device-privacy** invariants.
- DataBank stays a **separate repo** (server), ShrinkMedia the **portal**; they talk only via the
  shared **MCP contract** (§5 L1/L2).
- Every deliverable above is sequenced; nothing is run in parallel. Evidence is produced at each
  step per the Constitution (§11).

## 10. Related

ADR-010, ADR-011, ADR-012, ADR-013; `docs/personal-intelligence.md`; `docs/current-state.md`
(C11/C17); `Assets/forge-orchestrator-build-brief.md`; owner's notes (note-OCR log).
