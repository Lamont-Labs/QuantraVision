package com.lamontlabs.quantravision.core

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * PatternUsageLimiter
 * - Controls free/standard/pro tier gating.
 * - Free users: 5 detections max
 * - Standard: unlimited for half pattern library
 * - Pro: full pattern library unlocked
 * - No network required except for paid upgrade check.
 */
class PatternUsageLimiter(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("qv_usage", Context.MODE_PRIVATE)

    enum class Tier { FREE, STANDARD, PRO }

    data class State(
        val tier: Tier,
        val detectionsUsed: Int,
        val detectionsRemaining: Int
    )

    private val freeLimit = 5
    private val standardLimit = 9999

    fun currentTier(): Tier {
        val t = prefs.getString("tier", "FREE") ?: "FREE"
        return Tier.valueOf(t)
    }

    fun incrementUsage() {
        val used = prefs.getInt("used", 0) + 1
        prefs.edit().putInt("used", used).apply()
    }

    fun remaining(): Int {
        return when (currentTier()) {
            Tier.FREE -> (freeLimit - prefs.getInt("used", 0)).coerceAtLeast(0)
            Tier.STANDARD, Tier.PRO -> Int.MAX_VALUE
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
