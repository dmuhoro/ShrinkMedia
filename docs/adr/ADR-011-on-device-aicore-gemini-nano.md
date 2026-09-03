# ADR-011: On-Device Intelligence via AICore/Gemini Nano (Privacy-Preserving Default)

**Status:** Accepted (architecture)
**Date:** 2026-09-03
**Deciders:** Daniel Muhoro

## Context

ShrinkMedia is a private, on-device toolkit (Constitution Article II; NO INTERNET
permission in the shipped APK, enforced at the real merged-manifest boundary in CI).
ADR-010 staged AICore hand-off as the on-device AI ladder step:

> **flow (1) compression/PDF done → (2) OCR on-device (ADR-009) → (3) AICore on-device Gemini handoff (device-gated)**

Compression, PDF, and OCR are now real and device-verified. The product vision is to
become a "portal to the user's digital world" with **personal intelligence that works
offline AND online AND via the cloud** — but always with the private on-device default.

This ADR makes the on-device AI step concrete: integrate Google's **Gemini Nano** through
Android's **AICore** system service. Grounded facts (Android Developers, 2026-04-02; ML Kit
GenAI device list, 2026):

- AICore is a system service that **hosts, downloads, and updates** Gemini Nano; the app
  talks to it via ML Kit GenAI (or the AI Edge SDK). The model is **not** bundled in the APK.
- Inference runs **on-device** (LiteRT via NNAPI/NPU). No request leaves the device.
- The **first download needs network** (~1.7 GB, routed via Private Compute Services companion
  APK, not the app's INTERNET permission). After that, inference is offline.
- **Device-gated:** available on a growing but specific set (Pixel, Samsung Galaxy, Xiaomi,
  Motorola, OnePlus, OPPO, vivo, realme, Honor, etc.). Not every Android device. Availability
  **must be checked at runtime** and degrade gracefully.
- **Quotas + foreground-only:** AICore enforces per-app inference quotas and blocks inference
  from the background (`BACKGROUND_USE_BLOCKED`) — relevant to our foreground batch service.
- **Capabilities are limited:** Gemini Nano is small. Suited to summarization, proofreading,
  rewriting, and image description (ML Kit GenAI). It is NOT the full cloud Gemini agent.

## Decision

- Add an on-device AI capability built on **AICore + ML Kit GenAI** (the high-level, curl-free
  surface), gated on runtime availability.
- The **default remains fully on-device**: if AICore/Nano is unavailable or the user is
  offline and the model isn't downloaded, the AI assistant **does not** implicitly fall
  through to the cloud. It shows a clear "unavailable / download to enable" state (fail-closed:
  refusing is safer than silently uploading, per ALWAYS §5).
- Wrap access behind an `OnDeviceInferenceRepository` seam with a typed result, mirroring how
  `compressVideoFile` returns typed results. Availability check on model init; graceful
  fallback state surfaced to the UI.
- Cloud usage (ADR-012) is **separate, opt-in, and named Connected mode** — never the implicit
  fallback of this ADR.
- No INTERNET permission is added by this ADR. AICore's initial model fetch is handled by
  Private Compute Services outside the app manifest (the app keeps NO INTERNET).

## Consequences

**Positive:**
- Brings genuine on-device AI (summarization of PDFs/images, smart captions, content assist)
  fully offline with zero data leaving the device.
- Reuses ML Kit that ShrinkMedia already ships (OCR, ADR-009) — no new heavy dependency.
- Matches the product vision ("personal intelligence in our devices") while keeping the
  privacy identity that differentiates ShrinkMedia from forced-cloud tools.

**Negative / honest:**
- **Not every device has AICore/Nano.** UI must handle "AI not supported on this device"
  without degrading the core (which stays fully functional offline).
- First-time model download needs network + ~1.7 GB storage (handled by AICore).
- Nano's intelligence is small-model-limited by design; multi-modal/agentic behavior needs
  the ADR-012 connected layer.
- Inference is foreground-only and quota-limited — background batch AI must be designed around
  this (which pairs naturally with the existing foreground `BatchCompressionService`).

## Alternatives considered

- **Cloud Gemini as the only AI:** rejected as the default — breaks the privacy invariant and
  the ADR-010 fail-closed identity.
- **Bundling a model in the APK:** rejected — huge APK, no Google-managed updates, no NPU
  acceleration, storage cost. AICore owns the model lifecycle far better.
- **No AI:** rejected — the product vision wants AI; on-device-first is the correct way.

## Linking

ADR-010 (v2 ladder; AICore staged v2), ADR-009 (ML Kit OCR precedent), Constitution Article II
(privacy invariant), ADR-012 (Connected mode — the opt-in cloud complement), `docs/current-state.md`
C11 (transition from ASPIRATIONAL to a designed roadmap). See `docs/ideas.md` and the personal-
intelligence roadmap (`docs/personal-intelligence.md`).

This ADR is **architecture-only**: no code lands with it. Implementation is scoped as its own
future sprint with its own evidence gate (Constitution Article VII).