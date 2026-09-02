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

---

## I002 — "Make the app more lively" — liveliness idea radar

> Asked by the user alongside the Spr13 rename/delete work. This is a **staged
> radar**, not a commitment. Only the first item (first-run onboarding pointers)
> is implemented in v0.5.0; everything else is costed before it becomes a sprint.
> Every idea obeys the two invariants: **on-device, no INTERNET** (unless the row
> explicitly says ADR) and **no silent drops**.

### Tier 1 — Quick wins (no new dependencies, all within current stack)

| Id | Idea | Effort | Why it livens the app | Privacy |
|----|------|--------|------------------------|---------|
| L1 | **First-run onboarding pointers** ✅ (v0.5.0) | Done | 3 bit-sized habits: on-device privacy, where compressions land, free-space via Select/delete | None (on-device) |
| L2 | **Animated progress hero** — compression progress ring / confetti on batch done | S | Motion makes "waiting" feel alive (`LazyColumn` items already animate) | None |
| L3 | **Haptic taps + micro-toast polish** | S | Fire `HapticFeedback` (`VibrationEffect.createPredefined`) on compress/done; consistent snackbars | None |
| L4 | **Empty-state art** — friendly illustration + a "try an example" card when media library or recent is empty | S | Empty screens are the biggest "dead" feeling; a built-in sample JPEG (gitignored-gen, on-device) lets them try without leaving | None (bundled asset) |
| L5 | **Savings celebration card** — when lifetime savings crosses a threshold (100 KB / 1 MB / 10 MB), show a one-line milestone card + `updateSavingsMilestone` | S | Same DataStore counters, new "game" moment | None |
| L6 | **Live savings counter** (animated count-up) on the Media hero | S | Numbers that tick up feel alive; pure UI | None |

### Tier 2 — Meaningful (one existing dep or a plain OS API)

| Id | Idea | Effort | Why | Privacy |
|----|------|--------|-----|---------|
| L7 | **Micro-tutorials in-context** — contextual one-line pointers that appear above a control once (e.g. "Tip: enable Autosave to write to your gallery"), each gated by its own additive `Defaults` boolean | M | Same onboarding family, but delivered exactly where the action is; teach in flow, not a wall of text | None |
| L8 | **Recent-compressions revival** — a "compressed this week" histogram on the Documents/Media tab (count per day from `batch-audit.log` + savings) | M | Show momentum; all data already on-device | None |
| L9 | **Sheet peek of the PDF preview** — preview thumbnail + page-dots in the PDF card (PdfRenderer already used) | M | Docs tab stops feeling like a form; real visual feedback | None |
| L10 | **Pinned favorites** — star a compressed file so it stays on top of Recent (additive DataStore `favorite_path` list) | M | "My stuff" feelings = livelier product | None |

### Tier 3 — Ambitious (new dependency or an ADR)

| Id | Idea | Effort | Why | Privacy |
|----|------|--------|-----|---------|
| L11 | **On-device de-dup / "found X duplicates"** after a batch (file-size + perceptual-hash on-device) | L | The natural next step after delete: show what else could be freed | None (on-device hash), size cost |
| L12 | **Before/after "reveal" slider** for the just-compressed image | M | Drag to compare — the most persuasive live moment in a compactor | None |
| L13 | **PWA/web-sim parity for these** — mirror Tier-1 behaviours in `src/App.tsx` so the simulator stays a truthful preview | M | Web-sim is the marketing surface (ADR-006); parity keeps demos honest | None |

### Sequencing guidance (aligns with D001 "rapid-first, harden-second")

1. Tier 1 is a one-sprint bundle if wanted (L2–L6 are ~1–2 layers each, all pure
   Compose/DataStore — no new deps, fail-closed defaults, additive keys only).
2. L7 is the natural follow-up to the v0.5.0 onboarding card; it shares its
   "default-false additive flag" pattern.
3. L11/L12 are the highest "wow" but pair naturally as *delete → what-else-can-go*;
   stage them as their own sprint with a product decision on the hash threshold.
4. Any idea that would break the no-INTERNET invariant (e.g. remote fonts, analytics,
   cloud sync) is deliberately **excluded** unless a future ADR explicitly authorizes it.

### Implemented so far (v0.5.0)

- **L1** — first-run onboarding card ("Get to know ShrinkMedia": privacy / finding
  compressions / freeing space) with additive `ONBOARDING_DISMISSED`, fail-closed
  default `false`. See `docs/sprints/sprint-13-app-rename-media-delete-onboarding.md`
  and `docs/evidence/2026-09-02_app_rename_media_delete_onboarding.md`.
