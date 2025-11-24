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
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        clearScanState()
    }
    
    private fun clearScanState() {
        try {
            val prefs = getSecurePrefs()
            prefs?.edit()?.clear()?.apply()
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
    
    private fun getSecurePrefs(): android.content.SharedPreferences? {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                "qv_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun setTier(tier: String) {
        val prefs = getSecurePrefs()
        prefs?.edit()?.putString("qv_unlocked_tier", tier)?.apply()
    }
    
    // ============================================================
    // FREE TIER TESTS
    // ============================================================
    
    @Test
    fun `FREE tier allows 3 scans per day`() {
        setTier("FREE")
        
        assertTrue("First scan should succeed", ScanQuota.recordScan(context))
        assertTrue("Second scan should succeed", ScanQuota.recordScan(context))
        assertTrue("Third scan should succeed", ScanQuota.recordScan(context))
        assertFalse("Fourth scan should fail (limit is 3)", ScanQuota.recordScan(context))
    }
    
    @Test
    fun `FREE tier canScan returns correct value`() {
        setTier("FREE")
        
        assertTrue(ScanQuota.canScan(context))
        ScanQuota.recordScan(context)
        
        assertTrue(ScanQuota.canScan(context))
        ScanQuota.recordScan(context)
        
        assertTrue(ScanQuota.canScan(context))
        ScanQuota.recordScan(context)
        
        assertFalse("Should return false after 3 scans", ScanQuota.canScan(context))
    }
    
    @Test
    fun `FREE tier getRemainingScans returns correct count`() {
        setTier("FREE")
        
        assertEquals(3, ScanQuota.getRemainingScans(context))
        ScanQuota.recordScan(context)
        assertEquals(2, ScanQuota.getRemainingScans(context))
        ScanQuota.recordScan(context)
        assertEquals(1, ScanQuota.getRemainingScans(context))
        ScanQuota.recordScan(context)
        assertEquals(0, ScanQuota.getRemainingScans(context))
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
                ScanQuota.recordScan(context)
            )
        }
        assertFalse(
            "Scan ${limit + 1} should fail (limit is $limit)",
            ScanQuota.recordScan(context)
        )
    }
    
    @Test
    fun `BASIC tier getRemainingScans returns correct count`() {
        setTier("BASIC")
        
        val limit = TierRegistry.getScanLimit(Tier.BASIC)
        assertEquals(limit, ScanQuota.getRemainingScans(context))
        
        repeat(10) {
            ScanQuota.recordScan(context)
        }
        
        assertEquals(limit - 10, ScanQuota.getRemainingScans(context))
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
                ScanQuota.recordScan(context)
            )
        }
        assertFalse(
            "Scan ${limit + 1} should fail (limit is $limit)",
            ScanQuota.recordScan(context)
        )
    }
    
    @Test
    fun `PRO tier getRemainingScans returns correct count`() {
        setTier("PRO")
        
        val limit = TierRegistry.getScanLimit(Tier.PRO)
        assertEquals(limit, ScanQuota.getRemainingScans(context))
        
        repeat(25) {
            ScanQuota.recordScan(context)
        }
        
        assertEquals(limit - 25, ScanQuota.getRemainingScans(context))
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
                ScanQuota.recordScan(context)
            )
        }
        assertFalse(
            "Scan ${limit + 1} should fail (limit is $limit)",
            ScanQuota.recordScan(context)
        )
    }
    
    @Test
    fun `APEX tier getRemainingScans returns correct count`() {
        setTier("APEX")
        
        val limit = TierRegistry.getScanLimit(Tier.APEX)
        assertEquals(limit, ScanQuota.getRemainingScans(context))
        
        repeat(50) {
            ScanQuota.recordScan(context)
        }
        
        assertEquals(limit - 50, ScanQuota.getRemainingScans(context))
    }
    
    // ============================================================
    // UTC RESET TESTS
    // ============================================================
    
    @Test
    fun `scan quota resets at 00:00 UTC`() {
        setTier("FREE")
        
        // Use all scans
        repeat(3) {
            ScanQuota.recordScan(context)
        }
        assertFalse("Should be blocked after 3 scans", ScanQuota.canScan(context))
        
        // Simulate UTC reset by setting yesterday's date
        val prefs = getSecurePrefs()
        val yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1).toString()
        prefs?.edit()?.putString("qv_scan_date", yesterday)?.apply()
        
        // Should have 3 scans again after reset
        assertTrue("Should have scans available after UTC reset", ScanQuota.canScan(context))
        assertEquals("Should have full quota after reset", 3, ScanQuota.getRemainingScans(context))
    }
    
    @Test
    fun `quota resets at midnight but preserves tier`() {
        setTier("PRO")
        
        // Use some scans
        repeat(10) {
            ScanQuota.recordScan(context)
        }
        
        val preLimitRemaining = ScanQuota.getRemainingScans(context)
        val preLimit = ScanQuota.getTierLimit(context)
        assertEquals("Should be on PRO tier", 75, preLimit)
        assertEquals("Should have 65 scans remaining", 65, preLimitRemaining)
        
        // Simulate UTC reset
        val prefs = getSecurePrefs()
        val yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1).toString()
        prefs?.edit()?.putString("qv_scan_date", yesterday)?.apply()
        
        // Should reset to full PRO quota
        val postLimit = ScanQuota.getTierLimit(context)
        val postLimitRemaining = ScanQuota.getRemainingScans(context)
        
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
        ScanQuota.recordScan(context)
        ScanQuota.recordScan(context)
        assertEquals("Should have 1 scan remaining on FREE", 1, ScanQuota.getRemainingScans(context))
        
        // Upgrade to BASIC (25 scans/day)
        setTier("BASIC")
        
        // Should now have 23 scans remaining (25 - 2 used)
        val remaining = ScanQuota.getRemainingScans(context)
        assertEquals("Should have 23 scans remaining after upgrade", 23, remaining)
    }
    
    @Test
    fun `tier upgrade from BASIC to PRO increases quota immediately`() {
        setTier("BASIC")
        
        // Use 10 of 25 BASIC scans
        repeat(10) {
            ScanQuota.recordScan(context)
        }
        assertEquals("Should have 15 scans remaining on BASIC", 15, ScanQuota.getRemainingScans(context))
        
        // Upgrade to PRO (75 scans/day)
        setTier("PRO")
        
        // Should now have 65 scans remaining (75 - 10 used)
        val remaining = ScanQuota.getRemainingScans(context)
        assertEquals("Should have 65 scans remaining after upgrade", 65, remaining)
    }
    
    @Test
    fun `tier upgrade from PRO to APEX increases quota immediately`() {
        setTier("PRO")
        
        // Use 30 of 75 PRO scans
        repeat(30) {
            ScanQuota.recordScan(context)
        }
        assertEquals("Should have 45 scans remaining on PRO", 45, ScanQuota.getRemainingScans(context))
        
        // Upgrade to APEX (200 scans/day)
        setTier("APEX")
        
        // Should now have 170 scans remaining (200 - 30 used)
        val remaining = ScanQuota.getRemainingScans(context)
        assertEquals("Should have 170 scans remaining after upgrade", 170, remaining)
    }
    
    // ============================================================
    // CONSISTENCY TESTS
    // ============================================================
    
    @Test
    fun `all tier limits match TierRegistry exactly`() {
        setTier("FREE")
        assertEquals(3, ScanQuota.getTierLimit(context))
        
        setTier("BASIC")
        assertEquals(25, ScanQuota.getTierLimit(context))
        
        setTier("PRO")
        assertEquals(75, ScanQuota.getTierLimit(context))
        
        setTier("APEX")
        assertEquals(200, ScanQuota.getTierLimit(context))
    }
    
    @Test
    fun `recordScan returns false when quota exceeded`() {
        setTier("FREE")
        
        assertTrue(ScanQuota.recordScan(context))
        assertTrue(ScanQuota.recordScan(context))
        assertTrue(ScanQuota.recordScan(context))
        assertFalse("recordScan should return false when quota exceeded", ScanQuota.recordScan(context))
        assertFalse("recordScan should continue returning false", ScanQuota.recordScan(context))
    }
    
    @Test
    fun `getRemainingScans never goes negative`() {
        setTier("FREE")
        
        repeat(10) {
            ScanQuota.recordScan(context)
        }
        
        val remaining = ScanQuota.getRemainingScans(context)
        assertTrue("Remaining scans should not be negative", remaining >= 0)
        assertEquals("Should be exactly 0 when over quota", 0, remaining)
    }
    
    @Test
    fun `unknown tier defaults to FREE tier`() {
        setTier("UNKNOWN_TIER")
        
        val limit = ScanQuota.getTierLimit(context)
        assertEquals("Unknown tier should default to FREE (3 scans)", 3, limit)
    }
}
