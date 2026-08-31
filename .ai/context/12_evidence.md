# 12 — Evidence

## Purpose
Where verification lives and how to record a new entry.

## Authority Level
Foundational — Constitution Article VII.

## Consumers
Auditor agent, release work.

## Source Documents
`docs/evidence/README.md`.

## Update Rules
Add a file + an index row for every verification; never append narrative as
evidence.

---

## Index (as of 2026-08-30)

| Topic | File |
|---|---|
| Web lint/test/build baseline | `docs/evidence/2026-08-30_web_lint_test_build.md` |
| Governance scaffold verify | `docs/evidence/2026-08-30_governance_scaffold.md` |
| Android config self-check | `docs/evidence/2026-08-30_android_config_check.md` |
| Sprint 6 cleanup + sprint consolidation | `docs/evidence/2026-08-31_sprint6_cleanup.md` |

## Entry format

```markdown
# <Date> — <Topic>
## Command / Step
<exact command or step>
## Observed Result
<output / PASS / FAIL>
## Verdict
PASS / FAIL / BLOCKED
```

## Rules
- PASS rows in release-readiness cite a file here.
- BLOCKED records name the blocker and the unblocking step.
- Device runs that touch user media redact identifying filenames.