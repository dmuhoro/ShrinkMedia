package com.shrinkmedia.compressor.forge

import com.shrinkmedia.compressor.forge.EcosystemIndex.Doc
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class EcosystemIndexTest {

    private fun corpus(): EcosystemIndex {
        val idx = EcosystemIndex()
        idx.addDocuments(
            listOf(
                Doc("sop", "The SOP governs how we build. Every task follows intake, PRD, architecture, build, verify, review, evidence. Fail closed, never fail open."),
                Doc("adr-012", "Connected mode is OFF by default. A connected action needs explicit consent and explicit invocation. The default build declares no INTERNET."),
                Doc("adr-013", "DataBank is a separate repo. Forge is a separate program. The ecosystem speaks MCP as one standard protocol."),
                Doc("lessons", "Lessons are captured after every task so we never re-invent the wheel. Evidence over narrative. A green suite means correct code."),
            ),
        )
        return idx
    }

    @Test
    fun emptyCorpus_AnswersEmptyResults_NeverNull() {
        val idx = EcosystemIndex()
        val hits = idx.search("anything")
        assertTrue(hits.isEmpty())
        assertEquals(0, idx.documentCount)
        assertEquals(0, idx.chunkCount)
    }

    @Test
    fun blankQuery_AnswersEmptyResults_EvenWhenCorpusPresent() {
        val idx = corpus()
        assertTrue(idx.search("   ").isEmpty())
        assertTrue(idx.search("").isEmpty())
    }

    @Test
    fun search_FindsRelevantDoc_AndRanksItFirst() {
        val idx = corpus()
        val hits = idx.search("connected mode default off consent")
        assertEquals("adr-012", hits.first().docId)
    }

    @Test
    fun search_IsCaseInsensitiveAndPunctuationTolerant() {
        val idx = corpus()
        val hits = idx.search("Build: VERIFY, PRD, & FAIL-closed!")
        assertEquals("sop", hits.first().docId)
    }

    @Test
    fun search_NoMatch_ReturnsEmpty_NeverWholeCorpus() {
        val idx = corpus()
        val hits = idx.search("zzzzqwerty")
        assertTrue(hits.isEmpty())
    }

    @Test
    fun results_AreRanked_WithSnippets() {
        val idx = corpus()
        val hits = idx.search("separate repo program protocol")
        assertEquals("adr-013", hits.first().docId)
        assertTrue(hits.first().score > 0.0)
        assertTrue(hits.first().snippet.isNotBlank())
    }

    @Test
    fun addDocument_DuplicateId_IsIdempotent_ReturnsFalse_NoDuplicateChunks() {
        val idx = corpus()
        val before = idx.chunkCount
        val added = idx.addDocument(Doc("sop", "duplicate of sop body"))
        assertEquals(false, added)
        assertEquals(before, idx.chunkCount)
    }

    @Test
    fun addDocument_ReturnsTrue_AndIndexIncludesIt() {
        val idx = corpus()
        val added = idx.addDocument(Doc("new", "a brand new topic about quantum teleportation"))
        assertTrue(added)
        val hits = idx.search("quantum teleportation")
        assertEquals("new", hits.first().docId)
    }

    @Test
    fun maxResults_IsRespected() {
        val idx = corpus()
        val hits = idx.search("build", maxResults = 2)
        assertTrue(hits.size <= 2)
    }

    @Test
    fun maxResults_ZeroOrNegative_Throws() {
        val idx = corpus()
        assertThrows(IllegalArgumentException::class.java) { idx.search("build", maxResults = 0) }
        assertThrows(IllegalArgumentException::class.java) { idx.search("build", maxResults = -1) }
    }

    @Test
    fun longDocument_IsChunked() {
        val idx = EcosystemIndex()
        val long = Doc("big", "word ".repeat(2000)) // 10000 chars
        idx.addDocument(long)
        assertEquals(25, idx.chunkCount) // 10000 / 400
        assertEquals(1, idx.documentCount)
        val hits = idx.search("word")
        assertEquals("big", hits.first().docId)
    }
}