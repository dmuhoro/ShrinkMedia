package com.shrinkmedia.compressor.personal

import com.shrinkmedia.compressor.forge.EcosystemIndex
import org.junit.Assert.*
import org.junit.Test

class PersonalIntelligenceAgentTest {

    private fun agent(
        seed: List<Pair<String, String>> = emptyList(),
        now: Long = 1_000L,
    ): PersonalIntelligenceAgent {
        val index = EcosystemIndex()
        index.addDocuments(seed.map { (id, body) -> EcosystemIndex.Doc(id, body) })
        return PersonalIntelligenceAgent(index, now = { now })
    }

    private fun tickingAgent(seed: List<Pair<String, String>> = emptyList()): PersonalIntelligenceAgent {
        val index = EcosystemIndex()
        index.addDocuments(seed.map { (id, body) -> EcosystemIndex.Doc(id, body) })
        var tick = 900L
        return PersonalIntelligenceAgent(index, now = { tick += 100; tick })
    }

    @Test
    fun `blank input refuses - no silent answer`() {
        val a = agent()
        val decision = a.ask("   ", NoteSource.TYPED)
        assertTrue(decision is Decision.Refused)
        assertTrue((decision as Decision.Refused).reason.isNotBlank())
    }

    @Test
    fun `recall returns a corpus hit as evidence - never invents`() {
        val a = agent(seed = listOf("d1" to "the vault stores voice thoughts and photos on my own server"))
        val decision = a.ask("where does the vault store my thoughts?", NoteSource.TYPED)
        assertTrue(decision is Decision.Recall)
        assertEquals("d1", (decision as Decision.Recall).hit.docId)
    }

    @Test
    fun `first-time thought is classified and stored in the vault`() {
        val a = agent()
        val decision = a.ask("build a compounding ecosystem for myself", NoteSource.VOICE)
        assertTrue(decision is Decision.SaveToVault)
        val record = (decision as Decision.SaveToVault).record
        assertEquals(NoteSource.VOICE, record.source)
        assertEquals(1_000L, record.notedAt)
        assertEquals(1, a.vaultSize)
    }

    @Test
    fun `learn intent routes to EasyTutor - education stays in its domain`() {
        val a = agent()
        val decision = a.ask("teach me about probability theory", NoteSource.TYPED)
        assertTrue(decision is Decision.Learn)
        assertEquals("probability theory", (decision as Decision.Learn).topic)
    }

    @Test
    fun `explore question routes to EasyTutor when below the length cap`() {
        val a = agent()
        val decision = a.ask("how do transformers work?", NoteSource.TYPED)
        assertTrue(decision is Decision.Learn)
        assertTrue((decision as Decision.Learn).topic.startsWith("how do transformers"))
    }

    @Test
    fun `corpus learning is absorbed - later question can recall it`() {
        val a = agent()
        a.ask("my data should live on my own server and never leave it", NoteSource.TYPED)
        val decision = a.ask("where should my data live?", NoteSource.TYPED)
        assertTrue(decision is Decision.Recall)
    }

    @Test
    fun `reconcile threads links successor and re-appearance across the vault`() {
        val a = tickingAgent()
        a.ask("idea one about the vault", NoteSource.TYPED)
        a.ask("idea about the vault again later", NoteSource.VOICE)
        val reconciled = a.reconcileThreads()
        val newest = reconciled.maxByOrNull { it.notedAt }
        assertNotNull(newest)
        assertNotNull(newest!!.reappearedAt)
        assertEquals(2, reconciled.size)
    }

    @Test
    fun `absorb blank record is refused by construction - fail dosed`() {
        assertThrows(IllegalArgumentException::class.java) {
            NoteRecord("x", NoteSource.PHOTO, "", 0L)
        }
    }
}