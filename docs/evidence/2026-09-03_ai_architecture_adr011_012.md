# Architecture Evidence — ADR-011/012 Grounding Research (2026)

Date: 2026-09-03 · Sprint 15 · Owner: Daniel (Lead)

## What this covers

Layer 3 (personal-intelligence architecture): the grounding evidence for ADR-011 (on-device
AICore/Gemini Nano) and ADR-012 (opt-in Connected mode). Architecture-only — no code lands.

## Sources (websearch, 2026)

1. **Android Developers — "Gemini Nano | AI"** (developer.android.com/ai/gemini-nano, 2026-04-02):
   AICore is a system service hosting Gemini Nano; manages model distribution/updates; inference
   on-device; Restricted Package Binding; **AICore has no direct internet access** — model
   downloads route through the Private Compute Services companion APK. Enforces per-app quotas;
   inference **foreground-only** (`BACKGROUND_USE_BLOCKED`, PER_APP quota exceeded codes).
2. **Android Developers — "Google AI Edge SDK" / ML Kit GenAI**: Gemini Nano currently available
   **experimentally on Pixel 9 series** + a broad set (see ML Kit GenAI device list: Pixel 9/10/11,
   Samsung Galaxy S25/S26, Xiaomi 14/15/17, OnePlus, OPPO, vivo, Motorola, Honor, realme, iQOO,
   Lenovo, Sony, Sharp). Version-specific (nano-v2/v3/v4). **Not every device.**
3. **Google AI Edge / third-party engineering notes (xckevin.com, 2025-05)**: AICore is an APEX
   system service; model not bundled — downloaded on demand (~1.8 GB, deltas via bsdiff);
   availability gated on RAM/ NPU/CPU; Support for LoRA adapters (e.g. financial analyst on a
   40 MB LoRA). `com.google.ai.edge.aicore` / `com.google.android.aicore` deps, min SDK 31+.
4. **ML Kit GenAI (developers.google.com/ml-kit/genai, 2026)**: Feature-specific APIs
   (Summarization, Proofreading, Rewriting, Image Description) and Prompt API sit on top of
   AICore/Nano; device list as above; context-limited (Nano is small — long prompts truncate/lose
   quality vs cloud).

## Key facts locked into the ADRs

- On-device AI is **device-gated** → must `checkAvailability()` and degrade gracefully.
- First model download needs **network** (~1.7 GB) handled by **Private Compute Services**, NOT
  the app's INTERNET permission → the default app can stay no-INTERNET.
- Inference is **foreground-only + quota-limited** → pairs with the existing foreground batch
  service; no background AI inference without accounting.
- Nano is **small-model-limited** → cloud Gemini (vision/code/agents) is a separate opt-in lane
  (ADR-012), never an implicit fallback.
- Cloud usage requires a **constitutional change** (INTERNET) → ADR-012 is that explicit gate,
  default OFF, per-action consent.

## Observed results (as asserted in each ADR)

- ADR-011: on-device AICore integration architecture — accepted (architecture only, no code).
- ADR-012: opt-in Connected mode / cloud-AI INTERNET change — accepted (architecture only).
- `docs/personal-intelligence.md`: the 3-mode (offline/online/cloud) build + capability ladder.

## Honest note

Architecture-only means NO runtime evidence yet (no AICore device test, no cloud call). These are
**designed** and will become verified in their own sprints with on-device/CI gates, consistent
with Constitution Article VII and AGENTS §12 ("a capability marked ASPIRATIONAL stays ASPIRATIONAL
until implemented AND verified").