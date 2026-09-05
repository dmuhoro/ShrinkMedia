# Sprint 23 — Ecosystem Execute (Release-then-Continue) — Daftari Install + Sync Made Real

> **Scope:** the latest directive's Daftari work (install as a real app + wire the offline→Supabase
> sync that only existed in code), plus the combined operating doctrine and the owner-memory story
> persisted in ShrinkMedia, then defense-in-depth in DataBank and the next Forge step.
> **ShrinkMedia itself:** docs-only, maintenance mode, no app release.

## What was actually broken in Daftari (the real gaps, found by reading the project)

- **"Opens in the browser."** Live deployments were already installable (PNG-standalone manifest),
  so the real cause on a phone is a **stale pre-installability home-screen bookmark** — a gesture
  fix, but Daftari gave the user no in-app nudge to do it. And `usePWAInstall` could leave a **dead
  install button** after a dismissed prompt.
- **"Syncs offline to Supabase" was code-only.** The sync engine was real (IndexedDB → upsert →
  RLS pull), the tables were live in the project, but the **deployed bundle was compiled with the
  `localhost:0` no-op client** — Vercel (the production compiler) had no `VITE_SUPABASE_URL`/anon
  key, while the values sat in GitHub. So production "claimed" sync but shipped a client that could
  not reach Supabase, and nothing surfaced it.

## What was done (Daftari, released as v6.5.0)

1. **Install hardening** — persistent global `InstallBanner` (CTA on Android/Chrome, Share→"Add to
   Home Screen" on iOS) with one-tap dismiss + auto-hide once running installed; `usePWAInstall`
   now clears the button after any prompt outcome (no dead button). 7 new tests; suite 369→**376**.
2. **Sync wiring + visibility** — `isSyncConfigured` in `supabase.ts`, surfaced as a colored row in
   Settings so a misconfigured build says so instead of silently claiming sync.
3. **`verify:sync:live`** script — walks the real anon bearer path (write→RLS read→delete);
   exit 0/1/2 distinguishes proven / not-configured / live-failure.
4. **Wiring runbook** — `docs/supabase-wiring.md`.
5. **Deployed + verified at the boundary** — added both env vars to Vercel Production, redeployed,
   and verified the **live** bundle embeds `https://rjedivbpldkroffswoyb.supabase.co` with **zero**
   `localhost:0` fallback and a PNG-standalone manifest.

## ShrinkMedia (this repo) — docs generated this sprint

- `docs/operations/owner-memory.md` — the Owner's identity + abilities + resolve story, told simply.
- `docs/operations/SOP.md` §0 — the combined **execute mode** (study → pick → close → prove →
  record → ship → continue), including the deploy-boundary guardrail learned this sprint.
- This sprint record + CHANGELOG + `current-state` + `sprint-cross-reference`.

## Honest boundaries

- The **final phone install gesture** (remove stale bookmark, install fresh) and a **confirmed
  sign-in on a real handset** are the last human steps Daftari needs; code + cloud are wired to the
  bundle level and CI/unit gates are green.
- `verify:sync:live` reached the real network path but the project's **anon email rate limit** fired
  on the throwaway sign-up; surfaced honestly (exit 2), not mislabeled. Human/confirmed-auth runs it
  clean.
- DataBank restore command + fault drills and the next Forge step are listed in their own layers of
  this sprint.
- RSI remains OFF (dial 0); nothing here self-authorizes. `owner-memory.md` is documentation, not
  authorization.