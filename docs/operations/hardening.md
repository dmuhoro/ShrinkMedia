# Ecosystem Anti-Fragility — Hardening Audit

> Part of the 2026-09-05 directive. Tracks where each piece of the ecosystem is **proven at the real
> boundary** vs **proven only unit-style** vs **still needs a host/credentials**. The rule behind it
> (AGENTS.md §1): a unit test of a helper is not proof the real path works — cite the production
> function the claim is actually about.

## The four repos, where their truth lives

| Repo | Truth lives in | Proven at the real boundary, today |
|------|----------------|------------------------------------|
| **ShrinkMedia** (Android) | `BatchCompressionService.executeBatchProcessing` (real batch loop), `compressImageFile`/`compressVideoFile`, MediaStore autosave, on-device AICore probe | Compression, batch pause/resume, autosave, PDF, OCR: **real-path instrumented runs on a device** (C3–C10, C12; evidence 2026-08-30…09-03). AICore C11: honest `UNAVAILABLE` wall on the test handset — not a PASS |
| **Daftari** (PWA) | vite build + `vite preview` service worker; IndexedDB persistence in the product install | For every pushed change: dev e2e (10) **and** production-build e2e (5): PNG manifest, apple-touch, SW registered + controlling, offline shell + IndexedDB, offline sale `synced=0` never dropped. **Not proven:** real handset install (manual checklist), real Supabase upload (credentials) |
| **DataBank** (python vault) | `dvault/vault.py` open/put/close + `dvault` CLI | 23-test suite + a **live Ubuntu run** (native Python 3.14, not the server) in `docs/evidence/2026-09-05_ubuntu_run_proof.md`; CI ×3 OS. Corrupt-open refusal, oversized-refusal, backup snapshot reopen all exercised for real. **Not proven:** 24/7 hosting, network reachability, restore-on-bare-metal drill on the actual host |
| **Forge** (orchestrator) | `forge/engine.py` transition guard + `forge/cli.py` subprocess boundary | 16 contract tests including the **real CLI process** (`cli.py` invoked as a subprocess) + a live evidence journal (`docs/evidence/2026-09-05_step1_state_machine.md`). Merged/blocked/refusal paths run for real from the terminal. **Not proven:** the brief's exit criterion "hand-run 5 real Daftari tasks" — open by design, needs the Owner's real tasks |

## Failure-class hardening map (what happens when things go wrong)

| Failure | System | Guard at the real boundary | Verified? |
|---------|--------|---------------------------|-----------|
| Corrupt/truncated store | DataBank | open-time `PRAGMA quick_check` → `VaultRefused`; post-close use refused | ✅ on Ubuntu + suite |
| Oversized text record | DataBank | `MAX_TEXT_LEN` 200_000 → refusal (no partial write) | ✅ suite |
| Backup consistency | DataBank | `VACUUM INTO` snapshot + reopen-verify in `dvault backup` | ✅ Ubuntu + suite |
| Slow/crashed writer | DataBank | WAL + fsync'd append-only JSONL journal; on restart journal audited | ✅ suite |
| Browser makes a shortcut, not an app | Daftari | PNG manifest + apple-touch; **production-build** e2e asserts this | ✅ e2e prod |
| Offline loss of data | Daftari | sale persisted `synced=0` before/after failed sync; reload keeps shell+data | ✅ e2e prod |
| SW first-install not yet controlled | Daftari | e2e walks the real life-cycle (second navigation controlled) | ✅ e2e prod |
| Queue item dropped on pause | ShrinkMedia | `executeBatchProcessing` awaits `isPaused.first{!it}` | ✅ device-instrumented |
| Video/audio codec missing on device | ShrinkMedia | FFmpegKit full build (x264/H.264/MP4 muxer) replaced audio-only build; fail-closed `null` | ✅ device (rc=1 caught) |
| Task state machine violated | Forge | `engine.py` transitions: terminal never resurrects, blocked needs reason, attempts cap → human | ✅ CLI subprocess + live run |
| Journal line corrupt | Forge | refusal, never skip (audit stays append-only) | ✅ suite |
| Upload target unavailable | Daftari | queue keeps record across app reload; no silent drop on failed sync | ✅ e2e prod (local failure injected) |

## What a PASS on this table does NOT mean

- Hard-end **hosting** honestly still needs: DataBank on the always-on host (24/7 systemd + restore
  drill, Checkpoint 2 in `checkpoint-map.md`), a **private transport** (Tailscale/keystore-backed),
  and the **real device** for Daftari install + ShrinkMedia→vault write contract.
- RSI remains OFF (dial 0); nothing here self-authorizes.

Updated 2026-09-05 (Sprint 22).