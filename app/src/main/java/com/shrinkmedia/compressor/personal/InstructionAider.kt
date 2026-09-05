package com.shrinkmedia.compressor.personal

/**
 * Image Insight decision engine (ADR-015 §5): given what OCR could read from a scanned photo and
 * the owner's optional hint of intent, decide whether ShrinkMedia can proceed on its own or must
 * ask for clarification.
 *
 * HONESTY BOUNDARY — two distinct tiers:
 * - *Reading the words* out of a photo is REAL (ML Kit OCR, ADR-009, on-device).
 * - *"Vividly describing" what the image shows* (vision captioning) is ASPIRATIONAL (C11 wall —
 *   no AICore on the current handset). This engine never claims to see the image; it reasons only
 *   about the transcript it is given, on the seam where a later vision model would plug in.
 *
 * Fail-closed + no silent drops (Constitution §1, AGENTS §1):
 * - OCR failed (`transcript == null`) ⇒ [ImageInsight.Refused] — never guesses, never proceeds blind.
 * - No text found ⇒ [ImageInsight.NeedsClarification] with the targeted question.
 * - Self-explanatory intent ⇒ [ImageInsight.SelfExplanatory]; execution (classify + route) proceeds.
 * - Ambiguous ⇒ exactly ONE targeted question, then [answer] re-runs the engine.
 */
object InstructionAider {

    /**
     * Decide what to do with the scanned transcript. [intentHint] is the owner's answer to a
     * previous clarification (optional on first pass).
     */
    fun decide(transcript: String?, intentHint: String? = null): ImageInsight {
        if (transcript == null) {
            return ImageInsight.Refused("scan could not be read (OCR failed) — refusing to guess")
        }
        val text = transcript.trim()
        if (text.isEmpty()) {
            return ImageInsight.NeedsClarification(
                question = "I could not read any words in that image. Is it a note I should save, " +
                        "a task you want me to run, or something else?",
                reason = "no text recognized",
            )
        }
        if (!intentHint.isNullOrBlank()) {
            return ImageInsight.SelfExplanatory(text, intentHint.trim())
        }
        val v = verbsOf(text)
        return when {
            isTaskImperative(v) -> ImageInsight.SelfExplanatory(
                text,
                "task requested in the note (see transcript)"
            )
            isQuestion(text) -> ImageInsight.SelfExplanatory(text, "question — search the vault/corpus for an answer")
            hasVaultIntent(text) -> ImageInsight.SelfExplanatory(text, "captured thought — classify and save to vault")
            else -> ImageInsight.NeedsClarification(
                question = "I see \"${text.take(96)}…\". Should I save it as a thought (vault), " +
                        "search my knowledge for an answer, or treat it as a task to run?",
                reason = "intent ambiguous",
                transcript = text,
            )
        }
    }

    /** Re-run after the owner answers a clarification question — the answer becomes the intent. */
    fun answer(imageInsight: ImageInsight, answer: String): ImageInsight = when (imageInsight) {
        is ImageInsight.NeedsClarification -> decide(imageInsight.transcript, answer)
        else -> imageInsight
    }

    private fun isTaskImperative(verbs: Set<String>) =
        verbs.intersect(TASK_VERBS).isNotEmpty()

    private fun isQuestion(text: String) =
        text.trimEnd().endsWith("?") || text.contains(Regex("\\b(what|how|why|when|where|which|who)\\b"))

    private fun hasVaultIntent(text: String): Boolean {
        val t = text.lowercase()
        return t.contains("idea") || t.contains("thought") || t.contains("note") || t.contains("insight")
    }

    private fun verbsOf(text: String): Set<String> =
        (TASK_VERBS + QUESTION_HINTS)
            .filter { text.lowercase().contains(it) }
            .toSet()

    private val TASK_VERBS = setOf(
        "compress", "convert", "create", "build", "merge", "split", "extract", "scan",
        "save", "add", "remove", "delete", "reduce", "resize", "share", "record", "generate",
        "make", "rename", "organise", "organize", "summarise", "summarize", "search", "find",
    )

    private val QUESTION_HINTS = setOf("what", "how", "why", "when", "where", "which", "who", "do", "should", "can", "is", "are")
}

/** Typed result of the Image Insight decision — never null; every refusal/ambiguity is explicit. */
sealed class ImageInsight {
    /** The transcript is self-explanatory; execution proceeds. */
    data class SelfExplanatory(val transcript: String, val intent: String) : ImageInsight()

    /** One targeted question must be answered before we proceed (no silent guessing). */
    data class NeedsClarification(val question: String, val reason: String, val transcript: String? = null) : ImageInsight()

    /** We cannot proceed (OCR failed etc.) — explicit reason, no guess. */
    data class Refused(val reason: String) : ImageInsight()
}