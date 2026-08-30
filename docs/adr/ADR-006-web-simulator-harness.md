# ADR-006: Web Simulator Harness (Vite + React + TypeScript)

**Status:** Accepted
**Date:** 2026-08-30
**Deciders:** Daniel Muhoro

## Context

ShrinkMedia also carries a browser surface (Vite + React + TS + Tailwind) that
mirrors the native toolkit's UI — a live simulator with a phone-frame preview,
code tabs (`build.gradle.kts`, `BatchCompressionService`, `SettingsRepository`),
batch/undo/audit flows, and a Gemini-driven helper (`@google/genai`).

## Decision

Treat the web surface as a **simulator harness**, not a second product that
silently exceeds native capability. Pure logic (size formatting, compression
ratio math) is extracted into `src/lib/` so it is unit-testable, and every
simulated feature must correspond to a verified native capability.

## Consequences

**Positive:**
- Fast iteration on UX patterns before/without touching native code.
- The code tabs make the native implementation inspectable from the browser.
- `lib/` helpers are directly testable with Vitest.

**Negative:**
- Simulated compression numbers are NOT real compression — the UI must not
  claim otherwise (honesty, Article VI.7).
- Two surfaces to keep in parity; drift is caught by the simulator tests and
  `docs/sprint-cross-reference.md`.

## Alternatives considered

- **Compose Multiplatform web target:** interesting but heavier tooling and no
  parity gain for a simulator.
- **Native-only, no web surface:** loses the marketing/preview value the repo
  origin intends (Google AI Studio applet).

## Linking

Constitution Article II (no network permission in the native app; the web
simulator is a separate harness), ROADMAP Phase 3 (simulator discipline).