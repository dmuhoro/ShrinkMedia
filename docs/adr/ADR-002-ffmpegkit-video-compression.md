# ADR-002: FFmpegKit for Video Compression

**Status:** Accepted (amended)
**Date:** 2026-08-30
**Deciders:** Daniel Muhoro

## Amendment (same date)

The originally chosen coordinate `io.github.root0as:ffmpeg-kit-lite:6.0-2` did
not exist on any public repository (verified 404 on Google Maven, Maven
Central, GitHub releases, and JitPack) — the app could not build. The decision
stands (**FFmpegKit**, native, on-device, libx264 presets), but the package is
now the Maven-Central-published `io.github.nikita36078:ffmpeg-kit:6.0.LTS`,
which exposes the same `com.arthenica.ffmpegkit` API and ships libx264 on all
four ABIs. See `docs/evidence/2026-08-30_android_config_check.md`.

## Context

Video compression is the most computationally heavy operation in the toolkit.
It must run on-device, support quality-preset bitrate/CRF targets, and surface
progress/status without blocking the UI thread. Options included a pure-Kotlin
transcoder (none mature), Android `MediaCodec` manual pipelines (verbose, error
prone), and FFmpegKit.

## Decision

Use **FFmpegKit** (MainActivity imports `com.arthenica.ffmpegkit.FFmpegKit`) for video
encoding. Presets map to `libx264 -crf <N> -b:v <bitrate>` with a `aac` audio
track, executed async and **awaited via session state** before accepting the
output.

## Consequences

**Positive:**
- Industry-standard encode quality/size trade-off at the chosen presets.
- Async sessions integrate with the foreground service's progress model.
- Package keeps the APK lean (x264-only build) and the API surface minimal.

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