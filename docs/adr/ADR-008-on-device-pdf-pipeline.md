# ADR-008: On-Device PDF Pipeline via android.graphics.pdf

**Status:** Accepted
**Date:** 2026-08-30
**Deciders:** Daniel Muhoro

## Context

The Documents tab needs image-to-PDF portfolios, PDF merge, page split,
metrics, and embedded-text extraction. These must run locally with no network
(Mozilla's pdf.js and server-side converters are out, per Article II).

## Decision

Use the Android platform's **`android.graphics.pdf`** package
(`PdfDocument`, `PdfRenderer`) for build/merge/split/metrics. Extraction of
embedded text reads page content tokens from the raw stream (the PDF page
content operators) and concatenates literal strings; scanned (image-only)
documents are explicitly reported as needing an OCR engine rather than guessed.

## Consequences

**Positive:**
- Native, zero-dependency implementation across API 24–35.
- No network, no storage permissions beyond scoped URIs.
- Clear, honest behaviour for scanned PDFs (Article VI.7).

**Negative:**
- `PdfRenderer` renders pages to bitmaps for merge/split — large documents cost
  memory; the 2048px cap bounds this.
- Text extraction is heuristic; heavy/complex content may return a partial
  result and must say so.

## Alternatives considered

- **pdf.js (JS-in-WebView):** violates the no-WebView + native performance goal.
- **Third-party PDF SDKs:** heavyweight licensed binaries for modest needs.
- **Skip PDFs entirely:** loses a core audience need in a media/document toolkit.

## Linking

Constitution Articles II & III — on-device processing, typed results
(`createPdfFromImages`, `mergePdfDocuments`, `splitPdfIntoPages` throw explicit
errors; extraction returns a message instead of lying). AICore handoff remains
**ASPIRATIONAL** (see `docs/current-state.md`).