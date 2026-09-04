package com.shrinkmedia.compressor.forge

/**
 * Deterministic Forge task state machine (zero AI) — the portable core of the
 * Forge orchestrator (ecosystem ADR-013 §5 L3/L4 + `Assets/forge-orchestrator-build-brief.md`
 * §2/§4). This is a pure, unit-testable state machine with no I/O and no network:
 * it only holds state and applies legal transitions. The AI agents (Planner/Buider/Reviewer)
 * are plugged in later via the [transition] hooks / external orchestration.
 *
 * Invariants (mirror the ShrinkMedia Constitution §1):
 * - **No silent drops.** A task can never silently vanish. Every terminal path is either
 *   [ForgeTaskStatus.MERGED] or [ForgeTaskStatus.BLOCKED] (routes to the human). A task that
 *   fails review past [maxAttempts] is BLOCKED, never auto-swallowed and never auto-retried
 *   forever.
 * - **Deterministic transitions.** A state change is only legal if it is in the allowed map.
 *   An illegal transition throws [IllegalArgumentException] (surfaced, not swallowed).
 * - **Fail closed.** Only explicitly allowed transitions succeed; everything else refuses.
 * - **Full history.** Every transition is appended to [history] with a reason, so the dashboard
 *   and evals can read the complete trail later.
 */
class ForgeTask private constructor(
    val taskId: String,
    val repo: String,
    val title: String,
    val fileScope: List<String>,
    val acceptanceCriteria: List<String>,
    val maxAttempts: Int,
    var status: ForgeTaskStatus,
    var attempts: Int,
    var assignedAgent: String?,
    var retrievedContext: List<String>,
    var reviewResult: String?,
    val history: MutableList<ForgeHistoryEntry>,
) {

    companion object {
        /**
         * Allowed transition map, keyed by `from` status. Fail-closed: a status has only the
         * transitions explicitly listed here; any other attempted transition is refused.
         */
        private val ALLOWED: Map<ForgeTaskStatus, Set<ForgeTaskStatus>> = mapOf(
            ForgeTaskStatus.QUEUED to setOf(ForgeTaskStatus.RETRIEVING),
            ForgeTaskStatus.RETRIEVING to setOf(ForgeTaskStatus.BUILDING),
            ForgeTaskStatus.BUILDING to setOf(ForgeTaskStatus.REVIEWING),
            ForgeTaskStatus.REVIEWING to setOf(
                ForgeTaskStatus.CHANGES_REQUESTED,
                ForgeTaskStatus.MERGED,
                ForgeTaskStatus.BLOCKED,
            ),
            ForgeTaskStatus.CHANGES_REQUESTED to setOf(ForgeTaskStatus.BUILDING),
            // MERGED and BLOCKED are terminal — no outgoing transitions (no silent resurrection).
            ForgeTaskStatus.MERGED to emptySet(),
            ForgeTaskStatus.BLOCKED to emptySet(),
        )

        /** Legal transitions out of [from]; empty if terminal. */
        fun allowedTransitions(from: ForgeTaskStatus): Set<ForgeTaskStatus> =
            ALLOWED[from] ?: throw IllegalArgumentException("unknown status: $from")

        /**
         * Create a new task in QUEUED state. [maxAttempts] is the number of failed review cycles
         * that triggers a BLOCKED state: the task is blocked on the [maxAttempts]-th failure
         * (`attempts >= maxAttempts`), so `maxAttempts = 1` blocks after the first failed review
         * and `maxAttempts = 2` tolerates one failed review then blocks. Must be >= 1 (fail-closed;
         * a task with no retry budget still goes through exactly one review before blocking).
         */
        fun create(
            taskId: String,
            repo: String,
            title: String,
            fileScope: List<String>,
            acceptanceCriteria: List<String>,
            maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
        ): ForgeTask {
            require(taskId.isNotBlank()) { "taskId must not be blank" }
            require(repo.isNotBlank()) { "repo must not be blank" }
            require(maxAttempts >= 1) { "maxAttempts must be >= 1, was $maxAttempts" }
            val t = ForgeTask(
                taskId = taskId,
                repo = repo,
                title = title,
                fileScope = fileScope,
                acceptanceCriteria = acceptanceCriteria,
                maxAttempts = maxAttempts,
                status = ForgeTaskStatus.QUEUED,
                attempts = 0,
                assignedAgent = null,
                retrievedContext = emptyList(),
                reviewResult = null,
                history = mutableListOf(),
            )
            t.history.add(ForgeHistoryEntry(ForgeTaskStatus.QUEUED, "task created"))
            return t
        }

        private const val DEFAULT_MAX_ATTEMPTS = 2
    }

    val isTerminal: Boolean
        get() = status == ForgeTaskStatus.MERGED || status == ForgeTaskStatus.BLOCKED

    /**
     * Advance to [target], recording history. Enforces the allowed-transition map (fail-closed):
     * if [target] is not a legal transition from the current status, this throws and the state is
     * left unchanged.
     */
    fun transition(target: ForgeTaskStatus, reason: String) {
        if (target !in allowedTransitions(status)) {
            throw IllegalArgumentException(
                "illegal ForgeTask transition $status -> $target" +
                    " (task $taskId); allowed: ${allowedTransitions(status)}",
            )
        }
        status = target
        history.add(ForgeHistoryEntry(target, reason))
    }

    /** Advance through the wait-free chain: QUEUED -> RETRIEVING -> BUILDING -> REVIEWING. */
    fun advanceToReviewing(retrieved: List<String>, agent: String) {
        require(agent.isNotBlank()) { "agent must not be blank" }
        if (status != ForgeTaskStatus.QUEUED && status != ForgeTaskStatus.BUILDING) {
            throw IllegalArgumentException(
                "advanceToReviewing requires QUEUED or BUILDING, was $status (task $taskId)",
            )
        }
        when (status) {
            ForgeTaskStatus.QUEUED -> {
                transition(ForgeTaskStatus.RETRIEVING, "retrieval started")
                retrievedContext = retrieved
                transition(ForgeTaskStatus.BUILDING, "builder assigned: $agent")
                assignedAgent = agent
                transition(ForgeTaskStatus.REVIEWING, "build -> review")
            }
            ForgeTaskStatus.BUILDING -> {
                transition(ForgeTaskStatus.REVIEWING, "rebuild -> review")
            }
            else -> throw IllegalStateException("unreachable")
        }
    }

    /**
     * Record a review decision. A `pass` merges; a non-pass raises attempts and requests changes;
     * if attempts now exceed [maxAttempts] the task is BLOCKED (no silent drop — explicitly
     * surfaced, routes to the human, never auto-retried).
     */
    fun recordReview(reviewer: String, passed: Boolean, notes: List<String>) {
        if (status != ForgeTaskStatus.REVIEWING) {
            throw IllegalArgumentException("recordReview requires REVIEWING, was $status (task $taskId)")
        }
        reviewResult = if (passed) "pass" else "changes_requested: ${notes.joinToString("; ")}"
        if (passed) {
            transition(ForgeTaskStatus.MERGED, "reviewer $reviewer approved")
        } else {
            attempts += 1
            if (attempts >= maxAttempts) {
                transition(ForgeTaskStatus.BLOCKED, "exceeded maxAttempts=$maxAttempts at ${attempts} attempts")
            } else {
                transition(ForgeTaskStatus.CHANGES_REQUESTED, "reviewer $reviewer requested changes: ${notes.joinToString("; ")}")
            }
        }
    }

    override fun toString(): String =
        "ForgeTask($taskId, repo=$repo, status=$status, attempts=$attempts, terminal=$isTerminal)"
}

enum class ForgeTaskStatus {
    QUEUED,
    RETRIEVING,
    BUILDING,
    REVIEWING,
    CHANGES_REQUESTED,
    MERGED,
    BLOCKED,
}

/** One immutable step in the task's lifetime trail. */
data class ForgeHistoryEntry(
    val status: ForgeTaskStatus,
    val note: String,
)
