# ADR-004: Jetpack DataStore for Settings Persistence

**Status:** Accepted
**Date:** 2026-08-30
**Deciders:** Daniel Muhoro

## Context

The toolkit persists user preferences (theme, autosave, pause-on-low-battery,
quality presets) plus cumulative compression savings. The store must be
reactive (the UI should reflect changes as a flow), coroutine-friendly, and
crash-safe against on-disk corruption.

## Decision

Persist settings through **Jetpack DataStore Preferences**, exposing a single
`SettingsRepository` (`Context.dataStore`, name `user_settings`) whose
`userSettingsFlow` is collected by the ViewModel and read live by the service.
New settings are additive keys with fail-closed defaults.

## Consequences

**Positive:**
- A `Flow<PersistedUserSettings>` maps naturally to Compose state collection.
- No SQL/DAO boilerplate for a small, flat settings surface.
- `IOException` degrading to defaults keeps boot safe (Article IV.4).

**Negative:**
- Preferences is not a relational store — cumulative totals are a single
  counter, not per-file records (audit detail lives in the in-memory UI list).
- DataStore requires coroutines discipline (all writes are `suspend`).

## Alternatives considered

- **SharedPreferences:** synchronous, blocking, no first-class flow, and
  clunky to make corruption-safe.
- **Room:** overkill for a flat settings map; adds annotation processing.
- **Plain files:** manual marshalling, no reactive updates.

## Linking

Constitution Article IV — all settings live here behind `SettingsRepository`.