# Runbook: Keystore Off-Machine Backup (L6)

## Goal

Guarantee the ShrinkMedia **release signing keystore** survives this machine. The
keystore is a single private key that signs every Play-Store-compatible release AAB;
if it is lost and the app is already distributed, the store identity cannot be
recovered (a NEW key could be produced but existing installs/devices and the Play
listing would no longer accept updates). An **off-machine** copy removes the single
point of failure of this host.

## Why this is needed right now

- The live keystore exists in **exactly one place**:
  `/home/daniel-muhoro/.android/keystores/shrinkmedia-release.jks` (confirmed
  `2026-08-31`); it is referenced by `app/build.gradle.kts`
  (`storeFile=/home/daniel-muhoro/.android/keystores/shrinkmedia-release.jks`) and is
  gitignored explicitly (`keystore.properties`, `*.jks`).
- No backup currently exists off-machine. Loss of this host's storage = permanent loss
  of the Play-release identity.

## Do NOT do this

- Never commit the keystore or `keystore.properties` (both gitignored). This runbook is
  about a **human-driven, physical/off-machine** copy, not a repo artifact.

## Verified checklist (L6 — the copy is a human step)

1. **Confirm the live keystore file + fingerprint** (no secrets printed):

   ```bash
   ls -la --time-style=+%Y-%m-%d /home/daniel-muhoro/.android/keystores/shrinkmedia-release.jks
   sha256sum /home/daniel-muhoro/.android/keystores/shrinkmedia-release.jks
   ```

   Observed (2026-09-03, L6 verification:
   - 2770 bytes, dated 2026-08-31.
   - **SHA256: `2ecf8c807beeabef5a7f3df07902b997ebf846476eb85a56b4288ede4dca2d5b`**
     (anchor checksum; see Step 6).

2. **Copy to an off-machine medium** (choose ONE, per your threat model):
   - A hardware-backed device: insert a **bit-for-bit** copy onto an encrypted USB
     drive or an offline hardware token; **or**
   - A separate encrypted vault / cloud KMS-backed object store you control (NOT the
     app repo, NOT a public store).

3. **Record the backup location** (file:// path or drive serial + holder) in a
   private, non-repo place. This is your recovery pointer if the host dies.

4. **Verify the copy is bit-for-bit identical** on the backup medium:

   ```bash
   # on the backup host, for a copied file:
   sha256sum <backup-medium>/shrinkmedia-release.jks
   ```

5. **A match proves integrity.** The copied file's SHA256 MUST equal the anchor
   checksum from Step 1. Any difference means copy/upload corruption — redo it and
   re-verify (fail closed, no "close enough").

6. **Anchor reference (keep with the runbook):**
   ```
   L6_ANCHOR_SHA256=2ecf8c807beeabef5a7f3df07902b997ebf846476eb85a56b4288ede4dca2d5b
   ```

## Recovery (if/when needed)

```bash
# restore the backup copy into the expected path (or update storeFile in keystore.properties)
mkdir -p /home/daniel-muhoro/.android/keystores
cp <backup-medium>/shrinkmedia-release.jks /home/daniel-muhoro/.android/keystores/shrinkmedia-release.jks
sha256sum /home/daniel-muhoro/.android/keystores/shrinkmedia-release.jks   # must == anchor
```

## Honest status

- This runbook is **verified for the file + checksum** (the anchor SHA256 above was
  computed on the live keystore on 2026-09-03). The actual **off-machine copy is a
  human step that is NOT yet done** — until a verified off-machine copy exists, the
  release identity still has a single point of failure on this host. This runbook
  reduces the risk to a single, human-actionable step; it does not claim the copy
  already exists.