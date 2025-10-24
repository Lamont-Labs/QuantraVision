package com.lamontlabs.quantravision.licensing

import android.content.Context
import com.lamontlabs.quantravision.billing.BillingManager.Tier

class LicenseManager(private val context: Context) {
  private val prefs = context.getSharedPreferences("quantravision_prefs", Context.MODE_PRIVATE)

  fun tier(): Tier = Tier.valueOf(prefs.getString("tier", "FREE")!!)
  fun setTier(t: Tier) { prefs.edit().putString("tier", t.name).apply() }

  // Free gating: 5 highlights total
  fun incrementHighlight(): Boolean {
    if (tier() != Tier.FREE) return true
    val used = prefs.getInt("free_highlights", 0)
    if (used >= 5) return false
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
    private val FREE_SET = setOf("bull_flag","head_shoulders")
    private val STANDARD_SET = setOf(
      "bull_flag","bear_flag","head_shoulders","inverse_hs",
      "double_top","double_bottom","ascending_triangle","descending_triangle"
    )
  }
}
