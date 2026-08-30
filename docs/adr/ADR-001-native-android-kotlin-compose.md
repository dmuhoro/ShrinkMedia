# ADR-001: Native Android with Kotlin & Jetpack Compose

**Status:** Accepted
**Date:** 2026-08-30
**Deciders:** Daniel Muhoro

## Context

ShrinkMedia is a private media & document toolkit. The primary surface is a
mobile app that must run reliably on a wide range of Android hardware (API
24–35), process heavy media on-device, and never leave the user's network
permission surface open. We needed a UI and runtime stack that scales to
complex local workflows (pickers, progress, batch queues) without webview or
network dependencies.

## Decision

Build the app natively in **Kotlin** with **Jetpack Compose (Material 3)**.
Compose is declarative, testable at the unit/instrumentation level, and ships
sound defaults for theming, accessibility, and state. The manifest declares
no INTERNET permission — all processing is in-process on-device.

## Consequences

**Positive:**
- Direct access to `BitmapFactory`, `MediaStore`, `FileProvider`, and
  `android.graphics.pdf` — all required for the on-device invariant.
- No network permission app-wide, making the privacy contract structural.
- Compose previews mirror the shipped UI for the web simulator reference.

**Negative:**
- Native-only: no direct iOS/desktop reuse.
- Compose toolchain adds build-time weight (Compose compiler, BOM management).

## Alternatives considered

- **Flutter / React Native:** cross-platform, but adds a runtime and makes the
  "no network permission" invariant awkward to enforce app-wide.
- **WebView wrapper over the simulator:** fast to ship, but no native FFmpegKit,
  MediaStore, or scoped-storage; high risk of a broken promise.
- **XML Views:** battle-tested, but verbose for this state-heavy UI and slower to
  iterate on.