# ShrinkMedia — Ideas Radar (living)

> Non-binding concept notes, separate from committed decisions (`docs/decisions.md`)
> and formal ADRs (`docs/adr/`). An idea here is **not** a commitment to build;
> it is staged and costed before becoming an ADR + sprint work.

## I001 — Google Tools Bridge (concept, staged as v2; see ADR-010)

**The vision:** make relevant Google surfaces reachable from inside ShrinkMedia —
Google Photos, Drive, Docs, and Gemini as an in-app personal assistant —
leveraging that Android ships on virtually every device.

**Reality check (how each maps):**

| Google surface | Doable? | How | Privacy impact |
|---|---|---|---|
| Google **Photos** | Already handled | Android system picker / MediaStore already gives scoped read + write to device photos the app uses. No bridge needed. | None (scoped) |
| On-device **Gemini (AICore)** | ✅ High value, device-gated | `com.google.ai.client.generativeai` against the on-device model + `ModelDownloadService` availability check; graceful fallback | None (on-device) |
| **Drive / Docs** | ⚠️ Doable, invasive | OAuth + Google Drive/Docs API + `android.permission.INTERNET`; a constitutional change (Article II) | High — user data moves to Google cloud |
| **Cloud Gemini** | ⚠️ Doable, invasive | Gemini API with key; uploads content | High — uploads user files |

**Sequencing rule:** on-device first (OCR → AICore). The connected Drive/Docs/
cloud-AI mode, if ever built, is a **clearly-labelled opt-in** that keeps the
private on-device default (fail-closed). Not in v1.

**Why it's exciting (and what it says):** it sees ShrinkMedia as a layer atop the
world's most-installed platform and pairs an on-device privacy product with
optional Google reach. The *discipline* of staging it (v2, opt-in, ADR-driven)
is the stronger signal than shipping it half-baked in v1.

## Open questions to resolve before any v2 commitment

- Does the connected mode undermine the "your files never leave your device"
  marketing, or is the opt-in clear enough?
- OAuth scope: read-only Drive + Docs, or write too? For a "personal
  assistant" we likely want read + AICore summarize, not blind write.
- Which devices matter for AICore (currently Pixel/Samsung Gemini-capable)?
  The fallback matters more than the feature for a broad release.
