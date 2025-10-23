package com.lamontlabs.quantravision.tests.unit

import com.lamontlabs.quantravision.billing.HighlightQuotaManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit test: Highlight quota behavior (free vs pro mode)
 */
class TestHighlightQuota {

    private lateinit var quota: HighlightQuotaManager

    @Before
    fun setup() {
        quota = HighlightQuotaManager()
        quota.resetQuota()
    }

    @Test
    fun test_initialQuotaFree() {
        assertEquals(5, quota.remaining)
        assertFalse(quota.isPro)
    }

    @Test
    fun test_consumeQuota() {
        repeat(3) { quota.consume() }
        assertEquals(2, quota.remaining)
    }

    @Test
    fun test_quotaLockAfterLimit() {
        repeat(5) { quota.consume() }
        assertTrue(quota.locked)
    }

    @Test
    fun test_upgradeUnlocksQuota() {
        quota.upgradeToPro()
        assertTrue(quota.isPro)
        assertEquals(Int.MAX_VALUE, quota.remaining)
    }
}
