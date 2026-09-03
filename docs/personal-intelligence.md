# ShrinkMedia — Personal Intelligence & the Offline / Online / Cloud Architecture

> Purpose: the "where are we going" design for making ShrinkMedia a *portal to the user's
> digital world* with personal intelligence that works **offline, online, and via the cloud** —
> while keeping the private on-device default. Companion to ADR-011 (on-device AICore) and
> ADR-012 (opt-in Connected mode). Read before building the connected/AI program.

## 1. The core architectural truth (in the cooking analogy you asked for)

There are three fundamentally different ways an app can "exist". They are not interchangeable,
and most products quietly pick the worst one.

| Product type | Cooking analogy | Works offline? | Whose data? |
|--------------|-----------------|----------------|-------------|
| **Web app / thin client** (many SaaS, incl. Daftari) | A **restaurant**: your dish is cooked in *their* kitchen (a server you don't control). The line between you and the food is a delivery driver (the network). If the driver doesn't show, you get nothing — even if you have all the ingredients at home. | **No** (dies without network) | Theirs (server) |
| **Installable app, cloud-backed** | A **meal-kit box**: raw ingredients (code/data) live in *your* fridge, but the recipe/instructions still come from the vendor's server, and some components spoil or need a check-in (licenses, keys, sync). Delete the box and it's gone. | **Partial** | Mixed (yours + sync) |
| **On-device-installed → local intelligence** (what we build) | A **recipe book + your own full pantry + a chef in your home** (Gemini Nano/AICore). Everything — ingredients, recipe, chef — is local. It cooks offline, forever. The cloud is a *separate, invited guest chef* you call only when you want their specialty. | **Yes, by default** | Yours (local) |

ShrinkMedia today is the **third kind** for its core: compression, PDF, OCR (ML Kit), and now
ADR-011's Gemini Nano all run with **no INTERNET**. That is the foundation, not a limitation.

**Why Daftari failed offline (and how we avoid it):** Daftari was (per the owner) a thin cloud
contract — *kind #1* — so it stopped working without a server. ShrinkMedia must be *kind #3* for
everything that should always work, and only *invite the cloud* (kind #2) as an explicit opt-in.

## 2. The three operating modes we are engineering for

1. **OFFLINE (default — real today, grows with ADR-011):**
   Full compression, PDF tools, OCR, and on-device AI (summarize/extract from PDFs & images).
   Zero network. Ships with NO INTERNET permission.
2. **ONLINE (the bridge — ADR-012, opt-in):**
   A clearly-labelled **Connected mode** (OFF by default) that adds INTERNET + cloud Gemini +
   OAuth only when the user turns it on and invokes a connected action. The offline core never
   depends on it.
3. **Via the cloud (the portal — ADR-012, opt-in, per-action):**
   Full Gemini vision/code/agents and (later) Drive/Docs/Photos bridging. Sends selected content
   to Google **only after explicit consent at run time**.

The golden rule (ADR-010/011/012, Constitution Article II): **on-device is the default and the
fallback; the cloud is invited, never assumed.**

## 3. The personal-intelligence architecture (target)

```
┌────────────────────────────── ShrinkMedia App ──────────────────────────────┐
│  MediaCore (compression / PDF / OCR / gallery)   <- offline, today           │
│         │                                                                     │
│         ▼                                                                     │
│  [ OnDeviceInferenceRepository ]  (ADR-011)                                  │
│   └─ ML Kit GenAI -> AICore -> Gemini Nano  (summarize / captions / assist)   │
│         │                                                                    │
│         └── availability check -> UI "AI supported / download to enable"      │
│                                                                               │
│  [ ConnectedRouter ]  (ADR-012, SELD, OFF by default)                        │
│   └─ a user action explicitly marked "use cloud AI"                          │
│        └─ CloudGemini (Google AI / Gemini API)  <- only consenting actions    │
│        └─ GoogleBridge (Drive/Docs/Photos)  <- future, separate ADR           │
│                                                                               │
│  Single source of truth settings: SettingsDataStore (additive, fail-closed)   │
└──────────────────────────────────────────────────────────────────────────────┘
```

Key rules at every boundary:
- **Fail closed** (AGENTS §5): defaults refuse. `Connected mode` defaults OFF; `AI` shows
  "device not supported / model not downloaded" instead of silently calling the cloud.
- **No silent drops** (§4): if offline AI is unavailable, the UI says so explicitly (and there
  is an audit/metric record, consistent with the audited batch path).
- **Enforcement at the real boundary** (§2): the INTERNET guard is on the **shipped manifest**,
  and the connected build is separately gated so the default build never carries INTERNET.

## 4. Honest capability ladder (what is possible vs not)

| Tier | What it does | Status |
|------|--------------|--------|
| 1. Compression / PDF / OCR | Fully offline, real, device-verified | **DONE** (v0.6.0) |
| 2. On-device Nano AI (summarize, proofread, captions, describe PDF/image) | Offline, private | **Designed (ADR-011)** — implement as its own sprint |
| 3. Connected mode cloud AI | Vision/code/agents | **Designed (ADR-012)** — opt-in, multi-week program |
| 4. Google Bridge (Drive/Docs/Photos) | Portal to Google-world | **Designed (ADR-010/012)** — future, opt-in, OAuth |

**What is NOT honestly achievable today:** a *phone-only* Gemini Nano delivering full
cloud-Gemini-class reasoning or arbitrary tool use — the model is small by design. That is why
the connected lane (tier 3) exists *as opt-in*, not as a silent fallback. Anyone claiming
"fully local agentic AI on any phone in one sprint" is not being straight with you.

## 5. Sequenced build order (for the next passes)

1. **Sprint A — On-device AI (ADR-011):** dependency + `OnDeviceInferenceRepository` + runtime
   availability check → "AI for PDF/image" summarization, offline. Evidence: on-device test on an
   AICore-capable device; graceful "unsupported" on our Xiaomi (API 36) if no Nano.
2. **Sprint B — Connected mode foundations (ADR-012):** settings key (additive, default OFF),
   consent UX, and a **separate build/CI variant** so the default APK stays INTERNET-free while
   the connected variant is testable. Update the CI merged-manifest guard to assert the **default**
   variant carries no INTERNET.
3. **Sprint C — Cloud Gemini lane + (later) Google Bridge:** Gemini API integration behind
   Connected mode; then OAuth + Drive/Docs.

## 6. Definition of done for the AI program

- The **default (offline) build** ships NO INTERNET and all tier-1 + tier-2 AI works with no
  network (Constitution Article VII: evidence of an on-device test).
- Every AI surface has a typed result and an explicit "unavailable" UI state; nothing silently
  falls through to the cloud.
- Connected mode is OFF by default and there is a first-run privacy disclosure.
- CI enforces the default-build no-INTERNET invariant on the merged manifest (real boundary).

## 7. Related

ADR-010 (v2 ladder), ADR-011 (on-device AICore), ADR-012 (Connected mode), ADR-009 (ML Kit OCR),
Constitution Article II / AGENTS §2 (privacy invariant), `docs/current-state.md` C11/C17.