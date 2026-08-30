# ADR-007: In-Memory Bitmap Sampling + JPEG for Image Compression

**Status:** Accepted
**Date:** 2026-08-30
**Deciders:** Daniel Muhoro

## Context

Image compression must produce a visibly-good result with a dramatic size cut on
low-end hardware, without dumping giant decoded bitmaps into memory. Presets are
Low / Medium / High.

## Decision

Compress images in-memory with two passes:
1. **Bounds pass** (`inJustDecodeBounds`) reads only the dimensions, then
   computes an `inSampleSize` that cap-samples to ≤ 2× the preset max dimension.
2. **Decode + scale pass** decodes sampled, optionally downscales to the preset
   `maxDimension`, and writes **JPEG quality** (`imageQuality`) to the cache.

`compressImageFile` returns `File?` — `null` on any decode failure (typed,
surfaced by callers).

## Consequences

**Positive:**
- Memory stays bounded even for 100MP+ photos.
- Predictable quality knob (55 / 75 / 90 JPEG quality) and size cap.
- Pure helper logic, unit-testable via the web `lib/` mirror.

**Negative:**
- JPEG only — no WebP/HEIC output (acceptable for the toolkit v1).
- Integer `inSampleSize` skips exact dimensions for oddly-shaped images; the
  follow-up scale pass corrects this.

## Alternatives considered

- **Storing to PNG:** correct but larger than JPEG for photos.
- **Server-side re-encode:** violates the on-device invariant (Article II).
- **libjpeg-turbo native dependency:** better speed/quality, adds native blobs.

## Linking

Constitution Article III — typed results, no silent drops. Mirror test coverage
in `src/lib/`.