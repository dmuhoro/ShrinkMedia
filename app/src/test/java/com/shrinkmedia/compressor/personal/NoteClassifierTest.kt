package com.shrinkmedia.compressor.personal

import org.junit.Assert.*
import org.junit.Test

class NoteRecordTest {

    @Test
    fun `blank text is refused at construction - no silent drop`() {
        assertThrows(IllegalArgumentException::class.java) {
            NoteRecord("a", NoteSource.TYPED, "   ", 0L)
        }
    }

    @Test
    fun `blank id is refused at construction`() {
        assertThrows(IllegalArgumentException::class.java) {
            NoteRecord(" ", NoteSource.TYPED, "a valid thought", 0L)
        }
    }
}

class NoteClassifierTest {

    private fun note(id: String, text: String, at: Long, thread: String? = null) =
        NoteRecord(id = id, source = NoteSource.TYPED, text = text, notedAt = at, thread = thread)

    @Test
    fun `empty stream classifies to empty result - fail-closed`() {
        assertTrue(NoteClassifier.classify(emptyList()).isEmpty())
    }

    @Test
    fun `successor link - what came after is recorded`() {
        val a = note("a", "compression should live on device", 1L)
        val b = note("b", "compression must stay private", 2L)
        val out = NoteClassifier.classify(listOf(a, b))

        val linked = out.first { it.id == "a" }
        assertEquals("b", linked.followedBy)
    }

    @Test
    fun `reappearance is detected when the same idea shows up again`() {
        val a = note("a", "build a vault for my notes", 10L)
        val b = note("b", "recording a voice thought about the vault", 20L)
        val out = NoteClassifier.classify(listOf(a, b))

        val reappeared = out.first { it.id == "b" }
        assertEquals(20L, reappeared.reappearedAt)
        assertEquals("a", reappeared.thread)
    }

    @Test
    fun `unrelated ideas are not merged into one thread`() {
        val a = note("a", "webp compression quality", 1L)
        val b = note("b", "maslow hierarchy growth", 2L)
        val out = NoteClassifier.classify(listOf(a, b))
        assertNull(out.first { it.id == "a" }.followedBy)
        assertNull(out.first { it.id == "b" }.reappearedAt)
    }

    @Test
    fun `reappearance respects chronological order`() {
        val a = note("a", "vault idea", 100L)
        val b = note("b", "the vault idea again", 50L)
        val out = NoteClassifier.classify(listOf(a, b))
        // b is earlier, so a cannot re-appear in b (only later notes re-appear)
        assertNull(out.first { it.id == "b" }.reappearedAt)
    }

    @Test
    fun `thread id hints merge - explicit thread wins even without shared marker`() {
        val a = note("a", "alpha idea", 1L, thread = "t1")
        val b = note("b", "completely different words", 2L, thread = "t1")
        val out = NoteClassifier.classify(listOf(a, b))
        assertEquals("b", out.first { it.id == "a" }.followedBy)
    }

    @Test
    fun `marker overlap requires substantive tokens - three letters minimum`() {
        assertTrue(NoteClassifier.overlap("onboard the vault", "vault storage"))
        assertFalse(NoteClassifier.overlap("ab", "ab"))
        assertFalse(NoteClassifier.overlap("", "xyz"))
    }
}