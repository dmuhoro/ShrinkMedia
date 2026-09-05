package com.shrinkmedia.compressor.personal

/** Where a captured thought came from — "record any form of information" (ADR-015 §2). */
enum class NoteSource(val label: String) {
    PHOTO("photo / scan"),
    VOICE("voice"),
    TYPED("typed"),
    CAMERA("camera capture")
}

/**
 * A single vault record (ADR-015 §2). Immutable; the vault is append-only.
 *
 * The categorisation the owner asked for is structural:
 * - [notedAt] — the specific date the idea/thought was noted.
 * - [reappearedAt] — when the same idea showed up again (detected by [NoteClassifier]).
 * - [followedBy] — what thought/idea came after it (thread predecessor link).
 *
 * [supersedes] implements "delete old ways of thinking and install new ones" safely: a newer
 * record supersedes an older one; the old record is never removed — it stays archived + searchable
 * (past/present/future timelines are one line of thinking, ADR-015 §3).
 */
data class NoteRecord(
    val id: String,
    val source: NoteSource,
    val text: String,
    val notedAt: Long,
    val thread: String? = null,
    val reappearedAt: Long? = null,
    val followedBy: String? = null,
    val supersedes: String? = null,
) {
    init {
        require(id.isNotBlank()) { "vault id must not be blank" }
        require(text.isNotBlank()) { "a blank note is a silent drop — refuse to store it" }
    }
}