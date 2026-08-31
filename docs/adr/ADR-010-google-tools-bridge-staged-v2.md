# ADR-010: Google Tools Bridge — Deliberately Staged as v2 (Kept Out of v1)

**Status:** Accepted
**Date:** 2026-08-31
**Deciders:** Daniel Muhoro

## Context

Product vision proposed wiring Google's umbrella of apps (Google Photos, Drive,
Docs, and Gemini as an in-app personal assistant) into ShrinkMedia, leveraging
that Android is on nearly every device. ShrinkMedia's current identity is a
**private, on-device toolkit** (Constitution Article II; the manifest declares
no INTERNET permission; CI enforces this). Two of the proposed surfaces
collide with that identity:

- **Google Drive / Docs** live in the cloud. Accessing them requires
  `android.permission.INTERNET` + OAuth — a constitutional change and a
  reframing from "private on-device" to "connected Google client."
- **Cloud Gemini** requires uploading user content to Google's servers.

**Scope decision (v1 vs v2):** v1's job is to prove and ship the on-device core
verify-ably. Bolting on OAuth + Drive/Docs/cloud-AI would push v1 out by weeks,
dilute the crisp "your private shrinker" story, and compete with Google's own
apps. The on-device AI ladder is: **flow (1) compression/PDF done → (2) OCR
on-device (ADR-009) → (3) AICore on-device Gemini handoff (device-gated)**.
That ladder is strictly on-device and is the correct v1/v2-next sequence.

## Decision

- **v1 (now):** on-device first. Ship verified compression, documents, and
  ML Kit OCR (ADR-009). No Drive/Docs/cloud-Gemini. Google **Photos** needs no
  bridge — Android's system picker / MediaStore already gives scoped access to
  the device photos the app already uses.
- **v2 (next, ADR-driven):** an optional, clearly-labelled **Google Bridge**
  that preserves on-device as the default:
  - **On-device AICore Gemini** as the assistant (privacy-preserving,
    device-gated, graceful fallback when AICore is absent).
  - **Phot L2: write-to-Photos** and **read-from-Drive/Docs** only as an
    *opt-in connected mode* — an explicit on/off that keeps the private
    default and adds INTERNET + OAuth only when the user turns it on.
- Any v2 work that adds INTERNET / OAuth / cloud must be its own ADR and must
  not change the default (fail-closed to on-device).

## Consequences

**Positive:**
- v1 ships as a finished, honest, *verified* product on a tight timeline.
- The privacy identity stays credible; the "bridge" becomes a differentiator
  (most tools are forced-cloud; ShrinkMedia is *privately flexible*).
- Clear, sequenced AI roadmap (OCR → AICore) with no false claims.

**Negative:**
- v1 does not include the ambitious Google-assistant wow factor.
- Real v2 effort is meaningful (OAuth + Drive/Docs SDK + cloud/on-device AI
  gating is realistically weeks of work) and must be ADR-planned.

## Alternatives considered

- **Full Google integration in v1:** rejected — weeks of delay, identity
  dilution, crowded pitch.
- **Never integrate Google:** rejected — the connected-assistant ambition has
  real value and fits as a deliberate opt-in v2.

## Linking

Constitution Article II (privacy invariant; any INTERNET change = ADR);
ADR-009 (OCR first); ADR-008; `docs/current-state.md` C11 (AICore remains
**ASPIRATIONAL**), C12 (OCR becomes REAL via ADR-009). See `docs/ideas.md`
for the full Google-Bridge concept.
