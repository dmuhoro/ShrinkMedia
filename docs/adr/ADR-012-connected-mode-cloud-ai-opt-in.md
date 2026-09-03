# ADR-012: "Connected mode" — Opt-In Cloud AI & Google Bridge (Opt-In INTERNET)

**Status:** Accepted (architecture, gated)
**Date:** 2026-09-03
**Deciders:** Daniel Muhoro

## Context

The product vision is a "portal to the user's digital world" with personal intelligence that
works **offline, online, and via the cloud**. ADR-011 delivers the private on-device default
(Gemini Nano/AICore). Cloud-grade capabilities (full Gemini vision, code, agents, and bridging
into Drive/Docs) require **network access** — which is a **constitutional change**:

- Constitution Article II / AGENTS §2: the shipped APK declares NO INTERNET; CI enforces it on
  the merged release manifest at the real boundary.
- ADR-010 already staged this as an **opt-in "connected mode"** that "adds INTERNET + OAuth only
  when the user turns it on", keeping the private on-device default fail-closed.

**Grounding (2026):** Cloud Gemini (Google AI Studio / Gemini API) offers full multimodal
reasoning, arbitrary tool use, and agents — capabilities Gemini Nano does not have (ADR-011).
But using it requires sending user content to Google's servers. Daftari (referenced by the
owner) failed offline because it was a thin cloud contracture; ShrinkMedia's advantage is that
**offline + on-device works by default**, and cloud is a *supplement*, never the only path.

## Decision

- Introduce a **single, clearly-labelled, OFF-by-default setting: "Connected mode".**
  - Default: **OFF** (fail-closed). The app ships with NO INTERNET permission and full
    on-device functionality (compression, PDF, OCR, AICore Nano) works completely offline.
  - When the user explicitly enables it, the app may declare/acquire INTERNET and opt into a
    **cloud Gemini** lane and (later) a **Google Bridge** (Drive/Docs/Photos write) via OAuth.
- Enabling Connected mode is a **first-run, explicit, informed consent** action surfaced in the
  app + a prominent privacy disclosure (`ShrinkMedia stays private by default. Connected mode
  uploads selected content to Google so you can use cloud AI and bridge your Google account.`).
- INTERNET is **not added to the base manifest**. It is delivered as a **runtime-granted
  overlay or a separate release flavor** so the privacy-manifest guard in CI still passes for
  the privacy-preserving build. Design choice TBD at implementation; the invariant is that the
  **default build must declare NO INTERNET**.
- Enablement is **per-session capability, not a silent fallback**: no code path may use the
  cloud unless Connected mode is ON. ADR-011's on-device result is never replaced by a cloud
  call without the user's explicit selection at runtime.
- No Google content (file bytes, images, OCR text) is sent unless the user has both (a)
  enabled Connected mode, and (b) invoked a specific connected action.

## Consequences

**Positive:**
- Unlocks full Gemini (vision/code/agents) and the Drive/Docs bridge — the ambitious "portal"
  — while keeping the product's privacy-first identity for the vast majority who stay offline.
- Differentiator: most tools are forced-cloud; ShrinkMedia is *privately flexible*.

**Negative / honest:**
- Constitutional change — an explicit ADR is required (this is it). Requires CI/CI artifacts
  changes to keep the default build INTERNET-free while supporting a connected variant.
- Google OAuth + Drive/Docs SDK + cloud-AI gating is a non-trivial, multi-week engineering
  effort (secure token storage, one-tap consent, permission scopes), realistically a dedicated
  program, not a single sprint.
- Larger test matrix: must preserve offline behavior when Connected mode is off.
- The connected build diverges from the current "no INTERNET" release; the split must be
  sequenced to avoid shipping a silent INTERNET-enabled APK. CI merged-manifest guard must be
  updated to assert the **default** (privacy) variant carries no INTERNET.

## Alternatives considered

- **Add INTERNET unconditionally:** rejected — violates the invariant and the differentiate.
- **Hybrid default:** rejected — "on-device by default" must be the honest default; silently
  auto-enabling cloud would break trust.
- **Never connect:** rejected — the portal/agent vision needs the connected lane.

## Linking

Constitution Article II + AGENTS §2 (INTERNET change requires ADR — this ADR); ADR-011 (the
on-device default this complements); ADR-010 (v2 ladder / Google Bridge staged); `docs/current-state.md`
C11/C17 (both transition from ASPIRATIONAL to a designed, ordered path); personal-intelligence
roadmap (`docs/personal-intelligence.md`).

This ADR is **architecture-only**, like ADR-011: no code lands with it. The connected-mode
build is a separate, sequencing-planned sprint with its own evidence gate.