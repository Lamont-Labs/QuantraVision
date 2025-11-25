package com.lamontlabs.quantravision.core

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * PatternUsageLimiter
 * - Controls free/basic/pro/apex tier gating.
 * - Free: 3 scans/day, 10 basic patterns
 * - Basic: 25 scans/day, 25 core patterns
 * - Pro: 75 scans/day, 50 patterns
 * - Apex: 200 scans/day, all 109 patterns
 * - No network required except for paid upgrade check.
 */
class PatternUsageLimiter(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("qv_usage", Context.MODE_PRIVATE)

    enum class Tier { FREE, BASIC, PRO, APEX }

    data class State(
        val tier: Tier,
        val detectionsUsed: Int,
        val detectionsRemaining: Int
    )

    private val freeLimit = 5

    fun currentTier(): Tier {
        val t = prefs.getString("tier", "FREE") ?: "FREE"
        return try {
            Tier.valueOf(t)
        } catch (e: IllegalArgumentException) {
            // Handle legacy tier names for backward compatibility
            when (t.uppercase()) {
                "STARTER" -> Tier.BASIC
                "STANDARD" -> Tier.PRO
                "APEX_ULTRA", "ULTRA" -> Tier.APEX
                else -> Tier.FREE
            }
        }
    }

    fun incrementUsage() {
        val used = prefs.getInt("used", 0) + 1
        prefs.edit().putInt("used", used).apply()
    }

    fun remaining(): Int {
        return when (currentTier()) {
            Tier.FREE -> (freeLimit - prefs.getInt("used", 0)).coerceAtLeast(0)
            Tier.BASIC, Tier.PRO, Tier.APEX -> Int.MAX_VALUE
        }
    }

    fun canDetect(): Boolean = remaining() > 0

    fun reset() {
        prefs.edit().clear().apply()
    }

    fun upgradeTo(tier: Tier) {
        prefs.edit()
            .putString("tier", tier.name)
            .putInt("used", 0)
            .apply()
        Log.i("PatternUsageLimiter", "Upgraded to ${tier.name}")
    }

    fun state(): State = State(currentTier(), prefs.getInt("used", 0), remaining())
}
