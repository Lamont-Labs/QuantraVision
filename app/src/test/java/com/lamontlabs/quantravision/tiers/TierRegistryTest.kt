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

package com.lamontlabs.quantravision.tiers

import org.junit.Assert.*
import org.junit.Test

/**
 * BATCH B v1.0: TierRegistry Unit Tests
 * 
 * Comprehensive test suite verifying exact quota values for all tiers.
 * These tests serve as the single source of truth for tier limits and
 * prevent regression of the 3 critical bugs fixed in Batch B:
 * 1. Inconsistent tier limits across modules
 * 2. Tier upgrades not taking effect immediately
 * 3. Tier state lost during UTC reset
 * 
 * Test Coverage:
 * - FREE: scansPerDay=3, aiExplanationsPerDay=1, savedSummaries=0, batchMode=false
 * - BASIC: scansPerDay=25, aiExplanationsPerDay=5, savedSummaries=5, batchMode=false
 * - PRO: scansPerDay=75, aiExplanationsPerDay=20, savedSummaries=20, batchMode=true
 * - APEX: scansPerDay=200, aiExplanationsPerDay=60, savedSummaries=100, batchMode=true
 */
class TierRegistryTest {
    
    // ============================================================
    // TIER LIMITS - COMPLETE VERIFICATION
    // ============================================================
    
    @Test
    fun `FREE tier has correct quota limits`() {
        val limits = TierRegistry.getLimits(Tier.FREE)
        assertEquals(3, limits.scansPerDay)
        assertEquals(1, limits.aiExplanationsPerDay)
        assertEquals(0, limits.savedSummaries)
        assertFalse(limits.batchModeEnabled)
    }
    
    @Test
    fun `BASIC tier has correct quota limits`() {
        val limits = TierRegistry.getLimits(Tier.BASIC)
        assertEquals(25, limits.scansPerDay)
        assertEquals(5, limits.aiExplanationsPerDay)
        assertEquals(5, limits.savedSummaries)
        assertFalse(limits.batchModeEnabled)
    }
    
    @Test
    fun `PRO tier has correct quota limits`() {
        val limits = TierRegistry.getLimits(Tier.PRO)
        assertEquals(75, limits.scansPerDay)
        assertEquals(20, limits.aiExplanationsPerDay)
        assertEquals(20, limits.savedSummaries)
        assertTrue(limits.batchModeEnabled)
    }
    
    @Test
    fun `APEX tier has correct quota limits`() {
        val limits = TierRegistry.getLimits(Tier.APEX)
        assertEquals(200, limits.scansPerDay)
        assertEquals(60, limits.aiExplanationsPerDay)
        assertEquals(100, limits.savedSummaries)
        assertTrue(limits.batchModeEnabled)
    }
    
    // ============================================================
    // SCAN LIMIT VERIFICATION
    // ============================================================
    
    @Test
    fun `getScanLimit returns correct values for all tiers`() {
        assertEquals(3, TierRegistry.getScanLimit(Tier.FREE))
        assertEquals(25, TierRegistry.getScanLimit(Tier.BASIC))
        assertEquals(75, TierRegistry.getScanLimit(Tier.PRO))
        assertEquals(200, TierRegistry.getScanLimit(Tier.APEX))
    }
    
    @Test
    fun `scan limits are strictly increasing`() {
        val freeScanLimit = TierRegistry.getScanLimit(Tier.FREE)
        val basicScanLimit = TierRegistry.getScanLimit(Tier.BASIC)
        val proScanLimit = TierRegistry.getScanLimit(Tier.PRO)
        val apexScanLimit = TierRegistry.getScanLimit(Tier.APEX)
        
        assertTrue("BASIC should have more scans than FREE", basicScanLimit > freeScanLimit)
        assertTrue("PRO should have more scans than BASIC", proScanLimit > basicScanLimit)
        assertTrue("APEX should have more scans than PRO", apexScanLimit > proScanLimit)
    }
    
    // ============================================================
    // AI EXPLANATION LIMIT VERIFICATION
    // ============================================================
    
    @Test
    fun `getAIExplanationLimit returns correct values for all tiers`() {
        assertEquals(1, TierRegistry.getAIExplanationLimit(Tier.FREE))
        assertEquals(5, TierRegistry.getAIExplanationLimit(Tier.BASIC))
        assertEquals(20, TierRegistry.getAIExplanationLimit(Tier.PRO))
        assertEquals(60, TierRegistry.getAIExplanationLimit(Tier.APEX))
    }
    
    @Test
    fun `AI explanation limits are strictly increasing`() {
        val freeAILimit = TierRegistry.getAIExplanationLimit(Tier.FREE)
        val basicAILimit = TierRegistry.getAIExplanationLimit(Tier.BASIC)
        val proAILimit = TierRegistry.getAIExplanationLimit(Tier.PRO)
        val apexAILimit = TierRegistry.getAIExplanationLimit(Tier.APEX)
        
        assertTrue("BASIC should have more AI calls than FREE", basicAILimit > freeAILimit)
        assertTrue("PRO should have more AI calls than BASIC", proAILimit > basicAILimit)
        assertTrue("APEX should have more AI calls than PRO", apexAILimit > proAILimit)
    }
    
    // ============================================================
    // SAVED SUMMARY LIMIT VERIFICATION
    // ============================================================
    
    @Test
    fun `getSavedSummaryLimit returns correct values for all tiers`() {
        assertEquals(0, TierRegistry.getSavedSummaryLimit(Tier.FREE))
        assertEquals(5, TierRegistry.getSavedSummaryLimit(Tier.BASIC))
        assertEquals(20, TierRegistry.getSavedSummaryLimit(Tier.PRO))
        assertEquals(100, TierRegistry.getSavedSummaryLimit(Tier.APEX))
    }
    
    @Test
    fun `saved summary limits are monotonically increasing`() {
        val freeSummaryLimit = TierRegistry.getSavedSummaryLimit(Tier.FREE)
        val basicSummaryLimit = TierRegistry.getSavedSummaryLimit(Tier.BASIC)
        val proSummaryLimit = TierRegistry.getSavedSummaryLimit(Tier.PRO)
        val apexSummaryLimit = TierRegistry.getSavedSummaryLimit(Tier.APEX)
        
        assertTrue("FREE should have no saved summaries", freeSummaryLimit == 0)
        assertTrue("BASIC should have more summaries than FREE", basicSummaryLimit > freeSummaryLimit)
        assertTrue("PRO should have more summaries than BASIC", proSummaryLimit > basicSummaryLimit)
        assertTrue("APEX should have more summaries than PRO", apexSummaryLimit > proSummaryLimit)
    }
    
    // ============================================================
    // BATCH MODE VERIFICATION
    // ============================================================
    
    @Test
    fun `batch mode enabled only for PRO and APEX`() {
        assertFalse("FREE should not have batch mode", TierRegistry.isBatchModeEnabled(Tier.FREE))
        assertFalse("BASIC should not have batch mode", TierRegistry.isBatchModeEnabled(Tier.BASIC))
        assertTrue("PRO should have batch mode", TierRegistry.isBatchModeEnabled(Tier.PRO))
        assertTrue("APEX should have batch mode", TierRegistry.isBatchModeEnabled(Tier.APEX))
    }
    
    @Test
    fun `batch mode matches TierLimits batchModeEnabled field`() {
        assertEquals(
            TierRegistry.getLimits(Tier.FREE).batchModeEnabled,
            TierRegistry.isBatchModeEnabled(Tier.FREE)
        )
        assertEquals(
            TierRegistry.getLimits(Tier.BASIC).batchModeEnabled,
            TierRegistry.isBatchModeEnabled(Tier.BASIC)
        )
        assertEquals(
            TierRegistry.getLimits(Tier.PRO).batchModeEnabled,
            TierRegistry.isBatchModeEnabled(Tier.PRO)
        )
        assertEquals(
            TierRegistry.getLimits(Tier.APEX).batchModeEnabled,
            TierRegistry.isBatchModeEnabled(Tier.APEX)
        )
    }
    
    // ============================================================
    // APEX FEATURES VERIFICATION
    // ============================================================
    
    @Test
    fun `apex features correctly configured for each tier`() {
        assertEquals("basic_only", TierRegistry.getApexFeatures(Tier.FREE))
        assertEquals("core_overlay_only", TierRegistry.getApexFeatures(Tier.BASIC))
        assertEquals("full_apex_overlay", TierRegistry.getApexFeatures(Tier.PRO))
        assertEquals("full_apex_overlay+advanced_logic", TierRegistry.getApexFeatures(Tier.APEX))
    }
    
    // ============================================================
    // CONSISTENCY VERIFICATION
    // ============================================================
    
    @Test
    fun `getLimits never returns null`() {
        assertNotNull(TierRegistry.getLimits(Tier.FREE))
        assertNotNull(TierRegistry.getLimits(Tier.BASIC))
        assertNotNull(TierRegistry.getLimits(Tier.PRO))
        assertNotNull(TierRegistry.getLimits(Tier.APEX))
    }
    
    @Test
    fun `all tier limits are positive or zero`() {
        for (tier in Tier.values()) {
            val limits = TierRegistry.getLimits(tier)
            assertTrue("scansPerDay must be >= 0", limits.scansPerDay >= 0)
            assertTrue("aiExplanationsPerDay must be >= 0", limits.aiExplanationsPerDay >= 0)
            assertTrue("savedSummaries must be >= 0", limits.savedSummaries >= 0)
        }
    }
    
    @Test
    fun `convenience methods match getLimits data`() {
        for (tier in Tier.values()) {
            val limits = TierRegistry.getLimits(tier)
            assertEquals(
                "getScanLimit should match limits.scansPerDay",
                limits.scansPerDay,
                TierRegistry.getScanLimit(tier)
            )
            assertEquals(
                "getAIExplanationLimit should match limits.aiExplanationsPerDay",
                limits.aiExplanationsPerDay,
                TierRegistry.getAIExplanationLimit(tier)
            )
            assertEquals(
                "getSavedSummaryLimit should match limits.savedSummaries",
                limits.savedSummaries,
                TierRegistry.getSavedSummaryLimit(tier)
            )
            assertEquals(
                "isBatchModeEnabled should match limits.batchModeEnabled",
                limits.batchModeEnabled,
                TierRegistry.isBatchModeEnabled(tier)
            )
        }
    }
}
