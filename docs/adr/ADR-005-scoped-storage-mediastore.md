# ADR-005: Scoped Storage + MediaStore with No Global Permission

**Status:** Accepted
**Date:** 2026-08-30
**Deciders:** Daniel Muhoro

## Context

The app reads user media and writes compressed output. Reading must honour
modern scoped-storage rules (user grants access through the system picker), and
writing should default to app-private space, with an optional, explicit "save
to gallery" behaviour into public folders.

## Decision

- **Read** via `ActivityResultContracts.GetContent` / `GetMultipleContents`,
  which return grant-scoped `Uri`s the app resolves through
  `ContentResolver` — no `READ_EXTERNAL_STORAGE`/`READ_MEDIA_*` permission.
- **Write** to `context.cacheDir` by default; the output is shareable through
  the `FileProvider` (`@xml/file_paths`).
- **Autosave** (opt-in, default `false`, DataStore-backed) writes to public
  folders via `MediaStore.Images/Video` inserts with a `RELATIVE_PATH` of
  `Pictures/ShrinkMedia` / `Movies/ShrinkMedia` — requires no storage permission.

## Consequences

**Positive:**
- No broad storage permission in the manifest; Play policy-friendly and honest
  about the privacy contract.
- Public writes require the user to flip the explicit autosave toggle.
- FileProvider keeps sharing to WhatsApp/Gmail/Drive lightweight.

**Negative:**
- Picker-granted URIs are the only read path — no bulk "scan the whole library"
  UX without adding MediaStore read permissions later.
- Autosave needs per-file MediaStore inserts; failure must not be silent.

## Alternatives considered

- **Legacy `READ/WRITE_EXTERNAL_STORAGE`:** refused by modern policy and
  meaningless on API 30+.
- **SAF `ACTION_CREATE_DOCUMENT` for every output:** intrusive; bad default flow.
- **Writing only to cache forever:** simpler, but "save to gallery" is a core
  toolkit expectation.

## Linking

Constitution Articles II and III — privacy invariant and no-silent-drop
handling around autosave (`saveToPublicMediaStore` returns `Boolean`,
surfaced by callers).