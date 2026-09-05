package com.shrinkmedia.compressor.personal

import org.junit.Assert.*
import org.junit.Test

class InstructionAiderTest {

    @Test
    fun `ocr failure refuses - never guesses blind`() {
        val result = InstructionAider.decide(transcript = null)
        assertTrue(result is ImageInsight.Refused)
        assertTrue((result as ImageInsight.Refused).reason.isNotBlank())
    }

    @Test
    fun `empty transcript asks a targeted question`() {
        val result = InstructionAider.decide(transcript = "  ")
        assertTrue(result is ImageInsight.NeedsClarification)
        assertTrue((result as ImageInsight.NeedsClarification).reason == "no text recognized")
    }

    @Test
    fun `task imperative in the note is self-explanatory`() {
        val result = InstructionAider.decide("merge my pdfs please")
        assertTrue(result is ImageInsight.SelfExplanatory)
        assertEquals("task requested in the note (see transcript)", (result as ImageInsight.SelfExplanatory).intent)
    }

    @Test
    fun `question markup is self-explanatory`() {
        val result = InstructionAider.decide("how does the cloud store my data?")
        assertTrue(result is ImageInsight.SelfExplanatory)
    }

    @Test
    fun `an answer to a clarification becomes the intent and proceeds`() {
        val ambiguous = InstructionAider.decide("this morning I was thinking")
        assertTrue(ambiguous is ImageInsight.NeedsClarification)

        val resolved = InstructionAider.answer(ambiguous as ImageInsight.NeedsClarification, "save it as a thought to the vault")
        assertTrue(resolved is ImageInsight.SelfExplanatory)
    }

    @Test
    fun `already-resolved insight is not re-decided`() {
        val decided = InstructionAider.decide("compress this image")
        val again = InstructionAider.answer(decided, "anything")
        assertSame(decided, again)
    }

    @Test
    fun `vault-intent wording is self-explanatory even without task verb`() {
        val result = InstructionAider.decide("an idea about the vault")
        assertTrue(result is ImageInsight.SelfExplanatory)
        assertTrue((result as ImageInsight.SelfExplanatory).intent.startsWith("captured thought"))
    }

    @Test
    fun `ambiguous prose without any signal asks one question`() {
        val result = InstructionAider.decide("the garden was quiet today")
        assertTrue(result is ImageInsight.NeedsClarification)
        assertTrue((result as ImageInsight.NeedsClarification).question.contains("save it as a thought"))
    }
}