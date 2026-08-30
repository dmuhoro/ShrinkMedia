# Evidence Log

> Machine-readable record of verifications. Every entry cites the
> command/test/step and the observed result. A PASS requires evidence, never
> narrative (Constitution Article VII). Fidelity before optimism.

## Index

| Date | Topic | File | Verdict |
|------|-------|------|---------|
| 2026-08-30 | Web simulator lint/test/build baseline | `2026-08-30_web_lint_test_build.md` | PENDING |
| 2026-08-30 | Engineering governance scaffold verification | `2026-08-30_governance_scaffold.md` | PENDING |
| 2026-08-30 | Android config self-check (wrapper + DSL) | `2026-08-30_android_config_check.md` | PENDING |

## Format

```markdown
# <Date> — <Topic>

## Command / Step
<exact command or manual step>

## Observed Result
<output, screenshot ref, or PASS/FAIL>

## Verdict
PASS / FAIL / BLOCKED
```

## Rules

1. Every release-readiness PASS row must cite a file in this folder or a
   directly referenced log.
2. RUN-LOGS that touch user media must redact filenames if they are PII-like;
   verdicts stay honest.
3. A BLOCKED verdict lists the exact blocker and the next unblocking step.
4. Evidence is committed with the work that produced it (Article VII.4).