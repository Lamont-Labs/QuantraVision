package com.lamontlabs.quantravision.detection

import android.content.Context
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.licensing.ProFeatureGate
import com.lamontlabs.quantravision.quota.HighlightQuota

/**
 * HighlightGate
 * Enforces "first 3–5 highlights free, then pay" policy.
 * If Pro is active -> pass-through. Else -> allow until quota is exhausted.
 */
object HighlightGate {

    /** Call before rendering each highlight. Increments counter on allowed. */
    fun allowAndCount(context: Context): Boolean {
        if (ProFeatureGate.isActive(context)) return true
        
        // Check bonus highlights first (from achievements)
        val bonusHighlights = com.lamontlabs.quantravision.gamification.BonusHighlights.available(context)
        if (bonusHighlights > 0) {
            com.lamontlabs.quantravision.gamification.BonusHighlights.use(context, 1)
            return true
        }
        
        if (HighlightQuota.exhausted(context)) return false
        HighlightQuota.increment(context)
        return true
    }

    /** Convenience: filter a list of matches according to gating. */
    fun filterForRender(context: Context, matches: List<PatternMatch>): List<PatternMatch> {
        if (ProFeatureGate.isActive(context)) return matches
        
        // Include bonus highlights in available count
        val bonusHighlights = com.lamontlabs.quantravision.gamification.BonusHighlights.available(context)
        val quotaRemaining = HighlightQuota.remaining(context)
        val totalRemaining = bonusHighlights + quotaRemaining
        
        return if (totalRemaining <= 0) emptyList() else matches.take(totalRemaining)
    }
}
