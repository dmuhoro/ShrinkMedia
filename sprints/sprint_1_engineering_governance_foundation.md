# Sprint 1: Engineering Governance Foundation & Elite Scaffold

**Status:** In Progress
**Version Target:** v0.2.0
**Focus:** Repo completeness — governance, evidence, CI, web discipline

---

## Sprint Objectives
1. **Elite engineering folders**: Constitution, ADR index + records,
   decisions log, sprint records, evidence log, release-readiness gates,
   runbooks, and active sprint plans — modelled on the Kay's Wellness / Daftari /
   TraderOS reference repos.
2. **Root governance files**: `AGENTS.md`, `CONTRIBUTING.md`, `ROADMAP.md`,
   `SECURITY.md`, `LICENSE`, `CHANGELOG.md`, living `README.md`.
3. **AI engineering team**: `.ai/` with `VERSION`, agent definitions, and a
   numbered context mirror of the docs.
4. **CI quality gates**: `.github/workflows/ci.yml` (web lint/test/build plus
   an Android wrapper gate), issue + PR templates, CODEOWNERS.
5. **Web discipline**: extract pure helpers to `src/lib/`, add Vitest, green
   lint/test/build.
6. **Android reproducibility**: Gradle wrapper committed; `assembleDebug`
   green (SDK 35 installed locally; evidence in `docs/evidence/2026-08-30_android_config_check.md`).

---

## Key Files Created / Modified
- `docs/engineering/CONSTITUTION.md`, `docs/adr/` (001–008 + index),
  `docs/decisions.md`, `docs/architecture.md`, `docs/current-state.md`,
  `docs/code-standards.md`, `docs/evidence/`, `docs/release-readiness.md`,
  `docs/runbooks/`, `docs/sprints/`, `docs/sprint-cross-reference.md`
- `sprints/sprint_1_engineering_governance_foundation.md`
  (this file), `sprints/sprint_2_native_toolkit_hardening.md`
- `AGENTS.md`, `CONTRIBUTING.md`, `ROADMAP.md`, `SECURITY.md`, `LICENSE`,
  `CHANGELOG.md`, `README.md`
- `.ai/VERSION`, `.ai/agents/*`, `.ai/context/*`
- `.github/workflows/ci.yml`, `.github/ISSUE_TEMPLATE/*`,
  `.github/pull_request_template.md`, `.github/CODEOWNERS`
- `src/lib/compression.ts` + test, `src/lib/format.ts` + test
- `gradlew`, `gradle/wrapper/*`, `.gitignore`

---

## Validation & Verification Checklist
- [x] Prior in-progress work committed individually (settings → service → UI).
- [x] Web: `npm install` clean (0 vulnerabilities).
- [x] Web: `npm run lint` green.
- [x] Web: `npm test` green.
- [x] Web: `npm run build` green.
- [x] Android: `assembleDebug` green — SDK 35 + build-tools installed, wrapper
  used, `app-debug.apk` produced (see `docs/evidence/2026-08-30_android_config_check.md`).
- [x] Evidence logged under `docs/evidence/`.
- [x] CHANGELOG + sprint records updated.
- [x] All commits SSH-signed; pushed to `origin/main`.
- [x] Manifest still declares **no INTERNET permission**.