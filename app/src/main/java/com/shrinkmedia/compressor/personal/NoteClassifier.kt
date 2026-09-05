package com.shrinkmedia.compressor.personal

/**
 * Deterministic, offline vault classifier (ADR-015 §2). Given an ordered stream of notes it:
 *
 * 1. **Links each note to its chronological successor** in the same [thread] — the
 *    "what thought/idea came after" category ([NoteRecord.followedBy]).
 * 2. **Detects re-appearance** — a later note that carries the *core markers* of an earlier one
 *    gets its [NoteRecord.reappearedAt] set, answering "when did this idea show up again". A
 *    reappearing note that has no thread yet is joined to the original thread.
 *
 * HONESTY BOUNDARY (ADR-015 §2): detection is by **textual core-match** (a recurring idea marker
 * phrase), NOT by semantic meaning — semantic retrieval is ASPIRATIONAL and lives in a later
 * retrieval layer. No I/O, no network, no model calls; pure decision logic on the seam the later
 * DataBank server will sit behind.
 *
 * Fail-closed + no silent drops:
 * - Blank/unparseable text cannot make it into a [NoteRecord] (constructor refuses).
 * - Threads are only merged when a shared marker exists; otherwise a note stays in its own thread.
 * - Empty input ⇒ empty result (never crash, never `null`).
 */
object NoteClassifier {

    /**
     * Classify an ordered stream of raw vault notes. [notes] must already be ordered by
     * [NoteRecord.notedAt] ascending (the stream the phone produces).
     */
    fun classify(notes: List<NoteRecord>): List<NoteRecord> {
        if (notes.isEmpty()) return emptyList()
        val result = notes.toMutableList()

        // 1) Identity / normalisation: blank-safe text collapsed for marker extraction.
        val keyOf: (NoteRecord) -> String = { normalize(it.text) }

        // 2) Chromatic successor links: for each pair (earlier, later) in the SAME thread.
        for (i in notes.indices) {
            val earlier = notes[i]
            // find the next note that is in the same thread (by marker match, or same thread id)
            for (j in i + 1 until notes.size) {
                val later = notes[j]
                if (later.thread == null || earlier.thread == later.thread) {
                    val sharesMarker = overlap(keyOf(earlier), keyOf(later))
                    val sameExplicitThread = earlier.thread != null && earlier.thread == later.thread
                    if (sharesMarker || sameExplicitThread) {
                        result[i] = result[i].copy(followedBy = later.id)
                        break
                    }
                }
            }
        }

        // 3) Re-appearance detection: a later note carrying the earlier idea's core marker.
        for (i in notes.indices) {
            val earlier = notes[i]
            if (earlier.reappearedAt != null) continue
            for (j in i + 1 until notes.size) {
                val later = notes[j]
                val overlap = overlap(keyOf(earlier), keyOf(later))
                if (overlap && later.notedAt > earlier.notedAt) {
                    // mark the LATER note as a re-appearance, and join its thread to the original
                    result[j] = result[j].copy(
                        reappearedAt = later.notedAt,
                        thread = result[j].thread ?: earlier.thread ?: earlier.id
                    )
                    break
                }
            }
        }

        return result
    }

    /** Marker overlap: share at least one substantive (>= 3 char) token as a recurring idea marker. */
    fun overlap(a: String, b: String): Boolean {
        if (a.isBlank() || b.isBlank()) return false
        val tokensA = markers(a)
        val tokensB = markers(b)
        return tokensA.intersect(tokensB).isNotEmpty()
    }

    private fun markers(text: String): Set<String> =
        normalize(text)
            .split(Regex("[^a-z0-9]+"))
            .filter { it.length >= MIN_MARKER_LENGTH }
            .toSet()

    private fun normalize(text: String): String = text.trim().lowercase()

    private const val MIN_MARKER_LENGTH = 3
}