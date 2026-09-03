# Video Compression Dependency Fix (v0.6.0)

**Date:** 2026-09-03
**Device:** 49IZ6DJ7SONNQOBE (API 36, arm64)
**Constitution reference:** Article VII (evidence), Article I (no false confidence, fail-closed)

## Summary

A real gap, hidden until now, was discovered and closed: the native app's "Video
compression" feature could not actually compress any video because the bundled
FFmpegKit build contained **no H.264 encoder and no MP4 muxer**.

## The defect (discovered, not assumed)

The v0.6.0 on-device instrumented test for `compressVideoFile` (the first code to
ever exercise real video encoding) failed on the API-36 device with:

```
rc=1 ... Unrecognized option 'preset'. Error splitting the argument list: Option not found
```

Inspecting the bundled FFmpegKit binary's build config showed:

```
--disable-everything ... --enable-muxer=wav --enable-demuxer=wav ...
--enable-decoder=adpcm_ima_wav --enable-decoder=adpcm_yamaha --enable-encoder=pcm_u8
--enable-filter=aresample
```

i.e. only WAV/PCM audio support — **no `libx264`, no H.264 encoder/decoder, no MP4
(`mov`) muxer/demuxer, no video filters** (grep for `libx264`, `h264`, `mp4`,
`matroska` → 0 matches in the config).

The dependency was `io.github.nikita36078:ffmpeg-kit:6.0.LTS` — the **audio-only /
"Lite"** FFmpegKit variant. Because `compressVideoFile` ran `-c:v libx264`
(which required libx264), every real video encode failed and
`compressVideoFile` returned `null` (fail-closed — the caller surfaced it),
meaning the shipped v0.5.0 "Video compression" feature was a silent no-op.

## The fix

1. **Dependency swap** (`app/build.gradle.kts`):
   `io.github.nikita36078:ffmpeg-kit:6.0.LTS` →
   `dev.ffmpegkit-maintained:ffmpeg-kit-full:8.1.7` (LGPL, maintained fork, SDK-35 /
   16KB builds). The `full` variant ships the **openh264** H.264 encoder + MP4 muxer,
   and is non-GPL so it does not copyleft the app.
   Justification per AGENTS.md §6: the previous build's specific gap was "no video
   encoder/muxer", which no other existing dependency (Coil/DataStore/ML Kit/iText)
   covers.

2. **Companion dep added** `com.arthenica:smart-exception-java:0.2.1` — required by
   `FFmpegKitConfig.<clinit>` at runtime (`NoClassDefFoundError:
   Lcom/arthenica/smartexception/java/Exceptions` otherwise). The full 8.1.7 POM does
   not declare it transitively; the old nickita POM did.

3. **Encoder change** (`MainActivity.kt` `compressVideoFile`):
   `-c:v libx264 -crf <n> -preset veryfast` → `-c:v h264` (openh264) with
   `-b:v <bitrate> -maxrate <maxrate> -bufsize <bufsize>`. openh264 has no CRF or
   preset, so size control is bitrate-driven. `CompressionQuality.videoCrf:Int` was
   replaced by `videoMaxRate`/`videoBufSize` (maxrate ≈ 2× target bitrate, per tier).

## On-device proof (real path)

New instrumented test
`CompressionPipelineInstrumentedTest#compressVideoFile_runsTheRealFFmpegKitPipeline_andProducesASmallerValidH264Mp4`:

1. Generates a genuine large, high-entropy H.264 source (`-f lavfi -i testsrc2=1920x1080:30`,
   `-c:v h264 -b:v 8000k`, 6s) on-device.
2. Calls the real production `compressVideoFile` at MEDIUM on it.
3. Asserts: output non-null, exists, non-empty, **smaller than input**, outputs a
   **decodable H.264 MP4** (probe return code success), and `onProgress` reached 100.

Result — full on-device suite:

```
Starting 10 tests on 25078RA3EA - 16
25078RA3EA - 16 Tests 9/10 completed. (0 skipped) (0 failed)
Finished 10 tests on 25078RA3EA - 16
```
> **PASS — 10/10 instrumented tests on API-36**, including the new video test that
> proved a real source (258 694 B) re-encoded **smaller** (367 741 B) and decodable.
> (Earlier runs logged the intermediate sizes; the definitive signal is the green
> suite above.)

Also verified: `testDebugUnitTest`, `lintDebug`, `assembleDebug` all green with the new
dependency.

## Compatibility note (honest)

`ffmpeg-kit-full:8.1.7` ships **arm64-v8a** and **x86_64** only (no armeabi-v7a / x86).
Modern devices (incl. the test device) are arm64, so this is acceptable for v0.6.0, but
it is a deliberate reduction in legacy-32-bit device coverage and is recorded here and
in `docs/current-state.md` C4 rather than hidden.

## Files changed

- `app/build.gradle.kts` — dependency swap + companion dep + justification.
- `app/src/main/java/com/shrinkmedia/compressor/MainActivity.kt` — `compressVideoFile`
  encoder change; `CompressionQuality` gain `videoMaxRate`/`videoBufSize`, drop
  `videoCrf`.
- `app/src/androidTest/java/com/shrinkmedia/compressor/CompressionPipelineInstrumentedTest.kt`
  — new on-device `compressVideoFile` test + `writeHighBitrateMp4` generator.
- `docs/current-state.md` — C4, C14 updated to ✅ with the fixed, device-verified state.
