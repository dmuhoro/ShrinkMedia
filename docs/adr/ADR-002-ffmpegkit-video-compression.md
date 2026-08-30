# ADR-002: FFmpegKit Lite for Video Compression

**Status:** Accepted
**Date:** 2026-08-30
**Deciders:** Daniel Muhoro

## Context

Video compression is the most computationally heavy operation in the toolkit.
It must run on-device, support quality-preset bitrate/CRF targets, and surface
progress/status without blocking the UI thread. Options included a pure-Kotlin
transcoder (none mature), Android `MediaCodec` manual pipelines (verbose, error
prone), and FFmpegKit.

## Decision

Use **FFmpegKit Lite** (`io.github.root0as:ffmpeg-kit-lite`) for video
encoding. Presets map to `libx264 -crf <N> -b:v <bitrate>` with a `aac` audio
track, executed async and **awaited via session state** before accepting the
output.

## Consequences

**Positive:**
- Industry-standard encode quality/size trade-off at the chosen presets.
- Async sessions integrate with the foreground service's progress model.
- Lite variant keeps the APK small and the API surface minimal.

**Negative:**
- No hardware-encoder fallback in this build (CPU encoding of long clips costs
  battery).
- FFmpeg remains a native dependency inside the APK (size, but acceptable for
  the Lite build).

## Alternatives considered

- **MediaCodec pipeline:** fine control, but ~3x the code and a real risk of
  device-specific bugs on API 24–35.
- **Server-side transcoding:** violates the on-device invariant (Article II).
- **No re-encode (mux only):** not true compression for already-encoded clips.

## Linking

Enforced by Constitution Article III — sessions are awaited;
`compressVideoFile` returns `null` on failed encodes (typed result).