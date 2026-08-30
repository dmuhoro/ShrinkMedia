# Architecture Decision Records

Every significant architectural decision in ShrinkMedia is recorded here as an
ADR (status, context, decision, consequences, alternatives considered).

| ADR | Title | Status | Date |
|-----|-------|--------|------|
| [ADR-001](ADR-001-native-android-kotlin-compose.md) | Native Android with Kotlin & Jetpack Compose | Accepted | 2026-08-30 |
| [ADR-002](ADR-002-ffmpegkit-video-compression.md) | FFmpegKit Lite for video compression | Accepted | 2026-08-30 |
| [ADR-003](ADR-003-foreground-service-batch.md) | Foreground service for background batch compression | Accepted | 2026-08-30 |
| [ADR-004](ADR-004-datastore-settings.md) | Jetpack DataStore for settings persistence | Accepted | 2026-08-30 |
| [ADR-005](ADR-005-scoped-storage-mediastore.md) | Scoped storage + MediaStore with no global permission | Accepted | 2026-08-30 |
| [ADR-006](ADR-006-web-simulator-harness.md) | Web simulator harness (Vite + React + TS) | Accepted | 2026-08-30 |
| [ADR-007](ADR-007-bitmap-sampling-jpeg.md) | In-memory bitmap sampling + JPEG for image compression | Accepted | 2026-08-30 |
| [ADR-008](ADR-008-on-device-pdf-pipeline.md) | On-device PDF pipeline via android.graphics.pdf | Accepted | 2026-08-30 |

## Anatomy of an ADR

Each record states: **Status** (Accepted / Proposed / Superseded), **Date**,
**Deciders**, **Context**, **Decision**, **Consequences** (positive and
negative), and **Alternatives considered**.

## Linking

References use the `[ADR-00N](docs/adr/ADR-00N-*.md)` form so they survive
moves and renders on GitHub. Decision-log entries that predate the formal ADR
sequence live in `docs/decisions.md`.