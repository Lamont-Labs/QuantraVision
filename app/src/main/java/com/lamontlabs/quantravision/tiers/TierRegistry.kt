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

/**
 * SINGLE SOURCE OF TRUTH for all tier limits
 * Last updated: November 24, 2025 - Batch B v1.0
 */
object TierRegistry {
    
    private val limits = mapOf(
        Tier.FREE to TierLimits(
            scansPerDay = 3,
            aiExplanationsPerDay = 1,
            savedSummaries = 0,
            batchModeEnabled = false,
            apexFeatures = "basic_only"
        ),
        Tier.BASIC to TierLimits(
            scansPerDay = 25,
            aiExplanationsPerDay = 5,
            savedSummaries = 5,
            batchModeEnabled = false,
            apexFeatures = "core_overlay_only"
        ),
        Tier.PRO to TierLimits(
            scansPerDay = 75,
            aiExplanationsPerDay = 20,
            savedSummaries = 20,
            batchModeEnabled = true,
            apexFeatures = "full_apex_overlay"
        ),
        Tier.APEX to TierLimits(
            scansPerDay = 200,
            aiExplanationsPerDay = 60,
            savedSummaries = 100,
            batchModeEnabled = true,
            apexFeatures = "full_apex_overlay+advanced_logic"
        )
    )
    
    fun getLimits(tier: Tier): TierLimits {
        return limits[tier] ?: limits[Tier.FREE]!!
    }
    
    fun getScanLimit(tier: Tier): Int = getLimits(tier).scansPerDay
    fun getAIExplanationLimit(tier: Tier): Int = getLimits(tier).aiExplanationsPerDay
    fun getSavedSummaryLimit(tier: Tier): Int = getLimits(tier).savedSummaries
    fun isBatchModeEnabled(tier: Tier): Boolean = getLimits(tier).batchModeEnabled
    fun getApexFeatures(tier: Tier): String = getLimits(tier).apexFeatures
}
