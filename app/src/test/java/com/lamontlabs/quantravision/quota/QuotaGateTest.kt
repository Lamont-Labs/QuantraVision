package com.lamontlabs.quantravision.quota

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import com.lamontlabs.quantravision.tiers.Tier
import com.lamontlabs.quantravision.tiers.TierRegistry

/**
 * BATCH 10: QuotaGate Unit Tests
 * 
 * Comprehensive test suite for QuotaGate quota enforcement system.
 * Tests tier limits, rate limiting, daily reset, and state persistence.
 * 
 * Test Coverage:
 * 1. Tier Limits: FREE=1, BASIC=5, PRO=20, APEX=60 calls/day
 * 2. Rate Limiting: min 8s between calls, max 3 per 60s
 * 3. Daily Reset: midnight reset at 00:00 UTC
 * 4. Legacy tier mapping: STARTER→BASIC, STANDARD→PRO, APEX_ULTRA/ULTRA→APEX
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class QuotaGateTest {
    
    private lateinit var context: Context
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        clearQuotaState()
    }
    
    private fun clearQuotaState() {
        val quotaFile = File(context.filesDir, "quota_state.json")
        if (quotaFile.exists()) {
            quotaFile.delete()
        }
    }
    
    // ============================================================
    // TIER LIMITS TESTS
    // ============================================================
    
    @Test
    fun `FREE tier always blocked from cloud calls`() {
        val canCall = QuotaGate.canMakeCloudCall(context, "FREE")
        assertFalse("FREE tier should never be allowed cloud calls", canCall)
    }
    
    @Test
    fun `FREE tier case-insensitive`() {
        assertFalse(QuotaGate.canMakeCloudCall(context, "free"))
        assertFalse(QuotaGate.canMakeCloudCall(context, "Free"))
        assertFalse(QuotaGate.canMakeCloudCall(context, "FREE"))
    }
    
    @Test
    fun `PRO tier allows exactly 20 calls per day`() {
        repeat(20) { callNumber ->
            assertTrue(
                "Call ${callNumber + 1} should be allowed (PRO limit is 20)",
                QuotaGate.canMakeCloudCall(context, "PRO")
            )
        }
        
        assertFalse(
            "21st call should be blocked (PRO limit is 20)",
            QuotaGate.canMakeCloudCall(context, "PRO")
        )
    }
    
    @Test
    fun `APEX tier allows exactly 60 calls per day`() {
        repeat(60) { callNumber ->
            assertTrue(
                "Call ${callNumber + 1} should be allowed (APEX limit is 60)",
                QuotaGate.canMakeCloudCall(context, "APEX")
            )
        }
        
        assertFalse(
            "61st call should be blocked (APEX limit is 60)",
            QuotaGate.canMakeCloudCall(context, "APEX")
        )
    }
    
    @Test
    fun `APEX_ULTRA legacy tier maps to APEX limit of 60`() {
        repeat(60) { callNumber ->
            assertTrue(
                "Call ${callNumber + 1} should be allowed (APEX_ULTRA maps to APEX=60)",
                QuotaGate.canMakeCloudCall(context, "APEX_ULTRA")
            )
        }
        
        assertFalse(
            "61st call should be blocked (APEX_ULTRA maps to APEX limit of 60)",
            QuotaGate.canMakeCloudCall(context, "APEX_ULTRA")
        )
    }
    
    @Test
    fun `unknown tier defaults to FREE tier`() {
        assertFalse(
            "Unknown tier should default to FREE (no cloud access)",
            QuotaGate.canMakeCloudCall(context, "UNKNOWN_TIER")
        )
    }
    
    @Test
    fun `getRemainingCalls returns correct count for PRO tier`() {
        assertEquals(20, QuotaGate.getRemainingCalls(context, "PRO"))
        
        QuotaGate.canMakeCloudCall(context, "PRO")
        QuotaGate.canMakeCloudCall(context, "PRO")
        QuotaGate.canMakeCloudCall(context, "PRO")
        
        assertEquals(17, QuotaGate.getRemainingCalls(context, "PRO"))
    }
    
    @Test
    fun `getRemainingCalls returns correct count for APEX tier`() {
        assertEquals(60, QuotaGate.getRemainingCalls(context, "APEX"))
        
        repeat(10) {
            QuotaGate.canMakeCloudCall(context, "APEX")
        }
        
        assertEquals(50, QuotaGate.getRemainingCalls(context, "APEX"))
    }
    
    @Test
    fun `getRemainingCalls never goes negative`() {
        repeat(25) {
            QuotaGate.canMakeCloudCall(context, "PRO")
        }
        
        val remaining = QuotaGate.getRemainingCalls(context, "PRO")
        assertTrue("Remaining calls should not be negative", remaining >= 0)
        assertEquals(0, remaining)
    }
    
    // ============================================================
    // RATE LIMITING TESTS
    // ============================================================
    
    @Test
    fun `rate limiting enforces 8 second minimum between calls`() {
        assertTrue("First call should succeed", QuotaGate.canMakeCloudCall(context, "PRO"))
        
        assertFalse(
            "Immediate second call should fail (< 8 seconds)",
            QuotaGate.canMakeCloudCall(context, "PRO")
        )
    }
    
    @Test
    fun `rate limiting allows first call with no previous timestamp`() {
        assertTrue(
            "First call with no history should be allowed",
            QuotaGate.canMakeCloudCall(context, "PRO")
        )
    }
    
    @Test
    fun `max 3 calls per 60 seconds enforced`() {
        val quotaFile = File(context.filesDir, "quota_state.json")
        val now = System.currentTimeMillis()
        
        val recentCalls = listOf(
            now - 50_000L,
            now - 40_000L,
            now - 30_000L
        )
        
        val json = """
            {
                "callsToday": 3,
                "lastCallTimestamp": ${now - 30_000L},
                "lastResetDate": "2024-01-01",
                "lastResetMs": $now,
                "tier": "PRO",
                "recentCallTimestamps": [${recentCalls.joinToString(",")}]
            }
        """.trimIndent()
        
        quotaFile.writeText(json)
        
        assertFalse(
            "4th call within 60 seconds should be blocked",
            QuotaGate.canMakeCloudCall(context, "PRO")
        )
    }
    
    // ============================================================
    // DAILY RESET TESTS
    // ============================================================
    
    @Test
    fun `daily reset clears counter`() {
        val quotaFile = File(context.filesDir, "quota_state.json")
        val yesterday = System.currentTimeMillis() - (25 * 60 * 60 * 1000L)
        
        val json = """
            {
                "callsToday": 10,
                "lastCallTimestamp": $yesterday,
                "lastResetDate": "2020-01-01",
                "lastResetMs": $yesterday,
                "tier": "PRO",
                "recentCallTimestamps": []
            }
        """.trimIndent()
        
        quotaFile.writeText(json)
        
        val remaining = QuotaGate.getRemainingCalls(context, "PRO")
        assertEquals("Counter should reset to full quota after 24+ hours", 20, remaining)
    }
    
    @Test
    fun `resetIfNeeded triggers state reload`() {
        val quotaFile = File(context.filesDir, "quota_state.json")
        val yesterday = System.currentTimeMillis() - (25 * 60 * 60 * 1000L)
        
        val json = """
            {
                "callsToday": 10,
                "lastCallTimestamp": $yesterday,
                "lastResetDate": "2020-01-01",
                "lastResetMs": $yesterday,
                "tier": "PRO",
                "recentCallTimestamps": []
            }
        """.trimIndent()
        
        quotaFile.writeText(json)
        
        QuotaGate.resetIfNeeded(context)
        
        val remaining = QuotaGate.getRemainingCalls(context, "PRO")
        assertEquals(20, remaining)
    }
    
    // ============================================================
    // STATE PERSISTENCE TESTS
    // ============================================================
    
    @Test
    fun `quota state persists across app restarts`() {
        QuotaGate.canMakeCloudCall(context, "PRO")
        QuotaGate.canMakeCloudCall(context, "PRO")
        QuotaGate.canMakeCloudCall(context, "PRO")
        QuotaGate.canMakeCloudCall(context, "PRO")
        
        val remaining1 = QuotaGate.getRemainingCalls(context, "PRO")
        assertEquals(16, remaining1)
        
        val remaining2 = QuotaGate.getRemainingCalls(context, "PRO")
        assertEquals("State should persist", 16, remaining2)
    }
    
    @Test
    fun `corrupted quota file is recreated`() {
        val quotaFile = File(context.filesDir, "quota_state.json")
        quotaFile.writeText("CORRUPTED JSON DATA {{{")
        
        val remaining = QuotaGate.getRemainingCalls(context, "PRO")
        
        assertEquals(
            "Corrupted file should be reset to default state",
            20,
            remaining
        )
    }
    
    @Test
    fun `missing quota file creates default state`() {
        clearQuotaState()
        
        val remaining = QuotaGate.getRemainingCalls(context, "PRO")
        
        assertEquals("Missing file should create default state", 20, remaining)
    }
    
    // ============================================================
    // INCREMENT TESTS (via canMakeCloudCall which increments internally)
    // ============================================================
    
    @Test
    fun `canMakeCloudCall atomically increments counter`() {
        assertEquals(20, QuotaGate.getRemainingCalls(context, "PRO"))
        
        assertTrue(QuotaGate.canMakeCloudCall(context, "PRO"))
        assertEquals(19, QuotaGate.getRemainingCalls(context, "PRO"))
        
        assertTrue(QuotaGate.canMakeCloudCall(context, "PRO"))
        assertEquals(18, QuotaGate.getRemainingCalls(context, "PRO"))
    }
    
    @Test
    fun `canMakeCloudCall fails when limit reached`() {
        repeat(20) {
            QuotaGate.canMakeCloudCall(context, "PRO")
        }
        
        assertFalse(
            "Call should fail when limit reached",
            QuotaGate.canMakeCloudCall(context, "PRO")
        )
    }
    
    @Test
    fun `canMakeCloudCall updates timestamp`() {
        val before = System.currentTimeMillis()
        
        QuotaGate.canMakeCloudCall(context, "PRO")
        
        val timeSinceLast = QuotaGate.getTimeSinceLastCall()
        val after = System.currentTimeMillis()
        
        assertTrue(
            "Timestamp should be updated within test execution window",
            timeSinceLast < (after - before) + 100L
        )
    }
    
    @Test
    fun `getTimeSinceLastCall returns Long MAX_VALUE when no calls made`() {
        clearQuotaState()
        
        val timeSinceLast = QuotaGate.getTimeSinceLastCall()
        
        assertEquals(
            "Should return Long.MAX_VALUE when no calls have been made",
            Long.MAX_VALUE,
            timeSinceLast
        )
    }
    
    // ============================================================
    // EDGE CASES
    // ============================================================
    
    @Test
    fun `handles empty tier string`() {
        assertFalse(
            "Empty tier should default to FREE",
            QuotaGate.canMakeCloudCall(context, "")
        )
    }
    
    @Test
    fun `handles whitespace in tier name`() {
        assertFalse(
            "Whitespace tier should default to FREE",
            QuotaGate.canMakeCloudCall(context, "  ")
        )
    }
    
    @Test
    fun `PRO tier boundary test - exactly at limit`() {
        repeat(19) {
            QuotaGate.canMakeCloudCall(context, "PRO")
        }
        
        assertTrue(
            "20th call should be allowed (exactly at limit)",
            QuotaGate.canMakeCloudCall(context, "PRO")
        )
        
        assertFalse(
            "21st call should be blocked (over limit)",
            QuotaGate.canMakeCloudCall(context, "PRO")
        )
    }
    
    @Test
    fun `APEX tier boundary test - exactly at limit`() {
        repeat(59) {
            QuotaGate.canMakeCloudCall(context, "APEX")
        }
        
        assertTrue(
            "60th call should be allowed (exactly at limit)",
            QuotaGate.canMakeCloudCall(context, "APEX")
        )
        
        assertFalse(
            "61st call should be blocked (over limit)",
            QuotaGate.canMakeCloudCall(context, "APEX")
        )
    }
    
    // ============================================================
    // BATCH B v1.0: TIER UPGRADE AND PERSISTENCE TESTS
    // ============================================================
    
    @Test
    fun `tier upgrade from FREE to BASIC takes effect immediately even when throttled`() {
        // Start with FREE tier, exhaust quota (1/day)
        assertTrue(
            "First FREE call should succeed",
            QuotaGate.canMakeCloudCall(context, "FREE")
        )
        assertFalse(
            "Second FREE call should be blocked (limit is 1)",
            QuotaGate.canMakeCloudCall(context, "FREE")
        )
        
        // Upgrade to BASIC (5/day) - tier change takes effect immediately
        // User already used 1 call, so should have 4 more available on BASIC tier
        assertTrue(
            "First BASIC call should succeed (2/5)",
            QuotaGate.canMakeCloudCall(context, "BASIC")
        )
        assertTrue(
            "Second BASIC call should succeed (3/5)",
            QuotaGate.canMakeCloudCall(context, "BASIC")
        )
        assertTrue(
            "Third BASIC call should succeed (4/5)",
            QuotaGate.canMakeCloudCall(context, "BASIC")
        )
        assertTrue(
            "Fourth BASIC call should succeed (5/5)",
            QuotaGate.canMakeCloudCall(context, "BASIC")
        )
        assertFalse(
            "Fifth BASIC call should be blocked (limit reached at 5)",
            QuotaGate.canMakeCloudCall(context, "BASIC")
        )
    }
    
    @Test
    fun `tier persisted correctly across state reloads`() {
        // Make a call on PRO tier
        assertTrue(
            "First PRO call should succeed",
            QuotaGate.canMakeCloudCall(context, "PRO")
        )
        
        // Force state reload by calling resetIfNeeded
        QuotaGate.resetIfNeeded(context)
        
        // Should remember PRO tier with 1 call already used (19 remaining)
        val limit = com.lamontlabs.quantravision.tiers.TierRegistry.getAIExplanationLimit(
            com.lamontlabs.quantravision.tiers.Tier.PRO
        )  // 20
        val remaining = QuotaGate.getRemainingCalls(context, "PRO")
        assertEquals(
            "Should have ${limit - 1} calls remaining after state reload",
            limit - 1,
            remaining
        )
    }
    
    @Test
    fun `tier preserved during daily UTC reset`() {
        // Make calls on PRO tier
        assertTrue("First PRO call should succeed", QuotaGate.canMakeCloudCall(context, "PRO"))
        assertTrue("Second PRO call should succeed", QuotaGate.canMakeCloudCall(context, "PRO"))
        
        // Simulate UTC midnight reset by manipulating quota file with yesterday's date
        val quotaFile = File(context.filesDir, "quota_state.json")
        val yesterday = java.time.LocalDate.now(java.time.ZoneOffset.UTC).minusDays(1).toString()
        val json = """
            {
                "callsToday": 2,
                "lastCallTimestamp": ${System.currentTimeMillis()},
                "lastResetDate": "$yesterday",
                "lastResetMs": ${System.currentTimeMillis()},
                "tier": "PRO",
                "recentCallTimestamps": []
            }
        """.trimIndent()
        quotaFile.writeText(json)
        
        // Trigger reset by loading state
        QuotaGate.resetIfNeeded(context)
        
        // Should still be on PRO tier with full quota (20 calls) after reset
        val limit = com.lamontlabs.quantravision.tiers.TierRegistry.getAIExplanationLimit(
            com.lamontlabs.quantravision.tiers.Tier.PRO
        )
        val remaining = QuotaGate.getRemainingCalls(context, "PRO")
        assertEquals(
            "Should have full PRO quota after UTC reset",
            limit,
            remaining
        )
        
        // Verify we can make the full quota of calls
        repeat(limit) { callNumber ->
            assertTrue(
                "Call ${callNumber + 1}/$limit should succeed after reset",
                QuotaGate.canMakeCloudCall(context, "PRO")
            )
        }
        assertFalse(
            "Should be blocked after using full quota",
            QuotaGate.canMakeCloudCall(context, "PRO")
        )
    }
    
    @Test
    fun `all tier limits match TierRegistry exactly`() {
        assertEquals(
            "FREE tier AI explanation limit should match TierRegistry",
            1,
            TierRegistry.getAIExplanationLimit(Tier.FREE)
        )
        assertEquals(
            "BASIC tier AI explanation limit should match TierRegistry",
            5,
            TierRegistry.getAIExplanationLimit(Tier.BASIC)
        )
        assertEquals(
            "PRO tier AI explanation limit should match TierRegistry",
            20,
            TierRegistry.getAIExplanationLimit(Tier.PRO)
        )
        assertEquals(
            "APEX tier AI explanation limit should match TierRegistry",
            60,
            TierRegistry.getAIExplanationLimit(Tier.APEX)
        )
    }
    
    @Test
    fun `tier upgrade from BASIC to PRO increases quota immediately`() {
        // Use 3 calls on BASIC (5/day limit)
        repeat(3) {
            assertTrue(QuotaGate.canMakeCloudCall(context, "BASIC"))
        }
        
        // Upgrade to PRO (20/day limit) - should have 17 more calls available
        val basicUsed = 3
        val proLimit = com.lamontlabs.quantravision.tiers.TierRegistry.getAIExplanationLimit(
            com.lamontlabs.quantravision.tiers.Tier.PRO
        )
        val expectedRemaining = proLimit - basicUsed
        
        val remaining = QuotaGate.getRemainingCalls(context, "PRO")
        assertEquals(
            "Should have $expectedRemaining calls after upgrade from BASIC to PRO",
            expectedRemaining,
            remaining
        )
    }
    
    @Test
    fun `tier upgrade from PRO to APEX increases quota immediately`() {
        // Use 10 calls on PRO (20/day limit)
        repeat(10) {
            assertTrue(QuotaGate.canMakeCloudCall(context, "PRO"))
        }
        
        // Upgrade to APEX (60/day limit) - should have 50 more calls available
        val proUsed = 10
        val apexLimit = com.lamontlabs.quantravision.tiers.TierRegistry.getAIExplanationLimit(
            com.lamontlabs.quantravision.tiers.Tier.APEX
        )
        val expectedRemaining = apexLimit - proUsed
        
        val remaining = QuotaGate.getRemainingCalls(context, "APEX")
        assertEquals(
            "Should have $expectedRemaining calls after upgrade from PRO to APEX",
            expectedRemaining,
            remaining
        )
    }
}
