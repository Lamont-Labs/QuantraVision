package com.lamontlabs.quantravision.detection

import android.content.Context
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.licensing.PatternLibraryGate
import com.lamontlabs.quantravision.licensing.BasicFeatureGate
import com.lamontlabs.quantravision.licensing.ProFeatureGate
import com.lamontlabs.quantravision.quota.HighlightQuota

/**
 * HighlightGate
 * Enforces tier-based pattern access and highlight quota.
 * 
 * Tier Pricing:
 * Free: 3 highlights/day, 10 basic patterns
 * Basic ($4.99/mo): Unlimited highlights, 25 core patterns
 * Pro ($14.99/mo): Unlimited highlights, 50 patterns
 * Apex ($29.99/mo): Unlimited highlights, all 109 patterns
 */
object HighlightGate {

    /** Call before rendering each highlight. Increments counter on allowed. */
    fun allowAndCount(context: Context): Boolean {
        // Basic/Pro/Apex get unlimited highlights
        if (BasicFeatureGate.isActive(context) || ProFeatureGate.isActive(context)) {
            return true
        }
        
        // Free tier: check bonus highlights first (from achievements)
        val bonusHighlights = com.lamontlabs.quantravision.gamification.BonusHighlights.available(context)
        if (bonusHighlights > 0) {
            com.lamontlabs.quantravision.gamification.BonusHighlights.use(context, 1)
            return true
        }
        
        // Free tier: check quota
        if (HighlightQuota.exhausted(context)) return false
        HighlightQuota.increment(context)
        return true
    }

    /** 
     * Filter matches according to tier and quota.
     * 1. First filter by pattern library (which patterns user has access to)
     * 2. Then filter by highlight quota (how many can be shown)
     */
    fun filterForRender(context: Context, matches: List<PatternMatch>): List<PatternMatch> {
        // Step 1: Filter by pattern library (tier-based)
        val tierFilteredMatches = PatternLibraryGate.filterByTier(context, matches)
        
        // Step 2: Filter by highlight quota
        // Basic/Pro/Apex get all their tier patterns, Free gets quota-limited
        if (BasicFeatureGate.isActive(context) || ProFeatureGate.isActive(context)) {
            return tierFilteredMatches // Unlimited highlights
        }
        
        // Free tier: apply quota limit
        val bonusHighlights = com.lamontlabs.quantravision.gamification.BonusHighlights.available(context)
        val quotaRemaining = HighlightQuota.remaining(context)
        val totalRemaining = bonusHighlights + quotaRemaining
        
        return if (totalRemaining <= 0) emptyList() else tierFilteredMatches.take(totalRemaining)
    }
}
