# ADR-003: Foreground Service for Background Batch Compression

**Status:** Accepted
**Date:** 2026-08-30
**Deciders:** Daniel Muhoro

## Context

Batch compression of many large media files takes minutes. It must survive the
user leaving the app, report real-time progress, and remain visible to the user
(foreground notification). The service must also stop promptly on cancel and
preserve the user's files on any path.

## Decision

Run batches in a **foreground `Service`** — `BatchCompressionService`, type
`dataSync` — with a progress notification (`Ongoing`, progress bar) and a
cancel action. The service owns the queue loop
(`executeBatchProcessing`) and exposes pause/resume through the shared
`BatchCompressionPauseController.isPaused` state.

## Consequences

**Positive:**
- Batch work continues with the app backgrounded.
- A single queue loop keeps progress, pause, and per-file autosave consistent.
- Notification is the honest progress surface (no silent drops, Article I.6).

**Negative:**
- Foreground-service lifecycle + notification permissions must be handled
  (API 33+ `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE_DATA_SYNC` permissions).
- Service state is not an application database — history lives in the UI/DataStore.

## Alternatives considered

- **WorkManager:** better for deferrable jobs, but weaker support for
  tightly-coupled, cancellable, user-visible immediate queues.
- **Coroutines in the Activity:** dies with the process; no background guarantee.
- **Existing Hilt/DI service registry:** overcomplicated for one service.

## Linking

Constitution Article III.4 — `isPaused` is the single source of truth; queued
items are never dropped.