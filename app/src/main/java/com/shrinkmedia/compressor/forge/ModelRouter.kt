package com.shrinkmedia.compressor.forge

import com.shrinkmedia.compressor.ConnectedRepository

/**
 * The fail-closed decision seam for the open-source-AI edge (ADR-014): given the Connected-mode
 * preconditions (ADR-012) + runtime connectivity + the lists of available remote (open-weight) and
 * local (on-device/home-lab) models, return a typed [RouterDecision] — never `null`, never silent.
 *
 * HONESTY BOUNDARY (ADR-014): this decides the *route only* — it performs NO network call, NO
 * inference, and this repo still declares NO INTERNET in the default build (CI-guarded). The real
 * transport lives behind explicit Connected-mode consent + the later DataBank/Forge connected
 * flavor (ADR-013 L2). What is proven here is the decision logic on the seam that the later real
 * transport will sit behind.
 *
 * Edge (the owner's insight): online ⇒ use the best **free, open-weight** remote model; offline ⇒
 * fall back to **local** AI (on-device Nano where available, else the home-lab Ollama host). No
 * vendor lock-in, no per-token vendor cost, and it still works with no internet. Fails closed.
 */
object ModelRouter {

    /**
     * Choose the model route. Mirrors [ConnectedRepository.run]'s preconditions exactly (fail-closed):
     * - Connected mode OFF (or not consented, or not explicitly invoked) ⇒ [RouterDecision.Off]/Refused
     *   and the routing block context is never produced.
     * - Otherwise: online ⇒ prefer the best available open-weight remote model;
     *   offline ⇒ fall back to the best available local model.
     * - A requested route with no available model ⇒ [RouterDecision.Unavailable] with a reason
     *   (no silent drop).
     */
    fun route(
        connectedModeEnabled: Boolean,
        consentShown: Boolean,
        explicitlyInvoked: Boolean,
        online: Boolean,
        availableRemote: List<String> = emptyList(),
        availableLocal: List<String> = emptyList(),
        preferredRemote: String? = null,
        preferredLocal: String? = null,
    ): RouterDecision {
        // --- connected-mode preconditions (the invariable gate, fail-closed) ---
        if (!connectedModeEnabled) return RouterDecision.Off("connected mode is OFF (default)")
        if (!consentShown) return RouterDecision.Refused("connected-mode disclosure not acknowledged")
        if (!explicitlyInvoked) {
            return RouterDecision.Refused("non-explicit/silent routing is refused")
        }

        return when {
            online -> {
                val chosen = pick(availableRemote, preferredRemote) ?: return RouterDecision.Unavailable(
                    "online but no open-weight remote model available (have: ${availableRemote.ifEmpty { "none" }})",
                )
                RouterDecision.AllowedRemote(chosen)
            }
            else -> {
                // offline: prioritize on-device over host if both available; chose preferredLocal if present.
                val localPool = availableLocal
                val chosen = pick(localPool, preferredLocal) ?: return RouterDecision.Unavailable(
                    "offline and no local model available (have: ${localPool.ifEmpty { "none" }}) — cannot route",
                )
                RouterDecision.AllowedLocal(chosen)
            }
        }
    }

    /** Pick [preferred] if present in [pool], else the first entry of [pool]. */
    private fun pick(pool: List<String>, preferred: String?): String? {
        if (pool.isEmpty()) return null
        if (preferred != null && preferred in pool) return preferred
        return pool.first()
    }
}

/** Typed routing decision — never null; every refusal carries a reason. */
sealed class RouterDecision {

    /** Route to the best free, open-weight remote model while online. */
    data class AllowedRemote(val model: String) : RouterDecision()

    /** Fallback to a local model when offline. */
    data class AllowedLocal(val model: String) : RouterDecision()

    /** Connected mode is OFF (the default) — the whole feature refuses. */
    data class Off(val reason: String) : RouterDecision()

    /** Connected but the preconditions (consent / explicit invocation) failed. */
    data class Refused(val reason: String) : RouterDecision()

    /** A route was reachable but no model is available on it. */
    data class Unavailable(val reason: String) : RouterDecision()

    val allowed: Boolean get() = this is AllowedRemote || this is AllowedLocal
}