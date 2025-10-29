package com.lamontlabs.quantravision.licensing

import android.content.Context
import com.lamontlabs.quantravision.billing.BillingManager.Tier

class LicenseManager(private val context: Context) {
  private val prefs = context.getSharedPreferences("quantravision_prefs", Context.MODE_PRIVATE)

  fun tier(): Tier = Tier.valueOf(prefs.getString("tier", "FREE") ?: "FREE")
  fun setTier(t: Tier) { prefs.edit().putString("tier", t.name).apply() }

  // Free gating: 2 highlights per day (resets daily)
  fun incrementHighlight(): Boolean {
    if (tier() != Tier.FREE) return true
    
    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
    val lastDate = prefs.getString("free_highlight_date", "")
    val used = prefs.getInt("free_highlights", 0)
    
    // Reset daily quota
    if (lastDate != today) {
      prefs.edit()
        .putString("free_highlight_date", today)
        .putInt("free_highlights", 0)
        .apply()
      return incrementHighlight()
    }
    
    if (used >= 2) return false
    prefs.edit().putInt("free_highlights", used + 1).apply()
    return true
  }

  // Pattern availability by tier
  fun patternAllowed(patternId: String): Boolean {
    return when (tier()) {
      Tier.PRO -> true
      Tier.STANDARD -> STANDARD_SET.contains(patternId)
      Tier.FREE -> FREE_SET.contains(patternId)
    }
  }

  companion object {
    // keep short symbolic IDs
    private val FREE_SET = setOf("doji")  // Only 1 pattern for free tier
    private val STANDARD_SET = setOf(
      // 30 patterns for Standard ($19.99)
      "doji","bull_flag","bear_flag","head_shoulders","inverse_hs",
      "double_top","double_bottom","ascending_triangle","descending_triangle",
      "symmetrical_triangle","rising_wedge","falling_wedge","pennant",
      "cup_handle","rounding_bottom","triple_top","triple_bottom",
      "rectangle","channel","gap_up","gap_down","island_reversal",
      "exhaustion_gap","breakaway_gap","measuring_gap","three_white_soldiers",
      "three_black_crows","morning_star","evening_star","shooting_star"
    )
  }
}
