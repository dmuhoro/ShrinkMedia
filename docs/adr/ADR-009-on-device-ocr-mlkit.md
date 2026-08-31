# ADR-009: On-Device OCR via ML Kit Text Recognition

**Status:** Accepted
**Date:** 2026-08-31
**Deciders:** Daniel Muhoro

## Context

The Elite AI tab's core promise is "turn a scan into text." Today the app only
extracts **embedded** PDF text via a heuristic stream parser (ADR-008); scanned
(image-only) documents are honestly reported as "needs OCR." To make the
"Elite AI" tab actually read scans, we need a real OCR engine. Any engine we
add must preserve the on-device privacy invariant (Constitution Article II): no
`android.permission.INTERNET`, no uploads.

## Decision

Use **Google ML Kit Text Recognition** for on-device OCR:

- Image/PDF-page bitmaps are decoded to `Bitmap` locally and passed to ML Kit's
  on-device recognizer (`com.google.mlkit:text-recognition`).
- ML Kit runs **fully on-device** (Android's bundled ML models, no network, no
  INTERNET permission) — it does not disturb the on-device invariant.
- Add the dependency `com.google.mlkit:text-recognition:16.0.1` (or the current
  stable). This is a new dependency, justified by the concrete gap: no existing
  dependency (FFmpegKit, Coil, DataStore, `android.graphics.pdf`) provides OCR.
- Extraction returns a typed `String?` / status: on success the recognized
  text; on a decode/recognize failure an explicit message — never a silent
  empty string pretending success (Constitution III, I.6).
- ML Kit's optional downloadable language models fall back to bundled models;
  the app ships working Latin-script OCR out of the box.

## Consequences

**Positive:**
- Scans actually become searchable/editable text — the Elite AI tab's core
  value becomes real, on-device, private.
- No INTERNET permission, no account, no uploads — the identity is preserved.
- Well-documented, actively-maintained Google API; low implementation risk.

**Negative:**
- Adds a ~AAB-weight ML dependency; must verify R8/proguard keeps ML Kit.
- OCR quality varies with image quality and handwriting; results may need
  correction and the UI should present them as raw text (no false "structured"
  confidence).
- Downloadable-language models require explicit opt-in if users want them
  (out of scope for v1; bundled Latin model is the default).

## Alternatives considered

- **Tesseract via tess-two / tess4j:** heavier JNI/build complexity, worse
  result quality than ML Kit, harder to keep green with R8. Rejected.
- **Cloud OCR APIs:** violate the on-device invariant; rejected outright.
- **Keep heuristic-only extraction + "needs OCR" message:** still leaves scans
  unreadable — the core promise unfilled. Superseded by this ADR for scans.

## Linking

Constitution Articles II, III, VI.7 — on-device processing, typed failure
contract, no false confidence. Replaces the "needs OCR" dead-end in
`docs/current-state.md` C12 (C12 becomes REAL). AICore handoff (C11) stays
**ASPIRATIONAL** and is deliberately staged **after** OCR (see ADR-010).
