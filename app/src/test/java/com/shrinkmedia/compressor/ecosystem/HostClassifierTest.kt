package com.shrinkmedia.compressor.ecosystem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HostClassifierTest {

    private fun elitebook(ramMb: Long) = HostSpec(cpuCores = 2, ramMb = ramMb, hasSsd = false)

    @Test
    fun `hp elitebook 2540p stock 2GB classifies T0 tiny`() {
        val tier = HostClassifier.tierFor(elitebook(2048L))
        assertEquals(HostTier.T0_TINY, tier)
        val profile = HostClassifier.recommendedProfile(elitebook(2048L))!!
        assertEquals(DeploymentBackend.SQLITE_FTS5, profile.backend)
        assertEquals(Isolation.NATIVE_SYSTEMD, profile.isolation)
        assertEquals(1, profile.concurrency)
        assertFalse(profile.modelRuntime)
    }

    @Test
    fun `hp elitebook 2540p upgraded to 8GB stays T0 because only 2 cores`() {
        val tier = HostClassifier.tierFor(elitebook(8192L))
        assertEquals(HostTier.T0_TINY, tier)
        assertEquals(DeploymentBackend.SQLITE_FTS5, HostClassifier.recommendedProfile(elitebook(8192L))!!.backend)
    }

    @Test
    fun `mid desktop 8 cores and 8GB classifies T1 with containers`() {
        val spec = HostSpec(cpuCores = 8, ramMb = 8192L, hasSsd = true)
        assertEquals(HostTier.T1_MID, HostClassifier.tierFor(spec))
        val profile = HostClassifier.recommendedProfile(spec)!!
        assertEquals(Isolation.CONTAINERS, profile.isolation)
        assertEquals(2, profile.concurrency)
        assertFalse(profile.modelRuntime)
    }

    @Test
    fun `mid desktop 4 cores and 4GB classifies T1 with native systemd on tight RAM`() {
        val spec = HostSpec(cpuCores = 4, ramMb = 4096L)
        assertEquals(HostTier.T1_MID, HostClassifier.tierFor(spec))
        assertEquals(Isolation.NATIVE_SYSTEMD, HostClassifier.recommendedProfile(spec)!!.isolation)
    }

    @Test
    fun `gaming pc 16 cores and 32GB classifies T2 heavy with postgres`() {
        val spec = HostSpec(cpuCores = 16, ramMb = 32768L, hasSsd = true)
        assertEquals(HostTier.T2_HEAVY, HostClassifier.tierFor(spec))
        val profile = HostClassifier.recommendedProfile(spec)!!
        assertEquals(DeploymentBackend.POSTGRES_PGVECTOR, profile.backend)
        assertEquals(Isolation.CONTAINERS, profile.isolation)
        assertEquals(4, profile.concurrency)
        assertTrue(profile.modelRuntime)
    }

    @Test
    fun `8 cores and 16GB reaches T2 heavy at the threshold`() {
        val spec = HostSpec(cpuCores = 8, ramMb = 16384L)
        assertEquals(HostTier.T2_HEAVY, HostClassifier.tierFor(spec))
    }

    @Test
    fun `4 cores with plenty RAM stays T1 because cores gate T2`() {
        val spec = HostSpec(cpuCores = 4, ramMb = 32768L)
        assertEquals(HostTier.T1_MID, HostClassifier.tierFor(spec))
    }

    @Test
    fun `single core is refused with a reason`() {
        val spec = HostSpec(cpuCores = 1, ramMb = 4096L)
        assertNull(HostClassifier.tierFor(spec))
        assertNull(HostClassifier.recommendedProfile(spec))
        assertNotNull(HostClassifier.refusalReason(spec))
    }

    @Test
    fun `sub-1_5GB RAM is refused with a reason`() {
        val spec = HostSpec(cpuCores = 4, ramMb = 1024L)
        assertNull(HostClassifier.tierFor(spec))
        assertNotNull(HostClassifier.refusalReason(spec))
    }

    @Test
    fun `no refusal when tier is acceptable`() {
        assertNull(HostClassifier.refusalReason(elitebook(2048L)))
    }

    @Test
    fun `postgres backend requires T2 heavy - fail closed below it`() {
        val elite = HostSpec(cpuCores = 2, ramMb = 8192L)
        val mid = HostSpec(cpuCores = 8, ramMb = 8192L)
        val heavy = HostSpec(cpuCores = 16, ramMb = 32768L)
        assertFalse(HostClassifier.supportsBackend(HostClassifier.tierFor(elite)!!, DeploymentBackend.POSTGRES_PGVECTOR))
        assertFalse(HostClassifier.supportsBackend(HostClassifier.tierFor(mid)!!, DeploymentBackend.POSTGRES_PGVECTOR))
        assertTrue(HostClassifier.supportsBackend(HostClassifier.tierFor(heavy)!!, DeploymentBackend.POSTGRES_PGVECTOR))
    }

    @Test
    fun `sqlite backend is the universally supported default`() {
        for (tier in HostTier.entries) {
            assertTrue(HostClassifier.supportsBackend(tier, DeploymentBackend.SQLITE_FTS5))
        }
    }
}