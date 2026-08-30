# 06 — Decisions & ADRs

## Purpose
Decision index an agent needs before changing architecture.

## Authority Level
Reference — decisions are binding until superseded by another ADR.

## Consumers
Architect agent, anyone proposing a structural change.

## Source Documents
`docs/adr/`, `docs/decisions.md`.

## Update Rules
Add new ADRs to `docs/adr/`; mirror the index here.

---

## ADRs (Accepted)

| ID | Decision |
|----|----------|
| 001 | Native Android, Kotlin + Jetpack Compose (no webview/no INTERNET) |
| 002 | FFmpegKit Lite for video (awaited sessions, presets) |
| 003 | Foreground service for background batch compression |
| 004 | Jetpack DataStore Preferences via `SettingsRepository` |
| 005 | Scoped storage + MediaStore, no global storage permission |
| 006 | Web surface is a simulator harness (honest parity) |
| 007 | In-memory bitmap sampling + JPEG for images |
| 008 | On-device PDF pipeline via `android.graphics.pdf` |

## D-series (informal, `docs/decisions.md`)

D001 prototype-first → harden; D002 single `SettingsRepository`; D003 typed
`null` contract; D004 process-wide pause controller.

## Rule of thumb
Manifest permission change, engine change, settings-store change, or the
privacy boundary → **write an ADR first**.