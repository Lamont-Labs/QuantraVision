/*
 * Copyright (c) 2025 Lamont Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lamontlabs.quantravision.quota

import android.content.Context
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.test.core.app.ApplicationProvider
import com.lamontlabs.quantravision.tiers.Tier
import com.lamontlabs.quantravision.pipeline.QuotaGate as PipelineQuotaGate
import com.lamontlabs.quantravision.entitlements.EntitlementManager
import com.lamontlabs.quantravision.tiers.TierRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * BATCH B v1.0: ScanQuota Unit Tests
 * 
 * Comprehensive test suite for ScanQuota enforcement system.
 * Tests tier-based scan limits, daily reset, and quota tracking.
 * 
 * Test Coverage:
 * - FREE: 3 scans/day
 * - BASIC: 25 scans/day
 * - PRO: 75 scans/day
 * - APEX: 200 scans/day
 * - UTC reset behavior
 * - Tier upgrade scenarios
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class ScanQuotaTest {
    
    private lateinit var context: Context
    private lateinit var quota: PipelineQuotaGate
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        clearScanState()
        EntitlementManager.initialize(context)
        EntitlementManager.updateTierFromString("FREE")
        quota = PipelineQuotaGate(context, getSecurePrefs()!!)
    }
    
    private fun clearScanState() {
        try {
            val prefs = getSecurePrefs()
            prefs?.edit()?.clear()?.apply()
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
    
    // Quota arithmetic uses ordinary preferences in JVM units; production storage
    // remains encrypted and its unavailability is separately tested as fail-closed.
    private fun getSecurePrefs(): android.content.SharedPreferences? =
        context.getSharedPreferences("scan-ledger-test", Context.MODE_PRIVATE)

    private fun setTier(tier: String) { EntitlementManager.updateTierFromString(tier) }

    // ============================================================
    // FREE TIER TESTS
    // ============================================================
    
    @Test
    fun `FREE tier allows 3 scans per day`() {
        setTier("FREE")
        
        assertTrue("First scan should succeed", quota.recordScan())
        assertTrue("Second scan should succeed", quota.recordScan())
        assertTrue("Third scan should succeed", quota.recordScan())
        assertFalse("Fourth scan should fail (limit is 3)", quota.recordScan())
    }
    
    @Test
    fun `FREE tier canScan returns correct value`() {
        setTier("FREE")
        
        assertTrue(quota.checkScanQuota().allowed)
        quota.recordScan()
        
        assertTrue(quota.checkScanQuota().allowed)
        quota.recordScan()
        
        assertTrue(quota.checkScanQuota().allowed)
        quota.recordScan()
        
        assertFalse("Should return false after 3 scans", quota.checkScanQuota().allowed)
    }
    
    @Test
    fun `FREE tier getRemainingScans returns correct count`() {
        setTier("FREE")
        
        assertEquals(3, quota.getRemainingScan())
        quota.recordScan()
        assertEquals(2, quota.getRemainingScan())
        quota.recordScan()
        assertEquals(1, quota.getRemainingScan())
        quota.recordScan()
        assertEquals(0, quota.getRemainingScan())
    }
    
    // ============================================================
    // BASIC TIER TESTS
    // ============================================================
    
    @Test
    fun `BASIC tier allows 25 scans per day`() {
        setTier("BASIC")
        
        val limit = TierRegistry.getScanLimit(Tier.BASIC)  // 25
        for (i in 1..limit) {
            assertTrue(
                "Scan $i/$limit should succeed",
                quota.recordScan()
            )
        }
        assertFalse(
            "Scan ${limit + 1} should fail (limit is $limit)",
            quota.recordScan()
        )
    }
    
    @Test
    fun `BASIC tier getRemainingScans returns correct count`() {
        setTier("BASIC")
        
        val limit = TierRegistry.getScanLimit(Tier.BASIC)
        assertEquals(limit, quota.getRemainingScan())
        
        repeat(10) {
            quota.recordScan()
        }
        
        assertEquals(limit - 10, quota.getRemainingScan())
    }
    
    // ============================================================
    // PRO TIER TESTS
    // ============================================================
    
    @Test
    fun `PRO tier allows 75 scans per day`() {
        setTier("PRO")
        
        val limit = TierRegistry.getScanLimit(Tier.PRO)  // 75
        for (i in 1..limit) {
            assertTrue(
                "Scan $i/$limit should succeed",
                quota.recordScan()
            )
        }
        assertFalse(
            "Scan ${limit + 1} should fail (limit is $limit)",
            quota.recordScan()
        )
    }
    
    @Test
    fun `PRO tier getRemainingScans returns correct count`() {
        setTier("PRO")
        
        val limit = TierRegistry.getScanLimit(Tier.PRO)
        assertEquals(limit, quota.getRemainingScan())
        
        repeat(25) {
            quota.recordScan()
        }
        
        assertEquals(limit - 25, quota.getRemainingScan())
    }
    
    // ============================================================
    // APEX TIER TESTS
    // ============================================================
    
    @Test
    fun `APEX tier allows 200 scans per day`() {
        setTier("APEX")
        
        val limit = TierRegistry.getScanLimit(Tier.APEX)  // 200
        for (i in 1..limit) {
            assertTrue(
                "Scan $i/$limit should succeed",
                quota.recordScan()
            )
        }
        assertFalse(
            "Scan ${limit + 1} should fail (limit is $limit)",
            quota.recordScan()
        )
    }
    
    @Test
    fun `APEX tier getRemainingScans returns correct count`() {
        setTier("APEX")
        
        val limit = TierRegistry.getScanLimit(Tier.APEX)
        assertEquals(limit, quota.getRemainingScan())
        
        repeat(50) {
            quota.recordScan()
        }
        
        assertEquals(limit - 50, quota.getRemainingScan())
    }
    
    // ============================================================
    // UTC RESET TESTS
    // ============================================================
    
    @Test
    fun `scan quota resets at midnight UTC`() {
        setTier("FREE")
        
        // Use all scans
        repeat(3) {
            quota.recordScan()
        }
        assertFalse("Should be blocked after 3 scans", quota.checkScanQuota().allowed)
        
        // Simulate UTC reset by setting yesterday's date
        val prefs = getSecurePrefs()
        val yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1).toString()
        prefs?.edit()?.putString("last_reset_date", yesterday)?.apply()
        
        // Should have 3 scans again after reset
        assertTrue("Should have scans available after UTC reset", quota.checkScanQuota().allowed)
        assertEquals("Should have full quota after reset", 3, quota.getRemainingScan())
    }
    
    @Test
    fun `quota resets at midnight but preserves tier`() {
        setTier("PRO")
        
        // Use some scans
        repeat(10) {
            quota.recordScan()
        }
        
        val preLimitRemaining = quota.getRemainingScan()
        val preLimit = quota.checkScanQuota().limit
        assertEquals("Should be on PRO tier", 75, preLimit)
        assertEquals("Should have 65 scans remaining", 65, preLimitRemaining)
        
        // Simulate UTC reset
        val prefs = getSecurePrefs()
        val yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1).toString()
        prefs?.edit()?.putString("last_reset_date", yesterday)?.apply()
        
        // Should reset to full PRO quota
        val postLimit = quota.checkScanQuota().limit
        val postLimitRemaining = quota.getRemainingScan()
        
        assertEquals("Should still be on PRO tier after reset", 75, postLimit)
        assertEquals("Should have full quota after reset", 75, postLimitRemaining)
    }
    
    // ============================================================
    // TIER UPGRADE TESTS
    // ============================================================
    
    @Test
    fun `tier upgrade from FREE to BASIC increases quota immediately`() {
        setTier("FREE")
        
        // Use 2 of 3 FREE scans
        quota.recordScan()
        quota.recordScan()
        assertEquals("Should have 1 scan remaining on FREE", 1, quota.getRemainingScan())
        
        // Upgrade to BASIC (25 scans/day)
        setTier("BASIC")
        
        // Should now have 23 scans remaining (25 - 2 used)
        val remaining = quota.getRemainingScan()
        assertEquals("Should have 23 scans remaining after upgrade", 23, remaining)
    }
    
    @Test
    fun `tier upgrade from BASIC to PRO increases quota immediately`() {
        setTier("BASIC")
        
        // Use 10 of 25 BASIC scans
        repeat(10) {
            quota.recordScan()
        }
        assertEquals("Should have 15 scans remaining on BASIC", 15, quota.getRemainingScan())
        
        // Upgrade to PRO (75 scans/day)
        setTier("PRO")
        
        // Should now have 65 scans remaining (75 - 10 used)
        val remaining = quota.getRemainingScan()
        assertEquals("Should have 65 scans remaining after upgrade", 65, remaining)
    }
    
    @Test
    fun `tier upgrade from PRO to APEX increases quota immediately`() {
        setTier("PRO")
        
        // Use 30 of 75 PRO scans
        repeat(30) {
            quota.recordScan()
        }
        assertEquals("Should have 45 scans remaining on PRO", 45, quota.getRemainingScan())
        
        // Upgrade to APEX (200 scans/day)
        setTier("APEX")
        
        // Should now have 170 scans remaining (200 - 30 used)
        val remaining = quota.getRemainingScan()
        assertEquals("Should have 170 scans remaining after upgrade", 170, remaining)
    }
    
    // ============================================================
    // CONSISTENCY TESTS
    // ============================================================
    
    @Test
    fun `all tier limits match TierRegistry exactly`() {
        setTier("FREE")
        assertEquals(3, quota.checkScanQuota().limit)
        
        setTier("BASIC")
        assertEquals(25, quota.checkScanQuota().limit)
        
        setTier("PRO")
        assertEquals(75, quota.checkScanQuota().limit)
        
        setTier("APEX")
        assertEquals(200, quota.checkScanQuota().limit)
    }
    
    @Test
    fun `recordScan returns false when quota exceeded`() {
        setTier("FREE")
        
        assertTrue(quota.recordScan())
        assertTrue(quota.recordScan())
        assertTrue(quota.recordScan())
        assertFalse("recordScan should return false when quota exceeded", quota.recordScan())
        assertFalse("recordScan should continue returning false", quota.recordScan())
    }
    
    @Test
    fun `getRemainingScans never goes negative`() {
        setTier("FREE")
        
        repeat(10) {
            quota.recordScan()
        }
        
        val remaining = quota.getRemainingScan()
        assertTrue("Remaining scans should not be negative", remaining >= 0)
        assertEquals("Should be exactly 0 when over quota", 0, remaining)
    }
    
    @Test
    fun `unknown tier defaults to FREE tier`() {
        setTier("UNKNOWN_TIER")
        
        val limit = quota.checkScanQuota().limit
        assertEquals("Unknown tier should default to FREE (3 scans)", 3, limit)
    }
}
