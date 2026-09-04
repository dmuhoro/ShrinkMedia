package com.shrinkmedia.compressor.forge

import com.shrinkmedia.compressor.forge.EcosystemIndex.Doc
import com.shrinkmedia.compressor.forge.EcosystemIndex.SearchHit

/**
 * Lessons-learned capture (SOP Phase 9 / ADR-014): every completed task writes a lesson into the
 * searchable corpus so a future identical situation retrieves past experience instead of starting
 * from zero ("never re-invent the wheel", SOP §0.9). Backed by [EcosystemIndex] — deterministic,
 * offline, no I/O.
 *
 * Fail-closed + no silent drops:
 * - A lesson must have a non-blank [Lesson.id], [situation], and [lesson];
 *   otherwise [add] throws (refuses loudly) rather than saving garbage.
 * - [search] delegates to [EcosystemIndex] and returns an empty list for no match (never null).
 * - Duplicate [Lesson.id] is an explicit no-op returning `false` (idempotent).
 */
class LessonBook {

    private val index = EcosystemIndex()

    val lessonCount: Int get() = index.documentCount

    /** Capture a lesson. Returns `false` if the id already exists (no duplicate). */
    fun add(lesson: Lesson): Boolean {
        require(lesson.id.isNotBlank()) { "lesson id must not be blank" }
        require(lesson.situation.isNotBlank()) { "lesson situation must not be blank" }
        require(lesson.lesson.isNotBlank()) { "lesson insight must not be blank" }
        val body = buildString {
            append("SITUATION: ").append(lesson.situation).append('\n')
            append("ACTION: ").append(lesson.action).append('\n')
            append("OUTCOME: ").append(lesson.outcome).append('\n')
            append("LESSON: ").append(lesson.lesson)
            if (lesson.tags.isNotEmpty()) append("\nTAGS: ").append(lesson.tags.joinToString(", "))
        }
        return index.addDocument(Doc(lesson.id, body))
    }

    /** Search captured lessons. Delegates to the corpus index (ranked, offline, deterministic). */
    fun search(query: String, maxResults: Int = 8): List<SearchHit> = index.search(query, maxResults)
}

/**
 * One immutable, reusable experience record. [action]/[outcome]/[tags] may be empty; [id],
 * [situation], and [lesson] are mandatory (fail-closed).
 */
data class Lesson(
    val id: String,
    val situation: String,
    val action: String = "",
    val outcome: String = "",
    val lesson: String,
    val tags: List<String> = emptyList(),
)