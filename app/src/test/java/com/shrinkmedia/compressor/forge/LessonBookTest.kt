package com.shrinkmedia.compressor.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonBookTest {

    private fun lesson() = Lesson(
        id = "shrink-001",
        situation = "Add fail-closed gate to the batch compression path",
        action = "Insert guard at executeBatchProcessing boundary, not in a test helper",
        outcome = "Queued item held at pause gate, never dropped; typed refusal surfaces reason",
        lesson = "Enforcement must live where the production action runs, not in a helper the backtest uses.",
        tags = listOf("fail-closed", "batch", "boundary"),
    )

    @Test
    fun add_AndSearch_RoundTrip() {
        val book = LessonBook()
        assertTrue(book.add(lesson()))
        assertEquals(1, book.lessonCount)
        val hits = book.search("fail-closed batch boundary")
        assertEquals("shrink-001", hits.first().docId)
        assertTrue(hits.first().snippet.contains("SITUATION"))
    }

    @Test
    fun add_DuplicateId_ReturnsFalse_DoesNotDuplicate() {
        val book = LessonBook()
        assertTrue(book.add(lesson()))
        assertFalse(book.add(lesson()))
        assertEquals(1, book.lessonCount)
    }

    @Test
    fun add_BlankMandatoryFields_Throws() {
        val book = LessonBook()
        assertThrows(IllegalArgumentException::class.java) {
            book.add(lesson().copy(id = "  "))
        }
        assertThrows(IllegalArgumentException::class.java) {
            book.add(lesson().copy(situation = ""))
        }
        assertThrows(IllegalArgumentException::class.java) {
            book.add(lesson().copy(lesson = ""))
        }
        assertEquals(0, book.lessonCount)
    }

    @Test
    fun search_NoMatch_ReturnsEmpty_NeverNull() {
        val book = LessonBook()
        book.add(lesson())
        assertTrue(book.search("quantum waffles").isEmpty())
    }

    @Test
    fun search_Blank_ReturnsEmpty() {
        val book = LessonBook()
        book.add(lesson())
        assertTrue(book.search("   ").isEmpty())
    }

    @Test
    fun tags_AreSearchable() {
        val book = LessonBook()
        book.add(lesson())
        val hits = book.search("boundary")
        assertEquals("shrink-001", hits.first().docId)
    }
}