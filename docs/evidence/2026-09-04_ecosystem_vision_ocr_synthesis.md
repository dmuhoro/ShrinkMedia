# Evidence — Personal-ecosystem vision + DataBank/Forge design (ADR-013 + ecosystem doc)

**Date:** 2026-09-04
**Status:** PASS (design) — the owner's ecosystem vision, absorbed from handwritten notes via the
app's own on-device OCR pipeline, is synthesized into a concrete, sequenced architecture
(ADR-013 + `docs/ecosystem.md`) that answers the owner's explicit questions and guides all future
work. Honest limit: this is the **design + Layer-1 foundation**; the DataBank server and the
connected (INTERNET) variant are L2+ and need real hardware/credentials.

## Note OCR absorption (the app's own OCR pipeline, on-device)

The owner's handwritten notes were passed through ShrinkMedia's real `OcrHelper` on the handset
(a RAW pass + an enhanced contrast/brightness/2×-upscale pass). The RAW + ENHANCED output
(492-line log) surfaced these thematic threads, which became the design's pillars:

1. "How is a data server built / a storage facility for my data?" → **DataBank**.
2. "Own my hardware?" (Mac mini + RTX 3090 home lab) → self-hosted brain option.
3. "Connect to … Claude, ChatGPT, Brave, Chrome, Edge" + **"the ability to connect as an MCP"**
   → the connectable layer is **MCP**, not bespoke glue.
4. Meta-cognition / attention / senses / "assistant everywhere" → a thinking layer, not a chat box.
5. Multi-agent **Planner / Builder / Reviewer**; "every agent turns the PRD + ADR into a
   searchable knowledge layer" → the **Forge orchestrator**.
6. Roles / ML foundations / engineering team → a compounding, measurable system with guards+evals.

These corroborate and extend the earlier OCR pass and the `forge-orchestrator-build-brief.md`.

## The founder's directive (2026-09-04) is now documented in the design

- **Answer to "can you work on three projects at once?"**: No — one repo/workspace at a time;
  shared contracts first (ADR-013 + MCP), then sequential per-repo execution. Quality is
  protected by layering, not by parallel multi-repo work. (Recorded in `docs/ecosystem.md` §2.)
- **DataBank architecture decision**: a **separate project/repo** (it is a *server*, not an APK) —
  independent lifecycle/toolchain/release, sharing only the MCP `vault.*` contract with
  ShrinkMedia (the portal). Chosen for clarity over building a server inside the mobile app.
  (ADR-013 decision §1.)
- **Scale / schedule (80/20)**: L1–L4 ≈ the first ~2 months delivers most of the
  daily-operational value (portal + self-hosted vault + trustworthy Forge loop); the
  virtual-me/guardian/autonomous-factory/polymath-tail is months +
  research-grade. (`docs/ecosystem.md` §8.1.)
- **Compute floor**: one GPU box (RTX 3090+, 24 GB VRAM) + one low-power host (Mac mini ≥32 GB)
  ≈ $2,500–4,500; ShrinkMedia stays on-device with no server GPU. (`docs/ecosystem.md` §8.2.)
- **Founder's Engine pillars**: autonomous product factory + real-time preview + safety net +
  virtual-me oversight (Forge); virtual-me/identity guardian; think/architect modes + PRD clarity;
  polymath apprenticeship knowledge base (skills); life-as-a-system + reverse-engineering feedback
  loop. Honest **can/cannot automate** boundary recorded (`docs/ecosystem.md` §7.3).

## What shipped / documents

- `docs/ecosystem.md` — the "Founder's Engine" design doc (vision, architecture, sequencing,
  schedule, compute, can/cannot-automate).
- `docs/adr/ADR-013-personal-ecosystem-databank-portal.md` — binding design decision (DataBank =
  separate repo; MCP connectable layer; portal/Forge roles; sequencing L1→L5; expanded vision).
- `docs/current-state.md` — C17 → foundation-implemented; **C21** ecosystem-design row added.

## Cross-reference
- Sprint: `docs/sprints/sprint-18-v0.7.1-connected-foundation-ecosystem.md`
- Evidence: `2026-09-04_c17_connected_foundation.md` (the L1 seam that starts executing this design)
- Current state: `docs/current-state.md` C17 + C21.
