# 11 — Workflow Rules

## Purpose
The operational rules condensed for every session.

## Authority Level
Foundational — Constitution Articles I, VI, VII.

## Consumers
Every agent.

## Source Documents
`docs/engineering/CONSTITUTION.md`, `AGENTS.md`.

## Update Rules
Condense-after-amendment.

---

1. **Fail closed.** Defaults refuse. Conservative quality/bitrate caps.
2. **Real boundary.** Guards live at `compressImageFile` / `compressVideoFile`
   / `executeBatchProcessing` — not a demo helper.
3. **No silent drops.** `null`/`false` surfaced with reason + progress.
4. **On-device only.** INTERNET = ADR. Full stop.
5. **Typed results.** Every conversion returns a typed result or `null`.
6. **Read state docs first.** `docs/current-state.md` + `docs/architecture.md`;
   ASPIRATIONAL capabilities are not buildable assumptions.
7. **One layer at a time.** Code + tests + docs together, committed
   individually, SSH-signed, Conventional Commits.
8. **Dependencies require justification.**
9. **Evidence or bust.** `docs/evidence/`; PASS = cited command + observed
   result (Article VII).
10. **Honest verdicts.** Closing one gap never means "risk is complete".

## Feature-freeze trigger
Any open P0 in `docs/release-readiness.md` blocks starting a new subsystem.