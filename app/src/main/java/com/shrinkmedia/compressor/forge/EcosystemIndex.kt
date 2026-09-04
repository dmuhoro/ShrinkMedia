package com.shrinkmedia.compressor.forge

/**
 * Deterministic, offline, keyword-searchable corpus index — the local seed of the ecosystem's
 * "search up the knowledge base like an extension" capability (ADR-014). It chunks each document,
 * builds an inverted index, and answers ranked keyword queries.
 *
 * HONESTY BOUNDARY (ADR-014, SOP §2): this is a **local, deterministic, offline** index — NOT
 * semantic retrieval + rerank (that is the later Forge RAG layer: Daftari `ai-context/` +
 * pgvector + an embedding model, in the Forge repo). No network, no I/O, no model calls. It
 * exists to be portable, unit-testable, and lift into the Forge program unchanged.
 *
 * Fail-closed + no silent drops:
 * - An empty corpus answers an **empty** result set (never `null`, never a crash).
 * - A blank query answers an empty result set, never the whole corpus.
 * - Unknown/unindexed terms simply score zero; results are ranked by relevance, best first.
 */
class EcosystemIndex {

    private val chunks = mutableListOf<Chunk>()
    private val index = mutableMapOf<String, MutableList<Posting>>()
    private var dirty = true

    /** Seed an initial corpus of documents (ids must be unique). */
    fun addDocuments(docs: List<Doc>): Int {
        docs.forEach { doc -> addDocument(doc) }
        return docs.size
    }

    /** Add one document; re-appending the same [Doc.id] is an explicit no-op (idempotent, no dup). */
    fun addDocument(doc: Doc): Boolean {
        if (chunks.any { it.docId == doc.id }) return false // idempotent, no duplicate-chunk blowup
        val docChunks = chunkDoc(doc)
        docChunks.forEach { chunks.add(it) }
        dirty = true
        return true
    }

    val documentCount: Int get() = chunks.map { it.docId }.distinct().size
    val chunkCount: Int get() = chunks.size

    /**
     * Ranked keyword search over the corpus. Returns up to [maxResults] [SearchHit]s sorted by
     * descending score (tie-break: doc id). Empty corpus OR blank query ⇒ empty list (fail-closed).
     */
    fun search(query: String, maxResults: Int = 8): List<SearchHit> {
        if (query.isBlank() || chunks.isEmpty()) return emptyList()
        if (maxResults <= 0) throw IllegalArgumentException("maxResults must be > 0")
        rebuildIfDirty()
        val terms = tokenize(query)
        if (terms.isEmpty()) return emptyList()

        // score per document: sum of term frequency in that doc, weighted by corpus rarity (idf).
        val scores = mutableMapOf<String, Double>()
        val docsOfTerm = mutableMapOf<String, Double>()
        for (term in terms) {
            val postings = index[term] ?: continue
            val df = postings.map { it.chunkIndex }.distinct().size.toDouble()
            val idf = if (df == 0.0) 0.0 else 1.0 + kotlin.math.ln(chunks.size / df)
            docsOfTerm[term] = idf
        }
        for (term in terms) {
            val postings = index[term] ?: continue
            val idf = docsOfTerm[term] ?: continue
            for (p in postings) {
                val chunk = chunks[p.chunkIndex]
                scores[chunk.docId] = (scores[chunk.docId] ?: 0.0) + p.tf * idf
            }
        }
        return scores.entries
            .sortedWith(compareByDescending<Map.Entry<String, Double>> { it.value }.thenBy { it.key })
            .take(maxResults)
            .map { (docId, score) ->
                SearchHit(docId = docId, score = score, snippet = snippetFor(docId))
            }
    }

    private fun rebuildIfDirty() {
        if (!dirty) return
        index.clear()
        chunks.forEachIndexed { chunkIndex, chunk ->
            tokenize(chunk.text).forEach { term ->
                val postings = index.getOrPut(term) { mutableListOf() }
                val existing = postings.firstOrNull { it.chunkIndex == chunkIndex }
                if (existing != null) existing.tf += 1 else postings.add(Posting(chunkIndex, 1))
            }
        }
        dirty = false
    }

    private fun snippetFor(docId: String): String {
        val chunk = chunks.firstOrNull { it.docId == docId } ?: return ""
        val first = chunk.text.trim()
        return if (first.length > MAX_SNIPPET) first.take(MAX_SNIPPET) + "…" else first
    }

    /** Chunk strategy: split on paragraphs, then hard-split paragraph runs over [MAX_CHUNK_CHARS]. */
    private fun chunkDoc(doc: Doc): List<Chunk> {
        val out = mutableListOf<Chunk>()
        val paragraphs = doc.body.split(Regex("\\n\\s*\\n"))
        for (para in paragraphs) {
            val trimmed = para.trim()
            if (trimmed.isEmpty()) continue
            if (trimmed.length <= MAX_CHUNK_CHARS) {
                out.add(Chunk(doc.id, trimmed))
            } else {
                trimmed.chunked(MAX_CHUNK_CHARS).forEach { out.add(Chunk(doc.id, it)) }
            }
        }
        return out
    }

    /** Tokenizer: lowercase, keep letters/digits, split on whitespace and punctuation. */
    private fun tokenize(text: String): List<String> =
        text.lowercase()
            .split(Regex("[^a-z0-9]+"))
            .filter { it.isNotBlank() }

    private data class Chunk(val docId: String, val text: String)
    private data class Posting(val chunkIndex: Int, var tf: Int)

    /** A single indexed document. [id] must be unique across a corpus. */
    data class Doc(val id: String, val body: String)

    /** A ranked search result: the document id, relevance score, and a text snippet. */
    data class SearchHit(val docId: String, val score: Double, val snippet: String)

    private companion object {
        const val MAX_CHUNK_CHARS = 400
        const val MAX_SNIPPET = 200
    }
}