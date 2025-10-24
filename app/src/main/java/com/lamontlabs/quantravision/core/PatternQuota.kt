package com.lamontlabs.quantravision.core

import android.content.Context
import android.content.SharedPreferences
import com.lamontlabs.quantravision.billing.Entitlements
import com.lamontlabs.quantravision.billing.Tier

class PatternQuota(ctx: Context) {
    private val prefs: SharedPreferences =
        ctx.getSharedPreferences("qv_quota", Context.MODE_PRIVATE)

    fun remaining(ent: Entitlements): Int {
        if (ent.tier != Tier.FREE) return Int.MAX_VALUE
        val used = prefs.getInt("used", 0)
        return (ent.maxTrialHighlights - used).coerceAtLeast(0)
    }

    fun consume(ent: Entitlements): Boolean {
        if (ent.tier != Tier.FREE) return true
        val left = remaining(ent)
        if (left <= 0) return false
        prefs.edit().putInt("used", ent.maxTrialHighlights - (left - 1)).apply()
        return true
    }

    fun reset() { prefs.edit().clear().apply() }
}
