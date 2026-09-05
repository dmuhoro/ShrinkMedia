package com.shrinkmedia.compressor.ecosystem

enum class HostTier(val label: String) {
    T0_TINY("legacy-laptop / lite appliance: SQLite+FTS5, single systemd service, minimal footprint"),
    T1_MID("mid desktop / mini-PC: SQLite+FTS5 tuned, optional containers, moderate index work"),
    T2_HEAVY("homelab server: optional Postgres+pgvector, concurrency, model-runtime ready"),
}

data class HostSpec(
    val cpuCores: Int,
    val ramMb: Long,
    val hasSsd: Boolean = false,
) {
    init {
        require(cpuCores > 0) { "cpuCores must be > 0" }
        require(ramMb > 0L) { "ramMb must be > 0" }
    }
}

enum class DeploymentBackend { SQLITE_FTS5, POSTGRES_PGVECTOR }

enum class Isolation { NATIVE_SYSTEMD, CONTAINERS }

data class DeploymentProfile(
    val tier: HostTier,
    val backend: DeploymentBackend,
    val isolation: Isolation,
    val concurrency: Int,
    val modelRuntime: Boolean,
    val note: String,
)

object HostClassifier {

    const val MIN_RAM_MB = 1536L
    const val MIN_CORES = 2
    const val T1_RAM_MB = 4096L
    const val T1_CORES = 4
    const val T2_RAM_MB = 16384L
    const val T2_CORES = 8
    const val CONTAINER_RAM_MB = 8192L

    fun supportsBackend(tier: HostTier, backend: DeploymentBackend): Boolean = when (tier) {
        HostTier.T0_TINY, HostTier.T1_MID -> backend == DeploymentBackend.SQLITE_FTS5
        HostTier.T2_HEAVY -> true
    }

    fun refusalReason(spec: HostSpec): String? = when {
        spec.cpuCores < MIN_CORES -> "CPU too weak ($spec.cpuCores < $MIN_CORES cores)"
        spec.ramMb < MIN_RAM_MB -> "RAM too low (${spec.ramMb}MB < $MIN_RAM_MB MB)"
        else -> null
    }

    fun tierFor(spec: HostSpec): HostTier? {
        refusalReason(spec)?.let { return null }
        return when {
            spec.cpuCores >= T2_CORES && spec.ramMb >= T2_RAM_MB -> HostTier.T2_HEAVY
            spec.cpuCores >= T1_CORES && spec.ramMb >= T1_RAM_MB -> HostTier.T1_MID
            else -> HostTier.T0_TINY
        }
    }

    fun recommendedProfile(spec: HostSpec): DeploymentProfile? {
        val tier = tierFor(spec) ?: return null
        return when (tier) {
            HostTier.T0_TINY -> DeploymentProfile(
                tier = tier,
                backend = DeploymentBackend.SQLITE_FTS5,
                isolation = Isolation.NATIVE_SYSTEMD,
                concurrency = 1,
                modelRuntime = false,
                note = "headless vault unit on legacy hardware; zram swap advised; no containers on tight RAM",
            )
            HostTier.T1_MID -> DeploymentProfile(
                tier = tier,
                backend = DeploymentBackend.SQLITE_FTS5,
                isolation = if (spec.ramMb >= CONTAINER_RAM_MB) Isolation.CONTAINERS else Isolation.NATIVE_SYSTEMD,
                concurrency = 2,
                modelRuntime = false,
                note = "tuned index worker; Podman only when RAM allows (>=${CONTAINER_RAM_MB}MB), else systemd",
            )
            HostTier.T2_HEAVY -> DeploymentProfile(
                tier = tier,
                backend = DeploymentBackend.POSTGRES_PGVECTOR,
                isolation = Isolation.CONTAINERS,
                concurrency = 4,
                modelRuntime = true,
                note = "full server; Postgres+pgvector is the recommended vault backend here (SQLite+FTS5 stays supported by choice)",
            )
        }
    }
}