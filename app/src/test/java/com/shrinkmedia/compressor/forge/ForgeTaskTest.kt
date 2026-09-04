package com.shrinkmedia.compressor.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class ForgeTaskTest {

    private fun task(maxAttempts: Int = 2) = ForgeTask.create(
        taskId = "shrin-0001",
        repo = "shrinkmedia",
        title = "Add fail-closed gate to batch path",
        fileScope = listOf("app/src/main/java/com/shrinkmedia/compressor/BatchCompressionService.kt"),
        acceptanceCriteria = listOf("No silent drops", "Tests pass"),
        maxAttempts = maxAttempts,
    )

    @Test
    fun newTask_IsQueued_NotTerminal_AndHasHistory() {
        val t = task()
        assertEquals(ForgeTaskStatus.QUEUED, t.status)
        assertFalse(t.isTerminal)
        assertEquals(1, t.history.size)
        assertEquals("task created", t.history[0].note)
        assertTrue(t.acceptanceCriteria.isNotEmpty())
        assertFalse(t.fileScope.isEmpty())
    }

    @Test
    fun advanceToReviewing_FromQueued_ChainsQueuedRetrievingBuildingReviewing() {
        val t = task()
        t.advanceToReviewing(retrieved = listOf("chunk: fail-closed", "chunk: ADR-012"), agent = "builder-alpha")
        assertEquals(ForgeTaskStatus.REVIEWING, t.status)
        assertEquals(listOf("chunk: fail-closed", "chunk: ADR-012"), t.retrievedContext)
        assertEquals("builder-alpha", t.assignedAgent)
        assertEquals(
            listOf(
                ForgeTaskStatus.QUEUED,
                ForgeTaskStatus.RETRIEVING,
                ForgeTaskStatus.BUILDING,
                ForgeTaskStatus.REVIEWING,
            ),
            t.history.map { it.status },
        )
    }

    @Test
    fun advanceToReviewing_FromBuilding_OnlyMovesToReviewing() {
        val t = task()
        t.advanceToReviewing(retrieved = emptyList(), agent = "b1") // now REVIEWING with a stale agent
        // Force back to BUILDING via a code-review changes_requested cycle:
        t.recordReview(reviewer = "r1", passed = false, notes = listOf("fix scope"))
        assertEquals(ForgeTaskStatus.CHANGES_REQUESTED, t.status)
        t.transition(ForgeTaskStatus.BUILDING, "rebuild")
        t.advanceToReviewing(retrieved = emptyList(), agent = "b2")
        assertEquals(ForgeTaskStatus.REVIEWING, t.status)
    }

    @Test
    fun review_pass_Merges_AndBecomesTerminal() {
        val t = task()
        t.advanceToReviewing(retrieved = emptyList(), agent = "b1")
        t.recordReview(reviewer = "r1", passed = true, notes = emptyList())
        assertEquals(ForgeTaskStatus.MERGED, t.status)
        assertTrue(t.isTerminal)
        assertEquals("pass", t.reviewResult)
    }

    @Test
    fun review_fail_RequestsChanges_IncrementsAttempts_NotTerminal() {
        val t = task(maxAttempts = 2)
        t.advanceToReviewing(retrieved = emptyList(), agent = "b1")
        t.recordReview(reviewer = "r1", passed = false, notes = listOf("needs i18n"))
        assertEquals(ForgeTaskStatus.CHANGES_REQUESTED, t.status)
        assertEquals(1, t.attempts)
        assertFalse(t.isTerminal)
        assertTrue(t.reviewResult!!.startsWith("changes_requested"))
    }

    @Test
    fun repeatedFails_PastMaxAttempts_Blocked_NoSilentDrop() {
        val t = task(maxAttempts = 1) // only 1 retry budget before blocking
        t.advanceToReviewing(retrieved = emptyList(), agent = "b1")
        t.recordReview(reviewer = "r1", passed = false, notes = listOf("fails acceptance"))
        // attempts now 1 > 1 -> blocked
        assertEquals(ForgeTaskStatus.BLOCKED, t.status)
        assertTrue(t.isTerminal)
        assertEquals(1, t.attempts)
        // BLOCKED must surface explicitly in history with the reason
        val last = t.history.last()
        assertEquals(ForgeTaskStatus.BLOCKED, last.status)
        assertTrue(last.note.contains("maxAttempts=1"))
    }

    @Test
    fun blocked_IsTerminal_AndRejectsFurtherTransitions() {
        val t = task(maxAttempts = 1)
        t.advanceToReviewing(retrieved = emptyList(), agent = "b1")
        t.recordReview(reviewer = "r1", passed = false, notes = listOf("x"))
        assertEquals(ForgeTaskStatus.BLOCKED, t.status)
        assertThrows(IllegalArgumentException::class.java) {
            t.advanceToReviewing(retrieved = emptyList(), agent = "b2")
        }
    }

    @Test
    fun merged_IsTerminal_AndRejectsResurrection() {
        val t = task()
        t.advanceToReviewing(retrieved = emptyList(), agent = "b1")
        t.recordReview(reviewer = "r1", passed = true, notes = emptyList())
        assertEquals(ForgeTaskStatus.MERGED, t.status)
        assertThrows(IllegalArgumentException::class.java) {
            t.advanceToReviewing(retrieved = emptyList(), agent = "b2")
        }
    }

    @Test
    fun illegalTransition_Throws_AndLeavesStateUnchanged() {
        val t = task()
        assertThrows(IllegalArgumentException::class.java) {
            t.transition(ForgeTaskStatus.MERGED, "skip straight to merged")
        }
        assertEquals(ForgeTaskStatus.QUEUED, t.status)
    }

    @Test
    fun recordReview_RequiresReviewing() {
        val t = task()
        assertThrows(IllegalArgumentException::class.java) {
            t.recordReview(reviewer = "r1", passed = true, notes = emptyList())
        }
        assertEquals(ForgeTaskStatus.QUEUED, t.status)
    }

    @Test
    fun create_RejectsBlankIds_AndZeroMaxAttempts() {
        assertThrows(IllegalArgumentException::class.java) {
            ForgeTask.create("", "repo", "title", listOf("a"), listOf("b"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            ForgeTask.create("id", "", "title", listOf("a"), listOf("b"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            ForgeTask.create("id", "repo", "title", listOf("a"), listOf("b"), maxAttempts = 0)
        }
    }

    @Test
    fun allowedTransitions_FailClosed_TerminalHasNoOutgoing() {
        assertTrue(ForgeTask.allowedTransitions(ForgeTaskStatus.MERGED).isEmpty())
        assertTrue(ForgeTask.allowedTransitions(ForgeTaskStatus.BLOCKED).isEmpty())
        assertTrue(ForgeTaskStatus.CHANGES_REQUESTED in ForgeTask.allowedTransitions(ForgeTaskStatus.REVIEWING))
        assertTrue(ForgeTaskStatus.MERGED in ForgeTask.allowedTransitions(ForgeTaskStatus.REVIEWING))
    }
}
