# ShrinkMedia Privacy Policy

*Effective: 2026-09-03 · Applies to the Android app `com.shrinkmedia.compressor` (ShrinkMedia).
Draft for review; will be linked in the app and Play Console (see `docs/marketplace-2026-benchmark.md`).*

## Short version

**ShrinkMedia is a private, on-device toolkit. Your files stay on your device.**
In the default build the app declares **no Internet permission** — it cannot send your
photos, videos, PDFs, or extracted text off the device. Compression, conversion, OCR, and PDF
operations run locally. There are no accounts, no analytics, no advertising, and no server.

## What data the app accesses

- **Media and documents you choose.** ShrinkMedia only opens files you explicitly select (via
  the system picker or your on-device media library) for compression, PDF, or OCR.
- **On-device settings.** App preferences (quality presets, theme, auto-save, OCR language) are
  stored on-device in Android's DataStore.

## Why the app needs few permissions

The default release declares a minimal set (foreground service for background batch progress,
notifications). It does **not** request Internet, location, or contacts. On-device-only processing
means no user data is transmitted, stored, or shared with anyone — including us — which keeps the
data-safety disclosure minimal (per Google Play Data-safety rules, on-device processing does not
need to be disclosed).

## Third-party processing libraries

Third-party libraries run **on-device** and do not upload content:

| Library | Purpose | Network |
|---------|---------|---------|
| FFmpegKit (LGPL) | Video/audio compression | None (local execution) |
| iText 7 | PDF build/merge/split/extract | None |
| ML Kit (text recognition) | OCR on scanned PDFs/images | None by app; ML Kit's telemetry transport is stripped from the shipped manifest (NO INTERNET) |

No library in the default build is able to send your data because the app has **no Internet
permission**.

## AICore / on-device Google AI (designed, not yet active)

A future capability (ADR-011) may use Google's **on-device** Gemini Nano via AICore for
summarization/captions. Model download is managed by Android's Private Compute Services (outside
the app). Inference runs locally. This feature will only be enabled when available on your device
and will remain on-device; it does not send your content to Google's servers.

## "Connected mode" (designed, OFF by default, not yet active)

A future capability (ADR-012) may add an opt-in **Connected mode** for cloud AI and Google
account bridging. It is **OFF by default**. Nothing is uploaded unless you (a) explicitly enable
Connected mode and (b) invoke a specific connected action. The default app keeps NO Internet
permission. We will update this policy before any connected feature ships.

## Photos access

Android's system photo picker / MediaStore gives the app scoped access only to files you pick;
the app does not silently scan unrelated media beyond what you select for the on-device gallery.

## Data retention, deletion, security

- No user content is collected, so there is nothing for us to retain, sell, or delete.
- App renderings and audit logs live in the app's private sandbox and are removed if you
  uninstall or clear the app's data.
- Sensitive data is never logged to system logs (core-quality guideline) and stays in the app's
  internal storage.

## Changes

If a future release changes data handling (e.g. ships Connected mode), this policy will be
updated and the change gated by an architecture decision (ADR) before going live.

## Contact

This is a personal project. Developer: Daniel Muhoro. Corrections/comments via the project's
GitHub repository `dmuhoro/ShrinkMedia`.