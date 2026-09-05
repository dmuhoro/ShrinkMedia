package com.shrinkmedia.compressor.personal

import com.shrinkmedia.compressor.forge.EcosystemIndex

/**
 * The Personal Intelligence orchestrator (ADR-015 §4) — a typed, decision-only agent that answers
 * "what should the eco-system do with this question/thought?".
 *
 * Decisions (sealed, never null, every refusal carries a reason):
 * - [Decision.Recall] — the corpus already holds an answer; the best [EcosystemIndex.SearchHit] is
 *   shown as evidence (never an invented answer).
 * - [Decision.Clarify] — intent is ambiguous; exactly one targeted question is asked (no silent
 *   guessing).
 * - [Decision.SaveToVault] — first-time/new thought; it is classified (notedAt / reappearedAt /
 *   followedBy) and absorbed into the corpus.
 * - [Decision.Learn] — the owner wants to go deeper; the topic routes to EasyTutor (education stays
 *   in its domain — ADR-015 §6, anti-scope-creep).
 * - [Decision.Refused] — preconditions failed (blank input, unsupported mode) with the reason.
 *
 * HONESTY BOUNDARY: decision logic only — no network, no inference, no INTERNET (CI-guarded,
 * ADR-011/014). The real virtual-me brain / model transport is the later ModelRouter seam
 * (owner hardware). The corpus here is what the phone has captured so far.
 */
class PersonalIntelligenceAgent(
    private val corpus: EcosystemIndex,
    private val classifier: NoteClassifier = NoteClassifier,
    private val now: () -> Long = { System.currentTimeMillis() },
) {
    private val vault = mutableListOf<NoteRecord>()

    /** Route a free-text thought or question. [source] says where it was captured. */
    fun ask(text: String, source: NoteSource): Decision {
        if (text.isBlank()) return Decision.Refused("blank input — refuse to answer silently")
        val query = text.trim().take(MAX_QUERY_CHARS)

        // 1) Explicit learn intent — the owner wants to go master this: route to EasyTutor
        //    before anything else (education stays in its domain, ADR-015 §6).
        if (isLearnIntent(query)) {
            return Decision.Learn(learnTopic(query) ?: query)
        }

        // 2) Recall — do we already know the answer in this personal corpus?
        val hits = corpus.search(query, maxResults = 1)
        if (hits.isNotEmpty() && hits.first().score >= RECALL_SCORE_THRESHOLD) {
            return if (isQuestionLike(query)) {
                Decision.Recall(hits.first())
            } else {
                // Re-capture of an idea already known: it is STILL a new vault record (re-appearance),
                // never a silent drop. The classifier annotates reappearedAt on reconcile.
                Decision.SaveToVault(absorb(newRecord(query, source)), recallContext = hits.first())
            }
        }

        // 3) Otherwise it is a first-time thought to absorb into the vault + corpus.
        return Decision.SaveToVault(absorb(newRecord(query, source)), recallContext = hits.firstOrNull())
    }

    /** Record a note directly (e.g. OCR'd photo, typed capture) — returns the classified record. */
    fun absorb(record: NoteRecord): NoteRecord {
        val classified = classifier.classify(listOf(record))[0]
        vault.add(classified)
        corpus.addDocument(EcosystemIndex.Doc(classified.id, classified.text))
        return classified
    }

    /** Re-run the classifier over the whole vault (links successors + re-appearances). */
    fun reconcileThreads(): List<NoteRecord> {
        val sorted = vault.sortedBy { it.notedAt }
        val reconciled = classifier.classify(sorted)
        vault.clear()
        vault.addAll(reconciled)
        return reconciled
    }

    val vaultSize: Int get() = vault.size

    private fun newRecord(text: String, source: NoteSource) = NoteRecord(
        id = "n-${nextId++}",
        source = source,
        text = text,
        notedAt = now(),
    )

    private fun isLearnIntent(text: String): Boolean {
        val t = text.lowercase()
        return t.split(" ").any { it in LEARN_VERBS } ||
                t.contains("learn about") || t.contains("teach me") ||
                isDeepExplore(t)
    }

    /** "how does / why does / what is / explain" probe — a learning question, not a vault recall. */
    private fun isDeepExplore(t: String): Boolean =
        t.contains("how does") || t.contains("how do ") || t.contains("how do i") || t.contains("why does") ||
                t.contains("what is") || t.contains("what are") || t.contains("explain")

    private fun isQuestionLike(text: String): Boolean = text.trimEnd().endsWith("?")

    private fun learnTopic(text: String): String? {
        // Cut phrases like "teach me <topic>", "learn about <topic>", "... to <topic>" → topic remainder
        val t = text.trim()
        for (prefix in listOf("teach me about ", "teach me ", "learn about ", "master ", "study ")) {
            if (t.lowercase().startsWith(prefix)) return t.substring(prefix.length).trim().ifBlank { null }
        }
        return null
    }

    private var nextId = 0

    private companion object {
        const val MAX_QUERY_CHARS = 500
        const val RECALL_SCORE_THRESHOLD = 2.5
        val LEARN_VERBS = setOf(
            "learn", "teach", "understand", "study", "master", "explain", "deepen",
            "tutor", "educate", "train", "coach",
        )
    }
}

/** Typed routing decision of the Personal Intelligence agent. */
sealed class Decision {
    /** The corpus already answers this — evidence hit returned, never invented. */
    data class Recall(val hit: EcosystemIndex.SearchHit) : Decision()

    /** Intent ambiguous — ask exactly one targeted question before continuing. */
    data class Clarify(val question: String) : Decision()

    /** First-time thought — classified and stored in the vault + corpus. */
    data class SaveToVault(val record: NoteRecord, val recallContext: EcosystemIndex.SearchHit?) : Decision()

    /** Owner wants to go deeper — route the topic to EasyTutor (its own domain/product). */
    data class Learn(val topic: String) : Decision()

    /** Preconditions failed — explicit reason, no silent drop. */
    data class Refused(val reason: String) : Decision()
}