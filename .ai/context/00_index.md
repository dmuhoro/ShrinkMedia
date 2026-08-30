# 00 — AI Context Index

## Purpose
The routing map for ShrinkMedia agent sessions. Load this file at session start,
then pull the numbered context files you need. **`docs/` is the canonical
source; these files are condensed summaries — when in doubt, read the doc.**

## Authority Level
Operational — never overrides `docs/engineering/CONSTITUTION.md` or `AGENTS.md`.

## Consumers
All AI agents, the builder/auditor/architect agents, engineers.

## Dependencies
- `docs/current-state.md` — reality vs ASPIRATIONAL
- `docs/architecture.md` — system map

## Source Documents
- This folder mirrors `docs/`.

## Update Rules
- Bump `.ai/VERSION` when the context set changes materially.
- Condense from `docs/` after each sprint; never edit `docs/` here.

---

## Boot Sequence
1. `00_index.md` → 2. `11_workflow-rules.md` → 3. `01_architecture.md` +
   `10_roadmap.md` → 4. task-specific files.

## Routing Table

| Task type | Follow |
|---|---|
| Architecture / dependency / ADR | `01`, `06`, `architect-agent` |
| Media/PDF engine change | `02`, `05`, `03` |
| Settings / DataStore | `03`, `04` |
| Service / batch / pause-resume | `05`, `03` |
| Security (permissions, secrets) | `08` |
| Release gate / evidence | `07`, `12` |
| Roadmap / scope | `10` |