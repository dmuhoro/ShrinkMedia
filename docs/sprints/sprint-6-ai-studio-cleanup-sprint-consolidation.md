# Sprint 6 — AI Studio Fingerprint Removal & Sprint Consolidation

**Status:** COMPLETE
**Version Target:** v0.2.1
**Theme/Result:** Strip the inherited Google AI Studio/Gemini artifacts from the
repo and collapse the two sprint folders into a single, execution-ordered
`docs/sprints/`.

## Summary

| Capability | Status | Evidence |
|------------|--------|----------|
| No `@google/genai` / `dotenv` / `express` deps in the web package | ✅ | `package.json`, `package-lock.json` (121 packages pruned, 0 vulnerabilities) |
| `vite.config.ts` free of AI Studio HMR/watch logic | ✅ | `vite.config.ts` |
| No AI Studio applet artifacts (`metadata.json`, `assets/.aistudio`, `.env.example`) | ✅ | files removed |
| Docs free of Gemini / APP_URL / GEMINI_API_KEY references | ✅ | `rg` whole-repo audit returns no matches |
| Single sprint folder `docs/sprints/` ordered 1→7 by execution | ✅ | `docs/sprints/` contents |
| Web gates green after cleanup | ✅ | `npm run lint` / `npm test` (11) / `npm run build` (evidence file) |
| Manifest no-INTERNET guardrail fixed (no false positives) | ✅ | `.github/workflows/ci.yml`, `.ai/context/08`, `.ai/agents/auditor-agent.md` |

## Part 1 — AI Studio fingerprint removal

**Problem:** ShrinkMedia was forked from a Google AI Studio applet that left
unused Gemini/GenAI scaffolding behind: an `@google/genai` dependency (never
imported), a `metadata.json` applet descriptor, an `assets/.aistudio/` folder,
a `.env.example` documenting `GEMINI_API_KEY`/`APP_URL`, AI Studio config
comments in `vite.config.ts`, and scattered "Gemini-driven helper" narrative in
the docs. None of it ran in the simulator; it only confused the on-device story.

**Fix:** Removed every fingerprint. Deleted `metadata.json`, `assets/.aistudio/`,
and `.env.example`. Removed `@google/genai`, `dotenv`, `express`, and
`@types/express` from `package.json` (regenerated `package-lock.json`).
Simplified `vite.config.ts` (dropped `DISABLE_HMR` AI Studio logic) and renamed
the package to `shrinkmedia-web`. Stripped all Gemini / APP_URL /
GEMINI_API_KEY references from `SECURITY.md`, `.ai/context/08_security.md`,
`.ai/context/09_runbooks.md`, `docs/runbooks/web-simulator.md`,
`docs/sprints/sprint-2-web-simulator-bootstrap.md`, and
`docs/adr/ADR-006-web-simulator-harness.md`. The Android "Elite AI" /
AICore handoff narrative (CORRECTLY ASPIRATIONAL) is unrelated and was kept
intact.

**Files:**
- Removed: `metadata.json`, `assets/.aistudio/`, `.env.example`
- Modified: `package.json`, `package-lock.json`, `vite.config.ts`,
  `.gitignore`, `SECURITY.md`, `.ai/context/08_security.md`,
  `.ai/context/09_runbooks.md`, `docs/runbooks/web-simulator.md`,
  `docs/sprints/sprint-2-web-simulator-bootstrap.md`,
  `docs/adr/ADR-006-web-simulator-harness.md`

## Part 2 — Sprint consolidation

**Problem:** Two sprints folders existed with conflicting numbering: root
`sprints/` held two *plan* files mislabeled `sprint_1`/`sprint_2`
(`sprint_1_engineering_governance_foundation.md` duplicated the Sprint 5 record
with a wrong number; `sprint_2_native_toolkit_hardening.md` was genuine future
planned work), while `docs/sprints/` held the execution-ordered records 1–5.
This made "what was done vs what is planned" impossible to audit.

**Fix:** Consolidated everything into the canonical `docs/sprints/`, ordered by
execution (first done to last done):
1–5 stay as the executed history. Today's cleanup is recorded as **Sprint 6**.
The former root `sprint_2_native_toolkit_hardening.md` becomes the **Sprint 7**
active plan (Phase 2 test hardening). The stale root
`sprint_1_engineering_governance_foundation.md` was dropped — it duplicated the
Sprint 5 record under a misleading number. The root `sprints/` folder was
deleted and every reference updated to `docs/sprints/`.

**Files:**
- Added: `docs/sprints/sprint-6-*.md` (this record),
  `docs/sprints/sprint-7-native-toolkit-hardening.md` (active plan)
- Removed: `sprints/sprint_1_engineering_governance_foundation.md`,
  `sprints/sprint_2_native_toolkit_hardening.md` (moved/re-numbered)
- Updated references: `README.md`, `CONTRIBUTING.md`,
  `.ai/context/02_system-map.md`, `.ai/agents/builder-agent.md`,
  `docs/release-readiness.md`, `docs/sprint-cross-reference.md`,
  `.ai/context/07_release-readiness.md`, `docs/evidence/2026-08-30_governance_scaffold.md`

## Validation & Verification Checklist

- [x] `rg` for `google ai studio|aistudio|gemini|APP_URL|GEMINI` on tracked
      files → no matches.
- [x] `npm install` → 0 vulnerabilities; lockfile pruned.
- [x] `npm run lint` green.
- [x] `npm test` → 11 tests pass.
- [x] `npm run build` green.
- [x] `docs/sprints/` contains exactly `sprint-1..7` ordered by execution.
- [x] No reference points at the removed root `sprints/` folder.
- [x] Manifest no-INTERNET guardrail greps the permission declaration only;
      verified against `app/src/main/AndroidManifest.xml` (no INTERNET).
- [x] CHANGELOG v0.2.1 updated.

## Cross-Reference

Constitution Article I (no false confidence), Article VI.7 (honesty);
ADR-006 (web simulator harness); ROADMAP Phase 2 (next: Sprint 7);
`docs/current-state.md` C2 (web simulator).
