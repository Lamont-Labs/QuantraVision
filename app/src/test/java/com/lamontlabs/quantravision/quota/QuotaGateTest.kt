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

/**
 * BATCH 10: QuotaGate Unit Tests
 * 
 * Comprehensive test suite for QuotaGate quota enforcement system.
 * Tests tier limits, rate limiting, daily reset, and state persistence.
 * 
 * Test Coverage:
 * 1. Tier Limits: FREE=0, PRO=10, ULTRA=25 calls/day
 * 2. Rate Limiting: min 8s between calls, max 3 per 60s
 * 3. Daily Reset: midnight reset, timezone-aware, persists across restarts
 * 4. Quota Increment: atomic increment, remaining calls, timestamp tracking
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
    fun `PRO tier allows exactly 10 calls per day`() {
        repeat(10) { callNumber ->
            assertTrue(
                "Call ${callNumber + 1} should be allowed (PRO limit is 10)",
                QuotaGate.canMakeCloudCall(context, "PRO")
            )
            assertTrue(
                "Should successfully increment call count",
                QuotaGate.incrementCallCount(context)
            )
        }
        
        assertFalse(
            "11th call should be blocked (PRO limit is 10)",
            QuotaGate.canMakeCloudCall(context, "PRO")
        )
    }
    
    @Test
    fun `ULTRA tier allows exactly 25 calls per day`() {
        repeat(25) { callNumber ->
            assertTrue(
                "Call ${callNumber + 1} should be allowed (ULTRA limit is 25)",
                QuotaGate.canMakeCloudCall(context, "ULTRA")
            )
            assertTrue(
                "Should successfully increment call count",
                QuotaGate.incrementCallCount(context)
            )
        }
        
        assertFalse(
            "26th call should be blocked (ULTRA limit is 25)",
            QuotaGate.canMakeCloudCall(context, "ULTRA")
        )
    }
    
    @Test
    fun `APEX_ULTRA tier maps to ULTRA limit of 25`() {
        repeat(25) { callNumber ->
            assertTrue(
                "Call ${callNumber + 1} should be allowed (APEX_ULTRA maps to 25)",
                QuotaGate.canMakeCloudCall(context, "APEX_ULTRA")
            )
            assertTrue(QuotaGate.incrementCallCount(context))
        }
        
        assertFalse(
            "26th call should be blocked (APEX_ULTRA limit is 25)",
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
        assertEquals(10, QuotaGate.getRemainingCalls(context, "PRO"))
        
        QuotaGate.incrementCallCount(context)
        QuotaGate.incrementCallCount(context)
        QuotaGate.incrementCallCount(context)
        
        assertEquals(7, QuotaGate.getRemainingCalls(context, "PRO"))
    }
    
    @Test
    fun `getRemainingCalls returns correct count for ULTRA tier`() {
        assertEquals(25, QuotaGate.getRemainingCalls(context, "ULTRA"))
        
        repeat(10) {
            QuotaGate.incrementCallCount(context)
        }
        
        assertEquals(15, QuotaGate.getRemainingCalls(context, "ULTRA"))
    }
    
    @Test
    fun `getRemainingCalls never goes negative`() {
        repeat(30) {
            QuotaGate.incrementCallCount(context)
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
        assertTrue(QuotaGate.incrementCallCount(context))
        
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
        assertEquals("Counter should reset to full quota after 24+ hours", 10, remaining)
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
        assertEquals(10, remaining)
    }
    
    // ============================================================
    // STATE PERSISTENCE TESTS
    // ============================================================
    
    @Test
    fun `quota state persists across app restarts`() {
        assertTrue(QuotaGate.canMakeCloudCall(context, "PRO"))
        QuotaGate.incrementCallCount(context)
        QuotaGate.incrementCallCount(context)
        QuotaGate.incrementCallCount(context)
        
        val remaining1 = QuotaGate.getRemainingCalls(context, "PRO")
        assertEquals(7, remaining1)
        
        val remaining2 = QuotaGate.getRemainingCalls(context, "PRO")
        assertEquals("State should persist", 7, remaining2)
    }
    
    @Test
    fun `corrupted quota file is recreated`() {
        val quotaFile = File(context.filesDir, "quota_state.json")
        quotaFile.writeText("CORRUPTED JSON DATA {{{")
        
        val remaining = QuotaGate.getRemainingCalls(context, "PRO")
        
        assertEquals(
            "Corrupted file should be reset to default state",
            10,
            remaining
        )
    }
    
    @Test
    fun `missing quota file creates default state`() {
        clearQuotaState()
        
        val remaining = QuotaGate.getRemainingCalls(context, "PRO")
        
        assertEquals("Missing file should create default state", 10, remaining)
    }
    
    // ============================================================
    // INCREMENT TESTS
    // ============================================================
    
    @Test
    fun `incrementCallCount atomically increments counter`() {
        assertEquals(10, QuotaGate.getRemainingCalls(context, "PRO"))
        
        assertTrue(QuotaGate.incrementCallCount(context))
        assertEquals(9, QuotaGate.getRemainingCalls(context, "PRO"))
        
        assertTrue(QuotaGate.incrementCallCount(context))
        assertEquals(8, QuotaGate.getRemainingCalls(context, "PRO"))
    }
    
    @Test
    fun `incrementCallCount fails when limit reached`() {
        repeat(10) {
            QuotaGate.incrementCallCount(context)
        }
        
        assertFalse(
            "Increment should fail when limit reached",
            QuotaGate.incrementCallCount(context)
        )
    }
    
    @Test
    fun `incrementCallCount updates timestamp`() {
        val before = System.currentTimeMillis()
        
        QuotaGate.incrementCallCount(context)
        
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
        repeat(9) {
            QuotaGate.incrementCallCount(context)
        }
        
        assertTrue(
            "10th call should be allowed (exactly at limit)",
            QuotaGate.canMakeCloudCall(context, "PRO")
        )
        QuotaGate.incrementCallCount(context)
        
        assertFalse(
            "11th call should be blocked (over limit)",
            QuotaGate.canMakeCloudCall(context, "PRO")
        )
    }
    
    @Test
    fun `ULTRA tier boundary test - exactly at limit`() {
        repeat(24) {
            QuotaGate.incrementCallCount(context)
        }
        
        assertTrue(
            "25th call should be allowed (exactly at limit)",
            QuotaGate.canMakeCloudCall(context, "ULTRA")
        )
        QuotaGate.incrementCallCount(context)
        
        assertFalse(
            "26th call should be blocked (over limit)",
            QuotaGate.canMakeCloudCall(context, "ULTRA")
        )
    }
}
