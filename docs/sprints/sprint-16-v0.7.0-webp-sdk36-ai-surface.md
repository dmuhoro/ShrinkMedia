# Sprint 16 — WebP + SDK36 + On-Device AI Surface + Privacy (v0.7.0)

**Status:** EXECUTED (2026-09-03)
**Scope:** native release v0.7.0 — the **WebP output option**, **API-36 toolchain**
(compiling/shipping at the current Play-store target), the **real on-device AI
surface** (ADR-011 via ML Kit GenAI / Gemini Nano, build-verified and fail-closed),
and the **keystore off-machine backup runbook**.

## Focus

1. **SDK 36 (L2)**: bump compile/target SDK to API 36 (AGP 8.9.1, Gradle 8.11.1) —
   the level the Play Store now requires — with the full suite still green.
2. **WebP (L3)**: make WebP a selectable image format in the UI + batch, additive
   (`compressImageFileAsWebP`, lossy-default fail-closed).
3. **On-device AI surface (L4)**: the sequenced ADR-011 path — Kotlin 2.2 (L4a, so
   ML Kit GenAI metadata links), AGP 8.10 (so R8 parses that metadata), and the real
   `OnDeviceInferenceRepository` + Elite AI UI panel (L4b).
4. **Keystore backup (L6)**: a verified off-machine backup checklist anchored to the
   live keystore checksum.

## What shipped and what is honestly proven

| Layer | Change | Proof |
|-------|--------|-------|
| L2 | SDK 36 (AGP 8.9.1→8.10.0, Gradle 8.11.1) | compile/unit/lint green; on-device install + launch (L1) |
| L3 | WebP in image UI + batch | 7 tests 0 fail; lint 0 errors |
| L4a | Kotlin 2.0.21→2.2.0 | full suite green (12 tests 0 fail, lint 0 errors) |
| L4b | GenAI repo + Elite AI panel + tests | 12 tests (5 new), lint 0 errors, release shrinks clean **no R8 kotlin-metadata warning** |
| L6 | Keystore backup runbook | SHA256 anchor computed on live keystore |

## Honest status (what is NOT yet proven)

- **On-device AI inference is NOT Nano-proven (L5).** The real ML Kit GenAI path was
  **run on the connected Redmi API-36 test handset**: `checkStatus()` returned
  **`UNAVAILABLE`** — the device has no AICore/Gemini Nano runtime (non-Nano SoC). The
  gate failed closed correctly (no crash, no false AVAILABLE, no INTERNET), which is
  the honest non-Nano wall L5 expected. A real Gemini Nano inference still requires a
  **Nano-capable device** (Pixel-8-class / S24-class); until one runs the real path,
  the AI surface is build-verified + on-device-probed but **not** claimed PASS on
  hardware (AGENTS §11, Constitution Art. VII).
- **C5 batch device run** is still physically blocked by device storage.
- **Keystore off-machine copy** is a human step not yet performed (runbook ready).

## Privacy & fail-closed invariants held

- Merged release manifest declares **no `android.permission.INTERNET`** (GenAI lib
  surface is on-device, `tools:node="remove"` guard + CI enforced).
- AI card never falls to the cloud; below API 26 it refuses with API_TOO_OLD.
- Every compress/autosave path returns a typed result or `null`; callers surface it.

## Evidence

- `docs/evidence/2026-09-03_sdk36_toolchain_bump.md` (L2)
- `docs/evidence/2026-09-03_webp_in_image_ui.md` (L3)
- `docs/evidence/2026-09-03_kotlin_2_2_bump.md` (L4a)
- `docs/evidence/2026-09-03_adr011_on_device_ai_surface.md` (L4b)
- `docs/evidence/2026-09-03_on_device_ai_verification_l4c_l5.md` (L5: real on-device `checkStatus()` → UNAVAILABLE)
- `docs/evidence/2026-09-03_keystore_off_machine_backup.md` (L6)

## Outstanding (explicit, not hidden)

- L4c: reach the AI **tab panel** interactively for a screenshot (blocked on this
  Xiaomi build: `adb shell input tap` denied INJECT_EVENTS); the panel logic itself
  is verified via compile + lint + the real on-device repository probe above.
- L5: Gemini Nano/AICore on-hardware **inference output** (needs a Nano-capable device).
- C5 batch device run (storage-bound).